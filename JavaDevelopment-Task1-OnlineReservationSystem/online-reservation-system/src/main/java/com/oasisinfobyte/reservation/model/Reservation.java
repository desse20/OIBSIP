package com.oasisinfobyte.reservation.model;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Model representing a passenger ticket Reservation.
 */
public class Reservation {

    private Long id;
    private String pnr;
    private String passengerName;
    private Long trainId;
    private String classType;
    private LocalDate journeyDate;
    private String sourceStation;
    private String destinationStation;
    private Long userId;
    private Timestamp createdAt;

    // Associated entity details (populated for UI and reporting)
    private String trainNumber;
    private String trainName;
    private String username;

    public Reservation() {
    }

    public Reservation(String pnr, String passengerName, Long trainId, String classType,
                       LocalDate journeyDate, String sourceStation, String destinationStation, Long userId) {
        this.pnr = pnr;
        this.passengerName = passengerName;
        this.trainId = trainId;
        this.classType = classType;
        this.journeyDate = journeyDate;
        this.sourceStation = sourceStation;
        this.destinationStation = destinationStation;
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPnr() {
        return pnr;
    }

    public void setPnr(String pnr) {
        this.pnr = pnr;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public Long getTrainId() {
        return trainId;
    }

    public void setTrainId(Long trainId) {
        this.trainId = trainId;
    }

    public String getClassType() {
        return classType;
    }

    public void setClassType(String classType) {
        this.classType = classType;
    }

    public LocalDate getJourneyDate() {
        return journeyDate;
    }

    public void setJourneyDate(LocalDate journeyDate) {
        this.journeyDate = journeyDate;
    }

    public String getSourceStation() {
        return sourceStation;
    }

    public void setSourceStation(String sourceStation) {
        this.sourceStation = sourceStation;
    }

    public String getDestinationStation() {
        return destinationStation;
    }

    public void setDestinationStation(String destinationStation) {
        this.destinationStation = destinationStation;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(String trainNumber) {
        this.trainNumber = trainNumber;
    }

    public String getTrainName() {
        return trainName;
    }

    public void setTrainName(String trainName) {
        this.trainName = trainName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return Objects.equals(id, that.id) && Objects.equals(pnr, that.pnr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, pnr);
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "pnr='" + pnr + '\'' +
                ", passengerName='" + passengerName + '\'' +
                ", trainNumber='" + trainNumber + '\'' +
                ", trainName='" + trainName + '\'' +
                ", classType='" + classType + '\'' +
                ", journeyDate=" + journeyDate +
                ", sourceStation='" + sourceStation + '\'' +
                ", destinationStation='" + destinationStation + '\'' +
                '}';
    }
}

