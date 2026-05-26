package de.hsbi.immobilienverwaltung.security;

import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import de.hsbi.immobilienverwaltung.service.interfaces.AuthService;

public interface LoginRequired extends BeforeEnterObserver {

    @Override
    default void beforeEnter(BeforeEnterEvent event) {
        AuthService authService = SpringContext.getBean(AuthService.class);

        if (!authService.isLoggedIn()) {
            event.forwardTo("");
        }
    }
}