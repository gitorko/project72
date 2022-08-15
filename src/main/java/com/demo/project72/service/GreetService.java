package com.demo.project72.service;

import java.util.concurrent.TimeUnit;

import brave.Span;
import brave.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@EnableAsync
@RequiredArgsConstructor
public class GreetService {

    private final Tracer tracer;

    @SneakyThrows
    public void doSomeWorkSameSpan() {
        TimeUnit.SECONDS.sleep(1);
        log.info("Work Span");
    }

    public void doSomeWorkNewSpan() throws InterruptedException {
        log.info("Original span");
        Span newSpan = tracer.nextSpan().name("newSpan").start();
        try (Tracer.SpanInScope ws = tracer.withSpanInScope(newSpan.start())) {
            TimeUnit.SECONDS.sleep(1);
            log.info("New Span");
        } finally {
            newSpan.finish();
        }
        log.info("Original span");
    }

    @Async
    public void asyncMethod() throws InterruptedException {
        log.info("Start Async Method");
        TimeUnit.SECONDS.sleep(1);
        log.info("End Async Method");
    }
}
