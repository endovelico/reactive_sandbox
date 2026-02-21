package com.reactive.sandbox.reactor_sandbox;

import com.reactive.sandbox.aux.AnalysisResult;
import com.reactive.sandbox.aux.AuxMethods;
import com.reactive.sandbox.aux.ComputationResult;
import com.reactive.sandbox.aux.ComputationSummary;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;

import java.time.Duration;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

public class BeginnerProblems {

    public static void main(String[] args) throws InterruptedException {

        justOrEmptyExample();
        //createMonoAndPrintValue();
        //emitsNumbers1to5();
        //filterAndSquareNumbers();
        //transformeNameWithFlatMap();
        //asyncTransformationWithOrderDifference();
        //filterAndTransformStrings();
        //addGreetingToListOfName();
        //greetingWithMultipleFluxes();
        //zipTwoFluxes();
        //zipThreeFluxes();
        //combineFastAndSlow();
        //firstMatchAndGlobalAggregation();
        //divergingTransformations();
        //collectIntoAList();
        //collectIntoASet();
        //collectSumAndCount();
        //countAndSumInOnePass();
        //minAndMaxInOnePass();
        //sumCountAndProduct();
        //sumCountAverage();
        //executeASideEffect();
        //executeAsSubscription();
        //activateLog();
        //limitRateExample();

    }

    private static void justOrEmptyExample() {

        Optional<String> optionalWithValue = Optional.of("Sean Strickland");
        Optional<String> emptyOptional = Optional.empty();

        Mono<String> monoWithValue = Mono.justOrEmpty(optionalWithValue);
        Mono<String> monoEmpty = Mono.justOrEmpty(emptyOptional);

        monoWithValue.subscribe(
                element -> System.out.println("Mono with value - Value: " + element),
                error -> System.err.println("Mono with value - Error: " + error.getMessage()),
                () -> System.out.println("Mono with value complete")
        );

        monoEmpty.subscribe(
                element -> System.out.println("Mono empty - Value: " + element),
                error -> System.err.println("Mono empty - Error: " + error.getMessage()),
                () -> System.out.println("Mono empty complete")
        );
    }

    private static void limitRateExample() throws InterruptedException {

        // Simulate a slow upstream source of 10 movies
        Flux<String> movieFlux = Flux.range(1, 100)
                .map(i -> "Movie " + i)
                .delayElements(Duration.ofMillis(200)) // simulate slow fetch
                .doOnNext(movie -> System.out.println("[Upstream emitted] " + movie));

        // Apply limitRate to request 3 items at a time
        movieFlux.limitRate(3)
                .subscribe(
                        movie -> System.out.println("[Subscriber received] " + movie),
                        err -> System.err.println("Error: " + err),
                        () -> System.out.println("All movies processed!")
                );

        // Sleep main thread long enough for all items to be processed
        Thread.sleep(3000);
    }

    private static void activateLog() {

        Flux.range(1, 20)
                .log()
                .subscribe();

    }

    private static void executeAsSubscription() {

        Flux.range(1, 3)
                .doOnSubscribe(s -> System.out.println("Subscribed!"))
                .subscribe(System.out::println);

    }

    private static void executeASideEffect() {

        Flux.range(1, 3)
                .doOnNext(n -> System.out.println("Processing: " + n))
                .map(n -> n * 2)
                .subscribe(System.out::println);
    }

    private static void sumCountAverage() {

        Flux<Double> numbers = Flux.just(2d, 4d, 6d, 8d, 10d);

        numbers.collect(
                () -> new double[]{0, 0, 0},  // [sum, count, average]
                (acc, value) -> {
                    acc[0] += value;  // sum
                    acc[1]++;         // count
                }
        ).map(acc -> {
            acc[2] = acc[0] / acc[1];     // compute average after collecting
            return acc;
        }).subscribe(arr -> System.out.println("Sum: " + arr[0] +
                ", Count: " + arr[1] +
                ", Average: " + arr[2]));
    }

    private static void sumCountAndProduct() {

        Flux<Integer> numbers = Flux.range(0, 4);

        numbers.collect(
                () -> new long[]{0, 0, 1}, // sum=0, count=0, product=1
                (acc, value) -> {
                    acc[0] += value; // sum
                    acc[1]++;        // count
                    acc[2] *= value; // product
                }
        ).subscribe(arr -> System.out.println(
                "Sum: " + arr[0] +
                        ", Count: " + arr[1] +
                        ", Product: " + arr[2]
        ));


    }

    private static void minAndMaxInOnePass() {

        Flux<Integer> numbers = Flux.just(7, 3, 9, 2, 5);

        numbers.collect(
                () -> new int[]{Integer.MAX_VALUE, Integer.MIN_VALUE}, // [min, max]
                (acc, value) -> {
                    acc[0] = Math.min(acc[0], value); // update min
                    acc[1] = Math.max(acc[1], value); // update max
                }
        );
    }

    private static void countAndSumInOnePass() {

        Flux<Integer> numbers = Flux.just(1, 2, 3, 4, 5);

        numbers.collect(
                () -> new int[2],       // custom container: [count, sum]
                (acc, value) -> {
                    acc[0]++;            // increment count
                    acc[1] += value;     // add to sum
                }
        );
    }

    private static void collectSumAndCount() {

        Flux<Double> numbers = Flux.just(5d, 10d, 15d);

        numbers.collect(
                () -> new double[2],       // [sum, count]
                (acc, value) -> {
                    acc[0] += value;        // accumulate sum
                    acc[1]++;               // count elements
                }
        );
    }

    private static void collectIntoASet() {

        Flux<String> words = Flux.just("apple", "banana", "apple", "orange", "banana");

        words.collect(Collectors.toSet());

    }

    private static void collectIntoAList() {

        Flux<Integer> numbers = Flux.range(1, 10);

        numbers.filter(n -> n % 2 == 0)
                .collectList();
    }

    private static void divergingTransformations() {

        Flux<Integer> numbers = Flux.range(1, 8);

        Mono<Integer> productOfOddsMono = numbers
                        .filter(n -> n % 2 != 0)
                        .reduce(1, (a, b) -> a * b);

        Mono<Double> averageOfEvensMono = numbers
                        .filter(n -> n % 2 == 0)
                        .collect(
                                () -> new long[2], // [sum, count]
                                (acc, value) -> {
                                    acc[0] += value;
                                    acc[1]++;
                                }
                        )
                        .map(acc -> acc[1] == 0
                                ? 0.0
                                : (double) acc[0] / acc[1]);

        Mono.zip(productOfOddsMono, averageOfEvensMono)
                .map(tuple -> new ComputationSummary(
                        tuple.getT1(),
                        tuple.getT2()
                )).subscribe(System.out::println);
    }

    private static void firstMatchAndGlobalAggregation() {

        Flux<Integer> numbers = Flux.range(1, 50);

        Mono<Integer> firstMultipleOfSevenMono = numbers
                        .filter(n -> n % 7 == 0)
                        .next();

        Mono<Long> multiplesOfThreeCountMono = numbers
                        .filter(n -> n % 3 == 0)
                        .count();

        Mono.zip(firstMultipleOfSevenMono, multiplesOfThreeCountMono)
                .map(tuple -> new AnalysisResult(
                        tuple.getT1(),
                        tuple.getT2()
                )).subscribe(System.out::println);
    }

    private static void combineFastAndSlow() {

        Flux<Integer> numbers = Flux.range(1, 10);

        Mono<Integer> primeCountMono = numbers
                        .filter(AuxMethods::isPrime)
                        .count()
                        .map(Long::intValue);

        Mono<Integer> sumOfSquaresMono = numbers
                        .map(n -> n * n)
                        .reduce(0, Integer::sum);

        Mono.zip(primeCountMono, sumOfSquaresMono)
                .map(tuple -> new ComputationResult(
                        tuple.getT1(),
                        tuple.getT2()
                )).subscribe(System.out::println);
    }


    private static void zipThreeFluxes() {

        Flux<String> greetings = Flux.just("Hello", "Hi", "Hey");
        Flux<String> firstNames = Flux.just("Alice", "Bob", "Charlie");
        Flux<String> lastNames = Flux.just("Smith", "Johnson", "Williams");

        // Use Tuple3 instead of a 3-arg lambda
        Flux<Tuple3<String, String, String>> zipped = Flux.zip(greetings, firstNames, lastNames);

        zipped.map(tuple -> tuple.getT1() + " " + tuple.getT2() + " " + tuple.getT3())
                .subscribe(System.out::println);
    }

    private static void zipTwoFluxes() {

        Flux<String> firstNames = Flux.just("Alice", "Bob");
        Flux<String> lastNames = Flux.just("Smith", "Johnson");

        Flux<String> fullNames = Flux.zip(firstNames, lastNames, (first, last) -> first + "" + last);
        fullNames.subscribe(System.out::println);
    }

    private static void greetingWithMultipleFluxes() {

        Mono<String> greeting = Mono.just("Hello");
        Flux<String> firstNames = Flux.just("Alice", "Bob");
        Flux<String> lastNames = Flux.just("Smith", "Johnson");

        greeting.flatMapMany(g ->
                        firstNames.flatMap(first ->
                                lastNames.map(last -> g + " " + first + " " + last)
                        )
                )
                .subscribe(System.out::println);
    }

    private static void addGreetingToListOfName() {

        Mono<String> greeting = Mono.just("Hello");
        Flux<String> names = Flux.just("Alice", "Bob", "Charlie");

        greeting.flatMapMany(g -> names.map(name -> g + " " + name))
                .subscribe(System.out::println);
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
