package io.github.md5sha256.democracypost.database;

import io.github.md5sha256.democracypost.PostalPackage;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class UserState {

    private final Map<UUID, PostalPackage> packages = new HashMap<>();

    public Collection<PostalPackage> packages() {
        return Collections.unmodifiableCollection(this.packages.values());
    }

    @Nonnull
    public UserState deepCopy() {
        Map<UUID, PostalPackage> copy = new HashMap<>(this.packages.size());
        for (Map.Entry<UUID, PostalPackage> entry : this.packages.entrySet()) {
            copy.put(entry.getKey(), entry.getValue().deepCopy());
        }
        UserState userState = new UserState();
        userState.packages.putAll(copy);
        return userState;
    }

    public void addPackage(@Nonnull PostalPackage postalPackage) {
        this.packages.put(postalPackage.sender(), postalPackage);
    }

    @Nonnull
    public Optional<PostalPackage> removePackage(@Nonnull UUID id) {
        return Optional.ofNullable(this.packages.remove(id));
    }

    @Nonnull
    public Collection<PostalPackage> removeExpiredPackages() {
        List<PostalPackage> removed = new ArrayList<>();
        Date now = Date.from(Instant.now());
        for (PostalPackage postalPackage : this.packages.values()) {
            if (postalPackage.expiryDate().after(now)) {
                removed.add(postalPackage);
            }
        }
        removed.forEach(postalPackage -> this.packages.remove(postalPackage.sender()));
        return removed;
    }


}
