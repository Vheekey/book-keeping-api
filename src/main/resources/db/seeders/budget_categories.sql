--upsert--

INSERT INTO budget_categories (acc_no, description, is_active) VALUES
-- Fellowship & General
('B1', 'Fellowship (Specify Event below)', true),
('B2', 'Funeral, birth, farewell, gratitude etc.', true),

-- Admin & Office
('D1', 'Office Supplies', true),
('D2', 'Administrative needs', true),

-- Supplies
('E1', 'Kitchen Supplies & Sunday Fika', true),
('E2', 'Communion Supplies', true),

-- Infrastructure
('F1', 'Equipment', true),
('G2', 'Ministry Expenses', true),

-- Missions & Outreach
('H1', 'Missions', true),
('H2', 'Outreach', true),

-- Sunday & Worship
('I1', 'Sunday School', true),
('I2', 'Worship Team', true),
('I3', 'Youth', true),
('I4', 'Conference (Specify event below)', true),
('I5', 'Ladies'' Meeting', true), -- Note the escaped single quote
('I6', 'Men''s Meeting', true),
('I7', 'Small Group', true),
('I8', 'Benevolence', true),
('I9', 'Guest Speaker', true),
('I10', 'Equipping', true),

-- Catch-all
('K2', 'Other', true)
ON CONFLICT (acc_no) DO UPDATE
SET description = EXCLUDED.description;