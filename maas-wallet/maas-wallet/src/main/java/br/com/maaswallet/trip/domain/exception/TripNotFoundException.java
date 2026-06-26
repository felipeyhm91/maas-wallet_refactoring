package br.com.maaswallet.trip.domain.exception;

public class TripNotFoundException extends TripException {
    public TripNotFoundException(String message) {
        super(message);
    }
}
