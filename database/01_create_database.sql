-- ============================================================
-- PartsHub Pro  - 01_create_database.sql
-- Database : PartsHub
-- Engine   : MySQL 8.0
-- ============================================================
-- PURPOSE : Create the PartsHub database if it does not yet
--           exist.  Safe to run repeatedly - never drops anything.
-- ============================================================

-- Create the database (schema) only when it is absent.
CREATE DATABASE IF NOT EXISTS PartsHub
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- Select the database for all subsequent statements in this session.
USE PartsHub;

SELECT 'PartsHub database is ready.' AS status;
