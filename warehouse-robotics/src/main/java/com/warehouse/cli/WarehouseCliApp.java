package com.warehouse.cli;

import com.warehouse.db.DatabaseManager;
import com.warehouse.facade.WarehouseAdminFacade;
import com.warehouse.facade.WarehouseWorkflowFacade;
import com.warehouse.subsystemA.ShelfInventory;

import java.util.Scanner;

public class WarehouseCliApp {
    public static void main(String[] args) {
        DatabaseManager dbManager = new DatabaseManager("warehouse_live.db");
        ShelfInventory inventory = new ShelfInventory(100);
        WarehouseWorkflowFacade workflowFacade = new WarehouseWorkflowFacade(dbManager, inventory);
        WarehouseAdminFacade adminFacade = new WarehouseAdminFacade(dbManager, workflowFacade);

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n=== WAREHOUSE ROBOTICS CLI ===");
            System.out.println("1. Run Happy Path (A -> B -> C)");
            System.out.println("2. Run Scenario: Subsystem B Fails (A -> B[Fail] -> Compensation A)");
            System.out.println("3. Run Scenario: Subsystem C Fails (A -> B -> C[Fail] -> Compensation B -> A)");
            System.out.println("4. Inspect Order State");
            System.out.println("5. Trigger Manual Compensation");
            System.out.println("6. Reset Database");
            System.out.println("7. Exit");
            System.out.print("Select an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice == 7) break;

            switch (choice) {
                case 1:
                    System.out.print("Enter Order ID: ");
                    String id1 = scanner.nextLine();
                    boolean res1 = workflowFacade.executeFulfillmentChain(id1, "SKU-ITEM", 10, "ROBOT-01", "STATION-A", false, false);
                    System.out.println("Execution result: " + (res1 ? "SUCCESS" : "FAILED & COMPENSATED"));
                    break;
                case 2:
                    System.out.print("Enter Order ID: ");
                    String id2 = scanner.nextLine();
                    boolean res2 = workflowFacade.executeFulfillmentChain(id2, "SKU-ITEM", 10, "ROBOT-01", "STATION-A", true, false);
                    System.out.println("Execution result: " + (res2 ? "SUCCESS" : "FAILED & COMPENSATED"));
                    break;
                case 3:
                    System.out.print("Enter Order ID: ");
                    String id3 = scanner.nextLine();
                    boolean res3 = workflowFacade.executeFulfillmentChain(id3, "SKU-ITEM", 10, "ROBOT-01", "STATION-A", false, true);
                    System.out.println("Execution result: " + (res3 ? "SUCCESS" : "FAILED & COMPENSATED"));
                    break;
                case 4:
                    System.out.print("Enter Order ID: ");
                    String id4 = scanner.nextLine();
                    System.out.println(adminFacade.inspectOrderState(id4));
                    break;
                case 5:
                    System.out.print("Enter Order ID: ");
                    String id5 = scanner.nextLine();
                    adminFacade.triggerManualCompensation(id5);
                    System.out.println("Manual compensation triggered.");
                    break;
                case 6:
                    dbManager.clearAllTables();
                    System.out.println("Database cleared.");
                    break;
            }
        }
        scanner.close();
    }
}