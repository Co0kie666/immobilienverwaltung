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

@Route(value = "immobilien/:immobilieId/einheiten/neu", layout = MainLayout.class)
@PermitAll
public class MieteinheitFormView extends Div implements HasPageHeader, BeforeEnterObserver {

    private Long immobilieId;

    private final MieteinheitService mieteinheitService;

    private final TextField bezeichnungFeld = new TextField("Bezeichnung");
    private final Select<MieteinheitTyp> mieteinheitTypAuswahl = new Select<>();
    private final IntegerField groesseFeld = new IntegerField("Größe in m²");
    private final TextField stockwerkFeld = new TextField("Stockwerk");
    private final IntegerField zimmeranzahlFeld = new IntegerField("Zimmeranzahl");
    private final Select<Mieteinheitstatus> statusAuswahl = new Select<>();

    private final Binder<Mieteinheit> mieteinheitFormularBinder = new Binder<>(Mieteinheit.class);

    public MieteinheitFormView(MieteinheitService mieteinheitService) {
        this.mieteinheitService = mieteinheitService;

        addClassName("page-content");
        addClassName("mieteinheit-form-view");

        konfiguriereFormularBinder();

        add(erstelleFormularKarte());
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Die ID stammt aus der URL.
        // Beispiel: /immobilien/3/einheiten/neu -> immobilieId = 3
        this.immobilieId = event.getRouteParameters()
                .get("immobilieId")
                .map(Long::valueOf)
                .orElseThrow(() -> new IllegalArgumentException("Immobilie-ID fehlt."));
    }

    // Der Binder verbindet die Formularfelder mit den passenden Eigenschaften
    // der Mieteinheit und definiert gleichzeitig die Validierungsregeln.
    private void konfiguriereFormularBinder() {
        mieteinheitFormularBinder.forField(bezeichnungFeld)
                .asRequired("Bezeichnung darf nicht leer sein.")
                .bind(Mieteinheit::getBezeichnung, Mieteinheit::setBezeichnung);

        mieteinheitFormularBinder.forField(mieteinheitTypAuswahl)
                .asRequired("Typ muss ausgewählt werden.")
                .bind(Mieteinheit::getTyp, Mieteinheit::setTyp);

        mieteinheitFormularBinder.forField(statusAuswahl)
                .asRequired("Status muss ausgewählt werden.")
                .bind(Mieteinheit::getStatus, Mieteinheit::setStatus);

        mieteinheitFormularBinder.forField(groesseFeld)
                .withValidator(
                        groesse -> groesse == null || groesse >= 0,
                        "Größe darf nicht negativ sein."
                )
                .bind(Mieteinheit::getGroesse, Mieteinheit::setGroesse);

        mieteinheitFormularBinder.forField(zimmeranzahlFeld)
                .withValidator(
                        zimmerzahl -> zimmerzahl == null || zimmerzahl >= 0,
                        "Zimmeranzahl darf nicht negativ sein."
                )
                .bind(Mieteinheit::getZimmerzahl, Mieteinheit::setZimmerzahl);

        mieteinheitFormularBinder.forField(stockwerkFeld)
                .bind(Mieteinheit::getStockwerk, Mieteinheit::setStockwerk);
    }

    private Div erstelleFormularKarte() {
        Div formularKarte = new Div();
        formularKarte.addClassName("form-card");

        Div formularKopf = new Div();
        formularKopf.addClassName("form-card-header");

        H3 titel = new H3("Mieteinheit hinzufügen");
        titel.addClassName("form-card-title");

        formularKopf.add(titel);

        FormLayout formular = new FormLayout();
        formular.addClassName("form-card-content");

        konfiguriereFormularFelder();

        formular.add(
                bezeichnungFeld,
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
        bezeichnungFeld.setPlaceholder("z. B. WE-01, Büro EG, Gesamtobjekt");

        mieteinheitTypAuswahl.setLabel("Typ");
        mieteinheitTypAuswahl.setItems(MieteinheitTyp.values());
        mieteinheitTypAuswahl.setItemLabelGenerator(MieteinheitTyp::getLabel);

        statusAuswahl.setLabel("Status");
        statusAuswahl.setItems(Mieteinheitstatus.values());
        statusAuswahl.setValue(Mieteinheitstatus.FREI);
        statusAuswahl.setItemLabelGenerator(Mieteinheitstatus::getLabel);

        groesseFeld.setPlaceholder("z. B. 85");
        groesseFeld.setMin(0);
        groesseFeld.setErrorMessage("Größe darf nicht negativ sein");

        stockwerkFeld.setPlaceholder("z. B. EG, 1. OG");

        zimmeranzahlFeld.setPlaceholder("z. B. 3");
        zimmeranzahlFeld.setMin(0);
        zimmeranzahlFeld.setErrorMessage("Zimmeranzahl darf nicht negativ sein");

        bezeichnungFeld.setRequiredIndicatorVisible(true);
        mieteinheitTypAuswahl.setRequiredIndicatorVisible(true);
        statusAuswahl.setRequiredIndicatorVisible(true);
    }

    private Div erstelleFormularAktionen() {
        Div aktionen = new Div();
        aktionen.addClassName("form-actions");

        Button abbrechenButton = new Button("Abbrechen");
        abbrechenButton.addClassName("secondary-button");
        abbrechenButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate("immobilien/" + immobilieId))
        );

        Button speichernButton = new Button("Speichern", VaadinIcon.CHECK.create());
        speichernButton.addClassName("primary-button");
        speichernButton.addClickListener(event -> speichereMieteinheit());

        aktionen.add(abbrechenButton, speichernButton);

        return aktionen;
    }

    private void speichereMieteinheit() {
        try {
            Mieteinheit mieteinheit = new Mieteinheit();

            // Formularwerte validieren und in die neue Mieteinheit übernehmen.
            mieteinheitFormularBinder.writeBean(mieteinheit);

            mieteinheitService.speichereMieteinheit(immobilieId, mieteinheit);

            Notification.show("Mieteinheit wurde gespeichert: " + mieteinheit.getBezeichnung());

            getUI().ifPresent(ui -> ui.navigate("immobilien/" + immobilieId));

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
        return "Mieteinheit hinzufügen";
    }

    @Override
    public String getPageSubtitle() {
        return "Immobilien > Detailansicht > Mieteinheit hinzufügen";
    }
}