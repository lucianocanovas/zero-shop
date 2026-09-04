# Instrucciones para crear rutas

Este proyecto usa Spring Boot, Spring MVC, Spring Security y Thymeleaf. Una ruta nueva debe definirse en tres lugares cuando corresponda:

1. **Backend:** controlador Java que recibe la petición y devuelve una vista o una redirección.
2. **Seguridad:** regla en `SecurityConfig` que define si la ruta es pública, requiere sesión o requiere el rol `ADMIN`.
3. **Frontend:** enlace Thymeleaf con `th:href` o formulario con `th:action`.

No confíes únicamente en ocultar un enlace en HTML. La autorización debe existir también en Spring Security y, si procede, en el controlador.

## Tipos de rutas

### 1. Rutas públicas para clientes y visitantes

Son accesibles sin iniciar sesión. Ejemplos actuales:

- `/`
- `/products`
- `/login`
- `/register`
- `/styles/**`, `/assets/**` y `/scripts/**`

#### Backend

Añade el método a un controlador de vistas, por ejemplo `HomeController`:

```java
@GetMapping("/offers")
public String offers(Authentication authentication, Model model) {
    boolean loggedIn = isAuthenticated(authentication);
    model.addAttribute("loggedIn", loggedIn);
    model.addAttribute("isAdmin", loggedIn && hasRole(authentication, "ADMIN"));
    return "offers";
}
```

Crea la plantilla en:

```text
src/main/resources/templates/offers.html
```

#### Seguridad

Incluye la ruta exacta en `permitAll()`:

```java
.requestMatchers(
    "/",
    "/products",
    "/offers",
    "/login",
    "/register",
    "/register/**",
    "/styles/**",
    "/assets/**",
    "/scripts/**"
).permitAll()
```

No agregues una ruta de negocio a `permitAll()` si muestra datos privados, modifica información o forma parte del proceso de compra.

#### Frontend

Usa `th:href` para que Thymeleaf genere correctamente la URL:

```html
<a class="nav-link" th:href="@{/offers}">OFERTAS</a>
```

Para un formulario público:

```html
<form th:action="@{/newsletter}" method="post">
    <input type="email" name="email" required>
    <button type="submit">Suscribirme</button>
</form>
```

## 2. Rutas que requieren iniciar sesión

Estas rutas están disponibles para usuarios autenticados, sean `USER` o `ADMIN`. Ejemplos recomendados:

- `/checkout`: finalizar una compra.
- `/orders`: ver los pedidos activos del usuario.
- `/profile`: ver el perfil.

### Backend

Crea un controlador separado para el área de cliente cuando la funcionalidad crezca. El controlador debe comprobar la sesión y obtener los datos del usuario autenticado:

```java
@Controller
@RequestMapping("/account")
public class AccountController {

    @GetMapping("/orders")
    public String activeOrders(Authentication authentication, Model model) {
        if (!isAuthenticated(authentication)) {
            return "redirect:/login";
        }

        model.addAttribute("orders", orderService.findActiveByUserEmail(authentication.getName()));
        model.addAttribute("loggedIn", true);
        model.addAttribute("isAdmin", hasRole(authentication, "ADMIN"));
        return "account/orders";
    }
}
```

Para finalizar una compra:

```java
@PostMapping("/checkout")
public String checkout(Authentication authentication, @Valid CheckoutForm form,
                       BindingResult bindingResult) {
    if (!isAuthenticated(authentication)) {
        return "redirect:/login";
    }
    if (bindingResult.hasErrors()) {
        return "checkout";
    }

    orderService.createOrder(authentication.getName(), form);
    return "redirect:/account/orders";
}
```

El servicio debe usar el usuario autenticado del servidor. No tomes el email o el identificador del comprador desde un campo editable del formulario.

### Seguridad

Si se usa una convención por prefijo, agrega la regla antes de `anyRequest().authenticated()`:

```java
.requestMatchers("/account/**", "/checkout/**").authenticated()
```

La regla actual ya protege cualquier ruta que no esté en `permitAll()` mediante:

```java
.anyRequest().authenticated()
```

Aun así, declarar explícitamente las rutas privadas documenta la intención y evita errores cuando cambie la configuración.

### Frontend

Enlaza las rutas privadas desde plantillas, pero permite que Spring Security controle el acceso real:

```html
<a th:href="@{/account/orders}">MIS PEDIDOS</a>
<a th:href="@{/checkout}">FINALIZAR COMPRA</a>
```

Si el usuario no ha iniciado sesión, el enlace debe llevar a una ruta protegida y Spring Security lo redirigirá a `/login`. También puede mostrarse una alternativa visual:

```html
<a th:if="${loggedIn}" th:href="@{/account/orders}">MIS PEDIDOS</a>
<a th:unless="${loggedIn}" th:href="@{/login}">INICIAR SESIÓN</a>
```

## 3. Rutas exclusivas para administradores

Las rutas administrativas actuales están bajo `/admin/**`, por ejemplo:

- `/admin`
- `/admin/products`
- `/admin/users`
- `/admin/orders`
- `/admin/stock`
- `/admin/highlights`

### Backend

Usa un controlador administrativo con `@RequestMapping("/admin")`:

```java
@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("/reports")
    public String reports(Authentication authentication, Model model) {
        if (!isAuthenticated(authentication) || !hasRole(authentication, "ADMIN")) {
            return "redirect:/login";
        }

        model.addAttribute("loggedIn", true);
        model.addAttribute("isAdmin", true);
        model.addAttribute("reports", reportService.findAll());
        return "admin/reports";
    }
}
```

Crea la plantilla en:

```text
src/main/resources/templates/admin/reports.html
```

### Seguridad

Protege todo el prefijo administrativo:

```java
.requestMatchers("/admin/**").hasRole("ADMIN")
```

`hasRole("ADMIN")` busca la autoridad `ROLE_ADMIN`, que es la autoridad generada para el valor `ADMIN` del enum `Role`.

Si una ruta administrativa queda fuera de `/admin/**`, protégela explícitamente:

```java
.requestMatchers("/internal-reports/**").hasRole("ADMIN")
```

No uses solamente `isAdmin` en el modelo Thymeleaf como mecanismo de seguridad. Ese atributo solo controla la presentación del enlace.

### Frontend

Muestra enlaces administrativos únicamente cuando el modelo indique que el usuario tiene permisos:

```html
<ul class="nav-list admin" th:if="${isAdmin}">
    <li><a th:href="@{/admin}">DASHBOARD</a></li>
    <li><a th:href="@{/admin/products}">PRODUCTOS</a></li>
    <li><a th:href="@{/admin/reports}">REPORTES</a></li>
</ul>
```

Aunque el enlace no aparezca para un usuario normal, una petición manual a `/admin/reports` debe seguir siendo rechazada por Spring Security.

## Estructura recomendada

Para una funcionalidad nueva de pedidos:

```text
src/main/java/ingsoftware/zeroshop/controller/OrderController.java
src/main/java/ingsoftware/zeroshop/service/OrderService.java
src/main/java/ingsoftware/zeroshop/entity/Order.java
src/main/java/ingsoftware/zeroshop/repository/OrderRepository.java
src/main/resources/templates/account/orders.html
```

Rutas sugeridas:

| Ruta | Método | Acceso | Vista o acción |
|---|---|---|---|
| `/products` | `GET` | Público | Catálogo |
| `/checkout` | `GET` | Sesión requerida | Formulario de compra |
| `/checkout` | `POST` | Sesión requerida | Crear pedido |
| `/account/orders` | `GET` | Sesión requerida | Pedidos activos del usuario |
| `/admin` | `GET` | `ADMIN` | Dashboard principal |
| `/admin/products` | `GET` | `ADMIN` | Gestión de productos |

Usa `GET` para mostrar páginas y `POST`, `PUT` o `DELETE` para operaciones que cambian datos. Después de una operación exitosa, redirige a otra ruta con el patrón PRG (Post/Redirect/Get) para evitar envíos duplicados al actualizar el navegador.

## Checklist antes de terminar

- [ ] El mapping del controlador coincide exactamente con el enlace frontend.
- [ ] La plantilla existe dentro de `src/main/resources/templates`.
- [ ] Los enlaces usan `th:href` y los formularios usan `th:action`.
- [ ] La ruta pública está en `permitAll()`.
- [ ] La ruta de cliente no está en `permitAll()` y requiere autenticación.
- [ ] La ruta administrativa está bajo `/admin/**` o tiene `hasRole("ADMIN")` explícito.
- [ ] Las consultas de pedidos filtran por `authentication.getName()` para que un usuario no vea pedidos ajenos.
- [ ] El controlador carga los atributos que la plantilla necesita, como `loggedIn` e `isAdmin`.
- [ ] Se prueba el acceso como visitante, usuario normal y administrador.
- [ ] Se prueba también escribir la URL protegida directamente en el navegador.
- [ ] Se ejecutan las pruebas y se revisan los logs de Spring ante errores de Thymeleaf.
