package com.example.fixit.common.config;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates that db.changelog-master.yaml applies cleanly to an empty Postgres
 * matching the production image. Uses a dedicated container (not the shared
 * reusable one in PostgresIntegrationBase) so every run starts from scratch.
 */
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LiquibaseMigrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17");

    private static final String CHANGELOG = "db/changelog/db.changelog-master.yaml";

    private static final Set<String> EXPECTED_CHANGESET_IDS = Set.of(
            "001-create-sequences",
            "001-create-stores",
            "001-create-products",
            "001-create-product-images",
            "001-create-tires",
            "001-create-brakes",
            "001-create-batteries",
            "001-create-alternators",
            "001-create-air-filters",
            "001-create-oil-filters",
            "001-create-shock-absorbers",
            "001-create-spark-plugs",
            "001-create-suspension-springs",
            "001-create-transmission-fluids",
            "002-create-users",
            "002-create-store-staff",
            "003-create-carts",
            "003-create-cart-items"
    );

    @BeforeAll
    void applyChangelog() throws Exception {
        runLiquibaseUpdate();
    }

    @Test
    void changelog_appliesOnEmptyDb_recordingEveryChangeset() throws Exception {
        List<String> appliedIds = queryList("SELECT id FROM databasechangelog ORDER BY orderexecuted");

        assertTrue(appliedIds.containsAll(EXPECTED_CHANGESET_IDS),
                "Missing changesets: expected=" + EXPECTED_CHANGESET_IDS + " actual=" + appliedIds);
    }

    @Test
    void coreTablesExist() throws Exception {
        List<String> tables = queryList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'");

        assertTrue(tables.containsAll(List.of(
                        "users", "store_staff", "stores", "products", "product_images",
                        "tires", "brakes", "batteries", "alternators", "air_filters",
                        "oil_filters", "shock_absorbers", "spark_plugs",
                        "suspension_springs", "transmission_fluids",
                        "carts", "cart_items")),
                "Missing table(s) — got: " + tables);
    }

    @Test
    void criticalUniqueConstraintsExist() throws Exception {
        List<String> constraints = queryList(
                "SELECT constraint_name FROM information_schema.table_constraints " +
                        "WHERE table_schema = 'public' AND constraint_type = 'UNIQUE'");

        assertTrue(constraints.contains("uk_users_keycloak_id"),
                "Missing uk_users_keycloak_id — got: " + constraints);
        assertTrue(constraints.contains("uk_store_staff_user_store"),
                "Missing uk_store_staff_user_store — got: " + constraints);
    }

    @Test
    void rerun_isIdempotent() throws Exception {
        int before = countChangelogRows();
        runLiquibaseUpdate();
        int after = countChangelogRows();

        assertEquals(before, after, "Liquibase re-run created new changeset rows");
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void runLiquibaseUpdate() throws Exception {
        try (Connection conn = openConnection()) {
            Database db = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(conn));
            try (Liquibase liquibase =
                         new Liquibase(CHANGELOG, new ClassLoaderResourceAccessor(), db)) {
                liquibase.update(new Contexts(), new LabelExpression());
            }
        }
    }

    private Connection openConnection() throws Exception {
        return DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
    }

    private List<String> queryList(String sql) throws Exception {
        try (Connection conn = openConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<String> out = new ArrayList<>();
            while (rs.next()) {
                out.add(rs.getString(1));
            }
            return out;
        }
    }

    private int countChangelogRows() throws Exception {
        try (Connection conn = openConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM databasechangelog");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }
}
