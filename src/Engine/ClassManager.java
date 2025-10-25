package Engine;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.lang.reflect.Modifier;

public class ClassManager {

    private static final HashMap<Integer, Class<?>> indexMap = new HashMap<>();
    private static final HashMap<Class<?>, Integer> classMap = new HashMap<>();

    private static void registerClass(Class<?> cls) {
        int index = indexMap.size();
        indexMap.put(index, cls);
        classMap.put(cls, index);
    }

    public static int getIndexFromClass(Class<?> cls) {
        return classMap.get(cls);
    }

    public static Class<?> getClassFromIndex(int index) {
        return indexMap.get(index);
    }

    public static void registerClassesFromBasePackage(String basePackage) {
        try {
            List<Class<?>> classes = getClassesRecursive(basePackage);

            for (Class<?> cls : classes) {
                if (!Modifier.isAbstract(cls.getModifiers())) {
                    System.out.println("Registered class: " + cls.getName());
                    registerClass(cls);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<Class<?>> getClassesRecursive(String packageName) throws Exception {
        List<Class<?>> classes = new ArrayList<>();
        String path = packageName.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resource = classLoader.getResource(path);
        if (resource == null) {
            return Collections.emptyList();
        }

        String protocol = resource.getProtocol();

        if ("file".equals(protocol)) {
            // Running from a project or IntelliJ out/ folder
            File directory = new File(resource.getFile());
            if (directory.exists()) {
                findClassesInDirectory(directory, packageName, classes);
            }
        } else if ("jar".equals(protocol)) {
            // Running from JAR
            findClassesInJar(resource, path, classes);
        }

        return classes;
    }

    private static void findClassesInDirectory(File directory, String packageName, List<Class<?>> classes)
            throws ClassNotFoundException {

        for (File file : Objects.requireNonNull(directory.listFiles())) {
            if (file.isDirectory()) {
                findClassesInDirectory(file, packageName + "." + file.getName(), classes);
            } else if (file.getName().endsWith(".class") && !file.getName().contains("$")) {
                String className = packageName + '.' + file.getName().replace(".class", "");
                classes.add(Class.forName(className));
            }
        }
    }

    private static void findClassesInJar(URL resource, String path, List<Class<?>> classes)
            throws Exception {

        String jarPath = resource.getPath();
        jarPath = jarPath.substring(5, jarPath.indexOf("!")); // strip "file:" and everything after "!/"

        try (JarFile jarFile = new JarFile(URLDecoder.decode(jarPath, StandardCharsets.UTF_8))) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();

                if (name.startsWith(path) && name.endsWith(".class") && !name.contains("$")) {
                    String className = name.replace('/', '.').replace(".class", "");
                    classes.add(Class.forName(className));
                }
            }
        }
    }
}