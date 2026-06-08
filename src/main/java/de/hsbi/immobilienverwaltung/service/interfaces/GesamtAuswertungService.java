package de.hsbi.immobilienverwaltung.service.interfaces;

import java.math.BigDecimal;

public interface GesamtAuswertungService {

    double berechneLeerstandsquote();

    long berechneAnzahlLeerstehendeMieteinheiten();

    long berechneAnzahlMieteinheiten();

    long berechneAnzahlAktiveVertraege();
}