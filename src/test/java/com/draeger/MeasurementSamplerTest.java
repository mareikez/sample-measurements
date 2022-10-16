package com.draeger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MeasurementSamplerTest {

    private MeasurementSampler underTest;

    @BeforeEach
    void init() {
        underTest = new MeasurementSampler();
    }

    @Test
    void sampleExample() {
        Measurement measurement1 = new Measurement(Instant.parse("2017-01-03T10:04:45.00Z"), 35.79, MeasurementType.TEMP);
        Measurement measurement2 = new Measurement(Instant.parse("2017-01-03T10:01:18.00Z"), 98.78, MeasurementType.SPO2);
        Measurement measurement3 = new Measurement(Instant.parse("2017-01-03T10:09:07.00Z"), 35.01, MeasurementType.TEMP);
        Measurement measurement4 = new Measurement(Instant.parse("2017-01-03T10:03:34.00Z"), 96.49, MeasurementType.SPO2);
        Measurement measurement5 = new Measurement(Instant.parse("2017-01-03T10:02:01.00Z"), 35.82, MeasurementType.TEMP);
        Measurement measurement6 = new Measurement(Instant.parse("2017-01-03T10:05:00.00Z"), 97.17, MeasurementType.SPO2);
        Measurement measurement7 = new Measurement(Instant.parse("2017-01-03T10:05:01.00Z"), 95.08, MeasurementType.SPO2);

        List<Measurement> input = Arrays.asList(measurement1, measurement2, measurement3, measurement4, measurement5, measurement6, measurement7);

        Measurement sample1 = new Measurement(Instant.parse("2017-01-03T10:05:00.00Z"), 97.17, MeasurementType.SPO2);
        Measurement sample2 = new Measurement(Instant.parse("2017-01-03T10:10:00.00Z"), 95.08, MeasurementType.SPO2);
        Measurement sample3 = new Measurement(Instant.parse("2017-01-03T10:05:00.00Z"), 35.79, MeasurementType.TEMP);
        Measurement sample4 = new Measurement(Instant.parse("2017-01-03T10:10:00.00Z"), 35.01, MeasurementType.TEMP);

        Map<MeasurementType, List<Measurement>> result = underTest.sample(Instant.parse("2017-01-03T10:00:00.00Z"), input);
        assertThat(result).containsOnlyKeys(MeasurementType.SPO2, MeasurementType.TEMP);

        List<Measurement> spo2Measurements = result.get(MeasurementType.SPO2);
        assertThat(spo2Measurements).containsExactly(sample1, sample2);

        List<Measurement> tempMeasurements = result.get(MeasurementType.TEMP);
        assertThat(tempMeasurements).containsExactly(sample3, sample4);

    }

    @Test
    void sampleEmptyList() {
        Map<MeasurementType, List<Measurement>> result = underTest.sample(Instant.parse("2017-01-03T10:00:00.00Z"), Collections.emptyList());
        assertThat(result).isEmpty();
    }

    @Test
    void sampleOnLimit() {
        Measurement measurement1 = new Measurement(Instant.parse("2017-01-03T10:05:00.00Z"), 35.79, MeasurementType.TEMP);
        List<Measurement> input = Collections.singletonList(measurement1);
        Map<MeasurementType, List<Measurement>> result = underTest.sample(Instant.parse("2017-01-03T10:00:00.00Z"), input);

        assertThat(result).containsOnlyKeys(MeasurementType.TEMP);

        List<Measurement> tempMeasurements = result.get(MeasurementType.TEMP);
        assertThat(tempMeasurements).containsExactly(new Measurement(Instant.parse("2017-01-03T10:05:00.00Z"), 35.79, MeasurementType.TEMP));

    }

    @Test
    void sampleOnStartOfSampling() {
        Measurement measurement1 = new Measurement(Instant.parse("2017-01-03T10:05:00.00Z"), 35.79, MeasurementType.TEMP);
        List<Measurement> input = Collections.singletonList(measurement1);

        Map<MeasurementType, List<Measurement>> result = underTest.sample(Instant.parse("2017-01-03T10:05:00.00Z"), input);
        assertThat(result).isEmpty();
    }

    @Test
    void sampleIntervalWithoutValue() {
        Measurement measurement1 = new Measurement(Instant.parse("2017-01-03T10:02:01.00Z"), 35.82, MeasurementType.HEART_RATE);
        Measurement measurement2 = new Measurement(Instant.parse("2017-01-03T10:19:08.00Z"), 35.01, MeasurementType.HEART_RATE);
        Measurement measurement3 = new Measurement(Instant.parse("2017-01-03T10:19:06.00Z"), 35.02, MeasurementType.HEART_RATE);

        List<Measurement> input = Arrays.asList(measurement2, measurement1, measurement3);
        Map<MeasurementType, List<Measurement>> result = underTest.sample(Instant.parse("2017-01-03T10:00:00.00Z"), input);

        assertThat(result).containsOnlyKeys(MeasurementType.HEART_RATE);

        List<Measurement> heartRateMeasurements = result.get(MeasurementType.HEART_RATE);
        assertThat(heartRateMeasurements).containsExactly(new Measurement(Instant.parse("2017-01-03T10:05:00.00Z"), 35.82, MeasurementType.HEART_RATE),
                new Measurement(Instant.parse("2017-01-03T10:20:00.00Z"), 35.01, MeasurementType.HEART_RATE));
    }

    @Test
    void sampleIgnoreMeasurementBeforeStart() {
        Measurement measurement1 = new Measurement(Instant.parse("2017-01-03T09:02:01.00Z"), 35.82, MeasurementType.TEMP);
        Measurement measurement2 = new Measurement(Instant.parse("2017-01-03T10:19:08.00Z"), 35.01, MeasurementType.SPO2);
        Measurement measurement3 = new Measurement(Instant.parse("2017-01-03T09:18:07.00Z"), 35.73, MeasurementType.SPO2);

        List<Measurement> input = Arrays.asList(measurement2, measurement1, measurement3);
        Map<MeasurementType, List<Measurement>> result = underTest.sample(Instant.parse("2017-01-03T10:00:00.00Z"), input);

        assertThat(result).containsOnlyKeys(MeasurementType.SPO2);

        List<Measurement> spo2Measurements = result.get(MeasurementType.SPO2);
        assertThat(spo2Measurements).containsExactly(new Measurement(Instant.parse("2017-01-03T10:20:00.00Z"), 35.01, MeasurementType.SPO2));
    }
}
