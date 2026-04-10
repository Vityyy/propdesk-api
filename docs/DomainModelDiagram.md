```mermaid
classDiagram
    class User {
        id [id]
        name
        email
        phone_number
    }
    
    class Owner {
        admin_cut
    }
    
    class Admin {
    }
    
    class Property {
        id [id]
        name
        address
    }
    
    class Apartment {
        id [id]
        name
    }
    
    class Tenant {
        id [id]
        name
        due date
        amount due
        status
        email
        phone_number
    }
    
    class Expense {
        id [id]
        category
        description
        amount
        date
        payment status
    }
    
    User <|-- Owner: is
    User <|-- Admin: is
    
    Admin "0..1" -- "*" Owner: works with
    Owner "1" -- "*" Property: owns
    Property "1" -- "1..*" Apartment: has
    Apartment "*" -- "0..1" Tenant: rents
    Expense "0..*" -- "1" Property: from
