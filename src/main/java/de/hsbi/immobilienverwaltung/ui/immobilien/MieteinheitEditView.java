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
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;
import de.hsbi.immobilienverwaltung.domain.Mieteinheit;
import de.hsbi.immobilienverwaltung.domain.enums.MieteinheitTyp;
import de.hsbi.immobilienverwaltung.domain.enums.Mieteinheitstatus;
import de.hsbi.immobilienverwaltung.service.interfaces.MieteinheitService;
import jakarta.annotation.security.PermitAll;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;

@Route(value = "immobilien/:immobilieId/einheiten/:mieteinheitId/bearbeiten", layout = MainLayout.class)
@PermitAll
public class MieteinheitEditView extends Div implements HasPageHeader, BeforeEnterObserver {

    private final MieteinheitService mieteinheitService;
    private Mieteinheit mieteinheit;
    private Long immobilieId;
    private Long mieteinheitId;

    private final TextField nummerField = new TextField("Einheit-Nr.");
    private final Select<MieteinheitTyp> typSelect = new Select<>();
    private final IntegerField groesseField = new IntegerField("Größe in m²");
    private final TextField stockwerkField = new TextField("Stockwerk");
    private final IntegerField zimmeranzahlField = new IntegerField("Zimmeranzahl");
    private final Select<Mieteinheitstatus> statusSelect = new Select<>();
    private final Binder<Mieteinheit> binder = new Binder<>(Mieteinheit.class);

    public MieteinheitEditView(MieteinheitService mieteinheitService) {
        this.mieteinheitService = mieteinheitService;
        addClassName("page-content");
        addClassName("mieteinheit-edit-view");
        konfiguriereBinder();

        add(createFormCard());
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        this.immobilieId = event.getRouteParameters()
                .get("immobilieId")
                .map(Long::valueOf)
                .orElseThrow(() -> new IllegalArgumentException("Immobilie-ID fehlt."));

        this.mieteinheitId = event.getRouteParameters()
                .get("mieteinheitId")
                .map(Long::valueOf)
                .orElseThrow(() -> new IllegalArgumentException("Mieteinheit-ID fehlt."));

        this.mieteinheit = mieteinheitService.findeMieteinheitNachId(mieteinheitId)
                .orElseThrow(() -> new IllegalArgumentException("Mieteinheit wurde nicht gefunden."));

        binder.readBean(mieteinheit); // fuellt die Felder
    }

    private void konfiguriereBinder() {
        binder.forField(nummerField)
                .asRequired("Bezeichnung darf nicht leer sein.")
                .bind(Mieteinheit::getBezeichnung, Mieteinheit::setBezeichnung);

        binder.forField(typSelect)
                .asRequired("Typ muss ausgewählt werden.")
                .bind(Mieteinheit::getTyp, Mieteinheit::setTyp);

        binder.forField(groesseField)
                .withValidator(
                        groesse -> groesse == null || groesse >= 0,
                        "Größe darf nicht negativ sein."
                )
                .bind(Mieteinheit::getGroesse, Mieteinheit::setGroesse);

        binder.forField(stockwerkField)
                .bind(Mieteinheit::getStockwerk, Mieteinheit::setStockwerk);

        binder.forField(zimmeranzahlField)
                .withValidator(
                        zimmerzahl -> zimmerzahl == null || zimmerzahl >= 0,
                        "Zimmeranzahl darf nicht negativ sein."
                )
                .bind(Mieteinheit::getZimmerzahl, Mieteinheit::setZimmerzahl);

        binder.forField(statusSelect)
                .asRequired("Status muss ausgewählt werden.")
                .bind(Mieteinheit::getStatus, Mieteinheit::setStatus);
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
        typSelect.setItemLabelGenerator(MieteinheitTyp::getLabel);

        groesseField.setPlaceholder("z. B. 85");
        groesseField.setMin(0);

        stockwerkField.setPlaceholder("z. B. EG, 1. OG");

        zimmeranzahlField.setMin(0);
        zimmeranzahlField.setPlaceholder("z. B. 3");

        statusSelect.setLabel("Status");
        statusSelect.setItems(Mieteinheitstatus.values());
        statusSelect.setItemLabelGenerator(Mieteinheitstatus::getLabel);

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
            binder.writeBean(mieteinheit);

            mieteinheitService.speichereMieteinheit(immobilieId, mieteinheit);

            Notification.show("Mieteinheit wurde aktualisiert");

            getUI().ifPresent(ui -> ui.navigate(
                    "immobilien/" + immobilieId + "/einheiten/" + mieteinheitId + "/details"
            ));

        } catch (ValidationException ex) {
            Notification.show("Bitte überprüfe die Eingaben.", 4000, Notification.Position.BOTTOM_END);

        } catch (Exception ex) {
            Notification.show("Fehler beim Speichern: " + ex.getMessage(), 4000, Notification.Position.BOTTOM_END);
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