package com.example.coen6731_teamkank.SkierClientInvocation;

import com.example.coen6731_teamkank.service.SkierService;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

    public class SkierClientInvocation {
        private Gson gson = new Gson();

        private RestTemplate restTemplate = new RestTemplate();
        protected final Logger logger = LoggerFactory.getLogger(this.getClass());
        private static final String localPostURL = "http://localhost:9090/coen6731/skierevent";
        private static final String postURL = "http://155.248.238.80:9090/coen6731/skierevent";

        public void skierClientInvocationOfPostRequests() {
            String skierEventJson = gson.toJson(new SkierService().generateEvent());
            double startTime = System.nanoTime();
            //Sending single request
            sendPostRequestNew(skierEventJson);
            double endTime = System.nanoTime();
            double latency = endTime-startTime;
            logger.info("* ON COMPLETION OF A SINGLE POST REQUEST-----------------------");
            logger.info("1. Time taken for a request to complete " + latency/1000000 + " ms");

    }

        public void skierClientInvocationOf500PostRequests() {
            Integer totalNumberOfRequests = 500;
            String skierEventJson = gson.toJson(new SkierService().generateEvent());
            double startTime = System.currentTimeMillis();
            //Sending 500 requests from a single thread
            for(int i=0; i<=500; i++) {
                sendPostRequestNew(skierEventJson);
            }
            double endTime = System.currentTimeMillis();
            double latency = endTime-startTime;
            Double numberOfRequestsPerSecond = (totalNumberOfRequests/latency)*1000;
            logger.info("* ON COMPLETION OF 500 POST REQUESTS------------------------");
            logger.info("1. Time taken for 500 requests to complete " + latency + " ms");
            logger.info("2. Total Throughput in Requests per second " + String.format("%.3f",numberOfRequestsPerSecond));
            logger.info(" ");

        }

        private void sendPostRequestNew(String event) {

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(event, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                postURL, HttpMethod.POST, entity, String.class);
        }
    }
