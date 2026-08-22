package io.github.md5sha256.democracypost.notification;

import com.earth2me.essentials.Console;
import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import io.github.md5sha256.democracypost.localization.MessageContainer;
import net.essentialsx.api.v2.services.mail.MailService;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.UUID;

/**
 * Delivers parcel notices as Essentials mail — the original behaviour, and still the default on a
 * server without PlayerNotifications.
 *
 * <p>Essentials mail is plain text, so the message is flattened out of its component form. The
 * notice has no title here and does not expire with the parcel: Essentials mail is read and
 * deleted by the player, with no expiry concept to hand it.
 */
public class EssentialsMailService implements ParcelNotificationService {

    private final MessageContainer messageContainer;
    private final Essentials essentials;

    public EssentialsMailService(@Nonnull MessageContainer messageContainer) {
        this.messageContainer = messageContainer;
        this.essentials = JavaPlugin.getPlugin(Essentials.class);
    }

    @Override
    public void notifyNewParcel(@Nonnull UUID receiver,
                                @Nonnull UUID sender,
                                @Nonnull String senderName,
                                @Nonnull UUID parcelId,
                                @Nonnull Instant expiry) {
        String message = this.messageContainer.plaintextMessageFor(ParcelNotificationRenderer.BODY_MESSAGE_KEY)
                .replace("%player%", senderName);
        User receiverUser = this.essentials.getUser(receiver);
        MailService mailService = this.essentials.getMail();
        mailService.sendMail(receiverUser, Console.getInstance(), message);
    }

}
