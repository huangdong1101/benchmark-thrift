package com.mamba.benchmark.common.pressure;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PressureTests {

    @Test
    void test_parse_4_Fixed() {
        Pressure pressure = Pressure.parse(" 10 ", 60);
        Assertions.assertTrue(pressure instanceof Fixed);
        Assertions.assertEquals(pressure.currentQuantity(), 10);
        Assertions.assertEquals(pressure.getQuantity(0), 10);
        Assertions.assertEquals(pressure.getQuantity(30), 10);
        Assertions.assertEquals(pressure.getQuantity(59), 10);
        Assertions.assertEquals(pressure.getQuantity(60), -1);
    }

    @Test
    void test_parse_4_Gradient() {
        Pressure pressure = Pressure.parse(" 10 + 5 * 2", 20);
        Assertions.assertTrue(pressure instanceof Gradient);
        Assertions.assertEquals(pressure.currentQuantity(), 10);
        Assertions.assertEquals(pressure.getQuantity(0), 10);
        Assertions.assertEquals(pressure.getQuantity(19), 10);
        Assertions.assertEquals(pressure.getQuantity(20), 15);
        Assertions.assertEquals(pressure.getQuantity(39), 15);
        Assertions.assertEquals(pressure.getQuantity(40), 20);
        Assertions.assertEquals(pressure.getQuantity(59), 20);
        Assertions.assertEquals(pressure.getQuantity(60), -1);
    }

    @Test
    void test_parse_4_Custom() {
        Pressure pressure = Pressure.parse(" 10 , 20, 30", 20);
        Assertions.assertTrue(pressure instanceof Custom);
        Assertions.assertEquals(pressure.currentQuantity(), 10);
        Assertions.assertEquals(pressure.getQuantity(0), 10);
        Assertions.assertEquals(pressure.getQuantity(19), 10);
        Assertions.assertEquals(pressure.getQuantity(20), 20);
        Assertions.assertEquals(pressure.getQuantity(39), 20);
        Assertions.assertEquals(pressure.getQuantity(40), 30);
        Assertions.assertEquals(pressure.getQuantity(59), 30);
        Assertions.assertEquals(pressure.getQuantity(60), -1);
    }

}
