package com.example.coen6731_teamkank;

import com.google.gson.Gson;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class Controller {

    private Gson gson = new Gson();



    @GetMapping(value = "/index")
    public Skier index(){
        Skier skier = new EventGenerator().generateEvent();
        return skier;
    }

    @PostMapping(value = "/skierevent")
    public String createSkierEvent(@RequestBody Skier s){

        //String skierJson = this.gson.toJson(s);

        if(s.getSkierId()<=0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ID");
        }

        return "Skier Id is "+s.getSkierId();
    }
}
