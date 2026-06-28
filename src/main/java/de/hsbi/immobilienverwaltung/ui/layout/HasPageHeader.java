package de.hsbi.immobilienverwaltung.ui.layout;

/*
* Interface für Views, die ihren Seitentitel und Untertitel selbst bereitstellen.
* Das MainLayout liest diese Werte aus und aktualisiert damit den gemeinsamen Seitenheader.
*/
public interface HasPageHeader {

    // Titel, der im gemeinsamen Seitenheader angezeigt wird.
    String getPageTitle();

    // Untertitel/Beschreibung
    default String getPageSubtitle() {
        return "";
    }
}