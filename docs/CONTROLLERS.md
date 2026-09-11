# CONTROLADORES DEL SISTEMA

## Reglas para el uso de metodos HTTP en los controladores

- GET: Se utiliza para obtener información del servidor. No debe modificar el estado del servidor ni tener efectos secundarios. Se utiliza para mostrar páginas, obtener datos o recursos.
- POST: Se utiliza para enviar datos al servidor y crear nuevos recursos. Puede modificar el estado del servidor y tener efectos secundarios. Se utiliza para enviar formularios, crear registros o realizar acciones que cambien el estado del servidor.
- PUT: Se utiliza para actualizar recursos existentes en el servidor. Puede modificar el estado del servidor y tener efectos secundarios. Se utiliza para modificar datos de registros o recursos ya existentes.
- DELETE: Se utiliza para eliminar recursos del servidor. Puede modificar el estado del servidor y tener efectos secundarios. Se utiliza para borrar registros o recursos.

## 1. CONTROLADORES DE AUTENTICACIÓN Y CUENTA

- AuthController
  - GET /login
  - POST /login
  - GET /register
  - POST /register
  - GET /verify
  - POST /verify
  - POST /verify/resend
  - GET /logout

## 2. CONTROLADORES DE CLIENTE

- ClientController
  - GET /
- ProductController
  - GET /products
  - GET /products/:id
  - GET /offers
- CategoryController
  - GET /categories
  - GET /categories/:id
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

## 3. CONTROLADORES DE ADMINISTRACIÓN

- AdminController
  - GET /admin/
- ProductController
  - GET /admin/products
  - GET /admin/products/:id
  - POST /admin/products/
  - PUT /admin/products/:id
  - DELETE /admin/products/:id
  - GET /admin/products/:id/prices
  - POST /admin/products/:id/prices
  - PUT /admin/products/:id/prices/:priceId
  - DELETE /admin/products/:id/prices/:priceId
  - GET /admin/stock
- CategoryController
  - GET /admin/categories
  - GET /admin/categories/:id
  - POST /admin/categories/
  - PUT /admin/categories/:id
  - DELETE /admin/categories/:id
- OrderController
  - GET /admin/orders
  - GET /admin/orders/:id
  - POST /admin/orders/
  - PUT /admin/orders/:id
  - DELETE /admin/orders/:id
- ProviderController
  - GET /admin/providers
  - GET /admin/providers/:id
  - POST /admin/providers
  - PUT /admin/providers/:id
  - DELETE /admin/providers/:id
- PurchaseOrderController
  - GET /admin/purchase-orders
  - GET /admin/purchase-orders/:id
  - POST /admin/purchase-orders
  - PUT /admin/purchase-orders/:id
  - DELETE /admin/purchase-orders/:id
- OfficeController
  - GET /admin/offices
  - GET /admin/offices/:id
  - POST /admin/offices
  - PUT /admin/offices/:id
  - DELETE /admin/offices/:id
- UserController
  - GET /admin/users
  - GET /admin/users/:id
  - POST /admin/users
  - PUT /admin/users/:id
  - DELETE /admin/users/:id
- ReportController
  - GET /admin/reports
  - GET /admin/reports/sales
  - GET /admin/reports/stock
  - GET /admin/reports/suppliers
