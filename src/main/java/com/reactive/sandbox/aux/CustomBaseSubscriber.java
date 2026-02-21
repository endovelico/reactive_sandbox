package com.reactive.sandbox.aux;

import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;

public class CustomBaseSubscriber<T> extends BaseSubscriber<T> {

    @Override
    protected void hookOnSubscribe(Subscription subscription) {
        System.out.println("Subscribed");
        request(Long.MAX_VALUE); // request all elements
    }

    @Override
    protected void hookOnNext(T value) {
        System.out.println("Received: " + value);
    }

    @Override
    protected void hookOnComplete() {
        System.out.println("Completed");
    }

    @Override
    protected void hookOnError(Throwable throwable) {
        System.out.println("Error: " + throwable.getMessage());
    }
}
