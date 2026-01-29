# TurismoApp & Backend - Documentación General

<p align="center">
  <img src="https://nestjs.com/img/logo-small.svg" alt="NestJS" height="60"/>
  <img src="https://avatars.githubusercontent.com/u/17219288?s=200&v=4" alt="Prisma" height="60"/>
  <img src="https://www.postgresql.org/media/img/about/press/elephant.png" alt="PostgreSQL" height="60"/>
  <img src="https://developer.android.com/static/studio/images/new-studio-logo-1_1920.png" alt="Android Studio" height="60"/>
  <img src="https://upload.wikimedia.org/wikipedia/commons/7/74/Kotlin_Icon.png" alt="Kotlin" height="60"/>
</p>

---

## Descripción General
Este repositorio contiene dos proyectos principales:

- **turismo-backend**: Backend de una red social de turismo (NestJS, Prisma, PostgreSQL).
- **TurismoApp**: Aplicación móvil Android desarrollada en Kotlin.

---

## 1. Backend (carpeta `turismo-backend`)

![Backend API](https://user-images.githubusercontent.com/674621/180624978-6b7e2e2e-2e2e-4e2a-8e2e-2e2e2e2e2e2e.png)

### Funcionalidad
Permite a los usuarios:
- Crear publicaciones con imágenes, ubicación y contacto
- Dar likes, comentar, seguir a otros usuarios
- Recibir notificaciones
- Acceder a un feed personalizado y por cercanía
- Gestión avanzada de usuarios y dashboard para administradores

### Endpoints Principales
- **Autenticación:**
  - `POST /auth/register` — Registro de usuario
  - `POST /auth/login` — Login y obtención de JWT
- **Usuarios:**
  - `GET /users` — Listar usuarios (solo admin)
  - `PATCH /users/:id/block` — Bloquear usuario (solo admin)
  - `PATCH /users/:id/unblock` — Desbloquear usuario (solo admin)
  - `PATCH /users/:id/role` — Cambiar rol de usuario (solo admin)
  - `GET /users/:id` — Ver perfil
  - `POST /users/follow/:id` — Seguir usuario
  - `DELETE /users/follow/:id` — Dejar de seguir
- **Posts:**
  - `POST /posts` — Crear post
  - `GET /posts` — Listar posts activos
  - `DELETE /posts/:id` — Borrado lógico (solo admin)
  - `POST /posts/upload` — Subir imágenes (solo admin por defecto)
- **Likes:**
  - `POST /likes/:postId` — Dar like
  - `DELETE /likes/:postId` — Quitar like
- **Comentarios:**
  - `POST /posts/:postId/comments` — Crear comentario
  - `GET /posts/:postId/comments` — Listar comentarios
- **Feed:**
  - `GET /feed` — Feed de seguidos
  - `GET /feed/nearby?lat=...&lng=...&radiusKm=...` — Feed por cercanía
- **Notificaciones:**
  - `POST /notifications/send` — Enviar notificación (solo admin)
  - `GET /notifications/my` — Ver notificaciones propias
- **Dashboard Admin:**
  - `GET /admin/dashboard` — Métricas globales y top posts (solo admin)

> Para detalles y ejemplos de cada endpoint, consulta `turismo-backend/README_API.md` o la documentación Swagger en `/api`.

### Variables de Entorno
Crea un archivo `.env` en la raíz de `turismo-backend` con:
```
DATABASE_URL=postgresql://usuario:contraseña@host:puerto/base_de_datos
JWT_SECRET=tu_clave_secreta
```

### Scripts Útiles
- `pnpm install` — Instala dependencias
- `pnpm start:dev` — Modo desarrollo
- `pnpm prisma migrate dev` — Migraciones
- `pnpm test` — Tests

### Requisitos
- Node.js 18+
- PostgreSQL
- pnpm (o npm/yarn)

---

## 2. App Móvil (carpeta `TurismoApp`)

<p align="center">
  <img src="https://developer.android.com/static/studio/images/new-studio-logo-1_1920.png" alt="Android Studio" height="80"/>
  <img src="https://upload.wikimedia.org/wikipedia/commons/7/74/Kotlin_Icon.png" alt="Kotlin" height="80"/>
</p>

### Funcionalidad
Aplicación Android para consumir la API del backend, permitiendo:
- Registro/login de usuarios
- Visualización y creación de posts
- Likes, comentarios, seguir usuarios
- Notificaciones push
- Feed personalizado y por cercanía

### Estructura
- Proyecto Android con Gradle y Kotlin
- Carpeta principal: `TurismoApp/app/src/main/`
- Configuración de dependencias en `build.gradle.kts`

### Requisitos
- Android Studio
- JDK 17+
- Emulador o dispositivo físico

### Configuración
1. Clona el repositorio y abre `TurismoApp` en Android Studio.
2. Configura el endpoint base de la API en los archivos de recursos o variables de entorno según la implementación.
3. Sincroniza y ejecuta el proyecto.

---

## Notas Generales
- El backend está listo para ser consumido por apps móviles (Kotlin, Flutter, etc).
- Todos los contadores (likes, comentarios) se actualizan en transacciones para evitar inconsistencias.
- El feed por cercanía usa la fórmula de Haversine para calcular distancias.

---

Para más detalles técnicos y ejemplos de uso, revisa `turismo-backend/README_API.md` y la documentación Swagger en `/api`.
