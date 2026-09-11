# REPOSITORIOS DEL SISTEMA

## Reglas generales para el uso de repositorios

- Los repositorios se encargan de la persistencia de los datos y el acceso a la base de datos. Utilizan anotaciones de Spring Boot para definir repositorios y consultas personalizadas.
- Todas los repositorios implementan un metodo find(UUID id) que permite buscar un recurso por su identificador unico. Este metodo devuelve un Optional que puede contener el recurso encontrado o estar vacio si no se encuentra.
- Todos los repositorios que manejan entidades con borrado logico implementan un metodo findActive(UUID id) que permite buscar un recurso activo por su identificador unico. Este metodo devuelve un Optional que puede contener el recurso activo encontrado o estar vacio si no se encuentra o esta inactivo.
