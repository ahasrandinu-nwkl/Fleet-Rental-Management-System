package lk.ijse.fleet;

import java.util.Scanner;

public class Main {
    private FleetManager fleetManager;
    private UserManager userManager;
    private Scanner scanner;
    private SystemUser loggedInUser;

    public Main() {
        this.fleetManager = new FleetManager();
        this.userManager = new UserManager();
        this.scanner = new Scanner(System.in);
        this.loggedInUser = null;
    }

    public static void main(String[] args) {
        new Main().run();
    }

    public void run() {
        userManager.registerUser("ahas", "1234", "Nadeesha Perera");

        Thread statusThread = new Thread(new VehicleStatusMonitor(fleetManager));
        statusThread.setDaemon(true);
        statusThread.start();

        Thread overdueThread = new Thread(new OverdueReminderWorker(fleetManager));
        overdueThread.setDaemon(true);
        overdueThread.start();

        fleetManager.setRentalEventListener(agreement -> {
            System.out.println("[EVENT] New rental created: " + agreement.getAgreementId());
        });

        fleetManager.addVehicle(new Car("CAB-1234", "Toyota", "Corolla", 5000.0, FuelType.PETROL));
        fleetManager.addVehicle(new Van("WP-5678", "Toyota", "HiAce", 8000.0, FuelType.DIESEL, 10));
        fleetManager.addCustomer(new IndividualCustomer("Kasun Perera", "0771234567", 150));
        fleetManager.addCustomer(new CorporateCustomer("Coral Reef Traders", "0112233445", 50000.0));

        while (true) {
            if (loggedInUser == null) {
                showAuthMenu();
            } else {
                showMainMenu();
            }
        }
    }

    private void showAuthMenu() {
        System.out.println("\n==========================================");
        System.out.println("   FLEET RENTAL MANAGEMENT SYSTEM (FRMS)  ");
        System.out.println("==========================================");
        System.out.println("[1] Login");
        System.out.println("[2] Register New User");
        System.out.println("[0] Exit");
        System.out.print("Enter choice > ");

        try {
            int choice = Integer.parseInt(scanner.nextLine());
            switch (choice) {
                case 1:
                    System.out.print("Enter username: ");
                    String uname = scanner.nextLine();
                    System.out.print("Enter password: ");
                    String pass = scanner.nextLine();
                    loggedInUser = userManager.login(uname, pass);
                    if (loggedInUser != null) {
                        System.out.println("Welcome, " + loggedInUser.getFullName() + "! Loading fleet dashboard...");
                    } else {
                        System.out.println("Invalid username or password.");
                    }
                    break;
                case 2:
                    System.out.print("Enter new username: ");
                    String regUname = scanner.nextLine();
                    System.out.print("Enter password: ");
                    String regPass = scanner.nextLine();
                    System.out.print("Enter full name: ");
                    String regName = scanner.nextLine();
                    userManager.registerUser(regUname, regPass, regName);
                    break;
                case 0:
                    System.out.println("Exiting system. Goodbye!");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid numeric choice.");
        }
    }

    private void showMainMenu() {
        System.out.println("\n------------------------------------------");
        System.out.println("FRMS MAIN MENU | Logged in as: " + loggedInUser.getFullName());
        System.out.println("------------------------------------------");
        System.out.println("[1] Add Vehicle");
        System.out.println("[2] Add Customer");
        System.out.println("[3] View Available Vehicles");
        System.out.println("[4] Create Rental Agreement");
        System.out.println("[5] Return Vehicle");
        System.out.println("[6] View Revenue Report");
        System.out.println("[7] View Vehicles by Fuel Type");
        System.out.println("[8] View Combined Fleet & Customer Report (Reportable)");
        System.out.println("[9] View All Customers");
        System.out.println("[10] View All Rental Agreements");
        System.out.println("[0] Logout");
        System.out.print("Enter choice > ");

        try {
            int choice = Integer.parseInt(scanner.nextLine());
            switch (choice) {
                case 1:
                    System.out.print("Enter type (Car/Van/Truck): ");
                    String type = scanner.nextLine().trim();
                    System.out.print("Registration Number: ");
                    String regNo = scanner.nextLine();
                    System.out.print("Brand: ");
                    String brand = scanner.nextLine();
                    System.out.print("Model: ");
                    String model = scanner.nextLine();
                    System.out.print("Daily Rate: ");
                    double rate = Double.parseDouble(scanner.nextLine());
                    System.out.print("Fuel Type (PETROL/DIESEL/ELECTRIC/HYBRID): ");
                    FuelType fType = FuelType.valueOf(scanner.nextLine().toUpperCase());

                    if (type.equalsIgnoreCase("Car")) {
                        fleetManager.addVehicle(new Car(regNo, brand, model, rate, fType));
                        System.out.println("Car added successfully!");
                    } else if (type.equalsIgnoreCase("Van")) {
                        System.out.print("Passenger Capacity: ");
                        int cap = Integer.parseInt(scanner.nextLine());
                        fleetManager.addVehicle(new Van(regNo, brand, model, rate, fType, cap));
                        System.out.println("Van added successfully!");
                    } else if (type.equalsIgnoreCase("Truck")) {
                        System.out.print("Cargo Capacity (Tons): ");
                        double cargo = Double.parseDouble(scanner.nextLine());
                        fleetManager.addVehicle(new Truck(regNo, brand, model, rate, fType, cargo));
                        System.out.println("Truck added successfully!");
                    } else {
                        System.out.println("Invalid vehicle type.");
                    }
                    break;

                case 2:
                    System.out.print("Enter customer type (Individual/Corporate): ");
                    String cType = scanner.nextLine().trim();
                    System.out.print("Full Name: ");
                    String name = scanner.nextLine();
                    System.out.print("Contact Number: ");
                    String contact = scanner.nextLine();

                    if (cType.equalsIgnoreCase("Individual")) {
                        System.out.print("Initial Loyalty Points: ");
                        int points = Integer.parseInt(scanner.nextLine());
                        fleetManager.addCustomer(new IndividualCustomer(name, contact, points));
                        System.out.println("Individual customer added successfully!");
                    } else if (cType.equalsIgnoreCase("Corporate")) {
                        System.out.print("Credit Limit: ");
                        double limit = Double.parseDouble(scanner.nextLine());
                        fleetManager.addCustomer(new CorporateCustomer(name, contact, limit));
                        System.out.println("Corporate customer added successfully!");
                    } else {
                        System.out.println("Invalid customer type.");
                    }
                    break;

                case 3:
                    System.out.println("\n--- AVAILABLE VEHICLES ---");
                    for (Vehicle v : fleetManager.getAvailableVehicles()) {
                        System.out.println(v);
                    }
                    break;

                case 4:
                    System.out.print("Enter Vehicle ID: ");
                    String vId = scanner.nextLine();
                    System.out.print("Enter Customer ID: ");
                    String cId = scanner.nextLine();
                    System.out.print("Enter Rental Days: ");
                    int days = Integer.parseInt(scanner.nextLine());

                    try {
                        RentalAgreement agreement = fleetManager.createRentalAgreement(vId, cId, days);
                        if (agreement != null) {
                            System.out.println(agreement);
                        }
                    } catch (VehicleNotAvailableException | InsufficientCreditException e) {
                        System.out.println("Rental failed: " + e.getMessage());
                    }
                    break;

                case 5:
                    System.out.print("Enter Agreement ID to return: ");
                    String agrId = scanner.nextLine();
                    fleetManager.returnVehicle(agrId);
                    break;

                case 6:
                    System.out.println("\n=== FLEET REVENUE REPORT ===");
                    System.out.println("Total Revenue: " + String.format("%.2f", fleetManager.generateRevenueReport()));
                    break;

                case 7:
                    System.out.print("Enter Fuel Type (PETROL/DIESEL/ELECTRIC/HYBRID): ");
                    FuelType searchFuel = FuelType.valueOf(scanner.nextLine().toUpperCase());
                    System.out.println("\n--- VEHICLES FOR " + searchFuel + " ---");
                    for (Vehicle v : fleetManager.getVehiclesByFuelType(searchFuel)) {
                        System.out.println(v);
                    }
                    break;

                case 8:
                    System.out.println("\n=== COMBINED REPORTABLE POLYMORPHISM REPORT ===");
                    fleetManager.printAllReportables();
                    break;

                case 9:
                    System.out.println("\n--- ALL CUSTOMERS ---");
                    for (Customer c : fleetManager.getAllCustomers()) {
                        System.out.println(c);
                    }
                    break;

                case 10:
                    System.out.println("\n--- ALL RENTAL AGREEMENTS ---");
                    for (RentalAgreement a : fleetManager.getAllAgreements()) {
                        System.out.println(a);
                    }
                    break;

                case 0:
                    loggedInUser = null;
                    System.out.println("Logged out successfully.");
                    break;

                default:
                    System.out.println("Invalid menu choice.");
            }
        } catch (Exception e) {
            System.out.println("Error: Please check your input format. (" + e.getMessage() + ")");
        }
    }
}