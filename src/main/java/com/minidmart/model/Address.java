package com.minidmart.model;

public class Address {
    private int addressId;
    private int userId;
    private String label;
    private String line1;
    private String line2;
    private String city;
    private String state;
    private String pincode;
    private boolean isDefault;

    public int getAddressId() { return addressId; }
    public void setAddressId(int addressId) { this.addressId = addressId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getLine1() { return line1; }
    public void setLine1(String line1) { this.line1 = line1; }

    public String getLine2() { return line2; }
    public void setLine2(String line2) { this.line2 = line2; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public boolean isDefaultAddress() { return isDefault; }
    public void setDefaultAddress(boolean aDefault) { isDefault = aDefault; }

    public String getFormatted() {
        return line1 + (line2 != null && !line2.isBlank() ? ", " + line2 : "")
                + ", " + city + ", " + state + " - " + pincode;
    }
}
