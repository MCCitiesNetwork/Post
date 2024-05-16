package io.github.md5sha256.democracypost.model;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

public class PostalPackage {

    private final UUID id;
    private final Date expiryDate;
    private final PackageContent content;
    private final boolean isReturnPackage;


    public PostalPackage(@Nonnull Date expiryDate, @Nonnull PackageContent content, boolean isReturnPackage) {
        this(UUID.randomUUID(), expiryDate, content, isReturnPackage);
    }

    public PostalPackage(@Nonnull UUID id,
                         @Nonnull Date expiryDate,
                         @Nonnull PackageContent content,
                         boolean isReturnPackage) {
        this.id = id;
        this.expiryDate = expiryDate;
        this.content = content;
        this.isReturnPackage = isReturnPackage;
    }

    @Nonnull
    public PostalPackage deepCopy() {
        return new PostalPackage(this.id, this.expiryDate, this.content.deepCopy(), this.isReturnPackage);
    }

    @Nonnull
    public UUID id() {
        return this.id;
    }

    @Nonnull
    public Date expiryDate() {
        return this.expiryDate;
    }

    @Nonnull
    public PackageContent content() {
        return this.content;
    }

    public boolean isReturnPackage() {
        return this.isReturnPackage;
    }

    public boolean expired() {
        return this.expiryDate.after(Date.from(Instant.now()));
    }
}
