package io.github.md5sha256.democracypost.notification;

import io.github.md5sha256.playernotifications.api.TypedNotification;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A {@link ParcelEnqueuer} that stores the notice and pushes it straight away, through
 * PlayerNotifications' {@code enqueueAndDeliver}.
 *
 * <p>Plain enqueueing only stores the notice, so an online recipient is not told until they next
 * join. Delivering right away reaches an online recipient now, and an offline one with a linked
 * Discord account by DM now.
 *
 * <p>The returned future is never waited on: this runs on the main thread, and completion only
 * means delivery was attempted. A failure is logged and otherwise dropped; the notice is already
 * stored, so the recipient still sees it in their inbox.
 */
public final class DeliveringParcelEnqueuer implements ParcelEnqueuer {

    /**
     * The one call this needs from {@code NotificationService}, so the class can be tested without
     * a running server.
     */
    @FunctionalInterface
    public interface Delivery {

        @Nonnull
        CompletableFuture<Void> enqueueAndDeliver(@Nonnull TypedNotification<ParcelPayload> notification,
                                                  boolean overwriteAllowed);
    }

    private final Delivery delivery;
    private final Logger logger;

    public DeliveringParcelEnqueuer(@Nonnull Delivery delivery, @Nonnull Logger logger) {
        this.delivery = Objects.requireNonNull(delivery, "delivery");
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    @Override
    public void enqueue(@Nonnull TypedNotification<ParcelPayload> notification, boolean overwriteAllowed) {
        this.delivery.enqueueAndDeliver(notification, overwriteAllowed).whenComplete((ignored, error) -> {
            if (error != null) {
                this.logger.log(Level.WARNING,
                        "Failed to deliver parcel notice " + notification.notifKey(), error);
            }
        });
    }
}
