package com.skyding.distributedlock.demo;

import com.skyding.distributedlock.demo.lock.Synchronized;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author weichunhe
 * created at 2018/12/27
 */
@Component
public class DemoTask {

    @Synchronized
    @Scheduled(cron = "0 * * * * ? ")
    public void execute() {
        System.out.println("I locked successfully and start ...");
        // do everything here you want
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("I finished");
    }
}
