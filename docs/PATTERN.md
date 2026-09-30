# PATRONES DE SOFTWARE EN ZERO SHOP

El sistema informático E-Commerce **Zero Shop Mendoza** ha sido diseñado e implementado aplicando rigurosamente los patrones arquitectónicos, de diseño GoF (*Gang of Four*) y patrones de asignación de responsabilidades GRASP (*General Responsibility Assignment Software Patterns*) solicitados en la cátedra de **Ingeniería del Software II (UNCUYO 2026)**.

A continuación se detalla la justificación técnica, ubicación en el código fuente y fragmentos ilustrativos de cada uno de los patrones implementados:

---

## 1. Arquitectura en Capas (Layered Architecture)

El sistema se estructura estrictamente en un esquema multicapa desacoplado, donde cada capa tiene responsabilidades bien delimitadas y se comunica únicamente con sus capas adyacentes:

```text
┌────────────────────────────────────────────────────────┐
│               CAPA DE PRESENTACIÓN / VISTA             │
│   Thymeleaf Templates (SSR) + Bootstrap 5 + JavaScript │
└───────────────────────────┬────────────────────────────┘
                            │ Solicitudes HTTP (GET, POST)
                            ▼
┌────────────────────────────────────────────────────────┐
│                 CAPA DE CONTROLADORES                  │
│       Spring MVC Controllers (Dashboard, Client, Auth) │
└───────────────────────────┬────────────────────────────┘
                            │ Invocación de métodos de negocio
                            ▼
┌────────────────────────────────────────────────────────┐
│                   CAPA DE SERVICIOS                    │
│    Lógica de Negocio y Transacciones (@Transactional)  │
│  (StockService, SaleOrderService, ReportService, etc.) │
└───────────────────────────┬────────────────────────────┘
                            │ Invocación a repositorios
                            ▼
┌────────────────────────────────────────────────────────┐
│              CAPA DE PERSISTENCIA / ACCESO A DATOS     │
│   Spring Data JPA Repositories + Entidades ORM / SQL   │
└────────────────────────────────────────────────────────┘
```

- **Ubicación en código:**
  - Vistas: `src/main/resources/templates/`
  - Controladores: `ingsoftware.zeroshop.controller`
  - Servicios: `ingsoftware.zeroshop.service`
  - Persistencia: `ingsoftware.zeroshop.repository` y `ingsoftware.zeroshop.entity`

---

## 2. Modelo / Vista / Controlador (MVC)

El patrón MVC desacopla la representación visual de la lógica de procesamiento:
- **Modelo:** Representado por las entidades de dominio JPA (`Product`, `SaleOrder`, `User`, etc.) y DTOs transferidos a través del modelo de Spring (`org.springframework.ui.Model`).
- **Vista:** Plantillas HTML5 procesadas por el motor Thymeleaf (`templates/client/`, `templates/dashboard/`), consumiendo los atributos inyectados en el modelo.
- **Controlador:** Componentes Spring `@Controller` que interceptan las peticiones HTTP del usuario, delegan el procesamiento al servicio correspondiente y devuelven el nombre de la vista a renderizar o redirección.

```java
// Ejemplo en ProductController (Dashboard)
@GetMapping("/dashboard/products")
public String listProducts(Model model) {
    List<Product> products = productService.getAllActiveProducts();
    model.addAttribute("products", products); // Modelo
    return "dashboard/products";              // Vista
}
```

---

## 3. Inyección de Dependencias (Dependency Injection - DI)

Para lograr bajo acoplamiento y facilitar el testing unitario con dobles de prueba (Mocks), se implementa **Inyección de Dependencias por Constructor** en todos los componentes del sistema, evitando `@Autowired` directo sobre atributos privados y asegurando la inmutabilidad de los colaboradores (`private final`).

```java
// Ejemplo en SaleOrderService
@Service
public class SaleOrderService {
    private final SaleOrderRepository saleOrderRepository;
    private final StockService stockService;
    private final EmailService emailService;

    // Inyección de dependencias por constructor
    public SaleOrderService(SaleOrderRepository saleOrderRepository,
                            StockService stockService,
                            EmailService emailService) {
        this.saleOrderRepository = saleOrderRepository;
        this.stockService = stockService;
        this.emailService = emailService;
    }
}
```

---

## 4. Experto en Información (Information Expert - GRASP)

El patrón de Experto asigna la responsabilidad de una tarea a la clase que posee la información necesaria para llevarla a cabo:
- **`StockService`:** Es el único experto encargado de computar existencias, determinar niveles críticos de inventario (>50% Bueno, 20%-50% Regular, <20% Malo) y aplicar incrementos o decrementos de stock.
- **`ReportService`:** Es el experto en calcular totales facturados por rango de fecha, estructurar información de ventas y consolidar la comparativa del proveedor más económico.
- **`PriceService`:** Es el experto en verificar la caducidad bimestral de precios por contexto inflacionario en Argentina y calcular ajustes porcentuales.

---

## 5. Creador (Creator - GRASP)

Asigna la responsabilidad de instanciar un objeto a la clase que contiene, agrega, registra o utiliza estrechamente a dicho objeto:
- **`SaleOrderService`:** Como dueño del agregado de la compra del cliente, se encarga de crear las instancias de `OrderDetail` y `Payment` vinculadas a la `SaleOrder`.
- **`PurchaseOrderService`:** Crea y persiste las órdenes de compra a proveedores con sus respectivos totales calculados.
- **`UserService`:** Al registrar un nuevo cliente o empleado, asume la responsabilidad de crear e inicializar la instancia concreta de `Person` (`Client` o `Employee`).

```java
// Ejemplo en UserService (Patrón Creador)
Person savedPerson = prepareOrReactivatePerson(null, cleanIdNumber, firstName, lastName, dateOfBirth, idType, Role.CLIENT);

User user = new User();
user.setUsername(normalizedEmail);
user.setPassword(passwordEncoder.encode(password));
user.setRole(Role.CLIENT);
user.setPerson(savedPerson); // Vinculación de la Persona creada
userRepository.save(user);
```

---

## 6. Polimorfismo (Polymorphism - GRASP / GoF)

Se implementa polimorfismo para manejar variaciones de comportamiento según el tipo de entidad:
- **Jerarquía de Personas:** `Person` es la clase base que define la identidad (`idType`, `idNumber`, nombres), mientras que `Client` (con `clientNumber`) y `Employee` (con `employeeType`, `hireDate`) extienden de ella utilizando la estrategia de herencia `@Inheritance(strategy = InheritanceType.JOINED)` de JPA.
- **Medios de Pago (`PaymentMethod`):** Procesamiento polimórfico de medios de pago (`MERCADO_PAGO`, `CASH`, `CREDIT`, `DEBIT`), donde cada medio tiene su propia lógica de confirmación inmediata o redirección a pasarela externa.

---

## 7. Alta Cohesión y Bajo Acoplamiento (High Cohesion / Low Coupling - GRASP)

- **Alta Cohesión:** Cada servicio y repositorio se enfoca estrictamente en un agregado o dominio funcional:
  - `actor`: Usuarios, personas, clientes, empleados y proveedores.
  - `catalog`: Productos, categorías, subcategorías y precios.
  - `org`: Sucursales físicas e inventario de stock.
  - `transaction`: Órdenes de venta, órdenes de compra y pagos.
  - `report`: Consolidación analítica.
  - `notification`: Mensajería SMTP y boletines de ofertas.
- **Bajo Acoplamiento:** Los controladores no se comunican directamente con la base de datos ni contienen lógica SQL. Toda la comunicación se media mediante contratos de servicios e interfaces de repositorio.

---

## 8. DTO (Data Transfer Object) - Uso en Reportes y Transferencia

Requerimiento explícito del integrador: *"h) DTO (En el uso de reportes)"*. Se diseñaron DTOs específicos para desacoplar el modelo de base de datos de la estructura de datos consumida por las vistas analíticas, evitando consultas N+1 y transformaciones costosas en la presentación:

1. **`SalesReportDTO` (`ingsoftware.zeroshop.dto.SalesReportDTO`):** Encapsula el rango de fechas consultado, el total facturado, la cantidad de operaciones y el listado de ítems vendidos con nombre de producto, categoría, identificador de orden y medio de pago.
2. **`StockReportDTO` (`ingsoftware.zeroshop.dto.StockReportDTO`):** Contiene el porcentaje de stock por sucursal, el estado del semáforo (Bueno, Regular, Malo) y la URL preformateada para enviar mensaje directo vía WhatsApp al proveedor solicitando la cantidad exacta para alcanzar el 50%.
3. **`SupplierReportDTO` (`ingsoftware.zeroshop.dto.SupplierReportDTO`):** Proyecta la relación producto-proveedor con el costo de adquisición más bajo del mercado para optimizar la reposición de mercadería.
4. **`ClientProfileDTO`:** Utilizado para transferir y validar de manera atómica los datos personales y de domicilio del cliente.

```java
// Definición del DTO de Ventas
public class SalesReportDTO {
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalBilled;
    private int totalOrders;
    private List<SaleItemDTO> items;
    // ...
}
```

---

## 9. DAO / ORM (Data Access Object / Entidades / JPA)

La capa de persistencia se implementa mediante **Spring Data JPA**, aplicando el patrón DAO a través de interfaces que extienden `JpaRepository<T, UUID>`:
- **Abstracción OID / UUID:** Claves primarias universales de 128 bits (`UUID`) autogeneradas.
- **Borrado Lógico Unificado:** Todas las entidades poseen la columna `deleted (Boolean)`. Los repositorios proveen métodos estándar `find(UUID id)` y `findActive(UUID id)` como métodos por defecto para garantizar que los registros inactivos no interfieran en consultas operativas.
- **Consultas JPQL Optimizadas (`@Query`):** Empleadas para sumas y conteos agregados (ej. cálculo total de existencias por producto en `StockRepository`).

---

## 10. Reglas de Modelado y Persistencia

1. **Evitar relaciones pesadas bidireccionales:** No se utilizan colecciones `OneToMany` o `ManyToMany` bidireccionales en entidades transaccionales para prevenir problemas de rendimiento de memoria o LazyInitializationException. La navegación se resuelve mediante consultas indexadas en los repositorios (ej. `findByOrderIdAndDeletedFalse(UUID orderId)`).
2. **Borrado Lógico Obligatorio:** Ninguna operación de usuario elimina físicamente filas en la base de datos de producción; se efectúa un update a `deleted = true`.
