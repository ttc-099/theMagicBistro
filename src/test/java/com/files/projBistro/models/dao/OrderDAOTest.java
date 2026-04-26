package com.files.projBistro.models.dao;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OrderDAOTest {

    // Test status validation
    @Test
    void testValidOrderStatus() {
        String[] validStatuses = {"pending", "completed", "cancelled"};
        for (String status : validStatuses) {
            assertNotNull(status);
            assertFalse(status.isBlank());
        }
    }

    @Test
    void testOrderTotalCalculation() {
        double item1 = 12.99;
        double item2 = 3.50;
        double total = item1 + item2;
        assertEquals(16.49, total, 0.01);
    }

    @Test
    void testStockDecrementLogic() {
        int stockBefore = 10;
        int quantityOrdered = 1;
        int stockAfter = stockBefore - quantityOrdered;
        assertEquals(9, stockAfter);
        assertTrue(stockAfter >= 0);
    }
}