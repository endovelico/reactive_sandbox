package com.reactive.sandbox.aux;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class AuxMethods {


    private static Flux<Integer> slowPublisher() {
        return Flux.create(sink -> {
            for (int i = 1; i <= 10; i++) {
                try {
                    // Simulate blocking IO with a 1-second delay
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sink.next(i);
            }
            sink.complete();
        });
    }

    public static void fastConsumer(Integer value) {
        System.out.println("Received: " + value);
    }

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

    public static List<String> simulateApiCall() {
        List<String> userNames = Arrays.asList("Alice", "Bob", "Carol", "David");
        Random random = new Random();
        if (random.nextBoolean()) {
            throw new RuntimeException("Remote API error");
        }
        return userNames;
    }

    public static String fetchData(String url) {
        // Simulates fetching JSON data from the URL
        return "{\"data\": \"" + url + "\"}";
    }

    public static String parseJson(String jsonString) {
        // Simulates parsing JSON and throwing an exception for invalid JSON
        if (jsonString.contains("url3")) {
            throw new RuntimeException("Invalid JSON");
        }
        return jsonString.toUpperCase();
    }


    public static Mono<Integer> processNumber(int number) {
        int doubled = number * 2;
        if (doubled % 4 == 0)
            return Mono.error(new IllegalArgumentException("Result is divisible by 4"));
        else
            return Mono.just(doubled);
    }

    public static int fetchAndCountWords(String url) {
        int random = (int)(Math.random() * 500 + 100);
        System.out.println("Word count for " + url + ": " + random);
        return random;
    }


    public static Integer slowConsumer(Integer value) {
        try {
            // Simulate a slow consumer by adding a 1-second delay
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return value;
    }
}
