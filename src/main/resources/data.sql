INSERT INTO immobilie (bezeichnung, typ, baujahr, flaeche, strasse, hausnummer, plz, stadt)
VALUES
    ('Parkresidenz Süd', 'MEHRFAMILIENHAUS', 1998, 850,
     'Parkstraße', '43', '33605', 'Bielefeld'),

    ('Altbau Ensemble Mitte', 'WOHNGEBAEUDE', 1965, 850,
     'Apfelstraße', '1', '33602', 'Bielefeld'),

    ('Seeblick Quartier', 'GEWERBEIMMOBILIE', 2012, 850,
     'Seestraße', '21', '33613', 'Bielefeld');

INSERT INTO nutzer (vorname, nachname, email, passwort)
VALUES (
           'Test',
           'User',
           'test@immopro.de',
           '$2a$10$3W7jwY/gaIDV/LzwG1vJie9tZfeDwk2lMtXhlnXwI.IvM5mJoOZRC'
    --passwort: test
       );