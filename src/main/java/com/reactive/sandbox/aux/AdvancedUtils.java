package com.reactive.sandbox.aux;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class AdvancedUtils {

    public static CompletableFuture<String> createCompletableFuture() {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        return CompletableFuture.supplyAsync(() -> {
            System.out.println("Running in: " + Thread.currentThread().getName());
            return "Hi";
        }, executor);
    }

    public static Supplier<Publisher<String>> createPublisherSupplier() {
        return () -> Flux.just("Current time: " + System.currentTimeMillis());
    }
}
