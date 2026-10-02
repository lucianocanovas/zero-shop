# VISTAS DEL SISTEMA

## Plantilla Base y Diseño de Interfaz

- **Sitio Público:** Maquetación moderna responsiva con Bootstrap 5, Bootstrap Icons y hojas de estilo propias para catálogo e-commerce deportivo unisex de Mendoza.
- **Panel Administrativo (Dashboard):** Plantilla tipo Dashboard adaptada de [NiceAdmin Bootstrap](https://bootstrapmade.com/nice-admin-bootstrap-admin-html-template/) para control integral de operaciones, inventario, ventas y reportes.

---

## 1. Vistas de Autenticación y Cuenta (`/templates`)

- **Iniciar sesión:** `/login` (`templates/login.html`)
  - Formulario de autenticación con email y contraseña, alertas de verificación exitosa y logout.
- **Registrarse:** `/register` (`templates/register.html`)
  - Registro de cuenta para nuevos clientes con nombre, apellido, DNI, fecha de nacimiento, correo y contraseña.
- **Verificación / Activación de cuenta:** `/verify` (`templates/verify.html`)
  - Ingreso del código numérico de 6 dígitos enviado por correo para activar la cuenta creada, con opción de reenvío de código.
- **Cerrar sesión:** `/logout` (Manejado por Spring Security con redirección a `/login?logout=true`).

---

## 2. Vistas Públicas y de Clientes (`/templates/client`)

- **Inicio Institucional:** `/` (`templates/client/index.html`)
  - Banner principal, carrusel de marcas/categorías, productos destacados y llamadas a la acción.
- **Contacto y Atención al Cliente:** `/contact` (`templates/client/contact.html`)
  - Canales de comunicación de la tienda física en Mendoza, formulario de consulta y WhatsApp corporativo.
- **Catálogo de Productos:** `/products` (`templates/client/products.html`)
  - Grilla interactiva con búsqueda por texto, filtros por categoría, subcategoría, talle y ordenamiento por precio.
- **Detalle de Producto:** `/products/{id}` (`templates/client/product-detail.html`)
  - Galería de imágenes, descripción completa, talle, stock disponible en tiempo real y botón de agregado al carrito.
- **Ofertas Especiales:** `/offers` (`templates/client/offers.html`)
  - Sección dedicada exclusivamente a productos con stock disponible que poseen precio promocional/descuento activo.
- **Navegación de Categorías:** `/categories` (`templates/client/categories.html`)
  - Explorador por ramas (*Niños*, *Niñas*, *Mujeres*, *Hombres*) y tipos de artículos (*Calzado*, *Ropa*, *Accesorios*).
- **Proceso de Compra (Checkout):** `/checkout` (`templates/client/checkout.html`)
  - Resumen del carrito, selección de domicilio de entrega y medios de pago (Efectivo en entrega, Mercado Pago).
- **Compra Finalizada:** `/checkout/success` (`templates/client/checkout-success.html`)
  - Pantalla de confirmación con identificador de compra, resumen de pedido e información de seguimiento enviada por email.
- **Perfil de Cliente:** `/profile` (`templates/client/profile.html`)
  - Gestión completa de datos personales (Nombre, Apellido, DNI, Sexo, Fecha de Nacimiento) y libreta de direcciones.
- **Mis Compras (Historial):** `/orders` (`templates/client/orders.html`)
  - Listado de pedidos realizados con visualización del estado de entrega (`PENDIENTE DE PAGO`, `PAGO REALIZADO`, `PENDIENTE DE ENVIO`, `ENTREGADO`).
- **Detalle y Seguimiento de Orden:** `/orders/{id}` (`templates/client/order-detail.html`)
  - Desglose de artículos, comprobante de pago, dirección de envío y botón de anulación de compra si aplica.

---

## 3. Vistas Comunes del Dashboard (`/templates/dashboard`)

*Vistas compartidas para personal administrativo (`ADMIN`) y empleados (`EMPLOYEE`).*

- **Inicio del Dashboard:** `/dashboard` (`templates/dashboard/index.html`)
  - Indicadores clave de desempeño (KPIs), accesos directos y notificaciones de pedidos pendientes.
- **Gestión de Productos:** `/dashboard/products` (`templates/dashboard/products.html`)
  - Tabla de productos registrados con acciones rápidas de edición, eliminación y precios.
- **Alta de Producto:** `/dashboard/products/new` (`templates/dashboard/product-new.html`)
  - Formulario con subida de imagen, nombre, descripción, talle, categoría y asignación inicial de oferta.
- **Edición de Producto:** `/dashboard/products/{id}/edit` (`templates/dashboard/product-edit.html`)
- **Historial de Precios y Actualización:** `/dashboard/products/{id}/prices` (`templates/dashboard/product-prices.html`)
  - Registro cronológico de variaciones de precio y carga de nuevo precio de venta (actualizaciones bimestrales).
- **Control de Stock por Sucursal:** `/dashboard/stock` (`templates/dashboard/stock.html`)
  - Matriz de existencias discriminadas por sucursal física con ajuste manual de unidades.
- **Gestión de Categorías:** `/dashboard/categories` (`templates/dashboard/categories.html`)
- **Alta / Edición de Categoría:** `category-new.html`, `category-edit.html`, `category-detail.html`
- **Órdenes de Venta a Clientes:** `/dashboard/sale-orders` (`templates/dashboard/sale-orders.html`)
  - Control de pedidos entrantes, verificación de comprobantes y cambio de estado de envío.
- **Detalle de Pedido de Cliente:** `/dashboard/sale-orders/{id}` (`templates/dashboard/order-detail.html`)
- **Directorio de Proveedores:** `/dashboard/providers` (`templates/dashboard/providers.html`)
  - Razón social, correos y contacto directo vía WhatsApp para compras mayoristas.
- **Alta y Edición de Proveedor:** `provider-new.html`, `provider-edit.html`
- **Órdenes de Compra a Proveedores:** `/dashboard/purchase-orders` (`templates/dashboard/purchase-orders.html`)
- **Emisión de Orden de Compra:** `/dashboard/purchase-orders/new` (`templates/dashboard/purchase-order-new.html`)
- **Recepción de Mercadería:** `/dashboard/purchase-orders/{id}` (`templates/dashboard/purchase-order-detail.html`)
  - Verificación de entrega de proveedor y botón de recepción que incrementa el stock automáticamente.

---

## 4. Vistas Exclusivas de Administración (`/templates/dashboard/admin`)

*Acceso exclusivo para rol `ADMIN`.*

- **Panel de Administración General:** `/dashboard/admin` (`templates/dashboard/admin/index.html`)
- **Gestión de Sucursales:** `/dashboard/admin/offices` (`templates/dashboard/admin/offices.html`)
  - Casa central Mendoza, depósitos y salones comerciales. Formularios en `office-new.html` y `office-edit.html`.
- **ABM de Usuarios:** `/dashboard/admin/users` (`templates/dashboard/admin/users.html`)
  - Administración de cuentas, restablecimiento de credenciales y roles. Formularios en `user-new.html`, `user-edit.html` y `user-detail.html`.
- **Módulo de Reportes Analíticos:** `/dashboard/admin/reports` (`templates/dashboard/admin/reports.html`)
  - Menú de selección de reportes requeridos por el integrador.
- **Reporte de Ventas:** `/dashboard/admin/reports/sales` (`templates/dashboard/admin/reports-sales.html`)
  - Filtro por rango de fechas (ej: mensual), monto total facturado y detalle de producto, categoría, cantidad, ID y medio de pago.
- **Reporte de Stock y Reposición:** `/dashboard/admin/reports/stock` (`templates/dashboard/admin/reports-stock.html`)
  - Semáforo de existencias por sucursal (Bueno > 50%, Regular 20%-50%, Malo < 20%) y botón directo de WhatsApp para solicitar reposición al proveedor.
- **Reporte de Proveedores Económicos:** `/dashboard/admin/reports/suppliers` (`templates/dashboard/admin/reports-suppliers.html`)
  - Tabla comparativa de costos de compra por producto para determinar la opción más económica de abastecimiento.

---

## 5. Vistas Exclusivas de Empleados (`/templates/dashboard/employee`)

*Acceso exclusivo para rol `EMPLOYEE`.*

- **Escritorio Operativo del Empleado:** `/dashboard/employee/desk` (`templates/dashboard/employee/desk.html`)
  - Terminal de mostrador para gestión rápida de stock local, despacho de pedidos y consulta de catálogo en sucursal asignada.

---

## 6. Vistas de Error

- **Página de Error:** `/error` (`templates/error.html`)
  - Manejo amigable de errores HTTP (400, 403, 404, 500) con botón para retornar a la tienda.
