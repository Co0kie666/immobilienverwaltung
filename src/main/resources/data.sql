INSERT INTO adresse (id, strasse, hausnummer, plz, stadt)
VALUES
    (1, 'Parkstraße', '12', '10115', 'Berlin'),
    (2, 'Hauptstraße', '10', '80331', 'München'),
    (3, 'Seestraße', '8-10', '20099', 'Hamburg');

INSERT INTO immobilie (id, bezeichnung, typ, baujahr, flaeche, adresse_id)
VALUES
    (1, 'Parkresidenz Süd', 'MEHRFAMILIENHAUS', 1998, 850, 1),
    (2, 'Altbau Ensemble Mitte', 'WOHNGEBAEUDE', 1965, 850, 2),
    (3, 'Seeblick Quartier', 'GEWERBEIMMOBILIE', 2012, 850, 3);

--- damit H2 Datenbank neue IDs ab nummer 4 vergibt ---
ALTER TABLE adresse ALTER COLUMN id RESTART WITH 4;
ALTER TABLE immobilie ALTER COLUMN id RESTART WITH 4;