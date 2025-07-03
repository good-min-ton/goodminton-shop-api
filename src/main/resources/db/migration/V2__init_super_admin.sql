-- Create super admin account
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM account WHERE email = 'admin@goodminton.com'
    ) THEN
        INSERT INTO account (full_name, phone, email, password, role, status)
        VALUES (
            'Super Admin',
            '0999999999',
            'admin@goodminton.com',
            '$2a$10$5bCEzE7L.LcrZp/CcnFY5Ov7LkP9Qpi8nWB7z8DqINh2tYOG4cKu6', -- bcrypt: Admin@123
            'SUPER_ADMIN',
            'ACTIVE'
        );
    END IF;
END $$;