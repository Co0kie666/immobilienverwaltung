package de.hsbi.immobilienverwaltung.ui.components;

import com.vaadin.flow.component.html.Span;

public class StatusBadge extends Span {

    public StatusBadge(String text, String type) {
        setText("● " + text);
        addClassNames("status-badge", type);
    }

    public static StatusBadge success(String text) {
        return new StatusBadge(text, "success");
    }

    public static StatusBadge warning(String text) {
        return new StatusBadge(text, "warning");
    }

    public static StatusBadge danger(String text) {
        return new StatusBadge(text, "danger");
    }

    public static StatusBadge neutral(String text) {
        return new StatusBadge(text, "neutral");
    }

    public static StatusBadge primary(String text) {
        return new StatusBadge(text, "primary");
    }
}