package com.reactive.sandbox.aux;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public class AuxMethods {

    public static boolean isPrime(int n) {
        if (n <= 1) return false;

        return Flux.range(2, n - 2)
                .all(i -> n % i != 0)
                .block(); // ⚠️ We'll fix this below
    }


    public static Mono<Integer> getSquareAsync(Integer value) {
        // Create a Mono that computes the square on a separate thread asynchronously
        return Mono.fromCallable(() -> value * value)
                .subscribeOn(Schedulers.boundedElastic()); // runs on a thread pool suitable for blocking/async work
    }


    // Step 3: Method that returns a Flux emitting the number and its square
    public static Flux<Integer> getNumberAndSquare(Integer value) {
        return Flux.just(value, value * value);
    }

    // Method that returns an Iterable with the number and its square
    public static Iterable<Integer> getNumberAndSquareIterable(Integer value) {
        return Arrays.asList(value, value * value);
    }

    // Method that returns a Flux emitting the value and its square
    public static Flux<Integer> getValueAndSquare(Integer value) {
        return Flux.just(value, value * value);
    }

    // Simulates an external API call to get historical stock prices
    public static Mono<List<Double>> fetchHistoricalPrices(String stockSymbol) {
        int c = stockSymbol.charAt(0);
        return Mono.just(Arrays.asList(c*10.0, c*20.0, c*30.0))
                .delayElement(Duration.ofMillis(1000));
    }

    // Simulates an external API call to get the latest stock price
    public static Mono<Double> fetchLatestPrice(String stockSymbol) {
        int c = stockSymbol.charAt(0);
        return Mono.just(c * 10.0)
                .delayElement(Duration.ofMillis(935 + c)); // variable delay
    }
}
