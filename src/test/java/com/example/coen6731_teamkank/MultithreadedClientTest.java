package com.example.coen6731_teamkank;


import com.example.coen6731_teamkank.service.SkierService;
import com.google.gson.Gson;
import com.opencsv.CSVWriter;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.RestTemplate;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@SpringBootTest
@Import(MockMvcConfig.class)
class MultithreadedClientTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private SkierService skierService;

    private Gson gson = new Gson();

    private RestTemplate restTemplate = new RestTemplate();
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String CloudPostURL = "http://155.248.238.80:9090/skierevent";
    private static final String postURL = "http://localhost:9090/skierevent";
    private static final int MAX_QUEUE_SIZE = 10000;
    private BlockingQueue<String> liftRideEventQueue = new ArrayBlockingQueue<>(MAX_QUEUE_SIZE);

    @Test
    void createSkierEvent() throws Exception {

        String sampleSkierEventJson = gson.toJson(skierService.generateEvent());

       String url = "/skierevent";
        MockHttpServletResponse response = postCall(url, sampleSkierEventJson);

        logger.info("Json input: " + sampleSkierEventJson);
        logger.info(response.getContentAsString());
        logger.info(String.valueOf(response.getStatus()));
    }

    @Test
    public void test500PostRequests() {
        ApiResponse responses = new ApiResponse();
        List<ApiResponse> responseList = new ArrayList<>();
        String skierEventJson = gson.toJson(skierService.generateEvent());
        long startTime = System.currentTimeMillis();
        //Sending 500 requests from a single thread
        for(int i=0; i<=500; i++) {
            responses =  sendPostRequestNew(skierEventJson);
            responseList.add(responses);
        }
        long endTime = System.currentTimeMillis();
        long latency = endTime-startTime;
        Double numberOfRequestsPerSecond = (responseList.size()/(double) latency)*1000;
        logger.info(" Time taken for 500 requests to complete " + latency);
        logger.info(" Total Duration in Requests per second " + numberOfRequestsPerSecond);

    }

    //Multithreading concurrency test
    @Test
    void testPostRequests() throws InterruptedException {

        int totalPosts = 10000;
        int threadsToStart = 32;
        int postsPerThread = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(threadsToStart);

        int numEventsGenerated = 0;
        while (numEventsGenerated < totalPosts) {

            String event = gson.toJson(skierService.generateEvent());
            try {
                //Adding event details to liftRideEventQueue
                liftRideEventQueue.put(event);
                numEventsGenerated++;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
            // Start 32 threads, each sending 1000 POST requests
            List<Future<List<ApiResponse>>> futures = new ArrayList<>();
            //Recording timestamp before the start time of 32 threads
            long startTime = System.currentTimeMillis();
            for (int i = 0; i < threadsToStart; i++) {

                Future<List<ApiResponse>> future = executor.submit(() -> {
                    int numPostsSent = 0;
                    List<ApiResponse> responses = new ArrayList<>();
                    //check for liftRideEventQueue to not be empty and thread size less than 1000
                    while (!liftRideEventQueue.isEmpty() && numPostsSent < postsPerThread) {
                        //decrementing liftRideEventQueue size
                        String liftEvent = liftRideEventQueue.poll();
                        if (liftEvent != null) {
                            //hit the POST request
                            responses.add(sendPostRequestNew(liftEvent));
                            numPostsSent++;
                        }
                    }
                    return responses;
                });
                futures.add(future);
            }

            // Wait for all threads to complete
            List<ApiResponse> allResponses = new ArrayList<>();

            for (Future<List<ApiResponse>> future : futures) {
                try {
                    allResponses.addAll(future.get());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }

        //Recording timestamp after the end of 32 threads
        long endTime = System.currentTimeMillis();
            // Print all response
        List<String> responses = allResponses.stream().map(ApiResponse::getResponse).toList();
        List<String> statuses = allResponses.stream().map(ApiResponse::getStatus).toList();
        List<Long> latencies = allResponses.stream().map(ApiResponse::getLatency).toList();
        List<Long> startTimes = allResponses.stream().map(ApiResponse::getStartTime).toList();

            for (String status : statuses) {
                System.out.println(status);
            }


        Integer successCount = statuses.stream().filter(resp -> resp.equals(HttpStatus.CREATED.toString())).toList().size();
        Long wallTime = endTime - startTime;
        Double totalDurationInRequestsPerSecond = (allResponses.size()/(double) wallTime)*totalPosts;

        // Printing response on Completion of al 10K requests
        logger.info("Number of successful requests sent " + successCount);
        logger.info("Number of unsuccessful requests sent " + (totalPosts - successCount));
        logger.info("Wall time " + wallTime+ " ms");
        logger.info("Total duration in requestsPerSecond " + String.format("%.3f",totalDurationInRequestsPerSecond));

        // Recording details of Profiling Performance
        createCSVFile(latencies,startTimes,responses,statuses);
        profilingPerformance(latencies);

        executor.shutdown();
    }

    private ApiResponse sendPostRequestNew(String event) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(event, headers);
        long startTime = System.currentTimeMillis();
        ResponseEntity<String> response = restTemplate.exchange(
                postURL, HttpMethod.POST, entity, String.class);
        long endtime = System.currentTimeMillis();
        long latency = endtime - startTime;
        return new ApiResponse(response.getBody(),latency,response.getStatusCode().toString(),startTime);
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

    public void createCSVFile(List<Long> latencies, List<Long> startTimes, List<String> responses, List<String> statuses){
        String fileName = "Profiling Performance.csv";

        try (FileWriter fileWriter = new FileWriter(fileName);
             CSVWriter csvWriter = new CSVWriter(fileWriter)) {

            // Write headers
            csvWriter.writeNext(new String[] {"Start Time (Timestamp)", "Request Type", "Latency(ms)", "Response Code"});

            // Write data rows
            for (int i = 0; i < responses.size(); i++) {
                String[] rowData = new String[] {
                        startTimes.get(i).toString(),
                        "POST",
                        latencies.get(i).toString(),
                        statuses.get(i)};
                csvWriter.writeNext(rowData);
            }
        } catch (IOException e) {
            e.printStackTrace();
          }
    }

    private void profilingPerformance(List<Long> latencies) {

        List<Long> latencyList = latencies.stream().sorted().collect(Collectors.toList());
        double sum = latencies.stream().mapToLong(Long::longValue).sum();
        logger.info("Mean response time is " + sum/latencies.size());

        logger.info("Median response time is " + latencyList.get((latencies.size()/2)-1));

        logger.info("p99 response time is "+latencyList.get((int) (latencies.size()*0.99)));

        logger.info("Min response time is " + Collections.min(latencies));

        logger.info("Max response time is " +Collections.max(latencies));
    }

    public class ApiResponse {
        private String response;
        private long latency;
        private String status;
        private long startTime;

        public ApiResponse() {
        }
        public ApiResponse(String response, long latency, String status, long startTime) {
            this.response = response;
            this.latency = latency;
            this.status = status;
            this.startTime = startTime;
        }
        public String getResponse() {
            return response;
        }
        public long getLatency() {
            return latency;
        }
        public String getStatus() {
            return status;
        }
        public long getStartTime() {
            return startTime;
        }
    }
}