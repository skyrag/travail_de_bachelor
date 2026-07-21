package model.DTO.fighting;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AttackDTO.class, name = "attack"),
        @JsonSubTypes.Type(value = AbilityCastEvent.class, name = "abilityCast"),
        @JsonSubTypes.Type(value = EffectAppliedEventDTO.class, name = "effectApplied"),
        @JsonSubTypes.Type(value = MoveToDTO.class, name = "moveTo"),
        @JsonSubTypes.Type(value = DeathDTO.class, name = "death"),
        @JsonSubTypes.Type(value = CombatEndDTO.class, name = "combatEnd")
})
public sealed interface FightingEventDTO permits AttackDTO, MoveToDTO, DeathDTO, CombatEndDTO, EffectAppliedEventDTO, AbilityCastEvent {}


