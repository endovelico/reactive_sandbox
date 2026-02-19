package com.reactive.sandbox.reactor_sandbox;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Random;

public class BeginnerProblems {

    public static void main(String[] args) {

        //createMonoAndPrintValue();
        //emitsNumbers1to5();
        //filterAndSquareNumbers();
        //transformeNameWithFlatMap();
        //asyncTransformationWithOrderDifference();
        //filterAndTransformStrings();
        addGreetingToListOfName();

    }

    private static void addGreetingToListOfName() {

        Mono<String> greeting = Mono.just("Hello");
        Flux<String> names = Flux.just("Alice", "Bob", "Charlie");

        
    }

    private static void filterAndTransformStrings() {

        Flux<String> names = Flux.just("Alice", "Bob", "Charlie", "David");

        names.filter(name -> name.startsWith("C"))  // Keep only names starting with "C"
                .map(String::toUpperCase)              // Convert to uppercase
                .subscribe(System.out::println);       // Print each result
    }

    private static void asyncTransformationWithOrderDifference() {

        Flux<String> names = Flux.just("Alice", "Bob", "Charlie");
        Random random = new Random();

        System.out.println("=== Using flatMap (may be out of order) ===");
        names.flatMap(name ->
                        Mono.just(name.toUpperCase())
                                .delayElement(Duration.ofMillis(random.nextInt(200)))
                )
                .subscribe(System.out::println);

        // Small sleep to separate outputs
        try { Thread.sleep(1000); } catch (InterruptedException e) { }

        System.out.println("=== Using concatMap (guaranteed order) ===");
        names.concatMap(name ->
                        Mono.just(name.toUpperCase())
                                .delayElement(Duration.ofMillis(random.nextInt(200)))
                )
                .subscribe(System.out::println);

        // Wait for all async tasks to finish
        try { Thread.sleep(1000); } catch (InterruptedException e) { }
    }


    private static void transformeNameWithFlatMap() {

        Flux<String> names = Flux.just("Alice", "Bob", "Charlie");

        names.flatMap(name -> Mono.just(name.toUpperCase())) // transform each name asynchronously
                .subscribe(System.out::println);
    }

    private static void filterAndSquareNumbers() {

        Flux.range(1, 10)             // Emit numbers 1 to 10
                .filter(n -> n % 2 != 0)  // Keep only odd numbers
                .map(n -> n * n)          // Square each remaining number
                .subscribe(System.out::println); // Print each result
    }

    private static void createMonoAndPrintValue() {

        Mono<String> mono = Mono.just("Hello, Reactor");
        mono.subscribe(System.out::println);
    }

    private static void emitsNumbers1to5() {

        Flux<Integer> flux = Flux.range(1, 5);
        flux.subscribe(System.out::println);
    }
}
