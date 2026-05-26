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

INSERT INTO mieteinheit (groesse, zimmerzahl, immobilie_id, bezeichnung, stockwerk, status, typ)
VALUES
    (50, 3, 3, 'Testbüro 1', 1, 'FREI', 'BUERO'),
    (55, 2, 3, 'Testbüro 2', 1, 'FREI', 'BUERO'),
    (70, 4, 3, 'Testbüro 3', 2, 'VERMIETET', 'BUERO'),
    (45, 1, 3, 'Testbüro 4', 3, 'IN_RENOVIERUNG', 'BUERO');
