package lk.ijse.fleet;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class FleetManager {
    private List<Vehicle> vehicles = new ArrayList<>();
    private List<Customer> customers = new ArrayList<>();
    private List<RentalAgreement> agreements = new ArrayList<>();
    private RentalEventListener rentalEventListener;

    public void addVehicle(Vehicle v) { vehicles.add(v); }
    public void addCustomer(Customer c) { customers.add(c); }

    public Vehicle findVehicleById(String id) {
        for (Vehicle v : vehicles) {
            if (v.getVehicleId().equalsIgnoreCase(id)) return v;
        }
        return null;
    }

    public Customer findCustomerById(String id) {
        for (Customer c : customers) {
            if (c.getCustomerId().equalsIgnoreCase(id)) return c;
        }
        return null;
    }

    public void setRentalEventListener(RentalEventListener listener) {
        this.rentalEventListener = listener;
    }

    public List<Vehicle> getAvailableVehicles() {
        return vehicles.stream()
                .filter(v -> v.getStatus() == RentalStatus.AVAILABLE)
                .collect(Collectors.toList());
    }

    public RentalAgreement createRentalAgreement(String vehicleId, String customerId, int rentalDays)
            throws VehicleNotAvailableException, InsufficientCreditException {
        
        Vehicle vehicle = findVehicleById(vehicleId);
        if (vehicle == null) {
            System.out.println("Vehicle not found.");
            return null;
        }
        if (vehicle.getStatus() != RentalStatus.AVAILABLE) {
            throw new VehicleNotAvailableException("Vehicle " + vehicleId + " is currently RENTED.");
        }

        Customer customer = findCustomerById(customerId);
        if (customer == null) {
            System.out.println("Customer not found.");
            return null;
        }

        double subtotal = vehicle.calculateRentalCost(rentalDays);
        double total = customer.applyDiscount(subtotal);

        if (customer instanceof CorporateCustomer) {
            CorporateCustomer corp = (CorporateCustomer) customer;
            if (!corp.hasSufficientCredit(total)) {
                throw new InsufficientCreditException("Corporate customer has insufficient credit limit.");
            }
            corp.setOutstandingBalance(corp.getOutstandingBalance() + total);
        }

        RentalAgreement agreement = new RentalAgreement(vehicle, customer, rentalDays);
        vehicle.setStatus(RentalStatus.RENTED);
        agreements.add(agreement);

        if (rentalEventListener != null) {
            rentalEventListener.onRentalCreated(agreement);
        }

        return agreement;
    }

    public void returnVehicle(String agreementId) {
        RentalAgreement target = null;
        for (RentalAgreement a : agreements) {
            if (a.getAgreementId().equalsIgnoreCase(agreementId)) {
                target = a;
                break;
            }
        }
        if (target != null) {
            target.getVehicle().setStatus(RentalStatus.AVAILABLE);
            System.out.println("Vehicle returned successfully! Status updated to AVAILABLE.");
        } else {
            System.out.println("Agreement ID not found.");
        }
    }

    public FleetSnapshot generateSnapshot() {
        return new FleetSnapshot();
    }

    public synchronized void printFleetStatus() {
        generateSnapshot().printSummary();
    }

    public synchronized void checkOverdueAgreements() {
        for (RentalAgreement a : agreements) {
            a.setDaysElapsed(a.getDaysElapsed() + 1);
            if (a.getDaysElapsed() > a.getRentalDays()) {
                System.out.println("\n[OVERDUE ALERT] Agreement " + a.getAgreementId() + " has exceeded its rental period!");
            }
        }
    }

    public List<Vehicle> getVehiclesByFuelType(FuelType type) {
        return vehicles.stream()
                .filter(v -> v.getFuelType() == type)
                .collect(Collectors.toList());
    }

    public List<Customer> getCustomersSortedByName() {
        return customers.stream()
                .sorted(Comparator.comparing(Customer::getFullName))
                .collect(Collectors.toList());
    }

    public double generateRevenueReport() {
        return agreements.stream()
                .mapToDouble(RentalAgreement::getTotalCost)
                .sum();
    }

    public void printAllReportables() {
        List<Reportable> reportables = new ArrayList<>();
        reportables.addAll(vehicles);
        reportables.addAll(customers);
        for (Reportable item : reportables) {
            System.out.println(item.getReportCategory() + ":" + item.getSummary());
        }
    }

    public List<Customer> getAllCustomers() { return customers; }
    public List<RentalAgreement> getAllAgreements() { return agreements; }

    public class FleetSnapshot {
        private int availableCount;
        private int rentedCount;
        private double totalRevenue;

        public FleetSnapshot() {
            int avail = 0;
            int rented = 0;
            for (Vehicle v : vehicles) {
                if (v.getStatus() == RentalStatus.AVAILABLE) avail++;
                else rented++;
            }
            this.availableCount = avail;
            this.rentedCount = rented;
            this.totalRevenue = generateRevenueReport();
        }

        public void printSummary() {
            System.out.println("\n[FLEET STATUS] Available: " + availableCount + 
                               " | Rented: " + rentedCount + 
                               " | Total Revenue So Far: " + String.format("%.2f", totalRevenue));
        }
    }
}