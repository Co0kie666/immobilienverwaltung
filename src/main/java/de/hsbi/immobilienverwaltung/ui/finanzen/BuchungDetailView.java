package de.hsbi.immobilienverwaltung.ui.finanzen;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import de.hsbi.immobilienverwaltung.domain.Ausgabe;
import de.hsbi.immobilienverwaltung.domain.Zahlungseingang;
import de.hsbi.immobilienverwaltung.domain.enums.Ausgabenkategorie;
import de.hsbi.immobilienverwaltung.domain.enums.Zahlungseingangtyp;
import de.hsbi.immobilienverwaltung.service.interfaces.AusgabeService;
import de.hsbi.immobilienverwaltung.service.interfaces.ZahlungsEingangService;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;
import jakarta.annotation.security.PermitAll;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Route(value = "finanzen/buchungen/:typ/:id", layout = MainLayout.class)
@PermitAll
public class BuchungDetailView extends VerticalLayout implements HasPageHeader, BeforeEnterObserver {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final AusgabeService ausgabeService;
    private final ZahlungsEingangService zahlungsEingangService;

    private String typ;
    private Long id;

    private Ausgabe aktuelleAusgabe;
    private Zahlungseingang aktuellerZahlungseingang;

    private BigDecimalField betragField;
    private DatePicker datumField;
    private DatePicker zweitesDatumField;
    private ComboBox<Ausgabenkategorie> ausgabeKategorieField;
    private ComboBox<Zahlungseingangtyp> zahlungseingangTypField;
    private ComboBox<String> statusField;
    private TextArea beschreibungField;

    private H2 title;
    private Paragraph subtitle;
    private Span heroBadge;
    private Span heroAmount;
    private Span heroStatus;
    private Span heroDate;
    private Span heroCategory;

    public BuchungDetailView(
            AusgabeService ausgabeService,
            ZahlungsEingangService zahlungsEingangService
    ) {
        this.ausgabeService = ausgabeService;
        this.zahlungsEingangService = zahlungsEingangService;

        addClassNames("page-content", "buchung-detail-view");
        setPadding(false);
        setSpacing(false);
        setWidthFull();

        createFields();

        add(createContent());
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        typ = event.getRouteParameters().get("typ").orElse("");
        String idText = event.getRouteParameters().get("id").orElse("");

        try {
            id = Long.parseLong(idText);
        } catch (NumberFormatException e) {
            Notification.show("Ungültige Buchungs-ID.");
            event.rerouteTo(BuchungListView.class);
            return;
        }

        if ("ausgabe".equals(typ)) {
            ladeAusgabe(event);
        } else if ("einnahme".equals(typ)) {
            ladeZahlungseingang(event);
        } else {
            Notification.show("Unbekannter Buchungstyp.");
            event.rerouteTo(BuchungListView.class);
        }
    }

    private void createFields() {
        betragField = new BigDecimalField("Betrag");
        betragField.setPrefixComponent(new Span("€"));
        betragField.setWidthFull();
        betragField.setRequiredIndicatorVisible(true);

        datumField = new DatePicker("Datum");
        datumField.setWidthFull();
        datumField.setRequiredIndicatorVisible(true);

        zweitesDatumField = new DatePicker("Fälligkeitsdatum / Leistungsmonat");
        zweitesDatumField.setWidthFull();

        ausgabeKategorieField = new ComboBox<>("Kategorie");
        ausgabeKategorieField.setItems(Ausgabenkategorie.values());
        ausgabeKategorieField.setItemLabelGenerator(Ausgabenkategorie::getLabel);
        ausgabeKategorieField.setWidthFull();
        ausgabeKategorieField.setRequiredIndicatorVisible(true);

        zahlungseingangTypField = new ComboBox<>("Zahlungstyp");
        zahlungseingangTypField.setItems(Zahlungseingangtyp.values());
        zahlungseingangTypField.setItemLabelGenerator(Zahlungseingangtyp::getLabel);
        zahlungseingangTypField.setWidthFull();
        zahlungseingangTypField.setRequiredIndicatorVisible(true);

        statusField = new ComboBox<>("Status");
        statusField.setItems("Bezahlt / Erledigt", "Offen / Ausstehend");
        statusField.setWidthFull();
        statusField.setRequiredIndicatorVisible(true);

        beschreibungField = new TextArea("Beschreibung / Notiz");
        beschreibungField.setWidthFull();
        beschreibungField.setHeight("140px");

        betragField.addValueChangeListener(event -> updateHero());
        datumField.addValueChangeListener(event -> updateHero());
        zweitesDatumField.addValueChangeListener(event -> updateHero());
        ausgabeKategorieField.addValueChangeListener(event -> updateHero());
        zahlungseingangTypField.addValueChangeListener(event -> updateHero());
        statusField.addValueChangeListener(event -> updateHero());
        beschreibungField.addValueChangeListener(event -> updateHero());
    }

    private Component createContent() {
        Div shell = new Div();
        shell.addClassName("buchung-detail-shell");

        shell.add(
                createHeroCard(),
                createFormCard()
        );

        return shell;
    }

    private Component createHeroCard() {
        Div hero = new Div();
        hero.addClassName("buchung-detail-hero");

        Div left = new Div();

        Span eyebrow = new Span("Buchung bearbeiten");
        eyebrow.addClassName("buchungen-eyebrow");

        title = new H2("Buchung bearbeiten");
        title.addClassName("buchungen-hero-title");

        subtitle = new Paragraph("Passe Buchungsdaten, Status und Notizen an.");
        subtitle.addClassName("buchungen-hero-subtitle");

        heroBadge = new Span("-");
        heroBadge.addClassName("buchung-type-badge");

        left.add(eyebrow, title, subtitle, heroBadge);

        Div summary = new Div();
        summary.addClassName("buchung-detail-summary");

        heroAmount = new Span("0,00 €");
        heroAmount.addClassName("buchung-detail-amount");

        heroStatus = new Span("-");
        heroDate = new Span("-");
        heroCategory = new Span("-");

        summary.add(
                heroAmount,
                createSummaryRow("Status", heroStatus),
                createSummaryRow("Datum", heroDate),
                createSummaryRow("Kategorie", heroCategory)
        );

        hero.add(left, summary);

        return hero;
    }

    private Component createSummaryRow(String label, Span value) {
        Div row = new Div();
        row.addClassName("buchung-detail-summary-row");

        Span labelSpan = new Span(label);
        labelSpan.addClassName("buchung-detail-summary-label");

        value.addClassName("buchung-detail-summary-value");

        row.add(labelSpan, value);

        return row;
    }

    private Component createFormCard() {
        Div card = createCard(
                "Buchungsdaten",
                "Bearbeite Betrag, Datum, Status und Beschreibung der Buchung."
        );

        FormLayout form = createTwoColumnFormLayout();

        form.add(
                betragField,
                datumField,
                ausgabeKategorieField,
                zahlungseingangTypField,
                statusField,
                zweitesDatumField
        );

        beschreibungField.setWidthFull();

        card.add(form, beschreibungField, createActions());

        return card;
    }

    private Component createActions() {
        Div actions = new Div();
        actions.addClassName("buchung-detail-actions");

        Button backButton = new Button("Zurück", VaadinIcon.ARROW_LEFT.create());
        backButton.addClassName("secondary-button");
        backButton.addClickListener(event ->
                UI.getCurrent().navigate(BuchungListView.class)
        );

        Button deleteButton = new Button("Löschen", VaadinIcon.TRASH.create());
        deleteButton.addClassName("danger-button");
        deleteButton.addClickListener(event -> loescheBuchung());

        Button saveButton = new Button("Änderungen speichern", VaadinIcon.CHECK.create());
        saveButton.addClassName("primary-button");
        saveButton.addClickListener(event -> speichereBuchung());

        actions.add(backButton, deleteButton, saveButton);

        return actions;
    }

    private void ladeAusgabe(BeforeEnterEvent event) {
        aktuelleAusgabe = ausgabeService.findeAusgabeNachId(id)
                .orElse(null);

        if (aktuelleAusgabe == null) {
            Notification.show("Ausgabe wurde nicht gefunden.");
            event.rerouteTo(BuchungListView.class);
            return;
        }

        title.setText("Ausgabe bearbeiten");
        subtitle.setText("Ausgabe #" + id + " bearbeiten und bei Bedarf als erledigt markieren.");

        betragField.setValue(aktuelleAusgabe.getBetrag());
        datumField.setValue(aktuelleAusgabe.getDatum());
        zweitesDatumField.setValue(aktuelleAusgabe.getFaelligkeitsdatum());
        zweitesDatumField.setLabel("Fälligkeitsdatum");
        ausgabeKategorieField.setValue(aktuelleAusgabe.getKategorie());
        statusField.setValue(aktuelleAusgabe.getStatus());
        beschreibungField.setValue(aktuelleAusgabe.getBeschreibung() != null ? aktuelleAusgabe.getBeschreibung() : "");

        ausgabeKategorieField.setVisible(true);
        zahlungseingangTypField.setVisible(false);

        updateHero();
    }

    private void ladeZahlungseingang(BeforeEnterEvent event) {
        aktuellerZahlungseingang = zahlungsEingangService.findeZahlungseingangNachId(id)
                .orElse(null);

        if (aktuellerZahlungseingang == null) {
            Notification.show("Zahlungseingang wurde nicht gefunden.");
            event.rerouteTo(BuchungListView.class);
            return;
        }

        title.setText("Einnahme bearbeiten");
        subtitle.setText("Einnahme #" + id + " bearbeiten und dem passenden Leistungsmonat zuordnen.");

        betragField.setValue(aktuellerZahlungseingang.getBetrag());
        datumField.setValue(aktuellerZahlungseingang.getZahlungsdatum());
        zweitesDatumField.setValue(aktuellerZahlungseingang.getLeistungsmonat());
        zweitesDatumField.setLabel("Leistungsmonat");
        zahlungseingangTypField.setValue(aktuellerZahlungseingang.getTyp());
        statusField.setValue(aktuellerZahlungseingang.getStatus());
        beschreibungField.setValue(aktuellerZahlungseingang.getBeschreibung() != null ? aktuellerZahlungseingang.getBeschreibung() : "");

        ausgabeKategorieField.setVisible(false);
        zahlungseingangTypField.setVisible(true);

        updateHero();
    }

    private void speichereBuchung() {
        try {
            pruefePflichtfelder();

            if ("ausgabe".equals(typ)) {
                aktuelleAusgabe.setBetrag(betragField.getValue());
                aktuelleAusgabe.setDatum(datumField.getValue());
                aktuelleAusgabe.setFaelligkeitsdatum(zweitesDatumField.getValue());
                aktuelleAusgabe.setKategorie(ausgabeKategorieField.getValue());
                aktuelleAusgabe.setStatus(statusField.getValue());
                aktuelleAusgabe.setBeschreibung(beschreibungField.getValue());

                ausgabeService.speichereAusgabe(aktuelleAusgabe);

                Notification.show("Ausgabe gespeichert");
                UI.getCurrent().navigate(BuchungListView.class);

                return;
            }

            if ("einnahme".equals(typ)) {
                aktuellerZahlungseingang.setBetrag(betragField.getValue());
                aktuellerZahlungseingang.setZahlungsdatum(datumField.getValue());
                aktuellerZahlungseingang.setLeistungsmonat(zweitesDatumField.getValue());
                aktuellerZahlungseingang.setTyp(zahlungseingangTypField.getValue());
                aktuellerZahlungseingang.setStatus(statusField.getValue());
                aktuellerZahlungseingang.setBeschreibung(beschreibungField.getValue());

                zahlungsEingangService.speichereZahlungseingang(aktuellerZahlungseingang);

                Notification.show("Zahlungseingang gespeichert");
                UI.getCurrent().navigate(BuchungListView.class);
            }
        } catch (IllegalArgumentException e) {
            Notification.show(e.getMessage(), 4000, Notification.Position.MIDDLE);
        }
    }

    private void pruefePflichtfelder() {
        boolean fehler = false;

        boolean betragFehlt = betragField.getValue() == null;
        betragField.setInvalid(betragFehlt);
        betragField.setErrorMessage("Bitte Betrag eingeben");
        fehler |= betragFehlt;

        boolean datumFehlt = datumField.getValue() == null;
        datumField.setInvalid(datumFehlt);
        datumField.setErrorMessage("Bitte Datum auswählen");
        fehler |= datumFehlt;

        boolean statusFehlt = statusField.getValue() == null;
        statusField.setInvalid(statusFehlt);
        statusField.setErrorMessage("Bitte Status auswählen");
        fehler |= statusFehlt;

        if ("ausgabe".equals(typ)) {
            boolean kategorieFehlt = ausgabeKategorieField.getValue() == null;
            ausgabeKategorieField.setInvalid(kategorieFehlt);
            ausgabeKategorieField.setErrorMessage("Bitte Kategorie auswählen");
            fehler |= kategorieFehlt;
        }

        if ("einnahme".equals(typ)) {
            boolean typFehlt = zahlungseingangTypField.getValue() == null;
            zahlungseingangTypField.setInvalid(typFehlt);
            zahlungseingangTypField.setErrorMessage("Bitte Zahlungstyp auswählen");
            fehler |= typFehlt;
        }

        if (fehler) {
            throw new IllegalArgumentException("Bitte alle Pflichtfelder korrekt ausfüllen.");
        }
    }

    private void loescheBuchung() {
        if ("ausgabe".equals(typ)) {
            ausgabeService.loescheAusgabe(id);
            Notification.show("Ausgabe gelöscht");
            UI.getCurrent().navigate(BuchungListView.class);

        } else if ("einnahme".equals(typ)) {
            zahlungsEingangService.loescheZahlungseingang(id);
            Notification.show("Zahlungseingang gelöscht");
            UI.getCurrent().navigate(BuchungListView.class);
        }
    }

    private void updateHero() {
        if (heroBadge == null) {
            return;
        }

        boolean istAusgabe = "ausgabe".equals(typ);
        String typeText = istAusgabe ? "Ausgabe" : "Einnahme";

        heroBadge.setText(typeText);
        heroBadge.setClassName("buchung-type-badge");
        heroBadge.addClassName(istAusgabe ? "expense" : "income");

        heroAmount.setText(formatMoney(betragField.getValue()));
        heroAmount.setClassName("buchung-detail-amount");
        heroAmount.addClassName(istAusgabe ? "expense" : "income");

        heroStatus.setText(statusField.getValue() == null ? "-" : statusField.getValue());
        heroDate.setText(formatDate(datumField.getValue()));

        if (istAusgabe) {
            heroCategory.setText(
                    ausgabeKategorieField.getValue() == null
                            ? "-"
                            : ausgabeKategorieField.getValue().getLabel()
            );
        } else {
            heroCategory.setText(
                    zahlungseingangTypField.getValue() == null
                            ? "-"
                            : zahlungseingangTypField.getValue().getLabel()
            );
        }
    }

    private Div createCard(String title, String subtitle) {
        Div card = new Div();
        card.addClassName("form-card");

        H3 heading = new H3(title);
        heading.addClassName("form-card-title");

        Paragraph description = new Paragraph(subtitle);
        description.addClassName("form-card-subtitle");

        card.add(heading, description);

        return card;
    }

    private FormLayout createTwoColumnFormLayout() {
        FormLayout form = new FormLayout();
        form.addClassName("buchung-form-grid");

        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("640px", 2)
        );

        return form;
    }

    private String formatMoney(BigDecimal value) {
        if (value == null) {
            value = BigDecimal.ZERO;
        }

        return NumberFormat
                .getCurrencyInstance(Locale.GERMANY)
                .format(value);
    }

    private String formatDate(LocalDate value) {
        return value == null ? "-" : value.format(DATE_FORMATTER);
    }

    @Override
    public String getPageTitle() {
        return "Buchung bearbeiten";
    }

    @Override
    public String getPageSubtitle() {
        return "Finanzen › Buchung bearbeiten";
    }
}
