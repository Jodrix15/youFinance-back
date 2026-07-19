# Guía de Seguridad — Proyecto Finanzas

## ¿Qué problema resuelve todo esto?

Imagina que tienes una app con datos financieros. Necesitas que:
- Solo usuarios registrados puedan acceder
- Un **administrador** pueda hacer cosas que un **usuario normal** no puede
- Nadie pueda falsificar su identidad una vez dentro
- La app no guarde "sesiones" en el servidor (porque queremos que escale bien)

Para eso usamos **Spring Security** + **JWT (JSON Web Token)**.

---

## El flujo completo en palabras simples

```
1. Usuario envía usuario + contraseña  →  POST /api/auth/login
2. El servidor verifica las credenciales
3. Si son correctas, genera un TOKEN (cadena cifrada con su identidad y rol)
4. El usuario guarda ese token
5. En cada petición siguiente, envía el token en la cabecera
6. El servidor verifica el token y sabe quién es sin consultar la base de datos
```

---

## Estructura de archivos creados

```
config/
  ApplicationConfig.java        → Define el codificador de contraseñas
  SecurityConfig.java           → Reglas de acceso (quién puede ir a dónde)
  JwtAuthenticationFilter.java  → Intercepta cada petición y valida el token
  DataInitializer.java          → Crea usuarios por defecto al arrancar

model/
  Role.java                     → Enum con los roles posibles
  User.java                     → Entidad usuario en base de datos

repository/
  UserRepository.java           → Acceso a datos de usuarios

service/
  JwtService.java               → Genera y valida tokens JWT
  UserService.java              → Lógica de negocio de usuarios

controller/
  AuthController.java           → Endpoints de login y registro

dto/
  LoginRequest.java             → Datos que llegan al login
  LoginResponse.java            → Datos que devuelve el login
  RegisterRequest.java          → Datos que llegan al registro
```

---

## Archivo por archivo

---

### `model/Role.java`

```java
public enum Role {
    ROLE_USER,
    ROLE_ADMIN
}
```

**¿Qué es?** Un enumerado (lista fija de valores posibles).

**¿Por qué?** Define los dos roles del sistema. El prefijo `ROLE_` es obligatorio en Spring Security — cuando luego escribes `.hasRole("ADMIN")`, Spring internamente busca `ROLE_ADMIN`.

**Anotaciones:** Ninguna, es un enum simple de Java.

---

### `model/User.java`

**¿Qué es?** La entidad que representa un usuario en la base de datos Y en el sistema de seguridad al mismo tiempo.

**¿Por qué implementa `UserDetails`?** Spring Security no sabe nada de tu clase `User`. Para que pueda trabajar con ella, exige que implementes la interfaz `UserDetails`, que le dice: "este objeto tiene usuario, contraseña, roles y estado de cuenta". Es como un contrato.

#### Anotaciones explicadas

| Anotación | Viene de | Qué hace |
|-----------|----------|----------|
| `@Entity` | Jakarta Persistence (JPA) | Le dice a Hibernate que esta clase es una tabla en la base de datos |
| `@Table(name = "users")` | JPA | Especifica el nombre de la tabla. Sin esto, usaría "user", que es palabra reservada en SQL |
| `@Id` | JPA | Marca el campo como clave primaria |
| `@GeneratedValue(strategy = IDENTITY)` | JPA | La base de datos genera el ID automáticamente (auto-increment) |
| `@Column(unique = true, nullable = false)` | JPA | El username debe ser único y no puede ser nulo |
| `@Enumerated(EnumType.STRING)` | JPA | Guarda el rol como texto ("ROLE_ADMIN") en vez de número (0, 1). Más legible |
| `@Getter` | Lombok | Genera automáticamente todos los métodos `getX()` |
| `@Setter` | Lombok | Genera automáticamente todos los métodos `setX()` |
| `@Builder` | Lombok | Permite construir el objeto así: `User.builder().username("x").build()` |
| `@NoArgsConstructor` | Lombok | Genera constructor vacío `User()` — JPA lo necesita obligatoriamente |
| `@AllArgsConstructor` | Lombok | Genera constructor con todos los campos — necesario para que `@Builder` funcione |

#### Métodos de `UserDetails` implementados

```java
getAuthorities()       → devuelve el rol del usuario como lista de permisos
isAccountNonExpired()  → ¿la cuenta ha caducado? → false = bloqueada
isAccountNonLocked()   → ¿la cuenta está bloqueada?
isCredentialsNonExpired() → ¿la contraseña ha caducado?
isEnabled()            → ¿la cuenta está activa?
```

Todos devuelven `true` en esta implementación básica. En un sistema real podrías añadir campos `boolean` en la entidad para controlar cada uno.

---

### `repository/UserRepository.java`

```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}
```

**¿Qué es?** La capa de acceso a datos. Una interfaz que Spring Data JPA implementa automáticamente.

**¿Por qué no tiene código?** Spring Data JPA lee el nombre del método y genera la consulta SQL. `findByUsername` se convierte en `SELECT * FROM users WHERE username = ?`. No hay que escribir SQL.

**`JpaRepository<User, Long>`** significa: "repositorio de entidades `User` cuya clave primaria es `Long`".

#### Anotaciones

Ninguna en la interfaz. La magia la hace Spring Data al escanear interfaces que extiendan `JpaRepository`.

---

### `service/JwtService.java`

**¿Qué es un JWT?** Un token en formato `xxxxx.yyyyy.zzzzz`:
- `xxxxx` = cabecera (algoritmo usado)
- `yyyyy` = payload (datos: usuario, expiración, rol...)
- `zzzzz` = firma (garantiza que nadie lo modificó)

**¿Por qué no necesitamos base de datos para validarlo?** Porque la firma está hecha con una clave secreta que solo el servidor conoce. Si alguien modifica el payload, la firma no coincide y el token es rechazado.

#### Métodos

| Método | Qué hace |
|--------|----------|
| `generateToken(user)` | Crea un JWT con el username, fecha de emisión y expiración |
| `extractUsername(token)` | Lee el username del payload del token |
| `isTokenValid(token, user)` | Comprueba que el username coincida y que no haya expirado |
| `getSigningKey()` | Convierte el secret del `application.yaml` en una clave criptográfica |

#### Anotaciones

| Anotación | Qué hace |
|-----------|----------|
| `@Service` | Registra la clase como bean de Spring (componente de lógica de negocio) |
| `@Value("${jwt.secret}")` | Inyecta el valor de `jwt.secret` del `application.yaml` en el campo |

#### En `application.yaml`

```yaml
jwt:
  secret: ZmluYW56YXMtc2VjcmV0...   # Clave base64, mínimo 256 bits para HS256
  expiration: 86400000                # 24 horas en milisegundos
```

---

### `service/UserService.java`

**¿Qué es?** El servicio que gestiona usuarios. Implementa `UserDetailsService`, que es la interfaz que Spring Security usa para cargar un usuario por su nombre.

**¿Por qué implementar `UserDetailsService`?** Cuando llega un login, Spring Security pregunta: "¿existe este usuario?" Para saber dónde buscar, exige que le des una implementación de `UserDetailsService`. Al implementar `loadUserByUsername`, le estás diciendo: "busca en la base de datos".

#### Anotaciones

| Anotación | Qué hace |
|-----------|----------|
| `@Service` | Registra como bean de servicio |
| `@RequiredArgsConstructor` | Lombok: genera constructor con todos los campos `final` (inyección de dependencias) |

---

### `config/ApplicationConfig.java`

**¿Qué es?** Configuración de beans auxiliares que necesita el sistema de seguridad.

**¿Por qué en clase separada?** Para evitar dependencias circulares. Si `PasswordEncoder` estuviera en `SecurityConfig`, Spring tendría problemas al construir los beans porque `SecurityConfig` necesita `UserService`, que necesita `PasswordEncoder`, que está en `SecurityConfig` → bucle infinito.

#### El bean `PasswordEncoder`

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

**BCrypt** es un algoritmo de hash diseñado específicamente para contraseñas. A diferencia de MD5 o SHA, es intencionalmente lento y añade un "sal" (dato aleatorio) para que dos contraseñas iguales generen hashes diferentes.

**Nunca guardamos la contraseña en texto plano.** El usuario escribe "admin123", BCrypt lo convierte en `$2a$10$...` y eso es lo que va a la base de datos.

#### Anotaciones

| Anotación | Qué hace |
|-----------|----------|
| `@Configuration` | Indica que la clase define beans de Spring |
| `@Bean` | El método devuelve un objeto que Spring gestiona y puede inyectar en otras clases |

---

### `config/SecurityConfig.java`

**¿Qué es?** El "portero" de la aplicación. Define qué rutas son públicas, cuáles requieren autenticación y cuáles requieren un rol concreto.

#### La cadena de filtros (`SecurityFilterChain`)

Spring Security funciona como una cadena de filtros. Cada petición HTTP pasa por todos los filtros en orden. Aquí configuramos esa cadena:

```java
.csrf(disable)                          // Desactivamos CSRF (no usamos cookies de sesión)
.authorizeHttpRequests(...)             // Reglas de acceso
.sessionManagement(STATELESS)           // Sin sesiones en servidor
.authenticationProvider(...)            // Cómo verificar credenciales
.addFilterBefore(jwtFilter, ...)        // Añade nuestro filtro JWT antes del estándar
```

**¿Por qué `STATELESS`?** En APIs REST con JWT no hay sesión. Cada petición es independiente y lleva su propio token. Esto permite escalar horizontalmente (múltiples servidores sin compartir sesiones).

**¿Por qué deshabilitar CSRF?** CSRF protege aplicaciones con formularios y cookies. Como usamos JWT en cabeceras, no aplica.

#### Reglas de acceso definidas

| Ruta | Acceso |
|------|--------|
| `/api/auth/**` | Público (login, registro) |
| `/api/admin/**` | Solo `ROLE_ADMIN` |
| Cualquier otra | Requiere estar autenticado |

#### Anotaciones

| Anotación | Qué hace |
|-----------|----------|
| `@Configuration` | Clase de configuración |
| `@EnableWebSecurity` | Activa el módulo de seguridad web de Spring |
| `@RequiredArgsConstructor` | Inyección por constructor (Lombok) |
| `@Bean` | En cada método que devuelve un bean gestionado por Spring |

---

### `config/JwtAuthenticationFilter.java`

**¿Qué es?** El filtro que intercepta CADA petición HTTP y comprueba si lleva un token JWT válido.

**¿Por qué `OncePerRequestFilter`?** Spring garantiza que este filtro se ejecuta exactamente una vez por petición (hay filtros que pueden ejecutarse varias veces en redireccionamientos).

#### Flujo del filtro

```
1. ¿Tiene cabecera "Authorization: Bearer xxxxx"?  →  No: dejar pasar sin autenticar
2. Extraer el token (quitar "Bearer ")
3. ¿Es válido el token?  →  No: dejar pasar sin autenticar (Spring Security lo bloqueará)
4. Cargar el usuario de la base de datos
5. Crear un objeto de autenticación y meterlo en el SecurityContext
6. Dejar pasar la petición
```

**`SecurityContext`** es donde Spring Security guarda la identidad del usuario autenticado durante el procesamiento de una petición. Una vez que pones la autenticación ahí, Spring sabe quién es el usuario en todo el código de esa petición.

#### Anotaciones

| Anotación | Qué hace |
|-----------|----------|
| `@Component` | Registra como bean de Spring (componente genérico) |
| `@RequiredArgsConstructor` | Inyección por constructor |
| `@NonNull` | Indica que el parámetro no puede ser nulo (anotación de documentación/validación) |

---

### `config/DataInitializer.java`

**¿Qué es?** Un componente que se ejecuta automáticamente cuando arranca la aplicación. Crea usuarios por defecto si no existen.

**¿Por qué `ApplicationRunner`?** Es una interfaz de Spring Boot. Al implementar `run()`, Spring la ejecuta después de que el contexto esté completamente inicializado, justo antes de que la app empiece a aceptar peticiones.

**¿Por qué no en `DataInitializer` directamente en la entidad o SQL?** Porque necesitamos codificar las contraseñas con BCrypt antes de guardarlas. Un fichero SQL no puede hacer eso.

#### Usuarios creados

| Usuario | Contraseña | Rol |
|---------|-----------|-----|
| `admin` | `admin123` | ROLE_ADMIN |
| `usuario` | `user123` | ROLE_USER |

#### Anotaciones

| Anotación | Qué hace |
|-----------|----------|
| `@Component` | Registra como bean genérico |
| `@RequiredArgsConstructor` | Inyección por constructor |

---

### `controller/AuthController.java`

**¿Qué es?** El punto de entrada HTTP para autenticación. Expone dos endpoints.

#### `POST /api/auth/login`

1. `authenticationManager.authenticate(...)` → Spring verifica usuario y contraseña contra la base de datos (usando `UserService.loadUserByUsername` + BCrypt)
2. Si falla, lanza `BadCredentialsException` automáticamente (Spring devuelve 401)
3. Si pasa, genera el JWT y lo devuelve

#### `POST /api/auth/register`

1. Comprueba que el username no exista
2. Codifica la contraseña con BCrypt
3. Guarda el usuario con rol `ROLE_USER`
4. Devuelve JWT directamente (el usuario queda logado al registrarse)

#### Anotaciones

| Anotación | Qué hace |
|-----------|----------|
| `@RestController` | Combina `@Controller` + `@ResponseBody`: los métodos devuelven JSON directamente |
| `@RequestMapping("/api/auth")` | Prefijo de ruta para todos los endpoints de la clase |
| `@RequiredArgsConstructor` | Inyección por constructor |
| `@PostMapping("/login")` | Mapea `POST /api/auth/login` a ese método |
| `@RequestBody` | Deserializa el JSON del cuerpo de la petición al objeto Java |

---

### DTOs (`dto/`)

**¿Qué es un DTO (Data Transfer Object)?** Un objeto simple que define qué datos entran o salen de un endpoint. Separar DTOs de entidades evita exponer campos internos (como el hash de la contraseña) y desacopla la API del modelo de datos.

| Clase | Uso | Campos |
|-------|-----|--------|
| `LoginRequest` | Entrada en `/login` | `username`, `password` |
| `RegisterRequest` | Entrada en `/register` | `username`, `password` |
| `LoginResponse` | Salida en ambos | `token`, `username`, `role` |

**`@Data` (Lombok)** = `@Getter` + `@Setter` + `@ToString` + `@EqualsAndHashCode` + `@RequiredArgsConstructor` en una sola anotación.

**`@Builder` en `LoginResponse`** permite construir la respuesta de forma fluida: `LoginResponse.builder().token(t).username(u).role(r).build()`.

---

## Diagrama del flujo de autenticación

```
Cliente                     Servidor
  |                             |
  |  POST /api/auth/login       |
  |  { username, password }     |
  | ─────────────────────────► |
  |                             |  AuthenticationManager verifica credenciales
  |                             |  UserService carga User de BD
  |                             |  BCrypt compara contraseñas
  |                             |  JwtService genera token
  |  200 OK                     |
  |  { token, username, role }  |
  | ◄───────────────────────── |
  |                             |
  |  GET /api/cualquier-ruta    |
  |  Authorization: Bearer xxx  |
  | ─────────────────────────► |
  |                             |  JwtAuthenticationFilter intercepta
  |                             |  JwtService extrae username del token
  |                             |  JwtService valida firma y expiración
  |                             |  SecurityContext guarda la autenticación
  |                             |  Controller procesa la petición
  |  200 OK                     |
  | ◄───────────────────────── |
```

---

## ¿Por qué no hay sesiones?

En una arquitectura tradicional con sesiones:
- El servidor guarda en memoria quién está logado
- El cliente recibe una cookie con el ID de sesión
- Cada petición el servidor busca esa sesión en memoria

**Problema:** Si tienes 3 servidores, la sesión solo existe en uno de ellos.

Con JWT:
- El servidor no guarda nada
- El token lleva toda la información necesaria
- Cualquier servidor puede validarlo con la clave secreta

---

## Resumen de las dependencias añadidas

| Dependencia | Para qué |
|-------------|----------|
| `spring-boot-starter-web` | Servidor HTTP embebido (Tomcat) + controladores REST |
| `spring-boot-starter-data-jpa` | Acceso a base de datos con Hibernate |
| `spring-boot-starter-security` | Toda la infraestructura de Spring Security |
| `jjwt-api/impl/jackson` | Librería para crear y validar tokens JWT |
| `mysql-connector-j` | Driver JDBC para conectar a MySQL |
| `lombok` | Reducir código repetitivo (getters, constructores, builders) |

---

## Configuración en `application.yaml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/finanzas   # Conexión a MySQL
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root
    password: ""
  jpa:
    hibernate:
      ddl-auto: create-drop     # Recrea las tablas al arrancar (solo para desarrollo)
    database-platform: org.hibernate.dialect.MySQLDialect

jwt:
  secret: "clave base64..."     # Clave para firmar tokens — CAMBIAR en producción
  expiration: 86400000          # Expiración del token: 24 horas en milisegundos
```

> **`ddl-auto: create-drop`** elimina y recrea todas las tablas cada vez que arranca la app. Útil en desarrollo, **peligroso en producción**. Para producción usar `validate` o `none`.