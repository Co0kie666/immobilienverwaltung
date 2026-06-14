package de.hsbi.immobilienverwaltung.ui.mieter;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import de.hsbi.immobilienverwaltung.domain.Mietvertrag;
import de.hsbi.immobilienverwaltung.domain.enums.Vertragsstatus;
import de.hsbi.immobilienverwaltung.service.interfaces.MietvertragService;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;
import jakarta.annotation.security.PermitAll;
import java.time.temporal.ChronoUnit;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Route(value = "mietvertrag-details", layout = MainLayout.class)
@PermitAll
public class MietvertragListView extends Div implements HasPageHeader, HasUrlParameter<String> {

    private final MietvertragService mietvertragService;

    private Mietvertrag aktuellerMietvertrag;
    private boolean bearbeitenAktiv = false;

    private final DatePicker startdatumField = new DatePicker("Vertragsbeginn");
    private final DatePicker enddatumField = new DatePicker("Vertragsende");
    private final DatePicker kuendigungsfristField = new DatePicker("Kündigungsfrist bis");

    private final NumberField kaltmieteField = new NumberField("Kaltmiete");
    private final NumberField nebenkostenField = new NumberField("Nebenkosten");
    private final NumberField kautionField = new NumberField("Kaution");

    public MietvertragListView(MietvertragService mietvertragService) {
        this.mietvertragService = mietvertragService;
        addClassName("page-content");
    }

    @Override
    public void setParameter(BeforeEvent event, String mietvertragId) {
        try {
            Long id = Long.valueOf(mietvertragId);

            aktuellerMietvertrag = mietvertragService.findeMietvertragNachId(id)
                    .orElse(null);

            if (aktuellerMietvertrag == null) {
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
                createMietvertragHeader(),
                createContentLayout()
        );
    }

    private Component createMietvertragHeader() {
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
                getUI().ifPresent(ui -> ui.navigate("mieter-vertraege?tab=vertraege"))
        );

        VerticalLayout titleArea = new VerticalLayout();
        titleArea.setPadding(false);
        titleArea.setSpacing(false);

        HorizontalLayout titleRow = new HorizontalLayout();
        titleRow.setAlignItems(FlexComponent.Alignment.CENTER);
        titleRow.setSpacing(true);

        Span title = new Span("Mietvertrag MV-" + aktuellerMietvertrag.getId());
        title.addClassName("card-title");

        Span statusBadge = new Span(formatStatus());
        statusBadge.addClassNames("status-badge", getStatusStyle());

        titleRow.add(title, statusBadge);

        Span subtitle = new Span(formatMieter() + " • " + formatImmobilie() + " / " + formatMieteinheitKurz());
        subtitle.addClassName("card-subtitle");

        titleArea.add(titleRow, subtitle);

        HorizontalLayout leftArea = new HorizontalLayout();
        leftArea.setAlignItems(FlexComponent.Alignment.CENTER);
        leftArea.setSpacing(true);
        leftArea.add(backButton, titleArea);

        HorizontalLayout rightArea = new HorizontalLayout();
        rightArea.setAlignItems(FlexComponent.Alignment.CENTER);
        rightArea.setSpacing(true);

        Button ansichtButton = new Button("Ansicht");
        ansichtButton.addClassName(bearbeitenAktiv ? "secondary-button" : "primary-button");
        ansichtButton.addClickListener(event -> {
            bearbeitenAktiv = false;
            renderView();
        });

        Button bearbeitenButton = new Button("Bearbeiten");
        bearbeitenButton.addClassName(bearbeitenAktiv ? "primary-button" : "secondary-button");
        bearbeitenButton.setEnabled(aktuellerMietvertrag.getStatus() != Vertragsstatus.BEENDET);
        bearbeitenButton.addClickListener(event -> {
            bearbeitenAktiv = true;
            renderView();
        });

        Button kuendigenButton = new Button("Kündigen", VaadinIcon.TRASH.create());
        boolean kuendigenErlaubt = bearbeitenAktiv && aktuellerMietvertrag.getStatus() == Vertragsstatus.AKTIV;
        kuendigenButton.addClassName(kuendigenErlaubt ? "danger-button" : "secondary-button");
        kuendigenButton.setEnabled(kuendigenErlaubt);
        kuendigenButton.addClickListener(event -> kuendigeMietvertrag());

        Button speichernButton = new Button("Speichern", VaadinIcon.CHECK.create());
        speichernButton.addClassName(bearbeitenAktiv ? "primary-button" : "secondary-button");
        speichernButton.setEnabled(bearbeitenAktiv);
        speichernButton.addClickListener(event -> speichereAenderungen());

        rightArea.add(ansichtButton, bearbeitenButton, kuendigenButton, speichernButton);

        header.add(leftArea, rightArea);
        headerCard.add(header);

        return headerCard;
    }

    private Component createContentLayout() {
        HorizontalLayout contentLayout = new HorizontalLayout();
        contentLayout.setWidthFull();
        contentLayout.setMaxWidth("1400px");
        contentLayout.setSpacing(true);
        contentLayout.setAlignItems(FlexComponent.Alignment.START);
        contentLayout.getStyle().set("margin", "0 auto");
        contentLayout.getStyle().set("box-sizing", "border-box");

        VerticalLayout leftColumn = new VerticalLayout();
        leftColumn.setPadding(false);
        leftColumn.setSpacing(true);
        leftColumn.setWidth("0");
        leftColumn.getStyle().set("min-width", "0");

        leftColumn.add(
                createMieterEinheitCard(),
                createLaufzeitCard()
        );

        VerticalLayout rightColumn = new VerticalLayout();
        rightColumn.setPadding(false);
        rightColumn.setSpacing(true);
        rightColumn.setWidth("380px");
        rightColumn.getStyle().set("min-width", "360px");

        rightColumn.add(
                createFinanzenCard(),
                createNavigationButtons()
        );

        contentLayout.add(leftColumn, rightColumn);
        contentLayout.setFlexGrow(1, leftColumn);
        contentLayout.setFlexGrow(0, rightColumn);

        return contentLayout;
    }

    private Component createMieterEinheitCard() {
        Div card = new Div();
        card.addClassName("card");
        card.setWidthFull();

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        Span title = new Span("Mieter & Einheit");
        title.addClassName("card-title");

        content.add(
                title,
                createReadonlyInfoBlock("Hauptmieter", formatMieter()),
                createReadonlyInfoBlock("Mietobjekt", formatImmobilie()),
                createReadonlyInfoBlock("Einheit", formatMieteinheitDetails())
        );

        card.add(content);
        return card;
    }

    private Component createLaufzeitCard() {
        Div card = new Div();
        card.addClassName("card");
        card.setWidthFull();

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);
        content.setWidthFull();

        Span title = new Span("Vertragslaufzeit & Fristen");
        title.addClassName("card-title");

        if (bearbeitenAktiv) {
            startdatumField.setValue(aktuellerMietvertrag.getStartdatum());
            enddatumField.setValue(aktuellerMietvertrag.getEnddatum());
            kuendigungsfristField.setValue(aktuellerMietvertrag.getKuendigungsfrist());

            startdatumField.setRequiredIndicatorVisible(true);

            content.add(title, startdatumField, enddatumField, kuendigungsfristField);
        } else {
            content.add(
                    title,
                    createReadonlyInfoBlock("Vertragsbeginn", formatDatum(aktuellerMietvertrag.getStartdatum())),
                    createReadonlyInfoBlock("Vertragsende", formatDatumOderUnbefristet()),
                    createReadonlyInfoBlock("Kündigungsfrist", formatKuendigungsfrist())
            );
        }

        card.add(content);
        return card;
    }

    private String formatKuendigungsfrist() {
        if (aktuellerMietvertrag.getStartdatum() == null
                || aktuellerMietvertrag.getKuendigungsfrist() == null) {
            return "-";
        }

        long monate = ChronoUnit.MONTHS.between(
                aktuellerMietvertrag.getStartdatum(),
                aktuellerMietvertrag.getKuendigungsfrist()
        );

        if (monate <= 0) {
            return "-";
        }

        if (monate == 1) {
            return "1 Monat";
        }

        return monate + " Monate";
    }

    private Component createFinanzenCard() {
        Div card = new Div();
        card.addClassName("card");
        card.setWidthFull();

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);
        content.setWidthFull();

        Span title = new Span("Finanzen");
        title.addClassName("card-title");

        if (bearbeitenAktiv) {
            kaltmieteField.setValue(aktuellerMietvertrag.getKaltmiete());
            nebenkostenField.setValue(aktuellerMietvertrag.getNebenkosten());
            kautionField.setValue(aktuellerMietvertrag.getKaution());

            kaltmieteField.setSuffixComponent(new Span("€"));
            nebenkostenField.setSuffixComponent(new Span("€"));
            kautionField.setSuffixComponent(new Span("€"));

            content.add(title, kaltmieteField, nebenkostenField, kautionField);
        } else {
            content.add(
                    title,
                    createReadonlyInfoBlock("Kaltmiete", formatEuro(aktuellerMietvertrag.getKaltmiete())),
                    createReadonlyInfoBlock("Nebenkosten", formatEuro(aktuellerMietvertrag.getNebenkosten())),
                    createReadonlyInfoBlock("Warmmiete", formatWarmmiete()),
                    createReadonlyInfoBlock("Kaution", formatEuro(aktuellerMietvertrag.getKaution()))
            );
        }

        card.add(content);
        return card;
    }

    private void navigiereZumMieter() {
        if (aktuellerMietvertrag.getMieter() == null || aktuellerMietvertrag.getMieter().getId() == null) {
            Notification.show("Mieter wurde nicht gefunden.", 3000, Notification.Position.MIDDLE);
            return;
        }

        getUI().ifPresent(ui -> ui.navigate(
                MieterListView.class,
                String.valueOf(aktuellerMietvertrag.getMieter().getId())
        ));
    }

    private void navigiereZurMieteinheit() {
        if (aktuellerMietvertrag.getMieteinheit() == null
                || aktuellerMietvertrag.getMieteinheit().getId() == null
                || aktuellerMietvertrag.getMieteinheit().getImmobilie() == null
                || aktuellerMietvertrag.getMieteinheit().getImmobilie().getId() == null) {
            Notification.show("Mieteinheit wurde nicht gefunden.", 3000, Notification.Position.MIDDLE);
            return;
        }

        Long immobilieId = aktuellerMietvertrag.getMieteinheit().getImmobilie().getId();
        Long mieteinheitId = aktuellerMietvertrag.getMieteinheit().getId();

        getUI().ifPresent(ui -> ui.navigate(
                "immobilien/" + immobilieId + "/einheiten/" + mieteinheitId + "/details"
        ));
    }

    private Component createNavigationButtons() {
        Div buttonRow = new Div();
        buttonRow.setWidthFull();
        buttonRow.getStyle().set("display", "grid");
        buttonRow.getStyle().set("grid-template-columns", "repeat(2, minmax(0, 1fr))");
        buttonRow.getStyle().set("gap", "12px");

        Button mieterButton = new Button("Zum Mieter", VaadinIcon.USER.create());
        mieterButton.addClassName("primary-button");
        mieterButton.setWidthFull();
        mieterButton.addClickListener(event -> navigiereZumMieter());

        Button einheitButton = new Button("Zur Einheit", VaadinIcon.HOME.create());
        einheitButton.addClassName("primary-button");
        einheitButton.setWidthFull();
        einheitButton.addClickListener(event -> navigiereZurMieteinheit());

        buttonRow.add(mieterButton, einheitButton);

        return buttonRow;
    }



    private void speichereAenderungen() {
        try {
            if (startdatumField.getValue() == null) {
                startdatumField.setInvalid(true);
                startdatumField.setErrorMessage("Bitte Vertragsbeginn auswählen");
                throw new IllegalArgumentException("Startdatum muss angegeben werden.");
            }

            aktuellerMietvertrag.setStartdatum(startdatumField.getValue());
            aktuellerMietvertrag.setEnddatum(enddatumField.getValue());
            aktuellerMietvertrag.setKuendigungsfrist(kuendigungsfristField.getValue());
            aktuellerMietvertrag.setKaltmiete(kaltmieteField.getValue());
            aktuellerMietvertrag.setNebenkosten(nebenkostenField.getValue());
            aktuellerMietvertrag.setKaution(kautionField.getValue());

            mietvertragService.speichereMietvertrag(
                    aktuellerMietvertrag.getMieter().getId(),
                    aktuellerMietvertrag.getMieteinheit().getId(),
                    aktuellerMietvertrag
            );

            Notification.show("Mietvertrag wurde aktualisiert");

            bearbeitenAktiv = false;
            renderView();

        } catch (Exception ex) {
            Notification.show("Fehler beim Speichern: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
        }
    }

    private void kuendigeMietvertrag() {
        try {
            mietvertragService.kuendigeMietvertrag(aktuellerMietvertrag.getId());

            aktuellerMietvertrag = mietvertragService.findeMietvertragNachId(aktuellerMietvertrag.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Mietvertrag wurde nicht gefunden."));

            Notification.show("Mietvertrag wurde gekündigt");

            bearbeitenAktiv = false;
            renderView();

        } catch (Exception ex) {
            Notification.show("Fehler beim Kündigen: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
        }
    }

    private Component createReadonlyInfoBlock(String labelText, String valueText) {
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

        Span title = new Span("Mietvertrag nicht gefunden");
        title.addClassName("empty-state-title");

        Span text = new Span("Für diese ID gibt es aktuell keine Daten.");
        text.addClassName("empty-state-text");

        card.add(title, text);

        return card;
    }

    private String formatMieter() {
        if (aktuellerMietvertrag.getMieter() == null) {
            return "-";
        }

        return textOderLeer(aktuellerMietvertrag.getMieter().getVorname()) + " "
                + textOderLeer(aktuellerMietvertrag.getMieter().getNachname())
                + " • "
                + textOderLeer(aktuellerMietvertrag.getMieter().getEmail());
    }

    private String formatImmobilie() {
        if (aktuellerMietvertrag.getMieteinheit() == null
                || aktuellerMietvertrag.getMieteinheit().getImmobilie() == null) {
            return "-";
        }

        return aktuellerMietvertrag.getMieteinheit().getImmobilie().getBezeichnung();
    }

    private String formatMieteinheitDetails() {
        if (aktuellerMietvertrag.getMieteinheit() == null) {
            return "-";
        }

        String details = aktuellerMietvertrag.getMieteinheit().getBezeichnung();

        if (aktuellerMietvertrag.getMieteinheit().getGroesse() != null) {
            details += " • " + aktuellerMietvertrag.getMieteinheit().getGroesse() + " m²";
        }

        if (aktuellerMietvertrag.getMieteinheit().getZimmerzahl() != null) {
            details += " • " + aktuellerMietvertrag.getMieteinheit().getZimmerzahl() + " Zimmer";
        }

        if (aktuellerMietvertrag.getMieteinheit().getStockwerk() != null
                && !aktuellerMietvertrag.getMieteinheit().getStockwerk().isBlank()) {
            details += " • " + aktuellerMietvertrag.getMieteinheit().getStockwerk();
        }

        return details;
    }

    private String formatDatum(java.time.LocalDate datum) {
        if (datum == null) {
            return "-";
        }

        return datum.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }

    private String formatDatumOderUnbefristet() {
        if (aktuellerMietvertrag.getEnddatum() == null) {
            return "unbefristet";
        }

        return formatDatum(aktuellerMietvertrag.getEnddatum());
    }

    private String formatEuro(Double betrag) {
        if (betrag == null) {
            return "-";
        }

        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        return formatter.format(betrag);
    }

    private String formatWarmmiete() {
        double kaltmiete = aktuellerMietvertrag.getKaltmiete() == null ? 0 : aktuellerMietvertrag.getKaltmiete();
        double nebenkosten = aktuellerMietvertrag.getNebenkosten() == null ? 0 : aktuellerMietvertrag.getNebenkosten();

        return formatEuro(kaltmiete + nebenkosten);
    }

    private String formatStatus() {
        if (aktuellerMietvertrag.getStatus() == null) {
            return "-";
        }

        return aktuellerMietvertrag.getStatus().getLabel();
    }

    private String formatMieteinheitKurz() {
        if (aktuellerMietvertrag.getMieteinheit() == null) {
            return "-";
        }

        return aktuellerMietvertrag.getMieteinheit().getBezeichnung();
    }

    private String getStatusStyle() {
        if (aktuellerMietvertrag.getStatus() == null) {
            return "neutral";
        }

        return switch (aktuellerMietvertrag.getStatus()) {
            case AKTIV -> "success";
            case GEKUENDIGT, BEENDET -> "warning";
        };
    }

    private String textOderLeer(String text) {
        return text == null ? "" : text;
    }

    @Override
    public String getPageTitle() {
        return "Mietvertrag";
    }

    @Override
    public String getPageSubtitle() {
        return "Mietvertragsdaten anzeigen und bearbeiten";
    }
}