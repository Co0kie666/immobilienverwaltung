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
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;
import jakarta.annotation.security.PermitAll;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

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
        konfiguriereEingabefelder();
    }

    @Override
    public void setParameter(BeforeEvent event, String mieterId) {
        try {
            Long id = Long.valueOf(mieterId);

            aktuellerMieter = mieterService.findeMieterNachId(id)
                    .orElse(null);

            if (aktuellerMieter == null) {
                removeAll();
                add(createNotFoundCard());
                return;
            }

            bearbeitenAktiv = false;
            renderView();

        } catch (NumberFormatException ex) {
            removeAll();
            add(createNotFoundCard());
        }
    }

    private void renderView() {
        removeAll();

        add(
                createHeroSection(),
                createQuickStats(),
                createContentLayout()
        );
    }

    private void konfiguriereEingabefelder() {
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

    private Component createHeroSection() {
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
        avatar.setText(erstelleInitialen(aktuellerMieter));

        Div titleBox = new Div();
        titleBox.addClassName("mieter-detail-title-box");

        Span eyebrow = new Span("Mieterprofil · ID " + aktuellerMieter.getId());
        eyebrow.addClassName("mieter-detail-eyebrow");

        H2 name = new H2(formatMieterName(aktuellerMieter));
        name.addClassName("mieter-detail-name");

        Div meta = new Div();
        meta.addClassName("mieter-detail-meta");
        meta.add(createMetaPill(VaadinIcon.ENVELOPE, textOderStrich(aktuellerMieter.getEmail())));
        meta.add(createMetaPill(VaadinIcon.PHONE, textOderStrich(aktuellerMieter.getTelefonnummer())));
        meta.add(createMetaPill(VaadinIcon.MAP_MARKER, formatAdresseKurz(aktuellerMieter.getAdresse())));

        titleBox.add(eyebrow, name, meta);
        left.add(backButton, avatar, titleBox);

        Div right = new Div();
        right.addClassName("mieter-detail-hero-actions");

        Span statusBadge = new Span(ermittleMieterStatus());
        statusBadge.addClassNames("status-badge", getStatusStyle(ermittleMieterStatus()), "mieter-detail-status");

        Button editButton;

        if (bearbeitenAktiv) {
            editButton = new Button("Speichern", VaadinIcon.CHECK.create());
            editButton.addClassName("primary-button");
            editButton.addClickListener(event -> speichereAenderungen());

            Button cancelButton = new Button("Abbrechen", VaadinIcon.CLOSE.create());
            cancelButton.addClassName("secondary-button");
            cancelButton.addClickListener(event -> {
                bearbeitenAktiv = false;
                renderView();
            });

            Button archiveButton = new Button("Archivieren", VaadinIcon.TRASH.create());
            archiveButton.addClassName("danger-button");
            archiveButton.addClickListener(event -> archiviereMieter());

            right.add(statusBadge, cancelButton, archiveButton, editButton);
        } else {
            editButton = new Button("Mieter bearbeiten", VaadinIcon.EDIT.create());
            editButton.addClassName("secondary-button");
            editButton.addClickListener(event -> {
                bearbeitenAktiv = true;
                renderView();
            });

            Button newContractButton = new Button("Neuer Mietvertrag", VaadinIcon.PLUS.create());
            newContractButton.addClassName("primary-button");
            newContractButton.addClickListener(event -> navigiereZuNeuemMietvertrag());

            right.add(statusBadge, editButton, newContractButton);
        }

        hero.add(left, right);
        return hero;
    }

    private Component createMetaPill(VaadinIcon icon, String text) {
        Div pill = new Div();
        pill.addClassName("mieter-detail-meta-pill");

        Icon pillIcon = icon.create();
        Span label = new Span(text);

        pill.add(pillIcon, label);
        return pill;
    }

    private Component createQuickStats() {
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
                createStatCard("Mietverträge", String.valueOf(vertraege.size()), "Gesamte Historie", VaadinIcon.FILE_TEXT, "primary"),
                createStatCard("Aktiv", String.valueOf(aktiveVertraege), "Laufende Verträge", VaadinIcon.CHECK_CIRCLE, "success"),
                createStatCard("Läuft aus", String.valueOf(auslaufendeVertraege), "Gekündigte Verträge", VaadinIcon.CLOCK, "warning"),
                createStatCard("Warmmiete", formatiereBetrag(warmmiete), "Aktive Verträge mtl.", VaadinIcon.EURO, "danger")
        );

        return stats;
    }

    private Component createStatCard(String label, String value, String subtitle, VaadinIcon icon, String type) {
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

    private Component createContentLayout() {
        Div contentLayout = new Div();
        contentLayout.addClassName("mieter-detail-content-grid");

        Div leftColumn = new Div();
        leftColumn.addClassName("mieter-detail-side-column");
        leftColumn.add(
                createProfileCard(),
                createKontaktCard(),
                createBankdatenCard()
        );

        Div rightColumn = new Div();
        rightColumn.addClassName("mieter-detail-main-column");
        rightColumn.add(
                createVertraegeCard()
        );

        contentLayout.add(leftColumn, rightColumn);
        return contentLayout;
    }

    private Component createProfileCard() {
        Div card = createDetailCard("Stammdaten", "Personenbezogene Angaben und beruflicher Kontext", VaadinIcon.USER);

        if (bearbeitenAktiv) {
            vornameField.setValue(textOderLeer(aktuellerMieter.getVorname()));
            nachnameField.setValue(textOderLeer(aktuellerMieter.getNachname()));
            berufField.setValue(textOderLeer(aktuellerMieter.getBeruf()));

            FormLayout form = createFormLayout();
            form.add(vornameField, nachnameField, berufField);
            form.setColspan(berufField, 2);

            card.add(form);
        } else {
            card.add(
                    createInfoRow(VaadinIcon.USER, "Vollständiger Name", formatMieterName(aktuellerMieter)),
                    createInfoRow(VaadinIcon.CALENDAR, "Geburtsdatum", formatDatum(aktuellerMieter)),
                    createInfoRow(VaadinIcon.BRIEFCASE, "Beruf / Tätigkeit", textOderStrich(aktuellerMieter.getBeruf()))
            );
        }

        return card;
    }

    private Component createKontaktCard() {
        Div card = createDetailCard("Kontakt & Adresse", "Erreichbarkeit und postalische Daten", VaadinIcon.MAP_MARKER);
        Adresse adresse = aktuellerMieter.getAdresse();

        if (bearbeitenAktiv) {
            emailField.setValue(textOderLeer(aktuellerMieter.getEmail()));
            telefonField.setValue(textOderLeer(aktuellerMieter.getTelefonnummer()));
            strasseField.setValue(adresse == null ? "" : textOderLeer(adresse.getStrasse()));
            hausnummerField.setValue(adresse == null ? "" : textOderLeer(adresse.getHausnummer()));
            plzField.setValue(adresse == null ? "" : textOderLeer(adresse.getPlz()));
            ortField.setValue(adresse == null ? "" : textOderLeer(adresse.getStadt()));

            HorizontalLayout strasseHausnummerLayout = new HorizontalLayout();
            strasseHausnummerLayout.setWidthFull();
            strasseHausnummerLayout.setSpacing(true);

            strasseHausnummerLayout.add(strasseField, hausnummerField);
            strasseHausnummerLayout.setFlexGrow(2, strasseField);
            strasseHausnummerLayout.setFlexGrow(1, hausnummerField);

            FormLayout form = createFormLayout();
            form.add(emailField, telefonField, strasseHausnummerLayout, plzField, ortField);
            form.setColspan(strasseHausnummerLayout, 2);

            card.add(form);
        } else {
            card.add(
                    createInfoRow(VaadinIcon.ENVELOPE, "E-Mail", textOderStrich(aktuellerMieter.getEmail())),
                    createInfoRow(VaadinIcon.PHONE, "Telefon", textOderStrich(aktuellerMieter.getTelefonnummer())),
                    createInfoRow(VaadinIcon.HOME, "Adresse", formatAdresse(adresse))
            );
        }

        return card;
    }

    private Component createVertraegeCard() {
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
        newContractButton.addClickListener(event -> navigiereZuNeuemMietvertrag());

        header.add(titleBox, newContractButton);

        Grid<Mietvertrag> grid = new Grid<>(Mietvertrag.class, false);
        grid.setWidthFull();
        grid.setAllRowsVisible(true);
        grid.addClassName("mieter-detail-contract-grid");

        grid.addColumn(new ComponentRenderer<>(this::createContractNumber))
                .setHeader("Vertrag")
                .setAutoWidth(true)
                .setFlexGrow(1);

        grid.addColumn(this::formatMietobjekt)
                .setHeader("Einheit")
                .setAutoWidth(true)
                .setFlexGrow(2);

        grid.addColumn(this::formatLaufzeit)
                .setHeader("Laufzeit")
                .setAutoWidth(true)
                .setFlexGrow(1);

        grid.addColumn(new ComponentRenderer<>(this::createWarmmieteCell))
                .setHeader("Warmmiete")
                .setAutoWidth(true)
                .setFlexGrow(1);

        grid.addColumn(new ComponentRenderer<>(this::createVertragsStatusBadge))
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
            tableCard.add(createEmptyContractsState());
        } else {
            tableCard.add(grid);
        }

        return tableCard;
    }

    private Component createContractNumber(Mietvertrag mietvertrag) {
        Div wrapper = new Div();
        wrapper.addClassName("mieter-detail-contract-number");

        Span number = new Span("MV-" + mietvertrag.getId());
        number.addClassName("mieter-detail-contract-number-main");

        Span hint = new Span("Details öffnen");
        hint.addClassName("mieter-detail-contract-number-hint");

        wrapper.add(number, hint);
        return wrapper;
    }

    private Component createWarmmieteCell(Mietvertrag mietvertrag) {
        Span value = new Span(formatWarmmiete(mietvertrag));
        value.addClassName("mieter-detail-money");
        return value;
    }

    private Component createVertragsStatusBadge(Mietvertrag mietvertrag) {
        Span badge = new Span(formatStatus(mietvertrag));
        badge.addClassNames("status-badge", getVertragsStatusStyle(mietvertrag));
        return badge;
    }

    private Component createEmptyContractsState() {
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

    private Component createBankdatenCard() {
        Div card = createDetailCard("Bankdaten", "Hinterlegte Zahlungsinformationen", VaadinIcon.CREDIT_CARD);

        if (!aktuellerMieter.isBankdatenAktiv()) {
            Div empty = new Div();
            empty.addClassName("mieter-detail-muted-box");
            empty.add(VaadinIcon.LOCK.create(), new Span("Keine Bankdaten hinterlegt."));
            card.add(empty);
        } else {
            card.add(
                    createInfoRow(VaadinIcon.USER, "Kontoinhaber", textOderStrich(aktuellerMieter.getKontoinhaber())),
                    createInfoRow(VaadinIcon.CREDIT_CARD, "IBAN", maskiereIban(aktuellerMieter.getIban())),
                    createInfoRow(VaadinIcon.BUILDING, "BIC / Bankname", textOderStrich(aktuellerMieter.getBic()))
            );
        }

        return card;
    }

    private Div createDetailCard(String titleText, String subtitleText, VaadinIcon iconType) {
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

    private Component createInfoRow(VaadinIcon iconType, String labelText, String valueText) {
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

    private FormLayout createFormLayout() {
        FormLayout form = new FormLayout();
        form.addClassName("mieter-detail-edit-form");
        form.setWidthFull();
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("620px", 2)
        );

        return form;
    }

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
            renderView();

        } catch (Exception ex) {
            Notification.show("Fehler beim Speichern: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
        }
    }

    private Component createNotFoundCard() {
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

    private void archiviereMieter() {
        try {
            mieterService.archiviereMieter(aktuellerMieter.getId());

            Notification.show("Mieter wurde archiviert");

            getUI().ifPresent(ui -> ui.navigate("mieter-vertraege?tab=mieter"));

        } catch (Exception ex) {
            Notification.show("Fehler beim Archivieren: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
        }
    }

    private void navigiereZuNeuemMietvertrag() {
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

    private String getStatusStyle(String status) {
        return switch (status) {
            case "Aktiv" -> "success";
            case "Läuft aus", "Beendet" -> "warning";
            default -> "neutral";
        };
    }

    private String getVertragsStatusStyle(Mietvertrag mietvertrag) {
        if (mietvertrag == null || mietvertrag.getStatus() == null) {
            return "neutral";
        }

        return switch (mietvertrag.getStatus()) {
            case AKTIV -> "success";
            case GEKUENDIGT -> "warning";
            case BEENDET -> "neutral";
            default -> "neutral";
        };
    }

    private String formatMieterName(Mieter mieter) {
        if (mieter == null) {
            return "-";
        }

        String name = (textOderLeer(mieter.getVorname()) + " " + textOderLeer(mieter.getNachname())).trim();
        return name.isBlank() ? "Unbenannter Mieter" : name;
    }

    private String erstelleInitialen(Mieter mieter) {
        String vorname = textOderLeer(mieter.getVorname()).trim();
        String nachname = textOderLeer(mieter.getNachname()).trim();

        String ersteInitiale = vorname.isBlank() ? "" : vorname.substring(0, 1).toUpperCase(Locale.GERMANY);
        String zweiteInitiale = nachname.isBlank() ? "" : nachname.substring(0, 1).toUpperCase(Locale.GERMANY);
        String initialen = ersteInitiale + zweiteInitiale;

        return initialen.isBlank() ? "M" : initialen;
    }

    private String formatDatum(Mieter mieter) {
        if (mieter.getGeburtsdatum() == null) {
            return "-";
        }

        return mieter.getGeburtsdatum().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }

    private String formatAdresse(Adresse adresse) {
        if (adresse == null) {
            return "-";
        }

        String strasse = textOderLeer(adresse.getStrasse()).trim();
        String hausnummer = textOderLeer(adresse.getHausnummer()).trim();
        String plz = textOderLeer(adresse.getPlz()).trim();
        String stadt = textOderLeer(adresse.getStadt()).trim();

        String zeile1 = (strasse + " " + hausnummer).trim();
        String zeile2 = (plz + " " + stadt).trim();

        if (zeile1.isBlank() && zeile2.isBlank()) {
            return "-";
        }

        if (zeile1.isBlank()) {
            return zeile2;
        }

        if (zeile2.isBlank()) {
            return zeile1;
        }

        return zeile1 + ", " + zeile2;
    }

    private String formatAdresseKurz(Adresse adresse) {
        if (adresse == null) {
            return "Keine Adresse";
        }

        String stadt = textOderLeer(adresse.getStadt()).trim();
        String plz = textOderLeer(adresse.getPlz()).trim();

        String kurz = (plz + " " + stadt).trim();
        return kurz.isBlank() ? "Keine Adresse" : kurz;
    }

    private String formatMietobjekt(Mietvertrag mietvertrag) {
        if (mietvertrag.getMieteinheit() == null) {
            return "-";
        }

        if (mietvertrag.getMieteinheit().getImmobilie() == null) {
            return mietvertrag.getMieteinheit().getBezeichnung();
        }

        return mietvertrag.getMieteinheit().getImmobilie().getBezeichnung()
                + " / "
                + mietvertrag.getMieteinheit().getBezeichnung();
    }

    private String formatLaufzeit(Mietvertrag mietvertrag) {
        String start = mietvertrag.getStartdatum() == null
                ? "-"
                : mietvertrag.getStartdatum().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));

        String ende = mietvertrag.getEnddatum() == null
                ? "unbefristet"
                : mietvertrag.getEnddatum().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));

        return start + " - " + ende;
    }

    private String formatWarmmiete(Mietvertrag mietvertrag) {
        return formatiereBetrag(berechneWarmmiete(mietvertrag));
    }

    private double berechneWarmmiete(Mietvertrag mietvertrag) {
        double kaltmiete = mietvertrag.getKaltmiete() == null ? 0 : mietvertrag.getKaltmiete();
        double nebenkosten = mietvertrag.getNebenkosten() == null ? 0 : mietvertrag.getNebenkosten();
        return kaltmiete + nebenkosten;
    }

    private String formatiereBetrag(double betrag) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        return formatter.format(betrag);
    }

    private String formatStatus(Mietvertrag mietvertrag) {
        if (mietvertrag.getStatus() == null) {
            return "-";
        }

        return mietvertrag.getStatus().getLabel();
    }

    private String maskiereIban(String iban) {
        if (iban == null || iban.isBlank()) {
            return "-";
        }

        String cleaned = iban.replace(" ", "");

        if (cleaned.length() <= 8) {
            return iban;
        }

        return cleaned.substring(0, 4) + " •••• •••• " + cleaned.substring(cleaned.length() - 4);
    }

    private String textOderLeer(String text) {
        return text == null ? "" : text;
    }

    private String textOderStrich(String text) {
        return text == null || text.isBlank() ? "-" : text;
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
