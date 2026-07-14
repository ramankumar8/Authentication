-- =============================================
-- INITIAL AUTHENTICATION SCHEMA
-- =============================================

-- Enable UUID extension (PostgreSQL specific)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =============================================
-- USERS TABLE
-- =============================================

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL UNIQUE,

    salutation TEXT,
    first_name VARCHAR(255),
    middle_name VARCHAR(255),
    last_name VARCHAR(255),
    full_name VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    phone VARCHAR(20) UNIQUE,
    password VARCHAR(255),

    role VARCHAR(50) NOT NULL DEFAULT 'USER',

    is_email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    is_phone_verified BOOLEAN NOT NULL DEFAULT FALSE,
    is_locked BOOLEAN NOT NULL DEFAULT FALSE,

    is_2fa_enabled BOOLEAN NOT NULL DEFAULT FALSE,

    status TEXT DEFAULT 'active',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    deleted_at TIMESTAMP,
    deleted_by BIGINT
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_phone ON users(phone);
CREATE INDEX idx_users_public_id ON users(public_id);

-- =============================================
-- REFRESH TOKENS
-- =============================================

CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    public_id UUID NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    token TEXT NOT NULL UNIQUE,
    expiry_time TIMESTAMP NOT NULL,

    user_agent VARCHAR(500),
    device_name VARCHAR(255),
    ip_address VARCHAR(100),

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,

    status TEXT DEFAULT 'active',

    CONSTRAINT fk_refresh_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_refresh_public_id ON refresh_tokens(public_id);
CREATE INDEX idx_refresh_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_status ON refresh_tokens(status);
CREATE INDEX idx_refresh_expiry ON refresh_tokens(expiry_time);

-- =============================================
-- OTP TOKENS
-- =============================================

CREATE TABLE otp (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    channel TEXT NOT NULL,
    otp TEXT NOT NULL,
    expiry_time TIMESTAMP NOT NULL,
    is_used BOOLEAN DEFAULT FALSE,
    purpose TEXT NOT NULL CHECK (
                             purpose IN (
                                 'LOG_IN',
                                 'PASSWORD_RESET',
                                 'EMAIL_VERIFICATION',
                                 'PHONE_VERIFICATION',
                                 'TWO_FACTOR_AUTH'
                             )
                         ),
    attempt_count BIGINT DEFAULT 0,
    status TEXT DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    deleted_at TIMESTAMP DEFAULT NULL,

    CONSTRAINT fk_otp_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_created_by_user
        FOREIGN KEY (created_by)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_otp_user ON otp(user_id);

-- =============================================
-- PASSWORD RESET TOKENS
-- =============================================

CREATE TABLE password_reset_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL,
    expiry_time TIMESTAMP NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_reset_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_reset_token ON password_reset_tokens(token);

-- =============================================
-- EMAIL VERIFICATION TOKENS
-- =============================================

CREATE TABLE email_verification_tokens (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    otp TEXT NOT NULL,
    expiry_time TIMESTAMP NOT NULL,
    is_used BOOLEAN DEFAULT FALSE,
    purpose TEXT NOT NULL CHECK (
                         purpose IN (
                             'LOG_IN',
                             'PASSWORD_RESET',
                             'EMAIL_VERIFICATION',
                             'PHONE_VERIFICATION',
                             'TWO_FACTOR_AUTH'
                         )
                     ),
    attempt_count INT DEFAULT 0,
    status TEXT DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    deleted_at TIMESTAMP DEFAULT NULL,

    CONSTRAINT fk_email_verification_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_created_by_user
        FOREIGN KEY (created_by)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_email_verify_token ON email_verification_tokens(user_id);

-- =============================================
-- END OF INITIAL SCHEMA
-- =============================================