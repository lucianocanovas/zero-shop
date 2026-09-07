# Proyecto de E-Commerce Zero Shop

Este proyecto es un sitio web de comercio electrónico desarrollado con Java y Spring Boot. Proporciona una plataforma para que los usuarios puedan explorar y comprar productos en línea. El proyecto incluye funcionalidades como navegación por categorías, búsqueda de productos, carrito de compras y procesamiento de pagos.
Este proyecto tiene carácter educativo y no está destinado a ser utilizado en producción. Se recomienda a los desarrolladores que lo utilicen como referencia para aprender sobre el desarrollo de aplicaciones web con Java y Spring Boot.

## Dependencias

- Java 11 o superior
- Spring Boot 2.5 o superior
- Maven 3.6 o superior
- Acceso a una base de datos (se recomienda PostgreSQL)

## Instalación y Configuración

1. Clonar el repositorio:

   ```bash
   git clone https://github.com/lucianocanovas/zero-shop.git
   ```

2. Navegar al directorio del proyecto:

   ```bash
   cd zero-shop
   ```

3. Configurar la base de datos en `src/main/resources/application.properties` según tus necesidades.
(Se recomienda el uso de PostgreSQL como base de datos.)

4. Instalación y configuración de PostgreSQL en Linux:

```bash
sudo apt update

sudo apt install postgresql postgresql-contrib

sudo -i -u postgres

psql

CREATE DATABASE zero_shop;

CREATE USER zero_shop_user WITH ENCRYPTED PASSWORD 'your_password';

GRANT ALL PRIVILEGES ON DATABASE zero_shop TO zero_shop_user;

\q

exit
```

## Ejecución del Proyecto

1. Construir el proyecto utilizando Maven:

   ```bash
   mvn clean install
   ```

2. Ejecutar la aplicación:

   ```bash
   mvn spring-boot:run
   ```

3. Acceder a la aplicación en tu navegador web:

   ```text
   http://localhost:8080
   ```
