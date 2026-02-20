package com.reactive.sandbox.aux;

import reactor.core.publisher.Flux;

public class AuxMethods {

    public static boolean isPrime(int n) {
        if (n <= 1) return false;

        return Flux.range(2, n - 2)
                .all(i -> n % i != 0)
                .block(); // ⚠️ We'll fix this below
    }

}
