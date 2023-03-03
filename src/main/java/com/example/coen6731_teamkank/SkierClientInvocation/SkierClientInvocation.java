package com.example.coen6731_teamkank.SkierClientInvocation;

import com.example.coen6731_teamkank.SkierMultithreadedClient.ApiResponse;
import com.example.coen6731_teamkank.service.SkierService;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

    public class SkierClientInvocation {
        private Gson gson = new Gson();

        private RestTemplate restTemplate = new RestTemplate();
        protected final Logger logger = LoggerFactory.getLogger(this.getClass());
        private static final String postURL = "http://localhost:9090/skierevent";

        public void skierClientInvocationOfPostRequests() {
            ApiResponse responses = new ApiResponse();
            String skierEventJson = gson.toJson(new SkierService().generateEvent());
            double startTime = System.currentTimeMillis();
            //Sending single request
            responses =  sendPostRequestNew(skierEventJson);
            double endTime = System.currentTimeMillis();
            double latency = endTime-startTime;
            logger.info("* ON COMPLETION OF A SINGLE POST REQUEST-----------------------");
            logger.info("1. Time taken for a request to complete " + latency/1000 + " second");

    }

        public void skierClientInvocationOf500PostRequests() {
            ApiResponse responses = new ApiResponse();
            List<ApiResponse> responseList = new ArrayList<>();
            String skierEventJson = gson.toJson(new SkierService().generateEvent());
            double startTime = System.currentTimeMillis();
            //Sending 500 requests from a single thread
            for(int i=0; i<=500; i++) {
                responses =  sendPostRequestNew(skierEventJson);
                responseList.add(responses);
            }
            double endTime = System.currentTimeMillis();
            double latency = endTime-startTime;
            Double numberOfRequestsPerSecond = (responseList.size()/latency)*1000;
            logger.info("* ON COMPLETION OF 500 POST REQUESTS------------------------");
            logger.info("1. Time taken for 500 requests to complete " + latency/1000 + " seconds");
            logger.info("2. Total Throughput in Requests per second " + numberOfRequestsPerSecond);
            logger.info(" ");

        }

        private ApiResponse sendPostRequestNew(String event) {

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(event, headers);
            Double startTime = Double.parseDouble(String.valueOf(System.currentTimeMillis()));

            ResponseEntity<String> response = restTemplate.exchange(
                postURL, HttpMethod.POST, entity, String.class);
            Double endTime = Double.parseDouble(String.valueOf(System.currentTimeMillis()));
            Double latency = endTime - startTime;
            return new ApiResponse(response.getBody(),latency,response.getStatusCode().toString(),startTime);
        }
    }
