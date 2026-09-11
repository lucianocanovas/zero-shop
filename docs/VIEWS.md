# VISTAS DEL SISTEMA

## 1. VISTAS DE AUTENTICACIÓN Y CUENTA

- Iniciar sesión:
  /login
- Registrarse:
  /register
- Verificación / Activación de cuenta:
  /verify
- Cerrar sesión:
  /logout

## 2. VISTAS DE LA TIENDA

- Inicio:
  /
- Productos:
  /products
  /products/:id
- Ofertas especiales:
  /offers
- Categorías y subcategorías:
  /categories
  /categories/:id
- Checkout:
  /checkout
- Perfil del cliente:
  /profile
- Mis compras:
  /orders
  /orders/:id

## 3. VISTAS DE ADMINISTRACIÓN

- Inicio:
  /admin

- Productos y Stock:
  /admin/products
  /admin/products/:id
  /admin/products/:id/prices
  /admin/stock

- Categorías:
  /admin/categories
  /admin/categories/:id

- Pedidos de Clientes:
  /admin/orders
  /admin/orders/:id

- Proveedores:
  /admin/providers
  /admin/providers/:id

- Órdenes de Compra a Proveedores:
  /admin/purchase-orders
  /admin/purchase-orders/:id

- Sucursales:
  /admin/offices
  /admin/offices/:id

- Usuarios:
  /admin/users
  /admin/users/:id

- Reportes:
  /admin/reports
  /admin/reports/sales
  /admin/reports/stock
  /admin/reports/suppliers

## 4. VISTAS ADICIONALES

- Error:
  /error/:code
