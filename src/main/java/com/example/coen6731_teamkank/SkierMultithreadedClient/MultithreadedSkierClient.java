package com.example.coen6731_teamkank.SkierMultithreadedClient;

import com.example.coen6731_teamkank.service.SkierService;
import com.google.gson.Gson;
import com.opencsv.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Import(SkierService.class)
public class MultithreadedSkierClient {

    @Autowired
    private SkierService skierService;

    private Gson gson = new Gson();

    private RestTemplate restTemplate = new RestTemplate();
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String CloudPostURL = "http://155.248.238.80:9090/skierevent";
    private static final String postURL = "http://localhost:9090/skierevent";
    private static final int MAX_QUEUE_SIZE = 10000;
    private BlockingQueue<String> liftRideEventQueue = new ArrayBlockingQueue<>(MAX_QUEUE_SIZE);

    public void testMultithreadedClientFor10KPostRequests() throws InterruptedException {
        int totalPosts = 10000;
        int threadsToStart = 32;
        int postsPerThread = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(threadsToStart);

        int numEventsGenerated = 0;
        while (numEventsGenerated < totalPosts) {

            String event = gson.toJson(new SkierService().generateEvent());
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
        double startTime = System.currentTimeMillis();
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
        double endTime = System.currentTimeMillis();
        // Print all response
        List<String> responses = allResponses.stream().map(ApiResponse::getResponse).toList();
        List<String> statuses = allResponses.stream().map(ApiResponse::getStatus).toList();
        List<Double> latencies = allResponses.stream().map(ApiResponse::getLatency).toList();
        List<Double> startTimes = allResponses.stream().map(ApiResponse::getStartTime).toList();

        for (String status : statuses) {
            logger.info(status);
            logger.info(" ");
        }

        int successCount = statuses.stream().filter(resp -> resp.equals(HttpStatus.CREATED.toString())).toList().size();
        double wallTime = endTime - startTime;
        Double totalThroughputInRequestsPerSecond = (allResponses.size() / wallTime) * 1000;

        // Printing response on Completion of al 10K requests
        logger.info("* ON COMPLETION OF 10K POST REQUESTS------------------------");
        logger.info("1. Total Number of POST Requests: " + statuses.size());
        logger.info("1. Number of successful requests sent: " + successCount);
        logger.info("2. Number of unsuccessful requests sent: " + (totalPosts - successCount));
        logger.info("3. Wall time: " + wallTime/1000 + "seconds");
        logger.info("4. Total throughput in requestsPerSecond: " + String.format("%.3f", totalThroughputInRequestsPerSecond));
        logger.info(" ");

        // Recording details of Profiling Performance
        logger.info("* PROFILING PERFORMANCE -------------------------------------");
        createCSVFile(latencies, startTimes, responses, statuses);
        profilingPerformance(latencies);
        logger.info(" ");

        executor.shutdown();
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
        return new ApiResponse(response.getBody(), latency, response.getStatusCode().toString(), startTime);
    }

    public void createCSVFile(List<Double> latencies, List<Double> startTimes, List<String> responses, List<String> statuses) {
        String fileName = "Profiling Performance.csv";

        try (FileWriter fileWriter = new FileWriter(fileName);
             CSVWriter csvWriter = new CSVWriter(fileWriter)) {

            // Write headers
            csvWriter.writeNext(new String[]{"Start Time (Timestamp)", "Request Type", "Latency(ms)", "Response Code"});

            // Write data rows
            for (int i = 0; i < responses.size(); i++) {
                String[] rowData = new String[]{
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

    private void profilingPerformance(List<Double> latencies) {

        List<Double> latencyList = latencies.stream().sorted().collect(Collectors.toList());
        double sum = latencies.stream().mapToDouble(Double::doubleValue).sum();
        logger.info("1. Mean response time is " + (sum / latencies.size())/1000 + " seconds");

        logger.info("2. Median response time is " + (latencyList.get((latencies.size() / 2) - 1))/1000 + " seconds");

        logger.info("3. p99 response time is " + latencyList.get((int) (latencies.size() * 0.99))/1000+ " seconds");

        logger.info("4. Min response time is " + Collections.min(latencies)/1000+ " seconds");

        logger.info("5. Max response time is " + Collections.max(latencies)/1000+ " seconds");
    }
}




