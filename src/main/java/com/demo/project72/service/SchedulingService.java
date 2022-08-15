package com.demo.project72.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@EnableScheduling
public class SchedulingService {

    @Scheduled(fixedDelay = 60000)
    public void scheduledWork() throws InterruptedException {
        log.info("Work on Schedule!");
    }
}
