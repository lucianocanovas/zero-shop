# CONTROLADORES DEL SISTEMA

## Reglas para el uso de métodos HTTP en los controladores

- **GET:** Se utiliza para obtener información del servidor. No debe modificar el estado del servidor ni tener efectos secundarios. Se utiliza para mostrar páginas, obtener datos o recursos.
- **POST:** Se utiliza para enviar datos al servidor y crear nuevos recursos. Puede modificar el estado del servidor y tener efectos secundarios. Se utiliza para enviar formularios, crear registros o realizar acciones que cambien el estado del servidor.
- **PUT / PATCH:** Se utiliza para actualizar recursos existentes en el servidor. Puede modificar el estado del servidor y tener efectos secundarios.
- **DELETE:** Se utiliza para eliminar recursos del servidor (o marcarlos como borrado lógico).

---

## 1. Controladores de Autenticación y Cuenta (`ingsoftware.zeroshop.controller`)

- **`AuthController`**
  - `GET /login`: Muestra la pantalla de inicio de sesión.
  - `POST /login`: Procesado automáticamente por Spring Security (`CustomUserDetailsService`).
  - `GET /register`: Muestra el formulario de registro de nuevos clientes con datos personales y DNI.
  - `POST /register`: Registra un nuevo cliente, envía código de verificación de 6 dígitos por email y redirige a `/verify`.
  - `GET /verify`: Muestra la pantalla de ingreso de código de activación de cuenta.
  - `POST /verify`: Valida el código de 6 dígitos enviado al email del cliente y activa la cuenta.
  - `POST /verify/resend`: Reenvía un nuevo código de activación al email indicado.
  - `GET /logout`: Cierre de sesión y redirección a la página principal.

---

## 2. Controladores de Navegación Pública y Cliente (`ingsoftware.zeroshop.controller.client`)

- **`ClientController`**
  - `GET /`: Página de inicio institucional de la tienda con productos destacados, ofertas y categorías.
  - `GET /contact`: Página institucional de contacto y canales de atención de Zero Shop Mendoza.

- **`ProductController`**
  - `GET /products`: Catálogo público de productos con filtros por categoría, subcategoría, búsqueda y talle.
  - `GET /products/{id}`: Vista de detalle del producto, especificaciones, talle, stock disponible y selector de cantidad.
  - `GET /offers`: Sección especial con catálogo de productos que cuentan con stock y se encuentran en oferta.

- **`CategoryController`**
  - `GET /categories`: Vista de navegación por categorías principales (*Niños*, *Niñas*, *Mujeres*, *Hombres*) y subcategorías (*Calzado*, *Ropa / Indumentaria*, *Accesorios*).

- **`SubCategoryController`**
  - `GET /subcategories`: Redirige al catálogo filtrado por categoría o al árbol general.
  - `GET /subcategories/{id}`: Redirige a los productos de la subcategoría seleccionada.
  - `GET /subcategories/api`: Endpoint REST auxiliar que retorna las subcategorías en formato JSON.

- **`CheckoutController`**
  - `GET /checkout`: Pantalla de checkout con resumen del carrito, selección de dirección de envío y método de pago (Efectivo, Mercado Pago).
  - `POST /checkout`: Procesa la compra online, genera la orden de venta en estado `PENDING_PAYMENT`, despacha comprobante por email y genera preferencia de Mercado Pago si aplica.
  - `GET /checkout/success`: Pantalla de confirmación y agradecimiento tras finalizar una compra exitosa.

- **`ProfileController`**
  - `GET /profile`: Visualización y administración del perfil del cliente (nombre, apellido, sexo, fecha nacimiento, direcciones completas y teléfonos).
  - `POST /profile`: Actualización de datos personales y domicilios del cliente.

- **`OrderController`**
  - `GET /orders`: Listado de mis compras para el cliente autenticado con seguimiento de estados (`PENDIENTE DE PAGO`, `PAGO REALIZADO`, `PENDIENTE DE ENVIO`, `PENDIENTE DE ENTREGA`, `ENTREGADO`).
  - `GET /orders/{id}`: Detalle completo de una orden de compra, comprobante y tracking de envío.
  - `POST /orders/{id}/cancel`: Anulación de la orden por parte del cliente si se encuentra en estado cancelable.

---

## 3. Controladores Comunes del Dashboard (`ingsoftware.zeroshop.controller.dashboard`)

*Acceso autorizado para roles `ADMIN` y `EMPLOYEE`.*

- **`DashboardController`**
  - `GET /dashboard`: Panel principal con métricas resumidas, accesos rápidos a pedidos, stock y compras.

- **`ProductController`**
  - `GET /dashboard/products`: ABM de productos; listado con búsqueda y paginación.
  - `GET /dashboard/products/new`: Formulario para dar de alta un nuevo producto.
  - `POST /dashboard/products`: Guarda el nuevo producto con sus atributos (nombre, descripción, talle, oferta e imagen).
  - `GET /dashboard/products/{id}/edit`: Formulario de edición de producto.
  - `POST /dashboard/products/{id}/edit`: Actualiza la información del producto.
  - `POST /dashboard/products/{id}/delete`: Baja lógica (`deleted = true`) del producto.
  - `GET /dashboard/products/{id}/prices`: Historial de precios y actualización de precio vigente.
  - `POST /dashboard/products/{id}/prices`: Registra un nuevo precio para el producto (ajuste bimestral / inflación).

- **`StockController`**
  - `GET /dashboard/stock`: Matriz de existencias de inventario por producto y sucursal física.
  - `POST /dashboard/stock/{id}`: Ajuste manual de unidades de inventario.

- **`CategoryController`**
  - `GET /dashboard/categories`: Listado de categorías y subcategorías activas.
  - `GET /dashboard/categories/new`: Formulario de creación de categoría / subcategoría.
  - `POST /dashboard/categories`: Guarda una nueva categoría o subcategoría.
  - `GET /dashboard/categories/{id}/edit`: Formulario de edición.
  - `POST /dashboard/categories/{id}/edit`: Actualiza la categoría.
  - `POST /dashboard/categories/{id}/delete`: Baja lógica de la categoría.

- **`OrderController`**
  - `GET /dashboard/sale-orders`: Listado general de órdenes de venta a clientes con filtro por estado.
  - `GET /dashboard/sale-orders/{id}`: Detalle de orden de venta, productos, dirección de entrega y pagos asociados.
  - `POST /dashboard/sale-orders/{id}/status`: Transición manual de estado del pedido (`PAGO REALIZADO`, `PENDIENTE DE ENVIO`, `ENTREGADO`).

- **`ProviderController`**
  - `GET /dashboard/providers`: Listado de proveedores registrados (Razón Social, correo, WhatsApp).
  - `GET /dashboard/providers/new`: Formulario de alta de proveedor.
  - `POST /dashboard/providers`: Registra un nuevo proveedor en el sistema.
  - `GET /dashboard/providers/{id}/edit`: Formulario de modificación de proveedor.
  - `POST /dashboard/providers/{id}/edit`: Actualiza datos y contactos de proveedor.
  - `POST /dashboard/providers/{id}/delete`: Baja lógica del proveedor.

- **`PurchaseOrderController`**
  - `GET /dashboard/purchase-orders`: Listado de órdenes de compra a proveedores.
  - `GET /dashboard/purchase-orders/new`: Formulario para emitir orden de compra a proveedor (proveedor, producto, cantidad, costo unitario).
  - `POST /dashboard/purchase-orders`: Genera la orden de compra a proveedor.
  - `GET /dashboard/purchase-orders/{id}`: Detalle de la orden de compra.
  - `POST /dashboard/purchase-orders/{id}/receive`: Recepción de mercadería: marca la orden como entregada y aumenta automáticamente el stock del producto en la sucursal indicada.

- **`LocationController`**
  - `GET /dashboard/locations/states`: API REST que retorna provincias de un país en JSON.
  - `GET /dashboard/locations/cities`: API REST que retorna ciudades / departamentos de una provincia en JSON.

---

## 4. Controladores Exclusivos de Administración (`ingsoftware.zeroshop.controller.dashboard.admin`)

*Acceso restringido únicamente a usuarios con rol `ADMIN`.*

- **`UserController`**
  - `GET /dashboard/admin/users`: ABM de usuarios; listado general de clientes, empleados y administradores.
  - `GET /dashboard/admin/users/new`: Formulario para crear un nuevo usuario con rol asignado.
  - `POST /dashboard/admin/users`: Persistencia de nuevo usuario y persona asociada con clave encriptada.
  - `GET /dashboard/admin/users/{id}/edit`: Edición de datos y restablecimiento de contraseña.
  - `POST /dashboard/admin/users/{id}/edit`: Actualización de usuario y persona.
  - `POST /dashboard/admin/users/{id}/delete`: Baja lógica de usuario.

- **`OfficeController`**
  - `GET /dashboard/admin/offices`: Gestión de sucursales físicas de Zero Shop Mendoza.
  - `GET /dashboard/admin/offices/new`: Formulario de creación de sucursal.
  - `POST /dashboard/admin/offices`: Alta de sucursal con dirección y teléfono.
  - `GET /dashboard/admin/offices/{id}/edit`: Edición de sucursal y asignación de personal.
  - `POST /dashboard/admin/offices/{id}/edit`: Actualización de la sucursal.
  - `POST /dashboard/admin/offices/{id}/delete`: Baja lógica de la sucursal.

- **`ReportController`**
  - `GET /dashboard/admin/reports`: Menú principal del módulo analítico y de reportes.
  - `GET /dashboard/admin/reports/sales`: **Reporte de Ventas** por rango de fechas (especificación integrador): total facturado, cantidad de órdenes y detalle (fecha, producto, categoría, cantidad, ID de compra, forma de pago).
  - `GET /dashboard/admin/reports/stock`: **Reporte de Stock por Sucursal** con semáforo de inventario (Bueno > 50%, Regular 20%-50%, Malo < 20%) y generación directa de enlace a WhatsApp para solicitar reposición al proveedor.
  - `GET /dashboard/admin/reports/suppliers`: **Reporte de Proveedores Económicos**: determina el costo más bajo por producto para optimizar compras de reposición.

---

## 5. Controladores Exclusivos de Empleados (`ingsoftware.zeroshop.controller.dashboard.employee`)

*Acceso restringido a rol `EMPLOYEE`.*

- **`EmployeeController`**
  - `GET /dashboard/employee`: Panel de control del empleado operativo.
  - `GET /dashboard/employee/desk`: Escritorio de trabajo asignado según la sucursal del empleado para atención de mostrador y despacho de pedidos.
