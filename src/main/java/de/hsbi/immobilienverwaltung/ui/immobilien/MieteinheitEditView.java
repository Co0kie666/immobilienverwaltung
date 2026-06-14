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
import de.hsbi.immobilienverwaltung.domain.Mieteinheit;
import de.hsbi.immobilienverwaltung.domain.enums.MieteinheitTyp;
import de.hsbi.immobilienverwaltung.domain.enums.Mieteinheitstatus;
import de.hsbi.immobilienverwaltung.service.interfaces.MieteinheitService;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;
import jakarta.annotation.security.PermitAll;

@Route(value = "immobilien/:immobilieId/einheiten/:mieteinheitId/bearbeiten", layout = MainLayout.class)
@PermitAll
public class MieteinheitEditView extends Div implements HasPageHeader, BeforeEnterObserver {

    private final MieteinheitService mieteinheitService;

    private Long immobilieId;
    private Long mieteinheitId;
    private Mieteinheit mieteinheit;

    private final TextField einheitNummerFeld = new TextField("Einheit-Nr.");
    private final Select<MieteinheitTyp> mieteinheitTypAuswahl = new Select<>();
    private final IntegerField groesseFeld = new IntegerField("Größe in m²");
    private final TextField stockwerkFeld = new TextField("Stockwerk");
    private final IntegerField zimmeranzahlFeld = new IntegerField("Zimmeranzahl");
    private final Select<Mieteinheitstatus> statusAuswahl = new Select<>();

    private final Binder<Mieteinheit> mieteinheitFormularBinder = new Binder<>(Mieteinheit.class);

    public MieteinheitEditView(MieteinheitService mieteinheitService) {
        this.mieteinheitService = mieteinheitService;

        addClassName("page-content");
        addClassName("mieteinheit-edit-view");

        konfiguriereFormularBinder();

        add(erstelleFormularKarte());
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Die IDs stammen aus der URL
        // Beispiel: /immobilien/3/einheiten/7/bearbeiten
        this.immobilieId = event.getRouteParameters()
                .get("immobilieId")
                .map(Long::valueOf)
                .orElseThrow(() -> new IllegalArgumentException("Immobilie-ID fehlt."));

        this.mieteinheitId = event.getRouteParameters()
                .get("mieteinheitId")
                .map(Long::valueOf)
                .orElseThrow(() -> new IllegalArgumentException("Mieteinheit-ID fehlt."));

        ladeMieteinheit();

        // Die vorhandenen Daten werden mit Vaadin Binder in die Formularfelder geladen
        mieteinheitFormularBinder.readBean(mieteinheit);
    }

    private void ladeMieteinheit() {
        this.mieteinheit = mieteinheitService.findeMieteinheitNachId(mieteinheitId)
                .orElseThrow(() -> new IllegalArgumentException("Mieteinheit wurde nicht gefunden."));
    }

    // Vaadin Binder verbindet die Formularfelder mit den Getter- und Setter-Methoden der Entity
    private void konfiguriereFormularBinder() {
        mieteinheitFormularBinder.forField(einheitNummerFeld)
                .asRequired("Bezeichnung darf nicht leer sein.")
                .bind(Mieteinheit::getBezeichnung, Mieteinheit::setBezeichnung);

        mieteinheitFormularBinder.forField(mieteinheitTypAuswahl)
                .asRequired("Typ muss ausgewählt werden.")
                .bind(Mieteinheit::getTyp, Mieteinheit::setTyp);

        mieteinheitFormularBinder.forField(groesseFeld)
                .withValidator(
                        groesse -> groesse == null || groesse >= 0,
                        "Größe darf nicht negativ sein."
                )
                .bind(Mieteinheit::getGroesse, Mieteinheit::setGroesse);

        mieteinheitFormularBinder.forField(stockwerkFeld)
                .bind(Mieteinheit::getStockwerk, Mieteinheit::setStockwerk);

        mieteinheitFormularBinder.forField(zimmeranzahlFeld)
                .withValidator(
                        zimmerzahl -> zimmerzahl == null || zimmerzahl >= 0,
                        "Zimmeranzahl darf nicht negativ sein."
                )
                .bind(Mieteinheit::getZimmerzahl, Mieteinheit::setZimmerzahl);

        mieteinheitFormularBinder.forField(statusAuswahl)
                .asRequired("Status muss ausgewählt werden.")
                .bind(Mieteinheit::getStatus, Mieteinheit::setStatus);
    }

    private Div erstelleFormularKarte() {
        Div formularKarte = new Div();
        formularKarte.addClassName("form-card");

        Div formularKopf = new Div();
        formularKopf.addClassName("form-card-header");

        H3 titel = new H3("Mieteinheit bearbeiten");
        titel.addClassName("form-card-title");

        formularKopf.add(titel);

        FormLayout formular = new FormLayout();
        formular.addClassName("form-card-content");

        konfiguriereFormularFelder();

        formular.add(
                einheitNummerFeld,
                mieteinheitTypAuswahl,
                groesseFeld,
                stockwerkFeld,
                zimmeranzahlFeld,
                statusAuswahl
        );

        formularKarte.add(
                formularKopf,
                formular,
                erstelleFormularAktionen()
        );

        return formularKarte;
    }

    private void konfiguriereFormularFelder() {
        einheitNummerFeld.setPlaceholder("z. B. WE-01, Büro EG, Gesamtobjekt");

        mieteinheitTypAuswahl.setLabel("Typ");
        mieteinheitTypAuswahl.setItems(MieteinheitTyp.values());
        mieteinheitTypAuswahl.setItemLabelGenerator(MieteinheitTyp::getLabel);

        groesseFeld.setPlaceholder("z. B. 85");
        groesseFeld.setMin(0);
        groesseFeld.setErrorMessage("Größe darf nicht negativ sein");

        stockwerkFeld.setPlaceholder("z. B. EG, 1. OG");

        zimmeranzahlFeld.setPlaceholder("z. B. 3");
        zimmeranzahlFeld.setMin(0);
        zimmeranzahlFeld.setErrorMessage("Zimmeranzahl darf nicht negativ sein");

        statusAuswahl.setLabel("Status");
        statusAuswahl.setItems(Mieteinheitstatus.values());
        statusAuswahl.setItemLabelGenerator(Mieteinheitstatus::getLabel);

        einheitNummerFeld.setRequiredIndicatorVisible(true);
        mieteinheitTypAuswahl.setRequiredIndicatorVisible(true);
        statusAuswahl.setRequiredIndicatorVisible(true);
    }

    private Div erstelleFormularAktionen() {
        Div aktionen = new Div();
        aktionen.addClassName("form-actions");

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

        aktionen.add(abbrechenButton, speichernButton);

        return aktionen;
    }

    private void speichereAenderungen() {
        try {
            // Der Binder validiert die Eingaben und schreibt sie erst danach
            // zurück in die geladene Mieteinheit.
            mieteinheitFormularBinder.writeBean(mieteinheit);

            mieteinheitService.speichereMieteinheit(immobilieId, mieteinheit);

            Notification.show("Mieteinheit wurde aktualisiert");

            getUI().ifPresent(ui -> ui.navigate(
                    "immobilien/" + immobilieId + "/einheiten/" + mieteinheitId + "/details"
            ));

        } catch (ValidationException ex) {
            Notification.show(
                    "Bitte überprüfe die Eingaben.",
                    4000,
                    Notification.Position.BOTTOM_END
            );

        } catch (Exception ex) {
            Notification.show(
                    "Fehler beim Speichern: " + ex.getMessage(),
                    4000,
                    Notification.Position.BOTTOM_END
            );
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