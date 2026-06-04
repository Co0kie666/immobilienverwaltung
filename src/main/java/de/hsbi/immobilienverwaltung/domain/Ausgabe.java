package de.hsbi.immobilienverwaltung.domain;
import de.hsbi.immobilienverwaltung.domain.enums.Ausgabenkategorie;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
public class Ausgabe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Ausgabenkategorie kategorie;

    private String titel;

    private BigDecimal betrag;

    private LocalDate datum;

    private LocalDate faelligkeitsdatum;

    private String beschreibung;

    private String empfaenger;

    @ManyToOne
    private Immobilie immobilie;

    @ManyToOne
    private Mieteinheit mieteinheit;

    public Ausgabe() {
    }

    public Ausgabe(String titel,
                   Ausgabenkategorie kategorie,
                   BigDecimal betrag,
                   LocalDate datum,
                   LocalDate faelligkeitsdatum,
                   String beschreibung,
                   String empfaenger,
                   Immobilie immobilie,
                   Mieteinheit mieteinheit) {
        this.titel = titel;
        this.kategorie = kategorie;
        this.betrag = betrag;
        this.datum = datum;
        this.faelligkeitsdatum = faelligkeitsdatum;
        this.beschreibung = beschreibung;
        this.empfaenger = empfaenger;
        this.immobilie = immobilie;
        this.mieteinheit = mieteinheit;
    }
}