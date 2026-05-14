package de.hsbi.immobilienverwaltung.ui.mieter;

import java.util.List;
import java.util.Optional;

public final class MieterDummyDaten {

    private MieterDummyDaten() {
    }

    // Gibt alle Dummy-Mieter zurück
    public static List<MieterRow> getMieter() {
        return List.of(
                new MieterRow(
                        "TEN-2023-001",
                        "Max Mustermann",
                        "Herr Max Mustermann",
                        "12.02.1985 (39 Jahre)",
                        "Deutsch",
                        "Softwareentwickler bei CodeWorks GmbH",
                        "Positiv (Schufa)",
                        "success",
                        "Geprüft am 15.03.2023",
                        "max@example.com",
                        "+49 151 1234567",
                        "Musterstraße 12\n33602 Bielefeld",
                        "Parkresidenz Süd / WE-01",
                        "Seit 01.10.2021",
                        "1.400,00 €",
                        "Aktiv",
                        "success"
                ),
                new MieterRow(
                        "TEN-2023-089",
                        "Sarah Müller",
                        "Frau Sarah Katharina Müller",
                        "14.05.1988 (35 Jahre)",
                        "Deutsch",
                        "Marketing Managerin bei TechCorp GmbH",
                        "Positiv (Schufa)",
                        "success",
                        "Geprüft am 10.04.2023",
                        "sarah.m@example.com",
                        "+49 170 1234567",
                        "Goethestraße 15\n10625 Berlin",
                        "Stadthaus Mitte / WE-12",
                        "Seit 15.03.2023",
                        "1.250,00 €",
                        "Aktiv",
                        "success"
                ),
                new MieterRow(
                        "TEN-2023-105",
                        "Julia Müller",
                        "Frau Julia Müller",
                        "03.11.1992 (31 Jahre)",
                        "Deutsch",
                        "Projektmanagerin bei MediaPlan GmbH",
                        "Positiv (Schufa)",
                        "success",
                        "Geprüft am 22.06.2023",
                        "julia.m@web.de",
                        "+49 160 5555555",
                        "Kaiserstraße 8\n44135 Dortmund",
                        "Parkresidenz Süd / WE-05",
                        "01.01.2020 - 31.12.2023",
                        "1.850,00 €",
                        "Gekündigt",
                        "warning"
                ),
                new MieterRow(
                        "TEN-2044-007",
                        "Julian Stein",
                        "Herr Julian Stein",
                        "27.08.2004 (40 Jahre)",
                        "Deutsch",
                        "Unternehmer bei Stein Holding GmbH",
                        "Positiv (Schufa)",
                        "success",
                        "Geprüft am 27.08.2044",
                        "julian@example.com",
                        "+49 176 9999999",
                        "Manhattan North Tower\n10001 New York",
                        "Manhattan North / WE-07",
                        "Seit 27.08.2044",
                        "7.990,00 €",
                        "Aktiv",
                        "success"
                )
        );
    }

    public static Optional<MieterRow> findMieterById(String id) {
        return getMieter()
                .stream()
                .filter(mieter -> mieter.id().equals(id))
                .findFirst();
    }

    public static List<MietvertragRow> getMietvertraege() {
        return List.of(
                new MietvertragRow(
                        "MV-2023-042",
                        "TEN-2023-001",
                        "Max Mustermann",
                        "Parkresidenz Süd / WE-01",
                        "01.10.2021 - unbefristet",
                        "1.400,00 €",
                        "Aktiv",
                        "success"
                ),
                new MietvertragRow(
                        "MV-2023-089",
                        "TEN-2023-089",
                        "Sarah Schmidt",
                        "Stadthaus Mitte / WE-12",
                        "15.03.2023 - unbefristet",
                        "950,00 €",
                        "Zahlungsverzug",
                        "danger"
                ),
                new MietvertragRow(
                        "MV-2023-105",
                        "TEN-2023-105",
                        "Julia Müller",
                        "Parkresidenz Süd / WE-05",
                        "01.01.2020 - 31.12.2023",
                        "1.850,00 €",
                        "Gekündigt",
                        "warning"
                )
        );
    }

    // Gibt die letzten Buchungen für einen bestimmten Mieter zurück
    public static List<BuchungRow> getBuchungenByMieterId(String mieterId) {
        return List.of(
                        new BuchungRow(
                                "TEN-2023-089",
                                "01.10.2023",
                                "Miete Oktober 2023",
                                "MV-2023-042",
                                "1.250,00 €",
                                "Bezahlt",
                                "success"
                        ),
                        new BuchungRow(
                                "TEN-2023-089",
                                "01.09.2023",
                                "Miete September 2023",
                                "MV-2023-042",
                                "1.250,00 €",
                                "Bezahlt",
                                "success"
                        ),
                        new BuchungRow(
                                "TEN-2023-089",
                                "15.08.2023",
                                "Nebenkostenabrechnung 2022",
                                "MV-2021-105",
                                "145,50 €",
                                "Ausstehend",
                                "warning"
                        ),
                        new BuchungRow(
                                "TEN-2023-089",
                                "1.11.2023",
                                "Miete November 2023",
                                "MV-2023-042",
                                "1.250,00 €",
                                "Bezahlt",
                                "success"
                        )
                ).stream()
                .filter(buchung -> buchung.mieterId().equals(mieterId))
                .toList();
    }
    // Gibt die Notizen für einen bestimmten Mieter zurück
    public static List<NotizRow> getNotizenByMieterId(String mieterId) {
        return List.of(
                        new NotizRow(
                                "TEN-2023-089",
                                "Haustiere",
                                "Haltung einer Hauskatze wurde vertraglich genehmigt.",
                                "12.04.2023",
                                "warning"
                        ),
                        new NotizRow(
                                "TEN-2023-089",
                                "Kaution",
                                "Kaution wurde vollständig in bar hinterlegt.",
                                "01.05.2023",
                                "neutral"
                        ),
                        new NotizRow(
                                "TEN-2023-105",
                                "Auszug",
                                "Mieterin zieht zum Vertragsende aus.",
                                "15.11.2023",
                                "warning"
                        )
                ).stream()
                .filter(notiz -> notiz.mieterId().equals(mieterId))
                .toList();
    }

    // Datenstruktur für einen Mieter
    public record MieterRow(
            String id,
            String name,
            String vollstaendigerName,
            String geburtsdatum,
            String nationalitaet,
            String berufArbeitgeber,
            String bonitaet,
            String bonitaetStyle,
            String bonitaetPruefdatum,
            String email,
            String telefon,
            String meldeadresse,
            String immobilieEinheit,
            String vertragsdauer,
            String miete,
            String status,
            String statusStyle
    ) {
    }

    public record MietvertragRow(
            String id,
            String mieterId,
            String mieter,
            String immobilieEinheit,
            String laufzeit,
            String miete,
            String status,
            String statusStyle
    ) {
    }

    // Datenstruktur für eine Buchung
    public record BuchungRow(
            String mieterId,
            String datum,
            String verwendungszweck,
            String vertrag,
            String betrag,
            String status,
            String statusStyle
    ) {
    }

    // Datenstruktur für eine Notiz
    public record NotizRow(
            String mieterId,
            String titel,
            String text,
            String datum,
            String statusStyle
    ) {
    }
}