package io.github.md5sha256.democracypost.notification;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * The backend used when no notification plugin is usable: parcels are still posted and collected,
 * but nobody is told about them.
 *
 * <p>A missing notification medium is a configuration problem, not a reason to refuse parcels, so
 * this warns rather than throwing — once, because the alternative is a warning per parcel for as
 * long as the server runs misconfigured.
 */
public class NoopParcelNotificationService implements ParcelNotificationService {

    private final Logger logger;
    private final AtomicBoolean warned = new AtomicBoolean();

    public NoopParcelNotificationService(@Nonnull Logger logger) {
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    @Override
    public void notifyNewParcel(@Nonnull UUID receiver,
                                @Nonnull UUID sender,
                                @Nonnull String senderName,
                                @Nonnull UUID parcelId,
                                @Nonnull Instant expiry) {
        if (this.warned.compareAndSet(false, true)) {
            this.logger.warning("No parcel notification backend is available; recipients will not be "
                    + "told about new parcels. Check post-settings.notification-backend.");
        }
    }
}
