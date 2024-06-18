package io.github.md5sha256.democracypost.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.md5sha256.democracypost.DatabaseSettings;
import io.github.md5sha256.democracypost.model.PostalPackage;

import javax.annotation.Nonnull;
import java.io.Closeable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class DatabaseAdapter implements Closeable {

    private final DatabaseSettings databaseSettings;
    private final DatabaseSchema schema;

    private HikariDataSource dataSource;

    public DatabaseAdapter(@Nonnull DatabaseSettings databaseSettings) {
        this.databaseSettings = databaseSettings;
        this.schema = new MariaSchema();
    }

    public void init() throws SQLException {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(this.schema.formatJdbcUrl(this.databaseSettings.url()));
        config.setDataSourceClassName(this.schema.dataSourceClassName());
        if (this.databaseSettings.requireAuth()) {
            config.setUsername(this.databaseSettings.username());
            config.setPassword(this.databaseSettings.password());
        }
        config.addDataSourceProperty("prepStmtCacheSize", 250);
        config.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        config.addDataSourceProperty("cachePrepStmts", true);
        config.addDataSourceProperty("useServerPrepStmts", true);
        this.dataSource = new HikariDataSource(config);
        if (this.databaseSettings.initializeSchema()) {
            try (Connection connection = this.dataSource.getConnection()) {
                this.schema.initializeSchema(connection);
            }
        }
    }

    public void close() {
        this.dataSource.close();
    }

    public void transferExpiredPackages() throws SQLException {
        try (Connection connection = this.dataSource.getConnection()) {
            this.schema.cleanupExpiredPackages(connection);
        }
    }

    @Nonnull
    public List<PostalPackage> getPackagesForRecipient(@Nonnull UUID recipient) throws SQLException {
        try (Connection connection = this.dataSource.getConnection()) {
            return this.schema.getPackagesForReceiver(connection, recipient);
        }
    }

    public void addPackage(@Nonnull PostalPackage postalPackage) throws SQLException {
        try (Connection connection = this.dataSource.getConnection()) {
            this.schema.insertPackage(connection, postalPackage);
        }
    }

    public void removePackage(@Nonnull UUID packageId) throws SQLException {
        try (Connection connection = this.dataSource.getConnection()) {
            this.schema.removePackage(connection, packageId);
        }
    }

}
