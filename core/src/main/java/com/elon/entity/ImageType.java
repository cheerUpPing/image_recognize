package com.elon.entity;

public enum ImageType {

    JPG("jpg"),PNG("png"),GIF("gif"),BMP("bmp");

    private String type = null;

    ImageType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
