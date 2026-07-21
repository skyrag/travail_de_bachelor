package model.DTO;

import java.util.List;

public record ItemDTO(long id, String name, String description, List<StatChangingEffectDTO> effects) {
}
