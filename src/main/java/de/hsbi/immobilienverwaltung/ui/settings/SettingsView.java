package de.hsbi.immobilienverwaltung.ui.settings;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.Route;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;

@Route(value = "settings", layout = MainLayout.class)
public class SettingsView extends Div implements HasPageHeader {

    public SettingsView() {
        addClassName("page-content");

    }

    @Override
    public String getPageTitle() {
        return "Settings";
    }

    @Override
    public String getPageSubtitle() {
        return "Allgemeine Einstellungen";
    }
}