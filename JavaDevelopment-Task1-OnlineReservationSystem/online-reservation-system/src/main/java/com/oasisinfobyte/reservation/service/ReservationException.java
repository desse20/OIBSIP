package com.oasisinfobyte.reservation.service;

/**
 * Exception thrown when business validation or reservation operations fail.
 */
public class ReservationException extends RuntimeException {

    public ReservationException(String message) {
        super(message);
    }

    public ReservationException(String message, Throwable cause) {
        super(message, cause);
    }
}

