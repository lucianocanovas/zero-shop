# PATRON GENERAL DEL SISTEMA

## Indicaciones generales del sistema

- El carrito de compras se maneja del lado de la vista, no requiere un controlador ni un modelo en el backend. Se puede almacenar en la sesión del usuario o en el almacenamiento local del navegador.
- El sistema no requiere redirigir a una pagina nueva para crear nuevos recursos, se desplegara un formulario sobre la misma pagina de listado de recursos. Esto aplica para la creacion de productos, categorias, proveedores y ordenes de compra.
- El sistema no requiere redirigir a una pagina nueva para editar recursos existentes, se desplegara un formulario sobre la misma pagina de detalle del recurso. Esto aplica para la edicion de productos, categorias, proveedores y ordenes de compra.

## Comunicacion entre capas

- Capa de vista (/templates): Se encarga de la presentación de la información y la interacción con el usuario. Utiliza Thymeleaf para renderizar las vistas y mostrar los datos obtenidos de los controladores.
  - La capa de vista se comunica con la capa de controladores mediante solicitudes HTTP (GET, POST, PUT, DELETE) y recibe respuestas en forma de vistas renderizadas.

- Capa de controladores (/controller): Se encarga de manejar las solicitudes del usuario, procesar los datos y coordinar la interacción entre la capa de vista y la capa de servicios. Utiliza anotaciones de Spring Boot para definir rutas y métodos HTTP.
  - La capa de controladores se comunica con la capa de servicios mediante llamadas a métodos de los servicios, pasando los datos necesarios y recibiendo los resultados.

- Capa de servicios (/service): Se encarga de la lógica de negocio y la gestión de los datos. Utiliza anotaciones de Spring Boot para definir servicios y manejar transacciones. Se comunica con la capa de repositorios para acceder a la base de datos.
  - La capa de servicios se comunica con la capa de controladores mediante llamadas a métodos, devolviendo los resultados obtenidos o lanzando excepciones en caso de errores.

- Capa de repositorios (/repository): Se encarga de la persistencia de los datos y el acceso a la base de datos. Utiliza anotaciones de Spring Boot para definir repositorios y consultas personalizadas. Se comunica con la capa de servicios para realizar operaciones CRUD sobre las entidades.
  - La capa de repositorios se comunica con la capa de servicios mediante llamadas a métodos, devolviendo los resultados obtenidos o lanzando excepciones en caso de errores.
