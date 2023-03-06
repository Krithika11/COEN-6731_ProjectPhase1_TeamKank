package com.example.coen6731_teamkank;

import com.example.coen6731_teamkank.SkierMultithreadedClient.MultithreadedSkierClient;
import com.example.coen6731_teamkank.SkierClientInvocation.SkierClientInvocation;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Coen6731TeamKankApplication {

    public static void main(String[] args) throws InterruptedException {

        SpringApplication.run(Coen6731TeamKankApplication.class, args);
        
        MultithreadedSkierClient multithreadedSkierClient = new MultithreadedSkierClient();
        SkierClientInvocation skierClientInvocation = new SkierClientInvocation();
        multithreadedSkierClient.testMultithreadedClientFor10KPostRequests();
        skierClientInvocation.skierClientInvocationOf500PostRequests();
        skierClientInvocation.skierClientInvocationOfPostRequests();
    }

}
