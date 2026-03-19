package com.hotel.book.config;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class BookingStatusSchemaInitializer {

    private static final Logger log = LoggerFactory.getLogger(BookingStatusSchemaInitializer.class);
    private static final int REQUIRED_STATUS_LENGTH = 20;

    @Bean
    ApplicationRunner normalizeBookingStatusColumn(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        return args -> {
            if (!bookingsTableExists(dataSource)) {
                return;
            }

            BookingStatusColumnInfo columnInfo = getStatusColumnInfo(jdbcTemplate);
            if (columnInfo == null) {
                return;
            }

            boolean needsAlter =
                    !"varchar".equalsIgnoreCase(columnInfo.dataType())
                            || columnInfo.characterMaximumLength() == null
                            || columnInfo.characterMaximumLength() < REQUIRED_STATUS_LENGTH;

            if (!needsAlter) {
                normalizeLegacyBookingStatuses(jdbcTemplate);
                return;
            }

            log.warn(
                    "Updating bookings.status column to VARCHAR({}) to support current BookingStatus values. Current type={}, length={}",
                    REQUIRED_STATUS_LENGTH,
                    columnInfo.dataType(),
                    columnInfo.characterMaximumLength());

            jdbcTemplate.execute("ALTER TABLE bookings MODIFY COLUMN status VARCHAR(20) NOT NULL");
            normalizeLegacyBookingStatuses(jdbcTemplate);
        };
    }

    private void normalizeLegacyBookingStatuses(JdbcTemplate jdbcTemplate) {
        int updatedCreated = jdbcTemplate.update(
                "UPDATE bookings SET status = 'CONFIRMED' WHERE status = 'CREATED'");
        int updatedCompleted = jdbcTemplate.update(
                "UPDATE bookings SET status = 'CONFIRMED' WHERE status = 'COMPLETED'");

        if (updatedCreated > 0 || updatedCompleted > 0) {
            log.warn(
                    "Normalized legacy booking statuses to the simplified lifecycle. createdToConfirmed={}, completedToConfirmed={}",
                    updatedCreated,
                    updatedCompleted);
        }
    }

    private boolean bookingsTableExists(DataSource dataSource) throws SQLException {
        try (var connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String catalog = connection.getCatalog();
            try (ResultSet tables = metaData.getTables(catalog, null, "bookings", null)) {
                return tables.next();
            }
        }
    }

    private BookingStatusColumnInfo getStatusColumnInfo(JdbcTemplate jdbcTemplate) {
        return jdbcTemplate.query(
                """
                SELECT DATA_TYPE, CHARACTER_MAXIMUM_LENGTH
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'bookings'
                  AND COLUMN_NAME = 'status'
                """,
                rs -> rs.next()
                        ? new BookingStatusColumnInfo(
                                rs.getString("DATA_TYPE"),
                                toInteger(rs.getObject("CHARACTER_MAXIMUM_LENGTH")))
                        : null);
    }

    private Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        return ((Number) value).intValue();
    }

    private record BookingStatusColumnInfo(String dataType, Integer characterMaximumLength) {
    }
}
