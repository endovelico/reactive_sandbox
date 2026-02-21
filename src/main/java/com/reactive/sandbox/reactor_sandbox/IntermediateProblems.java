package com.reactive.sandbox.reactor_sandbox;

import com.reactive.sandbox.aux.AuxMethods;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static com.reactive.sandbox.aux.AuxMethods.*;

public class IntermediateProblems {

    public static void main(String[] args) throws InterruptedException {

        publishOnSlowconsumer();
        propagateExample();
        flatMapOnErrorResume();
        reactorTryCatchFinally();
        simulateAPICall();
        //hookStuff();
        //switchIfEmpty();
        //flatFlatMapFlux();
        /*iterableSquared();
        AnotherFlatMapSquared();
        squaredMono();
        backpressureExample();
        chatMessageLogger();
        stockPriceTicker();
        videoFrameProcessor();
        iotWaterLevelMonitor();
        parallelSquareCalculator();
        singleSchedulerExample();
        immediateExecutionLogger();
        simulatedApiCallWithErrorHandling();
        errorContinueExample();
        flakyApiCalls();
        hotVsCold();
        hotColdNewsFeed();*/
    }

    private static void publishOnSlowconsumer() throws InterruptedException {

        Flux<Integer> fastPublisher = Flux.range(1, 5);

        // Change the threading context and apply the slowConsumer method
        Flux<Integer> processedFlux = fastPublisher
                .publishOn(Schedulers.single())
                .map(AuxMethods::slowConsumer);

        // Subscribe and print the emitted elements
        processedFlux.subscribe(value ->
                System.out.println("Processed value: " + value)
        );

        Thread.sleep(15000);
    }

    private static void propagateExample() {

        Flux<String> stringFlux = Flux.just("1", "2", "three", "4", "5");

        stringFlux.map(stringNumber -> {
                    try {
                        // Try parsing the string into an integer
                        return Integer.parseInt(stringNumber);
                    } catch (NumberFormatException e) {
                        // Propagate the exception as a RuntimeException
                        throw Exceptions.propagate(e);
                    }
                })
                .subscribe(
                        number -> System.out.println("Parsed: " + number),
                        error -> System.out.println("Error: " + error.getMessage())
                );
    }

    private static void flatMapOnErrorResume() {

        String fallbackUrl = "fallbackUrl";

        Flux.just("url1", "url2", "url3", "url4")
                .flatMap(url -> Mono.fromCallable(() -> fetchData(url)) // fetch JSON data
                        .map(json -> parseJson(json))                     // parse JSON
                        .onErrorResume(e -> Mono.fromCallable(() -> fetchData(fallbackUrl)) // fallback
                                .map(json -> parseJson(json))
                        )
                )
                .subscribe(System.out::println);
    }

    private static void reactorTryCatchFinally() {

        List<Integer> inputList = Arrays.asList(1, 2, -3, 4);

        Flux<Integer> numbersFlux = Flux.fromIterable(inputList)
                .map(num -> {
                    // Equivalent of the try block logic
                    if (num < 0) {
                        throw new IllegalArgumentException("Negative numbers are not allowed.");
                    }
                    return num * 2;
                })
                .doOnError(e -> {
                    // Equivalent of the catch block
                    System.out.println("Error: " + e.getMessage());
                })
                .doFinally(signal -> {
                    // Equivalent of the finally block
                    System.out.println("Finished processing the list.");
                });

        // Subscribe to print the doubled numbers
        numbersFlux.subscribe(
                value -> System.out.println(value),
                error -> {
                    // Empty error consumer so that the exception stack trace is not printed again
                }
        );
    }

    private static void simulateAPICall() {

    }

    private static void hookStuff() {

        Flux<Double> stockPrices = Flux.just(120.0, 140.0, 130.0, 110.0, 150.0);

        stockPrices
                // Print when the subscription starts
                .doOnSubscribe(subscription -> System.out.println("Subscription started."))
                // Print before the first item is emitted
                .doFirst(() -> System.out.println("First stock price incoming..."))
                // Print each price
                .doOnNext(price -> System.out.println("Price: " + price))
                // Print when the Flux completes
                .doOnComplete(() -> System.out.println("All stock prices processed."))
                // Subscribe to trigger the pipeline
                .subscribe();
    }

    private static void switchIfEmpty() {

        Flux<String> stockSymbols = Flux.empty(); // Empty Flux
        Flux<String> defaultSymbols = Flux.just("AAPL", "GOOG", "MSFT", "AMZN", "FB");

        // Use defaultSymbols if stockSymbols is empty
        Flux<String> result = stockSymbols.switchIfEmpty(defaultSymbols);

        // Subscribe and print each item
        result.subscribe(
                symbol -> System.out.println("Symbol: " + symbol),
                error -> System.err.println("Error: " + error.getMessage()),
                () -> System.out.println("Completed")
        );
    }

    private static void flatFlatMapFlux() {

        // Step 2: Create a Flux emitting numbers from 1 to 5
        Flux<Integer> numbersFlux = Flux.range(1, 5);

        // Step 3: Use map to triple each emitted value
        Flux<Integer> tripledFlux = numbersFlux.map(value -> value * 3);

        // Step 4: Use flatMap to emit the tripled value and its square
        Flux<Integer> resultFlux = tripledFlux.flatMap(AuxMethods::getValueAndSquare);

        // Step 5: Subscribe and print each emitted item
        resultFlux.subscribe(System.out::println);
    }

    private static void iterableSquared() {

        // Create a Flux emitting numbers from 1 to 5
        Flux<Integer> numbersFlux = Flux.range(1, 5);

        // Transform each number using flatMapIterable to get number and its square
        Flux<Integer> resultFlux = numbersFlux.flatMapIterable(AuxMethods::getNumberAndSquareIterable);

        // Subscribe and print each emitted item
        resultFlux.subscribe(System.out::println);
    }

    private static void AnotherFlatMapSquared() {

        // Step 2: Create a Flux emitting numbers from 1 to 5
        Flux<Integer> numbersFlux = Flux.range(1, 5);

        // Step 4: Transform each emitted value to its number and square
        Flux<Integer> resultFlux = numbersFlux.flatMap(AuxMethods::getNumberAndSquare);

        // Subscribe and print each emitted item
        resultFlux.subscribe(System.out::println);
    }

    private static void squaredMono() {

        Mono<Integer> monoValue = Mono.just(5);

        // Step 4: Transform the emitted value asynchronously
        Mono<Integer> squaredMono = monoValue.flatMap(AuxMethods::getSquareAsync);

        // Step 5: Subscribe and print the result
        squaredMono.subscribe(result -> System.out.println("Squared value: " + result));

    }

    private static void hotColdNewsFeed() throws InterruptedException {

        System.out.println("=== Cold News Feed ===");

        Flux<String> coldFeed = Flux.interval(Duration.ofMillis(300))
                .take(5)
                .map(i -> "Headline " + (i + 1));

        // Subscriber A subscribes immediately
        coldFeed.subscribe(h -> System.out.println("[Cold Subscriber A] Received: " + h));

        // Subscriber B subscribes 900ms later
        Thread.sleep(900);
        coldFeed.subscribe(h -> System.out.println("[Cold Subscriber B] Received: " + h));

        Thread.sleep(1500); // wait for cold feed to finish

        System.out.println("\n=== Hot News Feed ===");

        Flux<String> hotFeed = Flux.interval(Duration.ofMillis(300))
                .take(5)
                .map(i -> "Headline " + (i + 1))
                .publish()
                .refCount(1); // make it hot

        // Subscriber C subscribes immediately
        hotFeed.subscribe(h -> System.out.println("[Hot Subscriber C] Received: " + h));

        // Subscriber D subscribes 900ms later
        Thread.sleep(900);
        hotFeed.subscribe(h -> System.out.println("[Hot Subscriber D] Received: " + h));

        Thread.sleep(1500); // wait for hot feed to finish
    }

    private static void hotVsCold() throws InterruptedException {

        System.out.println("=== Cold Publisher ===");

        // Cold Flux: emits every 500ms
        Flux<Long> coldSensor = Flux.interval(Duration.ofMillis(500))
                .take(5); // only 5 readings

        // Subscriber A subscribes immediately
        coldSensor.subscribe(i -> System.out.println("[Cold Subscriber A] Reading: " + i));

        // Subscriber B subscribes after 1 second
        Thread.sleep(1000);
        coldSensor.subscribe(i -> System.out.println("[Cold Subscriber B] Reading: " + i));

        Thread.sleep(3500); // wait for cold flux to complete

        System.out.println("\n=== Hot Publisher ===");

        // Hot Flux: shared emissions
        Flux<Long> hotSensor = Flux.interval(Duration.ofMillis(500))
                .take(5)
                .publish()       // make it hot
                .refCount(1);   // start emitting when first subscriber connects

        // Subscriber C subscribes immediately
        hotSensor.subscribe(i -> System.out.println("[Hot Subscriber C] Reading: " + i));

        // Subscriber D subscribes after 1 second
        Thread.sleep(1000);
        hotSensor.subscribe(i -> System.out.println("[Hot Subscriber D] Reading: " + i));

        Thread.sleep(3000); // wait for hot flux to complete
    }

    private static void flakyApiCalls() {

        Flux<String> userFlux = Flux.range(1, 5)
                .map(id -> {
                    if (id == 3) {
                        throw new RuntimeException("API failure for user " + id);
                    }
                    return "User " + id;
                })
                .doOnNext(user -> System.out.println("[Upstream] Processing: " + user))
                // Use onErrorResume to switch to a fallback Flux
                .onErrorResume(e -> {
                    System.out.println("[Error] Caught: " + e.getMessage() + " → switching to fallback");
                    return Flux.just("Fallback User 3A", "Fallback User 3B");
                })
                // Log downstream emissions
                .doOnNext(user -> System.out.println("[Downstream] Produced: " + user));

        userFlux.subscribe(
                user -> System.out.println("[Subscriber] Received: " + user),
                err -> System.err.println("[Subscriber] Error: " + err),
                () -> System.out.println("[Subscriber] Completed!")
        );
    }

    private static void errorContinueExample() {

        Flux<Integer> sensorFlux = Flux.range(1, 6)
                .map(reading -> {
                    if (reading == 3) {
                        throw new RuntimeException("Sensor failure at reading " + reading);
                    }
                    return reading * 10; // Simulate temperature processing
                })
                .doOnNext(reading -> System.out.println("[Upstream] Reading: " + reading))
                // Skip failed items and continue
                .onErrorContinue((throwable, reading) ->
                        System.out.println("[Error] Skipping reading " + reading + " due to: " + throwable.getMessage())
                );

        sensorFlux.subscribe(
                reading -> System.out.println("[Subscriber] Received: " + reading),
                err -> System.err.println("[Subscriber] Error: " + err),
                () -> System.out.println("[Subscriber] Completed!")
        );
    }

    private static void simulatedApiCallWithErrorHandling() {

        Flux<String> userFlux = Flux.range(1, 6)
                .map(id -> {
                    if (id == 4) {
                        throw new RuntimeException("Failed to fetch user " + id);
                    }
                    return "User " + id;
                })
                .doOnNext(user -> System.out.println("[Upstream] " + user))
                // Handle error by returning a single default value
                .onErrorReturn("Unknown User")
                // Alternative: handle error by switching to another Flux
                //.onErrorResume(e -> Flux.just("Fallback User 1", "Fallback User 2"))
                ;

        userFlux.subscribe(
                user -> System.out.println("[Subscriber] Received: " + user),
                err -> System.err.println("[Subscriber] Error: " + err),
                () -> System.out.println("[Subscriber] Completed!")
        );
    }

    private static void immediateExecutionLogger() {

        Flux.range(1, 5)
                // Log upstream emission
                .doOnNext(n -> System.out.println("[Upstream] Number: " + n + " | Thread: " + Thread.currentThread().getName()))
                // Use immediate scheduler (no thread switch)
                .publishOn(Schedulers.immediate())
                // Map to processed string
                .map(n -> "Processed " + n)
                // Log downstream processing
                .doOnNext(s -> System.out.println("[Downstream] " + s + " | Thread: " + Thread.currentThread().getName()))
                // Subscribe and print results
                .subscribe(result -> System.out.println("[Subscriber] Received: " + result + " | Thread: " + Thread.currentThread().getName()));

    }

    private static void singleSchedulerExample() throws InterruptedException {


        Flux.range(1, 5)
                // Log upstream emission
                .doOnNext(n -> System.out.println("[Upstream] Number: " + n + " | Thread: " + Thread.currentThread().getName()))
                // Switch downstream execution to single scheduler
                .publishOn(Schedulers.single())
                // Map each number to a processed string
                .map(n -> "Task " + n + " processed")
                // Log downstream processing
                .doOnNext(task -> System.out.println("[Downstream] " + task + " | Thread: " + Thread.currentThread().getName()))
                // Subscribe and print results
                .subscribe(result -> System.out.println("[Subscriber] Received: " + result + " | Thread: " + Thread.currentThread().getName()));

        // Keep main thread alive for processing
        Thread.sleep(2000);
    }

    private static void parallelSquareCalculator() throws InterruptedException {

        Flux.range(1, 6)
                // Log numbers upstream before scheduling
                .doOnNext(n -> System.out.println("[Upstream] Number: " + n + " | Thread: " + Thread.currentThread().getName()))
                // Switch downstream execution to parallel scheduler
                .publishOn(Schedulers.parallel())
                // Calculate square downstream
                .map(n -> n * n)
                // Log squared numbers with thread name
                .doOnNext(squared -> System.out.println("[Downstream] Squared: " + squared + " | Thread: " + Thread.currentThread().getName()))
                // Subscribe and print results
                .subscribe(result -> System.out.println("[Subscriber] Received: " + result + " | Thread: " + Thread.currentThread().getName()));

        // Keep main thread alive long enough for all processing
        Thread.sleep(2000);
    }

    private static void iotWaterLevelMonitor() throws InterruptedException {

        // Simulate fast upstream of water level readings
        Flux<Integer> waterLevelFlux = Flux.range(50, 12) // 50..61
                .delayElements(Duration.ofMillis(40)) // fast upstream: emits every 40ms
                .doOnNext(level -> System.out.println("[Upstream emitted] " + level))
                // Drop readings if subscriber is slow
                .onBackpressureDrop(droppedLevel -> System.out.println("[Dropped due to backpressure] " + droppedLevel));

        // Slow subscriber: processes each reading every 120ms
        waterLevelFlux
                .publishOn(Schedulers.boundedElastic()) // subscriber runs on separate thread
                .subscribe(
                        level -> {
                            System.out.println("[Subscriber received] " + level);
                            try {
                                Thread.sleep(120); // simulate slow processing
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        },
                        err -> System.err.println("Error: " + err),
                        () -> System.out.println("All water level readings processed!")
                );

        // Keep main thread alive long enough for processing
        Thread.sleep(5000);
    }

    private static void videoFrameProcessor() throws InterruptedException {

        // Simulate a fast upstream of video frames
        Flux<String> frameFlux = Flux.range(1, 15)
                .map(i -> "Frame " + i)
                .delayElements(Duration.ofMillis(30)) // fast upstream: emits every 30ms
                .doOnNext(frame -> System.out.println("[Upstream emitted] " + frame))
                // Buffer up to 5 frames; drop excess and log them
                .onBackpressureBuffer(
                        5,
                        dropped -> System.out.println("[Buffer overflow, dropped] " + dropped)
                );

        // Slow subscriber: processes 1 frame every 100ms
        frameFlux
                .publishOn(Schedulers.boundedElastic()) // subscriber on separate thread
                .subscribe(
                        frame -> {
                            System.out.println("[Subscriber received] " + frame);
                            try {
                                Thread.sleep(100); // simulate slow processing
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        },
                        err -> System.err.println("Error: " + err),
                        () -> System.out.println("All frames processed!")
                );

        // Keep main thread alive long enough for processing
        Thread.sleep(5000);
    }

    private static void stockPriceTicker() throws InterruptedException {


        // Simulate a fast upstream of stock prices
        Flux<Integer> stockFlux = Flux.range(100, 20) // 100..119
                .delayElements(Duration.ofMillis(50)) // fast upstream: every 50ms
                .doOnNext(price -> System.out.println("[Upstream emitted] " + price))
                // Backpressure strategy: keep only the latest unprocessed item
                .onBackpressureLatest();

        // Slow subscriber: processes each price every 150ms
        stockFlux
                .publishOn(Schedulers.boundedElastic()) // process asynchronously
                .subscribe(
                        price -> {
                            System.out.println("[Subscriber received] " + price);
                            try {
                                Thread.sleep(150); // simulate slow processing
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        },
                        err -> System.err.println("Error: " + err),
                        () -> System.out.println("All stock prices processed!")
                );

        // Keep main thread alive for the Flux to finish
        Thread.sleep(5000);
    }

    private static void chatMessageLogger() throws InterruptedException {

        // Simulate fast incoming chat messages
        Flux<String> chatFlux = Flux.range(1, 15)
                .map(i -> "Message " + i)
                .delayElements(Duration.ofMillis(50))
                .doOnNext(message -> System.out.println("[Upstream emitted] " + message))
                .onBackpressureBuffer(5, dropped -> System.out.println("[Buffer overflow, dropped] " + dropped));

        // Slow subscriber: logs messages every 200ms
        chatFlux
                .publishOn(Schedulers.boundedElastic())
                .subscribe(
                        msg -> {
                            System.out.println("[Subscriber received] " + msg);
                            try {
                                Thread.sleep(200); // simulate slow logging
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        },
                        err -> System.err.println("Error: " + err),
                        () -> System.out.println("All messages processed!")
                );

        // Keep main thread alive long enough for all processing
        Thread.sleep(5000);

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
