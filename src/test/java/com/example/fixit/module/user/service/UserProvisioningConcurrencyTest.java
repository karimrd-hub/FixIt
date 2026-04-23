package com.example.fixit.module.user.service;

import com.example.fixit.common.PostgresIntegrationBase;
import com.example.fixit.module.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Tag("concurrency")
class UserProvisioningConcurrencyTest extends PostgresIntegrationBase {

    @Autowired
    private UserProvisioningService provisioningService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanUsers() {
        userRepository.deleteAll();
    }

    @Test
    void twoConcurrentProvisions_sameSub_resultInExactlyOneRow() throws Exception {
        String keycloakId = "kc-race";
        List<Throwable> errors = runConcurrently(
                () -> provisioningService.provisionIfAbsent(keycloakId, "a@example.com", "Racer A"),
                () -> provisioningService.provisionIfAbsent(keycloakId, "b@example.com", "Racer B"));

        assertEquals(List.of(), errors, "No thread should propagate an exception");
        assertEquals(1, userRepository.findAll().size(),
                "Exactly one row should exist for sub=" + keycloakId);
    }

    @Test
    void twoConcurrentProvisions_differentSubs_resultInTwoRows() throws Exception {
        List<Throwable> errors = runConcurrently(
                () -> provisioningService.provisionIfAbsent("kc-alpha", "alpha@example.com", "Alpha"),
                () -> provisioningService.provisionIfAbsent("kc-beta", "beta@example.com", "Beta"));

        assertEquals(List.of(), errors, "No thread should propagate an exception");
        assertEquals(2, userRepository.findAll().size());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private List<Throwable> runConcurrently(Runnable... tasks) throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(tasks.length);
        try {
            List<Future<Throwable>> futures = new ArrayList<>();
            for (Runnable task : tasks) {
                Callable<Throwable> gated = () -> {
                    start.await();
                    try {
                        task.run();
                        return null;
                    } catch (Throwable t) {
                        return t;
                    }
                };
                futures.add(pool.submit(gated));
            }
            start.countDown();

            List<Throwable> errors = new ArrayList<>();
            for (Future<Throwable> f : futures) {
                Throwable err = f.get(10, TimeUnit.SECONDS);
                if (err != null) {
                    errors.add(err);
                }
            }
            return errors;
        } finally {
            pool.shutdownNow();
        }
    }
}
