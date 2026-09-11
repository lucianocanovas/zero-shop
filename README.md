# Zero Shop - E-Commerce Web

Plataforma de comercio electrónico desarrollada con **Java**, **Spring Boot** y **Thymeleaf**. Proyecto desarrollado con fines educativos bajo una arquitectura monolítica MVC con renderizado del lado del servidor (SSR) y autenticación basada en sesiones y roles.

---

## Tecnologías Principales

- **Lenguaje:** Java (compatible con Java 17+)
- **Framework:** Spring Boot (Web MVC, Data JPA, Security, Mail, Validation)
- **Motor de Plantillas:** Thymeleaf + Thymeleaf Extras Spring Security
- **Base de Datos:** PostgreSQL
- **Herramientas y Utilidades:** Maven, Lombok, Spring Dotenv, Spring DevTools

---

## Requisitos Previos

- **JDK 17** o superior instalado y configurado en el `JAVA_HOME`.
- **Apache Maven 3.8+** (o el wrapper de Maven).
- **PostgreSQL 14+** en ejecución local.
- IDE recomendado: IntelliJ IDEA, VS Code o Eclipse con el plugin de **Lombok** y *Annotation Processing* habilitado.

---

## Instalación y Configuración

### 1. Clonar el repositorio

```bash
git clone https://github.com/lucianocanovas/zero-shop.git
cd zero-shop
```

### 2. Configurar la Base de Datos (PostgreSQL)

Crea la base de datos `zero-shop`:

```sql
CREATE DATABASE "zero-shop";
```

Verifica o ajusta tus credenciales en `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/zero-shop?currentSchema=public
spring.datasource.username=postgres
spring.datasource.password=tu_password
```

### 3. Variables de Entorno (`.env`)

El proyecto utiliza variables de entorno para servicios externos (como el envío de correos). Copia el archivo de ejemplo en la raíz:

```bash
cp .env.example .env
```

Edita los valores en `.env` con tus credenciales de correo si deseas probar el servicio SMTP.

---

## Ejecución del Proyecto

1. **Compilar y descargar dependencias:**

   ```bash
   mvn clean install
   ```

2. **Ejecutar la aplicación:**

   ```bash
   mvn spring-boot:run
   ```

3. **Abrir en el navegador:**
   [http://localhost:8080](http://localhost:8080)

---

## Credenciales de Prueba por Defecto

Al arrancar por primera vez, el sistema inicializa automáticamente dos usuarios de prueba para facilitar el desarrollo:

| Rol | Correo | Contraseña | Acceso |
| --- | --- | --- | --- |
| **Administrador** | `admin@gmail.com` | `admin123` | Panel `/admin`, gestión de productos y usuarios |
| **Cliente** | `client@gmail.com` | `client123` | Navegación, perfil y compras |

---

## Estructura del Código

```text
src/main/java/ingsoftware/zeroshop/
 ├── config/       # Seguridad (Spring Security) e inicializadores de BD
 ├── controller/   # Controladores Web MVC (rutas y vistas)
 ├── entity/       # Entidades JPA (tablas de base de datos)
 ├── enums/        # Enums del dominio (roles, estados)
 ├── repository/   # Interfaces Spring Data JPA
 └── service/      # Lógica de negocio y servicios externos

src/main/resources/
 ├── static/       # CSS, JS e imágenes
 └── templates/    # Vistas HTML Thymeleaf
```
