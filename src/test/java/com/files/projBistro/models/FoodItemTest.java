package com.files.projBistro.models;

import com.files.projBistro.models.models.FoodItem;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FoodItemTest {

    @Test
    void testFoodItemBuilder_AllFieldsSet() {
        FoodItem item = new FoodItem.FoodItemBuilder()
                .setId(10)
                .setName("Pizza")
                .setPrice(15.99)
                .setStock(25)
                .setCharCategory("Chloe")
                .setItemType("Main")
                .setDescription("Cheesy pizza")
                .setImagePath("/images/pizza.png")
                .setIsActive(true)
                .build();

        assertEquals(10, item.getId());
        assertEquals("Pizza", item.getName());
        assertEquals(15.99, item.getPrice(), 0.01);
        assertEquals(25, item.getStock());
        assertEquals("Chloe", item.getCharCategory());
        assertEquals("Main", item.getItemType());
        assertEquals("Cheesy pizza", item.getDescription());
        assertEquals("/images/pizza.png", item.getImagePath());
        assertTrue(item.isActive());
    }

    @Test
    void testFoodItemBuilder_DefaultValues() {
        FoodItem item = new FoodItem.FoodItemBuilder()
                .setName("Burger")
                .setPrice(9.99)
                .build();

        assertEquals(0, item.getId());      // default int
        assertEquals(0, item.getStock());   // default stock = 0
        assertEquals("Food", item.getItemType()); // default item type
        assertTrue(item.isActive());         // default active = true
        assertNull(item.getCharCategory());  // not set
        assertNull(item.getDescription());
        assertNull(item.getImagePath());
    }

    @Test
    void testToString_ReturnsFormattedString() {
        FoodItem item = new FoodItem.FoodItemBuilder()
                .setName("Fries")
                .setPrice(2.50)
                .build();
        assertEquals("Fries - RM2.50", item.toString());
    }

    @Test
    void testSetActive() {
        FoodItem item = new FoodItem.FoodItemBuilder().setName("Cake").setPrice(5.00).build();
        assertTrue(item.isActive());
        item.setActive(false);
        assertFalse(item.isActive());
        item.setActive(true);
        assertTrue(item.isActive());
    }
}