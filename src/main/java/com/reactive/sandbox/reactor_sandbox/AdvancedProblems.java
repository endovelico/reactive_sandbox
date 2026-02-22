package com.reactive.sandbox.reactor_sandbox;

import com.reactive.sandbox.aux.AuxMethods;
import com.reactive.sandbox.aux.CustomBaseSubscriber;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;
import reactor.core.scheduler.Schedulers;
import reactor.util.context.Context;

import java.time.Duration;
import java.util.List;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.reactive.sandbox.aux.AdvancedUtils.createCompletableFuture;
import static com.reactive.sandbox.aux.AdvancedUtils.createPublisherSupplier;
import static com.reactive.sandbox.aux.AuxMethods.fetchLatestPrice;
import static com.reactive.sandbox.aux.AuxMethods.fetchUrlContent;

public class AdvancedProblems {

    public static void main(String[] args) throws InterruptedException {

        blockingNumberThree();
        blockingNumberTwo();
        blockingCallNumberOne();
        yetAnotherContext();
        anotherContextWrite();
        transformedDefferedcontextual();
        paralel4rails();
        paralelSchedudlersingleExample();
        slowPublisherBoundedElastic();
        webScrapperProblem();
        latestStockStuff();
        historicalPriceData();
        processingWithContext();
        createAPublisherPartExample();
        completableFutureToFlux();
        supplierWithDefer();
        emitSimpleFlux();
        baseSubscriberExample();
    }

    private static void blockingNumberThree() {
        // Create a Mono from the blockingOperation method
        Mono<String> blockingMono = Mono.fromCallable(AuxMethods::blockingOperation)
                // Run blocking code on boundedElastic scheduler
                .subscribeOn(Schedulers.boundedElastic());

        // Subscribe and print the emitted value
        blockingMono.subscribe(result ->
                System.out.println("Result: " + result)
        );

        // Keep JVM alive to allow async execution to complete
        Thread.sleep(4000);
    }

    private static void blockingNumberTwo() {

        // Generate a Flux from 1 to 10
        Flux<Integer> numberFlux = Flux.range(1, 10);

        // Transform the Flux into a lazy blocking Stream
        Stream<Integer> numberStream = numberFlux.toStream();

        // Iterate over the stream and print elements
        numberStream.forEach(number ->
                System.out.println("Value: " + number)
        );
    }

    private static void blockingCallNumberOne() {

        String url = "https://example.com";

        // Create a Mono from fetchUrlContent
        Mono<String> contentMono = fetchUrlContent(url);

        // Set timeout duration
        Duration timeout = Duration.ofSeconds(5);

        // Get Optional using timeout
        Optional<String> contentOptional = contentMono.blockOptional(timeout);

        // Handle the result
        contentOptional.ifPresentOrElse(
                content -> System.out.println("URL content: " + content),
                () -> System.out.println("The Mono completed empty.")
        );
    }

    private static void yetAnotherContext() {
        Mono<String> greetingMono = Mono.just("Hello");
        String key = "username";

        // Append the value of "username" from the context
        Mono<String> contextualizedGreetingMono =
                greetingMono.transformDeferredContextual((mono, contextView) ->
                        mono.map(greeting ->
                                greeting + " " + contextView.getOrDefault(key, "Guest")
                        )
                );

        // Add "Alice" to the Context and subscribe
        contextualizedGreetingMono
                .contextWrite(context -> context.put(key, "Alice"))
                .subscribe(result -> System.out.println("Result: " + result));

        // Add "Bob" to the Context and subscribe again
        contextualizedGreetingMono
                .contextWrite(context -> context.put(key, "Bob"))
                .subscribe(result -> System.out.println("Result: " + result));

    }

    private static void anotherContextWrite() {

        Flux<Integer> numbers = Flux.range(1, 5);
        String key = "multiplier";
        int value = 3;
        int defaultValue = 1;

        // Multiply each emitted number by the "multiplier" value from the context
        Flux<Integer> contextualizedNumbers = numbers
                .flatMap(number ->
                        Mono.deferContextual(contextView -> {
                            int multiplier = contextView.getOrDefault(key, defaultValue);
                            return Mono.just(number * multiplier);
                        })
                )
                // Enrich the Context with key-value pair
                .contextWrite(context -> context.put(key, value));

        // Subscribe and print emitted values
        contextualizedNumbers.subscribe(result ->
                System.out.println("Result: " + result)
        );
    }

    private static void transformedDefferedcontextual() {

        Flux<Integer> numbers = Flux.range(1, 5);
        String key = "divider";
        double value = 10.0;
        double defaultValue = 1.0;

        // Divide each emitted number by the "divider" value from the context
        Flux<Double> contextualizedNumbers = numbers
                .transformDeferredContextual((flux, ctxView) ->
                        flux.map(n -> n / ctxView.getOrDefault(key, defaultValue))
                )
                // Add key-value pair to the context
                .contextWrite(Context.of(key, value));

        // Subscribe and print the emitted values
        contextualizedNumbers.subscribe(result ->
                System.out.println("Result: " + result)
        );
    }

    private static void paralel4rails() throws InterruptedException {

        Flux<Integer> fluxRange = Flux.range(1, 10);

        // Create a ParallelFlux with 4 rails, run in parallel, and apply processingFunction
        ParallelFlux<Integer> parallelFlux = fluxRange
                .parallel(4)                     // Split into 4 rails
                .runOn(Schedulers.parallel())    // Process rails in parallel
                .map(AuxMethods::processingFunction);

        // Subscribe to the ParallelFlux (prints items as they are processed)
        parallelFlux.subscribe(value -> System.out.println("Received: " + value));

        Thread.sleep(10000); // Keep JVM alive for async processing
    }

    private static void paralelSchedudlersingleExample() throws InterruptedException {

        Flux<Integer> fluxRange = Flux.range(1, 10);

        // Use a parallel scheduler to process items, then change threading context for subscribing
        Flux<Integer> processedFlux = fluxRange
                .parallel() // Split processing across multiple threads
                .runOn(Schedulers.parallel()) // Process in parallel
                .map(AuxMethods::processingFunction)
                .sequential() // Merge back into a single Flux
                .publishOn(Schedulers.single()); // Subscribe on single thread

        // Subscribe and print processed values
        processedFlux.subscribe(value ->
                System.out.println("Received: " + value)
        );

        Thread.sleep(11000); // Keep JVM alive for async processing
    }

    private static void slowPublisherBoundedElastic() throws InterruptedException {

        // Call slowPublisher and use subscribeOn with a custom boundedElastic scheduler
        Flux<Integer> processedFlux = AuxMethods.slowPublisher()
                .subscribeOn(Schedulers.newBoundedElastic(
                        2,      // threadCap
                        2,      // queuedTaskCap
                        "bounded-elastic", // name
                        30,     // ttlSeconds
                        true    // daemon
                ));

        // Subscribe using fastConsumer
        processedFlux.subscribe(AuxMethods::fastConsumer);

        Thread.sleep(11000); // Keep JVM alive to allow async processing
    }


    private static void webScrapperProblem() throws InterruptedException {

        Flux<String> urlFlux = Flux.just("url1", "url2", "url3", "url4");

        // Change the threading context and apply fetchAndCountWords
        Flux<Integer> wordCountFlux = urlFlux
                .publishOn(Schedulers.boundedElastic())
                .map(AuxMethods::fetchAndCountWords);

        // Subscribe and print emitted elements
        wordCountFlux.subscribe(wordCount ->
                System.out.println("Word count: " + wordCount)
        );

        Thread.sleep(4000);
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
