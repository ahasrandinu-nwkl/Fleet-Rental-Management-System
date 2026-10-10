/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lk.ijse.fleet;

public class Car extends Vehicle {
    public Car(String registrationNumber, String brand, String model, double dailyRate, FuelType fuelType) {
        super(registrationNumber, brand, model, dailyRate, fuelType);
    }

    @Override
    public double calculateRentalCost(int rentalDays) {
        return getDailyRate() * rentalDays * getFuelType().getCostMultiplier();
    }
}
