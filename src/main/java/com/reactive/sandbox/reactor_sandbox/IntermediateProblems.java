package com.reactive.sandbox.reactor_sandbox;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

public class IntermediateProblems {

    public static void main(String[] args) throws InterruptedException {

        backpressureExample();
        chatMessageLogger();
    }

    private static void chatMessageLogger() {

        
    }

    private static void backpressureExample() throws InterruptedException {

        // Simulate a fast stream of readings
        Flux<Integer> sensorFlux = Flux.range(20, 20)
                .delayElements(Duration.ofMillis(50))
                .doOnNext(temp -> System.out.println("[Upstream emitted] " + temp))
                .onBackpressureBuffer(
                        5, // max buffer size
                        dropped -> System.out.println("[Buffer overflow, dropped] " + dropped) // overflow handler
                );


        // Slow subscriber: processes 300ms per item
        sensorFlux
                .publishOn(Schedulers.boundedElastic()) // make subscriber async
                .subscribe(
                        temp -> {
                            System.out.println("[Subscriber received] " + temp);
                            try {
                                Thread.sleep(300); // simulate slow processing
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        },
                        err -> System.err.println("Error: " + err),
                        () -> System.out.println("All sensor readings processed!")
                );

        // Sleep main thread long enough for all items to be processed
        Thread.sleep(10000);


    }
}
