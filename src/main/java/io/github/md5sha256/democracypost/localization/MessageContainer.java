package io.github.md5sha256.democracypost.localization;

import io.papermc.paper.text.PaperComponents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MessageContainer {

    private final Map<String, Component> messages = new ConcurrentHashMap<>();

    public String plaintextMessageFor(@Nonnull String key) {
        return PlainTextComponentSerializer.plainText().serialize(messageFor(key));
    }

    @Nonnull
    public Component messageFor(@Nonnull String key) {
        return this.messages.getOrDefault(key, Component.text(key));
    }

    @Nonnull
    public String miniMessageFormattedFor(@Nonnull String key) {
        return MiniMessage.miniMessage().serialize(messageFor(key));
    }

    public void setMessage(@Nonnull String key, @Nonnull Component message) {
        this.messages.put(key, message);
    }

    public void load(@Nonnull ConfigurationNode root) throws ConfigurateException {
        Map<String, Component> temp = new HashMap<>();
        loadInto("", root, temp);
        this.messages.putAll(temp);
    }

    public void save(@Nonnull ConfigurationNode root) throws ConfigurateException {
        for (Map.Entry<String, Component> entry : this.messages.entrySet()) {
            root.node((Object[]) entry.getKey().split("\\.")).set(entry.getValue());
        }
    }

    private void loadInto(String path,
                          ConfigurationNode root,
                          Map<String, Component> temp) throws ConfigurateException {
        if (!root.empty()) {
            Component component = root.get(Component.class);
            if (component != null) {
                temp.put(path, component);
            }
        }
        for (Map.Entry<Object, ? extends ConfigurationNode> entry : root.childrenMap().entrySet()) {
            String key = entry.getKey().toString();
            ConfigurationNode node = entry.getValue();
            String newPath = path.isEmpty() ? key : path + "." + key;
            loadInto(newPath, node, temp);
        }
    }

}
