Feature: Maintenance Fees Management
  As an owner or admin
  I want to assign maintenance fees to apartments
  So that they are automatically paid when the apartment rent is paid

  Background:
    Given the database is initialized for maintenance fees testing
    And an authenticated owner user "owner@example.com" exists

  Scenario: Assign a maintenance fee to an apartment
    When the user "owner@example.com" assigns a maintenance fee of 50.00 with category "Utilities" to apartment 101
    Then the maintenance fee is successfully assigned to apartment 101

  Scenario: Maintenance fee payment is automatically generated when rent is paid
    Given apartment 102 has a maintenance fee of 100.00 with category "Maintenance"
    And apartment 102 has a payment status of "PENDING"
    When the user "owner@example.com" marks apartment 102 as "PAID"
    Then a payment of type "MAINTENANCE_FEE" for 100.00 is generated for apartment 102 for the current month

  Scenario: Maintenance fee payment is canceled when rent is unpaid
    Given apartment 103 has a maintenance fee of 75.00 with category "Insurance"
    And apartment 103 has a payment status of "PAID"
    And a payment of type "MAINTENANCE_FEE" for 75.00 exists for apartment 103 for the current month
    When the user "owner@example.com" marks apartment 103 as "PENDING"
    Then the payment of type "MAINTENANCE_FEE" for 75.00 for apartment 103 is marked as canceled
