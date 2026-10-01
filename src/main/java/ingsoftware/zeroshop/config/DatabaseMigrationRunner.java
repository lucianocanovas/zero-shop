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

        try {
            jdbcTemplate.execute("ALTER TABLE users DROP CONSTRAINT IF EXISTS uk97ih1g5lcdf1s3fg7oo4e18jw");
            jdbcTemplate.execute("ALTER TABLE users DROP CONSTRAINT IF EXISTS users_person_id_key");
            jdbcTemplate.execute(
                "DO $$ DECLARE r RECORD; BEGIN " +
                "FOR r IN (SELECT conname FROM pg_constraint WHERE conrelid = 'users'::regclass AND contype = 'u' " +
                "AND array_to_string(conkey, ',') = (SELECT attnum::text FROM pg_attribute WHERE attrelid = 'users'::regclass AND attname = 'person_id')) " +
                "LOOP EXECUTE 'ALTER TABLE users DROP CONSTRAINT ' || quote_ident(r.conname); END LOOP; " +
                "END $$;"
            );
            log.info("Restricción única de users.person_id eliminada para permitir múltiples usuarios por persona.");
        } catch (Exception e) {
            log.warn("Aviso al verificar constraint users.person_id: {}", e.getMessage());
        }

        try {
            jdbcTemplate.execute("ALTER TABLE sale_orders DROP CONSTRAINT IF EXISTS fk48r5kf23juh8b8ryu7x06qqli");
            jdbcTemplate.execute("ALTER TABLE sale_orders DROP CONSTRAINT IF EXISTS fk_sale_orders_client");
            jdbcTemplate.execute("ALTER TABLE sale_orders ADD CONSTRAINT fk_sale_orders_client FOREIGN KEY (client_id) REFERENCES persons(id)");
            log.info("Restricción sale_orders.client_id actualizada para referenciar persons(id).");
        } catch (Exception e) {
            log.warn("Aviso de migración sale_orders.client_id: {}", e.getMessage());
        }

        try {
            jdbcTemplate.execute("ALTER TABLE person_addresses ALTER COLUMN id SET DEFAULT gen_random_uuid()");
            jdbcTemplate.execute("ALTER TABLE person_addresses ALTER COLUMN deleted SET DEFAULT false");
            log.info("Tabla person_addresses actualizada para permitir inserción de join table.");
        } catch (Exception e) {
            log.warn("No se pudo alterar defaults de person_addresses: {}", e.getMessage());
        }

        try {
            jdbcTemplate.execute("ALTER TABLE person_contacts ALTER COLUMN id SET DEFAULT gen_random_uuid()");
            jdbcTemplate.execute("ALTER TABLE person_contacts ALTER COLUMN deleted SET DEFAULT false");
            log.info("Tabla person_contacts actualizada para permitir inserción de join table.");
        } catch (Exception e) {
            log.warn("No se pudo alterar defaults de person_contacts: {}", e.getMessage());
        }

        try {
            jdbcTemplate.execute("ALTER TABLE supplier_addresses ALTER COLUMN id SET DEFAULT gen_random_uuid()");
            jdbcTemplate.execute("ALTER TABLE supplier_addresses ALTER COLUMN deleted SET DEFAULT false");
            log.info("Tabla supplier_addresses actualizada para permitir inserción de join table.");
        } catch (Exception e) {
            log.warn("No se pudo alterar defaults de supplier_addresses: {}", e.getMessage());
        }

        try {
            jdbcTemplate.execute("ALTER TABLE supplier_contacts ALTER COLUMN id SET DEFAULT gen_random_uuid()");
            jdbcTemplate.execute("ALTER TABLE supplier_contacts ALTER COLUMN deleted SET DEFAULT false");
            log.info("Tabla supplier_contacts actualizada para permitir inserción de join table.");
        } catch (Exception e) {
            log.warn("No se pudo alterar defaults de supplier_contacts: {}", e.getMessage());
        }

        try {
            jdbcTemplate.execute("ALTER TABLE office_addresses ALTER COLUMN id SET DEFAULT gen_random_uuid()");
            jdbcTemplate.execute("ALTER TABLE office_addresses ALTER COLUMN deleted SET DEFAULT false");
            log.info("Tabla office_addresses verificada para join table.");
        } catch (Exception ignored) {
        }

        try {
            jdbcTemplate.execute("ALTER TABLE office_contacts ALTER COLUMN id SET DEFAULT gen_random_uuid()");
            jdbcTemplate.execute("ALTER TABLE office_contacts ALTER COLUMN deleted SET DEFAULT false");
            log.info("Tabla office_contacts verificada para join table.");
        } catch (Exception ignored) {
        }
    }
}
