package de.hsbi.immobilienverwaltung.ui.auth;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import de.hsbi.immobilienverwaltung.service.interfaces.AuthService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

class RegisterViewTest {

    private Route route;
    private Constructor<RegisterView> constructor;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        route = RegisterView.class.getAnnotation(Route.class);

        constructor = RegisterView.class.getDeclaredConstructor(
                AuthService.class
        );
    }

    @AfterEach
    void tearDown() {
        route = null;
        constructor = null;
    }

    @Test
    void registerViewHatRegisterRouteUndIstAnonymErlaubt() {
        assertNotNull(route);
        assertEquals("register", route.value());

        assertTrue(
                RegisterView.class.isAnnotationPresent(AnonymousAllowed.class)
        );
    }

    @Test
    void registerViewHatOeffentlichenKonstruktorMitAuthService() {
        assertNotNull(constructor);

        assertTrue(
                Modifier.isPublic(constructor.getModifiers())
        );

        assertEquals(
                AuthService.class,
                constructor.getParameterTypes()[0]
        );

        assertTrue(
                Component.class.isAssignableFrom(RegisterView.class)
        );
    }
}