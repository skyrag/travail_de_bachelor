package model.service.fightingService.system;

import model.DTO.fighting.AttackDTO;
import model.DTO.fighting.DeathDTO;
import model.DTO.fighting.FightingEventDTO;
import model.DTO.fighting.MoveToDTO;
import model.service.fightingService.ComponentUnit;
import model.service.fightingService.FightingContext;
import model.utils.Tuple;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * NOTE IMPORTANTE : castAbility(...) parcourt
 * "ability.getStrategie().findTarget(caster, context)", mais le type exact
 * retourne par AbilityFragment#getStrategie() (l'interface/classe portant
 * findTarget) n'a pas ete fourni. Pour ne pas ecrire un mock qui ne
 * compilerait pas ou qui serait incorrect, les tests ci-dessous couvrent :
 *  - l'integralite du dispatch de update() (isFullMana() -> castAbility,
 *    sinon -> basicAttack), avec une liste de capacites VIDE pour l'unite
 *    en pleine mana (ce qui couvre deja l'appel a caster.getAbility() et
 *    caster.resetMana() sans dependre du type inconnu),
 *  - l'integralite de basicAttack(...) (toutes les branches : en/hors de
 *    portee, canAttack() true/false, critique ou non, cible qui meurt ou
 *    survit).
 * Pour couvrir les boucles internes de castAbility (fragment -> cible ->
 * effet, ajout de DeathDTO si la cible meurt, increment d'abilityUUID),
 * partagez la classe portant findTarget(caster, context) ainsi que la
 * classe Effect utilisee, et je completerai ces tests.
 */
public class TestActionSystem {

    // -----------------------------------------------------------
    // update() : dispatch
    // -----------------------------------------------------------

    @Test
    void update_fullManaUnit_delegatesToCastAbilityAndResetsMana() {
        FightingContext context = mock(FightingContext.class);
        ComponentUnit caster = mock(ComponentUnit.class);
        when(caster.isFullMana()).thenReturn(true);
        when(caster.getAbility()).thenReturn(List.of()); // aucune capacite -> pas de sous-boucle
        when(context.getAliveUnits()).thenReturn(List.of(caster));
        when(context.getTick()).thenReturn(1);

        ActionSystem system = new ActionSystem();
        List<FightingEventDTO> events = system.update(context);

        assertTrue(events.isEmpty());
        verify(caster, times(1)).getAbility();
        verify(caster, times(1)).resetMana();
        verify(context, never()).getCurrentTarget(caster);
    }

    @Test
    void update_nonFullManaUnit_delegatesToBasicAttack() {
        FightingContext context = mock(FightingContext.class);
        ComponentUnit attacker = mock(ComponentUnit.class);
        when(attacker.isFullMana()).thenReturn(false);
        when(attacker.getRange()).thenReturn(1);

        Tuple attackerPos = mock(Tuple.class);
        when(attacker.getCurrentPosition()).thenReturn(attackerPos);

        ComponentUnit enemy = mock(ComponentUnit.class);
        Tuple enemyPos = mock(Tuple.class);
        when(enemy.getCurrentPosition()).thenReturn(enemyPos);

        when(context.getAliveUnits()).thenReturn(List.of(attacker));
        when(context.getCurrentTarget(attacker)).thenReturn(enemy);
        when(attackerPos.distanceFrom(enemyPos)).thenReturn(999.0); // hors de portee
        Tuple move = mock(Tuple.class);
        when(context.getNextMove(attacker)).thenReturn(move);
        when(context.getTick()).thenReturn(2);

        try (MockedConstruction<MoveToDTO> mockedMove = mockConstruction(MoveToDTO.class)) {
            ActionSystem system = new ActionSystem();
            List<FightingEventDTO> events = system.update(context);

            assertEquals(1, events.size());
            verify(attacker, never()).getAbility();
            verify(context).move(attackerPos, move);
            verify(attacker).setCurrentPosition(move);
        }
    }

    // -----------------------------------------------------------
    // basicAttack()
    // -----------------------------------------------------------

    private ComponentUnit mockAttacker(int range, Tuple position) {
        ComponentUnit attacker = mock(ComponentUnit.class);
        when(attacker.getRange()).thenReturn(range);
        when(attacker.getCurrentPosition()).thenReturn(position);
        when(attacker.getId()).thenReturn(100L);
        return attacker;
    }

    private ComponentUnit mockEnemy(Tuple position) {
        ComponentUnit enemy = mock(ComponentUnit.class);
        when(enemy.getCurrentPosition()).thenReturn(position);
        when(enemy.getId()).thenReturn(200L);
        return enemy;
    }

    @Test
    void basicAttack_inRangeAndCanAttack_noCrit_enemySurvives_addsAttackEventOnly() {
        FightingContext context = mock(FightingContext.class);
        Tuple attackerPos = mock(Tuple.class);
        Tuple enemyPos = mock(Tuple.class);

        ComponentUnit attacker = mockAttacker(5, attackerPos);
        when(attacker.isFullMana()).thenReturn(false);
        when(attacker.canAttack()).thenReturn(true);
        when(attacker.getDamage()).thenReturn(10);
        when(attacker.getCrit()).thenReturn(25);

        ComponentUnit enemy = mockEnemy(enemyPos);
        when(enemy.damagePhysic(10)).thenReturn(8);
        when(enemy.isAlive()).thenReturn(true);

        when(attackerPos.distanceFrom(enemyPos)).thenReturn(3.0); // 5*sqrt(2) ~= 7.07 -> a portee
        when(context.getAliveUnits()).thenReturn(List.of(attacker));
        when(context.getCurrentTarget(attacker)).thenReturn(enemy);
        when(context.randomInt(100)).thenReturn(50); // >= 25 -> pas de critique
        when(context.getTick()).thenReturn(9);

        List<Object> attackArgs = new ArrayList<>();
        try (MockedConstruction<AttackDTO> mockedAttack = mockConstruction(AttackDTO.class,
                (mock, ctx) -> attackArgs.addAll(ctx.arguments()));
             MockedConstruction<DeathDTO> mockedDeath = mockConstruction(DeathDTO.class)) {

            ActionSystem system = new ActionSystem();
            List<FightingEventDTO> events = system.update(context);

            assertEquals(1, events.size());
            assertEquals(0, mockedDeath.constructed().size());
            verify(enemy).damagePhysic(10);
            verify(attacker).addMana(10);

            assertEquals(9L, attackArgs.get(0));
            assertEquals(100L, attackArgs.get(1));
            assertEquals(200L, attackArgs.get(2));
            assertEquals(8, attackArgs.get(3));
            assertEquals(false, attackArgs.get(4));
        }
    }

    @Test
    void basicAttack_criticalHit_doublesDamageBeforeApplyingIt() {
        FightingContext context = mock(FightingContext.class);
        Tuple attackerPos = mock(Tuple.class);
        Tuple enemyPos = mock(Tuple.class);

        ComponentUnit attacker = mockAttacker(5, attackerPos);
        when(attacker.isFullMana()).thenReturn(false);
        when(attacker.canAttack()).thenReturn(true);
        when(attacker.getDamage()).thenReturn(10);
        when(attacker.getCrit()).thenReturn(25);

        ComponentUnit enemy = mockEnemy(enemyPos);
        when(enemy.damagePhysic(20)).thenReturn(15); // 10 double -> 20
        when(enemy.isAlive()).thenReturn(true);

        when(attackerPos.distanceFrom(enemyPos)).thenReturn(3.0);
        when(context.getAliveUnits()).thenReturn(List.of(attacker));
        when(context.getCurrentTarget(attacker)).thenReturn(enemy);
        when(context.randomInt(100)).thenReturn(10); // < 25 -> critique
        when(context.getTick()).thenReturn(1);

        List<Object> attackArgs = new ArrayList<>();
        try (MockedConstruction<AttackDTO> mockedAttack = mockConstruction(AttackDTO.class,
                (mock, ctx) -> attackArgs.addAll(ctx.arguments()))) {

            ActionSystem system = new ActionSystem();
            system.update(context);

            verify(enemy).damagePhysic(20);
            assertEquals(15, attackArgs.get(3));
            assertEquals(true, attackArgs.get(4));
        }
    }

    @Test
    void basicAttack_enemyDies_addsDeathEventInAdditionToAttackEvent() {
        FightingContext context = mock(FightingContext.class);
        Tuple attackerPos = mock(Tuple.class);
        Tuple enemyPos = mock(Tuple.class);

        ComponentUnit attacker = mockAttacker(5, attackerPos);
        when(attacker.isFullMana()).thenReturn(false);
        when(attacker.canAttack()).thenReturn(true);
        when(attacker.getDamage()).thenReturn(50);
        when(attacker.getCrit()).thenReturn(25);

        ComponentUnit enemy = mockEnemy(enemyPos);
        when(enemy.damagePhysic(50)).thenReturn(50);
        when(enemy.isAlive()).thenReturn(false); // meurt sous le coup

        when(attackerPos.distanceFrom(enemyPos)).thenReturn(3.0);
        when(context.getAliveUnits()).thenReturn(List.of(attacker));
        when(context.getCurrentTarget(attacker)).thenReturn(enemy);
        when(context.randomInt(100)).thenReturn(99); // pas de critique
        when(context.getTick()).thenReturn(4);

        List<Object> deathArgs = new ArrayList<>();
        try (MockedConstruction<AttackDTO> mockedAttack = mockConstruction(AttackDTO.class);
             MockedConstruction<DeathDTO> mockedDeath = mockConstruction(DeathDTO.class,
                     (mock, ctx) -> deathArgs.addAll(ctx.arguments()))) {

            ActionSystem system = new ActionSystem();
            List<FightingEventDTO> events = system.update(context);

            assertEquals(2, events.size()); // DeathDTO + AttackDTO
            assertEquals(4L, deathArgs.get(0));
            assertEquals(200L, deathArgs.get(1));
        }
    }

    @Test
    void basicAttack_outOfRange_movesTowardTargetInsteadOfAttacking() {
        FightingContext context = mock(FightingContext.class);
        Tuple attackerPos = mock(Tuple.class);
        Tuple enemyPos = mock(Tuple.class);

        ComponentUnit attacker = mockAttacker(1, attackerPos); // portee*sqrt(2) ~= 1.41
        when(attacker.isFullMana()).thenReturn(false);

        ComponentUnit enemy = mockEnemy(enemyPos);

        when(attackerPos.distanceFrom(enemyPos)).thenReturn(50.0); // largement hors de portee
        when(context.getAliveUnits()).thenReturn(List.of(attacker));
        when(context.getCurrentTarget(attacker)).thenReturn(enemy);
        when(context.getTick()).thenReturn(6);

        Tuple move = mock(Tuple.class);
        when(move.x()).thenReturn(3);
        when(move.y()).thenReturn(4);
        when(context.getNextMove(attacker)).thenReturn(move);

        List<Object> moveArgs = new ArrayList<>();
        try (MockedConstruction<MoveToDTO> mockedMove = mockConstruction(MoveToDTO.class,
                (mock, ctx) -> moveArgs.addAll(ctx.arguments()))) {

            ActionSystem system = new ActionSystem();
            List<FightingEventDTO> events = system.update(context);

            assertEquals(1, events.size());
            verify(context).move(attackerPos, move);
            verify(attacker).setCurrentPosition(move);
            verify(attacker, never()).canAttack();

            assertEquals(6L, moveArgs.get(0));
            assertEquals(100L, moveArgs.get(1));
            assertEquals(3, moveArgs.get(2));
            assertEquals(4, moveArgs.get(3));
        }
    }

    @Test
    void basicAttack_inRangeButCannotAttackYet_movesInsteadOfAttacking() {
        FightingContext context = mock(FightingContext.class);
        Tuple attackerPos = mock(Tuple.class);
        Tuple enemyPos = mock(Tuple.class);

        ComponentUnit attacker = mockAttacker(5, attackerPos);
        when(attacker.isFullMana()).thenReturn(false);
        when(attacker.canAttack()).thenReturn(false); // en cooldown

        ComponentUnit enemy = mockEnemy(enemyPos);

        when(attackerPos.distanceFrom(enemyPos)).thenReturn(3.0); // a portee, mais canAttack() = false
        when(context.getAliveUnits()).thenReturn(List.of(attacker));
        when(context.getCurrentTarget(attacker)).thenReturn(enemy);
        when(context.getTick()).thenReturn(0);

        Tuple move = mock(Tuple.class);
        when(context.getNextMove(attacker)).thenReturn(move);

        try (MockedConstruction<MoveToDTO> mockedMove = mockConstruction(MoveToDTO.class)) {
            ActionSystem system = new ActionSystem();
            List<FightingEventDTO> events = system.update(context);

            assertEquals(1, events.size());
            verify(enemy, never()).damagePhysic(anyInt());
            verify(context).move(attackerPos, move);
        }
    }
}