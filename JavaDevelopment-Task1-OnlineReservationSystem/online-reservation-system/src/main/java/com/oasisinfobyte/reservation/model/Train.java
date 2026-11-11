package com.oasisinfobyte.reservation.model;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * Model representing a Train and its route.
 */
public class Train {

    private Long id;
    private String trainNumber;
    private String trainName;
    private String sourceStation;
    private String destinationStation;
    private Timestamp createdAt;

    public Train() {
    }

    public Train(String trainNumber, String trainName, String sourceStation, String destinationStation) {
        this.trainNumber = trainNumber;
        this.trainName = trainName;
        this.sourceStation = sourceStation;
        this.destinationStation = destinationStation;
    }

    public Train(Long id, String trainNumber, String trainName, String sourceStation, String destinationStation, Timestamp createdAt) {
        this.id = id;
        this.trainNumber = trainNumber;
        this.trainName = trainName;
        this.sourceStation = sourceStation;
        this.destinationStation = destinationStation;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Train train = (Train) o;
        return Objects.equals(id, train.id) && Objects.equals(trainNumber, train.trainNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, trainNumber);
    }

    @Override
    public String toString() {
        return trainNumber + " - " + trainName + " (" + sourceStation + " -> " + destinationStation + ")";
    }
}

