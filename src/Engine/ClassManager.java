package Engine;

import java.io.File;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Manage classes.
 */
public class ClassManager {


    private static HashMap<Integer, Class<?>> indexMap = new HashMap<>();
    private static HashMap<Class<?>, Integer> classMap = new HashMap<>();

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

    /**
     * Registers class from base package for serialization.
     * @param basePackage base package
     */
    public static void registerClassesFromBasePackage(String basePackage) {
        try {
            List<Class<?>> classes = getClassesRecursive(basePackage);

            for (Class<?> cls : classes) {
                if (!Modifier.isAbstract(cls.getModifiers())) {
                    System.out.println("Registered class: " + cls.getName());
                    ClassManager.registerClass(cls);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets classes from package.
     * @param packageName package name
     * @return list of classes
     * @throws Exception if there is an error loading class
     */
    public static List<Class<?>> getClassesRecursive(String packageName) throws Exception {
        List<Class<?>> classes = new ArrayList<>();
        String path = packageName.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resource = classLoader.getResource(path);

        if (resource == null) {
            return Collections.emptyList();
        }

        File directory = new File(resource.getFile());
        if (!directory.exists()) {
            return Collections.emptyList();
        }

        for (File file : Objects.requireNonNull(directory.listFiles())) {
            if (file.isDirectory()) {
                // Recurse into subpackages
                classes.addAll(getClassesRecursive(packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class") && !file.getName().contains("$")) {
                String className = packageName + '.' + file.getName().replace(".class", "");
                try {
                    classes.add(Class.forName(className));
                } catch (Throwable ignored) {
                    ignored.getMessage();
                }
            }
        }

        return classes;
    }
}
