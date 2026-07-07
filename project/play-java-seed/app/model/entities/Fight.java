package model.entities;

import jakarta.persistence.*;

/**
 * Represents the outcome of a fight between two teams stored in rounds.
 * <p>
 * A Fight links the winning round's team to the losing round's team,
 * recording the result of a single combat encounter.
 */
@Entity
public class Fight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner", nullable = false)
    private Round winner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loser", nullable = false)
    private Round loser;
}
