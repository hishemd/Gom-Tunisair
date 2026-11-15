package com.newhtcdridi.weather

import org.junit.Assert.assertEquals
import org.junit.Test

class ConditionTest {

    @Test
    fun mapsWmoCodesToIcons() {
        assertEquals(Condition.CLEAR, Condition.fromCode(0))
        assertEquals(Condition.CLEAR, Condition.fromCode(1))
        assertEquals(Condition.PARTLY_CLOUDY, Condition.fromCode(2))
        assertEquals(Condition.CLOUDY, Condition.fromCode(3))
        assertEquals(Condition.FOG, Condition.fromCode(45))
        assertEquals(Condition.DRIZZLE, Condition.fromCode(53))
        assertEquals(Condition.RAIN, Condition.fromCode(63))
        assertEquals(Condition.RAIN, Condition.fromCode(81))
        assertEquals(Condition.SNOW, Condition.fromCode(73))
        assertEquals(Condition.SNOW, Condition.fromCode(86))
        assertEquals(Condition.THUNDER, Condition.fromCode(95))
        assertEquals(Condition.THUNDER, Condition.fromCode(99))
    }

    @Test
    fun unknownCodesFallBackToCloudy() {
        assertEquals(Condition.CLOUDY, Condition.fromCode(-1))
        assertEquals(Condition.CLOUDY, Condition.fromCode(1234))
    }
}
