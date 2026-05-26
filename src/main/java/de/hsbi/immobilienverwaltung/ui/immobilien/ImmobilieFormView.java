package de.hsbi.immobilienverwaltung.ui.immobilien;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import de.hsbi.immobilienverwaltung.domain.Adresse;
import de.hsbi.immobilienverwaltung.domain.Immobilie;
import de.hsbi.immobilienverwaltung.domain.enums.Immobilientyp;
import de.hsbi.immobilienverwaltung.security.LoginRequired;
import de.hsbi.immobilienverwaltung.service.interfaces.ImmobilieService;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;

@Route(value = "immobilien/neu", layout = MainLayout.class)
public class ImmobilieFormView extends Div implements HasPageHeader, LoginRequired {

    private final TextField bezeichnungField = new TextField("Bezeichnung");
    private final Select<Immobilientyp> typSelect = new Select<>();
    private final IntegerField baujahrField = new IntegerField("Baujahr");
    private final IntegerField gesamtflaecheField = new IntegerField("Gesamtfläche in m²");

    private final TextField strasseField = new TextField("Straße");
    private final TextField hausnummerField = new TextField("Hausnummer");
    private final TextField plzField = new TextField("PLZ");
    private final TextField ortField = new TextField("Ort");

    private final Checkbox gesamtobjektCheckbox =
            new Checkbox("Gesamtobjekt als einzelne Mieteinheit erstellen");

    private final ImmobilieService immobilieService;

    public ImmobilieFormView(ImmobilieService immobilieService) {
        this.immobilieService = immobilieService;

        addClassName("page-content");
        addClassName("immobilie-form-view");

        add(createFormCard());
    }

    private Div createFormCard() {
        Div card = new Div();
        card.addClassName("form-card");

        Div header = new Div();
        header.addClassName("form-card-header");

        H3 title = new H3("Stammdaten");
        title.addClassName("form-card-title");

        header.add(title);

        FormLayout form = new FormLayout();
        form.addClassName("form-card-content");

        bezeichnungField.setPlaceholder("z. B. Parkresidenz Süd");

        typSelect.setLabel("Immobilientyp");
        typSelect.setItems(Immobilientyp.values());
        typSelect.setPlaceholder("Typ auswählen");
        typSelect.setItemLabelGenerator(this::formatImmobilientyp);

        baujahrField.setPlaceholder("z. B. 1998");

        gesamtflaecheField.setPlaceholder("z. B. 850");

        bezeichnungField.setRequiredIndicatorVisible(true);
        typSelect.setRequiredIndicatorVisible(true);
        strasseField.setRequiredIndicatorVisible(true);
        hausnummerField.setRequiredIndicatorVisible(true);
        plzField.setRequiredIndicatorVisible(true);
        ortField.setRequiredIndicatorVisible(true);

        form.add(
                bezeichnungField,
                typSelect,
                baujahrField,
                gesamtflaecheField,
                strasseField,
                hausnummerField,
                plzField,
                ortField,
                gesamtobjektCheckbox
        );

        baujahrField.setMin(0);
        baujahrField.setErrorMessage("Baujahr darf nicht negativ sein");

        gesamtflaecheField.setMin(0);
        gesamtflaecheField.setErrorMessage("Fläche darf nicht negativ sein");

        form.setColspan(gesamtobjektCheckbox, 2);

        Div actions = new Div();
        actions.addClassName("form-actions");

        Button abbrechenButton = new Button("Abbrechen");
        abbrechenButton.addClassName("secondary-button");
        abbrechenButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate(ImmobilienListView.class))
        );

        Button speichernButton = new Button("Speichern", VaadinIcon.CHECK.create());
        speichernButton.addClassName("primary-button");
        speichernButton.addClickListener(event -> speichereImmobilie());

        actions.add(abbrechenButton, speichernButton);

        card.add(header, form, actions);

        return card;
    }

    private void speichereImmobilie() {
        try {
            Adresse adresse = new Adresse(
                    strasseField.getValue(),
                    hausnummerField.getValue(),
                    plzField.getValue(),
                    ortField.getValue()
            );

            Immobilie immobilie = new Immobilie(
                    bezeichnungField.getValue(),
                    typSelect.getValue(),
                    baujahrField.getValue(),
                    gesamtflaecheField.getValue(),
                    adresse
            );

            immobilieService.speichereImmobilie(immobilie);

            Notification.show("Immobilie wurde gespeichert: " + bezeichnungField.getValue());

            getUI().ifPresent(ui -> ui.navigate(ImmobilienListView.class));

        } catch (Exception ex) {
            Notification.show("Fehler beim Speichern: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
        }
    }

    private String formatImmobilientyp(Immobilientyp typ) {
        return switch (typ) {
            case WOHNGEBAEUDE -> "Wohngebäude";
            case MEHRFAMILIENHAUS -> "Mehrfamilienhaus";
            case GEWERBEIMMOBILIE -> "Gewerbeimmobilie";
        };
    }

    @Override
    public String getPageTitle() {
        return "Immobilie anlegen";
    }

    @Override
    public String getPageSubtitle() {
        return "Immobilien > Immobilie anlegen";
    }
}