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
                        "TEN-2024-014",
                        "Lukas Becker",
                        "Herr Lukas Becker",
                        "21.08.1994 (29 Jahre)",
                        "Deutsch",
                        "Controller bei FinancePoint AG",
                        "Positiv (Schufa)",
                        "success",
                        "Geprüft am 02.01.2024",
                        "lukas.becker@example.com",
                        "+49 172 4455667",
                        "Ringstraße 22\n33602 Bielefeld",
                        "Stadthaus Mitte / WE-03",
                        "Seit 01.02.2024",
                        "1.080,00 €",
                        "Aktiv",
                        "success"
                ),
                new MieterRow(
                        "TEN-2024-018",
                        "Aylin Yildiz",
                        "Frau Aylin Yildiz",
                        "09.01.1990 (34 Jahre)",
                        "Deutsch",
                        "HR-Managerin bei PeopleFirst GmbH",
                        "Positiv (Schufa)",
                        "success",
                        "Geprüft am 16.01.2024",
                        "aylin.yildiz@example.com",
                        "+49 176 7788990",
                        "Bahnhofstraße 7\n33602 Bielefeld",
                        "Parkresidenz Süd / WE-08",
                        "Seit 01.03.2024",
                        "1.620,00 €",
                        "Aktiv",
                        "success"
                ),
                new MieterRow(
                        "TEN-2024-021",
                        "Jonas Weber",
                        "Herr Jonas Weber",
                        "30.06.1997 (26 Jahre)",
                        "Deutsch",
                        "Student / Werkstudent bei IT Solutions OWL",
                        "Positiv (Schufa)",
                        "success",
                        "Geprüft am 22.02.2024",
                        "jonas.weber@example.com",
                        "+49 152 9988776",
                        "Uniweg 4\n33615 Bielefeld",
                        "Campus Apartments / WE-21",
                        "Seit 15.03.2024",
                        "690,00 €",
                        "Aktiv",
                        "success"
                ),
                new MieterRow(
                        "TEN-2024-026",
                        "Nina Hoffmann",
                        "Frau Nina Hoffmann",
                        "18.04.1982 (42 Jahre)",
                        "Deutsch",
                        "Lehrerin am Gymnasium Nord",
                        "Positiv (Schufa)",
                        "success",
                        "Geprüft am 05.03.2024",
                        "nina.hoffmann@example.com",
                        "+49 151 2223344",
                        "Schulstraße 18\n33719 Bielefeld",
                        "Wohnpark Nord / WE-11",
                        "Seit 01.04.2024",
                        "1.310,00 €",
                        "Aktiv",
                        "success"
                ),
                new MieterRow(
                        "TEN-2024-033",
                        "Tom Schneider",
                        "Herr Tom Schneider",
                        "05.12.1989 (34 Jahre)",
                        "Deutsch",
                        "Selbstständiger Grafikdesigner",
                        "Nachprüfung empfohlen",
                        "warning",
                        "Geprüft am 18.03.2024",
                        "tom.schneider@example.com",
                        "+49 157 1122334",
                        "Kreativstraße 3\n33609 Bielefeld",
                        "Altbau Rosenstraße / WE-02",
                        "Seit 01.05.2024",
                        "980,00 €",
                        "Zahlungsverzug",
                        "danger"
                ),
                new MieterRow(
                        "TEN-2024-041",
                        "Mira Klein",
                        "Frau Mira Klein",
                        "27.09.1995 (28 Jahre)",
                        "Deutsch",
                        "Pflegefachkraft im Klinikum Mitte",
                        "Positiv (Schufa)",
                        "success",
                        "Geprüft am 28.04.2024",
                        "mira.klein@example.com",
                        "+49 163 5556677",
                        "Klinikallee 9\n33604 Bielefeld",
                        "Wohnpark Nord / WE-14",
                        "Seit 01.06.2024",
                        "1.190,00 €",
                        "Aktiv",
                        "success"
                ),
                new MieterRow(
                        "TEN-2024-052",
                        "Daniel Fischer",
                        "Herr Daniel Fischer",
                        "11.11.1978 (45 Jahre)",
                        "Deutsch",
                        "Vertriebsleiter bei Möbelhaus West",
                        "Positiv (Schufa)",
                        "success",
                        "Geprüft am 14.05.2024",
                        "daniel.fischer@example.com",
                        "+49 170 4443322",
                        "Weststraße 44\n33613 Bielefeld",
                        "Stadthaus Mitte / WE-09",
                        "Seit 01.07.2024",
                        "1.470,00 €",
                        "Aktiv",
                        "success"
                ),
                new MieterRow(
                        "TEN-2024-063",
                        "Lea Wagner",
                        "Frau Lea Wagner",
                        "02.03.1999 (25 Jahre)",
                        "Deutsch",
                        "Auszubildende bei Stadtwerke Bielefeld",
                        "Positiv (Schufa)",
                        "success",
                        "Geprüft am 20.06.2024",
                        "lea.wagner@example.com",
                        "+49 176 3332211",
                        "Azubiweg 6\n33607 Bielefeld",
                        "Campus Apartments / WE-17",
                        "Seit 01.08.2024",
                        "720,00 €",
                        "Aktiv",
                        "success"
                ),
                new MieterRow(
                        "TEN-2024-074",
                        "Omar Haddad",
                        "Herr Omar Haddad",
                        "19.07.1987 (36 Jahre)",
                        "Deutsch",
                        "Ingenieur bei Maschinenbau Ost GmbH",
                        "Positiv (Schufa)",
                        "success",
                        "Geprüft am 04.07.2024",
                        "omar.haddad@example.com",
                        "+49 159 7776655",
                        "Industrieweg 12\n33649 Bielefeld",
                        "Gewerbehof Süd / Einheit G-02",
                        "Seit 01.09.2024",
                        "2.200,00 €",
                        "Aktiv",
                        "success"
                ),
                new MieterRow(
                        "TEN-2024-081",
                        "Clara Neumann",
                        "Frau Clara Neumann",
                        "25.05.1991 (33 Jahre)",
                        "Deutsch",
                        "Architektin bei Bauform GmbH",
                        "Positiv (Schufa)",
                        "success",
                        "Geprüft am 18.08.2024",
                        "clara.neumann@example.com",
                        "+49 171 9090900",
                        "Planerstraße 5\n33602 Bielefeld",
                        "Altbau Rosenstraße / WE-07",
                        "Seit 01.10.2024",
                        "1.120,00 €",
                        "Aktiv",
                        "success"
                ),
                new MieterRow(
                        "TEN-2024-092",
                        "Felix Braun",
                        "Herr Felix Braun",
                        "08.10.1984 (39 Jahre)",
                        "Deutsch",
                        "Elektriker bei Elektro Braun",
                        "Positiv (Schufa)",
                        "success",
                        "Geprüft am 02.09.2024",
                        "felix.braun@example.com",
                        "+49 160 1112233",
                        "Handwerkerweg 21\n33659 Bielefeld",
                        "Wohnpark Nord / WE-03",
                        "Seit 01.11.2024",
                        "1.050,00 €",
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

    // Sucht einen Mieter über seine ID
    public static Optional<MieterRow> findMieterById(String id) {
        return getMieter()
                .stream()
                .filter(mieter -> mieter.id().equals(id))
                .findFirst();
    }

    // Gibt alle Dummy-Mietverträge zurück
    public static List<MietvertragRow> getMietvertraege() {
        return List.of(
                new MietvertragRow(
                        "MV-2023-042",
                        "TEN-2023-001",
                        "Max Mustermann",
                        "max@example.com",
                        "Parkresidenz Süd / WE-01",
                        "Bielefeld - Parkresidenz Süd, WE 01",
                        "3 Zimmer • 85 m² • 1. OG links",
                        "01.10.2021 - unbefristet",
                        "1.400,00 €",
                        "Aktiv",
                        "success",
                        "01.10.2021",
                        "",
                        "Gesetzlich (3 Monate)",
                        "01.10.2022",
                        "",
                        "1.050,00 €",
                        "220,00 €",
                        "130,00 €",
                        "1.400,00 €",
                        "Barkaution / Überweisung",
                        "2.100,00 €",
                        "2.100,00 €",
                        "Vollständig bezahlt",
                        "success"
                ),
                new MietvertragRow(
                        "MV-2023-084",
                        "TEN-2023-089",
                        "Sarah Müller",
                        "sarah.m@example.com",
                        "Berlin - Hauptstraße 1, WE 04",
                        "Berlin - Hauptstraße 1, WE 04",
                        "3 Zimmer • 85 m² • 2. OG rechts",
                        "01.05.2022 - unbefristet",
                        "1.250,00 €",
                        "Aktiv",
                        "success",
                        "01.05.2022",
                        "",
                        "Gesetzlich (3 Monate)",
                        "30.04.2023",
                        "",
                        "950,00 €",
                        "180,00 €",
                        "120,00 €",
                        "1.250,00 €",
                        "Barkaution / Überweisung",
                        "2.850,00 €",
                        "2.850,00 €",
                        "Vollständig bezahlt",
                        "success"
                ),
                new MietvertragRow(
                        "MV-2023-105",
                        "TEN-2023-105",
                        "Julia Müller",
                        "julia.m@web.de",
                        "Parkresidenz Süd / WE-05",
                        "Parkresidenz Süd, WE 05",
                        "4 Zimmer • 110 m² • 3. OG",
                        "01.01.2020 - 31.12.2023",
                        "1.850,00 €",
                        "Gekündigt",
                        "warning",
                        "01.01.2020",
                        "31.12.2023",
                        "Gesetzlich (3 Monate)",
                        "01.01.2021",
                        "30.09.2023",
                        "1.450,00 €",
                        "250,00 €",
                        "150,00 €",
                        "1.850,00 €",
                        "Barkaution / Überweisung",
                        "4.350,00 €",
                        "4.350,00 €",
                        "Vollständig bezahlt",
                        "success"
                ),
                new MietvertragRow(
                        "MV-2024-014",
                        "TEN-2024-014",
                        "Lukas Becker",
                        "lukas.becker@example.com",
                        "Stadthaus Mitte / WE-03",
                        "Bielefeld - Stadthaus Mitte, WE 03",
                        "2 Zimmer • 62 m² • 1. OG",
                        "01.02.2024 - unbefristet",
                        "1.080,00 €",
                        "Aktiv",
                        "success",
                        "01.02.2024",
                        "",
                        "Gesetzlich (3 Monate)",
                        "01.02.2025",
                        "",
                        "820,00 €",
                        "160,00 €",
                        "100,00 €",
                        "1.080,00 €",
                        "Barkaution / Überweisung",
                        "2.460,00 €",
                        "1.640,00 €",
                        "Teilweise bezahlt",
                        "warning"
                ),
                new MietvertragRow(
                        "MV-2024-018",
                        "TEN-2024-018",
                        "Aylin Yildiz",
                        "aylin.yildiz@example.com",
                        "Parkresidenz Süd / WE-08",
                        "Bielefeld - Parkresidenz Süd, WE 08",
                        "4 Zimmer • 101 m² • 2. OG rechts",
                        "01.03.2024 - unbefristet",
                        "1.620,00 €",
                        "Aktiv",
                        "success",
                        "01.03.2024",
                        "",
                        "Gesetzlich (3 Monate)",
                        "01.03.2025",
                        "",
                        "1.260,00 €",
                        "220,00 €",
                        "140,00 €",
                        "1.620,00 €",
                        "Bankbürgschaft",
                        "3.780,00 €",
                        "3.780,00 €",
                        "Vollständig bezahlt",
                        "success"
                ),
                new MietvertragRow(
                        "MV-2024-021",
                        "TEN-2024-021",
                        "Jonas Weber",
                        "jonas.weber@example.com",
                        "Campus Apartments / WE-21",
                        "Bielefeld - Campus Apartments, WE 21",
                        "1 Zimmer • 28 m² • 4. OG",
                        "15.03.2024 - 14.03.2026",
                        "690,00 €",
                        "Aktiv",
                        "success",
                        "15.03.2024",
                        "14.03.2026",
                        "1 Monat",
                        "15.03.2025",
                        "",
                        "520,00 €",
                        "100,00 €",
                        "70,00 €",
                        "690,00 €",
                        "Barkaution / Überweisung",
                        "1.560,00 €",
                        "1.560,00 €",
                        "Vollständig bezahlt",
                        "success"
                ),
                new MietvertragRow(
                        "MV-2024-026",
                        "TEN-2024-026",
                        "Nina Hoffmann",
                        "nina.hoffmann@example.com",
                        "Wohnpark Nord / WE-11",
                        "Bielefeld - Wohnpark Nord, WE 11",
                        "3 Zimmer • 78 m² • EG rechts",
                        "01.04.2024 - unbefristet",
                        "1.310,00 €",
                        "Aktiv",
                        "success",
                        "01.04.2024",
                        "",
                        "Gesetzlich (3 Monate)",
                        "01.04.2025",
                        "",
                        "990,00 €",
                        "190,00 €",
                        "130,00 €",
                        "1.310,00 €",
                        "Barkaution / Überweisung",
                        "2.970,00 €",
                        "2.970,00 €",
                        "Vollständig bezahlt",
                        "success"
                ),
                new MietvertragRow(
                        "MV-2024-033",
                        "TEN-2024-033",
                        "Tom Schneider",
                        "tom.schneider@example.com",
                        "Altbau Rosenstraße / WE-02",
                        "Bielefeld - Altbau Rosenstraße, WE 02",
                        "2 Zimmer • 55 m² • DG",
                        "01.05.2024 - unbefristet",
                        "980,00 €",
                        "Zahlungsverzug",
                        "danger",
                        "01.05.2024",
                        "",
                        "Gesetzlich (3 Monate)",
                        "01.05.2025",
                        "",
                        "760,00 €",
                        "130,00 €",
                        "90,00 €",
                        "980,00 €",
                        "Barkaution / Überweisung",
                        "2.280,00 €",
                        "760,00 €",
                        "Teilweise bezahlt",
                        "warning"
                ),
                new MietvertragRow(
                        "MV-2024-041",
                        "TEN-2024-041",
                        "Mira Klein",
                        "mira.klein@example.com",
                        "Wohnpark Nord / WE-14",
                        "Bielefeld - Wohnpark Nord, WE 14",
                        "2 Zimmer • 68 m² • 2. OG",
                        "01.06.2024 - unbefristet",
                        "1.190,00 €",
                        "Aktiv",
                        "success",
                        "01.06.2024",
                        "",
                        "Gesetzlich (3 Monate)",
                        "01.06.2025",
                        "",
                        "890,00 €",
                        "180,00 €",
                        "120,00 €",
                        "1.190,00 €",
                        "Barkaution / Überweisung",
                        "2.670,00 €",
                        "2.670,00 €",
                        "Vollständig bezahlt",
                        "success"
                ),
                new MietvertragRow(
                        "MV-2024-052",
                        "TEN-2024-052",
                        "Daniel Fischer",
                        "daniel.fischer@example.com",
                        "Stadthaus Mitte / WE-09",
                        "Bielefeld - Stadthaus Mitte, WE 09",
                        "3 Zimmer • 88 m² • 3. OG links",
                        "01.07.2024 - unbefristet",
                        "1.470,00 €",
                        "Aktiv",
                        "success",
                        "01.07.2024",
                        "",
                        "Gesetzlich (3 Monate)",
                        "01.07.2025",
                        "",
                        "1.120,00 €",
                        "210,00 €",
                        "140,00 €",
                        "1.470,00 €",
                        "Bankbürgschaft",
                        "3.360,00 €",
                        "3.360,00 €",
                        "Vollständig bezahlt",
                        "success"
                ),
                new MietvertragRow(
                        "MV-2024-063",
                        "TEN-2024-063",
                        "Lea Wagner",
                        "lea.wagner@example.com",
                        "Campus Apartments / WE-17",
                        "Bielefeld - Campus Apartments, WE 17",
                        "1 Zimmer • 31 m² • 2. OG",
                        "01.08.2024 - 31.07.2026",
                        "720,00 €",
                        "Aktiv",
                        "success",
                        "01.08.2024",
                        "31.07.2026",
                        "1 Monat",
                        "01.08.2025",
                        "",
                        "540,00 €",
                        "105,00 €",
                        "75,00 €",
                        "720,00 €",
                        "Barkaution / Überweisung",
                        "1.620,00 €",
                        "1.080,00 €",
                        "Teilweise bezahlt",
                        "warning"
                ),
                new MietvertragRow(
                        "MV-2024-074",
                        "TEN-2024-074",
                        "Omar Haddad",
                        "omar.haddad@example.com",
                        "Gewerbehof Süd / Einheit G-02",
                        "Bielefeld - Gewerbehof Süd, Einheit G-02",
                        "Gewerbeeinheit • 140 m² • Erdgeschoss",
                        "01.09.2024 - 31.08.2027",
                        "2.200,00 €",
                        "Aktiv",
                        "success",
                        "01.09.2024",
                        "31.08.2027",
                        "6 Monate",
                        "01.09.2025",
                        "",
                        "1.650,00 €",
                        "350,00 €",
                        "200,00 €",
                        "2.200,00 €",
                        "Bankbürgschaft",
                        "4.950,00 €",
                        "4.950,00 €",
                        "Vollständig bezahlt",
                        "success"
                ),
                new MietvertragRow(
                        "MV-2024-081",
                        "TEN-2024-081",
                        "Clara Neumann",
                        "clara.neumann@example.com",
                        "Altbau Rosenstraße / WE-07",
                        "Bielefeld - Altbau Rosenstraße, WE 07",
                        "2 Zimmer • 64 m² • 1. OG",
                        "01.10.2024 - unbefristet",
                        "1.120,00 €",
                        "Aktiv",
                        "success",
                        "01.10.2024",
                        "",
                        "Gesetzlich (3 Monate)",
                        "01.10.2025",
                        "",
                        "840,00 €",
                        "170,00 €",
                        "110,00 €",
                        "1.120,00 €",
                        "Barkaution / Überweisung",
                        "2.520,00 €",
                        "2.520,00 €",
                        "Vollständig bezahlt",
                        "success"
                ),
                new MietvertragRow(
                        "MV-2024-092",
                        "TEN-2024-092",
                        "Felix Braun",
                        "felix.braun@example.com",
                        "Wohnpark Nord / WE-03",
                        "Bielefeld - Wohnpark Nord, WE 03",
                        "2 Zimmer • 59 m² • EG links",
                        "01.11.2024 - 31.05.2025",
                        "1.050,00 €",
                        "Gekündigt",
                        "warning",
                        "01.11.2024",
                        "31.05.2025",
                        "Gesetzlich (3 Monate)",
                        "01.11.2025",
                        "28.02.2025",
                        "790,00 €",
                        "160,00 €",
                        "100,00 €",
                        "1.050,00 €",
                        "Barkaution / Überweisung",
                        "2.370,00 €",
                        "2.370,00 €",
                        "Vollständig bezahlt",
                        "success"
                ),
                new MietvertragRow(
                        "MV-2044-007",
                        "TEN-2044-007",
                        "Julian Stein",
                        "julian@example.com",
                        "Manhattan North / WE-07",
                        "New York - Manhattan North Tower, WE 07",
                        "5 Zimmer • 220 m² • 41. Etage",
                        "27.08.2044 - unbefristet",
                        "7.990,00 €",
                        "Aktiv",
                        "success",
                        "27.08.2044",
                        "",
                        "Individuell",
                        "27.08.2045",
                        "",
                        "6.500,00 €",
                        "890,00 €",
                        "600,00 €",
                        "7.990,00 €",
                        "Bankbürgschaft",
                        "19.500,00 €",
                        "19.500,00 €",
                        "Vollständig bezahlt",
                        "success"
                )
        );
    }

    // Sucht einen Mietvertrag über seine ID
    public static Optional<MietvertragRow> findMietvertragById(String id) {
        return getMietvertraege()
                .stream()
                .filter(mietvertrag -> mietvertrag.id().equals(id))
                .findFirst();
    }

    // Gibt alle Mietverträge für einen bestimmten Mieter zurück
    public static List<MietvertragRow> getMietvertraegeByMieterId(String mieterId) {
        return getMietvertraege()
                .stream()
                .filter(mietvertrag -> mietvertrag.mieterId().equals(mieterId))
                .toList();
    }

    // Gibt die letzten Buchungen für einen bestimmten Mieter zurück
    public static List<BuchungRow> getBuchungenByMieterId(String mieterId) {
        return List.of(
                        new BuchungRow("TEN-2023-001", "01.02.2025", "Miete Februar 2025", "MV-2023-042", "1.400,00 €", "Bezahlt", "success"),
                        new BuchungRow("TEN-2023-001", "01.01.2025", "Miete Januar 2025", "MV-2023-042", "1.400,00 €", "Bezahlt", "success"),
                        new BuchungRow("TEN-2023-001", "01.12.2024", "Miete Dezember 2024", "MV-2023-042", "1.400,00 €", "Bezahlt", "success"),
                        new BuchungRow("TEN-2023-001", "15.11.2024", "Nebenkostennachzahlung", "MV-2023-042", "185,40 €", "Bezahlt", "success"),

                        new BuchungRow("TEN-2023-089", "01.02.2025", "Miete Februar 2025", "MV-2023-084", "1.250,00 €", "Bezahlt", "success"),
                        new BuchungRow("TEN-2023-089", "01.01.2025", "Miete Januar 2025", "MV-2023-084", "1.250,00 €", "Bezahlt", "success"),
                        new BuchungRow("TEN-2023-089", "01.12.2024", "Miete Dezember 2024", "MV-2023-084", "1.250,00 €", "Bezahlt", "success"),
                        new BuchungRow("TEN-2023-089", "15.08.2024", "Nebenkostenabrechnung 2023", "MV-2023-084", "145,50 €", "Ausstehend", "warning"),

                        new BuchungRow("TEN-2023-105", "01.12.2023", "Miete Dezember 2023", "MV-2023-105", "1.850,00 €", "Bezahlt", "success"),
                        new BuchungRow("TEN-2023-105", "01.11.2023", "Miete November 2023", "MV-2023-105", "1.850,00 €", "Bezahlt", "success"),
                        new BuchungRow("TEN-2023-105", "01.10.2023", "Miete Oktober 2023", "MV-2023-105", "1.850,00 €", "Bezahlt", "success"),

                        new BuchungRow("TEN-2024-014", "01.02.2025", "Miete Februar 2025", "MV-2024-014", "1.080,00 €", "Bezahlt", "success"),
                        new BuchungRow("TEN-2024-014", "01.01.2025", "Miete Januar 2025", "MV-2024-014", "1.080,00 €", "Bezahlt", "success"),
                        new BuchungRow("TEN-2024-014", "01.12.2024", "Miete Dezember 2024", "MV-2024-014", "1.080,00 €", "Bezahlt", "success"),
                        new BuchungRow("TEN-2024-014", "01.11.2024", "Miete November 2024", "MV-2024-014", "1.080,00 €", "Offen", "warning"),

                        new BuchungRow("TEN-2024-018", "01.02.2025", "Miete Februar 2025", "MV-2024-018", "1.620,00 €", "Bezahlt", "success"),
                        new BuchungRow("TEN-2024-018", "01.01.2025", "Miete Januar 2025", "MV-2024-018", "1.620,00 €", "Bezahlt", "success"),
                        new BuchungRow("TEN-2024-018", "01.12.2024", "Miete Dezember 2024", "MV-2024-018", "1.620,00 €", "Bezahlt", "success"),

                        new BuchungRow("TEN-2024-021", "01.02.2025", "Miete Februar 2025", "MV-2024-021", "690,00 €", "Bezahlt", "success"),
                        new BuchungRow("TEN-2024-021", "01.01.2025", "Miete Januar 2025", "MV-2024-021", "690,00 €", "Bezahlt", "success"),
                        new BuchungRow("TEN-2024-021", "01.12.2024", "Miete Dezember 2024", "MV-2024-021", "690,00 €", "Bezahlt", "success"),

                        new BuchungRow("TEN-2024-026", "01.02.2025", "Miete Februar 2025", "MV-2024-026", "1.310,00 €", "Bezahlt", "success"),
                        new BuchungRow("TEN-2024-026", "01.01.2025", "Miete Januar 2025", "MV-2024-026", "1.310,00 €", "Bezahlt", "success"),
                        new BuchungRow("TEN-2024-026", "18.12.2024", "Reparaturbeteiligung Badarmatur", "MV-2024-026", "45,00 €", "Bezahlt", "success"),

                        new BuchungRow("TEN-2024-033", "01.02.2025", "Miete Februar 2025", "MV-2024-033", "980,00 €", "Überfällig", "danger"),
                        new BuchungRow("TEN-2024-033", "01.01.2025", "Miete Januar 2025", "MV-2024-033", "980,00 €", "Überfällig", "danger"),
                        new BuchungRow("TEN-2024-033", "01.12.2024", "Miete Dezember 2024", "MV-2024-033", "980,00 €", "Teilzahlung", "warning"),

                        new BuchungRow("TEN-2024-041", "01.02.2025", "Miete Februar 2025", "MV-2024-041", "1.190,00 €", "Bezahlt", "success"),
                        new BuchungRow("TEN-2024-041", "01.01.2025", "Miete Januar 2025", "MV-2024-041", "1.190,00 €", "Bezahlt", "success"),
                        new BuchungRow("TEN-2024-041", "01.12.2024", "Miete Dezember 2024", "MV-2024-041", "1.190,00 €", "Bezahlt", "success"),

                        new BuchungRow("TEN-2024-052", "01.02.2025", "Miete Februar 2025", "MV-2024-052", "1.470,00 €", "Bezahlt", "success"),
                        new BuchungRow("TEN-2024-052", "01.01.2025", "Miete Januar 2025", "MV-2024-052", "1.470,00 €", "Bezahlt", "success"),
                        new BuchungRow("TEN-2024-052", "20.12.2024", "Nachzahlung Heizkosten", "MV-2024-052", "210,30 €", "Ausstehend", "warning"),

                        new BuchungRow("TEN-2024-063", "01.02.2025", "Miete Februar 2025", "MV-2024-063", "720,00 €", "Bezahlt", "success"),
                        new BuchungRow("TEN-2024-063", "01.01.2025", "Miete Januar 2025", "MV-2024-063", "720,00 €", "Bezahlt", "success"),
                        new BuchungRow("TEN-2024-063", "01.12.2024", "Miete Dezember 2024", "MV-2024-063", "720,00 €", "Bezahlt", "success"),

                        new BuchungRow("TEN-2024-074", "01.02.2025", "Gewerbemiete Februar 2025", "MV-2024-074", "2.200,00 €", "Bezahlt", "success"),
                        new BuchungRow("TEN-2024-074", "01.01.2025", "Gewerbemiete Januar 2025", "MV-2024-074", "2.200,00 €", "Bezahlt", "success"),
                        new BuchungRow("TEN-2024-074", "01.12.2024", "Gewerbemiete Dezember 2024", "MV-2024-074", "2.200,00 €", "Bezahlt", "success"),

                        new BuchungRow("TEN-2024-081", "01.02.2025", "Miete Februar 2025", "MV-2024-081", "1.120,00 €", "Bezahlt", "success"),
                        new BuchungRow("TEN-2024-081", "01.01.2025", "Miete Januar 2025", "MV-2024-081", "1.120,00 €", "Bezahlt", "success"),
                        new BuchungRow("TEN-2024-081", "01.12.2024", "Miete Dezember 2024", "MV-2024-081", "1.120,00 €", "Bezahlt", "success"),

                        new BuchungRow("TEN-2024-092", "01.05.2025", "Miete Mai 2025", "MV-2024-092", "1.050,00 €", "Bezahlt", "success"),
                        new BuchungRow("TEN-2024-092", "01.04.2025", "Miete April 2025", "MV-2024-092", "1.050,00 €", "Bezahlt", "success"),
                        new BuchungRow("TEN-2024-092", "01.03.2025", "Miete März 2025", "MV-2024-092", "1.050,00 €", "Bezahlt", "success"),

                        new BuchungRow("TEN-2044-007", "01.10.2044", "Miete Oktober 2044", "MV-2044-007", "7.990,00 €", "Bezahlt", "success"),
                        new BuchungRow("TEN-2044-007", "01.09.2044", "Miete September 2044", "MV-2044-007", "7.990,00 €", "Bezahlt", "success")
                ).stream()
                .filter(buchung -> buchung.mieterId().equals(mieterId))
                .toList();
    }

    // Gibt die Notizen für einen bestimmten Mieter zurück
    public static List<NotizRow> getNotizenByMieterId(String mieterId) {
        return List.of(
                        new NotizRow("TEN-2023-001", "Kontakt", "Mieter bevorzugt Kommunikation per E-Mail.", "15.03.2023", "neutral"),
                        new NotizRow("TEN-2023-001", "Wartung", "Heizkörper im Wohnzimmer wurde 2024 entlüftet.", "02.11.2024", "neutral"),

                        new NotizRow("TEN-2023-089", "Haustiere", "Haltung einer Hauskatze wurde vertraglich genehmigt.", "12.04.2023", "warning"),
                        new NotizRow("TEN-2023-089", "Kaution", "Kaution wurde vollständig per Überweisung hinterlegt.", "01.05.2023", "neutral"),
                        new NotizRow("TEN-2023-089", "Kontakt", "Mieterin antwortet zuverlässig innerhalb von 1-2 Tagen.", "18.09.2024", "neutral"),

                        new NotizRow("TEN-2023-105", "Auszug", "Mieterin zieht zum Vertragsende aus.", "15.11.2023", "warning"),
                        new NotizRow("TEN-2023-105", "Wohnungsübergabe", "Übergabetermin muss noch final bestätigt werden.", "01.12.2023", "warning"),

                        new NotizRow("TEN-2024-014", "Kaution", "Zweite Kautionsrate noch offen.", "05.02.2024", "warning"),
                        new NotizRow("TEN-2024-014", "Kontakt", "Telefonisch am besten ab 17 Uhr erreichbar.", "14.03.2024", "neutral"),

                        new NotizRow("TEN-2024-018", "Familie", "Zieht mit Partner und Kind ein.", "20.02.2024", "neutral"),
                        new NotizRow("TEN-2024-018", "Stellplatz", "Interessiert an zusätzlichem Stellplatz ab Sommer.", "12.06.2024", "neutral"),

                        new NotizRow("TEN-2024-021", "Student", "Befristeter Vertrag wegen Studienzeit.", "10.03.2024", "neutral"),
                        new NotizRow("TEN-2024-021", "Internet", "Router wurde bei Einzug durch Hausverwaltung übergeben.", "16.03.2024", "neutral"),

                        new NotizRow("TEN-2024-026", "Reparatur", "Badarmatur wurde im Dezember geprüft.", "20.12.2024", "neutral"),

                        new NotizRow("TEN-2024-033", "Zahlungsverzug", "Mieter wurde wegen offener Mieten telefonisch kontaktiert.", "05.02.2025", "danger"),
                        new NotizRow("TEN-2024-033", "Ratenzahlung", "Ratenzahlung wurde mündlich angefragt, noch nicht bestätigt.", "06.02.2025", "warning"),

                        new NotizRow("TEN-2024-041", "Schichtdienst", "Mieterin arbeitet im Schichtdienst, Termine möglichst früh abstimmen.", "03.06.2024", "neutral"),

                        new NotizRow("TEN-2024-052", "Heizkosten", "Nachzahlung aus Dezember ist noch offen.", "21.12.2024", "warning"),

                        new NotizRow("TEN-2024-063", "Ausbildung", "Mieterin hat Ausbildungsnachweis eingereicht.", "01.08.2024", "neutral"),

                        new NotizRow("TEN-2024-074", "Gewerbe", "Einheit wird als kleines Planungsbüro genutzt.", "01.09.2024", "neutral"),
                        new NotizRow("TEN-2024-074", "Schlüssel", "Zusätzlicher Schlüssel für Mitarbeiter wurde genehmigt.", "05.09.2024", "neutral"),

                        new NotizRow("TEN-2024-081", "Altbau", "Mieterin wurde auf eingeschränkten Schallschutz hingewiesen.", "28.09.2024", "neutral"),

                        new NotizRow("TEN-2024-092", "Kündigung", "Kündigung wurde fristgerecht eingereicht.", "28.02.2025", "warning"),

                        new NotizRow("TEN-2044-007", "VIP-Mieter", "Sehr hohe Miete, alle Zahlungen pünktlich.", "01.09.2044", "success")
                ).stream()
                .filter(notiz -> notiz.mieterId().equals(mieterId))
                .toList();
    }

    // Gibt Notizen für einen bestimmten Mietvertrag zurück
    public static List<MietvertragNotizRow> getNotizenByMietvertragId(String mietvertragId) {
        return List.of(
                        new MietvertragNotizRow("MV-2023-042", "Vertrag geprüft", "Alle Unterlagen liegen vollständig vor.", "01.10.2021", "success"),
                        new MietvertragNotizRow("MV-2023-042", "Indexmiete", "Mietvertrag enthält keine Indexmietvereinbarung.", "01.10.2021", "neutral"),

                        new MietvertragNotizRow("MV-2023-084", "Kaution", "Kaution wurde vollständig eingezahlt.", "01.05.2022", "success"),
                        new MietvertragNotizRow("MV-2023-084", "Haustierregelung", "Eine Katze wurde im Vertrag zugelassen.", "12.04.2023", "warning"),

                        new MietvertragNotizRow("MV-2023-105", "Kündigung", "Kündigung wurde fristgerecht eingereicht.", "30.09.2023", "warning"),
                        new MietvertragNotizRow("MV-2023-105", "Übergabe", "Wohnungsübergabe zum Vertragsende vorbereiten.", "01.12.2023", "warning"),

                        new MietvertragNotizRow("MV-2024-014", "Kaution", "Kaution ist noch nicht vollständig gezahlt.", "02.02.2024", "warning"),
                        new MietvertragNotizRow("MV-2024-014", "Vertragsstart", "Einzug erfolgte ohne Mängelprotokoll.", "01.02.2024", "neutral"),

                        new MietvertragNotizRow("MV-2024-018", "Bankbürgschaft", "Bürgschaftsdokument liegt digital vor.", "01.03.2024", "success"),

                        new MietvertragNotizRow("MV-2024-021", "Befristung", "Befristung wegen Studentenapartment hinterlegt.", "15.03.2024", "neutral"),

                        new MietvertragNotizRow("MV-2024-026", "Mängel", "Kleiner Lackschaden an Tür bei Einzug dokumentiert.", "01.04.2024", "neutral"),

                        new MietvertragNotizRow("MV-2024-033", "Zahlungsverzug", "Mehrere offene Monatsmieten vorhanden.", "05.02.2025", "danger"),
                        new MietvertragNotizRow("MV-2024-033", "Mahnung", "Erste Mahnung wurde vorbereitet, aber noch nicht versendet.", "06.02.2025", "warning"),

                        new MietvertragNotizRow("MV-2024-041", "Vertrag geprüft", "Alle Pflichtfelder vollständig.", "01.06.2024", "success"),

                        new MietvertragNotizRow("MV-2024-052", "Heizkosten", "Nachzahlung Heizkosten noch offen.", "20.12.2024", "warning"),

                        new MietvertragNotizRow("MV-2024-063", "Kaution", "Letzte Kautionsrate steht noch aus.", "01.08.2024", "warning"),

                        new MietvertragNotizRow("MV-2024-074", "Gewerbevertrag", "Gewerbliche Nutzung wurde ausdrücklich erlaubt.", "01.09.2024", "neutral"),

                        new MietvertragNotizRow("MV-2024-081", "Altbau", "Mieterin wurde über mögliche Geräuschentwicklung informiert.", "01.10.2024", "neutral"),

                        new MietvertragNotizRow("MV-2024-092", "Kündigung", "Vertrag endet zum 31.05.2025.", "28.02.2025", "warning"),

                        new MietvertragNotizRow("MV-2044-007", "Sondervertrag", "Individuelle Kündigungsregelung vereinbart.", "27.08.2044", "neutral")
                ).stream()
                .filter(notiz -> notiz.mietvertragId().equals(mietvertragId))
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

    // Datenstruktur für einen Mietvertrag
    public record MietvertragRow(
            String id,
            String mieterId,
            String mieter,
            String mieterEmail,
            String immobilieEinheit,
            String mietobjektName,
            String mietobjektDetails,
            String laufzeit,
            String miete,
            String status,
            String statusStyle,
            String vertragsbeginn,
            String vertragsende,
            String kuendigungsfrist,
            String mindestmietdauerBis,
            String kuendigungseingang,
            String nettokaltmiete,
            String betriebskosten,
            String heizkosten,
            String bruttowarmmiete,
            String kautionsart,
            String kautionSoll,
            String kautionGezahlt,
            String kautionStatus,
            String kautionStatusStyle
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

    // Datenstruktur für eine Notiz vom Mieter
    public record NotizRow(
            String mieterId,
            String titel,
            String text,
            String datum,
            String statusStyle
    ) {
    }

    // Datenstruktur für eine Notiz vom Mietvertrag
    public record MietvertragNotizRow(
            String mietvertragId,
            String titel,
            String text,
            String datum,
            String statusStyle
    ) {
    }
}