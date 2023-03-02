package com.example.coen6731_teamkank.service;

import com.example.coen6731_teamkank.model.Skier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.temporal.ValueRange;
import java.util.Random;

@Service
public class SkierService {

    private final int maxRetryAttempts = 5;
    Random random = new Random();


    @Retryable(value = ResponseStatusException.class, maxAttempts = maxRetryAttempts, backoff = @Backoff(delay = 1000))
    public String createSkierEventDetails(Skier skierDetails) {
        String response = "";
        if (!isValidRange(skierDetails.getSkierId(), 1, 100000) ||
                !isValidRange(skierDetails.getResortId(), 1, 10) ||
                !isValidRange(skierDetails.getLiftId(), 1, 40) ||
                !isValidRange(skierDetails.getDayId(), 1, 30) ||
                !isValidRange(skierDetails.getTime(), 1, 360)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Input");
        }
        else response = "Skier details of Skier ID " + skierDetails.getSkierId() + " are successfully posted ";
        return response;

    }

    public Skier generateEvent(){
        return new Skier(random.nextInt(100000-1)+1,random.nextInt(10-1)+1,
                random.nextInt(40-1)+1,2022,random.nextInt(30-1)+1,
                random.nextInt(360-1)+1);
    }

    private boolean isValidRange(Integer number, Integer lowerBound, Integer upperBound) {
        final ValueRange range = ValueRange.of(lowerBound, upperBound);
        return range.isValidIntValue(number);
    }
}
