package io.github.md5sha256.democracypost.database;

import javax.annotation.Nullable;
import java.util.Date;

public record PackageExpiryData(int numPackagesAboutToExpire, @Nullable Date nearestExpiryDate) {
}
