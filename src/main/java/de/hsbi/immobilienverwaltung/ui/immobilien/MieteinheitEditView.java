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

@Route(value = "immobilien/:immobilieId/einheiten/:mieteinheitId/bearbeiten", layout = MainLayout.class)
public class MieteinheitEditView extends Div implements HasPageHeader, BeforeEnterObserver, LoginRequired {

    private Long immobilieId;
    private Long mieteinheitId;

    private final TextField nummerField = new TextField("Einheit-Nr.");
    private final Select<String> typSelect = new Select<>();
    private final NumberField groesseField = new NumberField("Größe in m²");
    private final TextField stockwerkField = new TextField("Stockwerk");
    private final NumberField zimmeranzahlField = new NumberField("Zimmeranzahl");
    private final Select<String> statusSelect = new Select<>();

    public MieteinheitEditView() {
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

        ladeDummyDaten();
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
        typSelect.setItems("Wohnung", "Büro", "Lagerhalle", "Gewerbefläche", "Gesamtobjekt");

        groesseField.setPlaceholder("z. B. 85");

        stockwerkField.setPlaceholder("z. B. EG, 1. OG");

        zimmeranzahlField.setPlaceholder("z. B. 3");

        statusSelect.setLabel("Status");
        statusSelect.setItems("Frei", "Vermietet", "In Renovierung");

        form.add(nummerField, typSelect, groesseField, stockwerkField, zimmeranzahlField, statusSelect);

        Div actions = new Div();
        actions.addClassName("form-actions");

        Button abbrechenButton = new Button("Abbrechen");
        abbrechenButton.addClassName("secondary-button");
        abbrechenButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate("immobilien/" + immobilieId))
        );

        Button speichernButton = new Button("Änderungen speichern", VaadinIcon.CHECK.create());
        speichernButton.addClassName("primary-button");
        speichernButton.addClickListener(event -> {
            Notification.show("Änderungen wurden gespeichert");
            getUI().ifPresent(ui -> ui.navigate("immobilien/" + immobilieId));
        });

        actions.add(abbrechenButton, speichernButton);

        card.add(header, form, actions);

        return card;
    }

    private void ladeDummyDaten() {
        if (mieteinheitId == null) {
            return;
        }

        if (mieteinheitId == 1L) {
            nummerField.setValue("WE-01");
            typSelect.setValue("Wohnung");
            groesseField.setValue(85.0);
            stockwerkField.setValue("EG");
            zimmeranzahlField.setValue(3.0);
            statusSelect.setValue("Vermietet");
        } else if (mieteinheitId == 2L) {
            nummerField.setValue("WE-02");
            typSelect.setValue("Wohnung");
            groesseField.setValue(92.0);
            stockwerkField.setValue("1. OG");
            zimmeranzahlField.setValue(4.0);
            statusSelect.setValue("Vermietet");
        } else {
            nummerField.setValue("WE-03");
            typSelect.setValue("Wohnung");
            groesseField.setValue(75.0);
            stockwerkField.setValue("2. OG");
            zimmeranzahlField.setValue(3.0);
            statusSelect.setValue("Frei");
        }
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