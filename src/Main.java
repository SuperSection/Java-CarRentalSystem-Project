import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

import org.apache.commons.lang3.RandomStringUtils;

class Car {
    private String carId;
    private String brand;
    private String model;
    private double basedPricePerDay;
    private int availableInStock;

    public Car(String carId, String brand, String model, double basedPricePerDay, int availableInStock) {
        this.carId = carId;
        this.brand = brand;
        this.model = model;
        this.basedPricePerDay = basedPricePerDay;
        this.availableInStock = availableInStock;
    }

    public String getCarId() {
        return carId;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public int getAvailableInStock() {
        return availableInStock;
    }

    public double calculatePrice(int rentalDays) {
        return (basedPricePerDay * rentalDays);
    }

    public void rent() {
        availableInStock -= 1;
    }

    public void returnCar() {
        availableInStock += 1;
    }
}

class Customer {
    private String customerId;
    private String customerName;

    public Customer(String customerId, String customerName) {
        this.customerId = customerId;
        this.customerName = customerName;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }
}

class Rental {
    private String rentalId;
    private Car car;
    private Customer customer;
    private int rentalDays;


    public Rental(Car car, Customer customer, int rentalDays) {
        this.rentalId = RandomStringUtils.randomNumeric(4) + RandomStringUtils.randomAlphabetic(3).toUpperCase();
        this.car = car;
        this.customer = customer;
        this.rentalDays = rentalDays;
    }

    public String getRentalId() {
        return rentalId;
    }

    public Car getCar() {
        return car;
    }

    public Customer getCustomer() {
        return customer;
    }

    public int getRentalDays() {
        return rentalDays;
    }
}

class CarRentalSystem {
    private List<Car> cars;
    private List<Customer> customers;
    private List<Rental> rentals;

    public CarRentalSystem() {
        cars = new ArrayList<>();
        customers = new ArrayList<>();
        rentals = new ArrayList<>();
    }

    public void addCar(Car car) {
        cars.add(car);
    }

    public void addCustomer(Customer customer) {
        customers.add(customer);
    }

    public void rentCar(Car car, Customer customer, int days) {
        if (car.getAvailableInStock() != 0) {
            car.rent();
            Rental newRent = new Rental(car, customer, days);
            rentals.add(newRent);
            System.out.println("Rental ID: " + newRent.getRentalId());
        } else {
            System.out.println("Car is not available for rent.");
        }
    }

    public void returnCar(String rentalId) {
        Rental rentalToRemove = null;
        for (Rental rental: rentals) {
            if (Objects.equals(rental.getRentalId(), rentalId)) {
                rentalToRemove = rental;
                break;
            }
        }

        if (rentalToRemove != null) {
            rentals.remove(rentalToRemove);
            System.out.println("Car returned successfully.");
        } else {
            System.out.println("Car was not rented.");
        }
    }

    public void menu() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n====== Car Rental System =======");
            System.out.println("1. Rent a Car");
            System.out.println("2. Return a Car");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume new line

            if (choice == 1) {
                System.out.println("\n---- RENT A CAR ----\n");
                System.out.print("Enter your name: ");
                String customerName = scanner.nextLine();

                System.out.println("\nAvailable Cars: ");
                for (Car car: cars) {
                    if (car.getAvailableInStock() != 0) {
                        System.out.println(car.getCarId() + " - " + car.getBrand() + " " + car.getModel());
                    }
                }

                System.out.print("\nEnter the Car ID you want to rent: ");
                String carId = scanner.nextLine();

                System.out.print("Enter the number of days for rental: ");
                int rentalDays = scanner.nextInt();
                scanner.nextLine(); // Consume new line

                Customer newCustomer = new Customer("CUS" + (customers.size()+1), customerName);
                customers.add(newCustomer);

                Car selectedCar = null;
                for (Car car: cars) {
                    if (car.getCarId().equals(carId) && car.getAvailableInStock() != 0) {
                        selectedCar = car;
                        break;
                    }
                }

                if (selectedCar != null) {
                    double totalPrice = selectedCar.calculatePrice(rentalDays);

                    System.out.println("\n---- Rental Information ----\n");
                    System.out.println("Customer ID: " + newCustomer.getCustomerId());
                    System.out.println("Customer Name: " +newCustomer.getCustomerName());
                    System.out.println("Car: " + selectedCar.getBrand() + " " + selectedCar.getModel());
                    System.out.println("Rental Days: " + rentalDays);
                    System.out.printf("Total Price: $%.2f\n", totalPrice);

                    System.out.print("\nConfirm rental (Y/N): ");
                    String confirm = scanner.nextLine();

                    if (confirm.equalsIgnoreCase("Y")) {
                        rentCar(selectedCar, newCustomer, rentalDays);
                        System.out.println("\nCar rented successfully.");
                        System.out.println("Rental ID for your rent: " + rentals.getLast().getRentalId());
                    } else {
                        System.out.println("\nRental canceled.");
                    }
                } else {
                    System.out.println("\nInvalid car selection or the car is not available for rent.");
                }

            } else if (choice == 2) {
                System.out.println("\n---- RETURN A CAR ----\n");
                System.out.print("Enter the rental ID of the car you rented: ");
                String rentalId = scanner.nextLine();

                Car carToReturn = null;
                Customer carReturningCustomer = null;
                for (Rental rental: rentals) {
                    if (rental.getRentalId().equals(rentalId)) {
                        carToReturn = rental.getCar();
                        carReturningCustomer = rental.getCustomer();
                        break;
                    }
                }

                if (carToReturn != null && carReturningCustomer != null) {
                    returnCar(rentalId);
                    System.out.println("Car is returned successfully by " + carReturningCustomer.getCustomerName());
                } else {
                    System.out.println("Car was not rented or rental information is missing.");
                }

            } else if (choice == 3) {
                break;
            } else {
                System.out.println("Invalid choices. Please enter a valid option.");
            }
        }

        System.out.println("\nThanks you for using the Car Rental System!");
    }
}


public class Main {
    public static void main(String[] args) {
        CarRentalSystem rentalSystem = new CarRentalSystem();

        Car car1 = new Car("C001", "Toyota", "Camry", 60.0, 5);
        Car car2 = new Car("C002", "Honda", "Accord", 70.0, 4);
        Car car3 = new Car("C003", "Mahindra", "Thar", 130.0, 8);
        rentalSystem.addCar(car1);
        rentalSystem.addCar(car2);
        rentalSystem.addCar(car3);

        rentalSystem.menu();
    }
}
