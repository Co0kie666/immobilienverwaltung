package de.hsbi.immobilienverwaltung.ui.auth;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import de.hsbi.immobilienverwaltung.service.interfaces.AuthService;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;

@Route("register")
@AnonymousAllowed
public class RegisterView extends Div implements HasPageHeader {

    private final AuthService authService;

    private final TextField vornameField;
    private final TextField nachnameField;
    private final EmailField emailField;
    private final PasswordField passwortField;
    private final PasswordField passwortWiederholungField;

    public RegisterView(AuthService authService) {
        this.authService = authService;

        addClassNames("auth-page", "register-page");
        setSizeFull();

        vornameField = new TextField("Vorname");
        vornameField.setPlaceholder("Max");
        vornameField.setRequiredIndicatorVisible(true);
        vornameField.setAutofocus(true);
        vornameField.addClassName("auth-input");

        nachnameField = new TextField("Nachname");
        nachnameField.setPlaceholder("Mustermann");
        nachnameField.setRequiredIndicatorVisible(true);
        nachnameField.addClassName("auth-input");

        emailField = new EmailField("E-Mail-Adresse");
        emailField.setPlaceholder("max@beispiel.de");
        emailField.setRequiredIndicatorVisible(true);
        emailField.addClassName("auth-input");

        passwortField = new PasswordField("Passwort");
        passwortField.setPlaceholder("Mindestens 8 Zeichen");
        passwortField.setRequiredIndicatorVisible(true);
        passwortField.addClassName("auth-input");

        passwortWiederholungField = new PasswordField("Passwort wiederholen");
        passwortWiederholungField.setPlaceholder("Passwort erneut eingeben");
        passwortWiederholungField.setRequiredIndicatorVisible(true);
        passwortWiederholungField.addClassName("auth-input");

        add(
                createBrandPanel(),
                createRegisterPanel()
        );
    }

    private Component createRegisterPanel() {
        Div formSide = new Div();
        formSide.addClassName("auth-form-side");

        Div card = new Div();
        card.addClassName("auth-form-card");

        Span eyebrow = new Span("KONTO ERSTELLEN");
        eyebrow.addClassName("auth-form-eyebrow");

        H2 title = new H2("Starte mit ImmoPro.");
        title.addClassName("auth-form-title");

        Paragraph subtitle = new Paragraph(
                "Erstelle dein Konto und verwalte dein Immobilienportfolio "
                        + "an einem zentralen Ort."
        );
        subtitle.addClassName("auth-form-subtitle");

        Div nameRow = new Div();
        nameRow.addClassName("auth-name-grid");
        nameRow.add(vornameField, nachnameField);

        Button registerButton = new Button(
                "Konto erstellen",
                VaadinIcon.CHECK.create()
        );

        registerButton.addClassName("auth-primary-button");
        registerButton.addClickShortcut(Key.ENTER);
        registerButton.addClickListener(event -> registrieren());

        RouterLink loginLink = new RouterLink(
                "Du hast bereits ein Konto? Jetzt anmelden",
                LoginView.class
        );
        loginLink.addClassName("auth-link");

        card.add(
                eyebrow,
                title,
                subtitle,
                nameRow,
                emailField,
                passwortField,
                passwortWiederholungField,
                registerButton,
                loginLink
        );

        formSide.add(card);

        return formSide;
    }

    private void registrieren() {
        String vorname = vornameField.getValue().trim();
        String nachname = nachnameField.getValue().trim();
        String email = emailField.getValue().trim();
        String passwort = passwortField.getValue();
        String passwortWiederholung = passwortWiederholungField.getValue();

        if (vorname.isBlank()
                || nachname.isBlank()
                || email.isBlank()
                || passwort.isBlank()
                || passwortWiederholung.isBlank()) {

            Notification.show(
                    "Bitte fülle alle Pflichtfelder aus.",
                    3_000,
                    Notification.Position.TOP_CENTER
            );
            return;
        }

        if (!passwort.equals(passwortWiederholung)) {
            Notification.show(
                    "Die Passwörter stimmen nicht überein.",
                    3_000,
                    Notification.Position.TOP_CENTER
            );
            return;
        }

        try {
            authService.registrieren(
                    vorname,
                    nachname,
                    email,
                    passwort,
                    passwortWiederholung
            );

            Notification.show(
                    "Konto erfolgreich erstellt. Du kannst dich jetzt anmelden.",
                    4_000,
                    Notification.Position.TOP_CENTER
            );

            UI.getCurrent().navigate(LoginView.class);

        } catch (IllegalArgumentException exception) {
            Notification.show(
                    exception.getMessage(),
                    4_000,
                    Notification.Position.TOP_CENTER
            );
        }
    }

    private Component createBrandPanel() {
        Div panel = new Div();
        panel.addClassName("auth-brand-panel");

        Icon logoIcon = VaadinIcon.BUILDING.create();
        logoIcon.addClassName("auth-brand-logo-icon");

        Span logoText = new Span("ImmoPro");
        logoText.addClassName("auth-brand-logo-text");

        HorizontalLayout logoRow = new HorizontalLayout(
                logoIcon,
                logoText
        );
        logoRow.addClassName("auth-brand-logo-row");
        logoRow.setPadding(false);
        logoRow.setSpacing(false);

        Span eyebrow = new Span("PORTFOLIO CONTROL CENTER");
        eyebrow.addClassName("auth-eyebrow");

        H1 title = new H1("Alles Wichtige\nauf einen Blick");
        title.addClassName("auth-brand-title");

        Paragraph subtitle = new Paragraph(
                "Verwalte Immobilien, Mietverträge, Einnahmen und Ausgaben "
                        + "an einem zentralen Ort."
        );
        subtitle.addClassName("auth-brand-subtitle");

        Div buildingArt = new Div();
        buildingArt.addClassName("auth-building-art");

        Div stats = new Div();
        stats.addClassName("auth-stat-grid");

        stats.add(
                createAuthStat("12", "IMMOBILIEN"),
                createAuthStat("84 %", "VERMIETET"),
                createAuthStat("8", "VERTRÄGE")
        );

        Div visualCard = new Div(buildingArt, stats);
        visualCard.addClassName("auth-visual-card");

        Div content = new Div(
                eyebrow,
                title,
                subtitle,
                visualCard
        );
        content.addClassName("auth-brand-content");

        Span footer = new Span(
                "ImmoPro · Immobilienverwaltung neu gedacht"
        );
        footer.addClassName("auth-brand-footer");

        panel.add(logoRow, content, footer);

        return panel;
    }

    private Component createAuthStat(String value, String label) {
        Div stat = new Div();
        stat.addClassName("auth-stat-card");

        Span valueText = new Span(value);
        valueText.addClassName("auth-stat-value");

        Span labelText = new Span(label);
        labelText.addClassName("auth-stat-label");

        stat.add(valueText, labelText);

        return stat;
    }

    @Override
    public String getPageTitle() {
        return "Registrieren";
    }

    @Override
    public String getPageSubtitle() {
        return "Erstelle dein ImmoPro-Konto";
    }
}