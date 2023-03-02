package com.example.coen6731_teamkank.controller;

import com.example.coen6731_teamkank.model.Skier;
import com.example.coen6731_teamkank.service.SkierService;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
public class SkierController {

    @Autowired
    private SkierService skierService;

    private Gson gson = new Gson();
    private RestTemplate restTemplate = new RestTemplate();


    @PostMapping(value = "/skierevent", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public String createSkierEvent(@RequestBody Skier skierDetails) {
        String postResopnse = "";
        if(!(skierDetails == null)) {
             postResopnse = skierService.createSkierEventDetails(skierDetails);
        }

        return postResopnse;
    }
}
