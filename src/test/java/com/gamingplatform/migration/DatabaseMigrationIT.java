package com.gamingplatform.migration;

import com.gamingplatform.entity.Difficulty;
import com.gamingplatform.GamingPlatformApplication;
import com.gamingplatform.entity.Challenge;
import com.gamingplatform.repository.ChallengeRepository;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.flywaydb.core.api.output.MigrateResult;
import org.flywaydb.core.api.output.ValidateResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
class DatabaseMigrationIT {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("migration_test")
            .withUsername("migration_user")
            .withPassword("migration_password")
            .withStartupTimeout(Duration.ofMinutes(2));

    @Container
    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>(
            DockerImageName.parse("mysql:8.4"))
            .withDatabaseName("migration_test")
            .withUsername("migration_user")
            .withPassword("migration_password")
            .withUrlParam("useUnicode", "true")
            .withUrlParam("characterEncoding", "UTF-8")
            .withCommand("--character-set-server=utf8mb4", "--collation-server=utf8mb4_0900_ai_ci")
            .withStartupTimeout(Duration.ofMinutes(3));

    @Test
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    void postgresqlFreshAndLegacyMigrationsAreSafe() throws Exception {
        assertThat(POSTGRES.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT)).isNotEqualTo(8080);
        verifyFreshAndLegacy(new DatabaseTarget(
                "PostgreSQL",
                POSTGRES.getJdbcUrl(),
                POSTGRES.getUsername(),
                POSTGRES.getPassword(),
                "org.postgresql.Driver",
                "classpath:db/migration/postgresql",
                "db/migration/postgresql/V1__legacy_baseline.sql",
                "legacy/postgresql.sql",
                "public"));
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    void mysqlFreshAndLegacyMigrationsAreSafe() throws Exception {
        assertThat(MYSQL.getMappedPort(MySQLContainer.MYSQL_PORT)).isNotEqualTo(8080);
        verifyFreshAndLegacy(new DatabaseTarget(
                "MySQL",
                MYSQL.getJdbcUrl(),
                MYSQL.getUsername(),
                MYSQL.getPassword(),
                "com.mysql.cj.jdbc.Driver",
                "classpath:db/migration/mysql",
                "db/migration/mysql/V1__legacy_baseline.sql",
                "legacy/mysql.sql",
                null));
    }

    private void verifyFreshAndLegacy(DatabaseTarget target) throws Exception {
        Flyway freshFlyway = flyway(target, false);
        freshFlyway.clean();

        MigrateResult freshResult = freshFlyway.migrate();
        assertThat(freshResult.migrationsExecuted)
                .as("%s fresh schema should apply V1 and V2", target.name())
                .isEqualTo(2);
        assertValidationSucceeds(freshFlyway, target);
        assertFreshHistory(target);
        assertSchemaContract(target);
        assertHibernateMapping(target, false);
        assertThat(freshFlyway.migrate().migrationsExecuted)
                .as("%s fresh migration should be idempotent", target.name())
                .isZero();

        freshFlyway.clean();
        executeScript(target, target.v1Fixture());
        executeScript(target, target.legacyDataFixture());
        assertLegacyFixtureBeforeMigration(target);

        Flyway legacyFlyway = flyway(target, true);
        MigrateResult legacyResult = legacyFlyway.migrate();
        assertThat(legacyResult.migrationsExecuted)
                .as("%s legacy schema should baseline V1 and apply only V2", target.name())
                .isEqualTo(1);
        assertValidationSucceeds(legacyFlyway, target);
        assertLegacyHistory(target);
        assertSchemaContract(target);
        assertLegacyDataWasPreservedAndBackfilled(target);
        assertDatabaseConstraintsAreEnforced(target);
        assertHibernateMapping(target, true);

        assertThat(legacyFlyway.migrate().migrationsExecuted)
                .as("%s legacy migration should be idempotent", target.name())
                .isZero();
        assertThat(queryLong(target, "SELECT COUNT(*) FROM submissions")).isEqualTo(3);
        assertThat(queryString(target, "SELECT feedback FROM evaluations WHERE id = 401"))
                .startsWith("迁移后长文本🙂");
    }

    private Flyway flyway(DatabaseTarget target, boolean baselineOnMigrate) {
        FluentConfiguration configuration = Flyway.configure()
                .dataSource(target.jdbcUrl(), target.username(), target.password())
                .locations(target.migrationLocation())
                .cleanDisabled(false)
                .baselineOnMigrate(baselineOnMigrate);
        if (baselineOnMigrate) {
            configuration.baselineVersion(MigrationVersion.fromVersion("1"));
        }
        return configuration.load();
    }

    private void assertValidationSucceeds(Flyway flyway, DatabaseTarget target) {
        ValidateResult validation = flyway.validateWithResult();
        assertThat(validation.validationSuccessful)
                .as("%s Flyway validation errors: %s", target.name(), validation.invalidMigrations)
                .isTrue();
    }

    private void assertFreshHistory(DatabaseTarget target) throws SQLException {
        assertThat(readHistory(target)).containsExactly(
                new HistoryRow("1", "SQL", true),
                new HistoryRow("2", "SQL", true));
    }

    private void assertLegacyHistory(DatabaseTarget target) throws SQLException {
        assertThat(readHistory(target)).containsExactly(
                new HistoryRow("1", "BASELINE", true),
                new HistoryRow("2", "SQL", true));
    }

    private List<HistoryRow> readHistory(DatabaseTarget target) throws SQLException {
        List<HistoryRow> rows = new ArrayList<>();
        try (Connection connection = connect(target);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "SELECT version, type, success FROM flyway_schema_history ORDER BY installed_rank")) {
            while (resultSet.next()) {
                rows.add(new HistoryRow(
                        resultSet.getString("version"),
                        resultSet.getString("type").toUpperCase(Locale.ROOT),
                        resultSet.getBoolean("success")));
            }
        }
        return rows;
    }

    private void assertSchemaContract(DatabaseTarget target) throws SQLException {
        try (Connection connection = connect(target)) {
            DatabaseMetaData metadata = connection.getMetaData();
            String catalog = connection.getCatalog();

            assertThat(indexNames(metadata, catalog, target.schema(), "challenges"))
                    .contains("idx_challenges_created_by");
            assertThat(indexNames(metadata, catalog, target.schema(), "submissions"))
                    .contains(
                            "idx_submissions_user_submitted",
                            "idx_submissions_challenge_user",
                            "idx_submissions_status_started",
                            "idx_submissions_user_status_challenge");
            assertThat(indexNames(metadata, catalog, target.schema(), "challenge_requirements"))
                    .contains("idx_challenge_requirements_challenge");
            assertThat(indexNames(metadata, catalog, target.schema(), "challenge_constraints"))
                    .contains("idx_challenge_constraints_challenge");
            assertThat(indexNames(metadata, catalog, target.schema(), "challenge_acceptance_criteria"))
                    .contains("idx_challenge_criteria_challenge");

            assertThat(uniqueIndexes(metadata, catalog, target.schema(), "submissions").values())
                    .contains(List.of("user_id", "idempotency_key"));
            assertThat(uniqueIndexes(metadata, catalog, target.schema(), "evaluations").values())
                    .contains(List.of("submission_id"));
            assertThat(hasForeignKey(metadata, catalog, target.schema(),
                    "challenges", "created_by_id", "users"))
                    .as("%s challenges.created_by_id foreign key", target.name())
                    .isTrue();

            assertNotNullable(metadata, catalog, target.schema(), "submissions", "answer_hash");
            assertNotNullable(metadata, catalog, target.schema(), "submissions", "idempotency_key");
            assertNotNullable(metadata, catalog, target.schema(), "submissions", "status");
            assertNotNullable(metadata, catalog, target.schema(), "evaluations", "provider");
            assertNotNullable(metadata, catalog, target.schema(), "evaluations", "strengths_json");
            assertNotNullable(metadata, catalog, target.schema(), "evaluations", "improvements_json");
            assertNotNullable(metadata, catalog, target.schema(), "evaluations", "example_outline");
            assertNotNullable(metadata, catalog, target.schema(), "evaluations", "rubric_weights_json");
        }
    }

    private void assertLegacyFixtureBeforeMigration(DatabaseTarget target) throws SQLException {
        assertThat(tableExists(target, "flyway_schema_history")).isFalse();
        assertThat(queryLong(target, "SELECT COUNT(*) FROM users")).isEqualTo(2);
        assertThat(queryLong(target, "SELECT COUNT(*) FROM challenges")).isEqualTo(2);
        assertThat(queryLong(target, "SELECT COUNT(*) FROM submissions")).isEqualTo(3);
        assertThat(queryLong(target, "SELECT COUNT(*) FROM evaluations")).isEqualTo(3);

        String answer = queryString(target, "SELECT answer FROM submissions WHERE id = 301");
        String feedback = queryString(target, "SELECT feedback FROM evaluations WHERE id = 401");
        assertThat(answer).startsWith("迁移前回答🙂—设计边界；").hasSizeGreaterThan(4_000);
        assertThat(feedback).startsWith("迁移前反馈🙂—保持原文；").hasSizeGreaterThan(3_000);
    }

    private void assertLegacyDataWasPreservedAndBackfilled(DatabaseTarget target) throws SQLException {
        assertThat(queryLong(target, "SELECT COUNT(*) FROM users")).isEqualTo(2);
        assertThat(queryLong(target, "SELECT COUNT(*) FROM challenges")).isEqualTo(2);
        assertThat(queryLong(target, "SELECT COUNT(*) FROM submissions")).isEqualTo(3);
        assertThat(queryLong(target, "SELECT COUNT(*) FROM evaluations")).isEqualTo(3);
        assertThat(queryLong(target, "SELECT xp FROM users WHERE id = 101")).isEqualTo(90);
        assertThat(Difficulty.valueOf(queryString(target,
                "SELECT difficulty FROM challenges WHERE id = 201")))
                .isEqualTo(Difficulty.INTERMEDIATE);
        assertThat(Difficulty.valueOf(queryString(target,
                "SELECT difficulty FROM challenges WHERE id = 202")))
                .isEqualTo(Difficulty.ADVANCED);

        assertThat(queryNullableLong(target,
                "SELECT created_by_id FROM challenges WHERE id = 201")).isEqualTo(101L);
        assertThat(queryNullableLong(target,
                "SELECT created_by_id FROM challenges WHERE id = 202")).isNull();
        assertThat(queryLong(target,
                "SELECT COUNT(*) FROM challenges WHERE generation_provider = 'legacy'")).isEqualTo(2);

        String legacyAnswer = queryString(target, "SELECT answer FROM submissions WHERE id = 301");
        String legacyFeedback = queryString(target, "SELECT feedback FROM evaluations WHERE id = 401");
        assertThat(legacyAnswer).startsWith("迁移前回答🙂—设计边界；").hasSizeGreaterThan(4_000);
        assertThat(legacyFeedback).startsWith("迁移前反馈🙂—保持原文；").hasSizeGreaterThan(3_000);

        assertThat(queryString(target, "SELECT answer_hash FROM submissions WHERE id = 301"))
                .isEqualTo("0".repeat(64));
        assertThat(queryString(target, "SELECT idempotency_key FROM submissions WHERE id = 301"))
                .isEqualTo("legacy-301");
        assertThat(queryString(target, "SELECT status FROM submissions WHERE id = 301"))
                .isEqualTo("COMPLETED");
        assertThat(queryLong(target,
                "SELECT COUNT(*) FROM submissions WHERE completed_at = submitted_at")).isEqualTo(3);

        assertThat(queryLong(target,
                "SELECT COUNT(*) FROM evaluations WHERE provider = 'legacy'")).isEqualTo(3);
        assertThat(queryString(target, "SELECT strengths_json FROM evaluations WHERE id = 401"))
                .isEqualTo("[]");
        assertThat(queryString(target, "SELECT improvements_json FROM evaluations WHERE id = 401"))
                .isEqualTo("[]");
        assertThat(queryString(target, "SELECT example_outline FROM evaluations WHERE id = 401"))
                .isEqualTo("Legacy evaluation");
        assertThat(queryString(target, "SELECT rubric_weights_json FROM evaluations WHERE id = 401"))
                .isEqualTo("{\"REQUIREMENT_UNDERSTANDING\":0.25,\"LOGICAL_CLARITY\":0.20,\"TECHNICAL_FEASIBILITY\":0.25,\"EDGE_CASE_COVERAGE\":0.15,\"COMMUNICATION_STRUCTURE\":0.15}");

        String postMigrationLongUnicode = "迁移后长文本🙂" + "容量、边界、恢复；".repeat(1_200);
        try (Connection connection = connect(target);
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE evaluations SET feedback = ? WHERE id = 401")) {
            statement.setString(1, postMigrationLongUnicode);
            assertThat(statement.executeUpdate()).isOne();
        }
        assertThat(queryString(target, "SELECT feedback FROM evaluations WHERE id = 401"))
                .isEqualTo(postMigrationLongUnicode)
                .hasSizeGreaterThan(5_000);
    }

    private void assertDatabaseConstraintsAreEnforced(DatabaseTarget target) {
        assertThatThrownBy(() -> {
            try (Connection connection = connect(target);
                 PreparedStatement statement = connection.prepareStatement("""
                         INSERT INTO submissions (
                             id, user_id, challenge_id, answer, submitted_at,
                             answer_hash, idempotency_key, status
                         ) VALUES (399, 101, 201, 'duplicate', CURRENT_TIMESTAMP, ?, 'legacy-301', 'COMPLETED')
                         """)) {
                statement.setString(1, "a".repeat(64));
                statement.executeUpdate();
            }
        }).as("%s should enforce unique (user_id, idempotency_key)", target.name())
                .isInstanceOf(SQLException.class);
    }

    private void assertHibernateMapping(DatabaseTarget target, boolean expectLegacyData) {
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(GamingPlatformApplication.class)
                .web(WebApplicationType.SERVLET)
                .run(
                        "--server.port=0",
                        "--spring.datasource.url=" + target.jdbcUrl(),
                        "--spring.datasource.username=" + target.username(),
                        "--spring.datasource.password=" + target.password(),
                        "--spring.datasource.driver-class-name=" + target.driverClassName(),
                        "--spring.jpa.hibernate.ddl-auto=validate",
                        "--spring.jpa.open-in-view=false",
                        "--spring.flyway.enabled=false",
                        "--spring.main.banner-mode=off",
                        "--logging.level.root=ERROR",
                        "--app.submissions.recovery-interval-ms=3600000")) {
            assertThat(context).isInstanceOf(WebServerApplicationContext.class);
            int port = ((WebServerApplicationContext) context).getWebServer().getPort();
            assertThat(port).isPositive().isNotEqualTo(8080);

            ChallengeRepository challenges = context.getBean(ChallengeRepository.class);
            if (expectLegacyData) {
                Challenge singleUserChallenge = challenges.findById(201L).orElseThrow();
                Challenge sharedChallenge = challenges.findById(202L).orElseThrow();
                assertThat(singleUserChallenge.getDifficulty()).isEqualTo(Difficulty.INTERMEDIATE);
                assertThat(sharedChallenge.getDifficulty()).isEqualTo(Difficulty.ADVANCED);
                assertThat(singleUserChallenge.getCreatedBy()).isNotNull();
                assertThat(singleUserChallenge.getCreatedBy().getId()).isEqualTo(101L);
                assertThat(sharedChallenge.getCreatedBy()).isNull();
            } else {
                assertThat(challenges.count()).isZero();
            }
        }
    }

    private void executeScript(DatabaseTarget target, String classpathLocation) throws SQLException {
        try (Connection connection = connect(target)) {
            ScriptUtils.executeSqlScript(connection, new EncodedResource(
                    new ClassPathResource(classpathLocation), StandardCharsets.UTF_8));
        }
    }

    private boolean tableExists(DatabaseTarget target, String tableName) throws SQLException {
        try (Connection connection = connect(target);
             ResultSet tables = connection.getMetaData().getTables(
                     connection.getCatalog(), target.schema(), tableName, new String[]{"TABLE"})) {
            return tables.next();
        }
    }

    private long queryLong(DatabaseTarget target, String sql) throws SQLException {
        try (Connection connection = connect(target);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            assertThat(resultSet.next()).isTrue();
            return resultSet.getLong(1);
        }
    }

    private Long queryNullableLong(DatabaseTarget target, String sql) throws SQLException {
        try (Connection connection = connect(target);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            assertThat(resultSet.next()).isTrue();
            long value = resultSet.getLong(1);
            return resultSet.wasNull() ? null : value;
        }
    }

    private String queryString(DatabaseTarget target, String sql) throws SQLException {
        try (Connection connection = connect(target);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            assertThat(resultSet.next()).isTrue();
            return resultSet.getString(1);
        }
    }

    private Connection connect(DatabaseTarget target) throws SQLException {
        return DriverManager.getConnection(target.jdbcUrl(), target.username(), target.password());
    }

    private List<String> indexNames(DatabaseMetaData metadata, String catalog, String schema, String table)
            throws SQLException {
        List<String> names = new ArrayList<>();
        try (ResultSet indexes = metadata.getIndexInfo(catalog, schema, table, false, false)) {
            while (indexes.next()) {
                String name = indexes.getString("INDEX_NAME");
                if (name != null) {
                    names.add(name.toLowerCase(Locale.ROOT));
                }
            }
        }
        return names;
    }

    private Map<String, List<String>> uniqueIndexes(
            DatabaseMetaData metadata, String catalog, String schema, String table) throws SQLException {
        Map<String, TreeMap<Short, String>> orderedColumns = new LinkedHashMap<>();
        try (ResultSet indexes = metadata.getIndexInfo(catalog, schema, table, true, false)) {
            while (indexes.next()) {
                String indexName = indexes.getString("INDEX_NAME");
                String columnName = indexes.getString("COLUMN_NAME");
                if (indexName != null && columnName != null) {
                    orderedColumns.computeIfAbsent(indexName, ignored -> new TreeMap<>())
                            .put(indexes.getShort("ORDINAL_POSITION"), columnName.toLowerCase(Locale.ROOT));
                }
            }
        }
        Map<String, List<String>> result = new LinkedHashMap<>();
        orderedColumns.forEach((index, columns) -> result.put(index, List.copyOf(columns.values())));
        return result;
    }

    private boolean hasForeignKey(
            DatabaseMetaData metadata,
            String catalog,
            String schema,
            String table,
            String foreignKeyColumn,
            String primaryKeyTable) throws SQLException {
        try (ResultSet keys = metadata.getImportedKeys(catalog, schema, table)) {
            while (keys.next()) {
                if (foreignKeyColumn.equalsIgnoreCase(keys.getString("FKCOLUMN_NAME"))
                        && primaryKeyTable.equalsIgnoreCase(keys.getString("PKTABLE_NAME"))) {
                    return true;
                }
            }
        }
        return false;
    }

    private void assertNotNullable(
            DatabaseMetaData metadata,
            String catalog,
            String schema,
            String table,
            String column) throws SQLException {
        try (ResultSet columns = metadata.getColumns(catalog, schema, table, column)) {
            assertThat(columns.next()).as("column %s.%s exists", table, column).isTrue();
            assertThat(columns.getInt("NULLABLE"))
                    .as("column %s.%s is NOT NULL", table, column)
                    .isEqualTo(DatabaseMetaData.columnNoNulls);
        }
    }

    private record DatabaseTarget(
            String name,
            String jdbcUrl,
            String username,
            String password,
            String driverClassName,
            String migrationLocation,
            String v1Fixture,
            String legacyDataFixture,
            String schema) {
    }

    private record HistoryRow(String version, String type, boolean success) {
    }
}
