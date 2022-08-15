package com.demo.project72.controller;

import com.demo.project72.service.GreetService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class HomeController {

    private final GreetService greetService;

    @SneakyThrows
    @GetMapping("/hello-span")
    public String helloSpan() {
        log.info("Same Span");
        greetService.doSomeWorkSameSpan();
        return "success";
    }

    @SneakyThrows
    @GetMapping("/hello-new-span")
    public String helloNewSpan() {
        log.info("New Span");
        greetService.doSomeWorkNewSpan();
        return "success";
    }


    @SneakyThrows
    @GetMapping("/hello-async")
    public String helloAsync() {
        log.info("Before Async Method Call");
        greetService.asyncMethod();
        log.info("After Async Method Call");
        return "success";
    }
}
