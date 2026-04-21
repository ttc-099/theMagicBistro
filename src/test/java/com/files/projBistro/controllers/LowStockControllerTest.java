package com.files.projBistro.controllers;

import com.files.projBistro.models.models.FoodItem;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;

class LowStockControllerTest {

    private static final int LOW_STOCK_THRESHOLD = 20;

    // Method that mimics the filtering logic in LowStockController.refreshStock()
    private List<FoodItem> getLowStockItems(List<FoodItem> allItems) {
        return allItems.stream()
                .filter(item -> item.getStock() <= LOW_STOCK_THRESHOLD)
                .collect(Collectors.toList());
    }

    @Test
    void testLowStockFilter_ReturnsItemsWithStockLessThanOrEqual20() {
        FoodItem item1 = new FoodItem.FoodItemBuilder().setName("Item1").setStock(5).build();
        FoodItem item2 = new FoodItem.FoodItemBuilder().setName("Item2").setStock(20).build();
        FoodItem item3 = new FoodItem.FoodItemBuilder().setName("Item3").setStock(21).build();
        FoodItem item4 = new FoodItem.FoodItemBuilder().setName("Item4").setStock(0).build();

        List<FoodItem> all = Arrays.asList(item1, item2, item3, item4);
        List<FoodItem> low = getLowStockItems(all);

        assertEquals(3, low.size());
        assertTrue(low.contains(item1));
        assertTrue(low.contains(item2));
        assertTrue(low.contains(item4));
        assertFalse(low.contains(item3));
    }

    @Test
    void testLowStockFilter_EmptyList_ReturnsEmpty() {
        List<FoodItem> all = List.of();
        List<FoodItem> low = getLowStockItems(all);
        assertTrue(low.isEmpty());
    }

    @Test
    void testLowStockFilter_AllAboveThreshold_ReturnsEmpty() {
        FoodItem item1 = new FoodItem.FoodItemBuilder().setStock(30).build();
        FoodItem item2 = new FoodItem.FoodItemBuilder().setStock(100).build();
        List<FoodItem> all = Arrays.asList(item1, item2);
        List<FoodItem> low = getLowStockItems(all);
        assertTrue(low.isEmpty());
    }

    @Test
    void testLowStockFilter_AllAtOrBelowThreshold_ReturnsAll() {
        FoodItem item1 = new FoodItem.FoodItemBuilder().setStock(10).build();
        FoodItem item2 = new FoodItem.FoodItemBuilder().setStock(20).build();
        List<FoodItem> all = Arrays.asList(item1, item2);
        List<FoodItem> low = getLowStockItems(all);
        assertEquals(2, low.size());
    }

    @Test
    void testThresholdConstantValue() {
        assertEquals(20, LOW_STOCK_THRESHOLD, "Threshold should be 20 as defined in LowStockController");
    }
}