// src/main/java/org/cryptotrader/admin/dev/JRebelHook.java
package org.cryptotrader.admin.dev;

import javafx.application.Platform;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Deprecated(forRemoval = true)
public final class JRebelHook {
    private static final String JREBEL_FACTORY_OLD = "org.zeroturnaround.javarebel.ReloaderFactory";
    private static final String JREBEL_FACTORY_NEW = "com.zeroturnaround.javarebel.ReloaderFactory";
    private static final String JREBEL_LISTENER_OLD = "org.zeroturnaround.javarebel.ClassEventListener";
    private static final String JREBEL_LISTENER_NEW = "com.zeroturnaround.javarebel.ClassEventListener";

    private JRebelHook() { }

    public static void register(final @NotNull Runnable reloadUiAction, final String @NotNull ... packagePrefixesToWatch) {
        try {
            final Class<?> reloaderFactory = tryLoad(JREBEL_FACTORY_OLD, JREBEL_FACTORY_NEW);
            final Class<?> classEventListener = tryLoad(JREBEL_LISTENER_OLD, JREBEL_LISTENER_NEW);

            if (reloaderFactory == null || classEventListener == null) {
                System.out.println("[JRebel] API not present; auto-reload disabled.");
                return;
            }
            final Object reloader = reloaderFactory.getMethod("getInstance").invoke(null);
            final Object listenerProxy = createListenerProxy(classEventListener, reloadUiAction, packagePrefixesToWatch);
            registerReloadListener(reloader, classEventListener, listenerProxy);

            System.out.println("[JRebel] UI auto-reload hook registered.");
        } catch (final Throwable throwable) {
            System.out.println("[JRebel] Failed to register hook:");
            throwable.printStackTrace();
        }
    }

    private static @NotNull Object createListenerProxy(final @NotNull Class<?> listenerClass,
                                                       final @NotNull Runnable reloadAction,
                                                       final String @NotNull [] packagePrefixes) {
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

    private static @Nullable Object handleProxyInvocation(final Object proxy, final @NotNull Method method, final Object[] args,
                                                          final @NotNull Runnable reloadAction, final String @NotNull [] packagePrefixes) {
        final String methodName = method.getName();

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

    private static boolean isPriorityMethod(final @NotNull Method method) {
        return "priority".equals(method.getName())
                && method.getParameterCount() == 0
                && method.getReturnType() == int.class;
    }

    private static boolean isObjectMethod(final String name, final int paramCount) {
        return ("equals".equals(name) && paramCount == 1)
                || ("hashCode".equals(name) && paramCount == 0)
                || ("toString".equals(name) && paramCount == 0);
    }

    private static @NotNull Object handleObjectMethod(final String name, final Object proxy, final Object[] args) {
        if ("equals".equals(name)) {
            return proxy == args[0];
        }
        if ("hashCode".equals(name)) {
            return System.identityHashCode(proxy);
        }
        return "JRebelHook$ListenerProxy";
    }

    private static boolean isClassChangeEvent(final String name, final Object @Nullable [] args) {
        return "onClassEvent".equals(name)
                && args != null
                && args.length >= 2
                && args[1] instanceof Class<?>;
    }

    private static void handleClassChange(final @NotNull Object changedClass, final @NotNull Runnable reloadAction, final String @NotNull [] packagePrefixes) {
        final String fullClassName = ((Class<?>) changedClass).getName();
        for (final String prefix : packagePrefixes) {
            if (fullClassName.startsWith(prefix)) {
                Platform.runLater(() -> {
                    try {
                        reloadAction.run();
                    } catch (final Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
                break;
            }
        }
    }

    private static void registerReloadListener(final @NotNull Object reloader, final Class<?> listenerClass, final Object listener) throws Exception {
        final Method addListener = reloader.getClass().getMethod("addClassReloadListener", listenerClass);
        addListener.invoke(reloader, listener);
    }

    private static @Nullable Class<?> tryLoad(final String @NotNull ... fullyQualifiedClassNames) {
        for (final String name : fullyQualifiedClassNames) {
            try {
                return Class.forName(name);
            } catch (final ClassNotFoundException ignored) {
            }
        }
        return null;
    }
}