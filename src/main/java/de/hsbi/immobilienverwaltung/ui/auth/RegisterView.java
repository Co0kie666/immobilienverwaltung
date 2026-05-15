package de.hsbi.immobilienverwaltung.ui.auth;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("register")
@PageTitle("ImmoPro | Registrierung")
    public class RegisterView extends VerticalLayout {

        public RegisterView() {

            setSizeFull();
            setAlignItems(Alignment.CENTER);
            setJustifyContentMode(JustifyContentMode.CENTER);

            H1 title = new H1("ImmoPro");

            TextField firstName = new TextField("Vorname");
            TextField lastName = new TextField("Nachname");

            HorizontalLayout names =
                    new HorizontalLayout(firstName, lastName);

            EmailField email = new EmailField("E-Mail");

            PasswordField password =
                    new PasswordField("Passwort");

            PasswordField repeatPassword =
                    new PasswordField("Passwort wiederholen");

            Button registerButton =
                    new Button("Registrieren");

            Button loginButton =
                    new Button("Bereits registriert? Anmelden");

            loginButton.addClickListener(_ ->
                    UI.getCurrent().navigate("")
            );

            registerButton.addClickListener(_ ->
                    UI.getCurrent().navigate("")
            );

            add(
                    title,
                    names,
                    email,
                    password,
                    repeatPassword,
                    registerButton,
                    loginButton
            );
        }
    }

