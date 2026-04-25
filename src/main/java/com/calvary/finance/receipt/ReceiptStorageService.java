package com.calvary.finance.receipt;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class ReceiptStorageService {
    private static final Set<String> IMAGE_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png"
    );
    private static final String PDF_CONTENT_TYPE = "application/pdf";

    private final ReceiptStorageProperties properties;

    public ReceiptStorageService(ReceiptStorageProperties properties) {
        this.properties = properties;
    }

    public StagedReceipt stage(MultipartFile receipt) {
        if (receipt == null || receipt.isEmpty()) {
            throw new IllegalArgumentException("Receipt file is required");
        }
        if (receipt.getSize() > properties.getMaxUploadBytes()) {
            throw new IllegalArgumentException("Receipt exceeds maximum upload size");
        }

        String contentType = normalizeContentType(receipt.getContentType());
        if (!IMAGE_CONTENT_TYPES.contains(contentType) && !PDF_CONTENT_TYPE.equals(contentType)) {
            throw new IllegalArgumentException("Receipt must be a JPEG, PNG, or PDF file");
        }

        Path relativePath = stagedRelativePath(receipt);
        Path destination = stagedRoot().resolve(relativePath).normalize();
        try {
            Files.createDirectories(destination.getParent());
            try (InputStream input = receipt.getInputStream()) {
                Files.copy(input, destination);
            }

            return StagedReceipt.builder()
                    .path(relativePath.toString())
                    .contentType(contentType)
                    .originalFilename(cleanOriginalFilename(receipt))
                    .originalSizeBytes(receipt.getSize())
                    .build();
        } catch (FileAlreadyExistsException ex) {
            throw new IllegalArgumentException("Receipt staging filename already exists", ex);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to stage receipt", ex);
        }
    }

    public StoredReceipt process(String stagedPath, String contentType) {
        Path root = stagedRoot().toAbsolutePath().normalize();
        Path stagedFile = root.resolve(stagedPath).normalize();
        if (!stagedFile.startsWith(root)) {
            throw new IllegalArgumentException("Invalid receipt path");
        }
        try {
            StoredBytes storedBytes = toStoredBytes(stagedFile, normalizeContentType(contentType));
            String sha256 = sha256(storedBytes.bytes());
            String extension = extensionFor(storedBytes.contentType());
            Path relativePath = storedRelativePath(sha256, extension);
            Path destination = storedRoot().resolve(relativePath).normalize();

            Files.createDirectories(destination.getParent());
            if (Files.notExists(destination)) {
                Files.write(destination, storedBytes.bytes());
            }

            return StoredReceipt.builder()
                    .path(relativePath.toString())
                    .contentType(storedBytes.contentType())
                    .storedSizeBytes(storedBytes.bytes().length)
                    .sha256(sha256)
                    .build();
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to process receipt", ex);
        }
    }

    public Resource processedResource(String storedPath) {
        Path root = storedRoot().toAbsolutePath().normalize();
        Path file = root.resolve(storedPath).normalize();
        if (!file.startsWith(root)) {
            throw new IllegalArgumentException("Invalid receipt path");
        }

        try {
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new IllegalArgumentException("Receipt file not found");
            }
            return resource;
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to read receipt file", ex);
        }
    }

    private StoredBytes toStoredBytes(Path stagedFile, String contentType) throws IOException {
        if (IMAGE_CONTENT_TYPES.contains(contentType)) {
            return new StoredBytes(compressImage(stagedFile), "image/jpeg");
        }
        if (PDF_CONTENT_TYPE.equals(contentType)) {
            return new StoredBytes(Files.readAllBytes(stagedFile), PDF_CONTENT_TYPE);
        }
        throw new IllegalArgumentException("Receipt must be a JPEG, PNG, or PDF file");
    }

    private byte[] compressImage(Path stagedFile) throws IOException {
        BufferedImage source = ImageIO.read(stagedFile.toFile());
        if (source == null) {
            throw new IllegalArgumentException("Receipt image could not be read");
        }

        BufferedImage resized = resize(source);
        BufferedImage rgbImage = new BufferedImage(
                resized.getWidth(),
                resized.getHeight(),
                BufferedImage.TYPE_INT_RGB
        );
        Graphics2D graphics = rgbImage.createGraphics();
        graphics.drawImage(resized, 0, 0, java.awt.Color.WHITE, null);
        graphics.dispose();

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        if (!writers.hasNext()) {
            throw new IllegalArgumentException("JPEG writer is not available");
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageWriter writer = writers.next();
        try (ImageOutputStream imageOutput = ImageIO.createImageOutputStream(output)) {
            writer.setOutput(imageOutput);
            ImageWriteParam params = writer.getDefaultWriteParam();
            params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            params.setCompressionQuality(properties.getJpegQuality());
            writer.write(null, new IIOImage(rgbImage, null, null), params);
        } finally {
            writer.dispose();
        }
        return output.toByteArray();
    }

    private BufferedImage resize(BufferedImage source) {
        int maxDimension = properties.getMaxImageDimension();
        int width = source.getWidth();
        int height = source.getHeight();
        int largestDimension = Math.max(width, height);
        if (largestDimension <= maxDimension) {
            return source;
        }

        double scale = (double) maxDimension / largestDimension;
        int targetWidth = Math.max(1, (int) Math.round(width * scale));
        int targetHeight = Math.max(1, (int) Math.round(height * scale));
        BufferedImage target = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = target.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.drawImage(source, 0, 0, targetWidth, targetHeight, null);
        graphics.dispose();
        return target;
    }

    private String normalizeContentType(String contentType) {
        return contentType == null ? "" : contentType.toLowerCase(Locale.ROOT);
    }

    private String extensionFor(String contentType) {
        return PDF_CONTENT_TYPE.equals(contentType) ? "pdf" : "jpg";
    }

    private Path stagedRelativePath(MultipartFile receipt) {
        LocalDate now = LocalDate.now();
        return Path.of(
                String.valueOf(now.getYear()),
                String.format("%02d", now.getMonthValue()),
                UUID.randomUUID() + "." + extensionForUpload(receipt)
        );
    }

    private Path storedRelativePath(String sha256, String extension) {
        LocalDate now = LocalDate.now();
        return Path.of(
                String.valueOf(now.getYear()),
                String.format("%02d", now.getMonthValue()),
                sha256 + "." + extension
        );
    }

    private String extensionForUpload(MultipartFile receipt) {
        String filename = cleanOriginalFilename(receipt);
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        }
        return extensionFor(normalizeContentType(receipt.getContentType()));
    }

    private Path stagedRoot() {
        return Path.of(properties.getStoragePath(), "staged");
    }

    private Path storedRoot() {
        return Path.of(properties.getStoragePath(), "processed");
    }

    private String cleanOriginalFilename(MultipartFile receipt) {
        String filename = receipt.getOriginalFilename();
        return filename == null ? null : StringUtils.cleanPath(filename);
    }

    private String sha256(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(bytes));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

    private record StoredBytes(byte[] bytes, String contentType) {
    }
}
