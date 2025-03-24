package com.supersection;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class CarRentalSystemTest {

    private CarRentalSystem rentalSystem;
    private Car mockCar;
    private Customer mockCustomer;

    @BeforeEach
    void setUp() {
        rentalSystem = new CarRentalSystem();
        mockCar = mock(Car.class);
        mockCustomer = mock(Customer.class);

        // Mock behavior
        when(mockCar.getCarId()).thenReturn("C001");
        when(mockCar.getBrand()).thenReturn("Toyota");
        when(mockCar.getModel()).thenReturn("Camry");
        when(mockCar.getAvailableInStock()).thenReturn(1);
    }

    @Test
    void testAddCar() {
        rentalSystem.addCar(mockCar);
        List<Car> cars = rentalSystem.getCars();

        assertEquals(1, cars.size());
        assertEquals("C001", cars.get(0).getCarId());
    }

    @Test
    void testAddCustomer() {
        rentalSystem.addCustomer(mockCustomer);
        List<Customer> customers = rentalSystem.getCustomers();

        assertEquals(1, customers.size());
    }

    @Test
    void testRentCar_Success() {
        rentalSystem.addCar(mockCar);
        rentalSystem.addCustomer(mockCustomer);

        rentalSystem.rentCar(mockCar, mockCustomer, 3);

        verify(mockCar, times(1)).rent();  // Ensure rent() was called once
    }

    @Test
    void testRentCar_Failure_CarNotAvailable() {
        when(mockCar.getAvailableInStock()).thenReturn(0);
        rentalSystem.addCar(mockCar);
        rentalSystem.addCustomer(mockCustomer);

        rentalSystem.rentCar(mockCar, mockCustomer, 3);

        verify(mockCar, never()).rent(); // Should never call rent() if stock is 0
    }

    @Test
    void testReturnCar_Success() {
        rentalSystem.addCar(mockCar);
        rentalSystem.addCustomer(mockCustomer);
        rentalSystem.rentCar(mockCar, mockCustomer, 2);

        Rental rental = rentalSystem.getRentals().get(0);
        rentalSystem.returnCar(rental.getRentalId());

        assertTrue(rentalSystem.getRentals().isEmpty()); // Rental should be removed
    }

    @Test
    void testReturnCar_Failure_InvalidRentalId() {
        rentalSystem.returnCar("INVALID_ID");

        assertTrue(rentalSystem.getRentals().isEmpty()); // Rentals should remain empty
    }
}
