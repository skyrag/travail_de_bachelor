package model.fightingDTO;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = TargetAcquiredDTO.class, name = "targetAcquired"),
        @JsonSubTypes.Type(value = TargetLostDTO.class, name = "targetLost"),
        @JsonSubTypes.Type(value = AttackDTO.class, name = "attack"),
        @JsonSubTypes.Type(value = BuffAppliedDTO.class, name = "buffApplied"),
        @JsonSubTypes.Type(value = MoveToDTO.class, name = "moveTo"),
        @JsonSubTypes.Type(value = DeathDTO.class, name = "death"),
        @JsonSubTypes.Type(value = CombatEndDTO.class, name = "combatEnd")
})
public sealed interface FightingEventDTO permits TargetAcquiredDTO, TargetLostDTO, AttackDTO, BuffAppliedDTO, MoveToDTO, DeathDTO, CombatEndDTO {}


