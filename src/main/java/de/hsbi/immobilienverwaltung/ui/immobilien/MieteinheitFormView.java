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
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;

import de.hsbi.immobilienverwaltung.domain.Mieteinheit;
import de.hsbi.immobilienverwaltung.domain.enums.MieteinheitTyp;
import de.hsbi.immobilienverwaltung.domain.enums.Mieteinheitstatus;
import de.hsbi.immobilienverwaltung.service.interfaces.MieteinheitService;
import jakarta.annotation.security.PermitAll;

@Route(value = "immobilien/:immobilieId/einheiten/neu", layout = MainLayout.class)
@PermitAll
public class MieteinheitFormView extends Div implements HasPageHeader, BeforeEnterObserver {

    private Long immobilieId;
    private final MieteinheitService mieteinheitService;

    private final TextField nummerField = new TextField("Bezeichnung");
    private final IntegerField groesseField = new IntegerField("Größe in m²");
    private final TextField stockwerkField = new TextField("Stockwerk");
    private final IntegerField zimmeranzahlField = new IntegerField("Zimmeranzahl");
    private final Select<MieteinheitTyp> typSelect = new Select<>();
    private final Select<Mieteinheitstatus> statusSelect = new Select<>();

    private final Binder<Mieteinheit> binder = new Binder<>(Mieteinheit.class);

    public MieteinheitFormView(MieteinheitService mieteinheitService) {
        this.mieteinheitService = mieteinheitService;

        addClassName("page-content");
        addClassName("mieteinheit-form-view");

        konfiguriereBinder();

        add(createFormCard());
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        this.immobilieId = event.getRouteParameters()
                .get("immobilieId")
                .map(Long::valueOf)
                .orElseThrow(() -> new IllegalArgumentException("Immobilie-ID fehlt."));
    }

    // // Vaadin Binder verbindet die Formularfelder mit den Getter- und Setter-Methoden der Entity
    private void konfiguriereBinder() {
        binder.forField(nummerField)
                .asRequired("Bezeichnung darf nicht leer sein.")
                .bind(Mieteinheit::getBezeichnung, Mieteinheit::setBezeichnung);

        binder.forField(typSelect)
                .asRequired("Typ muss ausgewählt werden.")
                .bind(Mieteinheit::getTyp, Mieteinheit::setTyp);

        binder.forField(statusSelect)
                .asRequired("Status muss ausgewählt werden.")
                .bind(Mieteinheit::getStatus, Mieteinheit::setStatus);

        binder.forField(groesseField)
                .withValidator(
                        groesse -> groesse == null || groesse >= 0,
                        "Größe darf nicht negativ sein."
                )
                .bind(Mieteinheit::getGroesse, Mieteinheit::setGroesse);

        binder.forField(zimmeranzahlField)
                .withValidator(
                        zimmerzahl -> zimmerzahl == null || zimmerzahl >= 0,
                        "Zimmeranzahl darf nicht negativ sein."
                )
                .bind(Mieteinheit::getZimmerzahl, Mieteinheit::setZimmerzahl);

        binder.forField(stockwerkField)
                .bind(Mieteinheit::getStockwerk, Mieteinheit::setStockwerk);
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
        typSelect.setItemLabelGenerator(MieteinheitTyp::getLabel);

        statusSelect.setLabel("Status");
        statusSelect.setItems(Mieteinheitstatus.values());
        statusSelect.setValue(Mieteinheitstatus.FREI);
        statusSelect.setItemLabelGenerator(Mieteinheitstatus::getLabel);

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
            Mieteinheit mieteinheit = new Mieteinheit();

            // schreibt alle Werte aus den Formularfeldern in das Mieteinheit Objekt
            binder.writeBean(mieteinheit);

            mieteinheitService.speichereMieteinheit(immobilieId, mieteinheit);

            Notification.show("Mieteinheit wurde gespeichert: " + mieteinheit.getBezeichnung());

            getUI().ifPresent(ui -> ui.navigate("immobilien/" + immobilieId));

        } catch (ValidationException ex) {
            Notification.show("Bitte überprüfe die Eingaben.", 4000, Notification.Position.BOTTOM_END);

        } catch (Exception ex) {
            Notification.show("Fehler beim Speichern: " + ex.getMessage(), 4000, Notification.Position.BOTTOM_END);
        }
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