package de.hsbi.immobilienverwaltung.ui.mieter;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
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

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Route(value = "mieter-details", layout = MainLayout.class)
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
    private final TextField strasseField = new TextField("Straße und Hausnummer");
    private final TextField plzField = new TextField("PLZ");
    private final TextField ortField = new TextField("Ort");

    public MieterListView(MieterService mieterService, MietvertragService mietvertragService) {
        this.mieterService = mieterService;
        this.mietvertragService = mietvertragService;

        addClassName("page-content");
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
                createMieterHeader(),
                createContentLayout()
        );
    }

    private Component createMieterHeader() {
        Div headerCard = new Div();
        headerCard.addClassName("card");
        headerCard.addClassName("page-section");

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Button backButton = new Button(VaadinIcon.ARROW_LEFT.create());
        backButton.addClassName("icon-button");
        backButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate("mieter-vertraege?tab=mieter"))
        );

        VerticalLayout textArea = new VerticalLayout();
        textArea.setPadding(false);
        textArea.setSpacing(false);

        Span name = new Span(formatMieterName(aktuellerMieter));
        name.addClassName("card-title");

        Span mieterInfo = new Span("Mieter-ID: " + aktuellerMieter.getId());
        mieterInfo.addClassName("card-subtitle");

        textArea.add(name, mieterInfo);

        Span statusBadge = new Span(ermittleMieterStatus());
        statusBadge.addClassNames("status-badge", getStatusStyle(ermittleMieterStatus()));

        HorizontalLayout leftArea = new HorizontalLayout();
        leftArea.setAlignItems(FlexComponent.Alignment.CENTER);
        leftArea.setSpacing(true);
        leftArea.add(backButton, textArea, statusBadge);

        Button editButton;

        if (bearbeitenAktiv) {
            editButton = new Button("Speichern", VaadinIcon.CHECK.create());
            editButton.addClassName("primary-button");
            editButton.addClickListener(event -> speichereAenderungen());
        } else {
            editButton = new Button("Mieter bearbeiten", VaadinIcon.EDIT.create());
            editButton.addClassName("secondary-button");
            editButton.addClickListener(event -> {
                bearbeitenAktiv = true;
                renderView();
            });
        }

        Button newContractButton = new Button("Neuer Mietvertrag", VaadinIcon.PLUS.create());
        newContractButton.addClassName("primary-button");
        newContractButton.setEnabled(!bearbeitenAktiv);
        newContractButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate(MietvertragFormView.class))
        );

        Button archiveButton = new Button("Mieter archivieren", VaadinIcon.TRASH.create());
        archiveButton.addClassName("danger-button");
        archiveButton.setVisible(bearbeitenAktiv);
        archiveButton.addClickListener(event -> archiviereMieter());

        HorizontalLayout rightArea = new HorizontalLayout();
        rightArea.setAlignItems(FlexComponent.Alignment.CENTER);
        rightArea.setSpacing(true);
        rightArea.add(editButton);

        if (bearbeitenAktiv) {
            rightArea.add(archiveButton);
        }

        rightArea.add(newContractButton);

        header.add(leftArea, rightArea);
        headerCard.add(header);

        return headerCard;
    }

    private Component createContentLayout() {
        HorizontalLayout contentLayout = new HorizontalLayout();
        contentLayout.setWidthFull();
        contentLayout.setSpacing(true);
        contentLayout.setAlignItems(FlexComponent.Alignment.START);

        VerticalLayout leftColumn = new VerticalLayout();
        leftColumn.setPadding(false);
        leftColumn.setSpacing(true);
        leftColumn.setWidth("430px");

        leftColumn.add(
                createStammdatenCard(),
                createKontaktCard()
        );

        VerticalLayout rightColumn = new VerticalLayout();
        rightColumn.setPadding(false);
        rightColumn.setSpacing(true);
        rightColumn.setWidthFull();

        rightColumn.add(
                createVertraegeCard(),
                createBankdatenCard()
        );

        contentLayout.add(leftColumn, rightColumn);
        contentLayout.setFlexGrow(0, leftColumn);
        contentLayout.setFlexGrow(1, rightColumn);

        return contentLayout;
    }

    private Component createStammdatenCard() {
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
            vornameField.setValue(textOderLeer(aktuellerMieter.getVorname()));
            nachnameField.setValue(textOderLeer(aktuellerMieter.getNachname()));
            berufField.setValue(textOderLeer(aktuellerMieter.getBeruf()));

            content.add(
                    vornameField,
                    nachnameField,
                    berufField,
                    createInfoBlock("Geburtsdatum", formatDatum(aktuellerMieter))
            );
        } else {
            content.add(
                    createInfoBlock("Vollständiger Name", formatMieterName(aktuellerMieter)),
                    createInfoBlock("Geburtsdatum", formatDatum(aktuellerMieter)),
                    createInfoBlock("Beruf / Tätigkeit", textOderStrich(aktuellerMieter.getBeruf()))
            );
        }

        card.add(content);
        return card;
    }

    private Component createKontaktCard() {
        Div card = new Div();
        card.addClassName("card");
        card.setWidthFull();

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        Span title = new Span("Kontaktinformationen");
        title.addClassName("card-title");

        content.add(title);

        Adresse adresse = aktuellerMieter.getAdresse();

        if (bearbeitenAktiv) {
            emailField.setValue(textOderLeer(aktuellerMieter.getEmail()));
            telefonField.setValue(textOderLeer(aktuellerMieter.getTelefonnummer()));
            strasseField.setValue(adresse == null ? "" : textOderLeer(adresse.getStrasse()));
            plzField.setValue(adresse == null ? "" : textOderLeer(adresse.getPlz()));
            ortField.setValue(adresse == null ? "" : textOderLeer(adresse.getStadt()));

            content.add(emailField, telefonField, strasseField, plzField, ortField);
        } else {
            content.add(
                    createInfoBlock("E-Mail", textOderStrich(aktuellerMieter.getEmail())),
                    createInfoBlock("Telefon", textOderStrich(aktuellerMieter.getTelefonnummer())),
                    createInfoBlock("Adresse", formatAdresse(adresse))
            );
        }

        card.add(content);
        return card;
    }

    private Component createVertraegeCard() {
        Div tableCard = new Div();
        tableCard.addClassName("table-card");
        tableCard.setWidthFull();

        HorizontalLayout header = new HorizontalLayout();
        header.addClassName("table-card-header");
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Span title = new Span("Mietverträge");
        title.addClassName("card-title");

        header.add(title);

        Grid<Mietvertrag> grid = new Grid<>(Mietvertrag.class, false);
        grid.setWidthFull();
        grid.setAllRowsVisible(true);

        grid.addColumn(mietvertrag -> "MV-" + mietvertrag.getId())
                .setHeader("Vertrag")
                .setAutoWidth(true);

        grid.addColumn(this::formatMietobjekt)
                .setHeader("Einheit")
                .setAutoWidth(true)
                .setFlexGrow(2);

        grid.addColumn(this::formatLaufzeit)
                .setHeader("Laufzeit")
                .setAutoWidth(true);

        grid.addColumn(this::formatWarmmiete)
                .setHeader("Warmmiete")
                .setAutoWidth(true);

        grid.addColumn(this::formatStatus)
                .setHeader("Status")
                .setAutoWidth(true);

        List<Mietvertrag> vertraege =
                mietvertragService.findeMietvertraegeNachMieter(aktuellerMieter.getId());

        grid.setItems(vertraege);

        grid.addItemDoubleClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate(
                        MietvertragListView.class,
                        String.valueOf(event.getItem().getId())
                ))
        );

        tableCard.add(header, grid);
        return tableCard;
    }

    private Component createBankdatenCard() {
        Div card = new Div();
        card.addClassName("card");
        card.setWidthFull();

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        Span title = new Span("Bankdaten");
        title.addClassName("card-title");

        content.add(title);

        if (!aktuellerMieter.isBankdatenAktiv()) {
            Span empty = new Span("Keine Bankdaten hinterlegt.");
            empty.addClassName("card-subtitle");
            content.add(empty);
        } else {
            content.add(
                    createInfoBlock("Kontoinhaber", textOderStrich(aktuellerMieter.getKontoinhaber())),
                    createInfoBlock("IBAN", textOderStrich(aktuellerMieter.getIban())),
                    createInfoBlock("BIC / Bankname", textOderStrich(aktuellerMieter.getBic()))
            );
        }

        card.add(content);
        return card;
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
            adresse.setHausnummer("");
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

    private Component createNotFoundCard() {
        Div card = new Div();
        card.addClassName("empty-state");

        Span title = new Span("Mieter nicht gefunden");
        title.addClassName("empty-state-title");

        Span text = new Span("Für diese ID gibt es aktuell keine Daten.");
        text.addClassName("empty-state-text");

        card.add(title, text);

        return card;
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

    private String ermittleMieterStatus() {
        List<Mietvertrag> vertraege = mietvertragService.findeMietvertraegeNachMieter(aktuellerMieter.getId());

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

    private String formatMieterName(Mieter mieter) {
        return textOderLeer(mieter.getVorname()) + " " + textOderLeer(mieter.getNachname());
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

        return textOderLeer(adresse.getStrasse()) + ", "
                + textOderLeer(adresse.getPlz()) + " "
                + textOderLeer(adresse.getStadt());
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
        double kaltmiete = mietvertrag.getKaltmiete() == null ? 0 : mietvertrag.getKaltmiete();
        double nebenkosten = mietvertrag.getNebenkosten() == null ? 0 : mietvertrag.getNebenkosten();

        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        return formatter.format(kaltmiete + nebenkosten);
    }

    private String formatStatus(Mietvertrag mietvertrag) {
        if (mietvertrag.getStatus() == null) {
            return "-";
        }

        return mietvertrag.getStatus().getLabel();
    }

    private String textOderLeer(String text) {
        return text == null ? "" : text;
    }

    private String textOderStrich(String text) {
        return text == null || text.isBlank() ? "-" : text;
    }

    @Override
    public String getPageTitle() {
        return "Mieter Details";
    }

    @Override
    public String getPageSubtitle() {
        return "Mieterdaten und Mietverträge anzeigen";
    }
}