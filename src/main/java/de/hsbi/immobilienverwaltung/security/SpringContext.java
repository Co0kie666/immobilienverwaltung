package de.hsbi.immobilienverwaltung.security;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class SpringContext {

    private static ApplicationContext context;

    public SpringContext(ApplicationContext context) {
        SpringContext.context = context;
    }

    public static <T> T getBean(Class<T> beanClass) {
        return context.getBean(beanClass);
    }
}