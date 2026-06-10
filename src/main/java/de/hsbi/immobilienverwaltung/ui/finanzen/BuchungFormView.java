package de.hsbi.immobilienverwaltung.ui.finanzen;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Route;
import de.hsbi.immobilienverwaltung.domain.Ausgabe;
import de.hsbi.immobilienverwaltung.domain.Immobilie;
import de.hsbi.immobilienverwaltung.domain.enums.Ausgabenkategorie;
import de.hsbi.immobilienverwaltung.service.interfaces.AusgabeService;
import de.hsbi.immobilienverwaltung.service.interfaces.ImmobilieService;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;
import de.hsbi.immobilienverwaltung.domain.Mieteinheit;
import de.hsbi.immobilienverwaltung.service.interfaces.MieteinheitService;
import de.hsbi.immobilienverwaltung.domain.Mietvertrag;
import de.hsbi.immobilienverwaltung.domain.Zahlungseingang;
import de.hsbi.immobilienverwaltung.domain.enums.Zahlungseingangtyp;
import de.hsbi.immobilienverwaltung.service.interfaces.MietvertragService;
import de.hsbi.immobilienverwaltung.service.interfaces.ZahlungsEingangService;
import jakarta.annotation.security.PermitAll;

@Route(value = "finanzen/buchung-neu", layout = MainLayout.class)
@PermitAll
public class BuchungFormView extends VerticalLayout implements HasPageHeader {

    private final AusgabeService ausgabeService;
    private final ImmobilieService immobilieService;
    private final MieteinheitService mieteinheitService;
    private final ZahlungsEingangService zahlungsEingangService;
    private final MietvertragService mietvertragService;

    private RadioButtonGroup<String> buchungstypGroup;
    private RadioButtonGroup<String> statusGroup;

    private BigDecimalField betragField;
    private DatePicker buchungsdatumField;
    private DatePicker faelligkeitsdatumField;

    private ComboBox<Enum<?>> kategorieField;
    private TextArea beschreibungField;

    private ComboBox<Immobilie> immobilieField;
    private ComboBox<Mieteinheit> mieteinheitField;
    private ComboBox<Mietvertrag> mieterVertragField;

    public BuchungFormView(AusgabeService ausgabeService,
                           ImmobilieService immobilieService,
                           MieteinheitService mieteinheitService,
                           ZahlungsEingangService zahlungsEingangService,
                           MietvertragService mietvertragService) {
        this.ausgabeService = ausgabeService;
        this.immobilieService = immobilieService;
        this.mieteinheitService = mieteinheitService;
        this.zahlungsEingangService = zahlungsEingangService;
        this.mietvertragService = mietvertragService;

        addClassName("buchung-form-view");
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        createFields();

        add(createContent());
    }

    private void createFields() {
        buchungstypGroup = new RadioButtonGroup<>();
        buchungstypGroup.setItems("Einnahme", "Ausgabe");
        buchungstypGroup.setValue("Einnahme");

        statusGroup = new RadioButtonGroup<>();
        statusGroup.setItems("Bezahlt / Erledigt", "Offen / Ausstehend");
        statusGroup.setValue("Bezahlt / Erledigt");

        betragField = new BigDecimalField("Betrag (€)");
        betragField.setPlaceholder("0,00");
        betragField.setPrefixComponent(new Span("€"));
        betragField.setWidthFull();
        betragField.setRequiredIndicatorVisible(true);

        buchungsdatumField = new DatePicker("Buchungsdatum");
        buchungsdatumField.setPlaceholder("dd/mm/yyyy");
        buchungsdatumField.setWidthFull();
        buchungsdatumField.setRequiredIndicatorVisible(true);

        faelligkeitsdatumField = new DatePicker("Fälligkeitsdatum (optional)");
        faelligkeitsdatumField.setPlaceholder("dd/mm/yyyy");
        faelligkeitsdatumField.setWidthFull();

        kategorieField = new ComboBox<>("Kategorie");
        kategorieField.setItemLabelGenerator(kategorie -> {
            if (kategorie instanceof Ausgabenkategorie ausgabenkategorie) {
                return ausgabenkategorie.getLabel();
            }
            if (kategorie instanceof Zahlungseingangtyp zahlungseingangtyp) {
                return zahlungseingangtyp.getLabel();
            }
            return "";
        });
        kategorieField.setAllowCustomValue(false);
        kategorieField.setWidthFull();
        kategorieField.setRequiredIndicatorVisible(true);
        aktualisiereKategorieField();

        beschreibungField = new TextArea("Beschreibung / Notiz");
        beschreibungField.setPlaceholder("Details zur Buchung eingeben...");
        beschreibungField.setWidthFull();
        beschreibungField.setHeight("120px");

        immobilieField = new ComboBox<>("Immobilie");
        immobilieField.setItems(immobilieService.findeAlleImmobilien());
        immobilieField.setItemLabelGenerator(Immobilie::getBezeichnung);
        immobilieField.setAllowCustomValue(false);
        immobilieField.setWidthFull();
        immobilieField.setRequiredIndicatorVisible(false);

        mieteinheitField = new ComboBox<>("Mieteinheit");
        mieteinheitField.setItemLabelGenerator(Mieteinheit::getBezeichnung);
        mieteinheitField.setAllowCustomValue(false);
        mieteinheitField.setWidthFull();
        immobilieField.addValueChangeListener(event -> {
            Immobilie selectedImmobilie = event.getValue();

            mieteinheitField.clear();

            if (selectedImmobilie != null) {
                mieteinheitField.setItems(
                        mieteinheitService.findeMieteinheitenNachImmobilie(selectedImmobilie.getId())
                );
            }
        });

        mieterVertragField = new ComboBox<>("Mieter / Vertrag");
        mieterVertragField.setItems(mietvertragService.findeAlleMietvertraege());
        mieterVertragField.setItemLabelGenerator(this::formatiereMietvertrag);
        mieterVertragField.setAllowCustomValue(false);
        mieterVertragField.setWidthFull();
    }

    private Component createContent() {
        HorizontalLayout content = new HorizontalLayout();
        content.addClassName("buchung-content");
        content.setWidthFull();
        content.setPadding(false);
        content.setSpacing(false);
        content.setAlignItems(FlexComponent.Alignment.START);

        VerticalLayout leftColumn = new VerticalLayout();
        leftColumn.addClassName("buchung-left-column");
        leftColumn.setPadding(false);
        leftColumn.setSpacing(true);
        leftColumn.setWidthFull();

        VerticalLayout rightColumn = new VerticalLayout();
        rightColumn.addClassName("buchung-right-column");
        rightColumn.setPadding(false);
        rightColumn.setSpacing(true);
        rightColumn.setWidth("380px");

        leftColumn.add(
                createBuchungstypSection(),
                createKerndatenSection()
        );

        rightColumn.add(
                createStatusSection(),
                createZuordnungSection(),
                createActionSection()
        );

        content.add(leftColumn, rightColumn);

        content.setFlexGrow(1, leftColumn);
        content.setFlexGrow(0, rightColumn);

        return content;
    }
    private Component createBuchungstypSection() {
        Div card = createCard("Buchungstyp");

        HorizontalLayout options = new HorizontalLayout();
        options.addClassName("booking-type-options");
        options.setWidthFull();

        Div incomeCard = createBookingTypeCard(
                "Einnahme / Zahlungseingang",
                "Miete, Nebenkosten, Kaution, sonstige Einnahmen",
                VaadinIcon.ARROW_DOWN,
                "income",
                true
        );

        Div expenseCard = createBookingTypeCard(
                "Ausgabe / Zahlungsausgang",
                "Instandhaltung, Handwerker, Verwaltungskosten",
                VaadinIcon.ARROW_UP,
                "expense",
                false
        );

        incomeCard.addClickListener(event -> {
            buchungstypGroup.setValue("Einnahme");
            incomeCard.addClassName("selected");
            expenseCard.removeClassName("selected");

            mieterVertragField.setEnabled(true);
            aktualisiereKategorieField();
        });

        expenseCard.addClickListener(event -> {
            buchungstypGroup.setValue("Ausgabe");
            expenseCard.addClassName("selected");
            incomeCard.removeClassName("selected");

            mieterVertragField.clear();
            mieterVertragField.setEnabled(false);
            aktualisiereKategorieField();
        });

        options.add(incomeCard, expenseCard);
        card.add(options);

        return card;
    }

    private Div createBookingTypeCard(
            String title,
            String subtitle,
            VaadinIcon icon,
            String colorClass,
            boolean selected
    ) {
        Div option = new Div();
        option.addClassNames("booking-type-card", colorClass);

        if (selected) {
            option.addClassName("selected");
        }

        HorizontalLayout inner = new HorizontalLayout();
        inner.setWidthFull();
        inner.setAlignItems(FlexComponent.Alignment.CENTER);
        inner.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        HorizontalLayout left = new HorizontalLayout();
        left.setAlignItems(FlexComponent.Alignment.START);

        Span radio = new Span();
        radio.addClassName("fake-radio");

        Div text = new Div();

        Span titleSpan = new Span(title);
        titleSpan.addClassName("booking-type-title");

        Span subtitleSpan = new Span(subtitle);
        subtitleSpan.addClassName("booking-type-subtitle");

        text.add(titleSpan, subtitleSpan);
        left.add(radio, text);

        Icon typeIcon = icon.create();
        typeIcon.addClassNames("booking-type-icon", colorClass);

        inner.add(left, typeIcon);
        option.add(inner);

        return option;
    }

    private Component createKerndatenSection() {
        Div card = createCard("Kerndaten");

        HorizontalLayout row1 = new HorizontalLayout();
        row1.setWidthFull();
        row1.addClassName("form-row");
        row1.add(betragField, buchungsdatumField);

        HorizontalLayout row2 = new HorizontalLayout();
        row2.setWidthFull();
        row2.addClassName("form-row");
        row2.add(kategorieField, faelligkeitsdatumField);

        card.add(row1, row2, beschreibungField);

        return card;
    }

    private Component createStatusSection() {
        Div card = createCard("Status");

        Div paid = createStatusOption("Bezahlt / Erledigt", "green", true);
        Div open = createStatusOption("Offen / Ausstehend", "orange", false);

        paid.addClickListener(event -> {
            statusGroup.setValue("Bezahlt / Erledigt");
            paid.addClassName("selected");
            open.removeClassName("selected");
        });

        open.addClickListener(event -> {
            statusGroup.setValue("Offen / Ausstehend");
            open.addClassName("selected");
            paid.removeClassName("selected");
        });

        card.add(paid, open);
        return card;
    }

    private Div createStatusOption(String text, String color, boolean selected) {
        Div option = new Div();
        option.addClassName("status-option");

        if (selected) {
            option.addClassName("selected");
        }

        HorizontalLayout layout = new HorizontalLayout();
        layout.setAlignItems(FlexComponent.Alignment.CENTER);

        Span radio = new Span();
        radio.addClassName("fake-radio");

        Span dot = new Span();
        dot.addClassNames("status-dot", color);

        Span label = new Span(text);
        label.addClassName("status-label");

        layout.add(radio, dot, label);
        option.add(layout);

        return option;
    }

    private Component createZuordnungSection() {
        Div card = createCard("Zuordnung");

        card.add(
                immobilieField,
                mieteinheitField,
                mieterVertragField
        );

        return card;
    }

    private Component createActionSection() {
        Div card = new Div();
        card.addClassNames("form-card", "action-card");

        Button saveButton = new Button("Buchung speichern", VaadinIcon.CHECK.create());
        saveButton.addClassName("save-booking-button");
        saveButton.setWidthFull();

        saveButton.addClickListener(event -> {
            try {
                if ("Ausgabe".equals(buchungstypGroup.getValue())) {
                    Ausgabe ausgabe = new Ausgabe();

                    Enum<?> ausgewaehlteKategorie = kategorieField.getValue();
                    if (!(ausgewaehlteKategorie instanceof Ausgabenkategorie ausgabenkategorie)) {
                        throw new IllegalArgumentException("Bitte eine gültige Ausgabenkategorie auswählen.");
                    }
                    ausgabe.setKategorie(ausgabenkategorie);
                    ausgabe.setKategorie(ausgabenkategorie);
                    ausgabe.setBetrag(betragField.getValue());
                    ausgabe.setDatum(buchungsdatumField.getValue());
                    ausgabe.setFaelligkeitsdatum(faelligkeitsdatumField.getValue());
                    ausgabe.setBeschreibung(beschreibungField.getValue());
                    ausgabe.setImmobilie(immobilieField.getValue());
                    ausgabe.setStatus(statusGroup.getValue());

                    ausgabeService.speichereAusgabe(ausgabe);

                    Notification.show("Ausgabe gespeichert");
                    UI.getCurrent().navigate(BuchungListView.class);
                } else if ("Einnahme".equals(buchungstypGroup.getValue())) {
                    Zahlungseingang zahlungseingang = new Zahlungseingang();

                    Enum<?> ausgewaehlteKategorie = kategorieField.getValue();
                    if (!(ausgewaehlteKategorie instanceof Zahlungseingangtyp zahlungseingangtyp)) {
                        throw new IllegalArgumentException("Bitte eine gültige Einnahmenkategorie auswählen.");
                    }
                    zahlungseingang.setTyp(zahlungseingangtyp);
                    zahlungseingang.setBetrag(betragField.getValue());
                    zahlungseingang.setZahlungsdatum(buchungsdatumField.getValue());
                    zahlungseingang.setLeistungsmonat(faelligkeitsdatumField.getValue());
                    zahlungseingang.setBeschreibung(beschreibungField.getValue());
                    zahlungseingang.setMietvertrag(mieterVertragField.getValue());
                    zahlungseingang.setStatus(statusGroup.getValue());

                    zahlungsEingangService.speichereZahlungseingang(zahlungseingang);

                    Notification.show("Zahlungseingang gespeichert");
                    UI.getCurrent().navigate(BuchungListView.class);
                }
            } catch (IllegalArgumentException e) {
                Notification.show(e.getMessage());
            }
        });

        Button cancelButton = new Button("Abbrechen");
        cancelButton.addClassName("cancel-booking-button");
        cancelButton.setWidthFull();

        cancelButton.addClickListener(event ->
                UI.getCurrent().navigate(FinanzDashboardView.class)
        );

        Div divider = new Div();
        divider.addClassName("action-divider");

        Button deleteButton = new Button("Buchung löschen", VaadinIcon.TRASH.create());
        deleteButton.addClassName("delete-booking-button");
        deleteButton.setWidthFull();

        deleteButton.addClickListener(event ->
                Notification.show("Löschen ist im Frontend-Prototyp noch nicht verbunden")
        );

        Span warning = new Span("Diese Aktion kann nicht rückgängig gemacht werden.");
        warning.addClassName("delete-warning");

        card.add(saveButton, cancelButton, divider, deleteButton, warning);

        return card;
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
            //default just to make it flexible if it changes
        };
    }
    // Formatiert einen Mietvertrag für die Anzeige in der ComboBox.
// Ohne diese Methode würde Vaadin nur die technische Objektadresse anzeigen.
// Beispiel: "Max Mustermann - Wohnung 1A"
    private String formatiereMietvertrag(Mietvertrag mietvertrag) {
        if (mietvertrag == null) {
            return "";
        }

        String mieterName = mietvertrag.getMieter() != null
                ? mietvertrag.getMieter().getVorname() + " " + mietvertrag.getMieter().getNachname()
                : "Unbekannter Mieter";

        String einheit = mietvertrag.getMieteinheit() != null
                ? mietvertrag.getMieteinheit().getBezeichnung()
                : "Unbekannte Mieteinheit";

        return mieterName + " - " + einheit;
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

    private void aktualisiereKategorieField() {
        kategorieField.clear();
        if ("Ausgabe".equals(buchungstypGroup.getValue())) {
            kategorieField.setItems(Ausgabenkategorie.values());
            kategorieField.setValue(Ausgabenkategorie.SONSTIGES);
        } else {
            kategorieField.setItems(Zahlungseingangtyp.values());
            kategorieField.setValue(Zahlungseingangtyp.SONSTIGES);
        }
    }

    @Override
    public String getPageTitle() {
        return "Buchung anlegen";
    }

    @Override
    public String getPageSubtitle() {
        return "Finanzen › Übersicht › Neue Buchung";
    }
}