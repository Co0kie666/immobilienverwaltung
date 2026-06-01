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

INSERT INTO nutzer (vorname, nachname, email, passwort)
VALUES
    (
        'Test',
        'User',
        'test@immopro.de',
        '$2a$10$3W7jwY/gaIDV/LzwG1vJie9tZfeDwk2lMtXhlnXwI.IvM5mJoOZRC'
        -- passwort: test
    ),
    (
        'Max',
        'Mustermann',
        'test@test.com',
        '$2a$10$3W7jwY/gaIDV/LzwG1vJie9tZfeDwk2lMtXhlnXwI.IvM5mJoOZRC'
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