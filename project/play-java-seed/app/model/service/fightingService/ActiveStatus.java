package model.service.fightingService;

public class ActiveStatus {
    private final StatusType type;
    private int remainingDuration;

    public ActiveStatus(StatusType type, int duration) {
        this.type = type;
        this.remainingDuration = duration;
    }

    public StatusType getType() { return type; }

    public boolean tick() {
        remainingDuration--;
        return remainingDuration <= 0;
    }

    public void refresh(int value){
        this.remainingDuration = value;
    }
}
