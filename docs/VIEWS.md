# VISTAS DEL SISTEMA

## Vistas de autenticación y cuenta

- Iniciar sesión:
  /login

- Registrarse:
  /register

- Verificación / Activación de cuenta:
  /verify

- Cerrar sesión:
  /logout

## Vistas de cliente

- Inicio:
  /

- Productos:
  /products
  /products/:id
  
- Ofertas especiales:
  /offers

- Categorías y subcategorías:
  /categories

- Checkout:
  /checkout

- Perfil del cliente:
  /profile

- Mis compras:
  /orders
  /orders/:id

## Vistas del Dashboard (/dashboard - ADMIN y EMPLOYEE)

- Inicio del Dashboard:
  /dashboard

- Productos y Stock:
  /dashboard/products
  /dashboard/products/:id
  /dashboard/products/:id/prices
  /dashboard/stock

- Categorías:
  /dashboard/categories
  /dashboard/categories/new
  /dashboard/categories/:id

- Pedidos de Clientes:
  /dashboard/sale-orders
  /dashboard/sale-orders/:id

- Proveedores:
  /dashboard/providers
  /dashboard/providers/:id

- Órdenes de Compra a Proveedores:
  /dashboard/purchase-orders
  /dashboard/purchase-orders/:id

## Vistas exclusivas de administración (/dashboard/admin - SOLO ADMIN)

- Panel de Administración:
  /dashboard/admin

- Sucursales:
  /dashboard/admin/offices
  /dashboard/admin/offices/new
  /dashboard/admin/offices/:id

- Usuarios:
  /dashboard/admin/users
  /dashboard/admin/users/:id

- Reportes:
  /dashboard/admin/reports
  /dashboard/admin/reports/sales
  /dashboard/admin/reports/stock
  /dashboard/admin/reports/suppliers

## Vistas exclusivas de empleados (/dashboard/employee - SOLO EMPLOYEE)

- Escritorio del Empleado / Sucursal Asignada:
  /dashboard/employee
  /dashboard/employee/desk

## Vistas de error

- Error:
  /error/:code
