package model.entities;

import jakarta.persistence.*;

@Entity
public class Fight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round1_id", nullable = false)
    private Round round1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round2_id", nullable = false)
    private Round round2;
}
