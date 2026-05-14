package de.hsbi.immobilienverwaltung.ui.mieter;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.router.Route;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;

@Route(value = "mieter-anlegen", layout = MainLayout.class)
public class MieterFormView extends Div implements HasPageHeader {

    public MieterFormView() {
        addClassName("page-content");
        add(new H2("Mieter anlegen"));
    }

    @Override
    public String getPageTitle() {
        return "Mieter anlegen";
    }

    @Override
    public String getPageSubtitle() {
        return "Neuen Mieter erfassen";
    }
}