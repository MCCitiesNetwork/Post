package io.github.md5sha256.democracypost.model;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

public class PostalPackage {

    private final UUID id;
    private final Date expiryDate;
    private final PackageContent content;
    private boolean claimed;


    public PostalPackage(@Nonnull Date expiryDate, @Nonnull PackageContent content) {
        this(UUID.randomUUID(), expiryDate, content);
    }

    public PostalPackage(@Nonnull UUID id, @Nonnull Date expiryDate, @Nonnull PackageContent content) {
        this(id, expiryDate, content, false);
    }

    public PostalPackage(@Nonnull UUID id, @Nonnull Date expiryDate, @Nonnull PackageContent content, boolean claimed) {
        this.id = id;
        this.expiryDate = expiryDate;
        this.content = content;
        this.claimed = claimed;
    }

    @Nonnull
    public PostalPackage deepCopy() {
        return new PostalPackage(this.id, this.expiryDate, this.content.deepCopy(), this.claimed);
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

    public boolean claimed() {
        return this.claimed;
    }

    public void setClaimed() {
        this.claimed = true;
    }

    public boolean expired() {
        return this.expiryDate.after(Date.from(Instant.now()));
    }
}
