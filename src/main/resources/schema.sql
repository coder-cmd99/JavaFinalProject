-- ============================================
-- CAFÉ POS SYSTEM - MySQL Database Schema
-- ============================================

CREATE DATABASE IF NOT EXISTS cafe_pos CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE cafe_pos;

-- Users table for authentication
CREATE TABLE IF NOT EXISTS users (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,   -- BCrypt hash in production; plain for demo
    full_name   VARCHAR(100),
    role        ENUM('admin','cashier') DEFAULT 'cashier',
    active      TINYINT(1) DEFAULT 1,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Product categories
CREATE TABLE IF NOT EXISTS categories (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(80) NOT NULL UNIQUE,
    icon_emoji  VARCHAR(10) DEFAULT '☕',
    sort_order  INT DEFAULT 0
);

-- Products / menu items
CREATE TABLE IF NOT EXISTS products (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    category_id     INT NOT NULL,
    name            VARCHAR(120) NOT NULL,
    description     VARCHAR(255),
    price           DECIMAL(10,2) NOT NULL,
    image_emoji     VARCHAR(10) DEFAULT '🍵',
    available       TINYINT(1) DEFAULT 1,
    FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- Orders (header)
CREATE TABLE IF NOT EXISTS orders (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    user_id         INT NOT NULL,
    order_date      DATETIME DEFAULT CURRENT_TIMESTAMP,
    subtotal        DECIMAL(10,2) NOT NULL,
    tax_amount      DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_amount    DECIMAL(10,2) NOT NULL,
    payment_method  ENUM('cash','card','e-wallet') DEFAULT 'cash',
    status          ENUM('completed','voided') DEFAULT 'completed',
    notes           VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Order line items
CREATE TABLE IF NOT EXISTS order_items (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    order_id    INT NOT NULL,
    product_id  INT NOT NULL,
    product_name VARCHAR(120) NOT NULL,  -- snapshot at time of sale
    unit_price  DECIMAL(10,2) NOT NULL,
    quantity    INT NOT NULL,
    line_total  DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- ============================================
-- Seed Data
-- ============================================

INSERT IGNORE INTO users (username, password, full_name, role) VALUES
  ('admin',   'admin123',    'Admin User',    'admin'),
  ('cashier', 'cashier123',  'Jane Barista',  'cashier');

INSERT IGNORE INTO categories (name, icon_emoji, sort_order) VALUES
  ('Hot Drinks',   '☕', 1),
  ('Cold Drinks',  '🧊', 2),
  ('Pastries',     '🥐', 3),
  ('Light Meals',  '🥗', 4),
  ('Desserts',     '🍰', 5);

INSERT IGNORE INTO products (category_id, name, description, price, image_emoji, available) VALUES
  -- Hot Drinks (1)
  (1, 'Espresso',          'Rich, concentrated coffee shot',              2.50, '☕', 1),
  (1, 'Cappuccino',        'Espresso with steamed milk foam',             4.00, '☕', 1),
  (1, 'Latte',             'Smooth espresso with steamed milk',           4.50, '☕', 1),
  (1, 'Flat White',        'Velvety microfoam over double ristretto',     4.50, '☕', 1),
  (1, 'Americano',         'Espresso topped with hot water',              3.00, '☕', 1),
  (1, 'Mocha',             'Espresso, chocolate & steamed milk',          5.00, '☕', 1),
  (1, 'Chai Latte',        'Spiced tea with creamy steamed milk',         4.50, '🍵', 1),
  (1, 'Hot Chocolate',     'Rich Belgian chocolate drink',                4.00, '🍫', 1),
  -- Cold Drinks (2)
  (2, 'Iced Latte',        'Espresso over ice with cold milk',            5.00, '🧋', 1),
  (2, 'Cold Brew',         '24-hour cold-steeped coffee',                 5.50, '🧊', 1),
  (2, 'Iced Matcha Latte', 'Premium matcha with oat milk over ice',       5.50, '🍵', 1),
  (2, 'Frappuccino',       'Blended coffee with whipped cream',           6.00, '🥤', 1),
  (2, 'Fresh Orange Juice','Freshly squeezed navel oranges',              4.50, '🍊', 1),
  (2, 'Sparkling Water',   'Chilled sparkling mineral water',             2.50, '💧', 1),
  -- Pastries (3)
  (3, 'Butter Croissant',  'Flaky, golden French-style croissant',        3.50, '🥐', 1),
  (3, 'Almond Croissant',  'Frangipane-filled, toasted almonds',          4.00, '🥐', 1),
  (3, 'Blueberry Muffin',  'Bursting with fresh blueberries',             3.50, '🧁', 1),
  (3, 'Banana Bread',      'Moist slice with walnuts',                    3.00, '🍌', 1),
  (3, 'Cinnamon Roll',     'Soft dough, cream cheese glaze',              4.50, '🌀', 1),
  (3, 'Scone',             'Classic with clotted cream & jam',            3.50, '🍞', 1),
  -- Light Meals (4)
  (4, 'Avocado Toast',     'Sourdough, smashed avo, poached egg',         8.50, '🥑', 1),
  (4, 'Egg & Cheese Bagel','Toasted bagel with farm egg & cheddar',       7.00, '🥯', 1),
  (4, 'Granola Bowl',      'Greek yogurt, house granola, fresh fruit',    7.50, '🥣', 1),
  (4, 'Club Sandwich',     'Triple-decker with chicken, bacon, veg',      9.50, '🥪', 1),
  (4, 'Quiche Lorraine',   'Buttery pastry, smoky bacon & gruyère',       8.00, '🥧', 1),
  -- Desserts (5)
  (5, 'Cheesecake',        'New York-style with berry compote',           6.00, '🍰', 1),
  (5, 'Tiramisu',          'Classic Italian espresso dessert',            6.50, '🍮', 1),
  (5, 'Chocolate Brownie', 'Fudgy dark chocolate brownie',                4.50, '🍫', 1),
  (5, 'Lemon Tart',        'Sharp curd in crisp pastry shell',            5.50, '🍋', 1),
  (5, 'Cookie',            'Warm chocolate chip from the oven',           2.50, '🍪', 1);
