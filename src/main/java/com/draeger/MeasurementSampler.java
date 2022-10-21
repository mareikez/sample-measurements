package com.draeger;

import java.time.Instant;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Samples measurements from a medical device to a grid. By default the interval for the grid is 5 minutes.
 * This may be changed by calling the constructor with another value.
 *
 * <p>There are different types of measurements e.g. temperature, spo2, heart rate. Every type is sampled separately.
 * From each interval the last value of a measurement type is sampled to the grid. A value on the grid belongs
 * to the grid represented by the measurement time.
 *
 * <p>To sample values call {@link MeasurementSampler#sample(Instant, List) MeasurementSampler.sample}.
 */
public class MeasurementSampler {

    private final GridCreator gridCreator;

    /**
     * Creates an instance of {@code MeasurementSampler} with a default interval of 5 minutes.
     */
    public MeasurementSampler() {
        this(5);
    }

    /**
     * Creates an instance of {@code MeasurementSampler} with the given interval in minutes.
     *
     * @param interval interval for grid in minutes. Must be greater than 0.
     */
    public MeasurementSampler(int interval) {
        if (interval < 1) {
            throw new IllegalArgumentException("interval must be greater than 0.");
        }
        this.gridCreator = new GridCreator(interval);
    }

    /**
     * Samples measurements from a medical device to a grid by measurement type. In the case there is no measurement
     * in an interval, the result doesn't contain a list entry for the corresponding grid time.
     *
     * @param startOfSampling       the time the measurement starts. The grid is calculated from this value.
     *                              All measurements before or on startOfSampling are ignored. May not be null.
     * @param unsampledMeasurements unordered list of measurements for all measurement types, that is supposed to
     *                              be sampled. May be empty, but not null.
     * @return unmodifiable map that contains an unmodifiable list of all sampled measurements ordered by grid time per measurement type
     */
    public Map<MeasurementType, List<Measurement>> sample(Instant startOfSampling, List<Measurement> unsampledMeasurements) {
        if (startOfSampling == null) {
            throw new IllegalArgumentException("startOfSampling is null.");
        }
        if (unsampledMeasurements == null) {
            throw new IllegalArgumentException("unsampledMeasurements is null.");
        }

        Map<MeasurementType, List<Measurement>> resultMap = new HashMap<>();

        List<Instant> grid = this.gridCreator.createGrid(startOfSampling, unsampledMeasurements.stream()
                .map(Measurement::getMeasurementTime)
                .toList());

        List<Measurement> validUnsampledMeasurements = this.getMeasurementsInGrid(grid, unsampledMeasurements, startOfSampling);

        //Create a list of measurements per measurement type
        Map<MeasurementType, List<Measurement>> measurementsPerMeasurementsType = validUnsampledMeasurements.stream()
                .collect(Collectors.groupingBy(Measurement::getType));

        //Create one ordered list with sampled measurements for every measurement type
        measurementsPerMeasurementsType.forEach((measurementType, measurements) -> {
            List<Measurement> sampledMeasurements = createSampledMeasurements(grid, measurements);
            if (!sampledMeasurements.isEmpty()) {
                resultMap.put(measurementType, Collections.unmodifiableList(sampledMeasurements));
            }
        });

        return Collections.unmodifiableMap(resultMap);
    }

    /**
     * Sample measurements to grid. If there is no measurement in an interval, no list entry is created.
     *
     * @param grid                       instants of the grid, to which the measurements are sampled
     * @param validUnsampledMeasurements unordered list of measurements for one measurement type
     * @return sampled measurements ordered by grid time
     */
    private List<Measurement> createSampledMeasurements(List<Instant> grid, List<Measurement> validUnsampledMeasurements) {

        //Find the last measurement in every interval
        Map<Instant, List<Measurement>> measurementsPerInterval = validUnsampledMeasurements.stream()
                .collect(Collectors.groupingBy(m -> getGridTime(m, grid)));

        List resultList = new ArrayList(measurementsPerInterval.size());
        measurementsPerInterval.forEach((gridTime, measurements) -> {
            if (measurements.size() > 1) {
                measurements.sort(Comparator.comparing(Measurement::getMeasurementTime));
                Measurement secondLastMeasurement = measurements.get(measurements.size() - 2);
                resultList.add(new Measurement(gridTime, secondLastMeasurement.getMeasurementValue(), secondLastMeasurement.getType()));
            }
        });

        return resultList;
    }

    /**
     * Gets the corresponding grid time for the measurement. The corresponding grid time is the first entry in the grid,
     * that is after or at the same time of the measurement time. It must exist, because the grid is created based on
     * all measurement times.
     *
     * @param measurement measurement
     * @param grid        instants of the grid, to which the measurements are sampled
     * @return corresponding grid time for the measurement
     */
    private Instant getGridTime(Measurement measurement, List<Instant> grid) {
        return grid.stream()
                .filter(instant -> !measurement.getMeasurementTime().isAfter(instant))
                .findFirst()
                .get();
    }

    /**
     * Valid measurements are measured during the time covered by the grid.
     *
     * @param grid            instants of the grid, to which the measurements are sampled
     * @param measurements    unordered list of measurements for all measurement types
     * @param startOfSampling the time the measurement starts
     * @return list of valid measurements
     */
    private List<Measurement> getMeasurementsInGrid(List<Instant> grid, List<Measurement> measurements, Instant startOfSampling) {
        if (grid == null) {
            throw new IllegalArgumentException("grid is null.");
        }
        if (measurements == null) {
            throw new IllegalArgumentException("grid is null.");
        }
        if (grid.isEmpty() || measurements.isEmpty()) {
            return Collections.emptyList();
        }

        Instant lastGridTime = grid.get(grid.size() - 1);
        return measurements.stream()
                .filter(m -> m.getMeasurementTime().isAfter(startOfSampling))
                .filter(m -> !m.getMeasurementTime().isAfter(lastGridTime))
                .toList();
    }
}
