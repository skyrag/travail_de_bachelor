package model.service.fightingService.system;

import model.DTO.fighting.FightingEventDTO;
import model.service.fightingService.ComponentUnit;
import model.service.fightingService.FightingContext;
import org.junit.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TestStatusSystem {

    @Test
    public void update_returnsEmptyList_whenNoAliveUnits() {
        FightingContext context = mock(FightingContext.class);
        when(context.getAliveUnits()).thenReturn(List.of());

        StatusSystem system = new StatusSystem();
        List<FightingEventDTO> events = system.update(context);

        assertNotNull(events);
        assertTrue(events.isEmpty());
    }

    @Test
    public void update_ticksStatusesOnEveryAliveUnitAndReturnsEmptyList() {
        FightingContext context = mock(FightingContext.class);
        ComponentUnit unit1 = mock(ComponentUnit.class);
        ComponentUnit unit2 = mock(ComponentUnit.class);
        when(context.getAliveUnits()).thenReturn(List.of(unit1, unit2));

        StatusSystem system = new StatusSystem();
        List<FightingEventDTO> events = system.update(context);

        assertTrue(events.isEmpty());
        verify(unit1, times(1)).tickStatuses();
        verify(unit2, times(1)).tickStatuses();
    }
}