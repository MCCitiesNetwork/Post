package io.github.md5sha256.democracypost.database;

import io.github.md5sha256.democracypost.model.PostalPackage;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

public interface DatabaseSchema {

    @Nonnull String driverClassName();

    @Nonnull String formatJdbcUrl(@Nonnull String url);

    void initializeSchema(@Nonnull Connection connection) throws SQLException;

    void cleanupExpiredPackages(@Nonnull Connection connection, @Nonnull Duration returnPackageExpiryDuration)
            throws SQLException;

    void insertPackage(@Nonnull Connection connection,
                       @Nonnull PostalPackage postalPackage) throws SQLException;

    void removePackage(@Nonnull Connection connection,
                       @Nonnull UUID packageId) throws SQLException;

    @Nonnull
    List<PostalPackage> getPackagesForReceiver(@Nonnull Connection connection, @Nonnull UUID receiver) throws SQLException;

    @Nonnull
    PackageExpiryData getPackageExpiryData(@Nonnull Connection connection, @Nonnull UUID receiver, @Nonnull Duration fromExpiry)
            throws SQLException;

    boolean isParcelTooLarge(@Nonnull PostalPackage postalPackage);
}
