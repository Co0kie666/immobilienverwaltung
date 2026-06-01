package de.hsbi.immobilienverwaltung.ui.mieter;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import de.hsbi.immobilienverwaltung.security.LoginRequired;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;
import com.vaadin.flow.component.formlayout.FormLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Route(value = "mietvertrag-details", layout = MainLayout.class)
public class MietvertragListView extends Div implements HasPageHeader, HasUrlParameter<String>, LoginRequired {

    private final List<TextField> editierbareFelder = new ArrayList<>();
    private final List<Select<String>> editierbareSelects = new ArrayList<>();

    private Button speichernButton;
    private Button kuendigenButton;
    private MieterDummyDaten.MietvertragRow aktuellerMietvertrag;

    public MietvertragListView() {
        addClassName("page-content");
    }

    // Holt die Mietvertrag-ID aus der URL
    @Override
    public void setParameter(BeforeEvent event, String mietvertragId) {
        removeAll();

        editierbareFelder.clear();
        editierbareSelects.clear();
        speichernButton = null;
        kuendigenButton = null;
        aktuellerMietvertrag = null;

        Optional<MieterDummyDaten.MietvertragRow> optionalMietvertrag =
                MieterDummyDaten.findMietvertragById(mietvertragId);

        if (optionalMietvertrag.isEmpty()) {
            add(createNotFoundCard());
            return;
        }

        MieterDummyDaten.MietvertragRow mietvertrag = optionalMietvertrag.get();
        aktuellerMietvertrag = mietvertrag;

        add(
                createMietvertragHeader(mietvertrag),
                createContentLayout(mietvertrag)
        );

        setBearbeitenModus(false);
    }

    // Oberer Bereich mit Titel, Status und Aktionen
    private Component createMietvertragHeader(MieterDummyDaten.MietvertragRow mietvertrag) {
        Div headerCard = new Div();
        headerCard.addClassName("card");
        headerCard.addClassName("page-section");

        boolean istGekuendigt = "Gekündigt".equals(mietvertrag.status());

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Button backButton = new Button(VaadinIcon.ARROW_LEFT.create());
        backButton.addClassName("icon-button");

        // Navigiert auf zurück zum Tab Verträge
        backButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate("mieter-vertraege?tab=vertraege"))
        );

        VerticalLayout titleArea = new VerticalLayout();
        titleArea.setPadding(false);
        titleArea.setSpacing(false);

        HorizontalLayout titleRow = new HorizontalLayout();
        titleRow.setAlignItems(FlexComponent.Alignment.CENTER);
        titleRow.setSpacing(true);

        Span title = new Span("Mietvertrag " + mietvertrag.id());
        title.addClassName("card-title");

        Span statusBadge = new Span(mietvertrag.status());
        statusBadge.addClassNames("status-badge", mietvertrag.statusStyle());

        titleRow.add(title, statusBadge);

        Span subtitle = new Span(mietvertrag.mieter() + " • " + mietvertrag.mietobjektName());
        subtitle.addClassName("card-subtitle");

        titleArea.add(titleRow, subtitle);

        HorizontalLayout leftArea = new HorizontalLayout();
        leftArea.setAlignItems(FlexComponent.Alignment.CENTER);
        leftArea.setSpacing(true);
        leftArea.add(backButton, titleArea);

        header.add(leftArea);

        // Gekündigte Verträge sind nur noch reine Ansicht
        if (!istGekuendigt) {
            Button ansichtButton = new Button("Ansicht");
            ansichtButton.addClassName("primary-button");

            Button bearbeitenButton = new Button("Bearbeiten");
            bearbeitenButton.addClassName("secondary-button");

            ansichtButton.addClickListener(event -> {
                setBearbeitenModus(false);

                ansichtButton.removeClassName("secondary-button");
                ansichtButton.addClassName("primary-button");

                bearbeitenButton.removeClassName("primary-button");
                bearbeitenButton.addClassName("secondary-button");
            });

            bearbeitenButton.addClickListener(event -> {
                setBearbeitenModus(true);

                bearbeitenButton.removeClassName("secondary-button");
                bearbeitenButton.addClassName("primary-button");

                ansichtButton.removeClassName("primary-button");
                ansichtButton.addClassName("secondary-button");
            });

            kuendigenButton = new Button("Kündigen", VaadinIcon.TRASH.create());
            kuendigenButton.addClassName("secondary-button");
            kuendigenButton.setEnabled(false);

            speichernButton = new Button("Speichern", VaadinIcon.CHECK.create());
            speichernButton.addClassName("secondary-button");
            speichernButton.setEnabled(false);

            HorizontalLayout rightArea = new HorizontalLayout();
            rightArea.setAlignItems(FlexComponent.Alignment.CENTER);
            rightArea.setSpacing(true);
            rightArea.add(ansichtButton, bearbeitenButton, kuendigenButton, speichernButton);

            header.add(rightArea);
        }

        headerCard.add(header);

        return headerCard;
    }

    // Inhalt unter dem Header
    private Component createContentLayout(MieterDummyDaten.MietvertragRow mietvertrag) {
        HorizontalLayout contentLayout = new HorizontalLayout();
        contentLayout.setWidthFull();
        contentLayout.setMaxWidth("1400px");
        contentLayout.setSpacing(true);
        contentLayout.setAlignItems(FlexComponent.Alignment.START);
        contentLayout.getStyle().set("margin", "0 auto");
        contentLayout.getStyle().set("box-sizing", "border-box");

        VerticalLayout leftColumn = new VerticalLayout();
        leftColumn.setPadding(false);
        leftColumn.setSpacing(true);
        leftColumn.setWidth("0");
        leftColumn.getStyle().set("min-width", "0");

        leftColumn.add(
                createMieterEinheitCard(mietvertrag),
                createLaufzeitCard(mietvertrag)
        );

        VerticalLayout rightColumn = new VerticalLayout();
        rightColumn.setPadding(false);
        rightColumn.setSpacing(true);
        rightColumn.setWidth("380px");
        rightColumn.getStyle().set("min-width", "360px");

        rightColumn.add(
                createFinanzenCard(mietvertrag),
                createKautionCard(mietvertrag)
        );

        contentLayout.add(leftColumn, rightColumn);
        contentLayout.setFlexGrow(1, leftColumn);
        contentLayout.setFlexGrow(0, rightColumn);

        return contentLayout;
    }

    // Karte für Mieter und Einheit
    private Component createMieterEinheitCard(MieterDummyDaten.MietvertragRow mietvertrag) {
        Div card = new Div();
        card.addClassName("card");
        card.setWidthFull();

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        Span title = new Span("Mieter & Einheit");
        title.addClassName("card-title");

        content.add(
                title,
                createReadonlyInfoBlock("Hauptmieter", mietvertrag.mieter() + " • " + mietvertrag.mieterEmail()),
                createReadonlyInfoBlock("Mietobjekt", mietvertrag.mietobjektName()),
                createReadonlyInfoBlock("Einheit", mietvertrag.mietobjektDetails())
        );

        card.add(content);
        return card;
    }

    // Karte für Vertragslaufzeit und Fristen
    private Component createLaufzeitCard(MieterDummyDaten.MietvertragRow mietvertrag) {
        Div card = new Div();
        card.addClassName("card");
        card.setWidthFull();
        card.getStyle().set("overflow", "hidden");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);
        content.setWidthFull();

        Span title = new Span("Vertragslaufzeit & Fristen");
        title.addClassName("card-title");

        TextField vertragsbeginn = createTextField("Vertragsbeginn", mietvertrag.vertragsbeginn());
        TextField vertragsende = createTextField(
                "Vertragsende",
                mietvertrag.vertragsende().isBlank() ? "tt.mm.jjjj" : mietvertrag.vertragsende()
        );

        FormLayout topForm = new FormLayout();
        topForm.setWidthFull();
        topForm.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("700px", 2)
        );
        topForm.add(vertragsbeginn, vertragsende);

        Select<String> kuendigungsfrist = createSelect("Kündigungsfrist", mietvertrag.kuendigungsfrist());
        TextField mindestmietdauer = createTextField("Mindestmietdauer bis", mietvertrag.mindestmietdauerBis());
        TextField kuendigungseingang = createTextField(
                "Kündigungseingang",
                mietvertrag.kuendigungseingang().isBlank() ? "tt.mm.jjjj" : mietvertrag.kuendigungseingang()
        );

        FormLayout bottomForm = new FormLayout();
        bottomForm.setWidthFull();
        bottomForm.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("700px", 3)
        );
        bottomForm.add(kuendigungsfrist, mindestmietdauer, kuendigungseingang);

        content.add(title, topForm, bottomForm);

        card.add(content);
        return card;
    }

    // Karte für Finanzen
    private Component createFinanzenCard(MieterDummyDaten.MietvertragRow mietvertrag) {
        Div card = new Div();
        card.addClassName("card");
        card.setWidthFull();
        card.getStyle().set("overflow", "hidden");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);
        content.setWidthFull();

        Span title = new Span("Finanzen");
        title.addClassName("card-title");

        TextField nettokaltmiete = createTextField("Nettokaltmiete", mietvertrag.nettokaltmiete());

        TextField betriebskosten = createTextField("Betriebskosten", mietvertrag.betriebskosten());
        TextField heizkosten = createTextField("Heizkosten", mietvertrag.heizkosten());

        FormLayout kostenForm = new FormLayout();
        kostenForm.setWidthFull();
        kostenForm.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("320px", 2)
        );
        kostenForm.add(betriebskosten, heizkosten);

        Span warmmieteLabel = new Span("Bruttowarmmiete");
        warmmieteLabel.addClassName("card-subtitle");

        Span warmmieteValue = new Span(mietvertrag.bruttowarmmiete());
        warmmieteValue.addClassName("card-title");

        HorizontalLayout warmmieteRow = new HorizontalLayout();
        warmmieteRow.setWidthFull();
        warmmieteRow.setAlignItems(FlexComponent.Alignment.CENTER);
        warmmieteRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        warmmieteRow.add(warmmieteLabel, warmmieteValue);

        content.add(title, nettokaltmiete, kostenForm, warmmieteRow);

        card.add(content);
        return card;
    }

    // Karte für Kaution
    private Component createKautionCard(MieterDummyDaten.MietvertragRow mietvertrag) {
        Div card = new Div();
        card.addClassName("card");
        card.setWidthFull();
        card.getStyle().set("overflow", "hidden");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);
        content.setWidthFull();

        Span title = new Span("Kaution");
        title.addClassName("card-title");

        Select<String> kautionsart = createSelect("Kautionsart", mietvertrag.kautionsart());

        TextField sollBetrag = createTextField("Soll-Betrag", mietvertrag.kautionSoll());
        TextField eingezahlt = createTextField("Eingezahlt", mietvertrag.kautionGezahlt());

        FormLayout kautionForm = new FormLayout();
        kautionForm.setWidthFull();
        kautionForm.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("320px", 2)
        );
        kautionForm.add(sollBetrag, eingezahlt);

        Span statusBadge = new Span(mietvertrag.kautionStatus());
        statusBadge.addClassNames("status-badge", mietvertrag.kautionStatusStyle());

        content.add(title, kautionsart, kautionForm, statusBadge);

        card.add(content);
        return card;
    }

    // Erstellt ein Textfeld und merkt es sich für den Bearbeiten-Modus
    private TextField createTextField(String label, String value) {
        TextField field = new TextField(label);
        field.setValue(value == null ? "" : value);
        field.setWidthFull();
        field.getStyle().set("min-width", "0");

        editierbareFelder.add(field);

        return field;
    }

    // Erstellt ein Select und merkt es sich für den Bearbeiten-Modus
    private Select<String> createSelect(String label, String value) {
        Select<String> select = new Select<>();
        select.setLabel(label);

        if ("Kündigungsfrist".equals(label)) {
            select.setItems(
                    "Gesetzlich (3 Monate)",
                    "1 Monat",
                    "6 Monate",
                    "Individuell"
            );
        } else if ("Kautionsart".equals(label)) {
            select.setItems(
                    "Barkaution / Überweisung",
                    "Bankbürgschaft",
                    "Keine Kaution"
            );
        } else {
            select.setItems(value);
        }

        select.setValue(value);
        select.setWidthFull();
        select.getStyle().set("min-width", "0");

        editierbareSelects.add(select);

        return select;
    }

    // Schaltet zwischen Ansicht und Bearbeiten um
    private void setBearbeitenModus(boolean bearbeitenAktiv) {
        editierbareFelder.forEach(field -> field.setReadOnly(!bearbeitenAktiv));
        editierbareSelects.forEach(select -> select.setEnabled(bearbeitenAktiv));

        if (speichernButton != null) {
            speichernButton.setEnabled(bearbeitenAktiv);

            if (bearbeitenAktiv) {
                speichernButton.removeClassName("secondary-button");
                speichernButton.addClassName("primary-button");
            } else {
                speichernButton.removeClassName("primary-button");
                speichernButton.addClassName("secondary-button");
            }
        }

        if (kuendigenButton != null) {
            boolean kuendigenMoeglich =
                    bearbeitenAktiv
                            && aktuellerMietvertrag != null
                            && "Aktiv".equals(aktuellerMietvertrag.status());

            kuendigenButton.setEnabled(kuendigenMoeglich);

            if (kuendigenMoeglich) {
                kuendigenButton.removeClassName("secondary-button");
                kuendigenButton.addClassName("danger-button");
            } else {
                kuendigenButton.removeClassName("danger-button");
                kuendigenButton.addClassName("secondary-button");
            }
        }
    }

    // Erstellt einen einfachen Info-Block
    private Component createReadonlyInfoBlock(String labelText, String valueText) {
        VerticalLayout block = new VerticalLayout();
        block.setPadding(false);
        block.setSpacing(false);

        Span label = new Span(labelText);
        label.addClassName("card-subtitle");

        Span value = new Span(valueText);

        block.add(label, value);

        return block;
    }

    // Wird angezeigt, wenn kein Mietvertrag gefunden wurde
    private Component createNotFoundCard() {
        Div card = new Div();
        card.addClassName("empty-state");

        Span title = new Span("Mietvertrag nicht gefunden");
        title.addClassName("empty-state-title");

        Span text = new Span("Für diese Dummy-ID gibt es aktuell keine Daten.");
        text.addClassName("empty-state-text");

        card.add(title, text);

        return card;
    }

    @Override
    public String getPageTitle() {
        return "Mietvertrag";
    }

    @Override
    public String getPageSubtitle() {
        return "Mietvertragsdaten anzeigen und bearbeiten";
    }
}