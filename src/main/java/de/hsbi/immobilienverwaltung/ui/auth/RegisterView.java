package de.hsbi.immobilienverwaltung.ui.auth;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
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
        setPadding(false);
        setSpacing(false);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        getStyle()
                .set("position", "relative")
                .set("overflow", "hidden")
                .set("background", "#f8fafc");

        Icon backgroundIcon = VaadinIcon.BUILDING.create();
        backgroundIcon.getStyle()
                .set("position", "absolute")
                .set("width", "520px")
                .set("height", "100%")
                .set("right", "50%")
                .set("bottom", "15px")
                .set("color", "#2563eb")
                .set("opacity", "0.06")
                .set("z-index", "0")
                .set("pointer-events", "none");

        VerticalLayout card = new VerticalLayout();
        card.setWidth("460px");
        card.setPadding(false);
        card.setSpacing(false);

        card.getStyle()
                .set("position", "relative")
                .set("z-index", "1")
                .set("background", "white")
                .set("border", "1px solid #e5e7eb")
                .set("border-radius", "20px")
                .set("padding", "38px")
                .set("box-shadow", "0 10px 40px -10px rgba(0, 0, 0, 0.08)")
                .set("box-sizing", "border-box");

        H1 logo = new H1("ImmoPro");
        logo.getStyle()
                .set("font-size", "34px")
                .set("font-weight", "900")
                .set("color", "#2563eb")
                .set("margin", "0");

        Span subtitle = new Span("Erstelle dein Konto");
        subtitle.getStyle()
                .set("font-size", "14px")
                .set("color", "#6b7280")
                .set("margin-top", "6px")
                .set("margin-bottom", "28px");

        TextField firstName = new TextField("Vorname");
        firstName.setWidthFull();

        TextField lastName = new TextField("Nachname");
        lastName.setWidthFull();

        HorizontalLayout nameRow = new HorizontalLayout(firstName, lastName);
        nameRow.setWidthFull();
        nameRow.setSpacing(true);
        nameRow.getStyle()
                .set("gap", "16px")
                .set("margin-bottom", "0");

        EmailField email = new EmailField("E-Mail");
        email.setWidthFull();
        email.setPlaceholder("example@mail.com");

        PasswordField password = new PasswordField("Passwort");
        password.setWidthFull();

        PasswordField repeatPassword = new PasswordField("Passwort wiederholen");
        repeatPassword.setWidthFull();

        Button registerButton = new Button("Registrieren");
        registerButton.setWidthFull();
        registerButton.getStyle()
                .set("background", "#2563eb")
                .set("color", "white")
                .set("border-radius", "12px")
                .set("font-weight", "700")
                .set("border", "none")
                .set("box-shadow", "0 4px 14px rgba(37, 99, 235, 0.25)")
                .set("margin-top", "14px");

        registerButton.addClickListener(event -> {

            Notification notification = Notification.show(
                    "Registrierung erfolgreich!",
                    2500,
                    Notification.Position.TOP_CENTER
            );

            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            UI.getCurrent().navigate("");
        });

        Button loginButton = new Button("Bereits registriert? Anmelden");
        loginButton.setWidthFull();
        loginButton.getStyle()
                .set("background", "transparent")
                .set("color", "#2563eb")
                .set("font-weight", "700")
                .set("box-shadow", "none")
                .set("margin-top", "14px");

        loginButton.addClickListener(event ->
                UI.getCurrent().navigate("")
        );

        card.add(
                logo,
                subtitle,
                nameRow,
                email,
                password,
                repeatPassword,
                registerButton,
                loginButton
        );

        add(backgroundIcon, card);
    }
}