# Documentación Técnica y Funcional - API Identity

**Proyecto:** api-identity  
**Repositorio:** Transversal-kl/api-identity  
**Rama:** Master  
**Fecha de Análisis:** 19 de noviembre de 2025  
**Versión:** 1.0.0-SNAPSHOT

---

## Tabla de Contenidos

1. [Resumen Ejecutivo](#resumen-ejecutivo)
2. [Stack Tecnológico](#stack-tecnológico)
3. [Arquitectura del Sistema](#arquitectura-del-sistema)
4. [Integraciones y Dependencias](#integraciones-y-dependencias)
5. [Modelo de Datos](#modelo-de-datos)
6. [Lógica de Negocio](#lógica-de-negocio)
7. [Endpoints de API](#endpoints-de-api)
8. [Seguridad](#seguridad)
9. [Análisis de Vulnerabilidades](#análisis-de-vulnerabilidades)
10. [Frontend](#frontend)
11. [Recomendaciones](#recomendaciones)

---

## Resumen Ejecutivo

**api-identity** es una API de gestión de identidad y control de acceso (IAM - Identity and Access Management) construida con Quarkus 3.26.1. El sistema proporciona funcionalidades completas de CRUD para usuarios, roles, grupos y permisos, con sincronización bidireccional entre Keycloak (servidor de autenticación y autorización) y PostgreSQL (base de datos relacional).

### Propósito del Sistema

El sistema actúa como una capa de abstracción y sincronización entre:
- **Keycloak**: Gestión de identidades, autenticación OAuth2/OIDC, y autorización basada en roles
- **PostgreSQL**: Persistencia de información extendida de usuarios y datos de perfil

### Características Principales

✅ **Gestión de Usuarios**: CRUD completo, búsqueda, gestión de imágenes de perfil  
✅ **Gestión de Roles**: Creación, actualización y eliminación de roles de realm  
✅ **Gestión de Grupos**: Organización jerárquica de usuarios  
✅ **Asignación de Permisos**: Roles a usuarios, roles a grupos, grupos a usuarios  
✅ **Sincronización Dual**: Keycloak ↔ PostgreSQL en tiempo real  
✅ **Arquitectura Reactiva**: Alto rendimiento con programación no bloqueante  
✅ **Seguridad**: Autenticación JWT con OIDC  
✅ **Documentación**: OpenAPI/Swagger integrado  

---

## Stack Tecnológico

### Lenguaje y Framework

| Componente | Tecnología | Versión |
|------------|-----------|---------|
| **Lenguaje** | Java | 21 |
| **Framework** | Quarkus | 3.26.1 |
| **Gestor de Dependencias** | Maven | 3.x |
| **Compilador** | Maven Compiler Plugin | 3.14.0 |

### Dependencias Principales

#### Core Quarkus
- **quarkus-arc**: Inyección de dependencias (CDI)
- **quarkus-rest**: JAX-RS RESTful endpoints
- **quarkus-rest-jackson**: Serialización/deserialización JSON
- **quarkus-smallrye-openapi**: Documentación OpenAPI 3.0

#### Persistencia y Datos
- **quarkus-hibernate-reactive-panache**: ORM reactivo con patrón Active Record
- **quarkus-reactive-pg-client**: Cliente reactivo de PostgreSQL
- **Database**: PostgreSQL (reactivo)

#### Seguridad
- **quarkus-oidc**: Autenticación OAuth2/OpenID Connect
- **keycloak-admin-client**: Cliente administrativo de Keycloak (26.0.0)
- **quarkus-hibernate-validator**: Validación de beans (Bean Validation)

#### Utilidades
- **lombok**: Reducción de código boilerplate (1.18.32)
- **smallrye-mutiny**: Programación reactiva

### Arquitectura de Despliegue

El proyecto incluye múltiples opciones de Dockerfiles:
- `Dockerfile.jvm`: Imagen JVM tradicional
- `Dockerfile.legacy-jar`: Imagen con JAR legacy
- `Dockerfile.native`: Compilación nativa con GraalVM
- `Dockerfile.native-micro`: Imagen nativa ultra-ligera

---

## Arquitectura del Sistema

### Patrón Arquitectónico

El sistema implementa una **arquitectura en capas** con el patrón **Repository-Service-Resource**:

```
┌─────────────────────────────────────────┐
│         Capa de Presentación            │
│    (Resources - JAX-RS Endpoints)       │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│         Capa de Servicio                │
│    (Services - Lógica de Negocio)       │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│      Capa de Repositorio                │
│  (Repositories - Acceso a Datos)        │
└──────┬────────────────────┬─────────────┘
       │                    │
┌──────▼──────┐      ┌─────▼─────────┐
│  PostgreSQL │      │   Keycloak    │
│  (Reactivo) │      │  Admin API    │
└─────────────┘      └───────────────┘
```

### Estructura de Paquetes

```
com.fv.billpay.api.identity/
├── dto/
│   ├── request/        # DTOs para peticiones entrantes
│   │   ├── UserRequestDto.java
│   │   ├── UserUpdateDto.java
│   │   ├── RoleRequestDto.java
│   │   ├── GroupRequestDto.java
│   │   └── ...
│   └── response/       # DTOs para respuestas
│       ├── UserResponseDto.java
│       ├── RoleResponseDto.java
│       ├── GroupResponseDto.java
│       └── PagedResponse.java
│
├── entity/             # Entidades JPA (PostgreSQL)
│   └── UserAccount.java
│
├── exception/          # Excepciones personalizadas
│   ├── GlobalExceptionMapper.java
│   ├── UserNotFoundException.java
│   ├── UserAlreadyExistsException.java
│   ├── InvalidUserDataException.java
│   └── KeycloakSyncException.java
│
├── mapper/             # Conversión entre entidades y DTOs
│   ├── UserMapper.java
│   ├── RoleMapper.java
│   ├── GroupMapper.java
│   └── GroupRoleMapper.java
│
├── repository/         # Acceso a datos
│   ├── UserAccountRepository.java (PostgreSQL - Reactivo)
│   ├── IUserKeycloakRepository.java
│   ├── IRoleRepository.java
│   ├── IGroupRepository.java
│   ├── IGroupRoleRepository.java
│   └── Impl/
│       ├── UserKeycloakRepositoryImpl.java
│       ├── RoleRepositoryImpl.java
│       ├── GroupRepositoryImpl.java
│       └── GroupRoleRepositoryImpl.java
│
├── resource/           # Endpoints REST (Controllers)
│   ├── UserResource.java
│   ├── RoleResource.java
│   ├── GroupResource.java
│   ├── UserRoleResource.java
│   ├── UserGroupResource.java
│   └── GroupRoleResource.java
│
├── service/            # Lógica de negocio
│   ├── IUserService.java
│   ├── IRoleService.java
│   ├── IGroupService.java
│   ├── IGroupRoleService.java
│   └── Impl/
│       ├── UserServiceImpl.java
│       ├── RoleServiceImpl.java
│       ├── GroupServiceImpl.java
│       └── GroupRoleServiceImpl.java
│
└── utils/              # Utilidades
    ├── KeycloakAdminProvider.java
    ├── UuidValidator.java
    └── Process.java
```

### Patrón Reactivo

El sistema utiliza **SmallRye Mutiny** (`Uni` y `Multi`) para programación reactiva no bloqueante:

- **Operaciones de PostgreSQL**: Totalmente reactivas con Hibernate Reactive Panache
- **Operaciones de Keycloak**: Síncronas (bloqueantes), envueltas en `Uni.createFrom().item()`
- **Composición**: Uso extensivo de operadores `.chain()`, `.map()`, `.invoke()` para flujos asincrónicos

---

## Integraciones y Dependencias

### 1. Base de Datos - PostgreSQL

#### Configuración

```properties
# PostgreSQL Reactive
quarkus.datasource.db-kind=postgresql
quarkus.datasource.username=${DB_USERNAME:postgres}
quarkus.datasource.password=${DB_PASSWORD:postgres}
quarkus.datasource.reactive.url=${DB_REACTIVE_URL:postgresql://localhost:5432/billpay_db}
quarkus.datasource.reactive.max-size=20

# Hibernate Reactive
quarkus.hibernate-orm.database.generation=update
quarkus.hibernate-orm.log.sql=true
```

#### Detalles de Conexión

- **Tipo**: PostgreSQL (Reactivo)
- **Base de Datos**: `billpay_db` (por defecto)
- **Puerto**: 5432
- **Pool de Conexiones**: Máximo 20 conexiones
- **Gestión de Esquema**: Actualización automática (`update`)

#### Tablas

Solo existe **una tabla** en PostgreSQL:

**Tabla: `user_account`**
```sql
CREATE TABLE user_account (
    id UUID PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255),
    first_name VARCHAR(25),
    last_name VARCHAR(25),
    profile_image BYTEA,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
```

**Nota**: Los roles, grupos y asignaciones se gestionan exclusivamente en Keycloak, no se persisten en PostgreSQL.

### 2. Servicio Externo - Keycloak Admin API

#### Configuración

```properties
# Keycloak Server
keycloak.auth-server-url=${KEYCLOAK_URL:http://localhost:8083/}
keycloak.realm=${KEYCLOAK_REALM:billpay_app}

# Service Account (Client Credentials Grant)
keycloak.client-id=${KEYCLOAK_CLIENT_ID:cli_billpay_app}
keycloak.client-secret=${KEYCLOAK_CLIENT_SECRET}

# OIDC para autenticación JWT
quarkus.oidc.enabled=true
quarkus.oidc.auth-server-url=${KEYCLOAK_URL}realms/${KEYCLOAK_REALM}
quarkus.oidc.client-id=${KEYCLOAK_CLIENT_ID}
quarkus.oidc.credentials.secret=${KEYCLOAK_CLIENT_SECRET}
quarkus.oidc.application-type=service
```

#### Tipo de Integración

- **Protocolo**: REST HTTP (Keycloak Admin REST API)
- **Autenticación**: OAuth2 Client Credentials Grant
- **Grant Type**: `client_credentials`
- **Cliente**: Service Account con permisos de `realm-management`

#### Operaciones con Keycloak

El sistema consume las siguientes APIs de Keycloak:

**Usuarios:**
- `POST /admin/realms/{realm}/users` - Crear usuario
- `PUT /admin/realms/{realm}/users/{id}` - Actualizar usuario
- `DELETE /admin/realms/{realm}/users/{id}` - Eliminar usuario
- `GET /admin/realms/{realm}/users/{id}` - Obtener usuario
- `GET /admin/realms/{realm}/users` - Listar usuarios

**Roles:**
- `POST /admin/realms/{realm}/roles` - Crear rol
- `PUT /admin/realms/{realm}/roles/{roleName}` - Actualizar rol
- `DELETE /admin/realms/{realm}/roles/{roleName}` - Eliminar rol
- `GET /admin/realms/{realm}/roles/{roleName}` - Obtener rol
- `GET /admin/realms/{realm}/roles` - Listar roles

**Grupos:**
- `POST /admin/realms/{realm}/groups` - Crear grupo
- `PUT /admin/realms/{realm}/groups/{id}` - Actualizar grupo
- `DELETE /admin/realms/{realm}/groups/{id}` - Eliminar grupo
- `GET /admin/realms/{realm}/groups/{id}` - Obtener grupo
- `GET /admin/realms/{realm}/groups` - Listar grupos

**Asignaciones:**
- `PUT /admin/realms/{realm}/users/{id}/groups/{groupId}` - Asignar grupo a usuario
- `POST /admin/realms/{realm}/users/{id}/role-mappings/realm` - Asignar roles a usuario
- `POST /admin/realms/{realm}/groups/{id}/role-mappings/realm` - Asignar roles a grupo

#### Resiliencia

La clase `KeycloakAdminProvider` implementa **retry automático** en caso de fallo de conexión:

```java
public RolesResource getRolesResource() {
    try {
        return keycloak.realm(realm).roles();
    } catch (Exception e) {
        // Retry con nueva conexión
        initializeKeycloakClient();
        return keycloak.realm(realm).roles();
    }
}
```

### 3. Servicios Expuestos (API REST)

La aplicación expone una **API REST** documentada con OpenAPI 3.0:

- **Puerto**: 8089
- **Base Path**: `/`
- **OpenAPI Spec**: `/openapi`
- **Swagger UI**: `/q/swagger-ui` (solo en modo dev)
- **Formato**: JSON
- **Autenticación**: Bearer Token (JWT)

---

## Modelo de Datos

### Entidad Principal: UserAccount

**Ubicación**: `com.fv.billpay.api.identity.entity.UserAccount`

```java
@Entity
@Table(name = "user_account")
public class UserAccount extends PanacheEntityBase {
    
    @Id
    private UUID id;                    // UUID del usuario (sincronizado con Keycloak)
    
    private String username;            // Nombre de usuario único
    private String email;               // Correo electrónico
    private String firstName;           // Nombre
    private String lastName;            // Apellido
    private byte[] profileImage;        // Imagen de perfil (BYTEA)
    private ZonedDateTime createdAt;    // Fecha de creación
}
```

### Modelo de Datos en Keycloak

El sistema gestiona las siguientes entidades en Keycloak (no persistidas en PostgreSQL):

#### 1. User (Usuario)
- `id` (UUID)
- `username` (String, único)
- `email` (String)
- `firstName` (String)
- `lastName` (String)
- `enabled` (Boolean)
- `emailVerified` (Boolean)
- `credentials` (Password)

#### 2. Role (Rol)
- `name` (String, único, clave primaria)
- `description` (String)
- `composite` (Boolean)

#### 3. Group (Grupo)
- `id` (UUID)
- `name` (String)
- `path` (String, ruta jerárquica)
- `subGroups` (List<Group>)

#### 4. Asignaciones
- **User → Groups** (relación N:N)
- **User → Roles** (relación N:N)
- **Group → Roles** (relación N:N)

### Flujo de Sincronización

```
┌──────────────┐          ┌──────────────┐
│   Keycloak   │          │  PostgreSQL  │
│              │          │              │
│  - Users     │◄────────►│ user_account │
│  - Roles     │          │              │
│  - Groups    │          └──────────────┘
│  - Mappings  │                ▲
└──────────────┘                │
       │                        │
       └────────────────────────┘
         Sincronización en
         cada operación CRUD
```

**Estrategia de Sincronización:**
1. **Crear**: Primero en Keycloak → Luego en PostgreSQL
2. **Actualizar**: Keycloak y PostgreSQL en paralelo
3. **Eliminar**: Keycloak y PostgreSQL (transaccional)
4. **Lectura**: Combinar datos de ambas fuentes

---

## Lógica de Negocio

### Dominio de Negocio

El sistema pertenece al dominio de **Gestión de Identidad y Acceso (IAM)** para la aplicación `billpay` (sistema de pagos de facturas).

### Casos de Uso Principales

#### 1. Gestión de Usuarios

**UC-001: Crear Usuario**
- **Actor**: Administrador con rol `admin_users`
- **Flujo**:
  1. Validar datos del usuario (username, email, password con política de complejidad)
  2. Crear usuario en Keycloak con credenciales
  3. Sincronizar usuario en PostgreSQL
  4. Retornar UserResponseDto

**UC-002: Actualizar Usuario**
- **Actor**: Administrador con rol `admin_users`
- **Flujo**:
  1. Verificar existencia del usuario en PostgreSQL
  2. Actualizar datos en Keycloak
  3. Sincronizar cambios en PostgreSQL
  4. Retornar usuario actualizado

**UC-003: Gestión de Imagen de Perfil**
- **Actor**: Administrador con rol `admin_users`
- **Flujo**:
  1. Recibir archivo multipart (imagen)
  2. Validar formato y tamaño
  3. Convertir a bytes
  4. Persistir en PostgreSQL (campo BYTEA)
  5. Retornar confirmación

#### 2. Gestión de Roles

**UC-004: Crear Rol**
- **Actor**: Administrador con rol `admin_role`
- **Flujo**:
  1. Validar nombre de rol (formato: `^[a-zA-Z0-9_-]{3,50}$`)
  2. Crear rol en Keycloak (realm role)
  3. Retornar RoleResponseDto

**UC-005: Asignar Roles a Usuario**
- **Actor**: Administrador con rol `admin_users`
- **Flujo**:
  1. Verificar existencia del usuario
  2. Obtener roles disponibles en Keycloak
  3. Asignar roles realm al usuario
  4. Retornar lista de roles asignados

#### 3. Gestión de Grupos

**UC-006: Crear Grupo**
- **Actor**: Administrador con rol `admin_groups`
- **Flujo**:
  1. Validar nombre de grupo
  2. Crear grupo en Keycloak (con ruta jerárquica)
  3. Retornar GroupResponseDto

**UC-007: Asignar Roles a Grupo**
- **Actor**: Administrador con rol `admin_groups`
- **Flujo**:
  1. Verificar existencia del grupo
  2. Asignar roles realm al grupo
  3. Usuarios del grupo heredan roles automáticamente
  4. Retornar lista de roles del grupo

#### 4. Gestión de Permisos

**UC-008: Asignar Grupos a Usuario**
- **Actor**: Administrador con rol `admin_users`
- **Flujo**:
  1. Verificar existencia de usuario y grupos
  2. Unir usuario a grupos en Keycloak
  3. Usuario hereda roles de los grupos
  4. Retornar grupos asignados

### Reglas de Negocio

#### RN-001: Validación de Password
```java
@Pattern(
    regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
    message = "La contraseña debe contener al menos: 1 mayúscula, 1 minúscula, 1 número y 1 carácter especial"
)
```
- Mínimo 8 caracteres
- Al menos 1 letra mayúscula
- Al menos 1 letra minúscula
- Al menos 1 número
- Al menos 1 carácter especial (`@$!%*?&`)

#### RN-002: Validación de Username
```java
@Pattern(
    regexp = "^[a-zA-Z0-9._-]+$",
    message = "El username solo puede contener letras, números, puntos, guiones y guiones bajos"
)
@Size(min = 3, max = 255)
```

#### RN-003: Validación de Nombre de Rol
```java
@Pattern(
    regexp = "^[a-zA-Z0-9_-]{3,50}$",
    message = "Formato de rol inválido. Solo alfanuméricos, guiones y guiones bajos (3-50 caracteres)"
)
```

#### RN-004: Unicidad de Username
- El username debe ser único en todo el sistema
- Validado tanto en PostgreSQL (constraint UNIQUE) como en Keycloak

#### RN-005: Paginación
- Tamaño de página por defecto: 10 elementos
- Tamaño mínimo: 1
- Tamaño máximo: 100

#### RN-006: Control de Acceso Basado en Roles (RBAC)

| Recurso | Operación | Roles Requeridos |
|---------|-----------|------------------|
| `/users` | Todas | `admin_users` |
| `/roles` | Todas | `admin_role` |
| `/groups` | Todas | `admin_groups` |

### Ubicación de la Lógica

#### Capa de Servicio (Lógica de Negocio)

**Ejemplo: `UserServiceImpl.createUser()`**

```java
@WithTransaction
public Uni<UserResponseDto> createUser(UserRequestDto userRequestDto) {
    return Uni.createFrom().item(() -> {
        // 1. Crear en Keycloak (bloqueante)
        String userId = userKeycloakRepository.createUser(userRequestDto);
        UserRepresentation keycloakUser = userKeycloakRepository.getUserById(userId);
        return UserMapper.fromKeycloakUser(keycloakUser);
    })
    .chain(userAccount -> {
        // 2. Sincronizar en PostgreSQL (reactivo)
        return userAccountRepository.persist(userAccount)
            .map(persisted -> {
                UserRepresentation keycloakUser = userKeycloakRepository.getUserById(
                    persisted.getId().toString()
                );
                return UserMapper.toResponseDto(persisted, keycloakUser);
            });
    })
    .onFailure().transform(error -> {
        // Manejo de errores personalizado
        // ...
    });
}
```

**Características**:
- Uso de `@WithTransaction` para garantizar atomicidad en PostgreSQL
- Composición reactiva con `.chain()` para operaciones secuenciales
- Manejo de errores con transformación de excepciones
- Logging estructurado

#### Capa de Repositorio (Acceso a Datos)

**PostgreSQL - Reactivo:**
```java
@ApplicationScoped
public class UserAccountRepository implements PanacheRepositoryBase<UserAccount, UUID> {
    
    public Uni<UserAccount> findByUsername(String username) {
        return find("username", username).firstResult();
    }
    
    public Uni<List<UserAccount>> findAllPaginated(int page, int size) {
        return findAll().page(Page.of(page, size)).list();
    }
}
```

**Keycloak - Síncrono:**
```java
@ApplicationScoped
public class UserKeycloakRepositoryImpl implements IUserKeycloakRepository {
    
    public String createUser(UserRequestDto userRequestDto) {
        UsersResource usersResource = getUsersResource();
        UserRepresentation user = new UserRepresentation();
        // ... configurar usuario
        Response response = usersResource.create(user);
        // ... manejar respuesta
    }
}
```

### Manejo de Excepciones

El sistema utiliza excepciones personalizadas del dominio:

| Excepción | Código HTTP | Escenario |
|-----------|-------------|-----------|
| `UserNotFoundException` | 404 | Usuario no encontrado |
| `UserAlreadyExistsException` | 409 | Username o email duplicado |
| `InvalidUserDataException` | 400 | Datos de entrada inválidos |
| `KeycloakSyncException` | 500 | Error en sincronización con Keycloak |

**Mapeo Global de Excepciones:**

La clase `GlobalExceptionMapper` implementa el patrón **Exception Translation**:

```java
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {
    @Override
    public Response toResponse(Exception exception) {
        // Mapeo de excepciones de dominio a respuestas HTTP
        // Retorna JSON estructurado con código de error y mensaje
    }
}
```

---

## Endpoints de API

### Configuración General

- **Base URL**: `http://localhost:8089`
- **Content-Type**: `application/json`
- **Autenticación**: Bearer Token (JWT)
- **Documentación**: OpenAPI 3.0 en `/openapi`

### 1. Gestión de Usuarios (`/users`)

#### POST /users
**Crear usuario**
- **Rol Requerido**: `admin_users`
- **Request Body**:
```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "password": "SecureP@ss123",
  "enabled": true
}
```
- **Response**: `201 Created`
```json
{
  "id": "a1b2c3d4-...",
  "username": "john_doe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "enabled": true,
  "createdAt": "2025-11-19T10:30:00Z"
}
```

#### PUT /users/{userId}
**Actualizar usuario**
- **Rol Requerido**: `admin_users`
- **Path Param**: `userId` (UUID)
- **Response**: `200 OK`

#### DELETE /users/{userId}
**Eliminar usuario**
- **Rol Requerido**: `admin_users`
- **Response**: `200 OK`

#### GET /users/{userId}
**Obtener usuario por ID**
- **Rol Requerido**: `admin_users`
- **Response**: `200 OK`

#### GET /users
**Listar usuarios con paginación**
- **Rol Requerido**: `admin_users`
- **Query Params**:
  - `page` (default: 0)
  - `size` (default: 20)
- **Response**: `200 OK` (Array de UserResponseDto)

#### GET /users/search?username={username}
**Buscar usuarios por username**
- **Rol Requerido**: `admin_users`
- **Response**: `200 OK` (Array de UserResponseDto)

#### PUT /users/{userId}/profile-image
**Actualizar imagen de perfil**
- **Rol Requerido**: `admin_users`
- **Content-Type**: `multipart/form-data`
- **Form Param**: `image` (FileUpload)
- **Response**: `200 OK`

#### GET /users/{userId}/profile-image
**Obtener imagen de perfil**
- **Rol Requerido**: `admin_users`
- **Response**: `200 OK` (image/jpeg, image/png, image/gif)

#### DELETE /users/{userId}/profile-image
**Eliminar imagen de perfil**
- **Rol Requerido**: `admin_users`
- **Response**: `200 OK`

### 2. Gestión de Roles (`/roles`)

#### POST /roles
**Crear rol**
- **Rol Requerido**: `admin_role`
- **Request Body**:
```json
{
  "name": "viewer",
  "description": "Usuario con permisos de solo lectura"
}
```
- **Response**: `200 OK`

#### PUT /roles/{roleName}
**Actualizar rol**
- **Rol Requerido**: `admin_role`
- **Path Param**: `roleName` (String)
- **Response**: `200 OK`

#### DELETE /roles/{roleName}
**Eliminar rol**
- **Rol Requerido**: `admin_role`
- **Response**: `200 OK`

#### GET /roles/{roleName}
**Obtener rol por nombre**
- **Rol Requerido**: `admin_role`
- **Response**: `200 OK`

#### GET /roles
**Listar roles con paginación**
- **Rol Requerido**: `admin_role`
- **Query Params**:
  - `page` (default: 0)
  - `size` (default: 10, max: 100)
- **Response**: `200 OK` (PagedResponse<RoleResponseDto>)

### 3. Gestión de Grupos (`/groups`)

#### POST /groups
**Crear grupo**
- **Rol Requerido**: `admin_groups`
- **Request Body**:
```json
{
  "name": "developers",
  "path": "/developers"
}
```
- **Response**: `200 OK`

#### PUT /groups/{groupId}
**Actualizar grupo**
- **Rol Requerido**: `admin_groups`
- **Response**: `200 OK`

#### DELETE /groups/{groupId}
**Eliminar grupo**
- **Rol Requerido**: `admin_groups`
- **Response**: `200 OK`

#### GET /groups/{groupId}
**Obtener grupo por ID**
- **Rol Requerido**: `admin_groups`
- **Response**: `200 OK`

#### GET /groups/name/{groupName}
**Obtener grupo por nombre**
- **Rol Requerido**: `admin_groups`
- **Response**: `200 OK`

#### GET /groups
**Listar grupos con paginación**
- **Rol Requerido**: `admin_groups`
- **Query Params**:
  - `page` (default: 0)
  - `size` (default: 10, max: 100)
- **Response**: `200 OK` (PagedResponse<GroupResponseDto>)

### 4. Asignación de Roles a Usuarios (`/users/{userId}/roles`)

#### POST /users/{userId}/roles
**Asignar roles a usuario**
- **Rol Requerido**: `admin_users`
- **Request Body**:
```json
{
  "roleNames": ["viewer", "editor"]
}
```
- **Response**: `200 OK` (Lista de UserRoleResponseDto)

#### DELETE /users/{userId}/roles
**Remover roles de usuario**
- **Rol Requerido**: `admin_users`
- **Response**: `200 OK`

#### GET /users/{userId}/roles
**Obtener roles de usuario**
- **Rol Requerido**: `admin_users`
- **Response**: `200 OK` (Lista de UserRoleResponseDto)

### 5. Asignación de Grupos a Usuarios (`/users/{userId}/groups`)

#### POST /users/{userId}/groups
**Asignar grupos a usuario**
- **Rol Requerido**: `admin_users`
- **Request Body**:
```json
{
  "groupIds": ["uuid-1", "uuid-2"]
}
```
- **Response**: `200 OK` (Lista de UserGroupResponseDto)

#### DELETE /users/{userId}/groups
**Remover grupos de usuario**
- **Rol Requerido**: `admin_users`
- **Response**: `200 OK`

#### GET /users/{userId}/groups
**Obtener grupos de usuario**
- **Rol Requerido**: `admin_users`
- **Response**: `200 OK` (Lista de UserGroupResponseDto)

### 6. Asignación de Roles a Grupos (`/groups/{groupId}/roles`)

#### POST /groups/{groupId}/roles
**Asignar roles a grupo**
- **Rol Requerido**: `admin_groups`
- **Request Body**:
```json
{
  "roleNames": ["viewer"]
}
```
- **Response**: `200 OK`

#### DELETE /groups/{groupId}/roles
**Remover roles de grupo**
- **Rol Requerido**: `admin_groups`
- **Response**: `200 OK`

#### GET /groups/{groupId}/roles
**Obtener roles de grupo**
- **Rol Requerido**: `admin_groups`
- **Response**: `200 OK`

#### GET /groups/{groupId}/roles/available
**Obtener roles disponibles para asignar al grupo**
- **Rol Requerido**: `admin_groups`
- **Response**: `200 OK`

---

## Seguridad

### Autenticación

**Mecanismo**: OAuth2 / OpenID Connect (OIDC)

El sistema utiliza **Keycloak** como Identity Provider:

```properties
quarkus.oidc.enabled=true
quarkus.oidc.auth-server-url=${KEYCLOAK_URL}realms/${KEYCLOAK_REALM}
quarkus.oidc.client-id=${KEYCLOAK_CLIENT_ID}
quarkus.oidc.credentials.secret=${KEYCLOAK_CLIENT_SECRET}
quarkus.oidc.application-type=service
```

**Flujo de Autenticación**:
1. Cliente obtiene token JWT de Keycloak usando Client Credentials Grant
2. Cliente incluye token en header: `Authorization: Bearer {token}`
3. Quarkus valida token con la clave pública de Keycloak
4. Si es válido, extrae roles y permite acceso

### Autorización

**Modelo**: Role-Based Access Control (RBAC)

Cada endpoint está protegido con la anotación `@RolesAllowed`:

```java
@RolesAllowed({"admin_users"})
public Uni<Response> createUser(UserRequestDto userRequestDto) {
    // ...
}
```

**Roles del Sistema**:

| Rol | Descripción | Permisos |
|-----|-------------|----------|
| `admin_users` | Administrador de usuarios | CRUD usuarios, asignar roles/grupos |
| `admin_role` | Administrador de roles | CRUD roles |
| `admin_groups` | Administrador de grupos | CRUD grupos, asignar roles a grupos |
| `viewer` | Visualizador | Solo lectura (si se implementa) |

### Validación de Entrada

**Bean Validation (Jakarta Validation)**:

```java
@NotBlank(message = "El username es obligatorio")
@Size(min = 3, max = 255)
@Pattern(regexp = "^[a-zA-Z0-9._-]+$")
private String username;

@Email(message = "El email debe tener un formato válido")
private String email;
```

**Validación de UUID**:
```java
public class UuidValidator {
    public static UUID parseUuid(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new InvalidUserDataException("ID inválido: " + id);
        }
    }
}
```

### Headers de Seguridad

```properties
quarkus.http.auth.policy.role-policy.roles-allowed=admin,role-manager,viewer
keycloak.ssl-required=external
```

### Service Account en Keycloak

El cliente `cli_billpay_app` es un **Service Account** con permisos especiales:

**Permisos Requeridos en Keycloak**:
- `realm-management`: `manage-users`
- `realm-management`: `manage-realm`
- `realm-management`: `view-users`
- `realm-management`: `view-realm`
- `realm-management`: `manage-clients`

---

## Análisis de Vulnerabilidades

### ✅ Buenas Prácticas Implementadas

#### 1. Externalización de Credenciales
✅ **Todas las credenciales se obtienen de variables de entorno**:

```properties
keycloak.client-secret=${KEYCLOAK_CLIENT_SECRET}
quarkus.datasource.username=${DB_USERNAME:postgres}
quarkus.datasource.password=${DB_PASSWORD:postgres}
```

**Comentario en código**:
```properties
# IMPORTANTE: Las credenciales deben estar en variables de entorno, NO hardcodeadas
```

#### 2. Archivo de Ejemplo
✅ Se incluye `.env.example` (verificar si contiene placeholders, no valores reales)

#### 3. Validación de Entrada Robusta
✅ Uso extensivo de Bean Validation con patrones regex seguros

#### 4. Manejo de Excepciones Seguro
✅ No se exponen stack traces en producción:

```java
private boolean isDevelopment() {
    return "dev".equalsIgnoreCase(profile) || "test".equalsIgnoreCase(profile);
}

// En producción: mensajes genéricos
String errorMessage = isDevelopment() 
    ? exception.getMessage() + " (" + exception.getClass().getSimpleName() + ")"
    : "Error interno del servidor";
```

#### 5. SQL Injection
✅ **Inmune a SQL Injection**: Uso de Hibernate Reactive Panache con consultas parametrizadas

#### 6. Passwords
✅ Passwords nunca se almacenan en PostgreSQL, solo en Keycloak (hasheado con bcrypt)

### ⚠️ Recomendaciones de Seguridad

#### 1. Logging de Contraseñas
⚠️ **Riesgo Bajo**: Verificar que el logging no registre passwords

**Recomendación**:
```java
log.info("Creando usuario: {}", userRequestDto.getUsername()); // ✅ Correcto
// NO: log.info("Request: {}", userRequestDto); // ❌ Podría loggear password
```

#### 2. Validación de Tamaño de Imagen
⚠️ **Riesgo Medio**: No hay validación explícita del tamaño de imagen de perfil

**Recomendación**:
```java
if (imageBytes.length > 5 * 1024 * 1024) { // 5 MB
    throw new InvalidUserDataException("La imagen no puede exceder 5 MB");
}
```

#### 3. Rate Limiting
⚠️ **Riesgo Medio**: No hay protección contra ataques de fuerza bruta

**Recomendación**: Implementar rate limiting con:
- `quarkus-bucket4j` o
- Keycloak Brute Force Protection

#### 4. CORS
⚠️ **Verificar**: No se observa configuración de CORS en `application.properties`

**Recomendación**:
```properties
quarkus.http.cors=true
quarkus.http.cors.origins=https://app.billpay.com
quarkus.http.cors.methods=GET,POST,PUT,DELETE
```

#### 5. HTTPS en Producción
⚠️ **Crítico**: Asegurar que en producción se use HTTPS

**Recomendación**:
```properties
# Producción
quarkus.http.ssl.certificate.key-store-file=keystore.jks
quarkus.http.ssl.certificate.key-store-password=${KEYSTORE_PASSWORD}
```

#### 6. Timeout de Conexión de PostgreSQL
⚠️ **Verificar**: No se observa configuración de timeouts

**Recomendación**:
```properties
quarkus.datasource.reactive.idle-timeout=10m
quarkus.datasource.reactive.connection-timeout=5s
```

### 🔒 Resumen de Seguridad

| Aspecto | Estado | Nivel de Riesgo |
|---------|--------|-----------------|
| Credenciales externalizadas | ✅ Implementado | Ninguno |
| Autenticación JWT | ✅ Implementado | Ninguno |
| Autorización RBAC | ✅ Implementado | Ninguno |
| Validación de entrada | ✅ Implementado | Ninguno |
| SQL Injection | ✅ Protegido | Ninguno |
| Password policy | ✅ Implementado | Ninguno |
| Logging de passwords | ⚠️ Verificar | Bajo |
| Validación tamaño imagen | ❌ No implementado | Medio |
| Rate limiting | ❌ No implementado | Medio |
| CORS | ⚠️ Verificar | Medio |
| HTTPS | ⚠️ Configurar en prod | Alto |

---

## Frontend

### Análisis de Frontend

**Conclusión**: **Este proyecto NO tiene frontend**

#### Evidencia

1. **No se encontraron archivos frontend**:
   - No existen archivos `.html`, `.js`, `.jsx`, `.ts`, `.tsx`, `.css`, `.vue`, `.angular`
   - No existe `package.json` (Node.js)
   - No existen carpetas típicas de frontend: `public/`, `static/`, `assets/`, `components/`

2. **Tipo de aplicación**:
   - Es una **API REST pura** (backend)
   - Configurado como `quarkus.oidc.application-type=service`

3. **Interfaz de usuario**:
   - La única interfaz disponible es **Swagger UI** en modo desarrollo (`/q/swagger-ui`)
   - Swagger UI es solo para testing/documentación, no es parte de la aplicación

### Arquitectura del Proyecto

```
┌──────────────────────────────────────────────┐
│          Frontend (Separado)                 │
│    Aplicación web/móvil de terceros          │
│    Consume API REST vía HTTP + JWT           │
└────────────────┬─────────────────────────────┘
                 │
                 │ HTTP/JSON
                 │ Bearer Token
                 ▼
┌──────────────────────────────────────────────┐
│      api-identity (Este proyecto)            │
│      API REST - Backend Only                 │
│                                              │
│  ┌─────────────────────────────────────┐    │
│  │  JAX-RS Resources                   │    │
│  │  (REST Endpoints)                   │    │
│  └──────────────┬──────────────────────┘    │
│                 │                            │
│  ┌──────────────▼──────────────────────┐    │
│  │  Services (Lógica de Negocio)       │    │
│  └──────────────┬──────────────────────┘    │
│                 │                            │
│  ┌──────────────▼──────────────────────┐    │
│  │  Repositories (Acceso a Datos)      │    │
│  └──────────────┬──────────────────────┘    │
└─────────────────┼────────────────────────────┘
                  │
      ┌───────────┴──────────┐
      ▼                      ▼
┌──────────┐          ┌─────────────┐
│PostgreSQL│          │  Keycloak   │
└──────────┘          └─────────────┘
```

### Consumo de la API

La API está diseñada para ser consumida por:
- **Aplicaciones web** (React, Angular, Vue.js, etc.)
- **Aplicaciones móviles** (iOS, Android)
- **Otros microservicios**
- **Sistemas externos** con autenticación JWT

**Ejemplo de consumo desde JavaScript**:
```javascript
// 1. Obtener token de Keycloak
const tokenResponse = await fetch('http://keycloak:8083/realms/billpay_app/protocol/openid-connect/token', {
  method: 'POST',
  headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
  body: new URLSearchParams({
    grant_type: 'password',
    client_id: 'frontend-app',
    username: 'user@example.com',
    password: 'password123'
  })
});
const { access_token } = await tokenResponse.json();

// 2. Consumir API con token
const usersResponse = await fetch('http://localhost:8089/users', {
  headers: {
    'Authorization': `Bearer ${access_token}`,
    'Content-Type': 'application/json'
  }
});
const users = await usersResponse.json();
```

---

## Recomendaciones

### 1. Mejoras de Arquitectura

#### 1.1 Implementar Cache
**Problema**: Cada consulta de usuario requiere dos llamadas (PostgreSQL + Keycloak)

**Solución**:
```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-cache</artifactId>
</dependency>
```

```java
@CacheResult(cacheName = "users")
public Uni<UserResponseDto> getUserById(String userId) {
    // ...
}
```

#### 1.2 Implementar Health Checks
```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-smallrye-health</artifactId>
</dependency>
```

```java
@Liveness
public class KeycloakHealthCheck implements HealthCheck {
    @Override
    public HealthCheckResponse call() {
        // Verificar conexión con Keycloak
    }
}
```

#### 1.3 Métricas y Observabilidad
```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-micrometer-registry-prometheus</artifactId>
</dependency>
```

### 2. Mejoras de Seguridad

#### 2.1 Implementar Secrets Management
**Recomendación**: Usar HashiCorp Vault o AWS Secrets Manager

```properties
quarkus.vault.url=http://localhost:8200
quarkus.vault.kv-secret-engine-version=2
```

#### 2.2 Implementar Auditoría
**Crear entidad `AuditLog`**:
```java
@Entity
public class AuditLog {
    private String action;      // CREATE_USER, DELETE_ROLE, etc.
    private String userId;      // Usuario que realizó la acción
    private String targetId;    // ID del recurso afectado
    private ZonedDateTime timestamp;
    private String ipAddress;
    private Map<String, Object> metadata;
}
```

#### 2.3 Implementar Rate Limiting
```xml
<dependency>
    <groupId>io.quarkiverse.bucket4j</groupId>
    <artifactId>quarkus-bucket4j</artifactId>
</dependency>
```

### 3. Mejoras de Rendimiento

#### 3.1 Operaciones en Lote
**Implementar**:
```java
public Uni<List<UserResponseDto>> createUsers(List<UserRequestDto> users) {
    // Crear usuarios en lote
}
```

#### 3.2 Paginación Cursor-Based
**En lugar de offset-based**, para mejor rendimiento en datasets grandes:
```java
public Uni<CursorPage<UserResponseDto>> getUsersCursor(String cursor, int size) {
    // ...
}
```

### 4. Mejoras de DevOps

#### 4.1 CI/CD Pipeline
**Archivo**: `.github/workflows/ci.yml`
```yaml
name: CI/CD
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Build with Maven
        run: ./mvnw clean package
      - name: Run tests
        run: ./mvnw test
```

#### 4.2 Docker Compose para Desarrollo
**Archivo**: `docker-compose.yml`
```yaml
version: '3.8'
services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: billpay_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
      
  keycloak:
    image: quay.io/keycloak/keycloak:26.0.0
    environment:
      KEYCLOAK_ADMIN: admin
      KEYCLOAK_ADMIN_PASSWORD: admin
    ports:
      - "8083:8080"
    command: start-dev
```

#### 4.3 Tests de Integración
```java
@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
public class UserResourceIT {
    
    @Test
    public void testCreateUser() {
        given()
            .contentType(ContentType.JSON)
            .body(new UserRequestDto(...))
        .when()
            .post("/users")
        .then()
            .statusCode(201);
    }
}
```

### 5. Documentación

#### 5.1 Mejorar OpenAPI
```java
@OpenAPIDefinition(
    info = @Info(
        title = "API Identity - BillPay",
        version = "1.0.0",
        description = "API de gestión de identidad y control de acceso",
        contact = @Contact(
            name = "Equipo BillPay",
            email = "support@billpay.com"
        )
    ),
    security = @SecurityRequirement(name = "bearer-jwt")
)
```

#### 5.2 Documentación de Arquitectura
**Crear**: `docs/architecture.md` con diagramas C4

#### 5.3 README Mejorado
- Prerrequisitos
- Instrucciones de instalación paso a paso
- Variables de entorno requeridas
- Ejemplos de uso de la API
- Troubleshooting

### 6. Mejoras de Testing

#### 6.1 Coverage Mínimo
**Objetivo**: 80% de cobertura de código

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <configuration>
        <rules>
            <rule>
                <element>BUNDLE</element>
                <limits>
                    <limit>
                        <counter>LINE</counter>
                        <value>COVEREDRATIO</value>
                        <minimum>0.80</minimum>
                    </limit>
                </limits>
            </rule>
        </rules>
    </configuration>
</plugin>
```

#### 6.2 Tests de Contrato (Contract Testing)
**Para garantizar compatibilidad con consumidores**:
```xml
<dependency>
    <groupId>au.com.dius.pact.consumer</groupId>
    <artifactId>junit5</artifactId>
</dependency>
```

---

## Conclusiones

### Fortalezas del Proyecto

✅ **Arquitectura moderna**: Uso de Quarkus con programación reactiva  
✅ **Separación de concerns**: Capas bien definidas (Resource-Service-Repository)  
✅ **Seguridad robusta**: Autenticación JWT + RBAC  
✅ **Validación exhaustiva**: Bean Validation en todos los DTOs  
✅ **Manejo de errores**: Excepciones de dominio + mapeo global  
✅ **Externalización de configuración**: Variables de entorno  
✅ **Documentación automática**: OpenAPI 3.0  
✅ **Sincronización dual**: Keycloak + PostgreSQL  
✅ **Resiliencia**: Retry automático en conexiones con Keycloak  

### Áreas de Mejora

⚠️ **Testing**: Falta de tests unitarios y de integración  
⚠️ **Observabilidad**: Sin métricas, tracing distribuido o health checks  
⚠️ **Cache**: Sin implementación de caché (performance)  
⚠️ **Rate Limiting**: Vulnerable a ataques de fuerza bruta  
⚠️ **Auditoría**: Sin logging de acciones administrativas  
⚠️ **Documentación**: README básico, falta documentación de arquitectura  

### Complejidad del Proyecto

**Nivel**: ⭐⭐⭐ Medio-Alto (3/5)

**Justificación**:
- Integración compleja con dos fuentes de datos (Keycloak + PostgreSQL)
- Programación reactiva con Mutiny
- Sincronización bidireccional
- Manejo de múltiples casos edge

**Líneas de Código Estimadas**: ~3,500 LOC (sin contar tests)

### Tiempo de Comprensión

Para un desarrollador con experiencia en Java y Quarkus:
- **Básico** (CRUD simple): 2-3 horas
- **Intermedio** (flujos de sincronización): 1 día
- **Avanzado** (arquitectura completa): 2-3 días

---

## Anexos

### A. Comandos Útiles

```bash
# Desarrollo
./mvnw quarkus:dev

# Build JVM
./mvnw clean package

# Build nativo
./mvnw package -Dnative

# Ejecutar tests
./mvnw test

# Generar reporte de dependencias
./mvnw dependency:tree

# Análisis de seguridad
./mvnw org.owasp:dependency-check-maven:check
```

### B. Variables de Entorno Requeridas

```bash
# Keycloak
export KEYCLOAK_URL=http://localhost:8083/
export KEYCLOAK_REALM=billpay_app
export KEYCLOAK_CLIENT_ID=cli_billpay_app
export KEYCLOAK_CLIENT_SECRET=your-secret-here

# PostgreSQL
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
export DB_REACTIVE_URL=postgresql://localhost:5432/billpay_db
```

### C. Estructura de Base de Datos PostgreSQL

```sql
-- Tabla: user_account
CREATE TABLE user_account (
    id UUID PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255),
    first_name VARCHAR(25),
    last_name VARCHAR(25),
    profile_image BYTEA,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_user_username ON user_account(username);
CREATE INDEX idx_user_email ON user_account(email);
```

### D. Configuración de Keycloak

**Realm**: `billpay_app`

**Cliente**: `cli_billpay_app`
- **Client Protocol**: openid-connect
- **Access Type**: confidential
- **Service Accounts Enabled**: ON
- **Authorization Enabled**: OFF

**Roles de Servicio** (Service Account Roles):
- `realm-management` → `manage-users`
- `realm-management` → `view-users`
- `realm-management` → `manage-realm`

**Realm Roles**:
- `admin_users`
- `admin_role`
- `admin_groups`
- `viewer`

---

**Fin del Documento**

_Este documento fue generado mediante análisis automatizado del codebase el 19 de noviembre de 2025._
