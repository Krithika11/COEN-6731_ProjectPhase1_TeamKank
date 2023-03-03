package com.example.coen6731_teamkank.SkierMultithreadedClient;

public class ApiResponse {
        private String response;
        private Double latency;
        private String status;
        private Double startTime;

        public ApiResponse() {
        }
        public ApiResponse(String response, Double latency, String status, Double startTime) {
            this.response = response;
            this.latency = latency;
            this.status = status;
            this.startTime = startTime;
        }
        public String getResponse() {
            return response;
        }
        public Double getLatency() {
            return latency;
        }
        public String getStatus() {
            return status;
        }
        public Double getStartTime() {
            return startTime;
        }
    }

