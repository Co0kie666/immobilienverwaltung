package de.hsbi.immobilienverwaltung.ui.auth;

import de.hsbi.immobilienverwaltung.service.interfaces.AuthService;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.mindrot.jbcrypt.BCrypt;

@Route("")
@PageTitle("ImmoPro | Anmeldung")
public class LoginView extends VerticalLayout {

    private final AuthService authService;

    public LoginView(AuthService authService) {
        this.authService = authService;

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
        card.setWidth("420px");
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

        Span subtitle = new Span("Melde dich in deinem Konto an");
        subtitle.getStyle()
                .set("font-size", "14px")
                .set("color", "#6b7280")
                .set("margin-top", "6px")
                .set("margin-bottom", "28px");

        EmailField email = new EmailField("E-Mail");
        email.setWidthFull();
        email.setPlaceholder("example@mail.com");
        email.setRequiredIndicatorVisible(true);

        PasswordField password = new PasswordField("Passwort");
        password.setWidthFull();
        password.setRequiredIndicatorVisible(true);

        Button loginButton = new Button("Anmelden");
        loginButton.setWidthFull();
        loginButton.getStyle()
                .set("background", "#2563eb")
                .set("color", "white")
                .set("border-radius", "12px")
                .set("font-weight", "700")
                .set("border", "none")
                .set("box-shadow", "0 4px 14px rgba(37, 99, 235, 0.25)")
                .set("margin-top", "8px");

        loginButton.addClickListener(event -> {
            try {
                authService.anmelden(
                        email.getValue(),
                        password.getValue()
                );

                Notification notification = Notification.show(
                        "Anmeldung erfolgreich!",
                        2000,
                        Notification.Position.TOP_CENTER
                );

                UI.getCurrent().navigate("dashboard");

            } catch (IllegalArgumentException ex) {

                Notification notification = Notification.show(
                        ex.getMessage(),
                        3000,
                        Notification.Position.TOP_CENTER
                );
            }
        });

        Button registerButton = new Button("Noch kein Konto? Registrieren");
        registerButton.setWidthFull();
        registerButton.getStyle()
                .set("background", "transparent")
                .set("color", "#2563eb")
                .set("font-weight", "700")
                .set("box-shadow", "none")
                .set("margin-top", "14px");

        registerButton.addClickListener(event ->
                UI.getCurrent().navigate("register")
        );

        card.add(
                logo,
                subtitle,
                email,
                password,
                loginButton,
                registerButton
        );

        add(backgroundIcon, card);
    }
}