package com.demo.project72;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import brave.Span;
import brave.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.sleuth.instrument.async.LazyTraceExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

@Service
@Slf4j
@EnableScheduling
class SchedulingService {

    @Autowired
    private SleuthService sleuthService;

    @Scheduled(fixedDelay = 30000)
    public void scheduledWork() throws InterruptedException {
        log.info("Start some work from the scheduled task");
        sleuthService.asyncMethod();
        log.info("End work from scheduled task");
    }
}

@RestController
@Slf4j
class SleuthController {

    @Autowired
    private SleuthService sleuthService;
    @Autowired
    private Executor executor;

    @GetMapping("/")
    public String helloSleuth() {
        log.info("Hello Sleuth");
        return "success";
    }

    @GetMapping("/same-span")
    public String helloSleuthSameSpan() throws InterruptedException {
        log.info("Same Span");
        sleuthService.doSomeWorkSameSpan();
        return "success";
    }

    @GetMapping("/new-span")
    public String helloSleuthNewSpan() throws InterruptedException {
        log.info("New Span");
        sleuthService.doSomeWorkNewSpan();
        return "success";
    }

    @GetMapping("/new-thread")
    public String helloSleuthNewThread() {
        log.info("New Thread");
        Runnable runnable = () -> {
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("I'm inside the new thread - with a new span");
        };
        executor.execute(runnable);

        log.info("I'm done - with the original span");
        return "success";
    }

    @GetMapping("/async")
    public String helloSleuthAsync() throws InterruptedException {
        log.info("Before Async Method Call");
        sleuthService.asyncMethod();
        log.info("After Async Method Call");
        return "success";
    }
}

@Service
@Slf4j
@EnableAsync
class SleuthService {

    @Autowired
    private Tracer tracer;

    public void doSomeWorkSameSpan() throws InterruptedException {
        Thread.sleep(1000L);
        log.info("Doing some work");
    }

    public void doSomeWorkNewSpan() throws InterruptedException {
        log.info("I'm in the original span");

        Span newSpan = tracer.nextSpan().name("newSpan").start();
        try (Tracer.SpanInScope ws = tracer.withSpanInScope(newSpan.start())) {
            Thread.sleep(1000L);
            log.info("I'm in the new span doing some cool work that needs its own span");
        } finally {
            newSpan.finish();
        }

        log.info("I'm in the original span");
    }

    @Async
    public void asyncMethod() throws InterruptedException {
        log.info("Start Async Method");
        Thread.sleep(1000L);
        log.info("End Async Method");
    }
}

@Configuration
class ThreadConfig extends AsyncConfigurerSupport implements SchedulingConfigurer {

    @Autowired
    private BeanFactory beanFactory;

    @Bean
    public Executor executor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(1);
        threadPoolTaskExecutor.setMaxPoolSize(1);
        threadPoolTaskExecutor.initialize();

        return new LazyTraceExecutor(beanFactory, threadPoolTaskExecutor);
    }

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(1);
        threadPoolTaskExecutor.setMaxPoolSize(1);
        threadPoolTaskExecutor.initialize();

        return new LazyTraceExecutor(beanFactory, threadPoolTaskExecutor);
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        scheduledTaskRegistrar.setScheduler(schedulingExecutor());
    }

    @Bean(destroyMethod = "shutdown")
    public Executor schedulingExecutor() {
        return Executors.newScheduledThreadPool(1);
    }
}
