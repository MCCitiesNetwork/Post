package io.github.md5sha256.democracypost;

import com.earth2me.essentials.Console;
import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import io.github.md5sha256.democracypost.localization.MessageContainer;
import net.essentialsx.api.v2.services.mail.MailService;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import java.util.UUID;

public class EssentialsMailService {

    private final MessageContainer messageContainer;
    private final Essentials essentials;

    public EssentialsMailService(@Nonnull MessageContainer messageContainer) {
        this.messageContainer = messageContainer;
        this.essentials = JavaPlugin.getPlugin(Essentials.class);
    }

    public void notifyNewParcel(UUID receiver, String senderName) {
        String message = this.messageContainer.plaintextMessageFor("messages.new-parcel")
                .replace("%player%", senderName);
        User receiverUser = essentials.getUser(receiver);
        MailService mailService = essentials.getMail();
        mailService.sendMail(receiverUser, Console.getInstance(), message);
    }

}
