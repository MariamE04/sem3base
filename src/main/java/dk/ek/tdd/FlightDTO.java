package dk.ek.tdd;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.TimeZone;

/**
 * Purpose: DTO to work with timezones and flights
 * Author: Thomas Hartmann
 */
public class FlightDTO {

    private String origin;
    private String destination;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private TimeZone originTimeZone;
    private TimeZone destinationTimeZone;
    private BigDecimal price;
    private int numberOfSeats;

    // Constructors, getters, setters, and other methods

    public FlightDTO() {
        // Default constructor
    }

    // Constructors
    public FlightDTO(String origin, String destination, LocalDateTime startTime, LocalDateTime endTime, TimeZone originTimeZone, TimeZone destinationTimeZone, BigDecimal price, int numberOfSeats) {
        this.origin = origin;
        this.destination = destination;
        this.startTime = startTime;
        this.endTime = endTime;
        this.originTimeZone = originTimeZone;
        this.destinationTimeZone = destinationTimeZone;
        this.price = price;
        this.numberOfSeats = numberOfSeats;
    }
}
