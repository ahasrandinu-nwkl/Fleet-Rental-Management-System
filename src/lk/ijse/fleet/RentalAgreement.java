package lk.ijse.fleet;

public class RentalAgreement {
    private static int counter = 1001;
    private String agreementId;
    private Vehicle vehicle;
    private Customer customer;
    private int rentalDays;
    private double totalCost;
    private int daysElapsed;

    public RentalAgreement(Vehicle vehicle, Customer customer, int rentalDays) {
        this.agreementId = "AGR-" + counter++;
        this.vehicle = vehicle;
        this.customer = customer;
        this.rentalDays = rentalDays;
        
        double subtotal = vehicle.calculateRentalCost(rentalDays);
        this.totalCost = customer.applyDiscount(subtotal);
        this.daysElapsed = 0;
    }

    public String getAgreementId() { return agreementId; }
    public Vehicle getVehicle() { return vehicle; }
    public Customer getCustomer() { return customer; }
    public int getRentalDays() { return rentalDays; }
    public double getTotalCost() { return totalCost; }
    public int getDaysElapsed() { return daysElapsed; }
    public void setDaysElapsed(int daysElapsed) { this.daysElapsed = daysElapsed; }

    @Override
    public String toString() {
        String custType = (customer instanceof CorporateCustomer) ? "Corporate" : "Individual";
        return "\n--- RENTAL AGREEMENT RECEIPT ---\n" +
               "Agreement ID: " + agreementId + "\n" +
               "Vehicle: " + vehicle.getVehicleId() + " - " + vehicle.getBrand() + " " + vehicle.getModel() + "\n" +
               "Customer: " + customer.getCustomerId() + " - " + customer.getFullName() + " (" + custType + ")\n" +
               "Rental Days: " + rentalDays + "\n" +
               "Total Cost: " + String.format("%.2f", totalCost);
    }
}