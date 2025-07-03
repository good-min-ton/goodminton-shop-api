-- Create super admin account
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM account WHERE email = 'admin@goodminton.com'
    ) THEN
        INSERT INTO account (full_name, phone, email, password, role, status)
        VALUES (
            'Super Admin',
            '0123456789',
            'goodmintonadm@gmail.com',
            '$2a$10$S0mZF2LQ2MfNxbJyX58tmuJhUe0LIcvIq8mTxzh8BAoa8g4QabY2S', -- bcrypt: Admin@123
            'SUPER_ADMIN',
            CURRENT_TIMESTAMP,
            'ACTIVE'
        );
    END IF;
END $$;