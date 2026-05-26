package de.hsbi.immobilienverwaltung.ui.immobilien;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import de.hsbi.immobilienverwaltung.security.LoginRequired;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;

@Route(value = "immobilien/:immobilieId/bearbeiten", layout = MainLayout.class)
public class ImmobilieEditView extends Div implements HasPageHeader, BeforeEnterObserver, LoginRequired {

    private Long immobilieId;

    private final TextField bezeichnungField = new TextField("Bezeichnung");
    private final Select<String> typSelect = new Select<>();
    private final IntegerField baujahrField = new IntegerField("Baujahr");
    private final NumberField gesamtflaecheField = new NumberField("Gesamtfläche in m²");

    private final TextField strasseField = new TextField("Straße");
    private final TextField hausnummerField = new TextField("Hausnummer");
    private final TextField plzField = new TextField("PLZ");
    private final TextField ortField = new TextField("Ort");

    public ImmobilieEditView() {
        addClassName("page-content");
        addClassName("immobilie-edit-view");

        add(createFormCard());
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        this.immobilieId = event.getRouteParameters()
                .get("immobilieId")
                .map(Long::valueOf)
                .orElse(null);

        ladeDummyDaten();
    }

    private Component createFormCard() {
        Div card = new Div();
        card.addClassName("form-card");

        Div header = new Div();
        header.addClassName("form-card-header");

        H3 title = new H3("Immobilie bearbeiten");
        title.addClassName("form-card-title");

        header.add(title);

        FormLayout form = new FormLayout();
        form.addClassName("form-card-content");

        bezeichnungField.setPlaceholder("z. B. Parkresidenz Süd");

        typSelect.setLabel("Immobilientyp");
        typSelect.setItems("Wohngebäude", "Mehrfamilienhaus", "Gewerbeimmobilie");

        baujahrField.setPlaceholder("z. B. 1998");

        gesamtflaecheField.setPlaceholder("z. B. 850");

        form.add(
                bezeichnungField,
                typSelect,
                baujahrField,
                gesamtflaecheField,
                strasseField,
                hausnummerField,
                plzField,
                ortField
        );

        Div actions = new Div();
        actions.addClassName("form-actions");

        Button abbrechenButton = new Button("Abbrechen");
        abbrechenButton.addClassName("secondary-button");
        abbrechenButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate(ImmobilienListView.class))
        );

        Button speichernButton = new Button("Änderungen speichern", VaadinIcon.CHECK.create());
        speichernButton.addClassName("primary-button");
        speichernButton.addClickListener(event -> {
            Notification.show("Änderungen wurden gespeichert");
            getUI().ifPresent(ui -> ui.navigate(ImmobilienListView.class));
        });

        actions.add(abbrechenButton, speichernButton);

        card.add(header, form, actions);

        return card;
    }

    private void ladeDummyDaten() {
        // Später wird hier aus dem ImmobilieService geladen.
        // Aktuell nur Dummy-Daten zum Testen.

        if (immobilieId == null) {
            return;
        }

        if (immobilieId == 1L) {
            bezeichnungField.setValue("Parkresidenz Süd");
            typSelect.setValue("Mehrfamilienhaus");
            baujahrField.setValue(1998);
            gesamtflaecheField.setValue(1850.0);

            strasseField.setValue("Parkstraße");
            hausnummerField.setValue("12");
            plzField.setValue("10115");
            ortField.setValue("Berlin");
        } else if (immobilieId == 2L) {
            bezeichnungField.setValue("Altbau Ensemble Mitte");
            typSelect.setValue("Wohngebäude");
            baujahrField.setValue(1965);
            gesamtflaecheField.setValue(980.0);

            strasseField.setValue("Hauptstraße");
            hausnummerField.setValue("45");
            plzField.setValue("80331");
            ortField.setValue("München");
        } else {
            bezeichnungField.setValue("Seeblick Quartier");
            typSelect.setValue("Gewerbeimmobilie");
            baujahrField.setValue(2012);
            gesamtflaecheField.setValue(2200.0);

            strasseField.setValue("Seestraße");
            hausnummerField.setValue("8-10");
            plzField.setValue("20099");
            ortField.setValue("Hamburg");
        }
    }

    @Override
    public String getPageTitle() {
        return "Immobilie bearbeiten";
    }

    @Override
    public String getPageSubtitle() {
        return "Immobilien > Immobilie bearbeiten";
    }

}