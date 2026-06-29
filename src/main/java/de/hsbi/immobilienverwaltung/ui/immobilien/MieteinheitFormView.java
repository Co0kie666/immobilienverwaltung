package de.hsbi.immobilienverwaltung.ui.immobilien;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
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
    private final IntegerField groesseFeld = new IntegerField("Größe");
    private final TextField stockwerkFeld = new TextField("Stockwerk");
    private final IntegerField zimmeranzahlFeld = new IntegerField("Zimmeranzahl");
    private final Select<Mieteinheitstatus> statusAuswahl = new Select<>();

    private final Binder<Mieteinheit> mieteinheitFormularBinder = new Binder<>(Mieteinheit.class);

    private final Span previewBezeichnung = new Span("Neue Mieteinheit");
    private final Span previewTyp = new Span("Typ auswählen");
    private final Span previewStatus = new Span("Frei");
    private final Span previewGroesse = new Span("– m²");
    private final Span previewZimmer = new Span("– Zimmer");
    private final Span previewStockwerk = new Span("Stockwerk –");

    public MieteinheitFormView(MieteinheitService mieteinheitService) {
        this.mieteinheitService = mieteinheitService;

        addClassName("page-content");
        addClassName("mieteinheit-form-view");

        konfiguriereFormularBinder();
        konfiguriereFormularFelder();
        konfiguriereLiveVorschau();

        add(erstelleSeitenInhalt());
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

    private Component erstelleSeitenInhalt() {
        Div wrapper = new Div();
        wrapper.addClassName("mieteinheit-form-wrapper");

        wrapper.add(
                erstelleHeroBereich(),
                erstelleFormularLayout(),
                erstelleFormularAktionen()
        );

        return wrapper;
    }

    private Component erstelleHeroBereich() {
        Div hero = new Div();
        hero.addClassName("mieteinheit-form-hero");

        Div textArea = new Div();
        textArea.addClassName("mieteinheit-form-hero-text");

        Span eyebrow = new Span("Immobilienverwaltung");
        eyebrow.addClassName("form-hero-eyebrow");

        H3 title = new H3("Neue Mieteinheit hinzufügen");
        title.addClassName("form-hero-title");

        Paragraph subtitle = new Paragraph("Erfasse Wohnungen, Büros, Gewerbeflächen oder Gesamtobjekte und lege direkt den passenden Status fest.");
        subtitle.addClassName("form-hero-subtitle");

        Div bullets = new Div();
        bullets.addClassName("mieteinheit-hero-bullets");
        bullets.add(
                erstelleHeroBullet("Typ & Status"),
                erstelleHeroBullet("Fläche & Zimmer"),
                erstelleHeroBullet("Direkt in der Immobilie")
        );

        textArea.add(eyebrow, title, subtitle, bullets);

        Div visual = erstelleVorschauKarte();

        hero.add(textArea, visual);
        return hero;
    }

    private Component erstelleHeroBullet(String text) {
        Span bullet = new Span(text);
        bullet.addClassName("mieteinheit-hero-bullet");
        return bullet;
    }

    private Div erstelleVorschauKarte() {
        Div preview = new Div();
        preview.addClassName("mieteinheit-preview-card");

        Div illustration = new Div();
        illustration.addClassName("mieteinheit-preview-illustration");

        Div door = new Div();
        door.addClassName("mieteinheit-preview-door");

        Div window = new Div();
        window.addClassName("mieteinheit-preview-window");

        illustration.add(door, window);

        Div content = new Div();
        content.addClassName("mieteinheit-preview-content");

        previewBezeichnung.addClassName("mieteinheit-preview-title");
        previewTyp.addClassName("mieteinheit-preview-subtitle");
        previewStatus.addClassNames("status-badge", "success");

        Div meta = new Div();
        meta.addClassName("mieteinheit-preview-meta");
        meta.add(
                erstellePreviewMeta("Fläche", previewGroesse),
                erstellePreviewMeta("Zimmer", previewZimmer),
                erstellePreviewMeta("Lage", previewStockwerk)
        );

        content.add(previewBezeichnung, previewTyp, previewStatus, meta);
        preview.add(illustration, content);

        return preview;
    }

    private Component erstellePreviewMeta(String labelText, Span value) {
        Div item = new Div();
        item.addClassName("mieteinheit-preview-meta-item");

        Span label = new Span(labelText);
        label.addClassName("mieteinheit-preview-meta-label");

        value.addClassName("mieteinheit-preview-meta-value");

        item.add(label, value);
        return item;
    }

    private Component erstelleFormularLayout() {
        Div layout = new Div();
        layout.addClassName("mieteinheit-form-grid");

        layout.add(
                erstelleBasisdatenKarte(),
                erstelleRaumdatenKarte(),
                erstelleHinweisKarte()
        );

        return layout;
    }

    private Component erstelleBasisdatenKarte() {
        FormLayout formular = erstelleZweispaltigesFormular();
        formular.add(bezeichnungFeld, mieteinheitTypAuswahl, statusAuswahl);
        formular.setColspan(bezeichnungFeld, 2);
        formular.setColspan(statusAuswahl, 2);

        return erstelleFormularKarte(
                "Basisdaten",
                "Name, Typ und Verfügbarkeit der neuen Einheit.",
                VaadinIcon.HOME,
                formular
        );
    }

    private Component erstelleRaumdatenKarte() {
        FormLayout formular = erstelleZweispaltigesFormular();
        formular.add(groesseFeld, zimmeranzahlFeld, stockwerkFeld);
        formular.setColspan(stockwerkFeld, 2);

        return erstelleFormularKarte(
                "Raumdaten",
                "Fläche, Zimmer und Lage innerhalb der Immobilie.",
                VaadinIcon.CUBES,
                formular
        );
    }

    private Component erstelleHinweisKarte() {
        Div card = new Div();
        card.addClassNames("form-card", "mieteinheit-info-card");

        Div iconBox = new Div(VaadinIcon.INFO_CIRCLE.create());
        iconBox.addClassName("mieteinheit-info-icon");

        Div text = new Div();
        text.addClassName("mieteinheit-info-text");

        Span title = new Span("Gut zu wissen");
        title.addClassName("mieteinheit-info-title");

        Paragraph body = new Paragraph("Nach dem Speichern kannst du in der Detailansicht einen Mietvertrag für diese Einheit anlegen.");
        body.addClassName("mieteinheit-info-body");

        text.add(title, body);
        card.add(iconBox, text);

        return card;
    }

    private Div erstelleFormularKarte(String titleText, String subtitleText, VaadinIcon icon, Component content) {
        Div card = new Div();
        card.addClassName("form-card");
        card.setWidthFull();

        Div header = new Div();
        header.addClassName("form-card-header");

        Div titleArea = new Div();
        titleArea.addClassName("form-card-title-area");

        Div iconBox = new Div(icon.create());
        iconBox.addClassName("form-card-icon");

        Div textArea = new Div();

        Span title = new Span(titleText);
        title.addClassName("form-card-title");

        Paragraph subtitle = new Paragraph(subtitleText);
        subtitle.addClassName("form-card-subtitle");

        textArea.add(title, subtitle);
        titleArea.add(iconBox, textArea);
        header.add(titleArea);

        Div body = new Div();
        body.addClassName("form-card-content");
        body.add(content);

        card.add(header, body);
        return card;
    }

    private void konfiguriereFormularFelder() {
        bezeichnungFeld.setPlaceholder("z. B. WE-01, Büro EG, Gesamtobjekt");
        bezeichnungFeld.setRequiredIndicatorVisible(true);
        bezeichnungFeld.setWidthFull();

        mieteinheitTypAuswahl.setLabel("Typ");
        mieteinheitTypAuswahl.setItems(MieteinheitTyp.values());
        mieteinheitTypAuswahl.setPlaceholder("Typ auswählen");
        mieteinheitTypAuswahl.setItemLabelGenerator(MieteinheitTyp::getLabel);
        mieteinheitTypAuswahl.setRequiredIndicatorVisible(true);
        mieteinheitTypAuswahl.setWidthFull();

        statusAuswahl.setLabel("Status");
        statusAuswahl.setItems(Mieteinheitstatus.values());
        statusAuswahl.setValue(Mieteinheitstatus.FREI);
        statusAuswahl.setItemLabelGenerator(Mieteinheitstatus::getLabel);
        statusAuswahl.setRequiredIndicatorVisible(true);
        statusAuswahl.setWidthFull();

        groesseFeld.setPlaceholder("z. B. 85");
        groesseFeld.setMin(0);
        groesseFeld.setErrorMessage("Größe darf nicht negativ sein");
        groesseFeld.setSuffixComponent(new Span("m²"));
        groesseFeld.setClearButtonVisible(true);
        groesseFeld.setWidthFull();

        stockwerkFeld.setPlaceholder("z. B. EG, 1. OG, Dachgeschoss");
        stockwerkFeld.setWidthFull();

        zimmeranzahlFeld.setPlaceholder("z. B. 3");
        zimmeranzahlFeld.setMin(0);
        zimmeranzahlFeld.setErrorMessage("Zimmeranzahl darf nicht negativ sein");
        zimmeranzahlFeld.setClearButtonVisible(true);
        zimmeranzahlFeld.setWidthFull();
    }

    private void konfiguriereLiveVorschau() {
        bezeichnungFeld.addValueChangeListener(event -> aktualisiereVorschau());
        mieteinheitTypAuswahl.addValueChangeListener(event -> aktualisiereVorschau());
        statusAuswahl.addValueChangeListener(event -> aktualisiereVorschau());
        groesseFeld.addValueChangeListener(event -> aktualisiereVorschau());
        zimmeranzahlFeld.addValueChangeListener(event -> aktualisiereVorschau());
        stockwerkFeld.addValueChangeListener(event -> aktualisiereVorschau());

        aktualisiereVorschau();
    }

    private void aktualisiereVorschau() {
        String bezeichnung = bezeichnungFeld.getValue();
        previewBezeichnung.setText(bezeichnung == null || bezeichnung.isBlank() ? "Neue Mieteinheit" : bezeichnung);

        MieteinheitTyp typ = mieteinheitTypAuswahl.getValue();
        previewTyp.setText(typ == null ? "Typ auswählen" : typ.getLabel());

        Mieteinheitstatus status = statusAuswahl.getValue();
        previewStatus.setText(status == null ? "Status wählen" : status.getLabel());
        previewStatus.removeClassNames("success", "warning", "danger", "neutral", "primary");
        previewStatus.addClassName(ermittleStatusStil(status));

        Integer groesse = groesseFeld.getValue();
        previewGroesse.setText(groesse == null ? "– m²" : groesse + " m²");

        Integer zimmer = zimmeranzahlFeld.getValue();
        previewZimmer.setText(zimmer == null ? "– Zimmer" : zimmer + " Zimmer");

        String stockwerk = stockwerkFeld.getValue();
        previewStockwerk.setText(stockwerk == null || stockwerk.isBlank() ? "Stockwerk –" : stockwerk);
    }

    private String ermittleStatusStil(Mieteinheitstatus status) {
        if (status == null) {
            return "neutral";
        }

        return switch (status) {
            case FREI -> "success";
            case IN_RENOVIERUNG -> "warning";
            case VERMIETET -> "primary";
        };
    }

    private Div erstelleFormularAktionen() {
        Div aktionen = new Div();
        aktionen.addClassName("form-bottom-actions");

        Button abbrechenButton = new Button("Abbrechen", VaadinIcon.CLOSE.create());
        abbrechenButton.addClassName("secondary-button");
        abbrechenButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate("immobilien/" + immobilieId))
        );

        Button speichernButton = new Button("Mieteinheit speichern", VaadinIcon.CHECK.create());
        speichernButton.addClassName("primary-button");
        speichernButton.addClickListener(event -> speichereMieteinheit());

        aktionen.add(abbrechenButton, speichernButton);

        return aktionen;
    }

    private FormLayout erstelleZweispaltigesFormular() {
        FormLayout form = new FormLayout();
        form.addClassName("form-layout-two-columns");

        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("620px", 2)
        );

        return form;
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
