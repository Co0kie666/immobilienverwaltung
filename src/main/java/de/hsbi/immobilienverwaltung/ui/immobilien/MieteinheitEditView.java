package de.hsbi.immobilienverwaltung.ui.immobilien;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import de.hsbi.immobilienverwaltung.security.LoginRequired;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;
import de.hsbi.immobilienverwaltung.domain.Mieteinheit;
import de.hsbi.immobilienverwaltung.domain.enums.MieteinheitTyp;
import de.hsbi.immobilienverwaltung.domain.enums.Mieteinheitstatus;
import de.hsbi.immobilienverwaltung.service.interfaces.MieteinheitService;

@Route(value = "immobilien/:immobilieId/einheiten/:mieteinheitId/bearbeiten", layout = MainLayout.class)
public class MieteinheitEditView extends Div implements HasPageHeader, BeforeEnterObserver, LoginRequired {

    private final MieteinheitService mieteinheitService;
    private Mieteinheit mieteinheit;
    private Long immobilieId;
    private Long mieteinheitId;

    private final TextField nummerField = new TextField("Einheit-Nr.");
    private final Select<MieteinheitTyp> typSelect = new Select<>();
    private final NumberField groesseField = new NumberField("Größe in m²");
    private final TextField stockwerkField = new TextField("Stockwerk");
    private final NumberField zimmeranzahlField = new NumberField("Zimmeranzahl");
    private final Select<Mieteinheitstatus> statusSelect = new Select<>();

    public MieteinheitEditView(MieteinheitService mieteinheitService) {
        this.mieteinheitService = mieteinheitService;
        addClassName("page-content");
        addClassName("mieteinheit-edit-view");

        add(createFormCard());
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        this.immobilieId = event.getRouteParameters()
                .get("immobilieId")
                .map(Long::valueOf)
                .orElse(null);

        this.mieteinheitId = event.getRouteParameters()
                .get("mieteinheitId")
                .map(Long::valueOf)
                .orElse(null);

        this.mieteinheit = mieteinheitService.findeMieteinheitNachId(mieteinheitId)
                .orElseThrow(() -> new IllegalArgumentException("Mieteinheit wurde nicht gefunden."));

        fuelleFelder();
    }

    private void fuelleFelder() {
        nummerField.setValue(mieteinheit.getBezeichnung());
        typSelect.setValue(mieteinheit.getTyp());
        groesseField.setValue(mieteinheit.getGroesse() != null ? mieteinheit.getGroesse().doubleValue() : null);
        stockwerkField.setValue(mieteinheit.getStockwerk());
        zimmeranzahlField.setValue(mieteinheit.getZimmerzahl() != null ? mieteinheit.getZimmerzahl().doubleValue() : null);
        statusSelect.setValue(mieteinheit.getStatus());
    }


    private Div createFormCard() {
        Div card = new Div();
        card.addClassName("form-card");

        Div header = new Div();
        header.addClassName("form-card-header");

        H3 title = new H3("Mieteinheit bearbeiten");
        title.addClassName("form-card-title");

        header.add(title);

        FormLayout form = new FormLayout();
        form.addClassName("form-card-content");

        nummerField.setPlaceholder("z. B. WE-01, Büro EG, Gesamtobjekt");

        typSelect.setLabel("Typ");
        typSelect.setItems(MieteinheitTyp.values());
        typSelect.setItemLabelGenerator(this::formatTyp);

        groesseField.setPlaceholder("z. B. 85");

        stockwerkField.setPlaceholder("z. B. EG, 1. OG");

        zimmeranzahlField.setPlaceholder("z. B. 3");

        statusSelect.setLabel("Status");
        statusSelect.setItems(Mieteinheitstatus.values());
        statusSelect.setItemLabelGenerator(this::formatStatus);

        form.add(nummerField, typSelect, groesseField, stockwerkField, zimmeranzahlField, statusSelect);

        Div actions = new Div();
        actions.addClassName("form-actions");

        Button abbrechenButton = new Button("Abbrechen");
        abbrechenButton.addClassName("secondary-button");
        abbrechenButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate(
                        "immobilien/" + immobilieId + "/einheiten/" + mieteinheitId + "/details"
                ))
        );

        Button speichernButton = new Button("Änderungen speichern", VaadinIcon.CHECK.create());
        speichernButton.addClassName("primary-button");
        speichernButton.addClickListener(event -> speichereAenderungen());



        actions.add(abbrechenButton, speichernButton);

        card.add(header, form, actions);

        return card;
    }

    private void speichereAenderungen() {
        try {
            mieteinheit.setBezeichnung(nummerField.getValue());
            mieteinheit.setTyp(typSelect.getValue());
            mieteinheit.setGroesse(toInteger(groesseField.getValue()));
            mieteinheit.setStockwerk(stockwerkField.getValue());
            mieteinheit.setZimmerzahl(toInteger(zimmeranzahlField.getValue()));
            mieteinheit.setStatus(statusSelect.getValue());

            mieteinheitService.speichereMieteinheit(immobilieId, mieteinheit);

            Notification.show("Mieteinheit wurde aktualisiert");

            getUI().ifPresent(ui -> ui.navigate(
                    "immobilien/" + immobilieId + "/einheiten/" + mieteinheitId + "/details"
            ));

        } catch (Exception ex) {
            Notification.show("Fehler beim Speichern: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
        }
    }

    private Integer toInteger(Double value) {
        return value == null ? null : value.intValue();
    }

    private String formatTyp(MieteinheitTyp typ) {
        if (typ == null) return null;

        return switch (typ) {
            case WOHNUNG -> "Wohnung";
            case BUERO -> "Büro";
            case LAGERHALLE -> "Lagerhalle";
            case GEWERBEFLAECHE -> "Gewerbefläche";
            case GESAMTOBJEKT -> "Gesamtobjekt";
        };
    }

    private String formatStatus(Mieteinheitstatus status) {
        if (status == null) return null;

        return switch (status) {
            case FREI -> "Frei";
            case VERMIETET -> "Vermietet";
            case IN_RENOVIERUNG -> "In Renovierung";
        };
    }

    @Override
    public String getPageTitle() {
        return "Mieteinheit bearbeiten";
    }

    @Override
    public String getPageSubtitle() {
        return "Immobilien > Mieteinheit bearbeiten";
    }

}