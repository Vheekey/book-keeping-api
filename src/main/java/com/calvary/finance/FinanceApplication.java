package com.calvary.finance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class FinanceApplication {
  public static void main(String[] args) {
    SpringApplication.run(FinanceApplication.class, args);
  }
}
