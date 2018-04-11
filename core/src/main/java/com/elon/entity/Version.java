package com.elon.entity;

public enum Version {

    V1("1"), V2("2"), V3("3");

    private String vesion = null;

    Version(String vesion) {
        this.vesion = vesion;
    }

    public String getVesion() {
        return vesion;
    }

    public void setVesion(String vesion) {
        this.vesion = vesion;
    }
}
