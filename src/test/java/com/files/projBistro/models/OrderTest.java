package com.files.projBistro.models;

import com.files.projBistro.models.models.FoodItem;
import com.files.projBistro.models.models.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    private Order order;
    private FoodItem item1;
    private FoodItem item2;

    @BeforeEach
    void setUp() {
        // Create a fresh order before each test
        order = new Order("Test Customer");

        // Create sample food items using the builder
        item1 = new FoodItem.FoodItemBuilder()
                .setName("Burger")
                .setPrice(12.99)
                .build();

        item2 = new FoodItem.FoodItemBuilder()
                .setName("Fries")
                .setPrice(3.50)
                .build();
    }

    // ---------- calculateTotal() tests ----------
    @Test
    void testCalculateTotal_EmptyCart_ReturnsZero() {
        assertEquals(0.0, order.getTotalPrice(), 0.01);
    }

    @Test
    void testCalculateTotal_SingleItem() {
        order.addItem(item1);
        assertEquals(12.99, order.getTotalPrice(), 0.01);
    }

    @Test
    void testCalculateTotal_MultipleItems() {
        order.addItem(item1);
        order.addItem(item2);
        assertEquals(16.49, order.getTotalPrice(), 0.01);
    }

    @Test
    void testCalculateTotal_AfterRemoveItem() {
        order.addItem(item1);
        order.addItem(item2);
        order.removeItem(item1);
        assertEquals(3.50, order.getTotalPrice(), 0.01);
    }

    // ---------- addItem() tests ----------
    @Test
    void testAddItem_IncreasesItemCount() {
        assertEquals(0, order.getItems().size());
        order.addItem(item1);
        assertEquals(1, order.getItems().size());
        order.addItem(item2);
        assertEquals(2, order.getItems().size());
    }

    @Test
    void testAddItem_UpdatesTotal() {
        order.addItem(item1);
        assertEquals(12.99, order.getTotalPrice(), 0.01);
        order.addItem(item2);
        assertEquals(16.49, order.getTotalPrice(), 0.01);
    }

    // ---------- removeItem() tests ----------
    @Test
    void testRemoveItem_DecreasesItemCount() {
        order.addItem(item1);
        order.addItem(item2);
        order.removeItem(item1);
        assertEquals(1, order.getItems().size());
        assertEquals(item2, order.getItems().get(0));
    }

    @Test
    void testRemoveItem_UpdatesTotal() {
        order.addItem(item1);
        order.addItem(item2);
        order.removeItem(item1);
        assertEquals(3.50, order.getTotalPrice(), 0.01);
    }

    @Test
    void testRemoveItem_ItemNotInCart_DoesNothing() {
        order.addItem(item1);
        order.removeItem(item2); // item2 not in cart
        assertEquals(1, order.getItems().size());
        assertEquals(12.99, order.getTotalPrice(), 0.01);
    }

    // ---------- constructor tests ----------
    @Test
    void testConstructor_WithOrderId() {
        Order orderWithId = new Order(100, "John Doe");
        assertEquals(100, orderWithId.getOrderId());
        assertEquals("John Doe", orderWithId.getCustomerName());
        assertEquals("", orderWithId.getCustomerPhone());
        assertEquals(0, orderWithId.getItems().size());
        assertEquals(0.0, orderWithId.getTotalPrice(), 0.01);
        assertEquals("pending", orderWithId.getStatus());
    }

    @Test
    void testConstructor_WithoutOrderId() {
        assertEquals("Test Customer", order.getCustomerName());
        assertEquals("", order.getCustomerPhone());
        assertEquals(0, order.getItems().size());
        assertEquals(0.0, order.getTotalPrice(), 0.01);
        assertEquals("pending", order.getStatus());
    }

    // ---------- setter / getter tests ----------
    @Test
    void testSetCustomerPhone() {
        order.setCustomerPhone("0123456789");
        assertEquals("0123456789", order.getCustomerPhone());
    }

    @Test
    void testSetOrderId() {
        order.setOrderId(999);
        assertEquals(999, order.getOrderId());
    }

    @Test
    void testSetTotalPrice_Manually() {
        order.setTotalPrice(50.00);
        assertEquals(50.00, order.getTotalPrice(), 0.01);
        // Adding an item should recalc and overwrite manual set
        order.addItem(item1);
        assertEquals(12.99, order.getTotalPrice(), 0.01);
    }

    @Test
    void testSetStatus() {
        order.setStatus("completed");
        assertEquals("completed", order.getStatus());
    }
}