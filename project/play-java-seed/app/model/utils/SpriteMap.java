package model.utils;

import java.util.ArrayList;
import java.util.List;

public class SpriteMap {

    private static SpriteMap singleton;
    private List<Long> key = new ArrayList<>();
    private List<String> sprite = new ArrayList<>();

    private SpriteMap(){
        //standard geralt
        key.add(1L);
        sprite.add("geralt");
    }

    public String getSprite(long spriteKey){
        return sprite.get(key.indexOf(spriteKey));
    }

    public static SpriteMap getSingleton(){
        if (singleton == null) {
            singleton = new SpriteMap();
        }
        return singleton;
    }
}
