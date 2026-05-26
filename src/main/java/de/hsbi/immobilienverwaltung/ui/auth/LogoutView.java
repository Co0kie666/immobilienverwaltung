package de.hsbi.immobilienverwaltung.ui.auth;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import de.hsbi.immobilienverwaltung.service.interfaces.AuthService;
import de.hsbi.immobilienverwaltung.ui.auth.LoginView;

@Route("logout")
public class LogoutView extends Div implements BeforeEnterObserver {

    private final AuthService authService;

    public LogoutView(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        authService.logout();
        event.forwardTo(LoginView.class);
    }
}