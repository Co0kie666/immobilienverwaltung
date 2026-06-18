package de.hsbi.immobilienverwaltung.ui.auth;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("login")
@PageTitle("ImmoPro | Anmeldung")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private LoginForm loginForm;

    public LoginView() {
        addClassNames("auth-page", "login-page");

        setPadding(false);
        setSpacing(false);
        setSizeFull();

        add(
                createBrandPanel(),
                createLoginPanel()
        );
    }

    private Component createLoginPanel() {
        Div formSide = new Div();
        formSide.addClassName("auth-form-side");

        Div card = new Div();
        card.addClassName("auth-form-card");

        Span eyebrow = new Span("WILLKOMMEN ZURÜCK");
        eyebrow.addClassName("auth-form-eyebrow");

        H2 title = new H2("Schön, dich wiederzusehen.");
        title.addClassName("auth-form-title");

        Paragraph subtitle = new Paragraph(
                "Melde dich an und behalte dein Portfolio im Blick."
        );
        subtitle.addClassName("auth-form-subtitle");

        loginForm = new LoginForm();
        loginForm.setAction("login");
        loginForm.setForgotPasswordButtonVisible(false);
        loginForm.addClassName("auth-login-form");

        RouterLink registerLink = new RouterLink(
                "Noch kein Konto? Jetzt registrieren",
                RegisterView.class
        );
        registerLink.addClassName("auth-link");

        card.add(
                eyebrow,
                title,
                subtitle,
                loginForm,
                registerLink
        );

        formSide.add(card);

        return formSide;
    }

    private Component createBrandPanel() {
        Div panel = new Div();
        panel.addClassName("auth-brand-panel");

        Icon logoIcon = VaadinIcon.BUILDING.create();
        logoIcon.addClassName("auth-brand-logo-icon");

        Span logoText = new Span("ImmoPro");
        logoText.addClassName("auth-brand-logo-text");

        HorizontalLayout logoRow = new HorizontalLayout(logoIcon, logoText);
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

        Div content = new Div(eyebrow, title, subtitle, visualCard);
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
            public void beforeEnter(BeforeEnterEvent event) {
                if (event.getLocation()
                        .getQueryParameters()
                        .getParameters()
                        .containsKey("error")) {
                    loginForm.setError(true);
                }
            }
        }