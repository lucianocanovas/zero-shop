package ingsoftware.zeroshop.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class DatabaseMigrationRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseMigrationRunner.class);
    private final JdbcTemplate jdbcTemplate;

    public DatabaseMigrationRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            jdbcTemplate.execute("ALTER TABLE orders DROP CONSTRAINT IF EXISTS orders_status_check");
            log.info("Restricción orders_status_check verificada y eliminada para permitir nuevos estados de pedido.");
        } catch (Exception e) {
            log.warn("No se pudo alterar constraint orders_status_check: {}", e.getMessage());
        }

        try {
            jdbcTemplate.execute("ALTER TABLE sale_orders ALTER COLUMN office_id DROP NOT NULL");
            log.info("Columna sale_orders.office_id verificada para permitir null en estado ON_CART.");
        } catch (Exception e) {
            log.warn("No se pudo alterar constraint office_id: {}", e.getMessage());
        }
    }
}
