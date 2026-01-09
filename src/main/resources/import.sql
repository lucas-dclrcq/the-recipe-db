-- Test data for The Recipe DB
-- Two cookbooks with recipes and ingredients

-- =====================
-- INGREDIENTS
-- =====================
INSERT INTO ingredient (id, name, created_at, updated_at) VALUES
    ('a1b2c3d4-1111-1111-1111-111111111111', 'Flour', NOW(), NOW()),
    ('a1b2c3d4-2222-2222-2222-222222222222', 'Sugar', NOW(), NOW()),
    ('a1b2c3d4-3333-3333-3333-333333333333', 'Butter', NOW(), NOW()),
    ('a1b2c3d4-4444-4444-4444-444444444444', 'Eggs', NOW(), NOW()),
    ('a1b2c3d4-5555-5555-5555-555555555555', 'Milk', NOW(), NOW()),
    ('a1b2c3d4-6666-6666-6666-666666666666', 'Salt', NOW(), NOW()),
    ('a1b2c3d4-7777-7777-7777-777777777777', 'Olive Oil', NOW(), NOW()),
    ('a1b2c3d4-8888-8888-8888-888888888888', 'Garlic', NOW(), NOW()),
    ('a1b2c3d4-9999-9999-9999-999999999999', 'Onion', NOW(), NOW()),
    ('a1b2c3d4-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Tomatoes', NOW(), NOW()),
    ('a1b2c3d4-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Chicken', NOW(), NOW()),
    ('a1b2c3d4-cccc-cccc-cccc-cccccccccccc', 'Beef', NOW(), NOW()),
    ('a1b2c3d4-dddd-dddd-dddd-dddddddddddd', 'Pasta', NOW(), NOW()),
    ('a1b2c3d4-eeee-eeee-eeee-eeeeeeeeeeee', 'Basil', NOW(), NOW()),
    ('a1b2c3d4-ffff-ffff-ffff-ffffffffffff', 'Parmesan Cheese', NOW(), NOW()),
    ('b1b2c3d4-1111-1111-1111-111111111111', 'Vanilla Extract', NOW(), NOW()),
    ('b1b2c3d4-2222-2222-2222-222222222222', 'Baking Powder', NOW(), NOW()),
    ('b1b2c3d4-3333-3333-3333-333333333333', 'Cocoa Powder', NOW(), NOW()),
    ('b1b2c3d4-4444-4444-4444-444444444444', 'Heavy Cream', NOW(), NOW()),
    ('b1b2c3d4-5555-5555-5555-555555555555', 'Lemon', NOW(), NOW());

-- =====================
-- COOKBOOK 1: Italian Classics
-- =====================
INSERT INTO cookbook (id, title, author, created_at, updated_at, has_cover, ocr_status) VALUES
    ('c0000001-0000-0000-0000-000000000001', 'Italian Classics', 'Maria Rossi', NOW(), NOW(), false, 'NONE');

-- Recipes for Italian Classics
INSERT INTO recipe (id, name, page_number, cookbook_id, created_at, updated_at) VALUES
    ('e0000001-0001-0000-0000-000000000001', 'Spaghetti Carbonara', 12, 'c0000001-0000-0000-0000-000000000001', NOW(), NOW()),
    ('e0000001-0002-0000-0000-000000000002', 'Margherita Pizza', 28, 'c0000001-0000-0000-0000-000000000001', NOW(), NOW()),
    ('e0000001-0003-0000-0000-000000000003', 'Chicken Parmesan', 45, 'c0000001-0000-0000-0000-000000000001', NOW(), NOW()),
    ('e0000001-0004-0000-0000-000000000004', 'Tiramisu', 78, 'c0000001-0000-0000-0000-000000000001', NOW(), NOW()),
    ('e0000001-0005-0000-0000-000000000005', 'Bruschetta', 8, 'c0000001-0000-0000-0000-000000000001', NOW(), NOW());

-- Recipe ingredients for Italian Classics
-- Spaghetti Carbonara: Pasta, Eggs, Parmesan, Garlic, Olive Oil, Salt
INSERT INTO recipe_ingredient (recipe_id, ingredient_id) VALUES
    ('e0000001-0001-0000-0000-000000000001', 'a1b2c3d4-dddd-dddd-dddd-dddddddddddd'),
    ('e0000001-0001-0000-0000-000000000001', 'a1b2c3d4-4444-4444-4444-444444444444'),
    ('e0000001-0001-0000-0000-000000000001', 'a1b2c3d4-ffff-ffff-ffff-ffffffffffff'),
    ('e0000001-0001-0000-0000-000000000001', 'a1b2c3d4-8888-8888-8888-888888888888'),
    ('e0000001-0001-0000-0000-000000000001', 'a1b2c3d4-7777-7777-7777-777777777777'),
    ('e0000001-0001-0000-0000-000000000001', 'a1b2c3d4-6666-6666-6666-666666666666');

-- Margherita Pizza: Flour, Tomatoes, Basil, Olive Oil, Salt
INSERT INTO recipe_ingredient (recipe_id, ingredient_id) VALUES
    ('e0000001-0002-0000-0000-000000000002', 'a1b2c3d4-1111-1111-1111-111111111111'),
    ('e0000001-0002-0000-0000-000000000002', 'a1b2c3d4-aaaa-aaaa-aaaa-aaaaaaaaaaaa'),
    ('e0000001-0002-0000-0000-000000000002', 'a1b2c3d4-eeee-eeee-eeee-eeeeeeeeeeee'),
    ('e0000001-0002-0000-0000-000000000002', 'a1b2c3d4-7777-7777-7777-777777777777'),
    ('e0000001-0002-0000-0000-000000000002', 'a1b2c3d4-6666-6666-6666-666666666666');

-- Chicken Parmesan: Chicken, Flour, Eggs, Tomatoes, Parmesan, Basil, Olive Oil
INSERT INTO recipe_ingredient (recipe_id, ingredient_id) VALUES
    ('e0000001-0003-0000-0000-000000000003', 'a1b2c3d4-bbbb-bbbb-bbbb-bbbbbbbbbbbb'),
    ('e0000001-0003-0000-0000-000000000003', 'a1b2c3d4-1111-1111-1111-111111111111'),
    ('e0000001-0003-0000-0000-000000000003', 'a1b2c3d4-4444-4444-4444-444444444444'),
    ('e0000001-0003-0000-0000-000000000003', 'a1b2c3d4-aaaa-aaaa-aaaa-aaaaaaaaaaaa'),
    ('e0000001-0003-0000-0000-000000000003', 'a1b2c3d4-ffff-ffff-ffff-ffffffffffff'),
    ('e0000001-0003-0000-0000-000000000003', 'a1b2c3d4-eeee-eeee-eeee-eeeeeeeeeeee'),
    ('e0000001-0003-0000-0000-000000000003', 'a1b2c3d4-7777-7777-7777-777777777777');

-- Tiramisu: Eggs, Sugar, Mascarpone (using Heavy Cream), Cocoa Powder, Vanilla Extract
INSERT INTO recipe_ingredient (recipe_id, ingredient_id) VALUES
    ('e0000001-0004-0000-0000-000000000004', 'a1b2c3d4-4444-4444-4444-444444444444'),
    ('e0000001-0004-0000-0000-000000000004', 'a1b2c3d4-2222-2222-2222-222222222222'),
    ('e0000001-0004-0000-0000-000000000004', 'b1b2c3d4-4444-4444-4444-444444444444'),
    ('e0000001-0004-0000-0000-000000000004', 'b1b2c3d4-3333-3333-3333-333333333333'),
    ('e0000001-0004-0000-0000-000000000004', 'b1b2c3d4-1111-1111-1111-111111111111');

-- Bruschetta: Tomatoes, Basil, Garlic, Olive Oil, Salt
INSERT INTO recipe_ingredient (recipe_id, ingredient_id) VALUES
    ('e0000001-0005-0000-0000-000000000005', 'a1b2c3d4-aaaa-aaaa-aaaa-aaaaaaaaaaaa'),
    ('e0000001-0005-0000-0000-000000000005', 'a1b2c3d4-eeee-eeee-eeee-eeeeeeeeeeee'),
    ('e0000001-0005-0000-0000-000000000005', 'a1b2c3d4-8888-8888-8888-888888888888'),
    ('e0000001-0005-0000-0000-000000000005', 'a1b2c3d4-7777-7777-7777-777777777777'),
    ('e0000001-0005-0000-0000-000000000005', 'a1b2c3d4-6666-6666-6666-666666666666');

-- =====================
-- COOKBOOK 2: Home Baking Essentials
-- =====================
INSERT INTO cookbook (id, title, author, created_at, updated_at, has_cover, ocr_status) VALUES
    ('c0000002-0000-0000-0000-000000000002', 'Home Baking Essentials', 'Sophie Baker', NOW(), NOW(), false, 'NONE');

-- Recipes for Home Baking Essentials
INSERT INTO recipe (id, name, page_number, cookbook_id, created_at, updated_at) VALUES
    ('e0000002-0001-0000-0000-000000000001', 'Classic Chocolate Cake', 15, 'c0000002-0000-0000-0000-000000000002', NOW(), NOW()),
    ('e0000002-0002-0000-0000-000000000002', 'Vanilla Cupcakes', 32, 'c0000002-0000-0000-0000-000000000002', NOW(), NOW()),
    ('e0000002-0003-0000-0000-000000000003', 'Lemon Tart', 56, 'c0000002-0000-0000-0000-000000000002', NOW(), NOW()),
    ('e0000002-0004-0000-0000-000000000004', 'Butter Croissants', 72, 'c0000002-0000-0000-0000-000000000002', NOW(), NOW()),
    ('e0000002-0005-0000-0000-000000000005', 'Chocolate Chip Cookies', 10, 'c0000002-0000-0000-0000-000000000002', NOW(), NOW());

-- Recipe ingredients for Home Baking Essentials
-- Classic Chocolate Cake: Flour, Sugar, Butter, Eggs, Milk, Cocoa Powder, Baking Powder, Vanilla Extract
INSERT INTO recipe_ingredient (recipe_id, ingredient_id) VALUES
    ('e0000002-0001-0000-0000-000000000001', 'a1b2c3d4-1111-1111-1111-111111111111'),
    ('e0000002-0001-0000-0000-000000000001', 'a1b2c3d4-2222-2222-2222-222222222222'),
    ('e0000002-0001-0000-0000-000000000001', 'a1b2c3d4-3333-3333-3333-333333333333'),
    ('e0000002-0001-0000-0000-000000000001', 'a1b2c3d4-4444-4444-4444-444444444444'),
    ('e0000002-0001-0000-0000-000000000001', 'a1b2c3d4-5555-5555-5555-555555555555'),
    ('e0000002-0001-0000-0000-000000000001', 'b1b2c3d4-3333-3333-3333-333333333333'),
    ('e0000002-0001-0000-0000-000000000001', 'b1b2c3d4-2222-2222-2222-222222222222'),
    ('e0000002-0001-0000-0000-000000000001', 'b1b2c3d4-1111-1111-1111-111111111111');

-- Vanilla Cupcakes: Flour, Sugar, Butter, Eggs, Milk, Vanilla Extract, Baking Powder
INSERT INTO recipe_ingredient (recipe_id, ingredient_id) VALUES
    ('e0000002-0002-0000-0000-000000000002', 'a1b2c3d4-1111-1111-1111-111111111111'),
    ('e0000002-0002-0000-0000-000000000002', 'a1b2c3d4-2222-2222-2222-222222222222'),
    ('e0000002-0002-0000-0000-000000000002', 'a1b2c3d4-3333-3333-3333-333333333333'),
    ('e0000002-0002-0000-0000-000000000002', 'a1b2c3d4-4444-4444-4444-444444444444'),
    ('e0000002-0002-0000-0000-000000000002', 'a1b2c3d4-5555-5555-5555-555555555555'),
    ('e0000002-0002-0000-0000-000000000002', 'b1b2c3d4-1111-1111-1111-111111111111'),
    ('e0000002-0002-0000-0000-000000000002', 'b1b2c3d4-2222-2222-2222-222222222222');

-- Lemon Tart: Flour, Butter, Sugar, Eggs, Lemon, Heavy Cream
INSERT INTO recipe_ingredient (recipe_id, ingredient_id) VALUES
    ('e0000002-0003-0000-0000-000000000003', 'a1b2c3d4-1111-1111-1111-111111111111'),
    ('e0000002-0003-0000-0000-000000000003', 'a1b2c3d4-3333-3333-3333-333333333333'),
    ('e0000002-0003-0000-0000-000000000003', 'a1b2c3d4-2222-2222-2222-222222222222'),
    ('e0000002-0003-0000-0000-000000000003', 'a1b2c3d4-4444-4444-4444-444444444444'),
    ('e0000002-0003-0000-0000-000000000003', 'b1b2c3d4-5555-5555-5555-555555555555'),
    ('e0000002-0003-0000-0000-000000000003', 'b1b2c3d4-4444-4444-4444-444444444444');

-- Butter Croissants: Flour, Butter, Milk, Sugar, Salt
INSERT INTO recipe_ingredient (recipe_id, ingredient_id) VALUES
    ('e0000002-0004-0000-0000-000000000004', 'a1b2c3d4-1111-1111-1111-111111111111'),
    ('e0000002-0004-0000-0000-000000000004', 'a1b2c3d4-3333-3333-3333-333333333333'),
    ('e0000002-0004-0000-0000-000000000004', 'a1b2c3d4-5555-5555-5555-555555555555'),
    ('e0000002-0004-0000-0000-000000000004', 'a1b2c3d4-2222-2222-2222-222222222222'),
    ('e0000002-0004-0000-0000-000000000004', 'a1b2c3d4-6666-6666-6666-666666666666');

-- Chocolate Chip Cookies: Flour, Butter, Sugar, Eggs, Vanilla Extract, Baking Powder, Salt
INSERT INTO recipe_ingredient (recipe_id, ingredient_id) VALUES
    ('e0000002-0005-0000-0000-000000000005', 'a1b2c3d4-1111-1111-1111-111111111111'),
    ('e0000002-0005-0000-0000-000000000005', 'a1b2c3d4-3333-3333-3333-333333333333'),
    ('e0000002-0005-0000-0000-000000000005', 'a1b2c3d4-2222-2222-2222-222222222222'),
    ('e0000002-0005-0000-0000-000000000005', 'a1b2c3d4-4444-4444-4444-444444444444'),
    ('e0000002-0005-0000-0000-000000000005', 'b1b2c3d4-1111-1111-1111-111111111111'),
    ('e0000002-0005-0000-0000-000000000005', 'b1b2c3d4-2222-2222-2222-222222222222'),
    ('e0000002-0005-0000-0000-000000000005', 'a1b2c3d4-6666-6666-6666-666666666666');

-- =====================
-- INGREDIENT DISAMBIGUATIONS (aliases)
-- =====================
INSERT INTO ingredient_disambiguation (id, ingredient_id, name, created_at) VALUES
    ('d0000001-0000-0000-0000-000000000001', 'a1b2c3d4-1111-1111-1111-111111111111', 'All-Purpose Flour', NOW()),
    ('d0000002-0000-0000-0000-000000000002', 'a1b2c3d4-1111-1111-1111-111111111111', 'Plain Flour', NOW()),
    ('d0000003-0000-0000-0000-000000000003', 'a1b2c3d4-3333-3333-3333-333333333333', 'Unsalted Butter', NOW()),
    ('d0000004-0000-0000-0000-000000000004', 'a1b2c3d4-8888-8888-8888-888888888888', 'Garlic Cloves', NOW()),
    ('d0000005-0000-0000-0000-000000000005', 'a1b2c3d4-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Roma Tomatoes', NOW()),
    ('d0000006-0000-0000-0000-000000000006', 'a1b2c3d4-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Cherry Tomatoes', NOW());
