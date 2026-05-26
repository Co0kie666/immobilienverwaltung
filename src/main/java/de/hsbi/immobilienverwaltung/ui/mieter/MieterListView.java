package de.hsbi.immobilienverwaltung.ui.mieter;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import de.hsbi.immobilienverwaltung.security.LoginRequired;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.textfield.TextArea;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Route(value = "mieter-details", layout = MainLayout.class)
public class MieterListView extends Div implements HasPageHeader, HasUrlParameter<String>, LoginRequired {

    private MieterDummyDaten.MieterRow aktuellerMieter;
    private boolean bearbeitenAktiv = false;
    private final List<MieterDummyDaten.NotizRow> temporaereNotizen = new ArrayList<>();

    public MieterListView() {
        addClassName("page-content");
    }

    // Holt die Mieter-ID aus der URL
    @Override
    public void setParameter(BeforeEvent event, String mieterId) {
        Optional<MieterDummyDaten.MieterRow> optionalMieter =
                MieterDummyDaten.findMieterById(mieterId);

        if (optionalMieter.isEmpty()) {
            removeAll();
            add(createNotFoundCard());
            return;
        }

        aktuellerMieter = optionalMieter.get();
        bearbeitenAktiv = false;
        temporaereNotizen.clear();

        renderView();
    }

    // Baut die Ansicht neu auf
    private void renderView() {
        removeAll();

        add(
                createMieterHeader(aktuellerMieter),
                createContentLayout(aktuellerMieter)
        );
    }

    // Oberer Bereich mit Zurück-Button, Name, Status und Aktionen
    private Component createMieterHeader(MieterDummyDaten.MieterRow mieter) {
        Div headerCard = new Div();
        headerCard.addClassName("card");
        headerCard.addClassName("page-section");

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Button backButton = new Button(VaadinIcon.ARROW_LEFT.create());
        backButton.addClassName("icon-button");

        // Geht zurück zur Mieter & Verträge Übersicht
        backButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate("mieter-vertraege?tab=mieter"))
        );

        VerticalLayout textArea = new VerticalLayout();
        textArea.setPadding(false);
        textArea.setSpacing(false);

        Span name = new Span(mieter.name());
        name.addClassName("card-title");

        Span mieterInfo = new Span("Mieter-ID: " + mieter.id());
        mieterInfo.addClassName("card-subtitle");

        textArea.add(name, mieterInfo);

        Span statusBadge = new Span(mieter.status());
        statusBadge.addClassNames("status-badge", mieter.statusStyle());

        HorizontalLayout leftArea = new HorizontalLayout();
        leftArea.setAlignItems(FlexComponent.Alignment.CENTER);
        leftArea.setSpacing(true);
        leftArea.add(backButton, textArea, statusBadge);

        Button editButton;

        if (bearbeitenAktiv) {
            editButton = new Button("Speichern", VaadinIcon.CHECK.create());
            editButton.addClassName("primary-button");
        } else {
            editButton = new Button("Mieter bearbeiten", VaadinIcon.EDIT.create());
            editButton.addClassName("secondary-button");
        }

        editButton.addClickListener(event -> {
            bearbeitenAktiv = !bearbeitenAktiv;
            renderView();
        });

        Button newContractButton = new Button("Neuer Mietvertrag", VaadinIcon.PLUS.create());

        if (bearbeitenAktiv) {
            newContractButton.addClassName("secondary-button");
            newContractButton.setEnabled(false);
        } else {
            newContractButton.addClassName("primary-button");
            newContractButton.setEnabled(true);
            newContractButton.addClickListener(event ->
                    getUI().ifPresent(ui -> ui.navigate(MietvertragFormView.class))
            );
        }

        HorizontalLayout rightArea = new HorizontalLayout();
        rightArea.setAlignItems(FlexComponent.Alignment.CENTER);
        rightArea.setSpacing(true);
        rightArea.add(editButton, newContractButton);

        header.add(leftArea, rightArea);
        headerCard.add(header);

        return headerCard;
    }

    // Erstellt den Inhaltsbereich unter dem Header
    private Component createContentLayout(MieterDummyDaten.MieterRow mieter) {
        HorizontalLayout contentLayout = new HorizontalLayout();
        contentLayout.setWidthFull();
        contentLayout.setSpacing(true);
        contentLayout.setAlignItems(FlexComponent.Alignment.START);

        // Linke Spalte mit den Mieterdaten
        VerticalLayout leftColumn = new VerticalLayout();
        leftColumn.setPadding(false);
        leftColumn.setSpacing(true);
        leftColumn.setWidth("430px");

        leftColumn.add(
                createStammdatenCard(mieter),
                createKontaktCard(mieter)
        );

        // Rechte Spalte mit Buchungen und Notizen
        VerticalLayout rightColumn = new VerticalLayout();
        rightColumn.setPadding(false);
        rightColumn.setSpacing(true);
        rightColumn.setWidthFull();

        rightColumn.add(
                createLetzteBuchungenCard(mieter),
                createNotizenCard(mieter)
        );

        contentLayout.add(leftColumn, rightColumn);
        contentLayout.setFlexGrow(0, leftColumn);
        contentLayout.setFlexGrow(1, rightColumn);

        return contentLayout;
    }

    // Karte mit Stammdaten vom Mieter
    private Component createStammdatenCard(MieterDummyDaten.MieterRow mieter) {
        Div card = new Div();
        card.addClassName("card");
        card.setWidthFull();

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        Span title = new Span("Stammdaten");
        title.addClassName("card-title");

        content.add(title);

        if (bearbeitenAktiv) {
            content.add(
                    createEditField("Vollständiger Name", mieter.vollstaendigerName()),

                    // Geburtsdatum bleibt fest, Nationalität kann bearbeitet werden
                    createBirthdateNationalityEditRow(mieter),

                    createEditField("Beruf / Arbeitgeber", mieter.berufArbeitgeber()),

                    // Bonität kann ausgewählt werden, Prüfdatum wird automatisch gesetzt
                    createBonitaetEditRow(mieter)
            );
        } else {
            content.add(
                    createInfoBlock("Vollständiger Name", mieter.vollstaendigerName()),
                    createTwoColumnInfoRow(
                            "Geburtsdatum", mieter.geburtsdatum(),
                            "Nationalität", mieter.nationalitaet()
                    ),
                    createInfoBlock("Beruf / Arbeitgeber", mieter.berufArbeitgeber()),
                    createBonitaetRow(mieter)
            );
        }

        card.add(content);

        return card;
    }

    // Erstellt die Bonitätsauskunft im Bearbeiten-Modus
    private Component createBonitaetEditRow(MieterDummyDaten.MieterRow mieter) {
        Div row = new Div();
        row.addClassName("detail-edit-row");

        Select<String> bonitaetSelect = new Select<>();
        bonitaetSelect.setLabel("Bonitätsauskunft");
        bonitaetSelect.setItems("Positiv (Schufa)", "Kritisch");
        bonitaetSelect.setValue(
                mieter.bonitaet().equals("Kritisch") ? "Kritisch" : "Positiv (Schufa)"
        );
        bonitaetSelect.setWidthFull();
        bonitaetSelect.addClassName("detail-edit-field");

        String aktuellesDatum = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));

        Component pruefdatum = createInfoBlock("Prüfdatum", "Geprüft am " + aktuellesDatum);

        row.add(bonitaetSelect, pruefdatum);

        return row;
    }

    // Karte mit Kontaktinformationen
    private Component createKontaktCard(MieterDummyDaten.MieterRow mieter) {
        Div card = new Div();
        card.addClassName("card");
        card.setWidthFull();

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        Span title = new Span("Kontaktinformationen");
        title.addClassName("card-title");

        content.add(title);

        if (bearbeitenAktiv) {
            content.add(
                    createEditField("E-Mail", mieter.email()),
                    createEditField("Telefon", mieter.telefon()),
                    createEditField("Aktuelle Meldeadresse", mieter.meldeadresse().replace("\n", ", "))
            );
        } else {
            content.add(
                    createInfoBlock("E-Mail", mieter.email()),
                    createInfoBlock("Telefon", mieter.telefon()),
                    createInfoBlock("Aktuelle Meldeadresse", mieter.meldeadresse())
            );
        }

        card.add(content);

        return card;
    }

    // Karte mit den letzten Buchungen
    private Component createLetzteBuchungenCard(MieterDummyDaten.MieterRow mieter) {
        Div tableCard = new Div();
        tableCard.addClassName("table-card");
        tableCard.setWidthFull();

        HorizontalLayout header = new HorizontalLayout();
        header.addClassName("table-card-header");
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Span title = new Span("Letzte Buchungen");
        title.addClassName("card-title");

        Button showAllButton = new Button("Alle anzeigen");
        showAllButton.addClassName("ghost-button");

        header.add(title, showAllButton);

        Grid<MieterDummyDaten.BuchungRow> buchungenGrid =
                new Grid<>(MieterDummyDaten.BuchungRow.class, false);

        buchungenGrid.setWidthFull();
        buchungenGrid.setAllRowsVisible(true);

        buchungenGrid.addColumn(MieterDummyDaten.BuchungRow::datum)
                .setHeader("Datum")
                .setAutoWidth(true)
                .setFlexGrow(1);

        buchungenGrid.addColumn(MieterDummyDaten.BuchungRow::verwendungszweck)
                .setHeader("Verwendungszweck")
                .setAutoWidth(true)
                .setFlexGrow(3);

        buchungenGrid.addColumn(MieterDummyDaten.BuchungRow::vertrag)
                .setHeader("Vertrag")
                .setAutoWidth(true)
                .setFlexGrow(1);

        buchungenGrid.addColumn(MieterDummyDaten.BuchungRow::betrag)
                .setHeader("Betrag")
                .setAutoWidth(true)
                .setFlexGrow(1);

        buchungenGrid.addColumn(new ComponentRenderer<>(this::createBuchungStatusBadge))
                .setHeader("Status")
                .setAutoWidth(true)
                .setFlexGrow(1);

        buchungenGrid.setItems(MieterDummyDaten.getBuchungenByMieterId(mieter.id()));

        tableCard.add(header, buchungenGrid);

        return tableCard;
    }

    // Karte mit Notizen zum Mieter
    private Component createNotizenCard(MieterDummyDaten.MieterRow mieter) {
        Div card = new Div();
        card.addClassName("card");
        card.setWidthFull();

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Span title = new Span("Notizen");
        title.addClassName("card-title");

        Button addNoteButton = new Button("Hinzufügen", VaadinIcon.PLUS.create());
        addNoteButton.addClassName("ghost-button");

        // Öffnet den Dialog zum Hinzufügen einer neuen Notiz
        addNoteButton.addClickListener(event -> openNotizDialog(mieter));

        header.add(title, addNoteButton);

        content.add(header);

        List<MieterDummyDaten.NotizRow> notizen = new ArrayList<>();

        // Neue Notizen sollen oben stehen
        notizen.addAll(temporaereNotizen);
        notizen.addAll(MieterDummyDaten.getNotizenByMieterId(mieter.id()));

        if (notizen.isEmpty()) {
            Span emptyText = new Span("Keine wichtigen Notizen vorhanden.");
            emptyText.addClassName("card-subtitle");
            content.add(emptyText);
        } else {
            notizen.forEach(notiz -> content.add(createNotizItem(notiz)));
        }

        card.add(content);

        return card;
    }

    // Öffnet einen Dialog zum Hinzufügen einer neuen Notiz
    private void openNotizDialog(MieterDummyDaten.MieterRow mieter) {
        Dialog dialog = new Dialog();
        dialog.setWidth("520px");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);
        content.setWidthFull();

        Span title = new Span("Neue Notiz hinzufügen");
        title.addClassName("card-title");

        Span subtitle = new Span("Die Notiz wird nur für diesen Mieter erstellt.");
        subtitle.addClassName("card-subtitle");

        TextField titelField = new TextField("Titel");
        titelField.setPlaceholder("z.B. Haustiere");
        titelField.setRequiredIndicatorVisible(true);
        titelField.setWidthFull();

        TextArea textArea = new TextArea("Notiz");
        textArea.setPlaceholder("Notiztext eingeben...");
        textArea.setRequiredIndicatorVisible(true);
        textArea.setWidthFull();
        textArea.setMinHeight("120px");

        Button cancelButton = new Button("Abbrechen", VaadinIcon.CLOSE.create());
        cancelButton.addClassName("secondary-button");
        cancelButton.addClickListener(event -> dialog.close());

        Button saveButton = new Button("Notiz speichern", VaadinIcon.CHECK.create());
        saveButton.addClassName("primary-button");

        saveButton.addClickListener(event -> {
            boolean titelLeer = titelField.getValue() == null || titelField.getValue().isBlank();
            boolean textLeer = textArea.getValue() == null || textArea.getValue().isBlank();

            titelField.setInvalid(titelLeer);
            textArea.setInvalid(textLeer);

            titelField.setErrorMessage("Bitte Titel eingeben");
            textArea.setErrorMessage("Bitte Notiztext eingeben");

            if (titelLeer || textLeer) {
                return;
            }

            String aktuellesDatum = LocalDate.now()
                    .format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));

            MieterDummyDaten.NotizRow neueNotiz = new MieterDummyDaten.NotizRow(
                    mieter.id(),
                    titelField.getValue(),
                    textArea.getValue(),
                    aktuellesDatum,
                    "neutral"
            );

            temporaereNotizen.add(0, neueNotiz);

            dialog.close();
            renderView();
        });

        HorizontalLayout actions = new HorizontalLayout();
        actions.setWidthFull();
        actions.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        actions.setAlignItems(FlexComponent.Alignment.CENTER);
        actions.setSpacing(true);
        actions.add(cancelButton, saveButton);

        content.add(
                title,
                subtitle,
                titelField,
                textArea,
                actions
        );

        dialog.add(content);
        dialog.open();
    }

    // Erstellt eine einzelne Notiz
    private Component createNotizItem(MieterDummyDaten.NotizRow notiz) {
        Div noteCard = new Div();
        noteCard.addClassName("card-no-shadow");
        noteCard.setWidthFull();

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(false);

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Span title = new Span(notiz.titel());
        title.addClassName("card-title");

        Span date = new Span(notiz.datum());
        date.addClassName("card-subtitle");

        header.add(title, date);

        Span text = new Span(notiz.text());
        text.addClassName("card-subtitle");

        content.add(header, text);
        noteCard.add(content);

        return noteCard;
    }

    // Erstellt den Status-Badge für eine Buchung
    private Component createBuchungStatusBadge(MieterDummyDaten.BuchungRow row) {
        Span badge = new Span(row.status());
        badge.addClassNames("status-badge", row.statusStyle());
        return badge;
    }

    // Erstellt eine einfache Zeile mit Bezeichnung und Wert
    private Component createInfoRow(String labelText, String valueText) {
        return createInfoRow(labelText, new Span(valueText));
    }

    // Erstellt eine einfache Zeile, bei der der Wert auch ein Badge sein kann
    private Component createInfoRow(String labelText, Component valueComponent) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Span label = new Span(labelText);
        label.addClassName("card-subtitle");

        row.add(label, valueComponent);

        return row;
    }

    // Erstellt einen kleinen Info-Block untereinander
    private Component createInfoBlock(String labelText, String valueText) {
        VerticalLayout block = new VerticalLayout();
        block.setPadding(false);
        block.setSpacing(false);

        Span label = new Span(labelText);
        label.addClassName("card-subtitle");

        Span value = new Span(valueText);

        block.add(label, value);

        return block;
    }

    // Erstellt ein Textfeld für den Bearbeiten-Modus
    private TextField createEditField(String labelText, String valueText) {
        TextField field = new TextField(labelText);
        field.setValue(valueText == null ? "" : valueText);
        field.setWidthFull();
        field.addClassName("detail-edit-field");

        return field;
    }

    // Erstellt zwei Info-Blöcke nebeneinander
    private Component createTwoColumnInfoRow(
            String firstLabel,
            String firstValue,
            String secondLabel,
            String secondValue
    ) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setSpacing(true);

        Component firstBlock = createInfoBlock(firstLabel, firstValue);
        Component secondBlock = createInfoBlock(secondLabel, secondValue);

        row.add(firstBlock, secondBlock);
        row.setFlexGrow(1, firstBlock);
        row.setFlexGrow(1, secondBlock);

        return row;
    }

    // Erstellt zwei Textfelder nebeneinander
    private Component createTwoColumnEditRow(
            String firstLabel,
            String firstValue,
            String secondLabel,
            String secondValue
    ) {
        Div row = new Div();
        row.addClassName("detail-edit-row");

        TextField firstField = createEditField(firstLabel, firstValue);
        TextField secondField = createEditField(secondLabel, secondValue);

        row.add(firstField, secondField);

        return row;
    }

    // Erstellt eine Zeile, bei der das Geburtsdatum nicht bearbeitet werden kann
    private Component createBirthdateNationalityEditRow(MieterDummyDaten.MieterRow mieter) {
        Div row = new Div();
        row.addClassName("detail-edit-row");

        Component geburtsdatum = createInfoBlock("Geburtsdatum", mieter.geburtsdatum());
        TextField nationalitaet = createEditField("Nationalität", mieter.nationalitaet());

        row.add(geburtsdatum, nationalitaet);

        return row;
    }

    // Erstellt die Bonitätszeile mit Badge und Prüfdatum
    private Component createBonitaetRow(MieterDummyDaten.MieterRow mieter) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.setSpacing(true);

        Span badge = new Span(mieter.bonitaet());
        badge.addClassNames("status-badge", mieter.bonitaetStyle());

        Span pruefdatum = new Span(mieter.bonitaetPruefdatum());
        pruefdatum.addClassName("card-subtitle");

        row.add(badge, pruefdatum);

        VerticalLayout block = new VerticalLayout();
        block.setPadding(false);
        block.setSpacing(false);

        Span label = new Span("Bonitätsauskunft");
        label.addClassName("card-subtitle");

        block.add(label, row);

        return block;
    }

    // Wird angezeigt, wenn keine passende Dummy-ID gefunden wurde
    private Component createNotFoundCard() {
        Div card = new Div();
        card.addClassName("empty-state");

        Span title = new Span("Mieter nicht gefunden");
        title.addClassName("empty-state-title");

        Span text = new Span("Für diese Dummy-ID gibt es aktuell keine Daten.");
        text.addClassName("empty-state-text");

        card.add(title, text);

        return card;
    }

    @Override
    public String getPageTitle() {
        return "Mieter Details";
    }

    @Override
    public String getPageSubtitle() {
        return "Mieterdaten und Buchungen anzeigen";
    }
}