package Engine.Networking;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A network event manager class, that handles all the NetEvents.
 */
public class Network {
    private static Map<String, Consumer<Object>> handlers = new HashMap<>();
    private static Map<String, Class<?>[]> methodParamTypes = new HashMap<>();
    private static Map<Integer, String> indexMap = new HashMap<>();
    private static Map<String, Integer> typeMap = new HashMap<>();

    /**
     * Registers a new handle.
     * @param type NetEvent type
     * @param paramTypes method parameter types
     * @param handler method handler
     */
    private static void registerHandler(String type, 
                                        Class<?>[] paramTypes, 
                                        Consumer<Object> handler) {
        handlers.put(type, (Consumer<Object>) handler);
        methodParamTypes.put(type, paramTypes);
    }


    /**
     * Finalize handle order in alphabetical order, for consistent order.
     */
    public static void finalizeHandlers() {
        List<String> keys = new ArrayList<>(handlers.keySet());
        Collections.sort(keys);
        indexMap = new HashMap<>();
        typeMap = new HashMap<>();
        for (int i = 0; i < keys.size(); i++) {
            String type = keys.get(i);
            indexMap.put(i, type);
            typeMap.put(type, i);
        }

        System.out.println("Net events: " + keys);
    }

    public static String getTypeFromIndex(int index) {
        return indexMap.get(index);
    }

    /**
     * Returns event index from event name.
     * @param name event name
     * @return event index
     */
    public static int getIndexFromName(String name) {
        if (!typeMap.containsKey(name)) {
            return -1;
        }
        return typeMap.get(name);
    }

    public static Class<?>[] getParamTypes(String name) {
        return methodParamTypes.get(name);
    }

    /**
     * Invokes the NetEvent method.
     * @param msg NetMessage
     */
    public static void onMessageReceived(NetMessage msg) {
        Consumer<Object> handler = handlers.get(msg.type);
        if (handler != null) {
            handler.accept((Object[]) msg.data);
        } else {
            System.out.println("No handler for type: " + msg.type);
        }
    }

    /**
     * Checks all methods in a class and registers NetEvents.
     * @param cls class
     */
    public static void registerHandlersFromClass(Class<?> cls) {
        for (Method method : cls.getDeclaredMethods()) {
            if (method.isAnnotationPresent(NetEvent.class)) {
                NetEvent annotation = method.getAnnotation(NetEvent.class);
                String messageType = annotation.value();

                // Check that it's static and has one parameter
                if (!Modifier.isStatic(method.getModifiers())) {
                    System.err.println("Method " + method.getName() + " must be static.");
                    continue;
                }

                Class<?>[] paramTypes = method.getParameterTypes();

                // Register the handler
                registerHandler(messageType, paramTypes, (Object args) -> {
                    try {
                        // Invokes the method with args
                        method.invoke(null, (Object[]) args);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }
}
