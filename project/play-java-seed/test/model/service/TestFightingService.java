package model.service;

import model.DTO.ComponentUnitDTO;
import model.DTO.UnitDTOMapper;
import model.DTO.fighting.FightingResultDTO;
import model.entities.Team;
import model.entities.unit.InstanceUnit;
import model.service.fightingService.ComponentUnit;
import model.service.fightingService.FightingContext;
import model.service.fightingService.FightingService;
import model.service.fightingService.system.ActionSystem;
import model.service.fightingService.system.EndSystem;
import model.service.fightingService.system.StatusSystem;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * FightingService orchestre plusieurs collaborateurs construits directement
 * via "new" en son sein (FightingContext, StatusSystem, ActionSystem,
 * EndSystem, ComponentUnit, FightingResultDTO). On isole donc entièrement
 * la logique propre à FightingService (boucle de simulation, calcul des PV
 * perdus, délégation) via Mockito#mockConstruction sur chacune de ces
 * classes, et Mockito#mockStatic sur UnitDTOMapper (méthode statique
 * componentToDTO utilisée par référence de méthode).
 *
 * Ces tests ne vérifient donc pas le comportement réel de la simulation
 * (ça, c'est le rôle des tests des systèmes eux-mêmes), mais uniquement la
 * façon dont FightingService les enchaîne.
 */
public class TestFightingService {

    private Team mockTeamWithUnits(long id, int lvl, int unitCount) {
        Team team = mock(Team.class);
        when(team.getId()).thenReturn(id);
        when(team.getLvl()).thenReturn(lvl);

        List<InstanceUnit> units = new ArrayList<>();
        for (int i = 0; i < unitCount; i++) {
            units.add(mock(InstanceUnit.class));
        }
        when(team.getUnits()).thenReturn(units);
        return team;
    }

    @Test
    public void constructor_createsOneComponentUnitPerUnitInBothTeamsAndInitializesCollaborators() {
        Team teamA = mockTeamWithUnits(1L, 3, 2);
        Team teamB = mockTeamWithUnits(2L, 3, 3);

        try (MockedConstruction<ComponentUnit> mockedComponentUnit = mockConstruction(ComponentUnit.class);
             MockedConstruction<FightingContext> mockedContext = mockConstruction(FightingContext.class,
                     (mock, ctx) -> when(mock.getAliveUnits()).thenReturn(List.of()));
             MockedConstruction<StatusSystem> mockedStatus = mockConstruction(StatusSystem.class);
             MockedConstruction<ActionSystem> mockedAction = mockConstruction(ActionSystem.class);
             MockedConstruction<EndSystem> mockedEnd = mockConstruction(EndSystem.class,
                     (mock, ctx) -> when(mock.isFinished()).thenReturn(true));
             MockedStatic<UnitDTOMapper> mockedMapper = mockStatic(UnitDTOMapper.class)) {

            mockedMapper.when(() -> UnitDTOMapper.componentToDTO(any())).thenReturn(mock(ComponentUnitDTO.class));

            FightingService service = new FightingService(42L, teamA, teamB);

            assertNotNull(service);
            // 2 unites teamA + 3 unites teamB = 5 ComponentUnit crees
            assertEquals(5, mockedComponentUnit.constructed().size());
            assertEquals(1, mockedContext.constructed().size());
            assertEquals(1, mockedStatus.constructed().size());
            assertEquals(1, mockedAction.constructed().size());
            assertEquals(1, mockedEnd.constructed().size());
        }
    }

    @Test
    public void simulate_stopsAsSoonAsEndSystemIsFinished() {
        Team teamA = mockTeamWithUnits(1L, 3, 1);
        Team teamB = mockTeamWithUnits(2L, 3, 1);

        try (MockedConstruction<ComponentUnit> mockedComponentUnit = mockConstruction(ComponentUnit.class);
             MockedConstruction<FightingContext> mockedContext = mockConstruction(FightingContext.class,
                     (mock, ctx) -> {
                         when(mock.getAliveUnits()).thenReturn(List.of());
                         when(mock.getTick()).thenReturn(0);
                     });
             MockedConstruction<StatusSystem> mockedStatus = mockConstruction(StatusSystem.class,
                     (mock, ctx) -> when(mock.update(any())).thenReturn(List.of()));
             MockedConstruction<ActionSystem> mockedAction = mockConstruction(ActionSystem.class,
                     (mock, ctx) -> when(mock.update(any())).thenReturn(List.of()));
             MockedConstruction<EndSystem> mockedEnd = mockConstruction(EndSystem.class,
                     (mock, ctx) -> {
                         // finie apres le 1er tour de boucle
                         when(mock.isFinished()).thenReturn(false, true);
                         when(mock.update(any())).thenReturn(List.of());
                     });
             MockedStatic<UnitDTOMapper> mockedMapper = mockStatic(UnitDTOMapper.class);
             MockedConstruction<FightingResultDTO> mockedResult = mockConstruction(FightingResultDTO.class)) {

            mockedMapper.when(() -> UnitDTOMapper.componentToDTO(any())).thenReturn(mock(ComponentUnitDTO.class));

            FightingService service = new FightingService(1L, teamA, teamB);
            FightingResultDTO result = service.simulate();

            assertNotNull(result);
            FightingContext context = mockedContext.constructed().get(0);
            StatusSystem status = mockedStatus.constructed().get(0);
            ActionSystem action = mockedAction.constructed().get(0);
            EndSystem end = mockedEnd.constructed().get(0);

            verify(context, times(1)).incrementTick();
            verify(status, times(1)).update(context);
            verify(action, times(1)).update(context);
            verify(end, times(1)).update(context);
        }
    }

    @Test
    public void simulate_stopsAtMaxTicksEvenIfEndSystemNeverFinishes() {
        Team teamA = mockTeamWithUnits(1L, 3, 1);
        Team teamB = mockTeamWithUnits(2L, 3, 1);

        AtomicInteger tickCounter = new AtomicInteger(0);

        try (MockedConstruction<ComponentUnit> mockedComponentUnit = mockConstruction(ComponentUnit.class);
             MockedConstruction<FightingContext> mockedContext = mockConstruction(FightingContext.class,
                     (mock, ctx) -> {
                         when(mock.getAliveUnits()).thenReturn(List.of());
                         when(mock.getTick()).thenAnswer(inv -> tickCounter.get());
                         doAnswer(inv -> {
                             tickCounter.incrementAndGet();
                             return null;
                         }).when(mock).incrementTick();
                     });
             MockedConstruction<StatusSystem> mockedStatus = mockConstruction(StatusSystem.class,
                     (mock, ctx) -> when(mock.update(any())).thenReturn(List.of()));
             MockedConstruction<ActionSystem> mockedAction = mockConstruction(ActionSystem.class,
                     (mock, ctx) -> when(mock.update(any())).thenReturn(List.of()));
             MockedConstruction<EndSystem> mockedEnd = mockConstruction(EndSystem.class,
                     (mock, ctx) -> {
                         // ne finit jamais tout seul : seule la limite de ticks doit arreter la boucle
                         when(mock.isFinished()).thenReturn(false);
                         when(mock.update(any())).thenReturn(List.of());
                     });
             MockedStatic<UnitDTOMapper> mockedMapper = mockStatic(UnitDTOMapper.class);
             MockedConstruction<FightingResultDTO> mockedResult = mockConstruction(FightingResultDTO.class)) {

            mockedMapper.when(() -> UnitDTOMapper.componentToDTO(any())).thenReturn(mock(ComponentUnitDTO.class));

            FightingService service = new FightingService(1L, teamA, teamB);
            FightingResultDTO result = service.simulate();

            assertNotNull(result);
            FightingContext context = mockedContext.constructed().get(0);
            // MAXTICKS = 1000
            verify(context, times(1000)).incrementTick();
        }
    }

    @Test
    public void simulate_pvLostAreZero_whenNoAliveUnitsRemain() {
        Team teamA = mockTeamWithUnits(1L, 3, 1);
        Team teamB = mockTeamWithUnits(2L, 3, 1);

        List<Object> resultArgs = new ArrayList<>();

        try (MockedConstruction<ComponentUnit> mockedComponentUnit = mockConstruction(ComponentUnit.class);
             MockedConstruction<FightingContext> mockedContext = mockConstruction(FightingContext.class,
                     (mock, ctx) -> {
                         when(mock.getAliveUnits()).thenReturn(List.of());
                         when(mock.getTick()).thenReturn(0);
                     });
             MockedConstruction<StatusSystem> mockedStatus = mockConstruction(StatusSystem.class,
                     (mock, ctx) -> when(mock.update(any())).thenReturn(List.of()));
             MockedConstruction<ActionSystem> mockedAction = mockConstruction(ActionSystem.class,
                     (mock, ctx) -> when(mock.update(any())).thenReturn(List.of()));
             MockedConstruction<EndSystem> mockedEnd = mockConstruction(EndSystem.class,
                     (mock, ctx) -> {
                         when(mock.isFinished()).thenReturn(true);
                         when(mock.update(any())).thenReturn(List.of());
                     });
             MockedStatic<UnitDTOMapper> mockedMapper = mockStatic(UnitDTOMapper.class);
             MockedConstruction<FightingResultDTO> mockedResult = mockConstruction(FightingResultDTO.class,
                     (mock, ctx) -> resultArgs.addAll(ctx.arguments()))) {

            mockedMapper.when(() -> UnitDTOMapper.componentToDTO(any())).thenReturn(mock(ComponentUnitDTO.class));

            FightingService service = new FightingService(1L, teamA, teamB);
            service.simulate();

            // args: initialState, events, finalState, teamAId, pvLostTeamA, teamBId, pvLostTeamB
            assertEquals(1L, resultArgs.get(3));
            assertEquals(0, resultArgs.get(4));
            assertEquals(2L, resultArgs.get(5));
            assertEquals(0, resultArgs.get(6));
        }
    }

    @Test
    public void simulate_aliveUnitsFromTeamA_increasePvLostTeamBWithLevelBonus() {
        Team teamA = mockTeamWithUnits(1L, 3, 1);
        Team teamB = mockTeamWithUnits(2L, 3, 1);

        ComponentUnit aliveFromTeamA = mock(ComponentUnit.class);
        when(aliveFromTeamA.getTeam()).thenReturn(teamA);

        List<Object> resultArgs = new ArrayList<>();

        try (MockedConstruction<ComponentUnit> mockedComponentUnit = mockConstruction(ComponentUnit.class);
             MockedConstruction<FightingContext> mockedContext = mockConstruction(FightingContext.class,
                     (mock, ctx) -> {
                         when(mock.getAliveUnits()).thenReturn(List.of(aliveFromTeamA));
                         when(mock.getTick()).thenReturn(0);
                     });
             MockedConstruction<StatusSystem> mockedStatus = mockConstruction(StatusSystem.class,
                     (mock, ctx) -> when(mock.update(any())).thenReturn(List.of()));
             MockedConstruction<ActionSystem> mockedAction = mockConstruction(ActionSystem.class,
                     (mock, ctx) -> when(mock.update(any())).thenReturn(List.of()));
             MockedConstruction<EndSystem> mockedEnd = mockConstruction(EndSystem.class,
                     (mock, ctx) -> {
                         when(mock.isFinished()).thenReturn(true);
                         when(mock.update(any())).thenReturn(List.of());
                     });
             MockedStatic<UnitDTOMapper> mockedMapper = mockStatic(UnitDTOMapper.class);
             MockedConstruction<FightingResultDTO> mockedResult = mockConstruction(FightingResultDTO.class,
                     (mock, ctx) -> resultArgs.addAll(ctx.arguments()))) {

            mockedMapper.when(() -> UnitDTOMapper.componentToDTO(any())).thenReturn(mock(ComponentUnitDTO.class));

            FightingService service = new FightingService(1L, teamA, teamB);
            service.simulate();

            // 1 survivant cote teamA => pvLostTeamB = 1 + teamA.getLvl()*2 = 1 + 6 = 7
            assertEquals(0, resultArgs.get(4)); // pvLostTeamA
            assertEquals(7, resultArgs.get(6)); // pvLostTeamB
        }
    }

    @Test
    public void simulate_aliveUnitsFromTeamB_increasePvLostTeamAWithLevelBonus() {
        Team teamA = mockTeamWithUnits(1L, 3, 1);
        Team teamB = mockTeamWithUnits(2L, 3, 1);

        ComponentUnit aliveFromTeamB = mock(ComponentUnit.class);
        when(aliveFromTeamB.getTeam()).thenReturn(teamB);

        List<Object> resultArgs = new ArrayList<>();

        try (MockedConstruction<ComponentUnit> mockedComponentUnit = mockConstruction(ComponentUnit.class);
             MockedConstruction<FightingContext> mockedContext = mockConstruction(FightingContext.class,
                     (mock, ctx) -> {
                         when(mock.getAliveUnits()).thenReturn(List.of(aliveFromTeamB));
                         when(mock.getTick()).thenReturn(0);
                     });
             MockedConstruction<StatusSystem> mockedStatus = mockConstruction(StatusSystem.class,
                     (mock, ctx) -> when(mock.update(any())).thenReturn(List.of()));
             MockedConstruction<ActionSystem> mockedAction = mockConstruction(ActionSystem.class,
                     (mock, ctx) -> when(mock.update(any())).thenReturn(List.of()));
             MockedConstruction<EndSystem> mockedEnd = mockConstruction(EndSystem.class,
                     (mock, ctx) -> {
                         when(mock.isFinished()).thenReturn(true);
                         when(mock.update(any())).thenReturn(List.of());
                     });
             MockedStatic<UnitDTOMapper> mockedMapper = mockStatic(UnitDTOMapper.class);
             MockedConstruction<FightingResultDTO> mockedResult = mockConstruction(FightingResultDTO.class,
                     (mock, ctx) -> resultArgs.addAll(ctx.arguments()))) {

            mockedMapper.when(() -> UnitDTOMapper.componentToDTO(any())).thenReturn(mock(ComponentUnitDTO.class));

            FightingService service = new FightingService(1L, teamA, teamB);
            service.simulate();

            // 1 survivant cote teamB => pvLostTeamA = 1 + teamA.getLvl()*2 = 1 + 6 = 7
            // NB: le code source utilise bien teamA.getLvl() pour les 2 bonus (comportement d'origine reproduit tel quel).
            assertEquals(7, resultArgs.get(4)); // pvLostTeamA
            assertEquals(0, resultArgs.get(6)); // pvLostTeamB
        }
    }

    @Test
    public void simulate_survivorsOnBothSides_increaseBothPvLostCounters() {
        Team teamA = mockTeamWithUnits(1L, 2, 1);
        Team teamB = mockTeamWithUnits(2L, 2, 1);

        ComponentUnit aliveFromTeamA = mock(ComponentUnit.class);
        when(aliveFromTeamA.getTeam()).thenReturn(teamA);
        ComponentUnit aliveFromTeamB = mock(ComponentUnit.class);
        when(aliveFromTeamB.getTeam()).thenReturn(teamB);

        List<Object> resultArgs = new ArrayList<>();

        try (MockedConstruction<ComponentUnit> mockedComponentUnit = mockConstruction(ComponentUnit.class);
             MockedConstruction<FightingContext> mockedContext = mockConstruction(FightingContext.class,
                     (mock, ctx) -> {
                         when(mock.getAliveUnits()).thenReturn(List.of(aliveFromTeamA, aliveFromTeamB));
                         when(mock.getTick()).thenReturn(0);
                     });
             MockedConstruction<StatusSystem> mockedStatus = mockConstruction(StatusSystem.class,
                     (mock, ctx) -> when(mock.update(any())).thenReturn(List.of()));
             MockedConstruction<ActionSystem> mockedAction = mockConstruction(ActionSystem.class,
                     (mock, ctx) -> when(mock.update(any())).thenReturn(List.of()));
             MockedConstruction<EndSystem> mockedEnd = mockConstruction(EndSystem.class,
                     (mock, ctx) -> {
                         when(mock.isFinished()).thenReturn(true);
                         when(mock.update(any())).thenReturn(List.of());
                     });
             MockedStatic<UnitDTOMapper> mockedMapper = mockStatic(UnitDTOMapper.class);
             MockedConstruction<FightingResultDTO> mockedResult = mockConstruction(FightingResultDTO.class,
                     (mock, ctx) -> resultArgs.addAll(ctx.arguments()))) {

            mockedMapper.when(() -> UnitDTOMapper.componentToDTO(any())).thenReturn(mock(ComponentUnitDTO.class));

            FightingService service = new FightingService(1L, teamA, teamB);
            service.simulate();

            // pvLostTeamA = 1 + teamA.getLvl()*2 = 1 + 4 = 5 ; pvLostTeamB = 1 + 4 = 5
            assertEquals(5, resultArgs.get(4));
            assertEquals(5, resultArgs.get(6));
        }
    }
}