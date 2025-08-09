-- 구버전 테이블
-- Category 테이블
CREATE TABLE IF NOT EXISTS category (
  largeCategory VARCHAR(50) NOT NULL,
  mediumCategory VARCHAR(50) NOT NULL,
  smallCategory VARCHAR(50) NOT NULL,
  startDate VARCHAR(8) NOT NULL,
  endDate VARCHAR(8) NOT NULL,
  productName VARCHAR(100),
  productQuantity INT,
  productAmount INT,
  PRIMARY KEY (startDate, endDate, largeCategory, mediumCategory, smallCategory)
);

-- Commission 테이블
CREATE TABLE IF NOT EXISTS commission (
  ID VARCHAR(20) NOT NULL,
  date VARCHAR(8) NOT NULL,
  smallCategory VARCHAR(50) NOT NULL,
  commissionCount INT,
  commissionAmount INT,
  channel INT NOT NULL,
  PRIMARY KEY (ID)
);

-- Expense 테이블
CREATE TABLE IF NOT EXISTS expense (
  date VARCHAR(14) NOT NULL,
  smallCategory VARCHAR(50) NOT NULL,
  productName VARCHAR(100),
  expenseCount INT,
  expenseAmount INT,
  PRIMARY KEY (date, smallCategory, productName)
);

-- Purchase 테이블
CREATE TABLE IF NOT EXISTS purchase (
  date VARCHAR(14) NOT NULL,
  smallCategory VARCHAR(50) NOT NULL,
  productName VARCHAR(100),
  purchaseAmount INT,
  purchaseCount INT,
  PRIMARY KEY (date, smallCategory, productName)
);

-- Report 테이블
CREATE TABLE IF NOT EXISTS report (
  date VARCHAR(14) NOT NULL,
  largeCategory VARCHAR(50) NOT NULL,
  mediumCategory VARCHAR(50) NOT NULL,
  smallCategory VARCHAR(50) NOT NULL,
  sumSales INT,
  sumPurchase INT,
  sumExpense INT,
  profit INT,
  PRIMARY KEY (date, largeCategory, mediumCategory, smallCategory)
);

-- Sales 테이블
CREATE TABLE IF NOT EXISTS sales (
  ID VARCHAR(20) NOT NULL,
  date VARCHAR(14) NOT NULL,
  smallCategory VARCHAR(50) NOT NULL,
  productName VARCHAR(100),
  payway INT,
  channel INT,
  salesCount INT,
  salesAmount INT,
  PRIMARY KEY (ID, date, productName)
);

-- User 테이블
CREATE TABLE IF NOT EXISTS users (
  username VARCHAR(50) NOT NULL PRIMARY KEY,
  password VARCHAR(100) NOT NULL
);

--리뉴얼 테이블