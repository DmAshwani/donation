package in.dataman.donation.comcontroller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@CrossOrigin(originPatterns = "**", allowCredentials = "true")
@RestController
@RequestMapping("/api/v1")
public class SseController {

    private static final Logger logger = LoggerFactory.getLogger(SseController.class);
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public SseController() {
        scheduler.scheduleAtFixedRate(() -> {
            emitters.forEach((userId, emitter) -> {
                try {
                    emitter.send(SseEmitter.event().name("heartbeat").data("ping"));
                } catch (IOException e) {
                    emitters.remove(userId);
                    logger.warn("Heartbeat failed for user {}. Removing emitter.", userId, e);
                }
            });
        }, 0, 10, TimeUnit.SECONDS);
    }

    @GetMapping(path = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@RequestParam String userId) {
        logger.info("User {} subscribed to SSE.", userId);
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L); // 30 minutes timeout
        emitters.put(userId, emitter);

        emitter.onCompletion(() -> {
            emitters.remove(userId);
            logger.info("SSE connection for user {} completed and removed.", userId);
        });

        emitter.onTimeout(() -> {
            emitters.remove(userId);
            logger.warn("SSE connection for user {} timed out and removed.", userId);
        });

        emitter.onError((ex) -> {
            emitters.remove(userId);
            logger.error("SSE connection for user {} encountered an error and was removed.", userId, ex);
        });

        // Send an initial event to confirm subscription
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("Successfully connected to SSE")
                    .reconnectTime(3000));
        } catch (IOException e) {
            logger.error("Failed to send initial event to user {}.", userId, e);
        }

        return emitter;
    }


    public void notifyTokenUpdate(String userId) {
        SseEmitter emitter = emitters.get(userId);

        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("token-update")
                        .data("Logout")
                        .id(UUID.randomUUID().toString())
                        .reconnectTime(3000));
            } catch (IOException e) {
                emitters.remove(userId);
                logger.error("Failed to send token update to user {}. Removing emitter.", userId, e);
            }
        } else {
            logger.warn("No active SSE connection found for user {}. Token update not sent.", userId);
        }
    }

    public void broadcast(String eventName, String message) {
        emitters.forEach((userId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(message)
                        .id(UUID.randomUUID().toString())
                        .reconnectTime(3000));
            } catch (IOException e) {
                emitters.remove(userId);
                logger.error("Failed to broadcast event to user {}. Removing emitter.", userId, e);
            }
        });
    }

    @PreDestroy //Cannot resolve symbol 'PreDestroy'
    public void onShutdown() {
        emitters.forEach((userId, emitter) -> {
            try {
                emitter.complete();
                logger.info("Emitter for user {} completed during shutdown.", userId);
            } catch (Exception e) {
                logger.warn("Failed to complete emitter for user {} during shutdown.", userId, e);
            }
        });
        emitters.clear();
        scheduler.shutdown();
    }
}
