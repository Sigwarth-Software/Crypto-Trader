// src/main/java/org/cryptotrader/admin/dev/JRebelHook.java
package org.cryptotrader.admin.dev;

import javafx.application.Platform;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Deprecated(forRemoval = true)
public final class JRebelHook {
    private static final String JREBEL_FACTORY_OLD = "org.zeroturnaround.javarebel.ReloaderFactory";
    private static final String JREBEL_FACTORY_NEW = "com.zeroturnaround.javarebel.ReloaderFactory";
    private static final String JREBEL_LISTENER_OLD = "org.zeroturnaround.javarebel.ClassEventListener";
    private static final String JREBEL_LISTENER_NEW = "com.zeroturnaround.javarebel.ClassEventListener";

    private JRebelHook() { }

    public static void register(Runnable reloadUiAction, String... packagePrefixesToWatch) {
        try {
            Class<?> reloaderFactory = tryLoad(JREBEL_FACTORY_OLD, JREBEL_FACTORY_NEW);
            Class<?> classEventListener = tryLoad(JREBEL_LISTENER_OLD, JREBEL_LISTENER_NEW);

            if (reloaderFactory == null || classEventListener == null) {
                System.out.println("[JRebel] API not present; auto-reload disabled.");
                return;
            }
            Object reloader = reloaderFactory.getMethod("getInstance").invoke(null);
            Object listenerProxy = createListenerProxy(classEventListener, reloadUiAction, packagePrefixesToWatch);
            registerReloadListener(reloader, classEventListener, listenerProxy);

            System.out.println("[JRebel] UI auto-reload hook registered.");
        } catch (Throwable throwable) {
            System.out.println("[JRebel] Failed to register hook:");
            throwable.printStackTrace();
        }
    }

    private static Object createListenerProxy(Class<?> listenerClass,
                                              Runnable reloadAction,
                                              String[] packagePrefixes) {
        return Proxy.newProxyInstance(
                listenerClass.getClassLoader(),
                new Class<?>[]{listenerClass},
                (proxy, method, args) -> handleProxyInvocation(proxy,
                                                                                   method,
                                                                                   args,
                                                                                   reloadAction,
                                                                                   packagePrefixes)
        );
    }

    private static Object handleProxyInvocation(Object proxy, Method method, Object[] args,
                                                Runnable reloadAction, String[] packagePrefixes) {
        String methodName = method.getName();

        if (isPriorityMethod(method)) {
            return 0;
        }
        if (isObjectMethod(methodName, method.getParameterCount())) {
            return handleObjectMethod(methodName, proxy, args);
        }
        if (isClassChangeEvent(methodName, args)) {
            handleClassChange(args[1], reloadAction, packagePrefixes);
        }
        return null;
    }

    private static boolean isPriorityMethod(Method method) {
        return "priority".equals(method.getName())
                && method.getParameterCount() == 0
                && method.getReturnType() == int.class;
    }

    private static boolean isObjectMethod(String name, int paramCount) {
        return ("equals".equals(name) && paramCount == 1)
                || ("hashCode".equals(name) && paramCount == 0)
                || ("toString".equals(name) && paramCount == 0);
    }

    private static Object handleObjectMethod(String name, Object proxy, Object[] args) {
        if ("equals".equals(name)) {
            return proxy == args[0];
        }
        if ("hashCode".equals(name)) {
            return System.identityHashCode(proxy);
        }
        return "JRebelHook$ListenerProxy";
    }

    private static boolean isClassChangeEvent(String name, Object[] args) {
        return "onClassEvent".equals(name)
                && args != null
                && args.length >= 2
                && args[1] instanceof Class<?>;
    }

    private static void handleClassChange(Object changedClass, Runnable reloadAction, String[] packagePrefixes) {
        String fullClassName = ((Class<?>) changedClass).getName();
        for (String prefix : packagePrefixes) {
            if (fullClassName.startsWith(prefix)) {
                Platform.runLater(() -> {
                    try {
                        reloadAction.run();
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
                break;
            }
        }
    }

    private static void registerReloadListener(Object reloader, Class<?> listenerClass, Object listener) throws Exception {
        Method addListener = reloader.getClass().getMethod("addClassReloadListener", listenerClass);
        addListener.invoke(reloader, listener);
    }

    private static Class<?> tryLoad(String... fullyQualifiedClassNames) {
        for (String name : fullyQualifiedClassNames) {
            try {
                return Class.forName(name);
            } catch (ClassNotFoundException ignored) {
            }
        }
        return null;
    }
}