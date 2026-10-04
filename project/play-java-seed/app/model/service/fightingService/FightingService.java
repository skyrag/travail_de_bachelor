package model.service.fightingService;

import model.DTO.ComponentUnitDTO;
import model.DTO.UnitDTOMapper;
import model.DTO.fighting.FightingEventDTO;
import model.DTO.fighting.FightingResultDTO;
import model.entities.Team;
import model.entities.unit.InstanceUnit;
import model.service.fightingService.system.ActionSystem;
import model.service.fightingService.system.EndSystem;
import model.service.fightingService.system.StatusSystem;
import org.apache.pekko.japi.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class FightingService {

    private final FightingContext context;
    private final StatusSystem statusSystem;
    private final ActionSystem actionSystem;
    private final EndSystem endSystem;
    private final List<ComponentUnitDTO> initialState;
    private final List<FightingEventDTO> events = new ArrayList<>();
    private Team teamA;
    private Team teamB;

    private final int MAXTICKS = 1000;

    public FightingService(long seed, Team teamA, Team teamB) {
        this.teamA = teamA;
        this.teamB = teamB;

        System.out.println("a");

        List<ComponentUnit> teamAUnits = new ArrayList<>();
        List<ComponentUnit> teamBUnits = new ArrayList<>();

        System.out.println("aa");

        teamAUnits.addAll(teamA.getUnits().stream()
                .filter(u -> !Team.isBench(u.getPos()))
                .map(ComponentUnit::new).toList());
        teamBUnits.addAll(teamB.getUnits().stream()
                .filter(u -> !Team.isBench(u.getPos()))
                .map(ComponentUnit::new).toList());

        System.out.println("aaa");

        this.context = new FightingContext(new Pair<>(teamAUnits, teamA.getId()), new Pair<>(teamBUnits, teamB.getId()), new Random(seed));
        System.out.println("1");

        this.statusSystem = new StatusSystem();
        System.out.println("2");

        this.actionSystem = new ActionSystem();
        System.out.println("3");

        this.endSystem = new EndSystem();
        System.out.println("4");

        this.initialState = context.getAliveUnits().stream().map(UnitDTOMapper::componentToDTO).toList();
        System.out.println("on a construit le combat");

    }

    public FightingResultDTO simulate(){
        while (!endSystem.isFinished() && context.getTick() < MAXTICKS){
            System.out.println("ca fight");
            events.addAll(endSystem.update(context));
            events.addAll(statusSystem.update(context));
            events.addAll(actionSystem.update(context));
            context.incrementTick();
        }

        System.out.println("fin de la boucle");

        int pvLostTeamA = 0;
        int pvLostTeamB = 0;
        for (ComponentUnit unit : context.getAliveUnits()){
            if (Objects.equals(unit.getTeam().getId(), teamA.getId())){
                pvLostTeamB++;
            } else {
                pvLostTeamA++;
            }
        }
        if (pvLostTeamA != 0){
            pvLostTeamA += teamB.getLvl() * 2;
        }
        if (pvLostTeamB != 0){
            pvLostTeamB += teamA.getLvl() * 2;
        }

        System.out.println("fin de la simu du comabt");

        return new FightingResultDTO(initialState, events, context.getAliveUnits().stream().map(UnitDTOMapper::componentToDTO).toList(), teamA.getId(), pvLostTeamA, teamB.getId(), pvLostTeamB);

    }
}
