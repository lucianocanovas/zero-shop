# TO-DO

1. [x] Agregar paginacion, busqueda y filtros a todos los listados de la app y verificar que funcione correctamente. *(Completado y verificado con suite de pruebas)*

2. [x] Eliminar la opcion de vender sin registrar un cliente en el POS, si o si se deben registrar los datos del cliente para poder vender. *(Completado y verificado con suite de pruebas)*

3. [x] Arreglar el estilo de la seccion lateral del punto de venta que se ve por encima del header, ajusta los z-indexes y el overflow para que no se vea por encima del header. *(Completado: topbar con z-index 1030 y lateral POS con z-index 10 y overflow-y con scrollbar dedicado)*

4. [x] Al agregar un objeto al carrito de ventas no se debe redirigir a la pagina de ventas, se debe mantener en la misma pagina y mostrar un mensaje de que el producto fue agregado al carrito. *(Completado y verificado con suite de pruebas)*

5. [x] La seccion Mis Compras devuelve un error 500 al intentar acceder a ella, revisar el log para ver que esta pasando. *(Completado: corrección de IllegalStateException por `int page` primitivo en query strings `?page=`, migración global a `Integer page`, transaccionalidad `@Transactional(readOnly = true)` para carga lazy y comprobado con test de integración)*

6. [x] Al tener un producto con stock critico, se debe enviar un correo al administrador para notificarle que el producto tiene stock critico y que debe reponerlo. *(Completado: umbral <= 5, despacho de email corporativo a administradores vía `EmailService` y `StockService`, validado con suite unitaria)*

7. [x] Al cargar una direccion, al seleccionar un pais, los inputs de provincia y ciudad quedan bloqueados, se debe habilitar la seleccion de provincia y ciudad al seleccionar un pais y actualizarse con los que corresponden a ese pais. *(Completado: mapeo dual `/api/locations` y `/dashboard/locations` en `LocationController`, apertura en `SecurityConfig`, control de errores JS en `address-form.html`, `profile.html`, `office-edit.html` y `provider-edit.html`, y verificado con test de integración)*

8. [x] Los iconos de la pagina de inicio de la tienda son ovalados en vez de circulares, se debe cambiar el estilo para que sean circulares o cuadrados y no ovalados. *(Completado: clase `.feature-icon-circle` en `styles.css` con ratio 1:1 (58px x 58px), `flex-shrink: 0` y `border-radius: 50%` en las 4 tarjetas de beneficios de `client/index.html`)*
