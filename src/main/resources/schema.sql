CREATE TABLE IF NOT EXISTS categories (
  id BIGINT NOT NULL AUTO_INCREMENT,
  large_category VARCHAR(50) NOT NULL,
  medium_category VARCHAR(50) NOT NULL,
  small_category VARCHAR(50) NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_category (large_category, medium_category, small_category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS delivery_fees (
  id BIGINT NOT NULL AUTO_INCREMENT,
  order_number VARCHAR(50) DEFAULT NULL,
  platform ENUM('nongra','coupang','smartstore') DEFAULT NULL,
  shipping_included TINYINT(1) DEFAULT '0',
  total_delivery_fee INT NOT NULL,
  unit_price INT NOT NULL DEFAULT '0',
  shipping_count INT DEFAULT '1',
  order_date DATE NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  category_id BIGINT GENERATED ALWAYS AS (
    CASE WHEN (shipping_included = TRUE) THEN 64 ELSE 63 END
  ) STORED,
  PRIMARY KEY (id),
  UNIQUE KEY uq_delivery (platform, order_number, order_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS expenses (
  id BIGINT NOT NULL AUTO_INCREMENT,
  expense_date DATE NOT NULL,
  expense_time TIME DEFAULT NULL,
  category_id BIGINT NOT NULL,
  expense_type ENUM('SHIPPING','PACKAGING','MACHINE_RENTAL','TAX_FREE_FUEL','FUEL','LABOR','AGROCHEMICAL','COMMISSION') DEFAULT NULL,
  unit_cost INT DEFAULT NULL,
  quantity INT DEFAULT NULL,
  total_cost INT NOT NULL,
  description VARCHAR(500) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_expenses (expense_date, expense_time, total_cost)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS products (
  id BIGINT NOT NULL AUTO_INCREMENT,
  product_name VARCHAR(100) NOT NULL,
  unit_price INT NOT NULL,
  unit_cnt INT DEFAULT NULL,
  platform ENUM('nongra','coupang','smartstore') NOT NULL,
  category_id BIGINT DEFAULT NULL,
  shipping_included TINYINT(1) DEFAULT '0',
  start_date DATE NOT NULL,
  end_date DATE DEFAULT NULL,
  active TINYINT(1) DEFAULT '1',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY unique_product (product_name, platform, unit_price, active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS purchases (
  id BIGINT NOT NULL AUTO_INCREMENT,
  product_id BIGINT DEFAULT NULL,
  category_id BIGINT DEFAULT NULL,
  purchase_date DATE NOT NULL,
  purchase_time TIME DEFAULT NULL,
  quantity INT NOT NULL,
  unit_cost INT NOT NULL,
  total_cost INT NOT NULL,
  supplier_name VARCHAR(500) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_purchases (purchase_date, purchase_time, total_cost)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS sales (
  id BIGINT NOT NULL AUTO_INCREMENT,
  order_number VARCHAR(50) NOT NULL,
  platform ENUM('nongra','coupang','smartstore') DEFAULT NULL,
  product_name_raw VARCHAR(200) NOT NULL,
  product_id BIGINT DEFAULT NULL,
  quantity INT NOT NULL,
  product_total INT NOT NULL,
  unit_price INT NOT NULL,
  shipping_included TINYINT(1) DEFAULT '0',
  order_date DATE NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_sales (platform, order_number, order_date, product_name_raw)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS users (
  username VARCHAR(50) NOT NULL,
  password VARCHAR(100) NOT NULL,
  PRIMARY KEY (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS commission (
  ID VARCHAR(20) NOT NULL,
  date VARCHAR(8) NOT NULL,
  smallCategory VARCHAR(50) NOT NULL,
  commissionCount INT DEFAULT NULL,
  commissionAmount INT DEFAULT NULL,
  channel INT NOT NULL,
  PRIMARY KEY (ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;