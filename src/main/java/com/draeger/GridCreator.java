package com.draeger;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Creates grid with given interval.
 */
public class GridCreator {

    private final int interval;

    /**
     * @param interval grid interval in minutes. Must be greater than 0.
     */
    public GridCreator(int interval) {
        if (interval < 1) {
            throw new IllegalArgumentException("interval must be greater than 0");
        }
        this.interval = interval;
    }

    /**
     * The first grid time is the startTime + interval. The subsequent grid times are generated
     * in the given interval. The last value of the grid is the first grid value, that is higher than lastTime.
     *
     * @param startOfSampling  the first grid time
     * @param measurementTimes unordered list of measurement times for all measurement types, that is supposed to
     *                         be sampled. May be empty, but not null.
     * @return grid as unmodifiable list
     */
    public List<Instant> createGrid(Instant startOfSampling, List<Instant> measurementTimes) {
        if (startOfSampling == null) {
            throw new IllegalArgumentException("startOfSampling is null.");
        }
        if (measurementTimes == null) {
            throw new IllegalArgumentException("unsampledMeasurements is null.");
        }

        Instant lastMeasurementTime = getLastMeasurementTime(measurementTimes);

        //Create a list with all instants, to which the measurements are sampled
        List<Instant> intervals = new ArrayList<>();
        if (lastMeasurementTime != null && lastMeasurementTime.isAfter(startOfSampling)) {
            for (Instant i = startOfSampling; i.isBefore(lastMeasurementTime); i = i.plus(interval, ChronoUnit.MINUTES)) {
                intervals.add(i.plus(interval, ChronoUnit.MINUTES));
            }
        }
        return Collections.unmodifiableList(intervals);
    }


    /**
     * Get the measurement time of the last measured measurement.
     *
     * @param measurementTimes unordered list of measurement times for all measurement types
     * @return last measurement time or null, if measurements are empty.
     */
    private Instant getLastMeasurementTime(List<Instant> measurementTimes) {
        if (measurementTimes == null || measurementTimes.isEmpty()) {
            return null;
        }
        return measurementTimes.stream()
                .max(Comparator.naturalOrder())
                .get();
    }
}
