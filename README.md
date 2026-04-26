
# Camo-Gear Bistro

A restaurant management system built with JavaFX for COMP1322 Programming II coursework.

## Overview

Camo-Gear Bistro is a complete point-of-sale system featuring character-themed menus, order management, inventory control, sales analytics, and customer feedback. Supports two user roles: Customers (place orders, view history) and Admins (manage inventory, view sales reports, handle orders).

## Tech Stack

- **Java:** 17+
- **JavaFX:** 21
- **Database:** SQLite
- **Build Tool:** Maven
- **Testing:** JUnit 5
- **Exporting:** iTextPDF

## Setup

1. Clone or download the project folder.
2. Open the project in IntelliJ IDEA (or your preferred Java IDE).
3. Ensure Maven dependencies are loaded.
4. Locate `src/main/java/com/files/projBistro/models/Main.java`.
5. Run `Main.main()`.

The SQLite database (`bistroTrue.db`) will be created automatically in the project root on the first run.

## Default Login

| Role | Username | Password | Admin PIN |
|------|----------|----------|-----------|
| Admin | `admin` | `secure123` | `1234` |
| Customer | Register new account | N/A | N/A |

## Features

### Customer
- **Login/Register:** Phone number validation included.
- **Character Menus:** Four distinct themed menus (Chloe, Mimi, Metsu, Lanaird).
- **Shopping Cart:** Real-time total calculation and item management.
- **Payment:** Support for Counter Payment or Credit Card simulation.
- **Invoices:** Automatic PDF generation upon successful checkout.
- **Feedback:** Star rating and review system for order satisfaction.
- **History:** View past orders with a quick "reorder" functionality.
- **Customization:** Light and Dark theme toggle for better accessibility.

### Admin
- **PIN Access:** Secondary 4-digit PIN protection for sensitive areas.
- **Inventory CRUD:** Full control to add, edit, or delete menu items.
- **Stock Management:** Bulk item addition and low stock alerts (threshold: 20).
- **Order Management:** Live dashboard to track and process active orders.
- **Analytics:** Sales reports with custom date filtering.
- **Exports:** Export sales data and inventory to CSV or PDF.
- **Dialogue System:** Capability to edit character dialogue lines.
- **Loyalty Tracking:** Basic tracking of customer interaction and feedback.

## Testing

Run the automated test suite via your IDE or using Maven:
```bash
mvn test
```

Test coverage includes input validation logic, DAO CRUD operations, sales total calculations, and file export formatting.

## Project Structure
Please refer to the project structure diagram in `project_structure.txt`.

## Dependencies

- **JavaFX 21:** (controls, fxml, media)
- **SQLite JDBC:** 3.45.1.0
- **iTextPDF:** 7.2.5
- **JUnit:** 5.12.1

## Author

**Sheri Anne Chong** <br> 
University of Southampton Malaysia <br>
COMP1322 Programming II  
**Submission Date:** 27 April 2026

## Academic Integrity
This project was developed individually. 
No Generative AI was used for core logic or code generation (Tier 1 compliance).
```