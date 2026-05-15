package de.hsbi.immobilienverwaltung.ui.auth;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("login")
@PageTitle("ImmoPro | Login")
public class LoginView extends VerticalLayout {

    public LoginView() {

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        H1 title = new H1("ImmoPro");

        EmailField email = new EmailField("E-Mail");
        PasswordField password = new PasswordField("Passwort");

        Button loginButton = new Button("Anmelden");

        Button registerButton =
                new Button("Noch kein Konto? Registrieren");

        registerButton.addClickListener(e ->
                UI.getCurrent().navigate("register")
        );

        add(
                title,
                email,
                password,
                loginButton,
                registerButton
        );
    }
}