package com.elon.entity;

import java.io.InputStream;

public class TicketInfo {

    private Long tempId;

    private Integer gameCode;

    private InputStream imageIps;

    private InputStream orcIps;

    public TicketInfo() {

    }

    public TicketInfo(Long tempId, Integer gameCode, InputStream imageIps, InputStream orcIps) {
        this.tempId = tempId;
        this.gameCode = gameCode;
        this.imageIps = imageIps;
        this.orcIps = orcIps;
    }

    public Long getTempId() {
        return tempId;
    }

    public void setTempId(Long tempId) {
        this.tempId = tempId;
    }

    public Integer getGameCode() {
        return gameCode;
    }

    public void setGameCode(Integer gameCode) {
        this.gameCode = gameCode;
    }

    public InputStream getImageIps() {
        return imageIps;
    }

    public void setImageIps(InputStream imageIps) {
        this.imageIps = imageIps;
    }

    public InputStream getOrcIps() {
        return orcIps;
    }

    public void setOrcIps(InputStream orcIps) {
        this.orcIps = orcIps;
    }

    @Override
    public String toString() {
        return "TicketInfo{" +
                "tempId=" + tempId +
                ", gameCode=" + gameCode +
                ", imageIps=" + imageIps +
                ", orcIps=" + orcIps +
                '}';
    }
}
