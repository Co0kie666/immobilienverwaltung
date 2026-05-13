package de.hsbi.immobilienverwaltung.ui.dashboard;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;

@Route(value = "dashboard", layout = MainLayout.class)
public class DashboardView extends Div implements HasPageHeader {

    public DashboardView() {

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
