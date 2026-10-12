package com.printxpress.app.models;

public class Address {
    private String addressId;
    private String uid;
    private String label;
    private String fullAddress;
    private String city;
    private boolean isDefault;

    public Address() {}

    public Address(String addressId, String uid, String label, String fullAddress, String city, boolean isDefault) {
        this.addressId = addressId;
        this.uid = uid;
        this.label = label;
        this.fullAddress = fullAddress;
        this.city = city;
        this.isDefault = isDefault;
    }

    public String getAddressId() { return addressId; }
    public void setAddressId(String addressId) { this.addressId = addressId; }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getFullAddress() { return fullAddress; }
    public void setFullAddress(String fullAddress) { this.fullAddress = fullAddress; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public boolean getIsDefault() { return isDefault; }
    public void setIsDefault(boolean isDefault) { this.isDefault = isDefault; }

    @Override
    public String toString() {
        return label + " - " + city;
    }
}
