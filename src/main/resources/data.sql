INSERT INTO immobilie (bezeichnung, typ, baujahr, flaeche, strasse, hausnummer, plz, stadt)
VALUES
    ('Parkresidenz Süd', 'MEHRFAMILIENHAUS', 1998, 850,
     'Parkstraße', '43', '33605', 'Bielefeld'),
    ('Altbau Ensemble Mitte', 'WOHNGEBAEUDE', 1965, 850,
     'Apfelstraße', '1', '33602', 'Bielefeld'),
    ('Seeblick Quartier', 'GEWERBEIMMOBILIE', 2012, 850,
     'Seestraße', '21', '33613', 'Bielefeld'),
    ('Waldhof Brackwede', 'MEHRFAMILIENHAUS', 1984, 720,
     'Hauptstraße', '88', '33647', 'Bielefeld'),
    ('City Apartments Ost', 'WOHNGEBAEUDE', 2005, 640,
     'Detmolder Straße', '152', '33604', 'Bielefeld'),
    ('Gewerbepark Nord', 'GEWERBEIMMOBILIE', 2018, 1450,
     'Industriestraße', '12', '33729', 'Bielefeld'),
    ('Lindenhof Schildesche', 'MEHRFAMILIENHAUS', 1978, 910,
     'Beckhausstraße', '64', '33611', 'Bielefeld'),
    ('Wohnhaus Johannisberg', 'WOHNGEBAEUDE', 1992, 430,
     'Am Johannisberg', '7', '33615', 'Bielefeld'),
    ('Business Center Mitte', 'GEWERBEIMMOBILIE', 2010, 1200,
     'Bahnhofstraße', '19', '33602', 'Bielefeld'),
    ('Sonnenhof Senne', 'MEHRFAMILIENHAUS', 2001, 780,
     'Buschkampstraße', '55', '33659', 'Bielefeld'),
    ('Wohnanlage Rosenweg', 'WOHNGEBAEUDE', 1999, 560,
     'Rosenweg', '14', '33330', 'Gütersloh'),
    ('Gewerbehalle West', 'GEWERBEIMMOBILIE', 2015, 1800,
     'Dieselstraße', '3', '33334', 'Gütersloh'),
    ('Mehrfamilienhaus Berliner Straße', 'MEHRFAMILIENHAUS', 1972, 690,
     'Berliner Straße', '101', '33330', 'Gütersloh'),
    ('Wohnpark Am Stadtwald', 'WOHNGEBAEUDE', 2008, 740,
     'Stadtwaldstraße', '22', '33609', 'Bielefeld'),
    ('Logistikzentrum Süd', 'GEWERBEIMMOBILIE', 2020, 2300,
     'Lagerstraße', '8', '33689', 'Bielefeld'),
    ('Haus an der Lutter', 'WOHNGEBAEUDE', 1989, 390,
     'Lutterstraße', '31', '33617', 'Bielefeld'),
    ('Quartier Ravensberg', 'MEHRFAMILIENHAUS', 2016, 980,
     'Ravensberger Straße', '45', '33602', 'Bielefeld'),
    ('Bürohaus Campus', 'GEWERBEIMMOBILIE', 2011, 1100,
     'Universitätsstraße', '25', '33615', 'Bielefeld'),
    ('Wohnhaus Quellenhof', 'WOHNGEBAEUDE', 1975, 510,
     'Quellenhofweg', '9', '33617', 'Bielefeld'),
    ('Mehrfamilienhaus Teutoblick', 'MEHRFAMILIENHAUS', 1995, 860,
     'Teutoburger Straße', '73', '33604', 'Bielefeld'),
    ('Gewerbefläche Innenstadt', 'GEWERBEIMMOBILIE', 2003, 950,
     'Niederwall', '18', '33602', 'Bielefeld'),
    ('Wohnanlage Nordpark', 'MEHRFAMILIENHAUS', 1982, 770,
     'Nordparkstraße', '11', '33613', 'Bielefeld'),
    ('Stadthaus Heepen', 'WOHNGEBAEUDE', 1997, 480,
     'Heeper Straße', '204', '33719', 'Bielefeld'),
    ('Gewerbeobjekt Senne', 'GEWERBEIMMOBILIE', 2019, 1600,
     'Senneweg', '40', '33659', 'Bielefeld');

INSERT INTO nutzer (vorname, nachname, email, passwort, rolle)
VALUES (
        'Test',
        'User',
        'test@immopro.de',
        '$2a$10$3W7jwY/gaIDV/LzwG1vJie9tZfeDwk2lMtXhlnXwI.IvM5mJoOZRC',
        'USER'
        -- passwort: test
        ),
    (
        'Max',
        'Mustermann',
        'test@test.com',
        '$2a$10$3W7jwY/gaIDV/LzwG1vJie9tZfeDwk2lMtXhlnXwI.IvM5mJoOZRC',
        'USER'
        -- passwort: test
    );

INSERT INTO mieteinheit (bezeichnung, status, typ, groesse, zimmerzahl, stockwerk, immobilie_id)
VALUES
    ('WE-01', 'VERMIETET', 'WOHNUNG', 85, 3, 'EG', 1),
    ('WE-02', 'VERMIETET', 'WOHNUNG', 92, 4, '1. OG', 1),
    ('WE-03', 'FREI', 'WOHNUNG', 75, 3, '2. OG', 1),
    ('WE-04', 'IN_RENOVIERUNG', 'WOHNUNG', 68, 2, '3. OG', 1),

    ('WE-01', 'VERMIETET', 'WOHNUNG', 70, 2, 'EG', 2),
    ('WE-02', 'FREI', 'WOHNUNG', 88, 3, '1. OG', 2),
    ('WE-03', 'VERMIETET', 'WOHNUNG', 96, 4, '2. OG', 2),

    ('Büro EG', 'VERMIETET', 'BUERO', 120, 5, 'EG', 3),
    ('Lager A', 'FREI', 'LAGERHALLE', 200, 1, 'UG', 3),
    ('Gewerbefläche 1', 'VERMIETET', 'GEWERBEFLAECHE', 150, 3, '1. OG', 3);
INSERT INTO AUSGABE
(BETRAG, DATUM, FAELLIGKEITSDATUM, BESCHREIBUNG, EMPFAENGER, TITEL, KATEGORIE, IMMOBILIE_ID, MIETEINHEIT_ID)
VALUES
    (700.00, '2026-06-02', NULL, 'Hausverwaltung Juni für Parkresidenz Süd', 'Hausverwaltung GmbH', 'Hausverwaltung Juni', 'VERWALTUNG', 1, NULL);

INSERT INTO AUSGABE
(BETRAG, DATUM, FAELLIGKEITSDATUM, BESCHREIBUNG, EMPFAENGER, TITEL, KATEGORIE, IMMOBILIE_ID, MIETEINHEIT_ID)
VALUES
    (320.00, '2026-06-04', NULL, 'Rohrreparatur in WE-01', 'Müller Sanitär', 'Reparatur WE-01', 'REPARATUR', 1, 1);

INSERT INTO AUSGABE
(BETRAG, DATUM, FAELLIGKEITSDATUM, BESCHREIBUNG, EMPFAENGER, TITEL, KATEGORIE, IMMOBILIE_ID, MIETEINHEIT_ID)
VALUES
    (180.00, '2026-06-06', NULL, 'Elektroprüfung in WE-02', 'Elektro Schmidt', 'Elektroprüfung WE-02', 'INSTANDHALTUNG', 1, 2);

INSERT INTO AUSGABE
(BETRAG, DATUM, FAELLIGKEITSDATUM, BESCHREIBUNG, EMPFAENGER, TITEL, KATEGORIE, IMMOBILIE_ID, MIETEINHEIT_ID)
VALUES
    (450.00, '2026-06-08', NULL, 'Renovierung in WE-04', 'Malerbetrieb Weber', 'Renovierung WE-04', 'INSTANDHALTUNG', 1, 4);

INSERT INTO AUSGABE
(BETRAG, DATUM, FAELLIGKEITSDATUM, BESCHREIBUNG, EMPFAENGER, TITEL, KATEGORIE, IMMOBILIE_ID, MIETEINHEIT_ID)
VALUES
    (250.00, '2026-06-10', NULL, 'Wartung für Altbau Ensemble Mitte', 'Hausservice Bielefeld', 'Wartung Gebäude', 'INSTANDHALTUNG', 2, NULL);

INSERT INTO AUSGABE
(BETRAG, DATUM, FAELLIGKEITSDATUM, BESCHREIBUNG, EMPFAENGER, TITEL, KATEGORIE, IMMOBILIE_ID, MIETEINHEIT_ID)
VALUES
    (390.00, '2026-06-12', NULL, 'Heizungsreparatur in WE-03', 'Heizung Krüger', 'Heizungsreparatur WE-03', 'REPARATUR', 2, 7);

INSERT INTO AUSGABE
(BETRAG, DATUM, FAELLIGKEITSDATUM, BESCHREIBUNG, EMPFAENGER, TITEL, KATEGORIE, IMMOBILIE_ID, MIETEINHEIT_ID)
VALUES
    (600.00, '2026-06-15', NULL, 'Gebäudeversicherung für Seeblick Quartier', 'Versicherung AG', 'Gebäudeversicherung', 'VERSICHERUNG', 3, NULL);

INSERT INTO AUSGABE
(BETRAG, DATUM, FAELLIGKEITSDATUM, BESCHREIBUNG, EMPFAENGER, TITEL, KATEGORIE, IMMOBILIE_ID, MIETEINHEIT_ID)
VALUES
    (520.00, '2026-06-18', NULL, 'Reparatur im Büro EG', 'Facility Service GmbH', 'Reparatur Büro EG', 'REPARATUR', 3, 8);

INSERT INTO AUSGABE
(BETRAG, DATUM, FAELLIGKEITSDATUM, BESCHREIBUNG, EMPFAENGER, TITEL, KATEGORIE, IMMOBILIE_ID, MIETEINHEIT_ID)
VALUES
    (280.00, '2026-06-20', NULL, 'Lagerwartung Lager A', 'Logistik Service Nord', 'Wartung Lager A', 'INSTANDHALTUNG', 3, 9);

INSERT INTO AUSGABE
(BETRAG, DATUM, FAELLIGKEITSDATUM, BESCHREIBUNG, EMPFAENGER, TITEL, KATEGORIE, IMMOBILIE_ID, MIETEINHEIT_ID)
VALUES
    (350.00, '2026-06-22', NULL, 'Sonstige Betriebskosten für Gewerbefläche 1', 'Dienstleister GmbH', 'Sonstige Kosten', 'SONSTIGES', 3, 10);