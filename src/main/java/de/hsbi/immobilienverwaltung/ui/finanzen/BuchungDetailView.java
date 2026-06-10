package de.hsbi.immobilienverwaltung.ui.finanzen;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
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
//import de.hsbi.immobilienverwaltung.security.LoginRequired;//
import jakarta.annotation.security.PermitAll;
import de.hsbi.immobilienverwaltung.service.interfaces.AusgabeService;
import de.hsbi.immobilienverwaltung.service.interfaces.ZahlungsEingangService;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;

@Route(value = "finanzen/buchungen/:typ/:id", layout = MainLayout.class)
@PermitAll
public class BuchungDetailView extends VerticalLayout implements HasPageHeader, BeforeEnterObserver {

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

    private H3 title;

    public BuchungDetailView(AusgabeService ausgabeService,
                             ZahlungsEingangService zahlungsEingangService) {
        this.ausgabeService = ausgabeService;
        this.zahlungsEingangService = zahlungsEingangService;

        addClassName("buchung-form-view");
        setWidthFull();
        setMinHeight("100%");
        setPadding(false);
        setSpacing(false);

        createFields();

        Component content = createContent();
        add(content);
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
        betragField = new BigDecimalField("Betrag (€)");
        betragField.setPrefixComponent(new Span("€"));
        betragField.setWidthFull();

        datumField = new DatePicker("Datum");
        datumField.setWidthFull();

        zweitesDatumField = new DatePicker("Fälligkeitsdatum / Leistungsmonat");
        zweitesDatumField.setWidthFull();

        ausgabeKategorieField = new ComboBox<>("Kategorie");
        ausgabeKategorieField.setItems(Ausgabenkategorie.values());
        ausgabeKategorieField.setItemLabelGenerator(this::formatiereAusgabenkategorie);
        ausgabeKategorieField.setWidthFull();

        zahlungseingangTypField = new ComboBox<>("Zahlungstyp");
        zahlungseingangTypField.setItems(Zahlungseingangtyp.values());
        zahlungseingangTypField.setWidthFull();

        statusField = new ComboBox<>("Status");
        statusField.setItems("Bezahlt / Erledigt", "Offen / Ausstehend");
        statusField.setWidthFull();

        beschreibungField = new TextArea("Beschreibung / Notiz");
        beschreibungField.setWidthFull();
        beschreibungField.setHeight("140px");
    }

        private Component createContent() {
            VerticalLayout content = new VerticalLayout();
            content.addClassName("page-content");
            content.setPadding(false);
            content.setSpacing(true);
            content.setWidthFull();
            content.setMinHeight("100%");
            content.getStyle().set("overflow-y", "auto");

            content.add(
                    createHeaderCard(),
                    createFormCard()
            );

            return content;
        }

    private Component createHeaderCard() {
        Div card = new Div();
        card.addClassNames("form-card", "action-card");

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Button backButton = new Button(VaadinIcon.ARROW_LEFT.create());
        backButton.addClickListener(event ->
                UI.getCurrent().navigate(BuchungListView.class)
        );

        title = new H3("Buchung bearbeiten");

        Button saveButton = new Button("Speichern", VaadinIcon.CHECK.create());
        saveButton.addClassName("save-booking-button");
        saveButton.addClickListener(event -> speichereBuchung());

        Button deleteButton = new Button("Löschen", VaadinIcon.TRASH.create());
        deleteButton.addClassName("delete-booking-button");
        deleteButton.addClickListener(event -> loescheBuchung());

        HorizontalLayout actions = new HorizontalLayout(saveButton, deleteButton);
        actions.setSpacing(true);

        header.add(backButton, title, actions);
        header.setFlexGrow(1, title);

        card.add(header);
        return card;
    }

    private Component createFormCard() {
        Div card = createCard("Buchungsdaten");
        card.getStyle().set("overflow", "visible");
        VerticalLayout formLayout = new VerticalLayout();
        formLayout.setPadding(false);
        formLayout.setSpacing(true);
        formLayout.setWidthFull();

        HorizontalLayout row1 = new HorizontalLayout();
        row1.setWidthFull();
        row1.addClassName("form-row");
        row1.add(betragField, datumField);

        HorizontalLayout row2 = new HorizontalLayout();
        row2.setWidthFull();
        row2.addClassName("form-row");
        row2.add(ausgabeKategorieField, zahlungseingangTypField, statusField);

        HorizontalLayout row3 = new HorizontalLayout();
        row3.setWidthFull();
        row3.addClassName("form-row");
        row3.add(zweitesDatumField);

        beschreibungField.setWidthFull();
        beschreibungField.setHeight("120px");

        formLayout.add(row1, row2, row3, beschreibungField);

        card.add(formLayout);

        return card;
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

        betragField.setValue(aktuelleAusgabe.getBetrag());
        datumField.setValue(aktuelleAusgabe.getDatum());
        zweitesDatumField.setValue(aktuelleAusgabe.getFaelligkeitsdatum());
        ausgabeKategorieField.setValue(aktuelleAusgabe.getKategorie());
        statusField.setValue(aktuelleAusgabe.getStatus());
        beschreibungField.setValue(aktuelleAusgabe.getBeschreibung() != null ? aktuelleAusgabe.getBeschreibung() : "");

        ausgabeKategorieField.setVisible(true);
        zahlungseingangTypField.setVisible(false);
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

        betragField.setValue(aktuellerZahlungseingang.getBetrag());
        datumField.setValue(aktuellerZahlungseingang.getZahlungsdatum());
        zweitesDatumField.setValue(aktuellerZahlungseingang.getLeistungsmonat());
        zahlungseingangTypField.setValue(aktuellerZahlungseingang.getTyp());
        statusField.setValue(aktuellerZahlungseingang.getStatus());
        beschreibungField.setValue(aktuellerZahlungseingang.getBeschreibung() != null ? aktuellerZahlungseingang.getBeschreibung() : "");

        ausgabeKategorieField.setVisible(false);
        zahlungseingangTypField.setVisible(true);
    }

    private void speichereBuchung() {
        try {
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

            } else if ("einnahme".equals(typ)) {
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
            Notification.show(e.getMessage());
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

    private String formatiereAusgabenkategorie(Ausgabenkategorie kategorie) {
        if (kategorie == null) {
            return "";
        }

        return switch (kategorie) {
            case INSTANDHALTUNG -> "Instandhaltung";
            case VERWALTUNG -> "Verwaltungskosten";
            case REPARATUR -> "Reparatur / Handwerker";
            case VERSICHERUNG -> "Versicherung";
            case SONSTIGES -> "Sonstiges";
            default -> kategorie.name();
        };
    }

    private Div createCard(String title) {
        Div card = new Div();
        card.addClassName("form-card");

        H3 heading = new H3(title);
        heading.addClassName("form-card-title");

        Div divider = new Div();
        divider.addClassName("form-card-divider");

        card.add(heading, divider);

        return card;
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
