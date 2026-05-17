package de.hsbi.immobilienverwaltung;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;

@Route("mainlayout")
public class MainView extends Div implements HasPageHeader, BeforeEnterObserver {

    // Homepage navigiert direkt zu /dashboard
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
            UI.getCurrent().getPage().setLocation("");
    }

    @Override
    public String getPageTitle() {
        return "Dashboard";
    }

    @Override
    public String getPageSubtitle() {
        return "Zentrale Übersicht und KPIs";
    }
}


