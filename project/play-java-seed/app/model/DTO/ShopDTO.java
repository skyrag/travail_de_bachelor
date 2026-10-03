package model.DTO;

import model.entities.unit.Unit;

import java.util.List;

public record ShopDTO (String slot1, String slot2, String slot3, String slot4, String slot5){


    public static ShopDTO from(List<Unit> shop) {
        if (shop.size() != 5) {
            throw new IllegalArgumentException("Shop must contain exactly 5 units, got " + shop.size());
        }
        return new ShopDTO(
                shop.get(0).getName(),
                shop.get(1).getName(),
                shop.get(2).getName(),
                shop.get(3).getName(),
                shop.get(4).getName()
        );
    }
}
