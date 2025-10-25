package Engine.Networking;

import Engine.ClassManager;
import Engine.GameObject;
import java.lang.reflect.Modifier;
import java.util.*;


/**
 * Automatic NetEvent registration.
 */
public class NetworkHandleRegister {
    /**
     * Registers all NetEvents.
     * @param basePackage package to get the GameObjects from
     */
    public static void registerAllGameObjectHandlers(String basePackage) {
        try {
            List<Class<?>> classes = ClassManager.getClassesRecursive(basePackage);

            for (Class<?> cls : classes) {
                if (GameObject.class.isAssignableFrom(cls) 
                    && !Modifier.isAbstract(cls.getModifiers())) {
                    Network.registerHandlersFromClass(cls);
                }
            }
            Network.finalizeHandlers();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}