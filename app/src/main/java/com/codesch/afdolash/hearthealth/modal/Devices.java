package com.codesch.afdolash.hearthealth.modal;

/**
 * Created by Afdolash on 8/10/2017.
 */

public class Devices {
    private String name, address;

    public Devices(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }
}
