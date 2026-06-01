package de.hsbi.immobilienverwaltung.ui.auth;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import de.hsbi.immobilienverwaltung.ui.dashboard.DashboardView;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Route("")
@AnonymousAllowed
public class StartView extends Div implements BeforeEnterObserver {

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        boolean loggedIn =
                authentication != null
                        && authentication.isAuthenticated()
                        && !"anonymousUser".equals(authentication.getName());

        if (loggedIn) {
            event.rerouteTo(DashboardView.class);
        } else {
            event.rerouteTo(LoginView.class);
        }
    }
}