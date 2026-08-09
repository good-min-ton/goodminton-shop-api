package com.lezh1n.goodminton_shop_api.services.impl;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.extern.slf4j.Slf4j;

/**
 * Server-sent events that nudge a connected client to refetch.
 *
 * <p>Deliberately an accelerator, not the delivery mechanism. The bell polls on
 * its own; this only makes it feel immediate. The API is reached through a
 * public tunnel whose behaviour with long-lived streams is unverified, so a
 * design where a buffered or dropped stream loses a notification would trade the
 * whole point of the feature for a nicety. Everything here is best-effort by
 * construction: emitters are dropped on the first failed write and the client
 * reconnects, having missed nothing that the next poll will not pick up.
 *
 * <p>The event carries no payload beyond "something changed". Pushing the
 * notification itself would mean two sources of truth for what the bell shows,
 * and would leak one recipient's message into another's stream if the emitter
 * map were ever keyed wrongly.
 *
 * <p>In-memory, so it is per-instance. That is correct for a single-container
 * deployment; more than one would need a shared bus, and the polling fallback is
 * what keeps that from being a correctness problem in the meantime.
 */
@Component
@Slf4j
public class NotificationStream {

    /** How long a browser may hold a stream open before reconnecting. */
    private static final long TIMEOUT_MS = 5 * 60 * 1000L;

    private final Map<Integer, Set<SseEmitter>> byRecipient = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Integer recipientId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MS);
        byRecipient.computeIfAbsent(recipientId, id -> new CopyOnWriteArraySet<>()).add(emitter);

        emitter.onCompletion(() -> remove(recipientId, emitter));
        emitter.onTimeout(() -> remove(recipientId, emitter));
        emitter.onError(e -> remove(recipientId, emitter));

        // An immediate event both proves the stream is open and gives a proxy
        // something to flush, which is where a buffering tunnel shows itself.
        try {
            emitter.send(SseEmitter.event().name("ready").data("ok"));
        } catch (IOException e) {
            remove(recipientId, emitter);
        }
        return emitter;
    }

    /** Nudge one recipient's open streams. Never throws. */
    public void push(Integer recipientId) {
        Set<SseEmitter> emitters = byRecipient.get(recipientId);
        if (emitters == null) {
            return; // nobody watching; the next poll picks it up
        }
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("notification").data("new"));
            } catch (Exception e) {
                // A dead emitter is normal - a closed tab, a dropped tunnel.
                remove(recipientId, emitter);
            }
        }
    }

    private void remove(Integer recipientId, SseEmitter emitter) {
        Set<SseEmitter> emitters = byRecipient.get(recipientId);
        if (emitters == null) {
            return;
        }
        emitters.remove(emitter);
        // Drop the key too, or a long-running instance accumulates one empty set
        // per account that has ever connected.
        byRecipient.remove(recipientId, Set.of());
        if (emitters.isEmpty()) {
            byRecipient.remove(recipientId, emitters);
        }
    }

    /** Open stream count for a recipient. Test seam. */
    public int openStreams(Integer recipientId) {
        Set<SseEmitter> emitters = byRecipient.get(recipientId);
        return emitters == null ? 0 : emitters.size();
    }
}
