package de.hsbi.immobilienverwaltung.ui.components;

import com.vaadin.flow.component.confirmdialog.ConfirmDialog;

public class ConfirmDeleteDialog extends ConfirmDialog {

    public ConfirmDeleteDialog(String title, String message, Runnable onConfirm) {
        setHeader(title);
        setText(message);

        setCancelable(true);
        setCancelText("Abbrechen");

        setConfirmText("Löschen");
        setConfirmButtonTheme("error primary");

        addConfirmListener(event -> {
            if (onConfirm != null) {
                onConfirm.run();
            }
        });
    }
}