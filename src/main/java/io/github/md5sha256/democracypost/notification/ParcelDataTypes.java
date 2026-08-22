package io.github.md5sha256.democracypost.notification;

import io.github.md5sha256.playernotifications.api.NotificationDataTypeRegistry;
import io.github.md5sha256.playernotifications.api.NotificationService;
import io.github.md5sha256.playernotifications.api.category.NotificationCategoryRegistry;

import javax.annotation.Nonnull;

/**
 * Registers and unregisters DemocracyPost's PlayerNotifications data type.
 *
 * <p>Registration is not just the renderer: a data type with no category falls into
 * {@code uncategorized} and gets no toggle of its own on the preference screen, so the category is
 * declared and the data type claimed under it in the same breath. PlayerNotifications handles the
 * fact that a separate plugin necessarily registers after its own category snapshot is built.
 *
 * <p>Uses {@code registerJsonRenderable}, never {@code registerJsonPayload}: an explicit processor
 * wins dispatch precedence and bypasses preferences and sinks entirely, which would defeat the
 * point of delivering through PlayerNotifications at all.
 */
public final class ParcelDataTypes {

    static final String CATEGORY_LABEL = "Parcels";
    static final String CATEGORY_DESCRIPTION = "Notices that another player has posted you a parcel.";

    private ParcelDataTypes() {
    }

    /**
     * Binds {@link ParcelPayload} to its data type, renderer and category. Re-registration is
     * idempotent — the underlying registries are plain map puts.
     */
    public static void registerAll(@Nonnull NotificationService service,
                                   @Nonnull ParcelNotificationRenderer renderer) {
        service.registerJsonRenderable(ParcelPayload.DATA_TYPE, ParcelPayload.class,
                (payload, target) -> {
                    RenderedParcel rendered = renderer.render(payload, target);
                    return new io.github.md5sha256.playernotifications.api.render.RenderableNotification(
                            rendered.title(), rendered.body());
                });
        NotificationCategoryRegistry categories = service.categoryRegistry();
        categories.registerCategory(ParcelPayload.DATA_TYPE, CATEGORY_LABEL, CATEGORY_DESCRIPTION);
        categories.claimDataType(ParcelPayload.DATA_TYPE, ParcelPayload.DATA_TYPE);
    }

    /**
     * Releases everything {@link #registerAll} claimed. Run on disable so a reloaded plugin does
     * not leave a renderer behind that is bound to a dead class loader.
     */
    public static void unregisterAll(@Nonnull NotificationService service) {
        NotificationDataTypeRegistry registry = service.dataTypeRegistry();
        registry.unregisterPayloadMapping(ParcelPayload.DATA_TYPE);
        // The mapping's removal already cascades into these, but a payload class this plugin owns
        // outright is cheap to be explicit about.
        registry.unregisterSerializer(ParcelPayload.class);
        registry.unregisterRenderer(ParcelPayload.class);
        service.categoryRegistry().unclaimDataType(ParcelPayload.DATA_TYPE, ParcelPayload.DATA_TYPE);
    }
}
