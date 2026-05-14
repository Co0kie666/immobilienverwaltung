package de.hsbi.immobilienverwaltung.ui.mieter;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.router.Route;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;

@Route(value = "mietvertrag-anlegen", layout = MainLayout.class)
public class MietvertragFormView extends Div implements HasPageHeader {

    public MietvertragFormView() {
        addClassName("page-content");

        add(new H2("Mietvertrag anlegen"));
    }

    @Override
    public String getPageTitle() {
        return "Mietvertrag anlegen";
    }

    @Override
    public String getPageSubtitle() {
        return "Neuen Mietvertrag erfassen";
    }
}