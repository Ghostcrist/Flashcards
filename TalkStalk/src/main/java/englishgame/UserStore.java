package englishgame;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Manejo de usuarios en archivos de texto (puerto de ensure_files/read_users/save_users del código Python). */
public class UserStore {
    public static final String ADMIN_EMAIL = "admin@est.local";
    public static final String ADMIN_PASSWORD = "admin123";
    public static final int MAX_LOGIN_ATTEMPTS = 3;

    private static final Path DATA_DIR = Paths.get("archivos");
    private static final Path USERS_FILE = DATA_DIR.resolve("usuarios.txt");
    private static final Path REMEMBER_FILE = DATA_DIR.resolve("recordar.txt");

    public static class User {
        public String id, nombre, correo, fecha, rol, password, pregunta, respuesta;

        String toLine() {
            return String.join("|", id, nombre, correo, fecha, rol, password, pregunta, respuesta);
        }
    }

    /** Quita el separador "|" y saltos de línea para no romper el formato del archivo. */
    public static String clean(String s) {
        return s == null ? "" : s.replace("|", " ").replace("\r", " ").replace("\n", " ").trim();
    }

    /** Crea la carpeta y el archivo de usuarios si no existen; fuerza las credenciales del administrador (id 1). */
    public static synchronized void ensureFiles() {
        try {
            Files.createDirectories(DATA_DIR);
            if (!Files.exists(USERS_FILE)) {
                User admin = new User();
                admin.id = "1"; admin.nombre = "Administrador"; admin.correo = ADMIN_EMAIL;
                admin.fecha = LocalDate.now().toString(); admin.rol = "creador"; admin.password = ADMIN_PASSWORD;
                admin.pregunta = "¿Cuál es el nombre de la aplicación?"; admin.respuesta = "TalkStalk";
                List<User> list = new ArrayList<>();
                list.add(admin);
                saveUsers(list);
            } else {
                List<User> users = readUsersRaw();
                for (User u : users) {
                    if (u.id.equals("1")) { u.correo = ADMIN_EMAIL; u.password = ADMIN_PASSWORD; }
                }
                saveUsers(users);
            }
        } catch (IOException e) {
            throw new RuntimeException("No se pudo preparar la carpeta 'archivos': " + e.getMessage(), e);
        }
    }

    private static List<User> readUsersRaw() throws IOException {
        List<User> users = new ArrayList<>();
        for (String line : Files.readAllLines(USERS_FILE, StandardCharsets.UTF_8)) {
            String[] p = line.split("\\|", -1);
            User u = new User();
            if (p.length >= 8) {
                u.id = p[0]; u.nombre = p[1]; u.correo = p[2]; u.fecha = p[3]; u.rol = p[4]; u.password = p[5];
                u.pregunta = p[6];
                StringBuilder r = new StringBuilder(p[7]);
                for (int i = 8; i < p.length; i++) r.append('|').append(p[i]);
                u.respuesta = r.toString();
            } else if (p.length >= 6) {
                u.id = p[0]; u.nombre = p[1]; u.correo = p[2]; u.fecha = p[3]; u.rol = p[4]; u.password = p[5];
                u.pregunta = "¿Cuál es tu nombre?"; u.respuesta = p[1];
            } else {
                continue;
            }
            users.add(u);
        }
        return users;
    }

    public static synchronized List<User> readUsers() {
        ensureFiles();
        try { return readUsersRaw(); } catch (IOException e) { return new ArrayList<>(); }
    }

    public static synchronized void saveUsers(List<User> users) {
        List<String> lines = new ArrayList<>();
        for (User u : users) lines.add(u.toLine());
        try {
            Files.createDirectories(DATA_DIR);
            Files.write(USERS_FILE, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo guardar usuarios.txt: " + e.getMessage(), e);
        }
    }

    public static User find(List<User> users, String email) {
        String t = email == null ? "" : email.trim().toLowerCase();
        for (User u : users) if (u.correo.toLowerCase().equals(t)) return u;
        return null;
    }

    /** Registra un usuario nuevo. Lanza IllegalArgumentException si el correo ya existe. */
    public static synchronized User register(String nombre, String correo, String password, String pregunta, String respuesta) {
        List<User> users = readUsers();
        if (find(users, correo) != null) throw new IllegalArgumentException("Ya existe una cuenta con ese correo.");
        int max = 0;
        for (User u : users) { try { max = Math.max(max, Integer.parseInt(u.id)); } catch (NumberFormatException ignore) { } }
        User u = new User();
        u.id = String.valueOf(max + 1); u.nombre = clean(nombre); u.correo = clean(correo).toLowerCase();
        u.fecha = LocalDate.now().toString(); u.rol = "usuario"; u.password = clean(password);
        u.pregunta = clean(pregunta); u.respuesta = clean(respuesta);
        users.add(u);
        saveUsers(users);
        return u;
    }

    // ---------- "Recordarme" ----------
    public static String loadRemembered() {
        try {
            if (Files.exists(REMEMBER_FILE)) return new String(Files.readAllBytes(REMEMBER_FILE), StandardCharsets.UTF_8).trim();
        } catch (IOException ignore) { }
        return "";
    }

    public static void saveRemembered(String email) {
        try {
            Files.createDirectories(DATA_DIR);
            if (email == null || email.isEmpty()) Files.deleteIfExists(REMEMBER_FILE);
            else Files.write(REMEMBER_FILE, email.getBytes(StandardCharsets.UTF_8));
        } catch (IOException ignore) { }
    }

    private UserStore() { }
}
