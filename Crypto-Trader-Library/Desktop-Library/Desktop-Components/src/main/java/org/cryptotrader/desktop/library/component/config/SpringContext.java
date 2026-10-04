package org.cryptotrader.desktop.library.component.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component("desktopSpringContext")
public class SpringContext implements ApplicationContextAware {
    private static ApplicationContext context;

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        SpringContext.context = applicationContext;
    }

    public static <T> @NotNull T getBean(final @NotNull Class<T> type) {
        return context.getBean(type);
    }

    public static ApplicationContext getContext() {
        return context;
    }
}
