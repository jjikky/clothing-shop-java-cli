package com.shop.services;

import com.shop.shared.SharedResource;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LogService {
    private final BufferedWriter writer;
    private final ExecutorService executor;
    private final SharedResource sharedResource;

    public LogService(String logFileName, SharedResource sharedResource) throws IOException {
        this.writer = new BufferedWriter(new FileWriter(logFileName, true));
        this.executor = Executors.newSingleThreadExecutor();
        this.sharedResource = sharedResource;
    }

    public void log(String message) {
        try {
            executor.submit(() -> {
                try {
                    String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    writer.write(timeStamp + " - " + message);
                    writer.newLine();
                    writer.flush();
                    sharedResource.updateLog();
                } catch (IOException e) {
                    e.getStackTrace();
                }
            }).get();
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    public void close() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
            writer.close();
        } catch (IOException | InterruptedException e) {
            e.getStackTrace();
        }
    }
}
