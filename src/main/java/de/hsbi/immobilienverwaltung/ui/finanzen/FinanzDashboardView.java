package de.hsbi.immobilienverwaltung.ui.finanzen;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;

@Route(value = "finanzen", layout = MainLayout.class)
public class FinanzDashboardView extends Div implements HasPageHeader {

    public FinanzDashboardView() {
        addClassName("page-content");

    }

    @Override
    public String getPageTitle() {
        return "Finanzen";
    }

    @Override
    public String getPageSubtitle() {
        return "Einnahmen, Ausgaben und offene Zahlungen";
    }
}