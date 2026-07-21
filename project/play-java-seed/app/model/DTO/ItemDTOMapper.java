package model.DTO;

import model.entities.effect.StatChangingEffect;
import model.entities.unit.Item;

public class ItemDTOMapper {

    public static ItemDTO itemToDTO(Item item){
        return new ItemDTO(item.getId(),
                item.getName(),
                item.getDescription(),
                item.getEffects().stream().map(ItemDTOMapper::changingEffectToDTO).toList()
                );
    }

    public static StatChangingEffectDTO changingEffectToDTO (StatChangingEffect effect){
        return new StatChangingEffectDTO(effect.getTypeChange().toString(), effect.getValue());
    }
}
