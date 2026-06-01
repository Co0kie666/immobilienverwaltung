package de.hsbi.immobilienverwaltung.ui.auth;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("login")
@PageTitle("ImmoPro | Anmeldung")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm loginForm = new LoginForm();
    
            public LoginView() {
                loginForm.setAction("login");
                loginForm.setForgotPasswordButtonVisible(false);

                add(loginForm);

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
                        .set("margin-bottom", "20px");

                com.vaadin.flow.component.button.Button registerButton =
                        new com.vaadin.flow.component.button.Button(
                                "Noch kein Konto? Registrieren"
                        );

                registerButton.getStyle()
                        .setWidth("100%")
                        .set("background", "transparent")
                        .set("color", "#2563eb")
                        .set("font-weight", "700")
                        .set("box-shadow", "none")
                        .set("margin-top", "14px");

                registerButton.addClickListener(event ->
                        UI.getCurrent().navigate(RegisterView.class)
                );

                card.add(
                        logo,
                        subtitle,
                        loginForm,
                        registerButton
                );
                add(backgroundIcon, card);
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