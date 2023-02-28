package com.example.coen6731_teamkank;


import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.*;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(SpringExtension.class)
@WebMvcTest(Controller.class)
class ControllerTest {


    @Autowired
    private MockMvc mvc;

    private Gson gson = new Gson();

    private RestTemplate restTemplate = new RestTemplate();

    @Test
    void createSkierEvent() throws Exception {

        String sampleSkierEventJson = gson.toJson(new EventGenerator().generateEvent());

        Skier s = gson.fromJson(sampleSkierEventJson, Skier.class);
        s.setSkierId(0);

        String errorSkierEventJson = gson.toJson(s);

        String url = "/skierevent";

        MockHttpServletResponse response = postCall(url, sampleSkierEventJson);


        System.out.println("Json input: " + sampleSkierEventJson);
        System.out.println(response.getContentAsString());
        System.out.println(response.getStatus());
        System.out.println("Hello");
    }

    public MockHttpServletResponse postCall(String url, String skierEvent) throws Exception {
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post(url)
                .accept(MediaType.APPLICATION_JSON)
                .content(skierEvent)
                .contentType(MediaType.APPLICATION_JSON);

        MvcResult result = mvc.perform(requestBuilder).andReturn();

        return result.getResponse();
    }

    //Multithreading concurrency test

        @Test
        public void testSendingPosts() throws InterruptedException {
            AtomicInteger counter = new AtomicInteger(0);

            // Create a thread pool with 32 threads
            ExecutorService threadPool = Executors.newFixedThreadPool(32);

            // Create 32 threads that each send 1000 POST requests and terminate
            for (int i = 0; i < 32; i++) {
                threadPool.submit(() -> {
                    for (int j = 0; j < 1000; j++) {
                        ResponseEntity<String> response = sendPostRequest(counter);
                        System.out.println(response.getBody());
                    }
                });
            }

            // Wait until the first 1000 POST requests are sent
            while (counter.get() < 1000) {
                Thread.sleep(100);
            }

            // Send additional POST requests until a total of 10K requests are sent
            while (counter.get() < 10000) {
                threadPool.submit(() -> {
                    ResponseEntity<String> response = sendPostRequest(counter);
                    System.out.println(response.getBody());
                });
            }

            // Shutdown the thread pool once all requests are sent
            threadPool.shutdown();
        }

        private ResponseEntity<String> sendPostRequest(AtomicInteger counter) {

        String postURL = "http://localhost:8080/skierevent/";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String sampleSkierRequestBody = gson.toJson(new EventGenerator().generateEvent());
            HttpEntity<String> entity = new HttpEntity<>(sampleSkierRequestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    postURL, HttpMethod.POST, entity, String.class);

            // Increment the counter once the request is sent
            counter.incrementAndGet();

            return response;
        }

    }