# ENTIDADES DEL SISTEMA

## Organización de las entidades

- catalog:
  - Product
  - Category
  - SubCategory
  - PriceHistory
- location
  - Country
  - State
  - City
  - Address
- media
  - Image
- org
  - Company
  - Office
- transaction
  - Order
  - OrderDetail
  - PurchaseOrder
  - SaleOrder
  - Invoice
  - InvoiceDetail
  - ClientInvoice
  - SupplierInvoice
  - Payment
  - PaymentMethod
- actor
  - Person
  - Client
  - Employee
  - Supplier
  - User
  - ContactInfo
  - ContactEmail
  - ContactPhone

## Entidades de catalog

- **Product**: Representa los productos disponibles para la venta en la tienda. Contiene información principal como nombre, descripción, stock, precio actual y sus relaciones con categorías, imágenes y variantes.
- **Category**: Clasificación principal de los artículos (ej. Calzado, Indumentaria, Accesorios). Facilita la navegación y organización del catálogo.
- **SubCategory**: Subdivisión específica perteneciente a una categoría principal para un filtrado más detallado (ej. Zapatillas de Running dentro de Calzado).
- **PriceHistory**: Registra el histórico de cambios de precio de un producto a lo largo del tiempo, incluyendo fechas de vigencia. Permite auditoría de precios y cálculo de promociones/ofertas.

## Entidades de location

- **Country**: Representa un país (nombre, código ISO). Nivel superior de la jerarquía geográfica.
- **State**: Provincia, estado o región perteneciente a un país (`Country`).
- **City**: Ciudad o localidad perteneciente a una provincia o estado (`State`).
- **Address**: Dirección física normalizada (calle, número, piso, departamento, código postal y observaciones) vinculada a una ciudad (`City`). Es utilizada transversalmente por sucursales (`Office`), clientes y proveedores.

## Entidades de media

- **Image**: Almacena o referencia archivos visuales multimedia (nombre, tipo MIME, contenido o ruta, y tipo de uso mediante `ImageType`: producto, perfil de usuario, banner, etc.). Permite desacoplar los archivos binarios de las entidades de negocio.

## Entidades de org

- **Company**: Representa la entidad legal o empresa matriz propietaria de la tienda. Almacena la razón social, CUIT/identificación tributaria, datos fiscales y configuración institucional.
- **Office**: Representa una sucursal, local comercial físico o depósito de la empresa. Contiene nombre, código de sucursal y está vinculada a una dirección física (`Address`).

## Entidades de transaction

- **Order**: Clase base (abstracta) que encapsula la estructura común de cualquier orden comercial (número o código de orden, fecha de emisión, estado y monto total).
- **SaleOrder**: Pedido de compra realizado por un cliente (`Client`) en la tienda. Hereda de `Order` y gestiona el flujo de venta, entrega y cobro.
- **PurchaseOrder**: Orden de compra emitida a un proveedor (`Supplier`) para reposición de stock. Hereda de `Order` y gestiona el aprovisionamiento de mercadería.
- **OrderDetail**: Renglón o ítem individual asociado a una orden (`Order`). Vincula el producto (`Product`), cantidad, precio unitario y subtotal. Es reutilizable tanto para ventas como para compras.
- **Invoice**: Clase base (abstracta) para comprobantes fiscales y contables (número legal de factura, fecha de emisión, subtotal, impuestos aplicados y total general).
- **ClientInvoice**: Factura de venta emitida a un cliente (`Client`), generada a partir de una orden de venta (`SaleOrder`).
- **SupplierInvoice**: Factura de compra emitida por un proveedor (`Supplier`), respaldando una orden de compra (`PurchaseOrder`).
- **InvoiceDetail**: Detalle de los conceptos facturados en una factura (`Invoice`), registrando producto/concepto, cantidades, precios unitarios y alícuotas impositivas.
- **PaymentMethod**: Define los medios de pago disponibles en el sistema (Tarjeta de Crédito, Débito, Transferencia, Efectivo, billeteras virtuales, etc.).
- **Payment**: Transacción efectiva de cobro o pago realizada mediante un método de pago (`PaymentMethod`), registrando monto, fecha, estado del pago y comprobante o código de autorización externa.

## Entidades de actor

- **Person**: Modela los datos civiles y biográficos de una persona física (nombre, apellido, tipo y número de documento, fecha de nacimiento).
- **User**: Modela la cuenta de seguridad y autenticación en la plataforma (correo electrónico, contraseña encriptada, rol del sistema y estado activo/inactivo). Se asocia a una persona (`Person`) para separar credenciales de datos personales.
- **Client**: Representa el rol de cliente/comprador en la plataforma. Extiende o asocia a una `Person` con su historial comercial, preferencias y direcciones de entrega.
- **Employee**: Representa a un trabajador o colaborador interno de la empresa. Extiende o asocia a una `Person` con sus datos laborales (legajo, tipo de empleado `EmployeeType`) y sucursal asignada (`Office`).
- **Supplier**: Representa a una empresa o entidad proveedora de mercadería (razón social, CUIT/identificador tributario y datos comerciales). Se vincula con órdenes de compra y facturas de compras.
- **ContactInfo**: Clase base (abstracta) que define la estructura para registrar canales de comunicación (observación, tipo de contacto `ContactType` y estado).
- **ContactEmail**: Especialización de `ContactInfo` para gestionar direcciones de correo electrónico de personas o proveedores.
- **ContactPhone**: Especialización de `ContactInfo` para gestionar líneas telefónicas (número, tipo de teléfono `PhoneType`).
