# Guía breve para trabajar en equipo

## Flujo recomendado con GitHub

1. Una persona crea el repositorio **vacío** en GitHub.
2. Descomprime este ZIP y sube **el contenido de `TalkStalk/`** (no el ZIP en sí) como primer commit.
3. Cada integrante clona el repositorio:

   ```bash
   git clone URL_DEL_REPOSITORIO
   cd NOMBRE_DEL_REPOSITORIO
   ```

4. Antes de comenzar una funcionalidad, actualiza tu rama principal: `git pull origin main`.
5. Crea una rama para cada tarea: `git switch -c feature/nombre-de-la-tarea`.
6. Haz cambios pequeños, verifica que compilen y guarda tus avances:

   ```bash
   git add .
   git commit -m "Describe brevemente el cambio"
   git push -u origin feature/nombre-de-la-tarea
   ```

7. Abre un **Pull Request** en GitHub para integrar los cambios tras revisarlos en equipo.

## Reglas sencillas

- **No subas** `archivos/usuarios.txt`, `recordar.txt`, contraseñas, grabaciones privadas ni datos personales.
- No subas la carpeta `target/` ni dependencias JAR o modelos grandes.
- Mantén `pom.xml` al día si añades bibliotecas.
- Usa nombres de rama descriptivos (`feature/login`, `fix/progreso`, etc.).
- Antes de fusionar cambios, prueba que el proyecto compile: `mvn clean compile`.
- Si cambias el modo de ejecución, actualiza también el `README.md`.
- Para evitar conflictos, acuerden quién trabaja en cada pantalla o componente.

> Nota: la aplicación guarda contraseñas como texto plano y tiene credenciales de prueba codificadas en `UserStore.java`. No la conecten a datos de usuarios reales hasta solucionar este problema.
