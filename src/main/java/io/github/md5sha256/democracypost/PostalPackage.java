package io.github.md5sha256.democracypost;

import javax.annotation.Nonnull;
import java.util.Date;
import java.util.UUID;

public class PostalPackage {

    private final UUID sender;
    private final Date expiryDate;
    private final PackageContent content;
    private boolean claimed;


    public PostalPackage(@Nonnull UUID sender, @Nonnull Date expiryDate, @Nonnull PackageContent content) {
        this(sender, expiryDate, content, false);
    }

    public PostalPackage(@Nonnull UUID sender, @Nonnull Date expiryDate, @Nonnull PackageContent content, boolean claimed) {
        this.sender = sender;
        this.expiryDate = expiryDate;
        this.content = content;
        this.claimed = claimed;
    }

    @Nonnull
    public PostalPackage deepCopy() {
        return new PostalPackage(this.sender, this.expiryDate, this.content.deepCopy(), this.claimed);
    }

    @Nonnull
    public UUID sender() {
        return this.sender;
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
}
