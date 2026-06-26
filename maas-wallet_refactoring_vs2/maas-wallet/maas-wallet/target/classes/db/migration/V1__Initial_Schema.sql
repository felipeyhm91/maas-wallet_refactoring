-- 1. Criação dos Schemas Lógicos para Isolamento por Módulo
CREATE SCHEMA IF NOT EXISTS auth;
CREATE SCHEMA IF NOT EXISTS wallet;
CREATE SCHEMA IF NOT EXISTS trips;
CREATE SCHEMA IF NOT EXISTS rewards;

-- 2. Tabela de Usuários (Módulo Auth)
CREATE TABLE auth.users (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    document VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    type VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL
);

-- 3. Tabela de Carteiras (Módulo Wallet)
CREATE TABLE wallet.wallets (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL UNIQUE,
    balance DECIMAL(18, 2) NOT NULL DEFAULT 0.00,
    cashback DECIMAL(18, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(20) NOT NULL
);

-- 4. Tabela do Livro-Razão Imutável (Ledger - Módulo Wallet)
CREATE TABLE wallet.ledger_entries (
    id VARCHAR(36) PRIMARY KEY,
    wallet_id VARCHAR(36) NOT NULL,
    amount DECIMAL(18, 2) NOT NULL,
    type VARCHAR(30) NOT NULL,
    description VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    reference_id VARCHAR(36),
    CONSTRAINT fk_ledger_wallet FOREIGN KEY (wallet_id) REFERENCES wallet.wallets(id)
);

-- 5. Tabela de Parceiros de Mobilidade (Módulo Trips)
CREATE TABLE trips.partners (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    api_key VARCHAR(100) NOT NULL
);

-- 6. Tabela de Viagens (Módulo Trips)
CREATE TABLE trips.trips (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    partner_id VARCHAR(36) NOT NULL,
    modal VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    price DECIMAL(18, 2) NOT NULL,
    cashback_amount DECIMAL(18, 2) NOT NULL DEFAULT 0.00,
    origin VARCHAR(255) NOT NULL,
    destination VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    partner_trip_id VARCHAR(100),
    CONSTRAINT fk_trip_partner FOREIGN KEY (partner_id) REFERENCES trips.partners(id)
);

-- 7. Tabela de Campanhas de Cashback (Módulo Rewards)
CREATE TABLE rewards.campaigns (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    percentage DECIMAL(5, 2) NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    modal_eligible VARCHAR(30) NOT NULL,
    user_limit DECIMAL(18, 2) NOT NULL,
    campaign_limit DECIMAL(18, 2) NOT NULL,
    status VARCHAR(20) NOT NULL
);

-- 8. Massa de Dados Inicial para Testes e Demonstração

-- Parceiros Padrão
INSERT INTO trips.partners (id, name, status, api_key) VALUES 
('p1-sptrans-uuid-000000000001', 'SPTrans', 'ACTIVE', 'sptrans-key-123'),
('p2-uber-uuid-00000000000002', 'Uber', 'ACTIVE', 'uber-key-456'),
('p3-99-uuid-0000000000000003', '99 App', 'ACTIVE', '99-key-789'),
('p4-bikeshare-uuid-0000000004', 'Bike Sampa', 'ACTIVE', 'bike-key-012'),
('p5-scooter-uuid-00000000005', 'Scooter GO', 'ACTIVE', 'scooter-key-345');

-- Campanhas de Cashback Ativas
INSERT INTO rewards.campaigns (id, name, percentage, start_date, end_date, modal_eligible, user_limit, campaign_limit, status) VALUES
('c1-bike-uuid-00000000000001', 'Semana Ciclista Integrada', 15.00, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 'BIKE', 20.00, 1000.00, 'ACTIVE'),
('c2-uber-uuid-00000000000002', 'Cashback Especial Carro', 5.00, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 'RIDE_HAILING', 50.00, 5000.00, 'ACTIVE'),
('c3-sptrans-uuid-00000000003', 'Conexão Transporte Público', 10.00, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 'BUS', 30.00, 3000.00, 'ACTIVE');
