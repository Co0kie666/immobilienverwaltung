package de.hsbi.immobilienverwaltung;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;

@Route(value = "", layout = MainLayout.class)
public class MainView extends Div implements HasPageHeader {

    public MainView() {

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