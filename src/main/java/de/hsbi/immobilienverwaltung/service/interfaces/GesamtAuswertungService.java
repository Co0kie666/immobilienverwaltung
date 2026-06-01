package de.hsbi.immobilienverwaltung.service.interfaces;

public interface GesamtAuswertungService {

    double berechneLeerstandsquote();

    long berechneAnzahlLeerstehendeMieteinheiten();

    long berechneAnzahlMieteinheiten();
}