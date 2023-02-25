package com.driver.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "driver")
public class Driver
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int driverId;

    private String mobile;
    private String password;


    //Connect Cab with Driver
    // Driver(Parent) to Cab(Child)  ---> One : One
    @OneToOne(mappedBy = "driver", cascade = CascadeType.ALL)
    private Cab cab;

    public Cab getCab() {
        return cab;
    }

    public void setCab(Cab cab) {
        this.cab = cab;
    }


    //Connect Driver with TripBooking
    // Driver(Parent) to TripBooking(Child)  ---> One : Many
    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL)
    private List<TripBooking> tripBookingList = new ArrayList<>();

    public List<TripBooking> getTripBookingList() {
        return tripBookingList;
    }

    public void setTripBookingList(List<TripBooking> tripBookingList) {
        this.tripBookingList = tripBookingList;
    }

    public Driver() {
    }

    public int getDriverId() {
        return driverId;
    }

    public void setDriverId(int driverId) {
        this.driverId = driverId;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}