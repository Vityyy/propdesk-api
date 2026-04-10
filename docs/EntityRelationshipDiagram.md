```mermaid
erDiagram
    USERS {
        id UUID PK
        name Text
        email Text UK
        phone_number Text
    }

    OWNERS {
        id UUID PK, FK
        admin_id UUID FK
        admin_cut Decimal
    }

    ADMINS {
        id UUID PK, FK
    }

    PROPERTIES {
        id UUID PK
        name Text UK
        address Text UK
        owner_id UUID FK, UK
    }

    EXPENSES {
        id UUID PK
        category Enumeration
        description Text UK
        amount Decimal
        date Date
        payment_status Enumeration
        property_id UUID FK, UK
    }

    APARTMENTS {
        id UUID PK
        name Text UK
        property_id UUID UK, FK
        tenant_id UUID FK
    }

    TENANTS {
        id UUID PK
        name Text
        due_date Date
        amount_due Decimal
        status Enumeration
        email Text
        phone_number Text
    }

    USERS ||--|| OWNERS: "is"
    USERS ||--|| ADMINS: "is"
    OWNERS }|--|o ADMINS: "works with"
    PROPERTIES }|--|| OWNERS: "owns"
    EXPENSES }o--|| PROPERTIES: "from"
    APARTMENTS }|--|| PROPERTIES: "from"
    TENANTS o|--o{ APARTMENTS: "rents"
