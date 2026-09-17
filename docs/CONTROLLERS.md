# CONTROLADORES DEL SISTEMA

## Reglas para el uso de metodos HTTP en los controladores

- GET: Se utiliza para obtener información del servidor. No debe modificar el estado del servidor ni tener efectos secundarios. Se utiliza para mostrar páginas, obtener datos o recursos.
- POST: Se utiliza para enviar datos al servidor y crear nuevos recursos. Puede modificar el estado del servidor y tener efectos secundarios. Se utiliza para enviar formularios, crear registros o realizar acciones que cambien el estado del servidor.
- PUT: Se utiliza para actualizar recursos existentes en el servidor. Puede modificar el estado del servidor y tener efectos secundarios. Se utiliza para modificar datos de registros o recursos ya existentes.
- DELETE: Se utiliza para eliminar recursos del servidor. Puede modificar el estado del servidor y tener efectos secundarios. Se utiliza para borrar registros o recursos.

## Controladores de autenticación y cuenta

- AuthController
  - GET /login
  - POST /login
  - GET /register
  - POST /register
  - GET /verify
  - POST /verify
  - POST /verify/resend
  - GET /logout

## Controladores de cliente

- ClientController
  - GET /

- ProductController
  - GET /products
  - GET /products/:id
  - GET /offers

- CategoryController
  - GET /categories

- CheckoutController
  - GET /checkout
  - POST /checkout
  - GET /checkout/success

- ProfileController
  - GET /profile
  - PUT /profile

- OrderController
  - GET /orders
  - GET /orders/:id
  - DELETE /orders/:id

## Controladores comunes del Dashboard (/dashboard - ADMIN y EMPLOYEE)

- DashboardController
  - GET /dashboard
  - GET /dashboard/

- ProductController
  - GET /dashboard/products
  - GET /dashboard/products/:id
  - POST /dashboard/products/
  - PUT /dashboard/products/:id
  - DELETE /dashboard/products/:id
  - GET /dashboard/products/:id/prices
  - POST /dashboard/products/:id/prices
  - PUT /dashboard/products/:id/prices/:priceId
  - DELETE /dashboard/products/:id/prices/:priceId
  - GET /dashboard/stock
  - PUT /dashboard/stock/:id

- CategoryController
  - GET /dashboard/categories
  - GET /dashboard/categories/:id
  - POST /dashboard/categories/
  - PUT /dashboard/categories/:id
  - DELETE /dashboard/categories/:id

- OrderController
  - GET /dashboard/sale-orders
  - GET /dashboard/sale-orders/:id
  - POST /dashboard/sale-orders/
  - PUT /dashboard/sale-orders/:id
  - PUT /dashboard/sale-orders/:id/status
  - DELETE /dashboard/sale-orders/:id

- ProviderController
  - GET /dashboard/providers
  - GET /dashboard/providers/:id
  - POST /dashboard/providers
  - PUT /dashboard/providers/:id
  - DELETE /dashboard/providers/:id

- PurchaseOrderController
  - GET /dashboard/purchase-orders
  - GET /dashboard/purchase-orders/:id
  - POST /dashboard/purchase-orders
  - PUT /dashboard/purchase-orders/:id
  - POST /dashboard/purchase-orders/:id/receive
  - DELETE /dashboard/purchase-orders/:id

## Controladores exclusivos de administración (/dashboard/admin - SOLO ADMIN)

- UserController
  - GET /dashboard/admin/users
  - GET /dashboard/admin/users/:id
  - POST /dashboard/admin/users
  - PUT /dashboard/admin/users/:id
  - DELETE /dashboard/admin/users/:id

- OfficeController
  - GET /dashboard/admin/offices
  - GET /dashboard/admin/offices/new
  - GET /dashboard/admin/offices/:id
  - POST /dashboard/admin/offices
  - PUT /dashboard/admin/offices/:id
  - DELETE /dashboard/admin/offices/:id

- ReportController
  - GET /dashboard/admin/reports
  - GET /dashboard/admin/reports/sales
  - GET /dashboard/admin/reports/stock
  - GET /dashboard/admin/reports/suppliers

## Controladores exclusivos de empleados (/dashboard/employee - SOLO EMPLOYEE)

- EmployeeController
  - GET /dashboard/employee
  - GET /dashboard/employee/desk
