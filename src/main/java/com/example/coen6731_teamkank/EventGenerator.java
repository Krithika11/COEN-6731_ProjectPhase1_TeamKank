package com.example.coen6731_teamkank;

import java.util.Random;

public class EventGenerator {

    Random random = new Random();

    public Skier generateEvent(){
        return new Skier(random.nextInt(100000-1)+1,random.nextInt(10-1)+1,random.nextInt(40-1)+1,2022,random.nextInt(30-1)+1);
    }


}
