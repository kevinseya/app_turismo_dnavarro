# Documentación técnica: Backend Red Social de Turismo

## Descripción general
Este backend está construido con NestJS, Prisma y PostgreSQL. Provee la API para una red social de turismo donde los usuarios pueden crear publicaciones, dar like, comentar, seguir a otros usuarios y ver un feed personalizado y por cercanía.

## Estructura principal
- **Usuarios**: Registro, login, roles (ADMIN, CLIENT), seguir/ser seguido, gestión avanzada (bloquear, cambiar rol).
- **Posts**: Publicaciones con título, descripción, ubicación (lat/lng), teléfono, likes, comentarios, rating.
- **Likes**: Like/Unlike a publicaciones, con contador actualizado en transacción.
- **Comentarios**: Crear y listar comentarios en publicaciones, con contador actualizado en transacción.
- **Feed**: Feed de posts de seguidos y feed por cercanía geográfica.
- **Notificaciones**: Envío de notificaciones globales o individuales (solo admin).
- **Dashboard Admin**: Métricas globales y top posts.
- **Autenticación**: JWT, guardas de roles y endpoints protegidos.
- **Documentación OpenAPI**: Swagger disponible en `/api`.

## Endpoints principales

### Autenticación
- `POST /auth/register` — Registro de usuario
- `POST /auth/login` — Login y obtención de JWT

### Usuarios
- `GET /users` — Listar usuarios (solo admin)
- `PATCH /users/:id/block` — Bloquear usuario (solo admin)
- `PATCH /users/:id/unblock` — Desbloquear usuario (solo admin)
- `PATCH /users/:id/role` — Cambiar rol de usuario (solo admin)
- `GET /users/:id` — Ver perfil
- `POST /users/follow/:id` — Seguir usuario
- `DELETE /users/follow/:id` — Dejar de seguir

### Posts
`POST /posts` — Crear post (cualquier usuario autenticado)
- `GET /posts` — Listar todos los posts activos
- `DELETE /posts/:id` — Borrado lógico (solo ADMIN)

### Likes
- `POST /likes/:postId` — Dar like a un post
- `DELETE /likes/:postId` — Quitar like

### Comentarios
- `POST /posts/:postId/comments` — Crear comentario en post
- `GET /posts/:postId/comments` — Listar comentarios de un post

### Feed
- `GET /feed` — Feed de posts de usuarios seguidos
- `GET /feed/nearby?lat=...&lng=...&radiusKm=...` — Feed de posts cercanos geográficamente

### Notificaciones
- `POST /notifications/send` — Enviar notificación (solo admin, global o individual)
- `GET /notifications/my` — Ver notificaciones del usuario autenticado

### Dashboard Admin
- `GET /admin/dashboard` — Ver métricas globales y top posts (solo admin)

## Subida de imágenes a posts (flujo recomendado)

1. Sube las imágenes usando:
   - `POST /posts/upload` (solo ADMIN, puedes pedir que se habilite para todos)
   - Tipo de request: `multipart/form-data`, campo: `images[]` (puedes subir hasta 5 imágenes por vez)
   - Respuesta: array de rutas relativas, por ejemplo: `["uploads/1706050000000-123456789.jpg", "uploads/1706050000001-987654321.jpg"]`

2. Crea el post usando:
   - `POST /posts` (cualquier usuario autenticado)
   - En el campo `images` del body, envía el array de rutas devueltas por el paso anterior.

Así las imágenes quedan asociadas al post y disponibles para mostrarse en el feed.

Puedes probar la subida de imágenes directamente desde Swagger en `/api`.


## Seguridad
- Autenticación JWT obligatoria para la mayoría de endpoints.
Roles: cualquier usuario autenticado puede crear posts. Solo ADMIN puede borrar posts, gestionar usuarios, enviar notificaciones y ver dashboard.

## Esquema de datos (resumido)
- **User**: id, name, email, password, role, isActive, posts, comments, likes, followers, following, notifications
- **Post**: id, title, description, latitude, longitude, phone, likesCount, commentsCount, ratingAvg, userId
- **Comment**: id, content, rating, userId, postId
- **Like**: id, userId, postId
- **Follow**: id, followerId, followingId
- **Notification**: id, title, message, userId, createdAt

## Documentación OpenAPI
- Accesible en `/api` para ver y probar todos los endpoints y sus contratos (útil para integración con Kotlin).

## Notas
- Todos los contadores (likes, comentarios) se actualizan en transacciones para evitar inconsistencias.
- El feed por cercanía usa la fórmula de Haversine para calcular distancias.
- El backend está listo para ser consumido por apps móviles (Kotlin, Flutter, etc).

---
