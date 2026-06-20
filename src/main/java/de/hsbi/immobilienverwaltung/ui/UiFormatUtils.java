package de.hsbi.immobilienverwaltung.ui;

import de.hsbi.immobilienverwaltung.domain.Adresse;
import de.hsbi.immobilienverwaltung.domain.Immobilie;
import de.hsbi.immobilienverwaltung.domain.Mieteinheit;
import de.hsbi.immobilienverwaltung.domain.Mieter;
import de.hsbi.immobilienverwaltung.domain.Mietvertrag;
import de.hsbi.immobilienverwaltung.domain.enums.Immobilientyp;
import de.hsbi.immobilienverwaltung.domain.enums.MieteinheitTyp;
import de.hsbi.immobilienverwaltung.domain.enums.Mieteinheitstatus;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Zentrale Hilfsklasse für reine Anzeige-Formatierungen in der UI.
 * Enthält keine Geschäftslogik, sondern nur wiederverwendbare Text-, Datums-,
 * Geld-, Adress- und Objektformatierungen für Vaadin-Views.
 */
public final class UiFormatUtils {

    private static final DateTimeFormatter DEUTSCHES_DATUM_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private UiFormatUtils() {
        // Utility-Klasse soll nicht instanziiert werden.
    }

    // =========================================================
    // Allgemeine Text-Helfer
    // =========================================================

    public static boolean istLeer(String text) {
        return text == null || text.isBlank();
    }

    public static String wertOderLeer(String wert) {
        return wert == null ? "" : wert;
    }

    public static String wertOderStrich(Object wert) {
        if (wert == null) {
            return "-";
        }

        if (wert instanceof String text && text.isBlank()) {
            return "-";
        }

        return wert.toString();
    }

    // =========================================================
    // Datum und Geldbeträge
    // =========================================================

    public static String formatiereDatum(LocalDate datum) {
        if (datum == null) {
            return "-";
        }

        return datum.format(DEUTSCHES_DATUM_FORMAT);
    }

    public static String formatiereBetrag(BigDecimal betrag) {
        if (betrag == null) {
            return "0,00 €";
        }

        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        return formatter.format(betrag);
    }

    public static String formatiereBetragOderStrich(BigDecimal betrag) {
        if (betrag == null) {
            return "-";
        }

        return formatiereBetrag(betrag);
    }

    public static String formatiereBetragKurz(BigDecimal betrag) {
        if (betrag == null || betrag.compareTo(BigDecimal.ZERO) == 0) {
            return "0 €";
        }

        return formatiereBetrag(betrag);
    }

    public static String formatiereEuro(Double betrag) {
        double wert = betrag == null ? 0 : betrag;
        return NumberFormat.getCurrencyInstance(Locale.GERMANY).format(wert);
    }

    public static String formatiereEuroOderStrich(Double betrag) {
        if (betrag == null) {
            return "-";
        }

        return formatiereEuro(betrag);
    }

    // =========================================================
    // Adressen
    // =========================================================

    public static String formatiereAdresse(Immobilie immobilie) {
        return formatiereAdresse(immobilie, "-");
    }

    public static String formatiereAdresse(Immobilie immobilie, String ersatzText) {
        if (immobilie == null) {
            return ersatzText;
        }

        return formatiereAdresse(immobilie.getAdresse(), ersatzText);
    }

    public static String formatiereAdresse(Adresse adresse) {
        return formatiereAdresse(adresse, "-");
    }

    public static String formatiereAdresse(Adresse adresse, String ersatzText) {
        if (adresse == null) {
            return ersatzText;
        }

        String strasseUndHausnummer = (
                wertOderLeer(adresse.getStrasse()) + " " + wertOderLeer(adresse.getHausnummer())
        ).trim();

        String plzUndStadt = (
                wertOderLeer(adresse.getPlz()) + " " + wertOderLeer(adresse.getStadt())
        ).trim();

        if (strasseUndHausnummer.isBlank() && plzUndStadt.isBlank()) {
            return ersatzText;
        }

        if (strasseUndHausnummer.isBlank()) {
            return plzUndStadt;
        }

        if (plzUndStadt.isBlank()) {
            return strasseUndHausnummer;
        }

        return strasseUndHausnummer + ", " + plzUndStadt;
    }

    public static String formatiereAdresse(
            String strasse,
            String hausnummer,
            String plz,
            String ort,
            String ersatzText
    ) {
        String strasseUndHausnummer = (
                wertOderLeer(strasse) + " " + wertOderLeer(hausnummer)
        ).trim();

        String plzUndOrt = (
                wertOderLeer(plz) + " " + wertOderLeer(ort)
        ).trim();

        if (strasseUndHausnummer.isBlank() && plzUndOrt.isBlank()) {
            return ersatzText;
        }

        if (strasseUndHausnummer.isBlank()) {
            return plzUndOrt;
        }

        if (plzUndOrt.isBlank()) {
            return strasseUndHausnummer;
        }

        return strasseUndHausnummer + ", " + plzUndOrt;
    }

    public static String formatiereAdresseKurz(Adresse adresse) {
        return formatiereAdresseKurz(adresse, "Keine Adresse");
    }

    public static String formatiereAdresseKurz(Adresse adresse, String ersatzText) {
        if (adresse == null) {
            return ersatzText;
        }

        String plz = wertOderLeer(adresse.getPlz()).trim();
        String stadt = wertOderLeer(adresse.getStadt()).trim();

        String kurz = (plz + " " + stadt).trim();

        return kurz.isBlank() ? ersatzText : kurz;
    }

    // =========================================================
    // Immobilien
    // =========================================================

    public static String formatiereImmobilientyp(Immobilientyp typ) {
        return typ == null ? "Immobilie" : typ.getLabel();
    }

    public static String formatiereImmobilientyp(Immobilientyp typ, String ersatzText) {
        return typ == null ? ersatzText : typ.getLabel();
    }

    public static String formatiereImmobilienBezeichnung(Immobilie immobilie, String ersatzText) {
        if (immobilie == null || istLeer(immobilie.getBezeichnung())) {
            return ersatzText;
        }

        return immobilie.getBezeichnung().trim();
    }

    public static String formatiereImmobilienBezeichnung(Mietvertrag mietvertrag, String ersatzText) {
        if (mietvertrag == null || mietvertrag.getMieteinheit() == null) {
            return ersatzText;
        }

        return formatiereImmobilienBezeichnung(
                mietvertrag.getMieteinheit().getImmobilie(),
                ersatzText
        );
    }

    // =========================================================
    // Mieteinheiten
    // =========================================================

    public static String formatiereMieteinheitTyp(Mieteinheit mieteinheit) {
        if (mieteinheit == null) {
            return "-";
        }

        return formatiereMieteinheitTyp(mieteinheit.getTyp());
    }

    public static String formatiereMieteinheitTyp(MieteinheitTyp typ) {
        return typ == null ? "-" : typ.getLabel();
    }

    public static String formatiereMieteinheitStatus(Mieteinheitstatus status) {
        return status == null ? "-" : status.getLabel();
    }

    public static String formatiereMieteinheitBezeichnung(Mieteinheit mieteinheit, String ersatzText) {
        if (mieteinheit == null || istLeer(mieteinheit.getBezeichnung())) {
            return ersatzText;
        }

        return mieteinheit.getBezeichnung().trim();
    }

    public static String formatiereMieteinheitBezeichnung(Mietvertrag mietvertrag, String ersatzText) {
        if (mietvertrag == null || mietvertrag.getMieteinheit() == null) {
            return ersatzText;
        }

        return formatiereMieteinheitBezeichnung(mietvertrag.getMieteinheit(), ersatzText);
    }

    public static String formatiereMieteinheitDetails(Mieteinheit mieteinheit) {
        if (mieteinheit == null) {
            return "-";
        }

        String details = wertOderLeer(mieteinheit.getBezeichnung());

        if (details.isBlank()) {
            details = "-";
        }

        if (mieteinheit.getGroesse() != null) {
            details += " • " + mieteinheit.getGroesse() + " m²";
        }

        if (mieteinheit.getZimmerzahl() != null) {
            details += " • " + mieteinheit.getZimmerzahl() + " Zimmer";
        }

        if (mieteinheit.getStockwerk() != null && !mieteinheit.getStockwerk().isBlank()) {
            details += " • " + mieteinheit.getStockwerk();
        }

        return details;
    }

    public static String formatiereMieteinheitDetails(Mietvertrag mietvertrag) {
        if (mietvertrag == null) {
            return "-";
        }

        return formatiereMieteinheitDetails(mietvertrag.getMieteinheit());
    }

    public static String formatiereFlaeche(Integer flaeche) {
        return flaeche == null ? "-" : flaeche + " m²";
    }

    public static String formatiereZimmer(Integer zimmerzahl) {
        if (zimmerzahl == null) {
            return "Zimmer nicht angegeben";
        }

        return zimmerzahl + " Zimmer";
    }

    // =========================================================
    // Mieter
    // =========================================================

    public static String formatiereMieterName(Mieter mieter) {
        if (mieter == null) {
            return "-";
        }

        String vorname = wertOderLeer(mieter.getVorname());
        String nachname = wertOderLeer(mieter.getNachname());

        String name = (vorname + " " + nachname).trim();

        return name.isBlank() ? "Unbenannter Mieter" : name;
    }

    public static String formatiereMieterName(Mieter mieter, String ersatzText) {
        if (mieter == null) {
            return ersatzText;
        }

        String name = (
                wertOderLeer(mieter.getVorname()) + " " + wertOderLeer(mieter.getNachname())
        ).trim();

        return name.isBlank() ? ersatzText : name;
    }

    public static String formatiereMieterName(Mietvertrag mietvertrag) {
        if (mietvertrag == null) {
            return "-";
        }

        return formatiereMieterName(mietvertrag.getMieter());
    }

    public static String formatiereMieterMitEmail(Mieter mieter) {
        if (mieter == null) {
            return "-";
        }

        String name = formatiereMieterName(mieter, "-");
        String email = wertOderLeer(mieter.getEmail());

        return email.isBlank() ? name : name + " • " + email;
    }

    public static String erstelleInitialen(Mieter mieter) {
        if (mieter == null) {
            return "M";
        }

        return erstelleInitialen(mieter.getVorname(), mieter.getNachname(), "M");
    }

    public static String erstelleInitialen(String vorname, String nachname) {
        return erstelleInitialen(vorname, nachname, "");
    }

    private static String erstelleInitialen(String vorname, String nachname, String ersatzText) {
        StringBuilder initialen = new StringBuilder();

        String bereinigterVorname = wertOderLeer(vorname).trim();
        String bereinigterNachname = wertOderLeer(nachname).trim();

        if (!bereinigterVorname.isBlank()) {
            initialen.append(bereinigterVorname.charAt(0));
        }

        if (!bereinigterNachname.isBlank()) {
            initialen.append(bereinigterNachname.charAt(0));
        }

        String ergebnis = initialen.toString().toUpperCase(Locale.GERMANY);

        return ergebnis.isBlank() ? ersatzText : ergebnis;
    }

    // =========================================================
    // Mietverträge
    // =========================================================

    public static String formatiereVertragslaufzeit(Mietvertrag mietvertrag) {
        if (mietvertrag == null) {
            return "-";
        }

        return formatiereDatum(mietvertrag.getStartdatum())
                + " - "
                + formatiereVertragsende(mietvertrag);
    }

    public static String formatiereZeitraum(Mietvertrag mietvertrag) {
        if (mietvertrag == null) {
            return "-";
        }

        return formatiereDatum(mietvertrag.getStartdatum())
                + " - "
                + formatiereDatum(mietvertrag.getEnddatum());
    }

    public static String formatiereVertragsende(Mietvertrag mietvertrag) {
        if (mietvertrag == null || mietvertrag.getEnddatum() == null) {
            return "unbefristet";
        }

        return formatiereDatum(mietvertrag.getEnddatum());
    }

    public static String formatiereVertragsstatus(Mietvertrag mietvertrag) {
        if (mietvertrag == null || mietvertrag.getStatus() == null) {
            return "-";
        }

        return mietvertrag.getStatus().getLabel();
    }

    public static String formatiereWarmmiete(Mietvertrag mietvertrag) {
        if (mietvertrag == null) {
            return "-";
        }

        double kaltmiete = mietvertrag.getKaltmiete() == null ? 0 : mietvertrag.getKaltmiete();
        double nebenkosten = mietvertrag.getNebenkosten() == null ? 0 : mietvertrag.getNebenkosten();

        return formatiereEuro(kaltmiete + nebenkosten);
    }

    public static String formatiereMietobjekt(Mietvertrag mietvertrag) {
        if (mietvertrag == null || mietvertrag.getMieteinheit() == null) {
            return "-";
        }

        Mieteinheit mieteinheit = mietvertrag.getMieteinheit();

        if (mieteinheit.getImmobilie() == null) {
            return wertOderStrich(mieteinheit.getBezeichnung());
        }

        return wertOderStrich(mieteinheit.getImmobilie().getBezeichnung())
                + " / "
                + wertOderStrich(mieteinheit.getBezeichnung());
    }

    public static String formatiereMietvertragKurz(Mietvertrag mietvertrag) {
        if (mietvertrag == null) {
            return "-";
        }

        return "MV-" + mietvertrag.getId()
                + " - "
                + formatiereMieterName(mietvertrag.getMieter(), "Unbekannter Mieter")
                + " - "
                + formatiereMieteinheitBezeichnung(
                mietvertrag.getMieteinheit(),
                "Unbekannte Mieteinheit"
        );
    }

    public static String formatiereMietvertragAuswahl(Mietvertrag mietvertrag) {
        if (mietvertrag == null) {
            return "";
        }

        String mieterName;

        if (mietvertrag.getMieter() != null) {
            mieterName = (
                    wertOderLeer(mietvertrag.getMieter().getVorname())
                            + " "
                            + wertOderLeer(mietvertrag.getMieter().getNachname())
            ).trim();
        } else {
            mieterName = "Unbekannter Mieter";
        }

        String einheit = formatiereMieteinheitBezeichnung(
                mietvertrag.getMieteinheit(),
                "Unbekannte Mieteinheit"
        );

        return mieterName + " - " + einheit;
    }

    public static String formatiereMietvertragMitPunkten(Mietvertrag mietvertrag) {
        if (mietvertrag == null) {
            return "-";
        }

        return "MV-" + mietvertrag.getId()
                + " • "
                + formatiereMieterName(mietvertrag.getMieter(), "-")
                + " • "
                + formatiereMieteinheitBezeichnung(mietvertrag.getMieteinheit(), "-");
    }

    // =========================================================
    // Sensible Daten
    // =========================================================

    public static String maskiereIban(String iban) {
        if (iban == null || iban.isBlank()) {
            return "-";
        }

        String bereinigteIban = iban.replace(" ", "");

        if (bereinigteIban.length() <= 8) {
            return iban;
        }

        return bereinigteIban.substring(0, 4)
                + " •••• •••• "
                + bereinigteIban.substring(bereinigteIban.length() - 4);
    }
}