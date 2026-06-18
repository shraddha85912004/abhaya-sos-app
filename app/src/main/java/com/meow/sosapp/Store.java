package com.meow.sosapp;

// This class represents a nearby place (like a police station)
public class Store {
    private String name; // Name of the place
    private String address; // Address or vicinity of the place
    // private String phoneNumber; // Phone number (commented out as it's not always available in Nearby Search)
    private double distance; // Distance from the user's current location in meters

    /**
     * Constructor for the Store object.
     *
     * @param name The name of the place.
     * @param address The address or vicinity of the place.
     * @param distance The distance to the place in meters.
     */
    public Store(String name, String address, double distance) {
        this.name = name;
        this.address = address;
        // this.phoneNumber = phoneNumber; // Initialize if phoneNumber is added back
        this.distance = distance;
    }

    /**
     * Gets the name of the place.
     * @return The name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the address of the place.
     * @return The address.
     */
    public String getAddress() {
        return address;
    }

    // /**
    //  * Gets the phone number of the place.
    //  * @return The phone number.
    //  */
    // public String getPhoneNumber() {
    //     return phoneNumber;
    // }

    /**
     * Gets the distance to the place in meters.
     * @return The distance in meters.
     */
    public double getDistance() {
        return distance;
    }

    // You can add setters if needed, but for immutable data like this, getters are often sufficient.
    // public void setName(String name) {
    //     this.name = name;
    // }
    //
    // public void setAddress(String address) {
    //     this.address = address;
    // }
    //
    // public void setDistance(double distance) {
    //     this.distance = distance;
    // }
}
