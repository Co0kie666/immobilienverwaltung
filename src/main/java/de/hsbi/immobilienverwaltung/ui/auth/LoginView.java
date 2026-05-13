package de.hsbi.immobilienverwaltung.ui.auth;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.router.Route;

@Route("login")
public class LoginView extends Div {

    public LoginView() {
        add(new H2("Login"));
    }
}