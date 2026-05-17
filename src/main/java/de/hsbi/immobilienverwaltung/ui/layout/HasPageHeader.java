package de.hsbi.immobilienverwaltung.ui.layout;

import java.util.List;

public interface HasPageHeader {

    String getPageTitle();

    default String getPageSubtitle() {
        return "";
    }

    default List<String> getBreadcrumbItems() {
        return List.of();
    }

}