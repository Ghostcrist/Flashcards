# TalkStalk — Juego de verbos en inglés

Proyecto colaborativo de **Java Swing** para practicar verbos en inglés mediante tarjetas, construcción de oraciones y reconocimiento de voz (Vosk).

## Requisitos

- **JDK 11 o superior**.
- **Apache Maven** (o NetBeans con soporte Maven).
- Conexión a internet la primera vez para que Maven descargue las dependencias.
- **Opcional para usar el micrófono:** modelo de reconocimiento de voz Vosk (instrucciones más abajo).

## Empezar a trabajar

1. Descarga o clona este repositorio y abre la carpeta `TalkStalk` en tu IDE (por ejemplo, NetBeans: **File > Open Project**).
2. En NetBeans, ejecuta la clase principal **`englishgame.Main`**. Maven descargará las librerías necesarias según el `pom.xml`.
3. También puedes iniciar el proyecto desde una terminal situada en la raíz del proyecto:

   ```bash
   mvn compile exec:java -Dexec.mainClass=englishgame.Main
   ```

4. Si quieres usar la función de **voz**, sigue la siguiente sección.

> **Importante:** el comando `java -jar target/TalkStalk-1.0.jar` por sí solo no está configurado para incluir las dependencias ni un `Main-Class` en el JAR. Para desarrollar, usa NetBeans o el comando Maven anterior.

## Descargar el modelo de reconocimiento de voz

Las bibliotecas de Vosk y JNA se obtienen mediante **Maven**. El **modelo de voz** es independiente y no se guarda en Git porque contiene archivos binarios grandes.

1. Descarga `vosk-model-small-en-us-0.15.zip` desde la página oficial: https://alphacephei.com/vosk/models
   (enlace directo indicado en el proyecto original: https://alphacephei.com/vosk/models/vosk-model-small-en-us-0.15.zip).
2. Descomprime el archivo dentro de la carpeta `modelo/`.
3. Comprueba que existan las carpetas `modelo/vosk-model-small-en-us-0.15/am/`, `conf/` y `graph/`.
4. Inicia la aplicación desde la raíz del proyecto. Alternativamente, puedes configurar la variable de entorno `TALKSTALK_MODEL` con la ruta completa de la carpeta del modelo.

Si no instalas el modelo o no tienes micrófono, puedes trabajar en el resto del juego; la función de voz mostrará un aviso.

En Linux, para la función de lectura de palabras en voz alta con `Speech.java`, puede ser necesario instalar `espeak`.

## Estructura del proyecto

```text
TalkStalk/
├── pom.xml                          # Configuración Maven (Vosk/JNA)
├── src/main/java/englishgame/       # 15 clases Java: interfaz y lógica
├── src/main/resources/englishgame/images/ # 17 imágenes de la app
├── modelo/                          # Modelo Vosk descargado localmente (no se sube)
├── archivos/                         # Datos locales generados al ejecutar (no se suben)
├── .gitignore                        # Excluye datos personales y archivos generados
└── CONTRIBUTING.md                   # Recomendaciones para trabajo en equipo
```

**Clases relevantes:** `Main` inicia la app, `MainFrame` administra pantallas, `UserStore` maneja el login, `FlashCardsPanel` y `SentenceBuilderPanel` contienen las actividades, `Pronunciation` usa Vosk y `Progress` calcula estadísticas.

Para **agregar un verbo**: editar `src/main/java/englishgame/VerbData.java` y añadir su imagen (`<verbo>.jpg`) a `src/main/resources/englishgame/images/`.

## Qué se excluyó deliberadamente del repositorio

- `target/`: clases compiladas y archivos generados.
- `__MACOSX`, `.DS_Store`, `nbproject/private/`: archivos de sistema o configuración privada.
- `archivos/usuarios.txt`, `archivos/recordar.txt`: **datos privados y contraseñas en texto plano**; cada desarrollador genera sus propios datos al ejecutar.
- `lib/*.jar`: bibliotecas binarias que Maven ya descarga.
- `modelo/vosk-model-*/`: modelo de voz binario, descargable por separado.

**Aviso de seguridad:** `UserStore.java` contiene un administrador de demostración con contraseña fija y almacena contraseñas sin cifrar. Antes de manejar datos de usuarios reales o publicar una versión de producción, es necesario corregir esto. No ingreses contraseñas personales o reutilizadas para probar el proyecto.

## Estado actual

El repositorio contiene el **código fuente original**, sin modificar la lógica de la aplicación. La persistencia del progreso de aprendizaje aún es en memoria y no se conserva entre sesiones. No se incluyen datos particulares de usuarios.

## Colaborar

Consulta [`CONTRIBUTING.md`](CONTRIBUTING.md) para trabajar con ramas, commits y pull requests.
