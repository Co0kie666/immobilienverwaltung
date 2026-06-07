-- ============================================================
-- IMMOBILIEN
-- ============================================================

INSERT INTO immobilie (
    bezeichnung,
    typ,
    baujahr,
    flaeche,
    strasse,
    hausnummer,
    plz,
    stadt
)
VALUES
    ('Parkresidenz Süd', 'MEHRFAMILIENHAUS', 1998, 850, 'Parkstraße', '43', '33605', 'Bielefeld'),
    ('Altbau Ensemble Mitte', 'WOHNGEBAEUDE', 1965, 850, 'Apfelstraße', '1', '33602', 'Bielefeld'),
    ('Seeblick Quartier', 'GEWERBEIMMOBILIE', 2012, 850, 'Seestraße', '21', '33613', 'Bielefeld'),
    ('Waldhof Brackwede', 'MEHRFAMILIENHAUS', 1984, 720, 'Hauptstraße', '88', '33647', 'Bielefeld'),
    ('City Apartments Ost', 'WOHNGEBAEUDE', 2005, 640, 'Detmolder Straße', '152', '33604', 'Bielefeld'),
    ('Gewerbepark Nord', 'GEWERBEIMMOBILIE', 2018, 1450, 'Industriestraße', '12', '33729', 'Bielefeld'),
    ('Lindenhof Schildesche', 'MEHRFAMILIENHAUS', 1978, 910, 'Beckhausstraße', '64', '33611', 'Bielefeld'),
    ('Wohnhaus Johannisberg', 'WOHNGEBAEUDE', 1992, 430, 'Am Johannisberg', '7', '33615', 'Bielefeld'),
    ('Business Center Mitte', 'GEWERBEIMMOBILIE', 2010, 1200, 'Bahnhofstraße', '19', '33602', 'Bielefeld'),
    ('Sonnenhof Senne', 'MEHRFAMILIENHAUS', 2001, 780, 'Buschkampstraße', '55', '33659', 'Bielefeld'),
    ('Wohnanlage Rosenweg', 'WOHNGEBAEUDE', 1999, 560, 'Rosenweg', '14', '33330', 'Gütersloh'),
    ('Gewerbehalle West', 'GEWERBEIMMOBILIE', 2015, 1800, 'Dieselstraße', '3', '33334', 'Gütersloh'),
    ('Mehrfamilienhaus Berliner Straße', 'MEHRFAMILIENHAUS', 1972, 690, 'Berliner Straße', '101', '33330', 'Gütersloh'),
    ('Wohnpark Am Stadtwald', 'WOHNGEBAEUDE', 2008, 740, 'Stadtwaldstraße', '22', '33609', 'Bielefeld'),
    ('Logistikzentrum Süd', 'GEWERBEIMMOBILIE', 2020, 2300, 'Lagerstraße', '8', '33689', 'Bielefeld'),
    ('Haus an der Lutter', 'WOHNGEBAEUDE', 1989, 390, 'Lutterstraße', '31', '33617', 'Bielefeld'),
    ('Quartier Ravensberg', 'MEHRFAMILIENHAUS', 2016, 980, 'Ravensberger Straße', '45', '33602', 'Bielefeld'),
    ('Bürohaus Campus', 'GEWERBEIMMOBILIE', 2011, 1100, 'Universitätsstraße', '25', '33615', 'Bielefeld'),
    ('Wohnhaus Quellenhof', 'WOHNGEBAEUDE', 1975, 510, 'Quellenhofweg', '9', '33617', 'Bielefeld'),
    ('Mehrfamilienhaus Teutoblick', 'MEHRFAMILIENHAUS', 1995, 860, 'Teutoburger Straße', '73', '33604', 'Bielefeld'),
    ('Gewerbefläche Innenstadt', 'GEWERBEIMMOBILIE', 2003, 950, 'Niederwall', '18', '33602', 'Bielefeld'),
    ('Wohnanlage Nordpark', 'MEHRFAMILIENHAUS', 1982, 770, 'Nordparkstraße', '11', '33613', 'Bielefeld'),
    ('Stadthaus Heepen', 'WOHNGEBAEUDE', 1997, 480, 'Heeper Straße', '204', '33719', 'Bielefeld'),
    ('Gewerbeobjekt Senne', 'GEWERBEIMMOBILIE', 2019, 1600, 'Senneweg', '40', '33659', 'Bielefeld');


-- ============================================================
-- NUTZER
-- ============================================================

INSERT INTO nutzer (
    vorname,
    nachname,
    email,
    passwort,
    rolle
)
VALUES
    (
        'Test',
        'User',
        'test@immopro.de',
        '$2a$10$3W7jwY/gaIDV/LzwG1vJie9tZfeDwk2lMtXhlnXwI.IvM5mJoOZRC',
        'USER'
    ),
    (
        'Max',
        'Mustermann',
        'test@test.com',
        '$2a$10$3W7jwY/gaIDV/LzwG1vJie9tZfeDwk2lMtXhlnXwI.IvM5mJoOZRC',
        'USER'
    );

-- Hinweis: Passwort für beide Nutzer: test


-- ============================================================
-- MIETEINHEITEN
-- ============================================================

INSERT INTO mieteinheit (
    bezeichnung,
    status,
    typ,
    groesse,
    zimmerzahl,
    stockwerk,
    immobilie_id
)
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


-- ============================================================
-- MIETER
-- ============================================================

INSERT INTO mieter (
    archiviert,
    bankdaten_aktiv,
    geburtsdatum,
    anrede,
    beruf,
    bic,
    email,
    hausnummer,
    iban,
    kontoinhaber,
    nachname,
    plz,
    stadt,
    strasse,
    telefonnummer,
    titel,
    vorname
)
VALUES
    (FALSE, TRUE,  '1990-04-12', 'Herr',   'Softwareentwickler', 'Sparkasse Bielefeld', 'max.mustermann@example.de',      '12',  'DE89370400440532013000', 'Max Mustermann',   'Mustermann', '33602', 'Bielefeld',   'Bahnhofstraße',       '+49 151 1234567',  '',    'Max'),
    (FALSE, TRUE,  '1985-09-23', 'Frau',   'Architektin',        'Volksbank OWL',       'anna.schmidt@example.de',        '8a',  'DE12500105170648489890', 'Anna Schmidt',    'Schmidt',    '33615', 'Bielefeld',   'Detmolder Straße',    '+49 160 9876543',  'Dr.', 'Anna'),
    (FALSE, FALSE, '1998-01-30', 'Herr',   'Student',            '',                    'lukas.weber@example.de',         '45',  '',                       '',                'Weber',      '33613', 'Bielefeld',   'Jöllenbecker Straße', '+49 176 11122233', '',    'Lukas'),
    (FALSE, TRUE,  '1977-06-18', 'Frau',   'Lehrerin',           'Commerzbank',         'sabine.fischer@example.de',      '3',   'DE75512108001245126199', 'Sabine Fischer',  'Fischer',    '33604', 'Bielefeld',   'August-Bebel-Straße', '+49 152 33344455', '',    'Sabine'),
    (FALSE, FALSE, '1992-11-05', 'Divers', 'Grafikdesigner',     '',                    'kim.wagner@example.de',          '21b', '',                       '',                'Wagner',     '33607', 'Bielefeld',   'Herforder Straße',    '+49 170 55566677', '',    'Kim'),

    (FALSE, TRUE,  '1969-03-14', 'Herr',   'Elektriker',         'Deutsche Bank',       'thomas.becker@example.de',       '17',  'DE02120300000000202051', 'Thomas Becker',   'Becker',     '33647', 'Bielefeld',   'Brackweder Straße',   '+49 171 22233344', '',    'Thomas'),
    (FALSE, TRUE,  '1988-12-01', 'Frau',   'Buchhalterin',       'ING-DiBa',            'julia.hoffmann@example.de',      '6',   'DE44500105175407324931', 'Julia Hoffmann',  'Hoffmann',   '33619', 'Bielefeld',   'Voltmannstraße',      '+49 172 44455566', '',    'Julia'),
    (FALSE, FALSE, '1995-07-27', 'Herr',   'Pflegefachkraft',    '',                    'benjamin.schulz@example.de',     '99',  '',                       '',                'Schulz',     '33609', 'Bielefeld',   'Heeper Straße',       '+49 173 77788899', '',    'Benjamin'),
    (FALSE, TRUE,  '1981-02-09', 'Frau',   'Rechtsanwältin',     'Postbank',            'maria.koch@example.de',          '14c', 'DE89370400440532013001', 'Maria Koch',      'Koch',       '33611', 'Bielefeld',   'Schloßhofstraße',     '+49 174 12398765', 'Dr.', 'Maria'),
    (TRUE,  FALSE, '1974-10-20', 'Herr',   'Rentner',            '',                    'peter.hansen@example.de',        '15',  '',                       '',                'Hansen',     '93476', 'Pfänningen', 'Fasanstraße',         '123456',           '',    'Peter');


-- ============================================================
-- MIETVERTRÄGE
-- ============================================================

INSERT INTO mietvertrag (
    enddatum,
    kaltmiete,
    kaution,
    kuendigungsfrist,
    nebenkosten,
    startdatum,
    mieteinheit_id,
    mieter_id,
    status
)
VALUES
    ('2027-02-17', 500.00,  1000.00, '2026-07-02', 150.00, '2026-06-02', 1,  1,  'AKTIV'),
    ('2028-03-31', 720.00,  1440.00, '2027-12-31', 210.00, '2026-04-01', 2,  2,  'AKTIV'),
    (NULL,         650.00,  1300.00, '2026-09-30', 180.00, '2026-01-15', 3,  3,  'AKTIV'),
    ('2027-12-31', 580.00,  1160.00, '2027-09-30', 160.00, '2025-11-01', 4,  4,  'AKTIV'),
    (NULL,         820.00,  1640.00, '2026-10-31', 230.00, '2026-02-01', 5,  5,  'AKTIV'),
    ('2026-12-31', 690.00,  1380.00, '2026-09-30', 190.00, '2025-08-01', 6,  6,  'GEKUENDIGT'),
    (NULL,         760.00,  1520.00, '2026-11-30', 200.00, '2026-03-01', 7,  7,  'AKTIV'),
    ('2027-06-30', 1100.00, 2200.00, '2027-03-31', 320.00, '2026-01-01', 8,  8,  'AKTIV'),
    (NULL,         950.00,  1900.00, '2026-12-31', 280.00, '2026-05-01', 9,  9,  'AKTIV'),
    ('2026-05-31', 480.00,  960.00,  '2026-02-28', 140.00, '2024-09-01', 10, 10, 'BEENDET');


-- ============================================================
-- ZAHLUNGSEINGÄNGE
-- ============================================================

INSERT INTO zahlungseingang (
    betrag,
    beschreibung,
    leistungsmonat,
    typ,
    zahlungsdatum,
    status,
    mietvertrag_id
)
VALUES
    (500.00,  'Kaltmiete Juni für WE-01',                       '2026-06-01', 'KALTMIETE',    '2026-06-03', 'Bezahlt / Erledigt', 1),
    (150.00,  'Nebenkosten Juni für WE-01',                      '2026-06-01', 'NEBENKOSTEN', '2026-06-03', 'Bezahlt / Erledigt', 1),
    (720.00,  'Kaltmiete Juni für WE-02',                       '2026-06-01', 'KALTMIETE',    '2026-06-05', 'Offen / Ausstehend', 2),
    (210.00,  'Nebenkosten Juni für WE-02',                      '2026-06-01', 'NEBENKOSTEN', '2026-06-05', 'Offen / Ausstehend', 2),
    (1300.00, 'Kaution Mietvertrag WE-03',                       '2026-06-01', 'KAUTION',      '2026-06-07', 'Bezahlt / Erledigt', 3),
    (650.00,  'Kaltmiete Juni für WE-03',                       '2026-06-01', 'KALTMIETE',    '2026-06-07', 'Bezahlt / Erledigt', 3),
    (820.00,  'Kaltmiete Juni für WE-01 Altbau Ensemble Mitte', '2026-06-01', 'KALTMIETE',    '2026-06-10', 'Offen / Ausstehend', 5),
    (230.00,  'Nebenkosten Juni für WE-01 Altbau Ensemble Mitte','2026-06-01', 'NEBENKOSTEN', '2026-06-10', 'Offen / Ausstehend', 5),
    (1100.00, 'Kaltmiete Juni für Büro EG',                     '2026-06-01', 'KALTMIETE',    '2026-06-12', 'Bezahlt / Erledigt', 8),
    (320.00,  'Nebenkosten Juni für Büro EG',                    '2026-06-01', 'NEBENKOSTEN', '2026-06-12', 'Bezahlt / Erledigt', 8);


-- ============================================================
-- AUSGABEN
-- ============================================================

INSERT INTO ausgabe (
    betrag,
    datum,
    faelligkeitsdatum,
    beschreibung,
    empfaenger,
    titel,
    kategorie,
    status,
    immobilie_id,
    mieteinheit_id
)
VALUES
    (390.00, '2026-06-12', NULL, 'Heizungsreparatur in WE-03',                 'Heizung Krüger',        'Heizungsreparatur WE-03', 'REPARATUR',       'Bezahlt / Erledigt', 2, 7),
    (600.00, '2026-06-15', NULL, 'Gebäudeversicherung für Seeblick Quartier',  'Versicherung AG',       'Gebäudeversicherung',     'VERSICHERUNG',    'Offen / Ausstehend', 3, NULL),
    (520.00, '2026-06-18', NULL, 'Reparatur im Büro EG',                       'Facility Service GmbH',  'Reparatur Büro EG',       'REPARATUR',       'Bezahlt / Erledigt', 3, 8),
    (280.00, '2026-06-20', NULL, 'Lagerwartung Lager A',                       'Logistik Service Nord', 'Wartung Lager A',         'INSTANDHALTUNG',  'Offen / Ausstehend', 3, 9),
    (350.00, '2026-06-22', NULL, 'Sonstige Betriebskosten für Gewerbefläche 1', 'Dienstleister GmbH',    'Sonstige Kosten',         'SONSTIGES',       'Bezahlt / Erledigt', 3, 10);