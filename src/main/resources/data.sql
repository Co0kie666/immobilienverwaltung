INSERT INTO immobilie (bezeichnung, typ, baujahr, flaeche, strasse, hausnummer, plz, stadt)
VALUES
    ('Parkresidenz Süd', 'MEHRFAMILIENHAUS', 1998, 850,
     'Parkstraße', '43', '33605', 'Bielefeld'),

    ('Altbau Ensemble Mitte', 'WOHNGEBAEUDE', 1965, 850,
     'Apfelstraße', '1', '33602', 'Bielefeld'),

    ('Seeblick Quartier', 'GEWERBEIMMOBILIE', 2012, 850,
     'Seestraße', '21', '33613', 'Bielefeld');

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