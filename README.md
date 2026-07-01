# ☕ Brew & Co. — Café POS System

A production-quality Java Swing Point-of-Sale application for a fictional café, backed by MySQL.

---

## 📸 Features

| Screen | What you get |
|--------|-------------|
| **Login** | Branded split-screen, DB-authenticated, loading state |
| **Menu Dashboard** | Card grid, category filter tabs, keyword search, availability badge, click-to-add with flash feedback |
| **Order / Cart** | Qty editing in-table, delete row, discount input, live subtotal/tax/total, payment method selector, confirm dialog, success receipt dialog |
| **Reports** | Date-range filter, stats cards (transactions / revenue / items sold), master-detail table, print/export via JTable.print() |
| **Sidebar Nav** | Active-state highlight, cashier name/role, logout, graceful exit |

---

## 🗂 Project Layout

```
CafePOS/
├── lib/                                ← Put mysql-connector-j-*.jar here
├── out/                                ← Compiled classes (generated)
├── src/main/java/com/cafepos/
│   ├── Main.java                       ← Entry point
│   ├── config/
│   │   ├── DatabaseConfig.java         ← DB host/user/password constants
│   │   └── ConnectionPool.java         ← Thread-safe JDBC pool
│   ├── model/
│   │   ├── User.java
│   │   ├── Category.java
│   │   ├── Product.java
│   │   ├── CartItem.java
│   │   ├── Order.java
│   │   └── OrderItem.java
│   ├── dao/
│   │   ├── UserDAO.java                ← authenticate()
│   │   ├── ProductDAO.java             ← getAll, getByCategory, search
│   │   └── OrderDAO.java               ← saveOrder(), getOrders(), stats
│   ├── util/
│   │   ├── UITheme.java                ← Colours, fonts, factory methods
│   │   ├── CurrencyUtil.java
│   │   └── AppContext.java             ← Session singleton
│   └── gui/
│       ├── MainFrame.java              ← Root window + CardLayout routing
│       ├── LoginPanel.java
│       ├── DashboardPanel.java
│       ├── OrderPanel.java
│       ├── ReportPanel.java
│       └── WrapLayout.java             ← Reflowing FlowLayout
└── src/main/resources/
    └── schema.sql                      ← Full DB schema + seed data
```

---

## ⚡ Quick-Start (5 Steps)

### 1. Install prerequisites

| Tool | Version |
|------|---------|
| JDK  | 17 or later |
| MySQL | 8.0 or later |

### 2. Set up the database

```bash
mysql -u root -p < src/main/resources/schema.sql
```

This creates the `cafe_pos` database, all tables, and 30 sample products.

### 3. Add the MySQL JDBC driver

Download `mysql-connector-j-*.jar` from  
<https://dev.mysql.com/downloads/connector/j/>  
(choose "Platform Independent" → ZIP)

Place the JAR inside the `lib/` folder:
```
CafePOS/lib/mysql-connector-j-9.x.x.jar
```

### 4. Configure the connection

Open `src/main/java/com/cafepos/config/DatabaseConfig.java` and update:

```java
public static final String DB_USER     = "root";       // your MySQL username
public static final String DB_PASSWORD = "";           // your MySQL password
```

### 5. Build & run

**Windows:**
```bat
build.bat
```

**macOS / Linux:**
```bash
chmod +x build.sh && ./build.sh
```

---

## 🔐 Demo Credentials

| Username | Password    | Role     |
|----------|-------------|----------|
| admin    | admin123    | Admin    |
| cashier  | cashier123  | Cashier  |

> ⚠️ Demo uses plain-text passwords. In production, hash with BCrypt before storage.

---

## 🗄 Database Schema

```
users          — id, username, password, full_name, role, active
categories     — id, name, icon_emoji, sort_order
products       — id, category_id, name, description, price, image_emoji, available
orders         — id, user_id, order_date, subtotal, tax_amount, discount_amount,
                 total_amount, payment_method, status, notes
order_items    — id, order_id, product_id, product_name, unit_price, quantity, line_total
```

---

## 🏗 Architecture Notes

- **MVC-ish**: Models (`model/`), data access (`dao/`), view (`gui/`), utilities (`util/`, `config/`)
- **Prepared statements** throughout to prevent SQL injection
- **SwingWorker** for all DB calls — UI never blocks
- **ConnectionPool** validates connections before use and replaces stale ones
- **Transactions** in `OrderDAO.saveOrder()` — all-or-nothing guarantee

---

## 🛠 Extending the App

| Task | File to modify |
|------|---------------|
| Add a product | Insert row into `products` table, or extend `DashboardPanel` with a form |
| Change tax rate | `DatabaseConfig.TAX_RATE` |
| Change currency | `CurrencyUtil` — swap `Locale.US` |
| Add BCrypt auth | Replace `password = ?` in `UserDAO` with BCrypt.checkpw() |
| New screen | Add panel to `MainFrame.mainContent`, add nav button in `buildSidebar()` |

---

## 📦 Dependencies

| Library | Purpose | Source |
|---------|---------|--------|
| `mysql-connector-j` | JDBC driver | dev.mysql.com |
| Java Swing (JDK) | GUI toolkit | bundled with JDK |

No Maven / Gradle required — single-JAR classpath.

---

*Built as a portfolio project demonstrating Java Swing, JDBC, MVC pattern, and professional UI design.*
