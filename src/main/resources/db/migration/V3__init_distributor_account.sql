-- Create distributor account
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM account WHERE email = 'goodmintondistributor@gmail.com'
    ) THEN
        INSERT INTO account (full_name, phone, email, password, role, create_at, status)
        VALUES (
            'Distributor',
            '01111111111',
            'goodmintondistributor@gmail.com',
            '$2a$10$mOZHH.PSVg199Rjjyolqc.tqMvP8A/zob8XvqbP1EIuyQvQ9mOaZu', -- bcrypt: Distributor@123
            'DISTRIBUTOR',
            CURRENT_TIMESTAMP,
            'ACTIVE'
        );
    END IF;
END $$;