# 1. Nombre del Proyecto

api-role

# 2. Descripción Técnica del Problema que Resuelve

Este microservicio expone una API REST para la gestión (CRUD) de roles en un servidor Keycloak externo. Permite a otros sistemas crear, actualizar, eliminar y consultar roles de manera centralizada, delegando la persistencia y lógica de seguridad a Keycloak. El objetivo es desacoplar la gestión de roles de las aplicaciones consumidoras, centralizando la administración y asegurando la consistencia de los permisos en un entorno distribuido.

**Requisitos funcionales:**
- CRUD de roles en Keycloak vía API REST.
- Validación de datos de entrada.
- Manejo de errores y respuestas HTTP estándar.

**Requisitos no funcionales:**
- Integración segura con Keycloak (autenticación admin).
- API documentada vía OpenAPI/Swagger.
- Soporte para despliegue en entornos cloud-native.

# 3. Arquitectura General del Sistema

- **Tipo:** Microservicio (arquitectura orientada a servicios, stateless)
- **Diagrama mental de componentes:**

```
[API REST (Quarkus)] <--> [Servicio de Dominio] <--> [Repositorio] <--> [Keycloak Admin Client SDK] <--> [Keycloak Server]
```

- **Flujo de datos:**
  1. El cliente realiza una petición HTTP a la API REST.
  2. El recurso invoca el servicio de dominio.
  3. El servicio valida, transforma y delega en el repositorio.
  4. El repositorio usa el SDK de Keycloak para operar sobre el servidor Keycloak.
  5. La respuesta se propaga de vuelta al cliente.

- **Comunicación:** HTTP REST (JSON) entre cliente y API, y entre API y Keycloak (vía SDK).

# 4. Componentes o Módulos Principales

## 4.1. API REST (Resource)
- **Responsabilidad:** Exponer endpoints HTTP para operaciones CRUD de roles.
- **Interfaces públicas:** `/roles` (POST, GET, PUT, DELETE)
- **Dependencias:** Servicio de dominio, validación, OpenAPI
- **Tecnologías:** Quarkus REST, Jakarta REST, Hibernate Validator

## 4.2. Servicio de Dominio
- **Responsabilidad:** Lógica de negocio, validación, orquestación de operaciones.
- **Interfaces públicas:** Métodos CRUD invocados por el recurso.
- **Dependencias:** Mapper, repositorio
- **Tecnologías:** Java, Quarkus DI

## 4.3. Mapper
- **Responsabilidad:** Conversión entre DTOs y modelos de Keycloak.
- **Interfaces públicas:** Métodos de mapeo entre request/response y entidades Keycloak.
- **Dependencias:** DTOs, modelos Keycloak
- **Tecnologías:** Java, MapStruct/Lombok (si aplica)

## 4.4. Repositorio
- **Responsabilidad:** Acceso a Keycloak usando el SDK oficial.
- **Interfaces públicas:** Métodos CRUD de roles.
- **Dependencias:** KeycloakAdminProvider
- **Tecnologías:** Keycloak Admin Client SDK, Java

## 4.5. KeycloakAdminProvider
- **Responsabilidad:** Proveer instancia autenticada del cliente Keycloak.
- **Interfaces públicas:** `getKeycloak()`, `getRolesResource(realm)`
- **Dependencias:** Configuración de acceso a Keycloak
- **Tecnologías:** Keycloak Admin Client SDK

## 4.6. DTOs y Validaciones
- **Responsabilidad:** Estructuras de datos para requests/responses y validación.
- **Interfaces públicas:** Clases DTO, anotaciones de validación
- **Dependencias:** Hibernate Validator
- **Tecnologías:** Java, Lombok

# 5. Casos de Uso Técnicos Principales

- **Crear rol:** POST `/roles` → valida → mapea → crea en Keycloak → responde
- **Actualizar rol:** PUT `/roles/{name}` → valida → mapea → actualiza en Keycloak
- **Eliminar rol:** DELETE `/roles/{name}` → elimina en Keycloak
- **Consultar rol:** GET `/roles/{name}` → obtiene de Keycloak
- **Listar roles:** GET `/roles` → pagina y lista desde Keycloak

# 6. Tecnologías y Herramientas Clave

- **Lenguaje:** Java 21
- **Framework principal:** Quarkus 3.26.x
- **Librerías:**
  - Keycloak Admin Client SDK (`org.keycloak:keycloak-admin-client`)
  - Jakarta REST, Hibernate Validator, Lombok
  - OpenAPI/Swagger (quarkus-smallrye-openapi)
- **Testing:** JUnit 5 (quarkus-junit5)
- **Documentación:** OpenAPI (Swagger UI)

# 7. Entorno de Ejecución / Infraestructura

- **Local:**
  - Java 21+
  - Keycloak corriendo en `http://localhost:8080` (configurable)
  - Variables/config en `application.properties` (URL, realm, usuario, password, clientId)
- **Contenedores:**
  - Dockerfile disponible para empaquetado JVM/native
- **Cloud-ready:**
  - Compatible con despliegue en Kubernetes/OpenShift

# 8. Requisitos Previos Técnicos para Ejecutarlo

- Java 21+
- Maven 3.9+
- Keycloak 21+ (recomendado 22.x)
- Configurar `application.properties` con credenciales y URL de Keycloak
- Comandos básicos:
  - `./mvnw clean install`
  - `./mvnw quarkus:dev` para desarrollo

# 9. Consideraciones de Seguridad, Escalabilidad y Rendimiento

- **Seguridad:**
  - El acceso a Keycloak se realiza con usuario admin y clientId `admin-cli` (no exponer en producción, usar secretos seguros)
  - Validación exhaustiva de datos de entrada
  - Manejo de errores y respuestas controladas
- **Escalabilidad:**
  - Stateless, puede escalar horizontalmente
  - El cuello de botella es Keycloak (verificar límites de API y concurrencia)
- **Rendimiento:**
  - Operaciones CRUD dependen de la latencia de Keycloak
  - No hay cache local por diseño (consistencia fuerte)

# 10. Notas sobre Roadmap Técnico o Áreas a Refactorizar

- Parametrizar realm y credenciales vía variables de entorno/secretos
- Mejorar manejo de errores y logging estructurado
- Agregar tests de integración contra Keycloak embebido o mock
- Considerar soporte para multi-realm
- Documentar ejemplos de requests/responses en OpenAPI

---

Este archivo es técnico y debe mantenerse actualizado ante cambios arquitectónicos o de dependencias.
