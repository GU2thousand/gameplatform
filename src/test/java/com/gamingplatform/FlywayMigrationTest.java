package com.gamingplatform;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FlywayMigrationTest {
    @Test
    void shouldCreateFreshDatabaseFromV1AndV2() {
        DriverManagerDataSource dataSource = dataSource();
        Flyway flyway = flyway(dataSource);
        assertThat(flyway.migrate().migrationsExecuted).isEqualTo(2);
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        assertThat(jdbc.queryForObject("select count(*) from information_schema.columns " +
                "where table_name='SUBMISSIONS' and column_name='IDEMPOTENCY_KEY'", Integer.class)).isEqualTo(1);
    }

    @Test
    void shouldBaselineExistingLegacySchemaAndPreserveRowsWhileApplyingV2() {
        DriverManagerDataSource dataSource = dataSource();
        new ResourceDatabasePopulator(new ClassPathResource("db/migration/h2/V1__legacy_baseline.sql"))
                .execute(dataSource);
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        jdbc.update("insert into users(username,xp,created_at) values (?,?,?)",
                "legacy-user", 12, Instant.now());
        Long userId = jdbc.queryForObject("select id from users where username='legacy-user'", Long.class);
        jdbc.update("insert into challenges(title,difficulty,context,expected_output_format,created_at) " +
                        "values (?,?,?,?,?)", "Legacy challenge", "INTERMEDIATE", "Legacy context", "Markdown",
                Instant.now());
        Long challengeId = jdbc.queryForObject("select id from challenges where title='Legacy challenge'", Long.class);
        jdbc.update("insert into submissions(user_id,challenge_id,answer,submitted_at) values (?,?,?,?)",
                userId, challengeId, "Legacy answer", Instant.now());

        assertThat(flyway(dataSource).migrate().migrationsExecuted).isEqualTo(1);
        assertThat(jdbc.queryForObject("select xp from users where username='legacy-user'", Integer.class))
                .isEqualTo(12);
        assertThat(jdbc.queryForObject("select count(*) from information_schema.columns " +
                "where table_name='USERS' and column_name='PASSWORD_HASH'", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("select created_by_id from challenges where id=?", Long.class, challengeId))
                .isEqualTo(userId);
    }

    private DriverManagerDataSource dataSource() {
        return new DriverManagerDataSource("jdbc:h2:mem:migration_" + UUID.randomUUID() +
                ";MODE=MYSQL;DB_CLOSE_DELAY=-1", "sa", "");
    }

    private Flyway flyway(DriverManagerDataSource dataSource) {
        return Flyway.configure().dataSource(dataSource).locations("classpath:db/migration/h2")
                .baselineOnMigrate(true).baselineVersion(MigrationVersion.fromVersion("1")).load();
    }
}
