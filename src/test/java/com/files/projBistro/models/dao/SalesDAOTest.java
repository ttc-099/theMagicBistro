package com.files.projBistro.models.dao;

import com.files.projBistro.models.models.Order;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// using in-memory database so tests don't mess up the real bistroTrue.db
class SalesDAOTest {

    private SalesDAO salesDAO;
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        // Use in-memory database for testing
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        salesDAO = new TestSalesDAO(connection);
        createTestSchema();
        insertTestData();
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    // ------------------------- Test Schema Setup -------------------------
    private void createTestSchema() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // users table (simplified)
            stmt.execute("CREATE TABLE users (user_id INTEGER PRIMARY KEY, username TEXT)");
            // characters table
            stmt.execute("CREATE TABLE characters (character_id INTEGER PRIMARY KEY, name TEXT)");
            // food_items table
            stmt.execute("CREATE TABLE food_items (" +
                    "item_id INTEGER PRIMARY KEY, name TEXT, price REAL, stock INTEGER, " +
                    "item_type TEXT, character_id INTEGER, is_active INTEGER)");
            // orders table
            stmt.execute("CREATE TABLE orders (" +
                    "order_id INTEGER PRIMARY KEY, user_id INTEGER, total_price REAL, " +
                    "status TEXT, order_date TIMESTAMP)");
            // order_items table
            stmt.execute("CREATE TABLE order_items (" +
                    "order_item_id INTEGER PRIMARY KEY, order_id INTEGER, item_id INTEGER, " +
                    "quantity INTEGER, price_at_purchase REAL)");
        }
    }

    private void insertTestData() throws SQLException {
        // Insert characters
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO characters VALUES (?, ?)")) {
            pstmt.setInt(1, 1); pstmt.setString(2, "Chloe"); pstmt.executeUpdate();
            pstmt.setInt(1, 2); pstmt.setString(2, "Mimi"); pstmt.executeUpdate();
        }

        // Insert food items
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO food_items VALUES (?,?,?,?,?,?,?)")) {
            // item_id, name, price, stock, item_type, character_id, is_active
            pstmt.setInt(1, 1); pstmt.setString(2, "Burger"); pstmt.setDouble(3, 10.99);
            pstmt.setInt(4, 50); pstmt.setString(5, "Main"); pstmt.setInt(6, 1); pstmt.setInt(7, 1);
            pstmt.executeUpdate();

            pstmt.setInt(1, 2); pstmt.setString(2, "Fries"); pstmt.setDouble(3, 3.50);
            pstmt.setInt(4, 100); pstmt.setString(5, "Side"); pstmt.setInt(6, 1); pstmt.setInt(7, 1);
            pstmt.executeUpdate();

            pstmt.setInt(1, 3); pstmt.setString(2, "Cake"); pstmt.setDouble(3, 5.00);
            pstmt.setInt(4, 20); pstmt.setString(5, "Dessert"); pstmt.setInt(6, 2); pstmt.setInt(7, 1);
            pstmt.executeUpdate();
        }

        // Insert orders (some completed, one cancelled)
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO orders VALUES (?,?,?,?,?)")) {
            // order_id, user_id, total_price, status, order_date
            pstmt.setInt(1, 101); pstmt.setInt(2, 1); pstmt.setDouble(3, 14.49);
            pstmt.setString(4, "completed"); pstmt.setString(5, "2025-03-01 10:00:00");
            pstmt.executeUpdate();

            pstmt.setInt(1, 102); pstmt.setInt(2, 1); pstmt.setDouble(3, 5.00);
            pstmt.setString(4, "completed"); pstmt.setString(5, "2025-03-02 12:00:00");
            pstmt.executeUpdate();

            pstmt.setInt(1, 103); pstmt.setInt(2, 1); pstmt.setDouble(3, 10.99);
            pstmt.setString(4, "cancelled"); pstmt.setString(5, "2025-03-03 09:00:00");
            pstmt.executeUpdate();

            pstmt.setInt(1, 104); pstmt.setInt(2, 1); pstmt.setDouble(3, 3.50);
            pstmt.setString(4, "completed"); pstmt.setString(5, "2025-03-04 14:00:00");
            pstmt.executeUpdate();
        }

        // Insert order_items
        try (PreparedStatement pstmt = connection.prepareStatement(
                "INSERT INTO order_items VALUES (?,?,?,?,?)")) {
            // order_item_id, order_id, item_id, quantity, price_at_purchase
            // Order 101: Burger + Fries
            pstmt.setInt(1, 1); pstmt.setInt(2, 101); pstmt.setInt(3, 1); pstmt.setInt(4, 1); pstmt.setDouble(5, 10.99);
            pstmt.executeUpdate();
            pstmt.setInt(1, 2); pstmt.setInt(2, 101); pstmt.setInt(3, 2); pstmt.setInt(4, 1); pstmt.setDouble(5, 3.50);
            pstmt.executeUpdate();

            // Order 102: Cake
            pstmt.setInt(1, 3); pstmt.setInt(2, 102); pstmt.setInt(3, 3); pstmt.setInt(4, 1); pstmt.setDouble(5, 5.00);
            pstmt.executeUpdate();

            // Order 103: cancelled – should be excluded from reports
            pstmt.setInt(1, 4); pstmt.setInt(2, 103); pstmt.setInt(3, 1); pstmt.setInt(4, 1); pstmt.setDouble(5, 10.99);
            pstmt.executeUpdate();

            // Order 104: Fries only
            pstmt.setInt(1, 5); pstmt.setInt(2, 104); pstmt.setInt(3, 2); pstmt.setInt(4, 1); pstmt.setDouble(5, 3.50);
            pstmt.executeUpdate();
        }
    }

    // ------------------------- Tests -------------------------

    @Test
    void testGetSalesSummary_NoDateRange() {
        SalesDAO.SalesSummary summary = salesDAO.getSalesSummary(null, null);

        // Completed orders: 101 (14.49), 102 (5.00), 104 (3.50) → total = 22.99
        // Cancelled order 103 excluded
        assertEquals(22.99, summary.getTotalRevenue(), 0.01);
        assertEquals(3, summary.getTotalOrders());
        // Items sold: order101(2 items) + order102(1) + order104(1) = 4
        assertEquals(4, summary.getTotalItemsSold());
        assertEquals(22.99 / 3, summary.getAvgOrderValue(), 0.01);
    }

    @Test
    void testGetSalesSummary_WithDateRange() {
        LocalDate start = LocalDate.of(2025, 3, 2);
        LocalDate end = LocalDate.of(2025, 3, 4);
        SalesDAO.SalesSummary summary = salesDAO.getSalesSummary(start, end);

        // Orders within range: 102 (5.00) and 104 (3.50) → total = 8.50, 2 orders, items = 2
        assertEquals(8.50, summary.getTotalRevenue(), 0.01);
        assertEquals(2, summary.getTotalOrders());
        assertEquals(2, summary.getTotalItemsSold());
        assertEquals(8.50 / 2, summary.getAvgOrderValue(), 0.01);
    }

    @Test
    void testGetSalesSummary_EmptyDateRange() {
        LocalDate start = LocalDate.of(2025, 4, 1);
        LocalDate end = LocalDate.of(2025, 4, 5);
        SalesDAO.SalesSummary summary = salesDAO.getSalesSummary(start, end);

        assertEquals(0.0, summary.getTotalRevenue(), 0.01);
        assertEquals(0, summary.getTotalOrders());
        assertEquals(0, summary.getTotalItemsSold());
        assertEquals(0.0, summary.getAvgOrderValue(), 0.01);
    }

    @Test
    void testGetPopularItems_NoDateRange() {
        List<SalesDAO.PopularItem> popular = salesDAO.getPopularItems(null, null);

        // Expected order: Fries (ordered twice: orders 101 & 104 → timesOrdered=2, revenue=7.00)
        //                  Burger (once → 1, 10.99)
        //                  Cake (once → 1, 5.00)
        assertFalse(popular.isEmpty());
        assertEquals(3, popular.size());

        SalesDAO.PopularItem first = popular.get(0);
        assertEquals("Fries", first.getName());
        assertEquals(2, first.getTimesOrdered());
        assertEquals(7.00, first.getRevenue(), 0.01);
    }

    @Test
    void testGetPopularItems_WithDateRange() {
        LocalDate start = LocalDate.of(2025, 3, 3);
        LocalDate end = LocalDate.of(2025, 3, 4);
        List<SalesDAO.PopularItem> popular = salesDAO.getPopularItems(start, end);

        // Only orders 104 (Fries) and 103 is cancelled so excluded
        assertEquals(1, popular.size());
        assertEquals("Fries", popular.get(0).getName());
        assertEquals(1, popular.get(0).getTimesOrdered());
        assertEquals(3.50, popular.get(0).getRevenue(), 0.01);
    }

    @Test
    void testGetPopularItemsWithFilters_CategoryFilter() {
        List<SalesDAO.PopularItem> filtered = salesDAO.getPopularItemsWithFilters(
                null, null, "Main", null, null);

        // Only Main category items: Burger
        assertEquals(1, filtered.size());
        assertEquals("Burger", filtered.get(0).getName());
    }

    @Test
    void testGetPopularItemsWithFilters_PriceRange() {
        List<SalesDAO.PopularItem> filtered = salesDAO.getPopularItemsWithFilters(
                null, null, null, 4.0, 6.0);

        // Price between 4 and 6: Cake (5.00)
        assertEquals(1, filtered.size());
        assertEquals("Cake", filtered.get(0).getName());
    }

    @Test
    void testGetPopularItemsWithFilters_CombinedFilters() {
        List<SalesDAO.PopularItem> filtered = salesDAO.getPopularItemsWithFilters(
                null, null, "Side", 3.0, 4.0);

        // Side category, price 3-4: Fries (3.50)
        assertEquals(1, filtered.size());
        assertEquals("Fries", filtered.get(0).getName());
    }

    @Test
    void testGetRecentOrders_NoDateRange() {
        List<Order> orders = salesDAO.getRecentOrders(null, null);
        // Should return up to 50, but we have 3 completed orders + maybe cancelled? The SQL excludes cancelled? Actually the query doesn't filter status, so all orders appear.
        // In our data: orders 101,102,103,104 → 4 total. Cancelled is included in recent orders? The method doesn't filter status.
        // So we check size = 4.
        assertEquals(4, orders.size());
        // First should be order 104 (most recent)
        assertEquals(104, orders.get(0).getOrderId());
    }

    @Test
    void testGetRecentOrders_WithDateRange() {
        LocalDate start = LocalDate.of(2025, 3, 2);
        LocalDate end = LocalDate.of(2025, 3, 3);
        List<Order> orders = salesDAO.getRecentOrders(start, end);
        // Orders 102 and 103 (cancelled also included)
        assertEquals(2, orders.size());
    }

    // Add these methods to your existing SalesDAOTest.java

    @Test
    void testGetSalesSummary_ExcludesCancelledOrders() {
        // The cancelled order (103) should NOT be included
        SalesDAO.SalesSummary summary = salesDAO.getSalesSummary(null, null);

        // Only orders 101, 102, 104 should count (3 orders)
        assertEquals(3, summary.getTotalOrders());
        // Total should not include order 103 (10.99)
        assertEquals(22.99, summary.getTotalRevenue(), 0.01);
    }

    // Replace those two test methods with these:

    @Test
    void testGetPopularItems_LimitTen() {
        // Add 15 items to test limit
        for (int i = 0; i < 15; i++) {
            try (PreparedStatement pstmt = connection.prepareStatement(
                    "INSERT INTO food_items (item_id, name, price, stock, item_type, character_id, is_active) VALUES (?,?,?,?,?,?,?)")) {
                pstmt.setInt(1, 100 + i);
                pstmt.setString(2, "Item" + i);
                pstmt.setDouble(3, 5.00);
                pstmt.setInt(4, 100);
                pstmt.setString(5, "Main");
                pstmt.setInt(6, 1);
                pstmt.setInt(7, 1);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                fail("Failed to insert test data: " + e.getMessage());
            }
        }

        List<SalesDAO.PopularItem> popular = salesDAO.getPopularItems(null, null);
        assertTrue(popular.size() <= 10);
    }

    @Test
    void testGetRecentOrders_LimitFifty() {
        // Add 60 orders
        for (int i = 0; i < 60; i++) {
            try (PreparedStatement pstmt = connection.prepareStatement(
                    "INSERT INTO orders (order_id, user_id, total_price, status, order_date) VALUES (?,?,?,?,?)")) {
                pstmt.setInt(1, 200 + i);
                pstmt.setInt(2, 1);
                pstmt.setDouble(3, 10.00);
                pstmt.setString(4, "completed");
                pstmt.setString(5, "2025-03-10 10:00:00");
                pstmt.executeUpdate();
            } catch (SQLException e) {
                fail("Failed to insert test data: " + e.getMessage());
            }
        }

        List<Order> orders = salesDAO.getRecentOrders(null, null);
        assertTrue(orders.size() <= 50);
    }



    // ------------------------- Helper: TestSalesDAO -------------------------
    /**
     * A test-specific SalesDAO that uses a provided Connection instead of the singleton.
     */
    private static class TestSalesDAO extends SalesDAO {
        private final Connection testConnection;

        TestSalesDAO(Connection conn) {
            this.testConnection = conn;
        }

        @Override
        protected Connection getConnection() {
            return testConnection;
        }
    }
}