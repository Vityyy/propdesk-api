CREATE TABLE users (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE admins (
    id UUID PRIMARY KEY,
    CONSTRAINT fk_admins_users
        FOREIGN KEY (id) REFERENCES users (id)
);

CREATE TABLE owners (
    id UUID PRIMARY KEY,
    admin_id UUID,
    admin_cut NUMERIC(38, 2),
    admin_association_accepted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_owners_users
        FOREIGN KEY (id) REFERENCES users (id),
    CONSTRAINT fk_owners_admins
        FOREIGN KEY (admin_id) REFERENCES admins (id)
);

CREATE TABLE properties (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(255) NOT NULL,
    image_url VARCHAR(255),
    owner_id UUID NOT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_properties_owners
        FOREIGN KEY (owner_id) REFERENCES owners (id),
    CONSTRAINT uk_properties_owner_name_address
        UNIQUE (name, address, owner_id)
);

CREATE TABLE tenant (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(255),
    email VARCHAR(255)
);

CREATE TABLE apartments (
    id UUID PRIMARY KEY,
    number INTEGER NOT NULL,
    due_date DATE,
    payment_status VARCHAR(255),
    square_meters NUMERIC(38, 2) NOT NULL,
    floor INTEGER NOT NULL,
    rent NUMERIC(38, 2) NOT NULL,
    tenant_id UUID,
    property_id UUID,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_apartments_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenant (id),
    CONSTRAINT fk_apartments_properties
        FOREIGN KEY (property_id) REFERENCES properties (id)
);

CREATE TABLE expenses (
    id UUID PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    amount NUMERIC(38, 2) NOT NULL,
    apartment_id UUID NOT NULL,
    CONSTRAINT fk_expenses_apartments
        FOREIGN KEY (apartment_id) REFERENCES apartments (id)
);

CREATE TABLE maintenance_fees (
    id UUID PRIMARY KEY,
    category VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    amount NUMERIC(38, 2) NOT NULL,
    apartment_id UUID NOT NULL,
    CONSTRAINT fk_maintenance_fees_apartments
        FOREIGN KEY (apartment_id) REFERENCES apartments (id)
);

CREATE TABLE payments (
    id UUID PRIMARY KEY,
    amount NUMERIC(38, 2) NOT NULL,
    payment_date DATE NOT NULL,
    type VARCHAR(255) NOT NULL,
    billing_month INTEGER,
    billing_year INTEGER,
    is_cancelled BOOLEAN NOT NULL DEFAULT FALSE,
    apartment_id UUID NOT NULL,
    CONSTRAINT fk_payments_apartments
        FOREIGN KEY (apartment_id) REFERENCES apartments (id)
);

CREATE INDEX idx_owners_admin_id ON owners (admin_id);
CREATE INDEX idx_properties_owner_id ON properties (owner_id);
CREATE INDEX idx_apartments_property_id ON apartments (property_id);
CREATE INDEX idx_apartments_tenant_id ON apartments (tenant_id);
CREATE INDEX idx_expenses_apartment_id ON expenses (apartment_id);
CREATE INDEX idx_maintenance_fees_apartment_id ON maintenance_fees (apartment_id);
CREATE INDEX idx_payments_apartment_billing
    ON payments (apartment_id, type, billing_year, billing_month, is_cancelled);
