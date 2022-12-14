# Graphical User Interface Prototype  

Authors: Elia Fontana, Andrea Palomba, Leonardo Perugini, Francesco Sattolo

Date: 21/04/2021

Version: 1.0

# Login 

Before every Use Case, users have to do the login, with which they enters in the EZShop application.

### 1) ![](Images/EZShop.png)
### 2) ![](Images/EZShop_login.png)
### 3) ![](Images/Menu_selection.png)

Depending on their role(s), they will have access to specific sections of the application:
 - Shop owner: all sections.
 - Cashier: cash register.
 - Inventory manager: catalogue (except for the order list button).
 - Accounting administrator: accounting.

### Forgot password

In case users forget their password, they can request a password reset by giving their email.

### 1) ![](Images/EZShop.png)
### 2) ![](Images/Forgot_password.png)
### 3) ![](Images/Request_password.png)

They will receive an email with a link to a page where they can reset their password.

### 4) ![](Images/Reset_password.png)


# UC1: Customer buys items
## Nominal Scenario
### 1) ![](Images/Menu_selection_cashregister.png)
### 2) ![](Images/Cash_register_empty.png)
### Cashier scans items
### 3) ![](Images/Cash_register_full.png)
### 4) ![](Images/Print_receipt.png)
### Cash register prints the receipt; return to point 2, ready for another transaction.

## Variant 1 - Scenario 1.1 - Customer with fidelity card
### (Same until point 3 in the Nominal Scenario)
### Cashier scans fidelity card
### ![](Images/Cash_register_full_fc.png)
### (Same after point 3 in the Nominal Scenario)

## Variant 2 - Scenario 1.2 - Payment failure
### (Same until point 2 in the Nominal Scenario)
### ![](Images/Abort_transaction.png)
### ![](Images/Abort_confirm.png)
### Return to point 2 in the Nominal Scenario, ready for another transaction.

# UC2: Shop owner/Inventory manager updates quantity of item in inventory
## Nominal Scenario
### 1) ![](Images/Menu_selection_catalogue.png)
### 2) ![](Images/Catalogue.png)
### User searches for the item (i.e., Vans ComfyCush)
### 3) ![](Images/Select_item.png)
### 4) ![](Images/Item.png)
### 5) ![](Images/Modify_quantity.png)

# UC3: Owner sees notifications
## Nominal Scenario 
### 1) ![](Images/Menu_selection_notifications.png)
### 2) ![](Images/Notification_list.png)

# UC4: Shop owner/Inventory manager changes threshold
## Nominal Scenario
### 1) ![](Images/Menu_selection_catalogue.png)
### 2) ![](Images/Catalogue.png)
### 3) ![](Images/Modify_threshold.png)

# UC5: Shop owner/Inventory manager adds item to catalogue
## Nominal Scenario
### 1) ![](Images/Menu_selection_catalogue.png)
### 2) ![](Images/Catalogue.png)
### 3) ![](Images/Add_new_item_catalogue.png)
### Fill all fields (if more sizes/colors are selected, more items will be created simultaneously)
### 4) ![](Images/Confirm_add_new_item.png)
### Return to point 2 in the Nominal Scenario, ready for other operations.

# UC6: Shop owner/Inventory manager removes item from catalogue
## Nominal Scenario
### 1) ![](Images/Menu_selection_catalogue.png)
### 2) ![](Images/Catalogue.png)
### User searches for the item (i.e., Vans ComfyCush)
### 3) ![](Images/Select_item.png)
### 4) ![](Images/Remove_item.png)
### 5) ![](Images/confirm_item_removal.png)

# UC7: Shop owner/Inventory manager updates item information
## Nominal Scenario
### 1) ![](Images/Menu_selection_catalogue.png)
### 2) ![](Images/Catalogue.png)
### User searches for the item (i.e., Vans ComfyCush)
### 3) ![](Images/Modify_discount.png)
### User clicks on any of the fields and type the desired values to update

# UC8: Shop owner/inventory manager adds item to order
## Nominal Scenario
### 1) ![](Images/Menu_selection_catalogue.png)
### 2) ![](Images/Catalogue.png)
### User searches for the item (i.e., Vans ComfyCush)
### 3) ![](Images/Select_item.png)
### 4) ![](Images/Modify_amount.png)
### User clicks on Amount and type the desired amount
### 5) ![](Images/Add_order.png)

# UC9: Shop owner removes item from order  
## Nominal Scenario
### 1) ![](Images/menu_selection_owner.png)
### 2) ![](Images/Order_list.png)
### 3) ![](Images/Remove_item_from_order.png)
### 4) ![](Images/confirm_item_removal_from_order.png)

# UC10: Shop owner places an order  
## Nominal Scenario
### 1) ![](Images/menu_selection_owner.png)
### 2) ![](Images/owner_section.png)
### 3) ![](Images/Order_list.png)
### 4) ![](Images/Select_supplier.png)
### 5) ![](Images/Send_order.png)

# UC11: Register fidelity card
## Nominal Scenario
### 1) ![](Images/Select_cash_register.png)
### 2) ![](Images/Select_create_new_fidelity.png)
### 3) ![](Images/add_costumer_info.png)
### Return to point 2, ready to create another fidelity card.

# UC12: Shop owner adds/removes employee
## Nominal Scenario
### 1) ![](Images/select_manage_employee.png)
### Add Employee
### 2) ![](Images/add_employee_button.png)
### 3) ![](Images/add_employee_fields.png)
### Remove Employee (same until point 1)
### 2) ![](Images/delete_field_employee.png)
### 3) ![](Images/confirm_employee_removal.png)
### After confirmation the application returns to Manage Employee section

# UC13: Shop owner changes employee's information
## Nominal Scenario
### 1) ![](Images/select_manage_employee.png)
### 2) ![](Images/employee_field_change.png)
### Owner selects which field to update

# UC15: Add transaction related to the shop
## Nominal Scenario
### 1) ![](Images/menu_selection_owner.png)
### 2) ![](Images/owner_section.png)
### Owner insert data about a specific transaction and add it to the accounting section
### 3) ![](Images/manageShop.png)

# UC16: Analize profits/losses
## Nominal Scenario
### 1) ![](Images/menu-selection_accounting.png)
### 2) ![](Images/accounting_ledger.png)
### 3) ![](Images/click_on_old_order.png)
### 4) ![](Images/old_orderList.png)
### User can switch and see also shop statistics 
### 5) ![](Images/accounting_change_page.png)
### 6) ![](Images/accounting_statistics.png)


