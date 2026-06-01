package de.hsbi.immobilienverwaltung.ui.immobilien;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
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

@Route(value = "immobilien/:immobilieId/einheiten/neu", layout = MainLayout.class)
public class MieteinheitFormView extends Div implements HasPageHeader, BeforeEnterObserver, LoginRequired {

    private Long immobilieId;
    private final MieteinheitService mieteinheitService;

    private final TextField nummerField = new TextField("Bezeichnung");
    private final IntegerField groesseField = new IntegerField("Größe in m²");
    private final TextField stockwerkField = new TextField("Stockwerk");
    private final IntegerField zimmeranzahlField = new IntegerField("Zimmeranzahl");
    private final Select<MieteinheitTyp> typSelect = new Select<>();
    private final Select<Mieteinheitstatus> statusSelect = new Select<>();

    public MieteinheitFormView(MieteinheitService mieteinheitService) {
        this.mieteinheitService = mieteinheitService;

        addClassName("page-content");
        addClassName("mieteinheit-form-view");

        add(createFormCard());
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        this.immobilieId = event.getRouteParameters()
                .get("immobilieId")
                .map(Long::valueOf)
                .orElseThrow(() -> new IllegalArgumentException("Immobilie-ID fehlt."));
    }

    private Div createFormCard() {
        Div card = new Div();
        card.addClassName("form-card");

        Div header = new Div();
        header.addClassName("form-card-header");

        H3 title = new H3("Mieteinheit hinzufügen");
        title.addClassName("form-card-title");

        header.add(title);

        FormLayout form = new FormLayout();
        form.addClassName("form-card-content");

        nummerField.setPlaceholder("z. B. WE-01, Büro EG, Gesamtobjekt");

        typSelect.setLabel("Typ");
        typSelect.setItems(MieteinheitTyp.values());
        typSelect.setItemLabelGenerator(this::formatTyp);

        statusSelect.setLabel("Status");
        statusSelect.setItems(Mieteinheitstatus.values());
        statusSelect.setValue(Mieteinheitstatus.FREI);
        statusSelect.setItemLabelGenerator(this::formatStatus);

        groesseField.setPlaceholder("z. B. 85");
        stockwerkField.setPlaceholder("z. B. EG, 1. OG");
        zimmeranzahlField.setPlaceholder("z. B. 3");

        nummerField.setRequiredIndicatorVisible(true);
        typSelect.setRequiredIndicatorVisible(true);
        statusSelect.setRequiredIndicatorVisible(true);

        groesseField.setMin(0);
        zimmeranzahlField.setMin(0);

        form.add(
                nummerField,
                typSelect,
                groesseField,
                stockwerkField,
                zimmeranzahlField,
                statusSelect
        );

        Div actions = new Div();
        actions.addClassName("form-actions");

        Button abbrechenButton = new Button("Abbrechen");
        abbrechenButton.addClassName("secondary-button");
        abbrechenButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate("immobilien/" + immobilieId))
        );

        Button speichernButton = new Button("Speichern", VaadinIcon.CHECK.create());
        speichernButton.addClassName("primary-button");
        speichernButton.addClickListener(event -> speichereMieteinheit());

        actions.add(abbrechenButton, speichernButton);

        card.add(header, form, actions);

        return card;
    }

    private void speichereMieteinheit() {
        try {
            Mieteinheit mieteinheit = new Mieteinheit(
                    nummerField.getValue(),
                    statusSelect.getValue(),
                    typSelect.getValue(),
                    groesseField.getValue(),
                    zimmeranzahlField.getValue(),
                    stockwerkField.getValue()
            );

            mieteinheitService.speichereMieteinheit(immobilieId, mieteinheit);

            Notification.show("Mieteinheit wurde gespeichert: " + nummerField.getValue());

            getUI().ifPresent(ui -> ui.navigate("immobilien/" + immobilieId));

        } catch (Exception ex) {
            Notification.show("Fehler beim Speichern: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
        }
    }

    private String formatStatus(Mieteinheitstatus status) {
        return switch (status) {
            case FREI -> "Frei";
            case VERMIETET -> "Vermietet";
            case IN_RENOVIERUNG -> "In Renovierung";
        };
    }

    private String formatTyp(MieteinheitTyp typ) {
        return switch (typ) {
            case WOHNUNG -> "Wohnung";
            case BUERO -> "Büro";
            case LAGERHALLE -> "Lagerhalle";
            case GEWERBEFLAECHE -> "Gewerbefläche";
            case GESAMTOBJEKT -> "Gesamtobjekt";
        };
    }

    @Override
    public String getPageTitle() {
        return "Mieteinheit hinzufügen";
    }

    @Override
    public String getPageSubtitle() {
        return "Immobilien > Detailansicht > Mieteinheit hinzufügen";
    }
}