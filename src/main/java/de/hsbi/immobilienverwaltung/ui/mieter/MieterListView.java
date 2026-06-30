package de.hsbi.immobilienverwaltung.ui.mieter;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import de.hsbi.immobilienverwaltung.domain.Adresse;
import de.hsbi.immobilienverwaltung.domain.Mieter;
import de.hsbi.immobilienverwaltung.domain.Mietvertrag;
import de.hsbi.immobilienverwaltung.domain.enums.Vertragsstatus;
import de.hsbi.immobilienverwaltung.service.interfaces.MieterService;
import de.hsbi.immobilienverwaltung.service.interfaces.MietvertragService;
import de.hsbi.immobilienverwaltung.ui.UiFormatUtils;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;
import jakarta.annotation.security.PermitAll;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.util.List;

@Route(value = "mieter-vertraege/mieter-details", layout = MainLayout.class)
@PermitAll
public class MieterListView extends Div implements HasPageHeader, HasUrlParameter<String> {

    private final MieterService mieterService;
    private final MietvertragService mietvertragService;

    private Mieter aktuellerMieter;
    private boolean bearbeitenAktiv = false;

    private final TextField vornameField = new TextField("Vorname");
    private final TextField nachnameField = new TextField("Nachname");
    private final TextField emailField = new TextField("E-Mail");
    private final TextField telefonField = new TextField("Telefon");
    private final TextField berufField = new TextField("Beruf / Tätigkeit");
    private final TextField strasseField = new TextField("Straße");
    private final TextField hausnummerField = new TextField("Hausnummer");
    private final TextField plzField = new TextField("PLZ");
    private final TextField ortField = new TextField("Ort");

    public MieterListView(MieterService mieterService, MietvertragService mietvertragService) {
        this.mieterService = mieterService;
        this.mietvertragService = mietvertragService;

        addClassNames("page-content", "mieter-detail-page");
        richteEingabefelderEin();
    }

    @Override
    public void setParameter(BeforeEvent event, String mieterId) {
        try {
            Long id = Long.valueOf(mieterId);

            aktuellerMieter = mieterService.findeMieterNachId(id)
                    .orElse(null);

            if (aktuellerMieter == null) {
                removeAll();
                add(erstelleNichtGefundenKarte());
                return;
            }

            bearbeitenAktiv = false;
            aktualisiereAnsicht();

        } catch (NumberFormatException ex) {
            removeAll();
            add(erstelleNichtGefundenKarte());
        }
    }

    // Baut die Seite neu auf. Das wird auch genutzt, wenn zwischen Ansicht
    // und Bearbeitung gewechselt wird.
    private void aktualisiereAnsicht() {
        removeAll();

        add(
                erstelleKopfbereich(),
                erstelleKennzahlenBereich(),
                erstelleInhaltsBereich()
        );
    }

    // Hier bekommen die Eingabefelder ihre gemeinsamen Einstellungen.
    // Dadurch muss das nicht in jeder einzelnen Karte wiederholt werden.
    private void richteEingabefelderEin() {
        List<TextField> felder = List.of(
                vornameField,
                nachnameField,
                emailField,
                telefonField,
                berufField,
                strasseField,
                hausnummerField,
                plzField,
                ortField
        );

        felder.forEach(field -> {
            field.setWidthFull();
            field.addClassName("mieter-detail-field");
        });

        vornameField.setRequiredIndicatorVisible(true);
        nachnameField.setRequiredIndicatorVisible(true);
        emailField.setPrefixComponent(VaadinIcon.ENVELOPE.create());
        telefonField.setPrefixComponent(VaadinIcon.PHONE.create());
        telefonField.setAllowedCharPattern("[0-9+ ]");
        plzField.setAllowedCharPattern("[0-9]");
    }

    // Der Kopfbereich zeigt die wichtigsten Daten direkt oben:
    // Name, Kontakt, Adresse, Status und die Aktionen.
    private Component erstelleKopfbereich() {
        Div hero = new Div();
        hero.addClassName("mieter-detail-hero");

        Div left = new Div();
        left.addClassName("mieter-detail-hero-left");

        Button backButton = new Button(VaadinIcon.ARROW_LEFT.create());
        backButton.addClassName("mieter-detail-back-button");
        backButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate("mieter-vertraege?tab=mieter"))
        );

        Div avatar = new Div();
        avatar.addClassName("mieter-detail-avatar");
        avatar.setText(UiFormatUtils.erstelleInitialen(aktuellerMieter));

        Div titleBox = new Div();
        titleBox.addClassName("mieter-detail-title-box");

        Span eyebrow = new Span("Mieterprofil · ID " + aktuellerMieter.getId());
        eyebrow.addClassName("mieter-detail-eyebrow");

        H2 name = new H2(UiFormatUtils.formatiereMieterName(aktuellerMieter));
        name.addClassName("mieter-detail-name");

        Div meta = new Div();
        meta.addClassName("mieter-detail-meta");
        meta.add(erstelleMetaInfo(VaadinIcon.ENVELOPE, UiFormatUtils.wertOderStrich(aktuellerMieter.getEmail())));
        meta.add(erstelleMetaInfo(VaadinIcon.PHONE, UiFormatUtils.wertOderStrich(aktuellerMieter.getTelefonnummer())));
        meta.add(erstelleMetaInfo(VaadinIcon.MAP_MARKER, UiFormatUtils.formatiereAdresseKurz(aktuellerMieter.getAdresse())));

        titleBox.add(eyebrow, name, meta);
        left.add(backButton, avatar, titleBox);

        Div right = new Div();
        right.addClassName("mieter-detail-hero-actions");

        String status = ermittleMieterStatus();

        Span statusBadge = new Span(status);
        statusBadge.addClassNames("status-badge", ermittleStatusStil(status), "mieter-detail-status");

        if (bearbeitenAktiv) {
            Button saveButton = new Button("Speichern", VaadinIcon.CHECK.create());
            saveButton.addClassName("primary-button");
            saveButton.addClickListener(event -> speichereAenderungen());

            Button cancelButton = new Button("Abbrechen", VaadinIcon.CLOSE.create());
            cancelButton.addClassName("secondary-button");
            cancelButton.addClickListener(event -> {
                bearbeitenAktiv = false;
                aktualisiereAnsicht();
            });

            Button archiveButton = new Button("Archivieren", VaadinIcon.TRASH.create());
            archiveButton.addClassName("danger-button");
            archiveButton.addClickListener(event -> archiviereMieter());

            right.add(statusBadge, cancelButton, archiveButton, saveButton);
        } else {
            Button editButton = new Button("Mieter bearbeiten", VaadinIcon.EDIT.create());
            editButton.addClassName("secondary-button");
            editButton.addClickListener(event -> {
                bearbeitenAktiv = true;
                aktualisiereAnsicht();
            });

            Button newContractButton = new Button("Neuer Mietvertrag", VaadinIcon.PLUS.create());
            newContractButton.addClassName("primary-button");
            newContractButton.addClickListener(event -> geheZuNeuemMietvertrag());

            right.add(statusBadge, editButton, newContractButton);
        }

        hero.add(left, right);
        return hero;
    }

    private Component erstelleMetaInfo(VaadinIcon icon, String text) {
        Div pill = new Div();
        pill.addClassName("mieter-detail-meta-pill");

        Icon pillIcon = icon.create();
        Span label = new Span(text);

        pill.add(pillIcon, label);
        return pill;
    }

    // Die Kennzahlen fassen die Verträge zusammen.
    // So sieht man direkt, ob der Mieter aktuell einen laufenden Vertrag hat.
    private Component erstelleKennzahlenBereich() {
        List<Mietvertrag> vertraege = ladeVertraege();

        long aktiveVertraege = vertraege.stream()
                .filter(vertrag -> vertrag.getStatus() == Vertragsstatus.AKTIV)
                .count();

        long auslaufendeVertraege = vertraege.stream()
                .filter(vertrag -> vertrag.getStatus() == Vertragsstatus.GEKUENDIGT)
                .count();

        double warmmiete = vertraege.stream()
                .filter(vertrag -> vertrag.getStatus() == Vertragsstatus.AKTIV)
                .mapToDouble(this::berechneWarmmiete)
                .sum();

        Div stats = new Div();
        stats.addClassName("mieter-detail-stats-grid");

        stats.add(
                erstelleKennzahlKarte("Mietverträge", String.valueOf(vertraege.size()), "Gesamte Historie", VaadinIcon.FILE_TEXT, "primary"),
                erstelleKennzahlKarte("Aktiv", String.valueOf(aktiveVertraege), "Laufende Verträge", VaadinIcon.CHECK_CIRCLE, "success"),
                erstelleKennzahlKarte("Läuft aus", String.valueOf(auslaufendeVertraege), "Gekündigte Verträge", VaadinIcon.CLOCK, "warning"),
                erstelleKennzahlKarte("Warmmiete", UiFormatUtils.formatiereEuro(warmmiete), "Aktive Verträge mtl.", VaadinIcon.EURO, "danger")
        );

        return stats;
    }

    private Component erstelleKennzahlKarte(String label, String value, String subtitle, VaadinIcon icon, String type) {
        Div card = new Div();
        card.addClassNames("mieter-detail-stat-card", type);

        Div iconBox = new Div(icon.create());
        iconBox.addClassNames("mieter-detail-stat-icon", type);

        Div text = new Div();
        text.addClassName("mieter-detail-stat-text");

        Span labelText = new Span(label);
        labelText.addClassName("mieter-detail-stat-label");

        Span valueText = new Span(value);
        valueText.addClassName("mieter-detail-stat-value");

        Span subtitleText = new Span(subtitle);
        subtitleText.addClassName("mieter-detail-stat-subtitle");

        text.add(labelText, valueText, subtitleText);
        card.add(text, iconBox);

        return card;
    }

    // Links stehen die Stammdaten, rechts die Verträge.
    // Dadurch bleibt die Detailseite trotz vieler Informationen übersichtlich.
    private Component erstelleInhaltsBereich() {
        Div contentLayout = new Div();
        contentLayout.addClassName("mieter-detail-content-grid");

        Div leftColumn = new Div();
        leftColumn.addClassName("mieter-detail-side-column");
        leftColumn.add(
                erstelleStammdatenKarte(),
                erstelleKontaktKarte(),
                erstelleBankdatenKarte()
        );

        Div rightColumn = new Div();
        rightColumn.addClassName("mieter-detail-main-column");
        rightColumn.add(
                erstelleVertraegeKarte()
        );

        contentLayout.add(leftColumn, rightColumn);
        return contentLayout;
    }

    // Im Bearbeitungsmodus werden aus den angezeigten Daten Eingabefelder.
    private Component erstelleStammdatenKarte() {
        Div card = erstelleDetailKarte("Stammdaten", "Personenbezogene Angaben und beruflicher Kontext", VaadinIcon.USER);

        if (bearbeitenAktiv) {
            vornameField.setValue(UiFormatUtils.wertOderLeer(aktuellerMieter.getVorname()));
            nachnameField.setValue(UiFormatUtils.wertOderLeer(aktuellerMieter.getNachname()));
            berufField.setValue(UiFormatUtils.wertOderLeer(aktuellerMieter.getBeruf()));

            FormLayout form = erstelleBearbeitungsFormular();
            form.add(vornameField, nachnameField, berufField);
            form.setColspan(berufField, 2);

            card.add(form);
        } else {
            card.add(
                    erstelleInfoZeile(VaadinIcon.USER, "Vollständiger Name", UiFormatUtils.formatiereMieterName(aktuellerMieter)),
                    erstelleInfoZeile(VaadinIcon.CALENDAR, "Geburtsdatum", UiFormatUtils.formatiereDatum(aktuellerMieter.getGeburtsdatum())),
                    erstelleInfoZeile(VaadinIcon.BRIEFCASE, "Beruf / Tätigkeit", UiFormatUtils.wertOderStrich(aktuellerMieter.getBeruf()))
            );
        }

        return card;
    }

    // Straße und Hausnummer werden im Bearbeiten bewusst nebeneinander angezeigt.
    // Die Straße bekommt mehr Platz, weil sie meistens länger ist.
    private Component erstelleKontaktKarte() {
        Div card = erstelleDetailKarte("Kontakt & Adresse", "Erreichbarkeit und postalische Daten", VaadinIcon.MAP_MARKER);
        Adresse adresse = aktuellerMieter.getAdresse();

        if (bearbeitenAktiv) {
            emailField.setValue(UiFormatUtils.wertOderLeer(aktuellerMieter.getEmail()));
            telefonField.setValue(UiFormatUtils.wertOderLeer(aktuellerMieter.getTelefonnummer()));
            strasseField.setValue(adresse == null ? "" : UiFormatUtils.wertOderLeer(adresse.getStrasse()));
            hausnummerField.setValue(adresse == null ? "" : UiFormatUtils.wertOderLeer(adresse.getHausnummer()));
            plzField.setValue(adresse == null ? "" : UiFormatUtils.wertOderLeer(adresse.getPlz()));
            ortField.setValue(adresse == null ? "" : UiFormatUtils.wertOderLeer(adresse.getStadt()));

            HorizontalLayout strasseHausnummerLayout = new HorizontalLayout();
            strasseHausnummerLayout.setWidthFull();
            strasseHausnummerLayout.setSpacing(true);

            strasseHausnummerLayout.add(strasseField, hausnummerField);
            strasseHausnummerLayout.setFlexGrow(2, strasseField);
            strasseHausnummerLayout.setFlexGrow(1, hausnummerField);

            FormLayout form = erstelleBearbeitungsFormular();
            form.add(emailField, telefonField, strasseHausnummerLayout, plzField, ortField);
            form.setColspan(strasseHausnummerLayout, 2);

            card.add(form);
        } else {
            card.add(
                    erstelleInfoZeile(VaadinIcon.ENVELOPE, "E-Mail", UiFormatUtils.wertOderStrich(aktuellerMieter.getEmail())),
                    erstelleInfoZeile(VaadinIcon.PHONE, "Telefon", UiFormatUtils.wertOderStrich(aktuellerMieter.getTelefonnummer())),
                    erstelleInfoZeile(VaadinIcon.HOME, "Adresse", UiFormatUtils.formatiereAdresse(adresse))
            );
        }

        return card;
    }

    // Die Tabelle zeigt alle Verträge dieses Mieters.
    // Ein Klick auf eine Zeile öffnet direkt die Vertragsdetails.
    private Component erstelleVertraegeKarte() {
        Div tableCard = new Div();
        tableCard.addClassName("mieter-detail-contract-card");

        Div header = new Div();
        header.addClassName("mieter-detail-card-header");

        Div titleBox = new Div();
        titleBox.addClassName("mieter-detail-card-title-box");

        Span icon = new Span();
        icon.add(VaadinIcon.FILE_TEXT.create());
        icon.addClassName("mieter-detail-card-icon");

        Div titleText = new Div();

        Span title = new Span("Mietverträge");
        title.addClassName("mieter-detail-card-title");

        Span subtitle = new Span("Laufende und vergangene Mietverhältnisse dieses Mieters");
        subtitle.addClassName("mieter-detail-card-subtitle");

        titleText.add(title, subtitle);
        titleBox.add(icon, titleText);

        Button newContractButton = new Button("Vertrag anlegen", VaadinIcon.PLUS.create());
        newContractButton.addClassName("primary-button");
        newContractButton.setEnabled(!bearbeitenAktiv);
        newContractButton.addClickListener(event -> geheZuNeuemMietvertrag());

        header.add(titleBox, newContractButton);

        Grid<Mietvertrag> grid = new Grid<>(Mietvertrag.class, false);
        grid.setWidthFull();
        grid.setAllRowsVisible(true);
        grid.addClassName("mieter-detail-contract-grid");

        grid.addColumn(new ComponentRenderer<>(this::erstelleVertragsNummer))
                .setHeader("Vertrag")
                .setAutoWidth(true)
                .setFlexGrow(1);

        grid.addColumn(UiFormatUtils::formatiereMietobjekt)
                .setHeader("Einheit")
                .setAutoWidth(true)
                .setFlexGrow(2);

        grid.addColumn(UiFormatUtils::formatiereVertragslaufzeit)
                .setHeader("Laufzeit")
                .setAutoWidth(true)
                .setFlexGrow(1);

        grid.addColumn(new ComponentRenderer<>(this::erstelleWarmmieteZelle))
                .setHeader("Warmmiete")
                .setAutoWidth(true)
                .setFlexGrow(1);

        grid.addColumn(new ComponentRenderer<>(this::erstelleVertragsStatusBadge))
                .setHeader("Status")
                .setAutoWidth(true)
                .setFlexGrow(1);

        List<Mietvertrag> vertraege = ladeVertraege();
        grid.setItems(vertraege);

        grid.addItemClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate(
                        MietvertragListView.class,
                        String.valueOf(event.getItem().getId())
                ))
        );

        tableCard.add(header);

        if (vertraege.isEmpty()) {
            tableCard.add(erstelleLeereVertraegeAnzeige());
        } else {
            tableCard.add(grid);
        }

        return tableCard;
    }

    private Component erstelleVertragsNummer(Mietvertrag mietvertrag) {
        Div wrapper = new Div();
        wrapper.addClassName("mieter-detail-contract-number");

        Span number = new Span("MV-" + mietvertrag.getId());
        number.addClassName("mieter-detail-contract-number-main");

        Span hint = new Span("Details öffnen");
        hint.addClassName("mieter-detail-contract-number-hint");

        wrapper.add(number, hint);
        return wrapper;
    }

    private Component erstelleWarmmieteZelle(Mietvertrag mietvertrag) {
        Span value = new Span(UiFormatUtils.formatiereWarmmiete(mietvertrag));
        value.addClassName("mieter-detail-money");
        return value;
    }

    private Component erstelleVertragsStatusBadge(Mietvertrag mietvertrag) {
        Span badge = new Span(UiFormatUtils.formatiereVertragsstatus(mietvertrag));
        badge.addClassNames("status-badge", ermittleVertragsStatusStil(mietvertrag));
        return badge;
    }

    private Component erstelleLeereVertraegeAnzeige() {
        Div empty = new Div();
        empty.addClassName("mieter-detail-empty-contracts");

        Div icon = new Div(VaadinIcon.FILE_TEXT.create());
        icon.addClassName("mieter-detail-empty-icon");

        Span title = new Span("Noch kein Mietvertrag vorhanden");
        title.addClassName("empty-state-title");

        Span text = new Span("Lege einen neuen Mietvertrag an, sobald dieser Mieter einer Einheit zugeordnet wird.");
        text.addClassName("empty-state-text");

        empty.add(icon, title, text);
        return empty;
    }

    // Bankdaten werden nur angezeigt, wenn sie beim Mieter auch wirklich aktiv sind.
    private Component erstelleBankdatenKarte() {
        Div card = erstelleDetailKarte("Bankdaten", "Hinterlegte Zahlungsinformationen", VaadinIcon.CREDIT_CARD);

        if (!aktuellerMieter.isBankdatenAktiv()) {
            Div empty = new Div();
            empty.addClassName("mieter-detail-muted-box");
            empty.add(VaadinIcon.LOCK.create(), new Span("Keine Bankdaten hinterlegt."));
            card.add(empty);
        } else {
            card.add(
                    erstelleInfoZeile(VaadinIcon.USER, "Kontoinhaber", UiFormatUtils.wertOderStrich(aktuellerMieter.getKontoinhaber())),
                    erstelleInfoZeile(VaadinIcon.CREDIT_CARD, "IBAN", UiFormatUtils.maskiereIban(aktuellerMieter.getIban())),
                    erstelleInfoZeile(VaadinIcon.BUILDING, "BIC / Bankname", UiFormatUtils.wertOderStrich(aktuellerMieter.getBic()))
            );
        }

        return card;
    }

    private Div erstelleDetailKarte(String titleText, String subtitleText, VaadinIcon iconType) {
        Div card = new Div();
        card.addClassName("mieter-detail-card");

        Div header = new Div();
        header.addClassName("mieter-detail-card-header");

        Div titleBox = new Div();
        titleBox.addClassName("mieter-detail-card-title-box");

        Span icon = new Span();
        icon.add(iconType.create());
        icon.addClassName("mieter-detail-card-icon");

        Div text = new Div();

        Span title = new Span(titleText);
        title.addClassName("mieter-detail-card-title");

        Span subtitle = new Span(subtitleText);
        subtitle.addClassName("mieter-detail-card-subtitle");

        text.add(title, subtitle);
        titleBox.add(icon, text);
        header.add(titleBox);

        card.add(header);
        return card;
    }

    private Component erstelleInfoZeile(VaadinIcon iconType, String labelText, String valueText) {
        Div row = new Div();
        row.addClassName("mieter-detail-info-row");

        Div icon = new Div(iconType.create());
        icon.addClassName("mieter-detail-info-icon");

        Div text = new Div();
        text.addClassName("mieter-detail-info-text");

        Span label = new Span(labelText);
        label.addClassName("mieter-detail-info-label");

        Span value = new Span(valueText);
        value.addClassName("mieter-detail-info-value");

        text.add(label, value);
        row.add(icon, text);

        return row;
    }

    private FormLayout erstelleBearbeitungsFormular() {
        FormLayout form = new FormLayout();
        form.addClassName("mieter-detail-edit-form");
        form.setWidthFull();
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("620px", 2)
        );

        return form;
    }

    // Beim Speichern werden die Werte aus den Feldern zurück in den geladenen Mieter geschrieben.
    // Falls noch keine Adresse existiert, wird dafür eine neue Adresse angelegt.
    private void speichereAenderungen() {
        try {
            aktuellerMieter.setVorname(vornameField.getValue());
            aktuellerMieter.setNachname(nachnameField.getValue());
            aktuellerMieter.setBeruf(berufField.getValue());
            aktuellerMieter.setEmail(emailField.getValue());
            aktuellerMieter.setTelefonnummer(telefonField.getValue());

            Adresse adresse = aktuellerMieter.getAdresse();

            if (adresse == null) {
                adresse = new Adresse();
                aktuellerMieter.setAdresse(adresse);
            }

            adresse.setStrasse(strasseField.getValue());
            adresse.setHausnummer(hausnummerField.getValue());
            adresse.setPlz(plzField.getValue());
            adresse.setStadt(ortField.getValue());

            mieterService.speichereMieter(aktuellerMieter);

            Notification.show("Mieter wurde aktualisiert");

            bearbeitenAktiv = false;
            aktualisiereAnsicht();

        } catch (Exception ex) {
            Notification.show("Fehler beim Speichern: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
        }
    }

    private Component erstelleNichtGefundenKarte() {
        Div wrapper = new Div();
        wrapper.addClassName("mieter-detail-not-found-wrapper");

        Div card = new Div();
        card.addClassName("empty-state");

        Div icon = new Div(VaadinIcon.SEARCH.create());
        icon.addClassName("mieter-detail-empty-icon");

        Span title = new Span("Mieter nicht gefunden");
        title.addClassName("empty-state-title");

        Span text = new Span("Für diese ID gibt es aktuell keine Daten.");
        text.addClassName("empty-state-text");

        Button backButton = new Button("Zurück zur Übersicht", VaadinIcon.ARROW_LEFT.create());
        backButton.addClassName("primary-button");
        backButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate("mieter-vertraege?tab=mieter"))
        );

        card.add(icon, title, text, backButton);
        wrapper.add(card);

        return wrapper;
    }

    // Archivieren läuft über den Service, weil dort geprüft wird,
    // ob der Mieter noch einen aktiven oder auslaufenden Vertrag hat.
    private void archiviereMieter() {
        try {
            mieterService.archiviereMieter(aktuellerMieter.getId());

            Notification.show("Mieter wurde archiviert");

            getUI().ifPresent(ui -> ui.navigate("mieter-vertraege?tab=mieter"));

        } catch (Exception ex) {
            Notification.show("Fehler beim Archivieren: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
        }
    }

    private void geheZuNeuemMietvertrag() {
        if (aktuellerMieter == null || aktuellerMieter.getId() == null) {
            Notification.show("Mieter wurde nicht gefunden.", 3000, Notification.Position.MIDDLE);
            return;
        }

        getUI().ifPresent(ui -> ui.navigate(
                "mieter-vertraege/mietvertrag-anlegen?mieterId=" + aktuellerMieter.getId()
        ));
    }

    private List<Mietvertrag> ladeVertraege() {
        return mietvertragService.findeMietvertraegeNachMieter(aktuellerMieter.getId());
    }

    // Der Status vom Mieter wird aus seinen Mietverträgen abgeleitet.
    // Ein aktiver Vertrag hat dabei Vorrang vor gekündigten oder beendeten Verträgen.
    private String ermittleMieterStatus() {
        List<Mietvertrag> vertraege = ladeVertraege();

        boolean aktiv = vertraege.stream()
                .anyMatch(vertrag -> vertrag.getStatus() == Vertragsstatus.AKTIV);

        if (aktiv) {
            return "Aktiv";
        }

        boolean gekuendigt = vertraege.stream()
                .anyMatch(vertrag -> vertrag.getStatus() == Vertragsstatus.GEKUENDIGT);

        if (gekuendigt) {
            return "Läuft aus";
        }

        boolean beendet = vertraege.stream()
                .anyMatch(vertrag -> vertrag.getStatus() == Vertragsstatus.BEENDET);

        if (beendet) {
            return "Beendet";
        }

        return "Ohne Vertrag";
    }

    private String ermittleStatusStil(String status) {
        return switch (status) {
            case "Aktiv" -> "success";
            case "Läuft aus", "Beendet" -> "warning";
            default -> "neutral";
        };
    }

    private String ermittleVertragsStatusStil(Mietvertrag mietvertrag) {
        if (mietvertrag == null || mietvertrag.getStatus() == null) {
            return "neutral";
        }

        return switch (mietvertrag.getStatus()) {
            case AKTIV -> "success";
            case GEKUENDIGT -> "warning";
            case BEENDET -> "neutral";
        };
    }

    private double berechneWarmmiete(Mietvertrag mietvertrag) {
        if (mietvertrag == null) {
            return 0;
        }

        double kaltmiete = mietvertrag.getKaltmiete() == null ? 0 : mietvertrag.getKaltmiete();
        double nebenkosten = mietvertrag.getNebenkosten() == null ? 0 : mietvertrag.getNebenkosten();

        return kaltmiete + nebenkosten;
    }

    @Override
    public String getPageTitle() {
        return bearbeitenAktiv ? "Mieter bearbeiten" : "Mieter Details";
    }

    @Override
    public String getPageSubtitle() {
        return bearbeitenAktiv
                ? "Mieterdaten aktualisieren"
                : "Mieterdaten und Mietverträge anzeigen";
    }
}