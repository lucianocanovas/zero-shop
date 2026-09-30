package ingsoftware.zeroshop.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ingsoftware.zeroshop.entity.actor.*;
import ingsoftware.zeroshop.entity.catalog.*;
import ingsoftware.zeroshop.entity.location.*;
import ingsoftware.zeroshop.entity.org.*;
import ingsoftware.zeroshop.entity.transaction.*;
import ingsoftware.zeroshop.enums.*;
import ingsoftware.zeroshop.repository.actor.*;
import ingsoftware.zeroshop.repository.catalog.*;
import ingsoftware.zeroshop.repository.location.*;
import ingsoftware.zeroshop.repository.org.*;
import ingsoftware.zeroshop.repository.transaction.*;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Component
@Order(1)
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    // Repositories
    private final CountryRepository countryRepository;
    private final StateRepository stateRepository;
    private final CityRepository cityRepository;

    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final ProductRepository productRepository;
    private final PriceHistoryRepository priceHistoryRepository;

    private final OfficeRepository officeRepository;
    private final StockRepository stockRepository;

    private final SupplierRepository supplierRepository;
    private final SupplierProductRepository supplierProductRepository;

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SaleOrderRepository saleOrderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final PaymentRepository paymentRepository;

    public DataInitializer(JdbcTemplate jdbcTemplate,
                           PasswordEncoder passwordEncoder,
                           CountryRepository countryRepository,
                           StateRepository stateRepository,
                           CityRepository cityRepository,
                           CategoryRepository categoryRepository,
                           SubCategoryRepository subCategoryRepository,
                           ProductRepository productRepository,
                           PriceHistoryRepository priceHistoryRepository,
                           OfficeRepository officeRepository,
                           StockRepository stockRepository,
                           SupplierRepository supplierRepository,
                           SupplierProductRepository supplierProductRepository,
                           UserRepository userRepository,
                           ClientRepository clientRepository,
                           EmployeeRepository employeeRepository,
                           PurchaseOrderRepository purchaseOrderRepository,
                           SaleOrderRepository saleOrderRepository,
                           OrderDetailRepository orderDetailRepository,
                           PaymentRepository paymentRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.countryRepository = countryRepository;
        this.stateRepository = stateRepository;
        this.cityRepository = cityRepository;
        this.categoryRepository = categoryRepository;
        this.subCategoryRepository = subCategoryRepository;
        this.productRepository = productRepository;
        this.priceHistoryRepository = priceHistoryRepository;
        this.officeRepository = officeRepository;
        this.stockRepository = stockRepository;
        this.supplierRepository = supplierRepository;
        this.supplierProductRepository = supplierProductRepository;
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.employeeRepository = employeeRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.saleOrderRepository = saleOrderRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.paymentRepository = paymentRepository;
    }

    private List<Contact> createContacts(Contact... contacts) {
        return new ArrayList<>(Arrays.asList(contacts));
    }

    private List<Address> createAddresses(Address... addresses) {
        return new ArrayList<>(Arrays.asList(addresses));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run(ApplicationArguments args) {
        boolean forceReset = args.containsOption("clean") || Boolean.getBoolean("app.data.clean");
        boolean alreadyHasNewSeedData = userRepository.findByUsernameIgnoreCaseAndDeletedFalse("marcos.deposito@zeroshop.com").isPresent()
                && userRepository.findByUsernameIgnoreCaseAndDeletedFalse("agustin.rodriguez@gmail.com").isPresent()
                && purchaseOrderRepository.count() > 0
                && saleOrderRepository.count() > 0;

        if (forceReset || !alreadyHasNewSeedData || userRepository.count() == 0) {
            log.info("Iniciando limpieza y repoblación integral de datos de Zero Shop...");
            cleanAllData();
            applyDatabaseMigrations();

            // 1. Ubicaciones
            Map<String, City> cityMap = seedLocations();

            // 2. Categorías y Subcategorías
            Map<String, SubCategory> subCategoryMap = seedCategoriesAndSubCategories();

            // 3. Sucursales (Offices)
            List<Office> offices = seedOffices(cityMap);

            // 4. Proveedores (Suppliers)
            List<Supplier> suppliers = seedSuppliers(cityMap);

            // 5. Productos, Precios de Costo y Stock
            List<Product> products = seedProductsAndStock(subCategoryMap, suppliers, offices);

            // 6. Usuarios: 1 Admin, 3 Empleados, 3 Clientes con múltiples direcciones y contactos
            Map<String, Object> usersMap = seedUsers(offices, cityMap);

            // 7. Órdenes de Compra (a Proveedores)
            seedPurchaseOrders(suppliers, offices, (List<Employee>) usersMap.get("employees"), products);

            // 8. Órdenes de Venta (a Clientes) con Detalles y Pagos
            seedSaleOrders((List<Client>) usersMap.get("clients"), (List<Employee>) usersMap.get("employees"), offices, products);

            log.info("Base de datos poblada exitosamente con datos reales para toda la plataforma.");
        } else {
            log.info("La base de datos ya contiene datos registrados. Omitiendo repoblación automática.");
        }
    }

    private void cleanAllData() {
        log.info("Vaciando tablas existentes...");
        try {
            List<String> tables = jdbcTemplate.queryForList(
                "SELECT tablename FROM pg_tables WHERE schemaname = 'public' AND tablename NOT LIKE 'flyway%'",
                String.class
            );
            if (!tables.isEmpty()) {
                jdbcTemplate.execute("TRUNCATE TABLE " + String.join(", ", tables) + " RESTART IDENTITY CASCADE");
                log.info("Se truncaron {} tablas correctamente.", tables.size());
            }
        } catch (Exception e) {
            log.error("Error al truncar tablas: {}", e.getMessage());
        }
    }

    private void applyDatabaseMigrations() {
        String[] migrations = {
            "ALTER TABLE orders DROP CONSTRAINT IF EXISTS orders_status_check",
            "ALTER TABLE sale_orders ALTER COLUMN office_id DROP NOT NULL",
            "ALTER TABLE person_addresses ALTER COLUMN id SET DEFAULT gen_random_uuid()",
            "ALTER TABLE person_addresses ALTER COLUMN deleted SET DEFAULT false",
            "ALTER TABLE person_contacts ALTER COLUMN id SET DEFAULT gen_random_uuid()",
            "ALTER TABLE person_contacts ALTER COLUMN deleted SET DEFAULT false",
            "ALTER TABLE supplier_addresses ALTER COLUMN id SET DEFAULT gen_random_uuid()",
            "ALTER TABLE supplier_addresses ALTER COLUMN deleted SET DEFAULT false",
            "ALTER TABLE supplier_contacts ALTER COLUMN id SET DEFAULT gen_random_uuid()",
            "ALTER TABLE supplier_contacts ALTER COLUMN deleted SET DEFAULT false",
            "ALTER TABLE office_addresses ALTER COLUMN id SET DEFAULT gen_random_uuid()",
            "ALTER TABLE office_addresses ALTER COLUMN deleted SET DEFAULT false",
            "ALTER TABLE office_contacts ALTER COLUMN id SET DEFAULT gen_random_uuid()",
            "ALTER TABLE office_contacts ALTER COLUMN deleted SET DEFAULT false"
        };
        for (String sql : migrations) {
            try {
                jdbcTemplate.execute(sql);
            } catch (Exception e) {
                log.debug("Aviso de migración DDL: {}", e.getMessage());
            }
        }

        try {
            jdbcTemplate.execute(
                "DO $$ DECLARE r RECORD; BEGIN " +
                "FOR r IN (SELECT conname FROM pg_constraint WHERE conrelid = 'office_employees'::regclass AND contype = 'u') " +
                "LOOP EXECUTE 'ALTER TABLE office_employees DROP CONSTRAINT ' || quote_ident(r.conname); END LOOP; " +
                "END $$;"
            );
            log.info("Restricción única de office_employees eliminada exitosamente.");
        } catch (Exception e) {
            log.debug("Aviso al remover constraints de office_employees: {}", e.getMessage());
        }
    }

    private Map<String, City> seedLocations() {
        log.info("Cargando ubicaciones desde locations.json...");
        Map<String, City> cityMap = new HashMap<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream inputStream = new ClassPathResource("data/locations.json").getInputStream();
            List<CountryJSON> countriesJSON = mapper.readValue(inputStream, new TypeReference<List<CountryJSON>>() {});

            for (CountryJSON cJson : countriesJSON) {
                Country country = new Country();
                country.setName(cJson.getName());
                country.setCode(cJson.getCode());
                country = countryRepository.save(country);

                if (cJson.getStates() != null) {
                    for (StateJSON sJson : cJson.getStates()) {
                        State state = new State();
                        state.setName(sJson.getName());
                        state.setCode(sJson.getCode());
                        state.setCountry(country);
                        state = stateRepository.save(state);

                        if (sJson.getCities() != null) {
                            for (CityJSON cityJson : sJson.getCities()) {
                                City city = new City();
                                city.setName(cityJson.getName());
                                city.setCode(cityJson.getCode());
                                city.setState(state);
                                city = cityRepository.save(city);
                                cityMap.putIfAbsent(city.getName().toLowerCase(), city);
                            }
                        }
                    }
                }
            }
            log.info("Ubicaciones cargadas con éxito ({} ciudades indexadas).", cityMap.size());
        } catch (Exception e) {
            log.error("Error al cargar locations.json: {}", e.getMessage());
        }
        return cityMap;
    }

    private City findCity(Map<String, City> cityMap, String name) {
        City city = cityMap.get(name.toLowerCase());
        if (city == null) {
            city = cityRepository.findAll().stream().findFirst().orElse(null);
        }
        return city;
    }

    private Map<String, SubCategory> seedCategoriesAndSubCategories() {
        log.info("Creando categorías y subcategorías...");
        Map<String, SubCategory> subCategoryMap = new HashMap<>();
        List<String> categoryNames = List.of("Hombres", "Mujeres", "Niños", "Niñas", "Unisex");
        List<String> subCategoryNames = List.of("Calzado", "Ropa", "Indumentaria", "Accesorios");

        for (String catName : categoryNames) {
            Category category = categoryRepository.save(
                Category.builder()
                    .name(catName)
                    .deleted(false)
                    .build()
            );

            for (String subName : subCategoryNames) {
                SubCategory subCategory = subCategoryRepository.save(
                    SubCategory.builder()
                        .name(subName)
                        .category(category)
                        .deleted(false)
                        .build()
                );
                subCategoryMap.put(catName + "_" + subName, subCategory);
            }
        }
        return subCategoryMap;
    }

    private List<Office> seedOffices(Map<String, City> cityMap) {
        log.info("Creando sucursales con múltiples direcciones y contactos...");
        City mendozaCity = findCity(cityMap, "Mendoza");
        City godoyCruzCity = findCity(cityMap, "Godoy Cruz");
        City lujanCity = findCity(cityMap, "Luján de Cuyo");
        City guaymallenCity = findCity(cityMap, "Guaymallén");

        // 1. Casa Central Mendoza
        Office central = new Office();
        central.setName("Casa Central Mendoza (Av. San Martín)");
        central.setCuit("30-71234567-8");
        central.setType(OfficeType.HEADQUARTERS);
        central.setDeleted(false);

        central.setAddress(createAddresses(
            Address.builder()
                .street("Av. San Martín")
                .number("1250")
                .floor("PB")
                .apartment("Local 1")
                .zipCode("5500")
                .observations("Sede central y salón de ventas al público")
                .city(mendozaCity)
                .deleted(false)
                .build(),
            Address.builder()
                .street("Ruta Provincial 50")
                .number("Km 10")
                .zipCode("5507")
                .observations("Centro logístico y depósito general")
                .city(lujanCity)
                .deleted(false)
                .build()
        ));

        central.setContact(createContacts(
            ContactPhone.builder()
                .phoneNumber("+54 261 420-0000")
                .phoneType(PhoneType.LANDLINE)
                .contactType(ContactType.WORK)
                .observation("Conmutador central")
                .deleted(false)
                .build(),
            ContactEmail.builder()
                .email("contacto@zeroshop.com")
                .contactType(ContactType.BUSINESS)
                .observation("Atención al cliente y consultas")
                .deleted(false)
                .build(),
            ContactEmail.builder()
                .email("administracion@zeroshop.com")
                .contactType(ContactType.WORK)
                .observation("Administración y pagos corporativos")
                .deleted(false)
                .build()
        ));
        central = officeRepository.save(central);

        // 2. Shopping Palmares
        Office palmares = new Office();
        palmares.setName("Sucursal Shopping Palmares (Godoy Cruz)");
        palmares.setCuit("30-71234567-9");
        palmares.setType(OfficeType.BRANCH);
        palmares.setDeleted(false);

        palmares.setAddress(createAddresses(
            Address.builder()
                .street("Av. San Martín Sur")
                .number("2650")
                .floor("1")
                .apartment("Local 140")
                .zipCode("5501")
                .observations("Shopping Palmares Nivel 1 frente a patio de comidas")
                .city(godoyCruzCity)
                .deleted(false)
                .build()
        ));

        palmares.setContact(createContacts(
            ContactPhone.builder()
                .phoneNumber("+54 261 413-9000")
                .phoneType(PhoneType.LANDLINE)
                .contactType(ContactType.WORK)
                .observation("Línea directa local Palmares")
                .deleted(false)
                .build(),
            ContactEmail.builder()
                .email("palmares@zeroshop.com")
                .contactType(ContactType.BUSINESS)
                .observation("Consultas de stock sucursal")
                .deleted(false)
                .build()
        ));
        palmares = officeRepository.save(palmares);

        // 3. Mendoza Plaza Shopping
        Office plaza = new Office();
        plaza.setName("Sucursal Mendoza Plaza Shopping (Guaymallén)");
        plaza.setCuit("30-71234567-0");
        plaza.setType(OfficeType.BRANCH);
        plaza.setDeleted(false);

        plaza.setAddress(createAddresses(
            Address.builder()
                .street("Acceso Este")
                .number("3280")
                .floor("PB")
                .apartment("Local 55")
                .zipCode("5519")
                .observations("Ala Oeste acceso principal")
                .city(guaymallenCity)
                .deleted(false)
                .build()
        ));

        plaza.setContact(createContacts(
            ContactPhone.builder()
                .phoneNumber("+54 261 449-0100")
                .phoneType(PhoneType.LANDLINE)
                .contactType(ContactType.WORK)
                .observation("Atención al público y mostrador POS")
                .deleted(false)
                .build(),
            ContactEmail.builder()
                .email("plazashopping@zeroshop.com")
                .contactType(ContactType.BUSINESS)
                .observation("Mostrador y entregas web")
                .deleted(false)
                .build()
        ));
        plaza = officeRepository.save(plaza);

        return List.of(central, palmares, plaza);
    }

    private List<Supplier> seedSuppliers(Map<String, City> cityMap) {
        log.info("Creando proveedores con múltiples direcciones y contactos...");
        City mendozaCity = findCity(cityMap, "Mendoza");
        City laPlataCity = findCity(cityMap, "La Plata");

        // 1. Distribuidora Textil Cuyana S.A.
        Supplier sup1 = new Supplier();
        sup1.setName("Distribuidora Textil Cuyana S.A.");
        sup1.setCuit("30-70891234-5");
        sup1.setDeleted(false);

        sup1.setAddress(createAddresses(
            Address.builder()
                .street("Parque Industrial Las Heras")
                .number("Calle 4 Galpón B")
                .zipCode("5539")
                .observations("Planta de confección y despacho mayorista")
                .city(mendozaCity)
                .deleted(false)
                .build(),
            Address.builder()
                .street("Av. Mitre")
                .number("540")
                .floor("2")
                .apartment("Of. 201")
                .zipCode("5500")
                .observations("Oficina administrativa y pagos")
                .city(mendozaCity)
                .deleted(false)
                .build()
        ));

        sup1.setContact(createContacts(
            ContactPhone.builder()
                .phoneNumber("+54 261 498-1122")
                .phoneType(PhoneType.LANDLINE)
                .contactType(ContactType.WORK)
                .observation("Ventas mayoristas indumentaria")
                .deleted(false)
                .build(),
            ContactEmail.builder()
                .email("ventas@textilcuyana.com")
                .contactType(ContactType.BUSINESS)
                .observation("Recepción de órdenes de compra")
                .deleted(false)
                .build()
        ));
        sup1 = supplierRepository.save(sup1);

        // 2. Calzados y Deportes del Plata S.R.L.
        Supplier sup2 = new Supplier();
        sup2.setName("Calzados y Deportes del Plata S.R.L.");
        sup2.setCuit("30-65432198-1");
        sup2.setDeleted(false);

        sup2.setAddress(createAddresses(
            Address.builder()
                .street("Av. del Libertador")
                .number("4800")
                .floor("6")
                .apartment("A")
                .zipCode("1426")
                .observations("Showroom y centro de distribución de calzado deportivo")
                .city(laPlataCity)
                .deleted(false)
                .build()
        ));

        sup2.setContact(createContacts(
            ContactPhone.builder()
                .phoneNumber("+54 11 4780-5500")
                .phoneType(PhoneType.LANDLINE)
                .contactType(ContactType.WORK)
                .observation("Mesa corporativa de atención")
                .deleted(false)
                .build(),
            ContactEmail.builder()
                .email("pedidos@calzadosdelplata.com.ar")
                .contactType(ContactType.BUSINESS)
                .observation("Ventas calzado y accesorios deportivos")
                .deleted(false)
                .build()
        ));
        sup2 = supplierRepository.save(sup2);

        // 3. Accesorios Urbanos y Moda S.A.
        Supplier sup3 = new Supplier();
        sup3.setName("Accesorios Urbanos y Moda S.A.");
        sup3.setCuit("30-71829304-3");
        sup3.setDeleted(false);

        sup3.setAddress(createAddresses(
            Address.builder()
                .street("Calle Defensa")
                .number("1020")
                .floor("PB")
                .apartment(null)
                .zipCode("1065")
                .observations("Depósito y taller central San Telmo")
                .city(laPlataCity)
                .deleted(false)
                .build()
        ));

        sup3.setContact(createContacts(
            ContactPhone.builder()
                .phoneNumber("+54 11 4361-9090")
                .phoneType(PhoneType.LANDLINE)
                .contactType(ContactType.WORK)
                .observation("Línea corporativa")
                .deleted(false)
                .build(),
            ContactEmail.builder()
                .email("info@accesoriosurbanos.com.ar")
                .contactType(ContactType.BUSINESS)
                .observation("Catálogos y presupuestos")
                .deleted(false)
                .build()
        ));
        sup3 = supplierRepository.save(sup3);

        return List.of(sup1, sup2, sup3);
    }

    private List<Product> seedProductsAndStock(Map<String, SubCategory> subCategoryMap,
                                               List<Supplier> suppliers,
                                               List<Office> offices) {
        log.info("Creando productos con imágenes, precios, promociones y stock...");

        Supplier supTextil = suppliers.get(0);
        Supplier supCalzado = suppliers.get(1);
        Supplier supAccesorios = suppliers.get(2);

        Office central = offices.get(0);
        Office palmares = offices.get(1);
        Office plaza = offices.get(2);

        record ProdDef(String code, String name, String description, Size size,
                       String imageUrl, String catSub, boolean onSale,
                       BigDecimal regularPrice, BigDecimal salePrice,
                       Supplier supplier, BigDecimal costPrice,
                       int stockCentral, int stockPalmares, int stockPlaza) {}

        List<ProdDef> defs = List.of(
            new ProdDef("RUN-NIKE-01", "Zapatillas Running Nike Air Zoom Pegasus",
                "Calzado running de alto rendimiento con amortiguación React y cápsula Zoom Air para máxima respuesta.",
                Size.M, "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&q=80",
                "Hombres_Calzado", true,
                new BigDecimal("145000.00"), new BigDecimal("115000.00"),
                supCalzado, new BigDecimal("62000.00"), 45, 25, 18),

            new ProdDef("ADI-FORUM-02", "Zapatillas Adidas Forum Low Streetwear",
                "Diseño retro de básquet reinventado para el streetstyle urbano con cuero prémium y suela de caucho vulcanizado.",
                Size.M, "https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?w=800&q=80",
                "Unisex_Calzado", false,
                new BigDecimal("129000.00"), null,
                supCalzado, new BigDecimal("70000.00"), 35, 20, 15),

            new ProdDef("PUM-CAR-03", "Zapatillas Urbanas Puma Carina Street",
                "Inspiradas en las playas de California de los 80, plantilla SoftFoam+ para confort prolongado todo el día.",
                Size.S, "https://images.unsplash.com/photo-1525966222134-fcfa99b8ae77?w=800&q=80",
                "Mujeres_Calzado", false,
                new BigDecimal("98000.00"), null,
                supCalzado, new BigDecimal("52000.00"), 40, 22, 12),

            new ProdDef("HOOD-FLEECE-04", "Buzo Hoodie Canguro Fleece Premium",
                "Buzo con capucha confeccionado en algodón frisado de alto gramaje con bolsillo frontal tipo canguro y rib elastizado.",
                Size.L, "https://images.unsplash.com/photo-1556905055-8f358a7a47b2?w=800&q=80",
                "Hombres_Indumentaria", true,
                new BigDecimal("85000.00"), new BigDecimal("68000.00"),
                supTextil, new BigDecimal("34000.00"), 50, 30, 20),

            new ProdDef("TSH-OVER-05", "Remera Oversize Algodón 24/1 White",
                "Corte moderno cuadrado oversize, tejido suave en jersey de algodón peinado 100% de máxima durabilidad.",
                Size.M, "https://images.unsplash.com/photo-1521572267360-ee0c2909d518?w=800&q=80",
                "Unisex_Indumentaria", false,
                new BigDecimal("32000.00"), null,
                supTextil, new BigDecimal("16000.00"), 60, 35, 25),

            new ProdDef("DEN-TRUCK-06", "Campera Denim Trucker Vintage Unisex",
                "Campera de jean rígido lavado medio con botones metálicos envejecidos y costuras en contraste.",
                Size.L, "https://images.unsplash.com/photo-1576995853123-5a10305d93c0?w=800&q=80",
                "Unisex_Indumentaria", true,
                new BigDecimal("120000.00"), new BigDecimal("96000.00"),
                supTextil, new BigDecimal("48000.00"), 25, 15, 8),

            new ProdDef("JOG-CARGO-07", "Pantalón Jogger Cargo Slim Fit",
                "Jogger elastizado con múltiples bolsillos funcionales en muslos y botamangas con puño elástico reforzado.",
                Size.M, "https://images.unsplash.com/photo-1624378439575-d8705ad7ae80?w=800&q=80",
                "Hombres_Indumentaria", false,
                new BigDecimal("54000.00"), null,
                supTextil, new BigDecimal("27000.00"), 35, 20, 15),

            new ProdDef("CREW-BEIGE-08", "Buzo Crewneck Beige Minimalist",
                "Cuello redondo clásico en algodón perchado, color neutro versátil de tacto ultra suave.",
                Size.S, "https://images.unsplash.com/photo-1578587018452-892bacefd3f2?w=800&q=80",
                "Mujeres_Indumentaria", false,
                new BigDecimal("59000.00"), null,
                supTextil, new BigDecimal("30000.00"), 30, 18, 10),

            new ProdDef("MOC-URB-09", "Mochila Urbana Porta Laptop Antirrobo 20L",
                "Compartimento acolchado para notebook de hasta 15.6'', cierre trasero oculto y tela cordura impermeable.",
                Size.M, "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=800&q=80",
                "Unisex_Accesorios", true,
                new BigDecimal("65000.00"), new BigDecimal("49000.00"),
                supAccesorios, new BigDecimal("24000.00"), 40, 25, 14),

            new ProdDef("CAP-STREET-10", "Gorra Trucker Streetwear Bordada Curve",
                "Gorra con frente estructurado de gabardina, malla trasera respirable y broche regulable snapback.",
                Size.M, "https://images.unsplash.com/photo-1588850561407-ed78c282e89b?w=800&q=80",
                "Unisex_Accesorios", false,
                new BigDecimal("22000.00"), null,
                supAccesorios, new BigDecimal("9500.00"), 50, 30, 20),

            new ProdDef("BAG-SPORT-11", "Bolso Deportivo Gym & Travel Impermeable",
                "Bolso espacioso con compartimento separado para zapatillas y correa acolchada desmontable.",
                Size.L, "https://images.unsplash.com/photo-1544816155-12df9643f363?w=800&q=80",
                "Unisex_Accesorios", false,
                new BigDecimal("42000.00"), null,
                supAccesorios, new BigDecimal("21000.00"), 20, 12, 4), // 4 en Plaza Shopping (Stock crítico para semáforo)

            new ProdDef("KID-SNEAK-12", "Zapatillas Deportivas Niños Velcro Flex",
                "Calzado liviano con ajuste por abrojo para mayor comodidad y suela de goma antideslizante con amortiguación EVA.",
                Size.S, "https://images.unsplash.com/photo-1503342217505-b0a15ec3261c?w=800&q=80",
                "Niños_Calzado", false,
                new BigDecimal("48000.00"), null,
                supCalzado, new BigDecimal("24000.00"), 25, 15, 8)
        );

        List<Product> savedProducts = new ArrayList<>();
        LocalDateTime pastMonth = LocalDateTime.now().minusDays(30);
        LocalDateTime pastWeek = LocalDateTime.now().minusDays(7);

        for (ProdDef def : defs) {
            SubCategory subCat = subCategoryMap.get(def.catSub());
            if (subCat == null) {
                subCat = subCategoryMap.values().stream().findFirst().orElse(null);
            }

            Product product = Product.builder()
                .code(def.code())
                .name(def.name())
                .description(def.description())
                .size(def.size())
                .imageUrl(def.imageUrl())
                .subCategory(subCat)
                .onSale(def.onSale())
                .deleted(false)
                .build();
            product = productRepository.save(product);
            savedProducts.add(product);

            // Precios e Historial de Precios
            if (def.onSale() && def.salePrice() != null) {
                // Precio anterior
                priceHistoryRepository.save(
                    PriceHistory.builder()
                        .product(product)
                        .price(def.regularPrice())
                        .startDate(pastMonth)
                        .endDate(pastWeek)
                        .deleted(false)
                        .build()
                );
                // Precio promocional actual
                priceHistoryRepository.save(
                    PriceHistory.builder()
                        .product(product)
                        .price(def.salePrice())
                        .startDate(pastWeek)
                        .endDate(null)
                        .deleted(false)
                        .build()
                );
            } else {
                // Precio estándar activo
                priceHistoryRepository.save(
                    PriceHistory.builder()
                        .product(product)
                        .price(def.regularPrice())
                        .startDate(pastMonth)
                        .endDate(null)
                        .deleted(false)
                        .build()
                );
            }

            // Vínculo con Proveedor y Costo
            supplierProductRepository.save(
                SupplierProduct.builder()
                    .product(product)
                    .supplier(def.supplier())
                    .costPrice(def.costPrice())
                    .deleted(false)
                    .build()
            );

            // Stock en las 3 sucursales
            stockRepository.save(
                Stock.builder()
                    .product(product)
                    .office(central)
                    .quantity(def.stockCentral())
                    .deleted(false)
                    .build()
            );
            stockRepository.save(
                Stock.builder()
                    .product(product)
                    .office(palmares)
                    .quantity(def.stockPalmares())
                    .deleted(false)
                    .build()
            );
            stockRepository.save(
                Stock.builder()
                    .product(product)
                    .office(plaza)
                    .quantity(def.stockPlaza())
                    .deleted(false)
                    .build()
            );
        }

        return savedProducts;
    }

    private Map<String, Object> seedUsers(List<Office> offices, Map<String, City> cityMap) {
        log.info("Creando 1 Administrador, 3 Empleados y 3 Clientes con direcciones y contactos...");
        City mendozaCity = findCity(cityMap, "Mendoza");
        City godoyCruzCity = findCity(cityMap, "Godoy Cruz");
        City guaymallenCity = findCity(cityMap, "Guaymallén");

        Office centralOffice = offices.get(0);
        Office palmaresOffice = offices.get(1);
        Office plazaOffice = offices.get(2);

        // ==========================================
        // 1. ADMIN USER
        // ==========================================
        Employee adminPerson = new Employee();
        adminPerson.setFirstName("Martín");
        adminPerson.setLastName("Administrador");
        adminPerson.setIdType(IDType.DNI);
        adminPerson.setIdNumber("28123456");
        adminPerson.setGender(Gender.MALE);
        adminPerson.setDateOfBirth(LocalDate.of(1982, 4, 10));
        adminPerson.setEmployeeType(EmployeeType.MANAGER);
        adminPerson.setHireDate(LocalDate.of(2020, 1, 15));
        adminPerson.setOffice(new ArrayList<>(List.of(centralOffice)));
        adminPerson.setDeleted(false);

        adminPerson.setAddress(createAddresses(
            Address.builder()
                .street("Av. Emilio Civit")
                .number("150")
                .floor("4")
                .apartment("A")
                .zipCode("5500")
                .observations("Domicilio particular administración")
                .city(mendozaCity)
                .deleted(false)
                .build()
        ));

        adminPerson.setContact(createContacts(
            ContactPhone.builder()
                .phoneNumber("+54 261 400-0001")
                .phoneType(PhoneType.LANDLINE)
                .contactType(ContactType.WORK)
                .observation("Línea corporativa")
                .deleted(false)
                .build(),
            ContactEmail.builder()
                .email("admin@zeroshop.com")
                .contactType(ContactType.WORK)
                .observation("Email de gestión gerencial")
                .deleted(false)
                .build()
        ));
        adminPerson = employeeRepository.save(adminPerson);

        User adminUser = new User();
        adminUser.setUsername("admin@gmail.com");
        adminUser.setPassword(passwordEncoder.encode("admin123"));
        adminUser.setRole(Role.ADMIN);
        adminUser.setPerson(adminPerson);
        adminUser.setDeleted(false);
        userRepository.save(adminUser);

        // ==========================================
        // 2. 3 EMPLEADOS
        // ==========================================
        // Empleado 1: Carlos Gómez (Vendedor en Casa Central) - usuario: employee@gmail.com
        Employee emp1 = new Employee();
        emp1.setFirstName("Carlos");
        emp1.setLastName("Gómez");
        emp1.setIdType(IDType.DNI);
        emp1.setIdNumber("32456789");
        emp1.setGender(Gender.MALE);
        emp1.setDateOfBirth(LocalDate.of(1990, 3, 15));
        emp1.setEmployeeType(EmployeeType.VENDOR);
        emp1.setHireDate(LocalDate.of(2021, 6, 1));
        emp1.setOffice(new ArrayList<>(List.of(centralOffice)));
        emp1.setDeleted(false);

        emp1.setAddress(createAddresses(
            Address.builder()
                .street("Calle Las Heras")
                .number("340")
                .floor("PB")
                .apartment("A")
                .zipCode("5500")
                .observations("Cerca de plaza Independencia")
                .city(mendozaCity)
                .deleted(false)
                .build()
        ));
        emp1.setContact(createContacts(
            ContactPhone.builder()
                .phoneNumber("+54 261 411-2233")
                .phoneType(PhoneType.MOBILE)
                .contactType(ContactType.PERSONAL)
                .observation("Celular personal")
                .deleted(false)
                .build(),
            ContactEmail.builder()
                .email("carlos.gomez@zeroshop.com")
                .contactType(ContactType.WORK)
                .observation("Correo corporativo ventas")
                .deleted(false)
                .build()
        ));
        emp1 = employeeRepository.save(emp1);

        User empUser1 = new User();
        empUser1.setUsername("employee@gmail.com");
        empUser1.setPassword(passwordEncoder.encode("employee123"));
        empUser1.setRole(Role.EMPLOYEE);
        empUser1.setPerson(emp1);
        empUser1.setDeleted(false);
        userRepository.save(empUser1);

        // Empleado 2: Lucía Fernández (Cajera en Palmares) - usuario: lucia.cajera@zeroshop.com
        Employee emp2 = new Employee();
        emp2.setFirstName("Lucía");
        emp2.setLastName("Fernández");
        emp2.setIdType(IDType.DNI);
        emp2.setIdNumber("35123987");
        emp2.setGender(Gender.FEMALE);
        emp2.setDateOfBirth(LocalDate.of(1994, 8, 22));
        emp2.setEmployeeType(EmployeeType.CASHIER);
        emp2.setHireDate(LocalDate.of(2022, 3, 15));
        emp2.setOffice(new ArrayList<>(List.of(palmaresOffice)));
        emp2.setDeleted(false);

        emp2.setAddress(createAddresses(
            Address.builder()
                .street("Av. San Martín Sur")
                .number("2550")
                .floor("2")
                .apartment("4B")
                .zipCode("5501")
                .observations("Frente a ciclovía")
                .city(godoyCruzCity)
                .deleted(false)
                .build()
        ));
        emp2.setContact(createContacts(
            ContactPhone.builder()
                .phoneNumber("+54 261 455-6677")
                .phoneType(PhoneType.MOBILE)
                .contactType(ContactType.PERSONAL)
                .observation("Celular personal de contacto")
                .deleted(false)
                .build(),
            ContactEmail.builder()
                .email("lucia.fernandez@zeroshop.com")
                .contactType(ContactType.WORK)
                .observation("Email laboral Palmares")
                .deleted(false)
                .build()
        ));
        emp2 = employeeRepository.save(emp2);

        User empUser2 = new User();
        empUser2.setUsername("lucia.cajera@zeroshop.com");
        empUser2.setPassword(passwordEncoder.encode("employee123"));
        empUser2.setRole(Role.EMPLOYEE);
        empUser2.setPerson(emp2);
        empUser2.setDeleted(false);
        userRepository.save(empUser2);

        // Empleado 3: Marcos Sosa (Encargado de Depósito y Stock en Plaza Shopping) - usuario: marcos.deposito@zeroshop.com
        Employee emp3 = new Employee();
        emp3.setFirstName("Marcos");
        emp3.setLastName("Sosa");
        emp3.setIdType(IDType.DNI);
        emp3.setIdNumber("29876543");
        emp3.setGender(Gender.MALE);
        emp3.setDateOfBirth(LocalDate.of(1988, 11, 5));
        emp3.setEmployeeType(EmployeeType.STOCKER);
        emp3.setHireDate(LocalDate.of(2021, 9, 10));
        emp3.setOffice(new ArrayList<>(List.of(plazaOffice)));
        emp3.setDeleted(false);

        emp3.setAddress(createAddresses(
            Address.builder()
                .street("Calle Belgrano")
                .number("820")
                .floor("PB")
                .apartment(null)
                .zipCode("5519")
                .observations("Casa con rejas blancas")
                .city(guaymallenCity)
                .deleted(false)
                .build()
        ));
        emp3.setContact(createContacts(
            ContactPhone.builder()
                .phoneNumber("+54 261 488-9900")
                .phoneType(PhoneType.MOBILE)
                .contactType(ContactType.PERSONAL)
                .observation("Celular guardia de stock")
                .deleted(false)
                .build(),
            ContactEmail.builder()
                .email("marcos.sosa@zeroshop.com")
                .contactType(ContactType.WORK)
                .observation("Logística y recepción")
                .deleted(false)
                .build()
        ));
        emp3 = employeeRepository.save(emp3);

        User empUser3 = new User();
        empUser3.setUsername("marcos.deposito@zeroshop.com");
        empUser3.setPassword(passwordEncoder.encode("employee123"));
        empUser3.setRole(Role.EMPLOYEE);
        empUser3.setPerson(emp3);
        empUser3.setDeleted(false);
        userRepository.save(empUser3);

        // ==========================================
        // 3. 3 CLIENTES
        // ==========================================
        // Cliente 1: Juan Pérez - client@gmail.com
        Client cli1 = new Client();
        cli1.setClientNumber("CLI-0001");
        cli1.setFirstName("Juan");
        cli1.setLastName("Pérez");
        cli1.setIdType(IDType.DNI);
        cli1.setIdNumber("40123456");
        cli1.setGender(Gender.MALE);
        cli1.setDateOfBirth(LocalDate.of(1995, 5, 20));
        cli1.setDeleted(false);

        cli1.setAddress(createAddresses(
            Address.builder()
                .street("Av. Emilio Civit")
                .number("450")
                .floor("PB")
                .apartment(null)
                .zipCode("5500")
                .observations("Casa particular frente al parque con timbre negro")
                .city(mendozaCity)
                .deleted(false)
                .build(),
            Address.builder()
                .street("Calle Colón")
                .number("120")
                .floor("3")
                .apartment("B")
                .zipCode("5500")
                .observations("Oficina laboral (recibir de 9:00 a 18:00 hs)")
                .city(mendozaCity)
                .deleted(false)
                .build()
        ));

        cli1.setContact(createContacts(
            ContactPhone.builder()
                .phoneNumber("+54 9 261 511-2233")
                .phoneType(PhoneType.MOBILE)
                .contactType(ContactType.PERSONAL)
                .observation("Celular personal con WhatsApp")
                .deleted(false)
                .build(),
            ContactPhone.builder()
                .phoneNumber("+54 261 425-9988")
                .phoneType(PhoneType.LANDLINE)
                .contactType(ContactType.PERSONAL)
                .observation("Teléfono fijo del hogar")
                .deleted(false)
                .build(),
            ContactEmail.builder()
                .email("juan.perez@gmail.com")
                .contactType(ContactType.PERSONAL)
                .observation("Email de compras y notificaciones")
                .deleted(false)
                .build()
        ));
        cli1 = clientRepository.save(cli1);

        User cliUser1 = new User();
        cliUser1.setUsername("client@gmail.com");
        cliUser1.setPassword(passwordEncoder.encode("client123"));
        cliUser1.setRole(Role.CLIENT);
        cliUser1.setPerson(cli1);
        cliUser1.setDeleted(false);
        userRepository.save(cliUser1);

        // Cliente 2: María González - maria.gonzalez@gmail.com
        Client cli2 = new Client();
        cli2.setClientNumber("CLI-0002");
        cli2.setFirstName("María");
        cli2.setLastName("González");
        cli2.setIdType(IDType.DNI);
        cli2.setIdNumber("38999888");
        cli2.setGender(Gender.FEMALE);
        cli2.setDateOfBirth(LocalDate.of(1993, 10, 12));
        cli2.setDeleted(false);

        cli2.setAddress(createAddresses(
            Address.builder()
                .street("Calle Sarmiento")
                .number("780")
                .floor("PB")
                .apartment(null)
                .zipCode("5519")
                .observations("Portón corredizo negro, dejar paquete con encargado")
                .city(guaymallenCity)
                .deleted(false)
                .build(),
            Address.builder()
                .street("Av. Champagnat")
                .number("1500")
                .floor("Mza C")
                .apartment("Casa 12")
                .zipCode("5500")
                .observations("Barrio Dalvian - Ingreso por guardia")
                .city(mendozaCity)
                .deleted(false)
                .build()
        ));

        cli2.setContact(createContacts(
            ContactPhone.builder()
                .phoneNumber("+54 9 261 633-4455")
                .phoneType(PhoneType.MOBILE)
                .contactType(ContactType.PERSONAL)
                .observation("Celular con WhatsApp")
                .deleted(false)
                .build(),
            ContactEmail.builder()
                .email("maria.gonzalez@gmail.com")
                .contactType(ContactType.PERSONAL)
                .observation("Email particular")
                .deleted(false)
                .build()
        ));
        cli2 = clientRepository.save(cli2);

        User cliUser2 = new User();
        cliUser2.setUsername("maria.gonzalez@gmail.com");
        cliUser2.setPassword(passwordEncoder.encode("client123"));
        cliUser2.setRole(Role.CLIENT);
        cliUser2.setPerson(cli2);
        cliUser2.setDeleted(false);
        userRepository.save(cliUser2);

        // Cliente 3: Agustín Rodríguez - agustin.rodriguez@gmail.com
        Client cli3 = new Client();
        cli3.setClientNumber("CLI-0003");
        cli3.setFirstName("Agustín");
        cli3.setLastName("Rodríguez");
        cli3.setIdType(IDType.DNI);
        cli3.setIdNumber("42111222");
        cli3.setGender(Gender.MALE);
        cli3.setDateOfBirth(LocalDate.of(1998, 2, 18));
        cli3.setDeleted(false);

        cli3.setAddress(createAddresses(
            Address.builder()
                .street("Calle Espejo")
                .number("330")
                .floor("1")
                .apartment("2")
                .zipCode("5500")
                .observations("Edificio centro, tocar timbre 1B")
                .city(mendozaCity)
                .deleted(false)
                .build()
        ));

        cli3.setContact(createContacts(
            ContactPhone.builder()
                .phoneNumber("+54 9 261 744-8899")
                .phoneType(PhoneType.MOBILE)
                .contactType(ContactType.PERSONAL)
                .observation("Celular personal")
                .deleted(false)
                .build(),
            ContactEmail.builder()
                .email("agustin.rodriguez@gmail.com")
                .contactType(ContactType.PERSONAL)
                .observation("Email de contacto")
                .deleted(false)
                .build()
        ));
        cli3 = clientRepository.save(cli3);

        User cliUser3 = new User();
        cliUser3.setUsername("agustin.rodriguez@gmail.com");
        cliUser3.setPassword(passwordEncoder.encode("client123"));
        cliUser3.setRole(Role.CLIENT);
        cliUser3.setPerson(cli3);
        cliUser3.setDeleted(false);
        userRepository.save(cliUser3);

        Map<String, Object> res = new HashMap<>();
        res.put("admin", adminPerson);
        res.put("employees", List.of(emp1, emp2, emp3));
        res.put("clients", List.of(cli1, cli2, cli3));
        return res;
    }

    private void seedPurchaseOrders(List<Supplier> suppliers,
                                    List<Office> offices,
                                    List<Employee> employees,
                                    List<Product> products) {
        log.info("Creando órdenes de compra a proveedores...");

        Supplier supTextil = suppliers.get(0);
        Supplier supCalzado = suppliers.get(1);
        Supplier supAccesorios = suppliers.get(2);

        Office centralOffice = offices.get(0);
        Office palmaresOffice = offices.get(1);
        Office plazaOffice = offices.get(2);

        Employee carlos = employees.get(0);
        Employee marcos = employees.get(2);

        // OC 1: Ropa de Textil Cuyana a Casa Central (Entregada)
        PurchaseOrder po1 = PurchaseOrder.builder()
            .supplier(supTextil)
            .office(centralOffice)
            .employee(carlos)
            .date(LocalDateTime.now().minusDays(18))
            .status(OrderStatus.DELIVERED)
            .totalAmount(new BigDecimal("3300000.00"))
            .deleted(false)
            .build();
        po1 = purchaseOrderRepository.save(po1);

        orderDetailRepository.save(OrderDetail.builder()
            .order(po1)
            .product(products.get(3)) // Buzo Hoodie
            .quantity(50)
            .unitPrice(new BigDecimal("34000.00"))
            .total(new BigDecimal("1700000.00"))
            .deleted(false)
            .build());

        orderDetailRepository.save(OrderDetail.builder()
            .order(po1)
            .product(products.get(4)) // Remera Oversize
            .quantity(100)
            .unitPrice(new BigDecimal("16000.00"))
            .total(new BigDecimal("1600000.00"))
            .deleted(false)
            .build());

        // OC 2: Calzados a Sucursal Palmares (Pagada)
        PurchaseOrder po2 = PurchaseOrder.builder()
            .supplier(supCalzado)
            .office(palmaresOffice)
            .employee(marcos)
            .date(LocalDateTime.now().minusDays(7))
            .status(OrderStatus.PAID)
            .totalAmount(new BigDecimal("3260000.00"))
            .deleted(false)
            .build();
        po2 = purchaseOrderRepository.save(po2);

        orderDetailRepository.save(OrderDetail.builder()
            .order(po2)
            .product(products.get(0)) // Nike Pegasus
            .quantity(30)
            .unitPrice(new BigDecimal("62000.00"))
            .total(new BigDecimal("1860000.00"))
            .deleted(false)
            .build());

        orderDetailRepository.save(OrderDetail.builder()
            .order(po2)
            .product(products.get(1)) // Adidas Forum
            .quantity(20)
            .unitPrice(new BigDecimal("70000.00"))
            .total(new BigDecimal("1400000.00"))
            .deleted(false)
            .build());

        // OC 3: Accesorios a Plaza Shopping (Pendiente de Entrega)
        PurchaseOrder po3 = PurchaseOrder.builder()
            .supplier(supAccesorios)
            .office(plazaOffice)
            .employee(carlos)
            .date(LocalDateTime.now().minusDays(2))
            .status(OrderStatus.PENDING_DELIVERY)
            .totalAmount(new BigDecimal("1075000.00"))
            .deleted(false)
            .build();
        po3 = purchaseOrderRepository.save(po3);

        orderDetailRepository.save(OrderDetail.builder()
            .order(po3)
            .product(products.get(8)) // Mochila Urbana
            .quantity(25)
            .unitPrice(new BigDecimal("24000.00"))
            .total(new BigDecimal("600000.00"))
            .deleted(false)
            .build());

        orderDetailRepository.save(OrderDetail.builder()
            .order(po3)
            .product(products.get(9)) // Gorra Streetwear
            .quantity(50)
            .unitPrice(new BigDecimal("9500.00"))
            .total(new BigDecimal("475000.00"))
            .deleted(false)
            .build());
    }

    private void seedSaleOrders(List<Client> clients,
                                List<Employee> employees,
                                List<Office> offices,
                                List<Product> products) {
        log.info("Creando órdenes de venta a clientes con detalles y pagos...");

        Client juan = clients.get(0);
        Client maria = clients.get(1);
        Client agustin = clients.get(2);

        Employee carlos = employees.get(0);
        Employee lucia = employees.get(1);

        Office central = offices.get(0);
        Office palmares = offices.get(1);
        Office plaza = offices.get(2);

        Address juanAddr1 = juan.getAddress().iterator().next();
        Address juanAddr2 = juan.getAddress().stream().skip(1).findFirst().orElse(juanAddr1);
        Address mariaAddr1 = maria.getAddress().iterator().next();
        Address agustinAddr1 = agustin.getAddress().iterator().next();

        // 1. Venta a Juan Pérez (Entregada, pagada con Tarjeta de Crédito)
        SaleOrder so1 = SaleOrder.builder()
            .client(juan)
            .employee(carlos)
            .office(central)
            .shippingAddress(juanAddr1)
            .date(LocalDateTime.now().minusDays(12))
            .status(OrderStatus.DELIVERED)
            .totalAmount(new BigDecimal("137000.00"))
            .deleted(false)
            .build();
        so1 = saleOrderRepository.save(so1);

        orderDetailRepository.save(OrderDetail.builder()
            .order(so1)
            .product(products.get(0)) // Nike Pegasus ($115.000)
            .quantity(1)
            .unitPrice(new BigDecimal("115000.00"))
            .total(new BigDecimal("115000.00"))
            .deleted(false)
            .build());

        orderDetailRepository.save(OrderDetail.builder()
            .order(so1)
            .product(products.get(9)) // Gorra Streetwear ($22.000)
            .quantity(1)
            .unitPrice(new BigDecimal("22000.00"))
            .total(new BigDecimal("22000.00"))
            .deleted(false)
            .build());

        paymentRepository.save(Payment.builder()
            .order(so1)
            .amount(new BigDecimal("137000.00"))
            .date(LocalDateTime.now().minusDays(12))
            .method(PaymentMethod.CREDIT)
            .deleted(false)
            .build());

        // 2. Venta a Juan Pérez (Pendiente de Envío, pagada con Mercado Pago)
        SaleOrder so2 = SaleOrder.builder()
            .client(juan)
            .employee(lucia)
            .office(palmares)
            .shippingAddress(juanAddr2)
            .date(LocalDateTime.now().minusDays(3))
            .status(OrderStatus.PENDING_SHIPPING)
            .totalAmount(new BigDecimal("68000.00"))
            .deleted(false)
            .build();
        so2 = saleOrderRepository.save(so2);

        orderDetailRepository.save(OrderDetail.builder()
            .order(so2)
            .product(products.get(3)) // Buzo Hoodie ($68.000)
            .quantity(1)
            .unitPrice(new BigDecimal("68000.00"))
            .total(new BigDecimal("68000.00"))
            .deleted(false)
            .build());

        paymentRepository.save(Payment.builder()
            .order(so2)
            .amount(new BigDecimal("68000.00"))
            .date(LocalDateTime.now().minusDays(3))
            .method(PaymentMethod.MERCADO_PAGO)
            .deleted(false)
            .build());

        // 3. Venta a María González (Entregada, pagada con Débito)
        SaleOrder so3 = SaleOrder.builder()
            .client(maria)
            .employee(carlos)
            .office(central)
            .shippingAddress(mariaAddr1)
            .date(LocalDateTime.now().minusDays(6))
            .status(OrderStatus.DELIVERED)
            .totalAmount(new BigDecimal("113000.00"))
            .deleted(false)
            .build();
        so3 = saleOrderRepository.save(so3);

        orderDetailRepository.save(OrderDetail.builder()
            .order(so3)
            .product(products.get(8)) // Mochila Urbana ($49.000)
            .quantity(1)
            .unitPrice(new BigDecimal("49000.00"))
            .total(new BigDecimal("49000.00"))
            .deleted(false)
            .build());

        orderDetailRepository.save(OrderDetail.builder()
            .order(so3)
            .product(products.get(4)) // 2 Remeras Oversize ($32.000 c/u)
            .quantity(2)
            .unitPrice(new BigDecimal("32000.00"))
            .total(new BigDecimal("64000.00"))
            .deleted(false)
            .build());

        paymentRepository.save(Payment.builder()
            .order(so3)
            .amount(new BigDecimal("113000.00"))
            .date(LocalDateTime.now().minusDays(6))
            .method(PaymentMethod.DEBIT)
            .deleted(false)
            .build());

        // 4. Venta a Agustín Rodríguez (Pendiente de Pago)
        SaleOrder so4 = SaleOrder.builder()
            .client(agustin)
            .employee(null)
            .office(plaza)
            .shippingAddress(agustinAddr1)
            .date(LocalDateTime.now().minusDays(1))
            .status(OrderStatus.PENDING_PAYMENT)
            .totalAmount(new BigDecimal("96000.00"))
            .deleted(false)
            .build();
        so4 = saleOrderRepository.save(so4);

        orderDetailRepository.save(OrderDetail.builder()
            .order(so4)
            .product(products.get(5)) // Campera Denim ($96.000)
            .quantity(1)
            .unitPrice(new BigDecimal("96000.00"))
            .total(new BigDecimal("96000.00"))
            .deleted(false)
            .build());

        // 5. Carrito activo para Juan Pérez
        SaleOrder soCart = SaleOrder.builder()
            .client(juan)
            .employee(null)
            .office(null)
            .shippingAddress(null)
            .date(LocalDateTime.now())
            .status(OrderStatus.ON_CART)
            .totalAmount(new BigDecimal("54000.00"))
            .deleted(false)
            .build();
        soCart = saleOrderRepository.save(soCart);

        orderDetailRepository.save(OrderDetail.builder()
            .order(soCart)
            .product(products.get(6)) // Jogger Cargo ($54.000)
            .quantity(1)
            .unitPrice(new BigDecimal("54000.00"))
            .total(new BigDecimal("54000.00"))
            .deleted(false)
            .build());
    }

    // --- DTOs internos para leer locations.json ---

    @Data
    public static class CountryJSON {
        private String name;
        private String code;
        private List<StateJSON> states;
    }

    @Data
    public static class StateJSON {
        private String name;
        private String code;
        private List<CityJSON> cities;
    }

    @Data
    public static class CityJSON {
        private String name;
        private String code;
    }

}
