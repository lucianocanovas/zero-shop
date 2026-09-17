# REPOSITORIOS DEL SISTEMA

## Reglas generales para el uso de repositorios

- **Persistencia y abstracción:** Los repositorios se encargan exclusivamente de la persistencia y acceso a la base de datos, extendiendo `JpaRepository<T, UUID>` de Spring Data JPA.
- **Búsqueda estándar unificada:**
  - Todo repositorio implementa el método `find(UUID id)` que busca un registro por su clave primaria, retornando un `Optional<T>`.
  - Todo repositorio con soporte de borrado lógico implementa el método `findActive(UUID id)` que busca un registro activo (`deleted = false`), retornando un `Optional<T>`.
  - Ambos métodos se implementan como métodos por defecto (`default`) en las interfaces, delegando respectivamente a `findById(id)` y `findByIdAndDeletedFalse(id)`, garantizando compatibilidad sin romper la inferencia de consultas de Spring Data JPA.
- **Respeto al borrado lógico:** Las consultas de recuperación de datos (`findAllByDeletedFalse`, `findByName...AndDeletedFalse`, etc.) deben filtrar siempre los registros donde `deleted = false`, preservando la integridad referencial y la trazabilidad histórica.
- **Relaciones y rendimiento:** Conforme a las directrices de arquitectura de [PATTERN.md](PATTERN.md), se evitan colecciones bidireccionales `OneToMany` y `ManyToMany` pesadas en las entidades. La navegación relacional se resuelve eficientemente mediante métodos de consulta por clave foránea (ej. `findByProductIdAndDeletedFalse(UUID productId)`).
- **Consultas de agregación:** Cuando se requieren sumas o cálculos analíticos (como el cálculo total de stock para el semáforo o dashboard), se utilizan consultas JPQL optimizadas mediante la anotación `@Query`.

---

## Organización de los repositorios por módulo

- **actor:**
  - `UserRepository`
  - `PersonRepository`
  - `ClientRepository`
  - `EmployeeRepository`
  - `SupplierRepository`
  - `SupplierProductRepository`
  - `ContactRepository`

- **catalog:**
  - `ProductRepository`
  - `CategoryRepository`
  - `SubCategoryRepository`
  - `PriceHistoryRepository`

- **location:**
  - `CountryRepository`
  - `StateRepository`
  - `CityRepository`
  - `AddressRepository`

- **media:**
  - `ImageRepository`
  - `ProductImageRepository`
  - `UserImageRepository`

- **org:**
  - `StockRepository`
  - `OfficeRepository`
  - `OrganizationRepository`

- **transaction:**
  - `OrderRepository`
  - `SaleOrderRepository`
  - `PurchaseOrderRepository`
  - `OrderDetailRepository`
  - `PaymentRepository`
  - `InvoiceRepository`
  - `InvoiceDetailRepository`

---

## Detalle de repositorios y métodos principales

### 1. Módulo actor (`ingsoftware.zeroshop.repository.actor`)

- **`UserRepository`**
  - `Optional<User> find(UUID id)` / `Optional<User> findActive(UUID id)`
  - `Optional<User> findByEmailIgnoreCase(String email)`
  - `Optional<User> findByEmailIgnoreCaseAndDeletedFalse(String email)`
  - `boolean existsByEmailIgnoreCase(String email)`
  - `boolean existsByEmailIgnoreCaseAndIdNot(String email, UUID id)`

- **`PersonRepository`**
  - `Optional<Person> find(UUID id)` / `Optional<Person> findActive(UUID id)`
  - `Optional<Person> findByIdTypeAndIdNumberAndDeletedFalse(IDType idType, String idNumber)`
  - `List<Person> findByFirstNameIgnoreCaseAndDeletedFalse(String firstName)`
  - `List<Person> findByLastNameIgnoreCaseAndDeletedFalse(String lastName)`

- **`ClientRepository`**
  - `Optional<Client> find(UUID id)` / `Optional<Client> findActive(UUID id)`
  - `List<Client> findAllByDeletedFalse()`

- **`EmployeeRepository`**
  - `Optional<Employee> find(UUID id)` / `Optional<Employee> findActive(UUID id)`
  - `List<Employee> findByEmployeeTypeAndDeletedFalse(EmployeeType employeeType)`

- **`SupplierRepository`**
  - `Optional<Supplier> find(UUID id)` / `Optional<Supplier> findActive(UUID id)`
  - `Optional<Supplier> findByNameIgnoreCaseAndDeletedFalse(String name)`
  - `boolean existsByNameIgnoreCase(String name)`

- **`SupplierProductRepository`**
  - `List<SupplierProduct> findByProductIdAndDeletedFalse(UUID productId)`
  - `List<SupplierProduct> findBySupplierIdAndDeletedFalse(UUID supplierId)`
  - `Optional<SupplierProduct> findBySupplierIdAndProductIdAndDeletedFalse(UUID supplierId, UUID productId)`
  - `List<SupplierProduct> findByProductIdAndDeletedFalseOrderByCostPriceAsc(UUID productId)` *(Determinación de proveedor más económico)*

- **`ContactRepository`**
  - `Optional<Contact> find(UUID id)` / `Optional<Contact> findActive(UUID id)`
  - `List<Contact> findAllByDeletedFalse()`

---

### 2. Módulo catalog (`ingsoftware.zeroshop.repository.catalog`)

- **`ProductRepository`**
  - `Optional<Product> find(UUID id)` / `Optional<Product> findActive(UUID id)`
  - `Optional<Product> findByNameAndDeletedFalse(String name)`
  - `Optional<Product> findByCodeAndDeletedFalse(String code)`
  - `List<Product> findBySizeAndDeletedFalse(Size size)`
  - `List<Product> findByOnSaleTrueAndDeletedFalse()` *(Catálogo de ofertas especiales)*
  - `List<Product> findBySubCategoryIdAndDeletedFalse(UUID subCategoryId)`
  - `List<Product> findBySubCategoryCategoryNameAndDeletedFalse(String categoryName)`

- **`CategoryRepository`**
  - `Optional<Category> find(UUID id)` / `Optional<Category> findActive(UUID id)`
  - `Optional<Category> findByNameIgnoreCase(String name)`
  - `List<Category> findAllByDeletedFalse()`

- **`SubCategoryRepository`**
  - `Optional<SubCategory> find(UUID id)` / `Optional<SubCategory> findActive(UUID id)`
  - `List<SubCategory> findByCategoryIdAndDeletedFalse(UUID categoryId)`

- **`PriceHistoryRepository`**
  - `Optional<PriceHistory> find(UUID id)` / `Optional<PriceHistory> findActive(UUID id)`
  - `Optional<PriceHistory> findFirstByProductIdAndDeletedFalseOrderByStartDateDesc(UUID productId)` *(Precio vigente)*
  - `List<PriceHistory> findByProductIdAndDeletedFalseOrderByStartDateDesc(UUID productId)`
  - `List<PriceHistory> findByStartDateBetweenAndProductIdAndDeletedFalse(...)`

---

### 3. Módulo location (`ingsoftware.zeroshop.repository.location`)

- **`CountryRepository`**
  - `Optional<Country> find(UUID id)` / `Optional<Country> findActive(UUID id)`
  - `Optional<Country> findByNameIgnoreCaseAndDeletedFalse(String name)`

- **`StateRepository`**
  - `Optional<State> find(UUID id)` / `Optional<State> findActive(UUID id)`
  - `List<State> findByCountryIdAndDeletedFalse(UUID countryId)`

- **`CityRepository`**
  - `Optional<City> find(UUID id)` / `Optional<City> findActive(UUID id)`
  - `List<City> findByStateIdAndDeletedFalse(UUID stateId)`

- **`AddressRepository`**
  - `Optional<Address> find(UUID id)` / `Optional<Address> findActive(UUID id)`
  - `List<Address> findByCityIdAndDeletedFalse(UUID cityId)`

---

### 4. Módulo media (`ingsoftware.zeroshop.repository.media`)

- **`ImageRepository`**
  - `Optional<Image> find(UUID id)` / `Optional<Image> findActive(UUID id)`
  - `List<Image> findAllByDeletedFalse()`

- **`ProductImageRepository`**
  - `List<ProductImage> findByProductIdAndDeletedFalse(UUID productId)`

- **`UserImageRepository`**
  - `Optional<UserImage> findByUserIdAndDeletedFalse(UUID userId)`

---

### 5. Módulo org (`ingsoftware.zeroshop.repository.org`)

- **`StockRepository`**
  - `Optional<Stock> find(UUID id)` / `Optional<Stock> findActive(UUID id)`
  - `Optional<Stock> findByProductIdAndOfficeIdAndDeletedFalse(UUID productId, UUID officeId)`
  - `List<Stock> findByProductIdAndDeletedFalse(UUID productId)`
  - `List<Stock> findByOfficeIdAndDeletedFalse(UUID officeId)`
  - `@Query`: `Integer getTotalQuantityByProductId(UUID productId)` *(Cálculo de semáforo y disponibilidad global)*

- **`OfficeRepository`**
  - `Optional<Office> find(UUID id)` / `Optional<Office> findActive(UUID id)`
  - `Optional<Office> findByCuitAndDeletedFalse(String cuit)`

- **`OrganizationRepository`**
  - `Optional<Organization> find(UUID id)` / `Optional<Organization> findActive(UUID id)`
  - `List<Organization> findAllByDeletedFalse()`

---

### 6. Módulo transaction (`ingsoftware.zeroshop.repository.transaction`)

- **`OrderRepository`**
  - `Optional<Order> find(UUID id)` / `Optional<Order> findActive(UUID id)`
  - `List<Order> findByStatusAndDeletedFalse(OrderStatus status)`
  - `List<Order> findByDateBetweenAndDeletedFalse(LocalDateTime start, LocalDateTime end)`

- **`SaleOrderRepository`**
  - `Optional<SaleOrder> find(UUID id)` / `Optional<SaleOrder> findActive(UUID id)`
  - `List<SaleOrder> findByClientIdAndDeletedFalseOrderByDateDesc(UUID clientId)`
  - `List<SaleOrder> findByStatusAndDeletedFalse(OrderStatus status)`
  - `List<SaleOrder> findByDateBetweenAndDeletedFalse(LocalDateTime start, LocalDateTime end)` *(Reporte de ventas)*

- **`PurchaseOrderRepository`**
  - `Optional<PurchaseOrder> find(UUID id)` / `Optional<PurchaseOrder> findActive(UUID id)`
  - `List<PurchaseOrder> findBySupplierIdAndDeletedFalse(UUID supplierId)`
  - `List<PurchaseOrder> findByStatusAndDeletedFalse(OrderStatus status)`

- **`OrderDetailRepository`**
  - `Optional<OrderDetail> find(UUID id)` / `Optional<OrderDetail> findActive(UUID id)`
  - `List<OrderDetail> findByOrderIdAndDeletedFalse(UUID orderId)`
  - `List<OrderDetail> findByProductIdAndDeletedFalse(UUID productId)`

- **`PaymentRepository`**
  - `Optional<Payment> find(UUID id)` / `Optional<Payment> findActive(UUID id)`
  - `List<Payment> findByOrderIdAndDeletedFalse(UUID orderId)`
  - `List<Payment> findByMethodAndDeletedFalse(PaymentMethod method)`

- **`InvoiceRepository`**
  - `Optional<Invoice> find(UUID id)` / `Optional<Invoice> findActive(UUID id)`
  - `Optional<Invoice> findByNumberAndDeletedFalse(String number)`
  - `Optional<Invoice> findByOrderIdAndDeletedFalse(UUID orderId)`

- **`InvoiceDetailRepository`**
  - `Optional<InvoiceDetail> find(UUID id)` / `Optional<InvoiceDetail> findActive(UUID id)`
  - `List<InvoiceDetail> findByInvoiceIdAndDeletedFalse(UUID invoiceId)`
