package dk.ek.tdd;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.TimeZone;

/**
 * Purpose: Entity to work with timezones and flights
 * Author: Thomas Hartmann
 */
@Entity
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String origin;
    private String destination;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private TimeZone originTimeZone;
    private TimeZone destinationTimeZone;
    private BigDecimal price;
    private int numberOfSeats;

    // Constructors, getters, setters, and other methods

    public Flight() {
        // Default constructor
    }

    public Flight(String origin, String destination, LocalDateTime startTime, LocalDateTime endTime,
                         TimeZone originTimeZone, TimeZone destinationTimeZone, BigDecimal price, int numberOfSeats) {
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

