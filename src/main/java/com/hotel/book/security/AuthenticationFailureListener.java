package com.hotel.book.security;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticationFailureListener {

    private final MeterRegistry meterRegistry;

    @EventListener
    public void onFailure(AuthenticationFailureBadCredentialsEvent event) {

        Counter counter =
                meterRegistry.counter("auth.failure.count");

        counter.increment();
    }
}