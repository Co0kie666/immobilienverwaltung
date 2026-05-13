package de.hsbi.immobilienverwaltung.ui.mieter;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;

@Route(value = "mieter-vertraege", layout = MainLayout.class)
public class MieterVertraegeView extends Div implements HasPageHeader {

    public MieterVertraegeView() {
        addClassName("page-content");

    }

    @Override
    public String getPageTitle() {
        return "Mieter & Verträge";
    }

    @Override
    public String getPageSubtitle() {
        return "Mieter und laufende Mietverhältnisse verwalten";
    }
}