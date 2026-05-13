package de.hsbi.immobilienverwaltung.ui.immobilien;


import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.Route;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;

@Route(value = "immobilien", layout = MainLayout.class)
public class ImmobilienListView extends Div implements HasPageHeader {

    public ImmobilienListView() {
        addClassName("page-content");
        H2 h = new H2("Hallo");
        h.setClassName("ueberschrift");
        add(h);
    }

    @Override
    public String getPageTitle() {
        return "Immobilienübersicht";
    }

    @Override
    public String getPageSubtitle() {
        return "Alle Immobilienobjekte im Überblick";
    }
}