-- ============================================================================
-- MediaTech Security Enhancement - Database Migration Script
-- Version: 1.0
-- Date: 2025-12-19
-- Description: Creates new security tables and updates existing schema
-- ============================================================================

-- Use the MediaTech database
USE mediatech_db_v2;

-- ============================================================================
-- STEP 1: Backup Existing Data (Recommended)
-- ============================================================================
-- Run these commands in a separate session to backup before migration:
-- mysqldump -u root mediatech_db_v2 users > users_backup.sql
-- mysqldump -u root mediatech_db_v2 > full_backup.sql

-- ============================================================================
-- STEP 2: Update Users Table (Add Account Lockout Fields)
-- ============================================================================

-- Check if columns already exist before adding
SET @exist := (SELECT COUNT(*) 
               FROM information_schema.COLUMNS 
               WHERE TABLE_SCHEMA = 'mediatech_db_v2' 
               AND TABLE_NAME = 'users' 
               AND COLUMN_NAME = 'account_locked');

SET @sqlstmt := IF(@exist = 0, 
    'ALTER TABLE users ADD COLUMN account_locked BOOLEAN DEFAULT FALSE',
    'SELECT "Column account_locked already exists" AS message');

PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add lockout_time column
SET @exist := (SELECT COUNT(*) 
               FROM information_schema.COLUMNS 
               WHERE TABLE_SCHEMA = 'mediatech_db_v2' 
               AND TABLE_NAME = 'users' 
               AND COLUMN_NAME = 'lockout_time');

SET @sqlstmt := IF(@exist = 0, 
    'ALTER TABLE users ADD COLUMN lockout_time DATETIME NULL',
    'SELECT "Column lockout_time already exists" AS message');

PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add failed_attempts column
SET @exist := (SELECT COUNT(*) 
               FROM information_schema.COLUMNS 
               WHERE TABLE_SCHEMA = 'mediatech_db_v2' 
               AND TABLE_NAME = 'users' 
               AND COLUMN_NAME = 'failed_attempts');

SET @sqlstmt := IF(@exist = 0, 
    'ALTER TABLE users ADD COLUMN failed_attempts INT DEFAULT 0',
    'SELECT "Column failed_attempts already exists" AS message');

PREPARE stmt FROM @sqlstmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Verify users table structure
SELECT 'Users table updated successfully with lockout fields' AS status;

-- ============================================================================
-- STEP 3: Create Refresh Tokens Table
-- ============================================================================

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(512) NOT NULL UNIQUE,
    expiry_date DATETIME NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    revoked_at DATETIME NULL,
    
    -- Foreign key to users table
    CONSTRAINT fk_refresh_token_user 
        FOREIGN KEY (user_id) 
        REFERENCES users(id_user) 
        ON DELETE CASCADE,
    
    -- Indexes for performance
    INDEX idx_token (token),
    INDEX idx_user_id (user_id),
    INDEX idx_expiry_date (expiry_date),
    INDEX idx_revoked (revoked)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SELECT 'Refresh tokens table created successfully' AS status;

-- ============================================================================
-- STEP 4: Create Token Blacklist Table
-- ============================================================================

CREATE TABLE IF NOT EXISTS token_blacklist (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expiry_date DATETIME NOT NULL,
    blacklisted_at DATETIME NOT NULL,
    reason VARCHAR(100) NULL,
    
    -- Indexes for fast lookup
    INDEX idx_token_hash (token_hash),
    INDEX idx_expiry_date (expiry_date),
    INDEX idx_blacklisted_at (blacklisted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SELECT 'Token blacklist table created successfully' AS status;

-- ============================================================================
-- STEP 5: Create Login Attempts Table
-- ============================================================================

CREATE TABLE IF NOT EXISTS login_attempts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    ip_address VARCHAR(45) NOT NULL,
    success BOOLEAN NOT NULL,
    attempt_time DATETIME NOT NULL,
    user_agent VARCHAR(255) NULL,
    
    -- Indexes for performance
    INDEX idx_username (username),
    INDEX idx_ip_address (ip_address),
    INDEX idx_attempt_time (attempt_time),
    INDEX idx_success (success),
    INDEX idx_username_time (username, attempt_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SELECT 'Login attempts table created successfully' AS status;

-- ============================================================================
-- STEP 6: Verify Tables
-- ============================================================================

-- Show all new tables
SELECT 
    table_name AS 'Table Name',
    table_rows AS 'Row Count',
    ROUND((data_length + index_length) / 1024, 2) AS 'Size (KB)'
FROM information_schema.tables
WHERE table_schema = 'mediatech_db_v2' 
AND table_name IN ('refresh_tokens', 'token_blacklist', 'login_attempts')
ORDER BY table_name;

-- Show users table structure
SHOW COLUMNS FROM users;

-- ============================================================================
-- STEP 7: Set Default Values for Existing Users
-- ============================================================================

-- Update existing users to have default lockout values
UPDATE users 
SET 
    account_locked = IFNULL(account_locked, FALSE),
    lockout_time = NULL,
    failed_attempts = IFNULL(failed_attempts, 0)
WHERE account_locked IS NULL OR failed_attempts IS NULL;

SELECT 'Existing users updated with default lockout values' AS status;

-- ============================================================================
-- STEP 8: Create Stored Procedures (Optional - for manual management)
-- ============================================================================

DELIMITER //

-- Procedure to unlock a user account
DROP PROCEDURE IF EXISTS unlock_user_account//
CREATE PROCEDURE unlock_user_account(IN p_username VARCHAR(100))
BEGIN
    UPDATE users 
    SET account_locked = FALSE,
        lockout_time = NULL,
        failed_attempts = 0
    WHERE username = p_username;
    
    SELECT CONCAT('User ', p_username, ' has been unlocked') AS message;
END//

-- Procedure to view user security status
DROP PROCEDURE IF EXISTS view_user_security_status//
CREATE PROCEDURE view_user_security_status(IN p_username VARCHAR(100))
BEGIN
    SELECT 
        u.username,
        u.role,
        u.enabled,
        u.account_locked,
        u.lockout_time,
        u.failed_attempts,
        COUNT(rt.id) AS active_refresh_tokens,
        (SELECT COUNT(*) FROM login_attempts la 
         WHERE la.username = p_username 
         AND la.attempt_time > DATE_SUB(NOW(), INTERVAL 24 HOUR)) AS login_attempts_24h,
        (SELECT COUNT(*) FROM login_attempts la 
         WHERE la.username = p_username 
         AND la.success = FALSE
         AND la.attempt_time > DATE_SUB(NOW(), INTERVAL 24 HOUR)) AS failed_attempts_24h
    FROM users u
    LEFT JOIN refresh_tokens rt ON u.id_user = rt.user_id AND rt.revoked = FALSE
    WHERE u.username = p_username
    GROUP BY u.id_user;
END//

-- Procedure to cleanup expired data
DROP PROCEDURE IF EXISTS cleanup_security_data//
CREATE PROCEDURE cleanup_security_data()
BEGIN
    DECLARE deleted_blacklist INT;
    DECLARE deleted_tokens INT;
    DECLARE deleted_attempts INT;
    
    -- Delete expired blacklist entries
    DELETE FROM token_blacklist WHERE expiry_date < NOW();
    SET deleted_blacklist = ROW_COUNT();
    
    -- Delete expired or revoked refresh tokens
    DELETE FROM refresh_tokens WHERE expiry_date < NOW() OR revoked = TRUE;
    SET deleted_tokens = ROW_COUNT();
    
    -- Delete old login attempts (older than 30 days)
    DELETE FROM login_attempts WHERE attempt_time < DATE_SUB(NOW(), INTERVAL 30 DAY);
    SET deleted_attempts = ROW_COUNT();
    
    SELECT 
        deleted_blacklist AS 'Blacklist Entries Deleted',
        deleted_tokens AS 'Refresh Tokens Deleted',
        deleted_attempts AS 'Login Attempts Deleted',
        NOW() AS 'Cleanup Time';
END//

DELIMITER ;

SELECT 'Stored procedures created successfully' AS status;

-- ============================================================================
-- STEP 9: Create Views for Monitoring (Optional)
-- ============================================================================

-- View: Active Security Sessions
CREATE OR REPLACE VIEW v_active_sessions AS
SELECT 
    u.username,
    u.role,
    COUNT(rt.id) AS active_tokens,
    MAX(rt.created_at) AS last_token_created,
    MAX(rt.expiry_date) AS token_expires_at
FROM users u
LEFT JOIN refresh_tokens rt ON u.id_user = rt.user_id 
WHERE rt.revoked = FALSE AND rt.expiry_date > NOW()
GROUP BY u.id_user;

-- View: Recent Login Activity
CREATE OR REPLACE VIEW v_recent_login_activity AS
SELECT 
    username,
    ip_address,
    success,
    attempt_time,
    user_agent
FROM login_attempts
WHERE attempt_time > DATE_SUB(NOW(), INTERVAL 24 HOUR)
ORDER BY attempt_time DESC;

-- View: Locked Accounts
CREATE OR REPLACE VIEW v_locked_accounts AS
SELECT 
    username,
    role,
    account_locked,
    lockout_time,
    failed_attempts,
    TIMESTAMPDIFF(MINUTE, NOW(), lockout_time) AS minutes_until_unlock
FROM users
WHERE account_locked = TRUE
ORDER BY lockout_time;

-- View: Security Statistics
CREATE OR REPLACE VIEW v_security_statistics AS
SELECT 
    (SELECT COUNT(*) FROM users WHERE account_locked = TRUE) AS locked_accounts,
    (SELECT COUNT(*) FROM refresh_tokens WHERE revoked = FALSE AND expiry_date > NOW()) AS active_refresh_tokens,
    (SELECT COUNT(*) FROM token_blacklist WHERE expiry_date > NOW()) AS active_blacklist_entries,
    (SELECT COUNT(*) FROM login_attempts WHERE attempt_time > DATE_SUB(NOW(), INTERVAL 24 HOUR)) AS login_attempts_24h,
    (SELECT COUNT(*) FROM login_attempts WHERE success = FALSE AND attempt_time > DATE_SUB(NOW(), INTERVAL 24 HOUR)) AS failed_attempts_24h;

SELECT 'Monitoring views created successfully' AS status;

-- ============================================================================
-- STEP 10: Grant Permissions (if using separate app user)
-- ============================================================================

-- If you have a separate application database user, grant permissions:
-- GRANT SELECT, INSERT, UPDATE, DELETE ON mediatech_db_v2.refresh_tokens TO 'mediatech_app'@'localhost';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON mediatech_db_v2.token_blacklist TO 'mediatech_app'@'localhost';
-- GRANT SELECT, INSERT, UPDATE, DELETE ON mediatech_db_v2.login_attempts TO 'mediatech_app'@'localhost';
-- GRANT SELECT, UPDATE ON mediatech_db_v2.users TO 'mediatech_app'@'localhost';
-- FLUSH PRIVILEGES;

-- ============================================================================
-- STEP 11: Verification Queries
-- ============================================================================

-- Verify all tables exist
SELECT 
    'VERIFICATION: All required tables exist' AS status,
    (SELECT COUNT(*) FROM information_schema.tables 
     WHERE table_schema = 'mediatech_db_v2' 
     AND table_name IN ('users', 'refresh_tokens', 'token_blacklist', 'login_attempts')) AS table_count,
    '4 expected' AS expected;

-- Verify users table has new columns
SELECT 
    'VERIFICATION: Users table has lockout columns' AS status,
    (SELECT COUNT(*) FROM information_schema.columns 
     WHERE table_schema = 'mediatech_db_v2' 
     AND table_name = 'users' 
     AND column_name IN ('account_locked', 'lockout_time', 'failed_attempts')) AS column_count,
    '3 expected' AS expected;

-- ============================================================================
-- STEP 12: Final Summary
-- ============================================================================

SELECT '========================================' AS '';
SELECT 'MIGRATION COMPLETED SUCCESSFULLY!' AS 'STATUS';
SELECT '========================================' AS '';
SELECT '' AS '';
SELECT 'Summary:' AS '';
SELECT '- Users table enhanced with lockout fields' AS 'Step 1';
SELECT '- Refresh tokens table created' AS 'Step 2';
SELECT '- Token blacklist table created' AS 'Step 3';
SELECT '- Login attempts table created' AS 'Step 4';
SELECT '- Stored procedures created' AS 'Step 5';
SELECT '- Monitoring views created' AS 'Step 6';
SELECT '' AS '';
SELECT 'Next Steps:' AS '';
SELECT '1. Restart the Spring Boot application' AS 'Action';
SELECT '2. Test login with new token system' AS 'Action';
SELECT '3. Verify scheduled cleanup jobs are running' AS 'Action';
SELECT '4. Monitor v_security_statistics view' AS 'Action';
SELECT '' AS '';
SELECT 'For rollback, restore from backup:' AS 'Rollback';
SELECT 'mysql -u root mediatech_db_v2 < full_backup.sql' AS 'Command';
SELECT '' AS '';

-- ============================================================================
-- Useful Queries for Monitoring
-- ============================================================================

-- Query: Check security statistics
-- SELECT * FROM v_security_statistics;

-- Query: View locked accounts
-- SELECT * FROM v_locked_accounts;

-- Query: View recent login activity
-- SELECT * FROM v_recent_login_activity LIMIT 20;

-- Query: Manually unlock a user
-- CALL unlock_user_account('username_here');

-- Query: View user security status
-- CALL view_user_security_status('username_here');

-- Query: Manual cleanup
-- CALL cleanup_security_data();

-- ============================================================================
-- END OF MIGRATION SCRIPT
-- ============================================================================
