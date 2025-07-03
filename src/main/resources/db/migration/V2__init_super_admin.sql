-- Create super admin account
INSERT INTO
    account (full_name, phone, email, password, role, status)
VALUES
    (
        'Super Admin',
        '0123456789',
        'goodmintonadm@gmail.com',
        '$2a$10$thPE6RvZTakNYb9jNTAhTeBpFFITPXWOfoYwHCzrcMVQrQbS906iC', -- bcrypt: Admin@123
        'SUPER_ADMIN',
        'ACTIVE'
    );