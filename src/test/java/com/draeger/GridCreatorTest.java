package com.draeger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GridCreatorTest {

    private GridCreator underTest;

    @BeforeEach
    void init() {
        underTest = new GridCreator(5);
    }

    @Test
    void createGrid() {
        Instant startTime = Instant.parse("2017-01-03T10:00:00.00Z");
        Instant betweenTime = Instant.parse("2017-01-03T10:05:00.00Z");
        Instant lastTime = Instant.parse("2017-01-03T10:09:07.00Z");

        List<Instant> intervals = underTest.createGrid(startTime, Arrays.asList(startTime, betweenTime, lastTime));
        assertThat(intervals).hasSize(2).containsExactly(Instant.parse("2017-01-03T10:05:00.00Z"), Instant.parse("2017-01-03T10:10:00.00Z"));
    }

    @Test
    void createGridStartTimeEqualsLastTime() {
        Instant startTime = Instant.parse("2017-01-03T10:00:00.00Z");
        Instant lastTime = Instant.parse("2017-01-03T10:00:00.00Z");

        List<Instant> intervals = underTest.createGrid(startTime, Collections.singletonList(lastTime));
        assertThat(intervals).isEmpty();
    }

    @Test
    void createGridStartTimeAfterLastTime() {
        Instant startTime = Instant.parse("2017-01-03T10:05:00.00Z");
        Instant lastTime = Instant.parse("2017-01-03T10:00:00.00Z");

        List<Instant> intervals = underTest.createGrid(startTime, Collections.singletonList((lastTime)));
        assertThat(intervals).isEmpty();
    }
}
