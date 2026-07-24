package model.service.fightingService.system;

import model.DTO.fighting.CombatEndDTO;
import model.DTO.fighting.FightingEventDTO;
import model.entities.Team;
import model.service.fightingService.ComponentUnit;
import model.service.fightingService.FightingContext;
import org.junit.Test;
import org.mockito.MockedConstruction;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * CombatEndDTO est construite directement via "new" dans EndSystem ; on
 * intercepte sa construction via Mockito#mockConstruction pour capturer les
 * arguments passes (tick, gagnant) sans dependre des accesseurs reels de
 * CombatEndDTO, que nous n'avons pas.
 */
public class TestEndSystem {

    private FightingContext mockContextFor(ComponentUnit firstAlive, List<ComponentUnit> allies,
                                           List<ComponentUnit> enemies, int tick) {
        FightingContext context = mock(FightingContext.class);
        when(context.getAliveUnits()).thenReturn(List.of(firstAlive));
        when(context.getAllies(firstAlive)).thenReturn(allies);
        when(context.getEnemies(firstAlive)).thenReturn(enemies);
        when(context.getTick()).thenReturn(tick);
        return context;
    }

    private ComponentUnit mockUnit(Team team, boolean alive) {
        ComponentUnit unit = mock(ComponentUnit.class);
        when(unit.getTeam()).thenReturn(team);
        when(unit.isAlive()).thenReturn(alive);
        return unit;
    }

    @Test
    public void isFinished_isFalseByDefault_beforeAnyUpdate() {
        EndSystem system = new EndSystem();
        assertFalse(system.isFinished());
    }

    @Test
    public void update_bothTeamsHaveNoAliveUnitsLeft_declaresDrawAndFinishesCombat() {
        ComponentUnit lastUnit = mock(ComponentUnit.class);
        ComponentUnit deadAlly = mock(ComponentUnit.class);
        when(deadAlly.isAlive()).thenReturn(false);
        ComponentUnit deadEnemy = mock(ComponentUnit.class);
        when(deadEnemy.isAlive()).thenReturn(false);

        FightingContext context = mockContextFor(lastUnit, List.of(deadAlly), List.of(deadEnemy), 7);

        List<Object> capturedArgs = new ArrayList<>();
        try (MockedConstruction<CombatEndDTO> mocked = mockConstruction(CombatEndDTO.class,
                (mock, ctx) -> capturedArgs.addAll(ctx.arguments()))) {

            EndSystem system = new EndSystem();
            List<FightingEventDTO> events = system.update(context);

            assertEquals(1, events.size());
            assertTrue(system.isFinished());
            assertEquals(7L, capturedArgs.get(0));
            assertEquals(-1L,capturedArgs.get(1));
            // le champ "winner" (Long) vaut null -> l'auto-unboxing de getWinner() (long) doit lever une NPE
            assertThrows(NullPointerException.class, system::getWinner);
        }
    }

    @Test
    public void update_teamAEmpty_teamBWins() {
        Team teamB = mock(Team.class);
        when(teamB.getId()).thenReturn(2L);

        ComponentUnit lastUnit = mock(ComponentUnit.class);
        ComponentUnit deadAlly = mock(ComponentUnit.class);
        when(deadAlly.isAlive()).thenReturn(false);
        ComponentUnit aliveEnemy = mockUnit(teamB, true);

        FightingContext context = mockContextFor(lastUnit, List.of(deadAlly), List.of(aliveEnemy), 12);

        List<Object> capturedArgs = new ArrayList<>();
        try (MockedConstruction<CombatEndDTO> mocked = mockConstruction(CombatEndDTO.class,
                (mock, ctx) -> capturedArgs.addAll(ctx.arguments()))) {

            EndSystem system = new EndSystem();
            List<FightingEventDTO> events = system.update(context);

            assertEquals(1, events.size());
            assertTrue(system.isFinished());
            assertEquals(12L, capturedArgs.get(0));
            assertEquals(2L, capturedArgs.get(1));
            assertEquals(2L, system.getWinner());
        }
    }

    @Test
    public void update_teamBEmpty_teamAWins() {
        Team teamA = mock(Team.class);
        when(teamA.getId()).thenReturn(1L);

        ComponentUnit lastUnit = mock(ComponentUnit.class);
        ComponentUnit aliveAlly = mockUnit(teamA, true);
        ComponentUnit deadEnemy = mock(ComponentUnit.class);
        when(deadEnemy.isAlive()).thenReturn(false);

        FightingContext context = mockContextFor(lastUnit, List.of(aliveAlly), List.of(deadEnemy), 5);

        List<Object> capturedArgs = new ArrayList<>();
        try (MockedConstruction<CombatEndDTO> mocked = mockConstruction(CombatEndDTO.class,
                (mock, ctx) -> capturedArgs.addAll(ctx.arguments()))) {

            EndSystem system = new EndSystem();
            List<FightingEventDTO> events = system.update(context);

            assertEquals(1, events.size());
            assertTrue(system.isFinished());
            assertEquals(5L, capturedArgs.get(0));
            assertEquals(1L, capturedArgs.get(1));
            assertEquals(1L, system.getWinner());
        }
    }

    @Test
    public void update_bothTeamsStillHaveAliveUnits_combatContinuesAndStaysUnfinished() {
        Team teamA = mock(Team.class);
        Team teamB = mock(Team.class);

        ComponentUnit lastUnit = mock(ComponentUnit.class);
        ComponentUnit aliveAlly = mockUnit(teamA, true);
        ComponentUnit aliveEnemy = mockUnit(teamB, true);

        FightingContext context = mockContextFor(lastUnit, List.of(aliveAlly), List.of(aliveEnemy), 3);

        EndSystem system = new EndSystem();
        List<FightingEventDTO> events = system.update(context);

        assertTrue(events.isEmpty());
        assertFalse(system.isFinished());
    }

    @Test
    public void update_throwsWhenThereAreNoAliveUnitsAtAll() {
        // context.getAliveUnits().getFirst() leve NoSuchElementException si la liste est vide
        FightingContext context = mock(FightingContext.class);
        when(context.getAliveUnits()).thenReturn(List.of());

        EndSystem system = new EndSystem();

        assertThrows(NoSuchElementException.class, () -> system.update(context));
    }
}