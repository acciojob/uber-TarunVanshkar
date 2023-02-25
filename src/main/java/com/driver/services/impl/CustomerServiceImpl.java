package com.driver.services.impl;

import com.driver.model.*;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;

	@Override
	public void register(Customer customer)
	{
		//Save the customer in database
		customerRepository2.save(customer);
	}

	@Override
	public void deleteCustomer(Integer customerId)
	{
		// Delete customer without using deleteById function

		// Since we are deleting the customer
		// Hence all booking of customer also be deleted and if any cab is booked then it should be available

		if(customerRepository2.findById(customerId).get() != null)
		{
			Customer customer = customerRepository2.findById(customerId).get();
			List<TripBooking> tripBookings = customer.getTripBookingList();

			for(TripBooking currTrip : tripBookings)
			{
				//Since driver is parent and cab is child
				//Hence changes in driver(Parent) will automatically make changes in cab(Child)
				Driver driver = currTrip.getDriver();
				Cab cab = driver.getCab();
				cab.setAvailable(true);
				driverRepository2.save(driver);
			}

			//Now we will delete the customer from the repository and due to cascading effect tripBooking will also be deleted
			customerRepository2.delete(customer);
		}
	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception
	{
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE).
		// If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query

		// To find the available driver
		List<Driver> driverList = driverRepository2.findAll(Sort.by(Sort.Direction.ASC, "driverId"));
		Driver driver = null;
		for(Driver currDriver : driverList)
		{
			if(currDriver.getCab().isAvailable() == true)
			{
				driver = currDriver;
				break;
			}
		}

		// Check if no driver available
		if(driver==null)
		{
			throw new Exception("No cab available!");
		}
		// Now this driver should not be available to anyone
		driver.getCab().setAvailable(false);

		// Since driver is available. Hence we can initiate the process of booking the trip
		Customer customer = customerRepository2.findById(customerId).get();

		// TripBooking should be auto generated with every new Booking
		TripBooking tripBooking = new TripBooking();
		tripBooking.setFromLocation(fromLocation);
		tripBooking.setToLocation(toLocation);
		tripBooking.setDistanceInKm(distanceInKm);
		tripBooking.setStatus(TripStatus.CONFIRMED);
		tripBooking.setCustomer(customer);
		tripBooking.setDriver(driver);

		int perKmRate = driver.getCab().getPerKmRate();
		tripBooking.setBill(perKmRate * distanceInKm);

		// Updating trip booking list for driver
		List<TripBooking> tripBookingList = driver.getTripBookingList();
		tripBookingList.add(tripBooking);
		driver.setTripBookingList(tripBookingList);


		// Updating trip booking list for customer
		List<TripBooking> tripBookings = customer.getTripBookingList();
		tripBookings.add(tripBooking);
		customer.setTripBookingList(tripBookings);

		tripBookingRepository2.save(tripBooking);
		driverRepository2.save(driver);
		customerRepository2.save(customer);


		return tripBooking;
	}

	@Override
	public void cancelTrip(Integer tripId)
	{
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking tripBooking = tripBookingRepository2.findById(tripId).get();
		tripBooking.setStatus(TripStatus.CANCELED);
		tripBooking.setBill(0);

		Driver driver = tripBooking.getDriver();
		driver.getCab().setAvailable(true);
		driverRepository2.save(driver);
		tripBookingRepository2.save(tripBooking);
	}

	@Override
	public void completeTrip(Integer tripId)
	{
		//Complete the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking tripBooking = tripBookingRepository2.findById(tripId).get();
		tripBooking.setStatus(TripStatus.COMPLETED);

		// Driver should be available now
		Driver driver = tripBooking.getDriver();
		driver.getCab().setAvailable(true);
		driverRepository2.save(driver);
		tripBookingRepository2.save(tripBooking);
	}
}
