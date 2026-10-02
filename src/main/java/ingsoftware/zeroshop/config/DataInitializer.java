package ingsoftware.zeroshop.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ingsoftware.zeroshop.entity.actor.*;
import ingsoftware.zeroshop.entity.actor.Contact;
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

    private final ObjectMapper objectMapper = new ObjectMapper();
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
    private final InvoiceRepository invoiceRepository;
    private final InvoiceDetailRepository invoiceDetailRepository;

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
                           PaymentRepository paymentRepository,
                           InvoiceRepository invoiceRepository,
                           InvoiceDetailRepository invoiceDetailRepository) {
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
        this.invoiceRepository = invoiceRepository;
        this.invoiceDetailRepository = invoiceDetailRepository;
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
            log.info("Iniciando carga integral de datos desde archivos JSON (data/*.json)...");
            cleanAllData();
            applyDatabaseMigrations();

            // 1. Ubicaciones (locations.json)
            Map<String, City> cityMap = seedLocations();

            // 2. Categorías y Subcategorías (categories.json)
            Map<String, SubCategory> subCategoryMap = seedCategoriesAndSubCategories();

            // 3. Sucursales (offices.json)
            List<Office> offices = seedOffices(cityMap);

            // 4. Proveedores (suppliers.json)
            List<Supplier> suppliers = seedSuppliers(cityMap);

            // 5. Productos, Precios y Stock (products.json)
            List<Product> products = seedProductsAndStock(subCategoryMap, suppliers, offices);

            // 6. Usuarios: Admin, Empleados y Clientes (users.json)
            Map<String, Object> usersMap = seedUsers(offices, cityMap);

            // 7. Órdenes de Compra a Proveedores (purchase_orders.json)
            seedPurchaseOrders(suppliers, offices, (List<Employee>) usersMap.get("employees"), products);

            // 8. Órdenes de Venta a Clientes con Detalles, Pagos y Facturas (sale_orders.json)
            seedSaleOrders((List<Client>) usersMap.get("clients"), (List<Employee>) usersMap.get("employees"), offices, products);

            log.info("Base de datos poblada exitosamente a partir de los datos en data/*.json.");
        } else {
            log.info("La base de datos ya contiene datos registrados. Omitiendo repoblación automática.");
            updateExistingProductSeedImages();
        }
    }

    private void updateExistingProductSeedImages() {
        try {
            InputStream inputStream = new ClassPathResource("data/products.json").getInputStream();
            List<ProductSeedJSON> productsJSON = objectMapper.readValue(inputStream, new TypeReference<List<ProductSeedJSON>>() {});
            for (ProductSeedJSON pJson : productsJSON) {
                productRepository.findByCodeAndDeletedFalse(pJson.getCode()).ifPresent(p -> {
                    p.setName(pJson.getName());
                    p.setImageUrl(pJson.getImageUrl());
                    productRepository.save(p);
                });
            }
            log.info("Imágenes y nombres del catálogo inicial sincronizados con éxito desde products.json.");
        } catch (Exception e) {
            log.error("Error al sincronizar imágenes desde products.json: {}", e.getMessage());
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
            "ALTER TABLE users DROP CONSTRAINT IF EXISTS uk97ih1g5lcdf1s3fg7oo4e18jw",
            "ALTER TABLE users DROP CONSTRAINT IF EXISTS users_person_id_key",
            "ALTER TABLE sale_orders DROP CONSTRAINT IF EXISTS fk48r5kf23juh8b8ryu7x06qqli",
            "ALTER TABLE sale_orders DROP CONSTRAINT IF EXISTS fk_sale_orders_client",
            "ALTER TABLE sale_orders ADD CONSTRAINT fk_sale_orders_client FOREIGN KEY (client_id) REFERENCES persons(id)",
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

    // 1. Ubicaciones
    private Map<String, City> seedLocations() {
        log.info("Cargando ubicaciones desde data/locations.json...");
        Map<String, City> cityMap = new HashMap<>();
        try {
            InputStream inputStream = new ClassPathResource("data/locations.json").getInputStream();
            List<CountryJSON> countriesJSON = objectMapper.readValue(inputStream, new TypeReference<List<CountryJSON>>() {});

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
            log.error("Error al cargar locations.json: {}", e.getMessage(), e);
        }
        return cityMap;
    }

    private City findCity(Map<String, City> cityMap, String name) {
        if (name == null) return null;
        City city = cityMap.get(name.trim().toLowerCase());
        if (city == null) {
            city = cityRepository.findAll().stream().findFirst().orElse(null);
        }
        return city;
    }

    // 2. Categorías y Subcategorías
    private Map<String, SubCategory> seedCategoriesAndSubCategories() {
        log.info("Cargando categorías y subcategorías desde data/categories.json...");
        Map<String, SubCategory> subCategoryMap = new HashMap<>();
        try {
            InputStream is = new ClassPathResource("data/categories.json").getInputStream();
            List<CategorySeedJSON> list = objectMapper.readValue(is, new TypeReference<List<CategorySeedJSON>>() {});

            for (CategorySeedJSON cJson : list) {
                Category category = categoryRepository.save(
                    Category.builder()
                        .name(cJson.getName())
                        .deleted(false)
                        .build()
                );

                if (cJson.getSubcategories() != null) {
                    for (String subName : cJson.getSubcategories()) {
                        SubCategory subCategory = subCategoryRepository.save(
                            SubCategory.builder()
                                .name(subName)
                                .category(category)
                                .deleted(false)
                                .build()
                        );
                        subCategoryMap.put(cJson.getName() + "_" + subName, subCategory);
                    }
                }
            }
            log.info("Categorías y subcategorías cargadas con éxito ({} subcategorías indexadas).", subCategoryMap.size());
        } catch (Exception e) {
            log.error("Error al cargar categories.json: {}", e.getMessage(), e);
        }
        return subCategoryMap;
    }

    // 3. Sucursales (Offices)
    private List<Office> seedOffices(Map<String, City> cityMap) {
        log.info("Cargando sucursales desde data/offices.json...");
        List<Office> savedOffices = new ArrayList<>();
        try {
            InputStream is = new ClassPathResource("data/offices.json").getInputStream();
            List<OfficeSeedJSON> list = objectMapper.readValue(is, new TypeReference<List<OfficeSeedJSON>>() {});

            for (OfficeSeedJSON oJson : list) {
                Office office = new Office();
                office.setName(oJson.getName());
                office.setCuit(oJson.getCuit());
                office.setType(OfficeType.valueOf(oJson.getType()));
                office.setDeleted(false);

                if (oJson.getAddresses() != null) {
                    List<Address> addresses = oJson.getAddresses().stream()
                        .map(a -> buildAddress(a, cityMap))
                        .toList();
                    office.setAddress(new ArrayList<>(addresses));
                }
                if (oJson.getContacts() != null) {
                    List<Contact> contacts = oJson.getContacts().stream()
                        .map(this::buildContact)
                        .toList();
                    office.setContact(new ArrayList<>(contacts));
                }
                savedOffices.add(officeRepository.save(office));
            }
            log.info("Sucursales cargadas con éxito ({} sucursales).", savedOffices.size());
        } catch (Exception e) {
            log.error("Error al cargar offices.json: {}", e.getMessage(), e);
        }
        return savedOffices;
    }

    // 4. Proveedores (Suppliers)
    private List<Supplier> seedSuppliers(Map<String, City> cityMap) {
        log.info("Cargando proveedores desde data/suppliers.json...");
        List<Supplier> savedSuppliers = new ArrayList<>();
        try {
            InputStream is = new ClassPathResource("data/suppliers.json").getInputStream();
            List<SupplierSeedJSON> list = objectMapper.readValue(is, new TypeReference<List<SupplierSeedJSON>>() {});

            for (SupplierSeedJSON sJson : list) {
                Supplier supplier = new Supplier();
                supplier.setName(sJson.getName());
                supplier.setCuit(sJson.getCuit());
                supplier.setDeleted(false);

                if (sJson.getAddresses() != null) {
                    List<Address> addresses = sJson.getAddresses().stream()
                        .map(a -> buildAddress(a, cityMap))
                        .toList();
                    supplier.setAddress(new ArrayList<>(addresses));
                }
                if (sJson.getContacts() != null) {
                    List<Contact> contacts = sJson.getContacts().stream()
                        .map(this::buildContact)
                        .toList();
                    supplier.setContact(new ArrayList<>(contacts));
                }
                savedSuppliers.add(supplierRepository.save(supplier));
            }
            log.info("Proveedores cargados con éxito ({} proveedores).", savedSuppliers.size());
        } catch (Exception e) {
            log.error("Error al cargar suppliers.json: {}", e.getMessage(), e);
        }
        return savedSuppliers;
    }

    // 5. Productos, Precios y Stock
    private List<Product> seedProductsAndStock(Map<String, SubCategory> subCategoryMap,
                                               List<Supplier> suppliers,
                                               List<Office> offices) {
        log.info("Cargando productos, precios y stock desde data/products.json...");
        List<Product> savedProducts = new ArrayList<>();
        Map<String, Supplier> supplierByName = mapSuppliersByName(suppliers);
        Map<String, Office> officeByName = mapOfficesByName(offices);

        LocalDateTime pastMonth = LocalDateTime.now().minusDays(30);
        LocalDateTime pastWeek = LocalDateTime.now().minusDays(7);

        try {
            InputStream is = new ClassPathResource("data/products.json").getInputStream();
            List<ProductSeedJSON> list = objectMapper.readValue(is, new TypeReference<List<ProductSeedJSON>>() {});

            for (ProductSeedJSON pJson : list) {
                String catSubKey = pJson.getCategory() + "_" + pJson.getSubcategory();
                SubCategory subCat = subCategoryMap.get(catSubKey);
                if (subCat == null) {
                    subCat = subCategoryMap.values().stream().findFirst().orElse(null);
                }

                Product product = Product.builder()
                    .code(pJson.getCode())
                    .name(pJson.getName())
                    .description(pJson.getDescription())
                    .size(pJson.getSize() != null ? Size.valueOf(pJson.getSize()) : Size.M)
                    .imageUrl(pJson.getImageUrl())
                    .subCategory(subCat)
                    .onSale(Boolean.TRUE.equals(pJson.getOnSale()))
                    .deleted(false)
                    .build();
                product = productRepository.save(product);
                savedProducts.add(product);

                // Precios e Historial de Precios
                if (Boolean.TRUE.equals(pJson.getOnSale()) && pJson.getSalePrice() != null) {
                    // Precio anterior regular
                    priceHistoryRepository.save(PriceHistory.builder()
                        .product(product)
                        .price(pJson.getRegularPrice())
                        .startDate(pastMonth)
                        .endDate(pastWeek)
                        .deleted(false)
                        .build());
                    // Precio promocional actual
                    priceHistoryRepository.save(PriceHistory.builder()
                        .product(product)
                        .price(pJson.getSalePrice())
                        .startDate(pastWeek)
                        .endDate(null)
                        .deleted(false)
                        .build());
                } else {
                    // Precio estándar activo
                    priceHistoryRepository.save(PriceHistory.builder()
                        .product(product)
                        .price(pJson.getRegularPrice())
                        .startDate(pastMonth)
                        .endDate(null)
                        .deleted(false)
                        .build());
                }

                // Vínculo con Proveedor y Costo
                Supplier sup = supplierByName.get(pJson.getSupplierName());
                if (sup != null && pJson.getCostPrice() != null) {
                    supplierProductRepository.save(SupplierProduct.builder()
                        .product(product)
                        .supplier(sup)
                        .costPrice(pJson.getCostPrice())
                        .deleted(false)
                        .build());
                }

                // Stock por sucursal
                if (pJson.getStocks() != null) {
                    for (Map.Entry<String, Integer> entry : pJson.getStocks().entrySet()) {
                        Office office = officeByName.get(entry.getKey());
                        if (office != null) {
                            stockRepository.save(Stock.builder()
                                .product(product)
                                .office(office)
                                .quantity(entry.getValue())
                                .deleted(false)
                                .build());
                        }
                    }
                }
            }
            log.info("Productos y stock cargados con éxito ({} productos).", savedProducts.size());
        } catch (Exception e) {
            log.error("Error al cargar products.json: {}", e.getMessage(), e);
        }
        return savedProducts;
    }

    // 6. Usuarios: Admin, Empleados y Clientes
    private Map<String, Object> seedUsers(List<Office> offices, Map<String, City> cityMap) {
        log.info("Cargando usuarios desde data/users.json...");
        Map<String, Office> officeByName = mapOfficesByName(offices);

        Employee adminEmployee = null;
        List<Employee> employeeList = new ArrayList<>();
        List<Client> clientList = new ArrayList<>();

        try {
            InputStream is = new ClassPathResource("data/users.json").getInputStream();
            List<UserSeedJSON> list = objectMapper.readValue(is, new TypeReference<List<UserSeedJSON>>() {});

            for (UserSeedJSON uJson : list) {
                PersonSeedJSON pJson = uJson.getPerson();
                Person person = null;

                if ("CLIENT".equalsIgnoreCase(pJson.getType())) {
                    Client client = new Client();
                    client.setClientNumber(pJson.getClientNumber());
                    client.setFirstName(pJson.getFirstName());
                    client.setLastName(pJson.getLastName());
                    client.setIdType(pJson.getIdType() != null ? IDType.valueOf(pJson.getIdType()) : IDType.DNI);
                    client.setIdNumber(pJson.getIdNumber());
                    client.setGender(pJson.getGender() != null ? Gender.valueOf(pJson.getGender()) : Gender.OTHER);
                    client.setDateOfBirth(pJson.getDateOfBirth() != null ? LocalDate.parse(pJson.getDateOfBirth()) : null);
                    client.setDeleted(false);

                    if (pJson.getAddresses() != null) {
                        List<Address> addresses = pJson.getAddresses().stream()
                            .map(a -> buildAddress(a, cityMap))
                            .toList();
                        client.setAddress(new ArrayList<>(addresses));
                    }
                    if (pJson.getContacts() != null) {
                        List<Contact> contacts = pJson.getContacts().stream()
                            .map(this::buildContact)
                            .toList();
                        client.setContact(new ArrayList<>(contacts));
                    }
                    client = clientRepository.save(client);
                    clientList.add(client);
                    person = client;
                } else {
                    Employee employee = new Employee();
                    employee.setFirstName(pJson.getFirstName());
                    employee.setLastName(pJson.getLastName());
                    employee.setIdType(pJson.getIdType() != null ? IDType.valueOf(pJson.getIdType()) : IDType.DNI);
                    employee.setIdNumber(pJson.getIdNumber());
                    employee.setGender(pJson.getGender() != null ? Gender.valueOf(pJson.getGender()) : Gender.OTHER);
                    employee.setDateOfBirth(pJson.getDateOfBirth() != null ? LocalDate.parse(pJson.getDateOfBirth()) : null);
                    employee.setEmployeeType(pJson.getEmployeeType() != null ? EmployeeType.valueOf(pJson.getEmployeeType()) : EmployeeType.VENDOR);
                    employee.setHireDate(pJson.getHireDate() != null ? LocalDate.parse(pJson.getHireDate()) : LocalDate.now());
                    employee.setDeleted(false);

                    if (pJson.getOfficeNames() != null) {
                        List<Office> empOffices = pJson.getOfficeNames().stream()
                            .map(officeByName::get)
                            .filter(Objects::nonNull)
                            .toList();
                        employee.setOffice(new ArrayList<>(empOffices));
                    }
                    if (pJson.getAddresses() != null) {
                        List<Address> addresses = pJson.getAddresses().stream()
                            .map(a -> buildAddress(a, cityMap))
                            .toList();
                        employee.setAddress(new ArrayList<>(addresses));
                    }
                    if (pJson.getContacts() != null) {
                        List<Contact> contacts = pJson.getContacts().stream()
                            .map(this::buildContact)
                            .toList();
                        employee.setContact(new ArrayList<>(contacts));
                    }
                    employee = employeeRepository.save(employee);
                    if ("ADMIN".equalsIgnoreCase(uJson.getRole())) {
                        adminEmployee = employee;
                    } else {
                        employeeList.add(employee);
                    }
                    person = employee;
                }

                User user = new User();
                user.setUsername(uJson.getUsername());
                user.setPassword(passwordEncoder.encode(uJson.getPassword()));
                user.setRole(Role.valueOf(uJson.getRole()));
                user.setPerson(person);
                user.setDeleted(false);
                userRepository.save(user);
            }
            log.info("Usuarios cargados con éxito ({} empleados, {} clientes).", employeeList.size() + (adminEmployee != null ? 1 : 0), clientList.size());
        } catch (Exception e) {
            log.error("Error al cargar users.json: {}", e.getMessage(), e);
        }

        Map<String, Object> res = new HashMap<>();
        res.put("admin", adminEmployee);
        res.put("employees", employeeList);
        res.put("clients", clientList);
        return res;
    }

    // 7. Órdenes de Compra (a Proveedores)
    private void seedPurchaseOrders(List<Supplier> suppliers,
                                    List<Office> offices,
                                    List<Employee> employees,
                                    List<Product> products) {
        log.info("Cargando órdenes de compra desde data/purchase_orders.json...");
        Map<String, Supplier> supplierByName = mapSuppliersByName(suppliers);
        Map<String, Office> officeByName = mapOfficesByName(offices);
        Map<String, Product> productByCode = mapProductsByCode(products);

        try {
            InputStream is = new ClassPathResource("data/purchase_orders.json").getInputStream();
            List<PurchaseOrderSeedJSON> list = objectMapper.readValue(is, new TypeReference<List<PurchaseOrderSeedJSON>>() {});

            for (PurchaseOrderSeedJSON poJson : list) {
                Supplier sup = supplierByName.get(poJson.getSupplierName());
                Office off = officeByName.get(poJson.getOfficeName());
                Employee emp = userRepository.findByUsernameIgnoreCaseAndDeletedFalse(poJson.getEmployeeUsername())
                    .map(u -> (Employee) u.getPerson())
                    .orElse(employees.isEmpty() ? null : employees.get(0));

                PurchaseOrder po = PurchaseOrder.builder()
                    .supplier(sup)
                    .office(off)
                    .employee(emp)
                    .date(LocalDateTime.now().minusDays(poJson.getDaysAgo() != null ? poJson.getDaysAgo() : 0))
                    .status(OrderStatus.valueOf(poJson.getStatus()))
                    .totalAmount(poJson.getTotalAmount())
                    .deleted(false)
                    .build();
                po = purchaseOrderRepository.save(po);

                if (poJson.getDetails() != null) {
                    for (OrderDetailSeedJSON dJson : poJson.getDetails()) {
                        Product prod = productByCode.get(dJson.getProductCode());
                        orderDetailRepository.save(OrderDetail.builder()
                            .order(po)
                            .product(prod)
                            .quantity(dJson.getQuantity())
                            .unitPrice(dJson.getUnitPrice())
                            .total(dJson.getTotal())
                            .deleted(false)
                            .build());
                    }
                }
            }
            log.info("Órdenes de compra cargadas con éxito ({} órdenes).", list.size());
        } catch (Exception e) {
            log.error("Error al cargar purchase_orders.json: {}", e.getMessage(), e);
        }
    }

    // 8. Órdenes de Venta (a Clientes)
    private void seedSaleOrders(List<Client> clients,
                                List<Employee> employees,
                                List<Office> offices,
                                List<Product> products) {
        log.info("Cargando órdenes de venta desde data/sale_orders.json...");
        Map<String, Office> officeByName = mapOfficesByName(offices);
        Map<String, Product> productByCode = mapProductsByCode(products);

        try {
            InputStream is = new ClassPathResource("data/sale_orders.json").getInputStream();
            List<SaleOrderSeedJSON> list = objectMapper.readValue(is, new TypeReference<List<SaleOrderSeedJSON>>() {});

            for (SaleOrderSeedJSON soJson : list) {
                Client client = userRepository.findByUsernameIgnoreCaseAndDeletedFalse(soJson.getClientUsername())
                    .map(u -> (Client) u.getPerson())
                    .orElse(null);

                Employee employee = soJson.getEmployeeUsername() != null ?
                    userRepository.findByUsernameIgnoreCaseAndDeletedFalse(soJson.getEmployeeUsername())
                        .map(u -> (Employee) u.getPerson())
                        .orElse(null) : null;

                Office office = soJson.getOfficeName() != null ? officeByName.get(soJson.getOfficeName()) : null;

                Address shippingAddress = null;
                if (client != null && soJson.getShippingAddressStreet() != null && client.getAddress() != null) {
                    shippingAddress = client.getAddress().stream()
                        .filter(a -> a.getStreet().toLowerCase().contains(soJson.getShippingAddressStreet().toLowerCase()))
                        .findFirst()
                        .orElse(client.getAddress().isEmpty() ? null : client.getAddress().iterator().next());
                }

                LocalDateTime orderDate = LocalDateTime.now().minusDays(soJson.getDaysAgo() != null ? soJson.getDaysAgo() : 0);

                SaleOrder so = SaleOrder.builder()
                    .client(client)
                    .employee(employee)
                    .office(office)
                    .shippingAddress(shippingAddress)
                    .date(orderDate)
                    .status(OrderStatus.valueOf(soJson.getStatus()))
                    .paymentMethod(soJson.getPaymentMethod() != null ? PaymentMethod.valueOf(soJson.getPaymentMethod()) : null)
                    .totalAmount(soJson.getTotalAmount())
                    .deleted(false)
                    .build();
                so = saleOrderRepository.save(so);

                if (soJson.getDetails() != null) {
                    for (OrderDetailSeedJSON dJson : soJson.getDetails()) {
                        Product prod = productByCode.get(dJson.getProductCode());
                        orderDetailRepository.save(OrderDetail.builder()
                            .order(so)
                            .product(prod)
                            .quantity(dJson.getQuantity())
                            .unitPrice(dJson.getUnitPrice())
                            .total(dJson.getTotal())
                            .deleted(false)
                            .build());
                    }
                }

                if (soJson.getPayment() != null) {
                    paymentRepository.save(Payment.builder()
                        .order(so)
                        .amount(soJson.getPayment().getAmount())
                        .date(LocalDateTime.now().minusDays(soJson.getPayment().getDaysAgo() != null ? soJson.getPayment().getDaysAgo() : 0))
                        .method(PaymentMethod.valueOf(soJson.getPayment().getMethod()))
                        .deleted(false)
                        .build());
                }

                if (soJson.getInvoice() != null) {
                    InvoiceSeedJSON invJson = soJson.getInvoice();
                    Invoice inv = invoiceRepository.save(Invoice.builder()
                        .number(invJson.getNumber())
                        .date(LocalDateTime.now().minusDays(invJson.getDaysAgo() != null ? invJson.getDaysAgo() : 0))
                        .totalAmount(invJson.getTotalAmount())
                        .status(InvoiceStatus.valueOf(invJson.getStatus()))
                        .order(so)
                        .deleted(false)
                        .build());

                    if (invJson.getDetails() != null) {
                        for (OrderDetailSeedJSON dJson : invJson.getDetails()) {
                            Product prod = productByCode.get(dJson.getProductCode());
                            invoiceDetailRepository.save(InvoiceDetail.builder()
                                .invoice(inv)
                                .product(prod)
                                .quantity(dJson.getQuantity())
                                .unitPrice(dJson.getUnitPrice())
                                .total(dJson.getTotal())
                                .deleted(false)
                                .build());
                        }
                    }
                }
            }
            log.info("Órdenes de venta, pagos y facturas cargados con éxito ({} órdenes).", list.size());
        } catch (Exception e) {
            log.error("Error al cargar sale_orders.json: {}", e.getMessage(), e);
        }
    }

    // --- Helpers de Construcción y Mapeo de Entidades ---

    private Map<String, Office> mapOfficesByName(List<Office> offices) {
        Map<String, Office> map = new HashMap<>();
        for (Office o : offices) {
            if (o.getName() != null) {
                map.putIfAbsent(o.getName(), o);
            }
        }
        return map;
    }

    private Map<String, Supplier> mapSuppliersByName(List<Supplier> suppliers) {
        Map<String, Supplier> map = new HashMap<>();
        for (Supplier s : suppliers) {
            if (s.getName() != null) {
                map.putIfAbsent(s.getName(), s);
            }
        }
        return map;
    }

    private Map<String, Product> mapProductsByCode(List<Product> products) {
        Map<String, Product> map = new HashMap<>();
        for (Product p : products) {
            if (p.getCode() != null) {
                map.putIfAbsent(p.getCode(), p);
            }
        }
        return map;
    }

    private Address buildAddress(AddressSeedJSON aJson, Map<String, City> cityMap) {
        City city = findCity(cityMap, aJson.getCityName());
        return Address.builder()
            .street(aJson.getStreet())
            .number(aJson.getNumber())
            .floor(aJson.getFloor())
            .apartment(aJson.getApartment())
            .zipCode(aJson.getZipCode())
            .observations(aJson.getObservations())
            .city(city)
            .deleted(false)
            .build();
    }

    private Contact buildContact(ContactSeedJSON cJson) {
        if ("EMAIL".equalsIgnoreCase(cJson.getType())) {
            return ContactEmail.builder()
                .email(cJson.getEmail())
                .contactType(cJson.getContactType() != null ? ContactType.valueOf(cJson.getContactType()) : ContactType.WORK)
                .observation(cJson.getObservation())
                .deleted(false)
                .build();
        } else {
            return ContactPhone.builder()
                .phoneNumber(cJson.getPhoneNumber())
                .phoneType(cJson.getPhoneType() != null ? PhoneType.valueOf(cJson.getPhoneType()) : PhoneType.MOBILE)
                .contactType(cJson.getContactType() != null ? ContactType.valueOf(cJson.getContactType()) : ContactType.PERSONAL)
                .observation(cJson.getObservation())
                .deleted(false)
                .build();
        }
    }

    // --- DTOs internos para mapear los archivos JSON de inicialización ---

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

    @Data
    public static class CategorySeedJSON {
        private String name;
        private List<String> subcategories;
    }

    @Data
    public static class OfficeSeedJSON {
        private String name;
        private String cuit;
        private String type;
        private List<AddressSeedJSON> addresses;
        private List<ContactSeedJSON> contacts;
    }

    @Data
    public static class SupplierSeedJSON {
        private String name;
        private String cuit;
        private List<AddressSeedJSON> addresses;
        private List<ContactSeedJSON> contacts;
    }

    @Data
    public static class AddressSeedJSON {
        private String street;
        private String number;
        private String floor;
        private String apartment;
        private String zipCode;
        private String observations;
        private String cityName;
    }

    @Data
    public static class ContactSeedJSON {
        private String type; // "PHONE" o "EMAIL"
        private String phoneNumber;
        private String phoneType;
        private String email;
        private String contactType;
        private String observation;
    }

    @Data
    public static class ProductSeedJSON {
        private String code;
        private String name;
        private String description;
        private String size;
        private String imageUrl;
        private String category;
        private String subcategory;
        private Boolean onSale;
        private BigDecimal regularPrice;
        private BigDecimal salePrice;
        private String supplierName;
        private BigDecimal costPrice;
        private Map<String, Integer> stocks;
    }

    @Data
    public static class UserSeedJSON {
        private String username;
        private String password;
        private String role;
        private PersonSeedJSON person;
    }

    @Data
    public static class PersonSeedJSON {
        private String type; // "EMPLOYEE" o "CLIENT"
        private String clientNumber;
        private String firstName;
        private String lastName;
        private String idType;
        private String idNumber;
        private String gender;
        private String dateOfBirth;
        private String employeeType;
        private String hireDate;
        private List<String> officeNames;
        private List<AddressSeedJSON> addresses;
        private List<ContactSeedJSON> contacts;
    }

    @Data
    public static class PurchaseOrderSeedJSON {
        private String supplierName;
        private String officeName;
        private String employeeUsername;
        private Integer daysAgo;
        private String status;
        private BigDecimal totalAmount;
        private List<OrderDetailSeedJSON> details;
    }

    @Data
    public static class SaleOrderSeedJSON {
        private String clientUsername;
        private String employeeUsername;
        private String officeName;
        private String shippingAddressStreet;
        private Integer daysAgo;
        private String status;
        private String paymentMethod;
        private BigDecimal totalAmount;
        private List<OrderDetailSeedJSON> details;
        private PaymentSeedJSON payment;
        private InvoiceSeedJSON invoice;
    }

    @Data
    public static class OrderDetailSeedJSON {
        private String productCode;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal total;
    }

    @Data
    public static class PaymentSeedJSON {
        private BigDecimal amount;
        private Integer daysAgo;
        private String method;
    }

    @Data
    public static class InvoiceSeedJSON {
        private String number;
        private Integer daysAgo;
        private BigDecimal totalAmount;
        private String status;
        private List<OrderDetailSeedJSON> details;
    }

}
