package com.draeger;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a measurement of a medical device. There are different types of measurements e.g. temperature, spo2, heart rate.
 */
public class Measurement {

    private final Instant measurementTime;

    private final Double measurementValue;

    private final MeasurementType type;

    public Measurement(Instant measurementTime, Double measurementValue, MeasurementType type) {
        if (measurementTime == null) {
            throw new IllegalArgumentException("measurementTime is null.");
        }
        if (measurementValue == null) {
            throw new IllegalArgumentException("measurementValue is null.");
        }
        if (type == null) {
            throw new IllegalArgumentException("type is null.");
        }

        this.measurementTime = measurementTime;
        this.measurementValue = measurementValue;
        this.type = type;
    }

    public MeasurementType getType() {
        return type;
    }

    public Double getMeasurementValue() {
        return measurementValue;
    }

    public Instant getMeasurementTime() {
        return measurementTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Measurement that = (Measurement) o;
        return measurementTime.equals(that.measurementTime) && measurementValue.equals(that.measurementValue) && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(measurementTime, measurementValue, type);
    }

    @Override
    public String toString() {
        return "Measurement{" +
                "measurementTime=" + measurementTime +
                ", measurementValue=" + measurementValue +
                ", type=" + type +
                '}';
    }
}
