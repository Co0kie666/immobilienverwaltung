package de.hsbi.immobilienverwaltung.ui.auth;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

class LoginViewTest {

    private Route route;
    private Constructor<LoginView> constructor;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        route = LoginView.class.getAnnotation(Route.class);
        constructor = LoginView.class.getDeclaredConstructor();
    }

    @AfterEach
    void tearDown() {
        route = null;
        constructor = null;
    }

    @Test
    void loginViewHatLoginRouteUndIstAnonymErlaubt() {
        assertNotNull(route);
        assertEquals("login", route.value());

        assertTrue(
                LoginView.class.isAnnotationPresent(AnonymousAllowed.class)
        );
    }

    @Test
    void loginViewIstVaadinComponentUndHatPublicKonstruktor() {
        assertTrue(
                Component.class.isAssignableFrom(LoginView.class)
        );

        assertTrue(
                Modifier.isPublic(constructor.getModifiers())
        );

        assertEquals(
                0,
                constructor.getParameterCount()
        );
    }
}