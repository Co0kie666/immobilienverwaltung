package de.hsbi.immobilienverwaltung.domain;

import de.hsbi.immobilienverwaltung.domain.enums.Vertragsstatus;
import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * Entity für einen Mietvertrag.
 * Ein Vertrag verbindet genau einen Mieter mit genau einer Mieteinheit.
 */
@Entity
public class Mietvertrag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Der Status zeigt, ob der Vertrag aktiv, gekündigt oder beendet ist.
     * Gekündigt bedeutet dabei nicht automatisch beendet.
     */
    @Enumerated(EnumType.STRING)
    private Vertragsstatus status;

    private LocalDate startdatum;
    private LocalDate enddatum;

    /**
     * Die monatliche Warmmiete ergibt sich später aus Kaltmiete plus Nebenkosten.
     * Gespeichert werden die Werte getrennt, damit sie einzeln bearbeitet werden können.
     */
    private Double kaltmiete;
    private Double nebenkosten;

    private Double kaution;

    /**
     * Hier wird gespeichert, bis wann beziehungsweise mit welcher Frist gekündigt werden kann.
     * Die genaue Logik zur Kündigung liegt im MietvertragService.
     */
    private LocalDate kuendigungsfrist;

    /**
     * Ein Mieter kann mehrere Mietverträge haben,
     * zum Beispiel wenn er historisch schon in einer anderen Einheit gewohnt hat.
     */
    @ManyToOne
    @JoinColumn(name = "mieter_id", nullable = false)
    private Mieter mieter;

    /**
     * Eine Mieteinheit kann über die Zeit mehrere Verträge haben.
     * Gleichzeitig darf fachlich aber nur ein Vertrag aktuell laufen.
     */
    @ManyToOne
    @JoinColumn(name = "mieteinheit_id", nullable = false)
    private Mieteinheit mieteinheit;

    public Mietvertrag(
            Vertragsstatus status,
            LocalDate startdatum,
            LocalDate enddatum,
            Double kaltmiete,
            Double nebenkosten,
            Double kaution,
            LocalDate kuendigungsfrist
    ) {
        this.status = status;
        this.startdatum = startdatum;
        this.enddatum = enddatum;
        this.kaltmiete = kaltmiete;
        this.nebenkosten = nebenkosten;
        this.kaution = kaution;
        this.kuendigungsfrist = kuendigungsfrist;
    }

    public Mietvertrag() {}

    public Long getId() {
        return id;
    }

    public LocalDate getStartdatum() {
        return startdatum;
    }

    public void setStartdatum(LocalDate startdatum) {
        this.startdatum = startdatum;
    }

    public LocalDate getEnddatum() {
        return enddatum;
    }

    public void setEnddatum(LocalDate enddatum) {
        this.enddatum = enddatum;
    }

    public Double getKaltmiete() {
        return kaltmiete;
    }

    public void setKaltmiete(Double kaltmiete) {
        this.kaltmiete = kaltmiete;
    }

    public Mieter getMieter() {
        return mieter;
    }

    public void setMieter(Mieter mieter) {
        this.mieter = mieter;
    }

    public Mieteinheit getMieteinheit() {
        return mieteinheit;
    }

    public void setMieteinheit(Mieteinheit mieteinheit) {
        this.mieteinheit = mieteinheit;
    }

    public Double getKaution() {
        return kaution;
    }

    public void setKaution(Double kaution) {
        this.kaution = kaution;
    }

    public LocalDate getKuendigungsfrist() {
        return kuendigungsfrist;
    }

    public void setKuendigungsfrist(LocalDate kuendigungsfrist) {
        this.kuendigungsfrist = kuendigungsfrist;
    }

    public Double getNebenkosten() {
        return nebenkosten;
    }

    public void setNebenkosten(Double nebenkosten) {
        this.nebenkosten = nebenkosten;
    }

    public Vertragsstatus getStatus() {
        return status;
    }

    public void setStatus(Vertragsstatus status) {
        this.status = status;
    }
}