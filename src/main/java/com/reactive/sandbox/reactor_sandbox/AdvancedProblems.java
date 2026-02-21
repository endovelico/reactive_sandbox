package com.reactive.sandbox.reactor_sandbox;

import com.reactive.sandbox.aux.AuxMethods;
import com.reactive.sandbox.aux.CustomBaseSubscriber;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.context.Context;

import java.time.Duration;
import java.util.List;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static com.reactive.sandbox.aux.AdvancedUtils.createCompletableFuture;
import static com.reactive.sandbox.aux.AdvancedUtils.createPublisherSupplier;
import static com.reactive.sandbox.aux.AuxMethods.fetchLatestPrice;

public class AdvancedProblems {

    public static void main(String[] args) throws InterruptedException {

        latestStockStuff();
        historicalPriceData();
        processingWithContext();
        createAPublisherPartExample();
        completableFutureToFlux();
        supplierWithDefer();
        emitSimpleFlux();
        baseSubscriberExample();
    }

    private static void latestStockStuff() throws InterruptedException {

        Flux<String> stockSymbols = Flux.just("AAPL", "GOOG", "MSFT", "AMZN", "FB")
                .delayElements(Duration.ofSeconds(1));

        stockSymbols
                // switchMap cancels the previous API call if a new symbol is emitted
                .switchMap(symbol ->
                        fetchLatestPrice(symbol)
                                .map(price -> symbol + " -> " + price)
                )
                .subscribe(System.out::println);

        // Keep the app alive to see all emissions
        Thread.sleep(15000);

    }

    private static void historicalPriceData() throws InterruptedException {

        List<String> stockSymbols = (List<String>) Arrays.asList("AAPL", "GOOG", "MSFT", "AMZN", "FB");

        // Create a Flux from the stockSymbols list
        Flux.fromIterable(stockSymbols)
                        .concatMap(symbol ->
                                AuxMethods.fetchHistoricalPrices(symbol).map(prices -> symbol + " -> " + prices))
                .subscribe(System.out::println);


        Thread.sleep(5500);
    }

    private static void baseSubscriberExample() {

        // Create a simple Flux that emits values
        Flux<Integer> flux = Flux.just(1, 2, 3);


        // Create a custom BaseSubscriber
        CustomBaseSubscriber<Integer> customSubscriber =
                new CustomBaseSubscriber<>();

        // Subscribe using custom BaseSubscriber
        flux.subscribe(customSubscriber);
    }

    private static void emitSimpleFlux() {

        // Create a simple Flux that emits values
        Flux<Integer> flux = Flux.just(1, 2, 3);
    }

    private static void supplierWithDefer() {

        // Create the Supplier
        Supplier<Publisher<String>> supplier = createPublisherSupplier();

        // Create Flux using defer (lazy instantiation)
        Flux<String> flux = Flux.defer(supplier);

        // First subscription
        flux.subscribe(value -> System.out.println("First: " + value));

        // Small delay so timestamps differ
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Second subscription
        flux.subscribe(value -> System.out.println("Second: " + value));
    }

    private static void completableFutureToFlux() {

        CompletableFuture<String> future = createCompletableFuture();

        Mono<String> mono = Mono.fromFuture(future);

        // Subscribe and print the result
        mono.subscribe(
                value -> System.out.println("Received: " + value),
                error -> System.err.println("Error: " + error),
                () -> System.out.println("Completed")
        );

        // Prevent the program from exiting immediately
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void createAPublisherPartExample() {

        // Create a simple Publisher
        Publisher<Integer> publisher = createAPublisher();

        // Convert the Publisher to a Mono using fromDirect()
        Mono<Integer> mono = Mono.fromDirect(publisher);

        // Subscribe to the Mono
        mono.subscribe(
                value -> System.out.println("Received: " + value),
                error -> System.err.println("Error: " + error),
                () -> System.out.println("Completed")
        );
    }

    private static Flux<Integer> createAPublisher() {

        // Create a Flux publisher that emits two values
        return Flux.just(1, 2);
    }

    private static void processingWithContext() {

        String correlationId = "dassdsadas";
        Mono.deferContextual(ctxView -> {

                    String id = ctxView.get("correlationId");

                    return Mono.just("Processed with ID: " + id);
                })
                .publishOn(Schedulers.parallel())
                .contextWrite(Context.of("correlationId", correlationId));
    }
}
