package com.example.coen6731_teamkank;

import lombok.Builder;

@Builder
public class Skier {
    private int skierId;
    private int resortId;
    private int liftId;
    private int seasonId;
    private int dayId;

    public Skier(int skierId, int resortId, int liftId, int seasonId, int dayId) {
        this.skierId = skierId;
        this.resortId = resortId;
        this.liftId = liftId;
        this.seasonId = seasonId;
        this.dayId = dayId;
    }

    public int getSkierId() {
        return skierId;
    }

    public void setSkierId(int skierId) {
        this.skierId = skierId;
    }

    public int getResortId() {
        return resortId;
    }

    public void setResortId(int resortId) {
        this.resortId = resortId;
    }

    public int getLiftId() {
        return liftId;
    }

    public void setLiftId(int liftId) {
        this.liftId = liftId;
    }

    public int getSeasonId() {
        return seasonId;
    }

    public void setSeasonId(int seasonId) {
        this.seasonId = seasonId;
    }

    public int getDayId() {
        return dayId;
    }

    public void setDayId(int dayId) {
        this.dayId = dayId;
    }
}
