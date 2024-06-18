package io.github.md5sha256.democracypost.database;

import io.github.md5sha256.democracypost.model.PackageContent;
import io.github.md5sha256.democracypost.model.PostalPackage;
import io.github.md5sha256.democracypost.serializer.Serializers;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class MariaSchema implements DatabaseSchema {


    private static final String CREATE_POST_PACKAGES_TABLE = """
            CREATE TABLE POST_PACKAGE (
              package_id BINARY(16) NOT NULL,
              package_expiry_date DATETIME NOT NULL,
              package_is_return BOOLEAN DEFAULT FALSE NOT NULL,
              package_sender BINARY(16) NOT NULL,
              package_receiver BINARY(16) NOT NULL,
              package_content BLOB NOT NULL
            );
            """;

    private static final String CREATE_POST_PACKAGES_CONSTRAINTS = """
            ALTER TABLE
              POST_PACKAGE
            ADD
              CONSTRAINT post_packages_pk PRIMARY KEY(package_id);
            """;

    private static final String CREATE_POST_PACKAGES_INDEX = """
            CREATE INDEX post_package_id_expiry_date_index
            ON POST_PACKAGE(package_id, package_expiry_date);
            """;

    private static final String UPDATE_EXPIRED_PACKAGES = """
            UPDATE POST_PACKAGE
            SET
              package_receiver = package_sender,
              package_expiry = ?
            WHERE package_expiry >= ?
            );
            """;

    private static final String DELETE_EXPIRED_RETURN_PACKAGES = """
            DELETE FROM POST_PACKAGE
            WHERE package_is_return = TRUE AND package_expiry >= ?;
            """;

    private static final String SELECT_PACKAGES_FOR_RECEIVER = """
            SELECT package_id, package_expiry_date, package_is_return, package_sender, package_content
            FROM POST_PACKAGE
            WHERE package_sender = ?;
            """;

    private static final String INSERT_PACKAGE = """
            INSERT INTO POST_PACKAGE
                (package_id, package_expiry_date, package_is_return, package_sender, package_receiver, package_content)
            VALUES
                (?, ?, ?, ?, ?, ?);
            """;

    private static final String DELETE_PACKAGE = """
            DELETE FROM POST_PACKAGE
            WHERE package_id = ?;
            """;

    @NotNull
    @Override
    public String dataSourceClassName() {
        return "org.mariadb.jdbc.Driver";
    }

    @NotNull
    @Override
    public String formatJdbcUrl(@NotNull String url) {
        return "jdbc:mariadb://" + url;
    }

    @Override
    public void initializeSchema(@Nonnull Connection connection) throws SQLException {
        try (PreparedStatement statementCreateTables = connection.prepareStatement(CREATE_POST_PACKAGES_TABLE);
             PreparedStatement statementCreateConstraints = connection.prepareStatement(CREATE_POST_PACKAGES_CONSTRAINTS);
             PreparedStatement statementCreateIndexes = connection.prepareStatement(CREATE_POST_PACKAGES_INDEX)) {
            statementCreateTables.execute();
            statementCreateConstraints.execute();
            statementCreateIndexes.execute();
        }
    }

    @Override
    public void cleanupExpiredPackages(@Nonnull Connection connection) throws SQLException {
        try (PreparedStatement updateStatement = connection.prepareStatement(UPDATE_EXPIRED_PACKAGES);
        PreparedStatement deletionStatement = connection.prepareStatement(DELETE_EXPIRED_RETURN_PACKAGES);){
            updateStatement.executeUpdate();
            deletionStatement.setTimestamp(1, Timestamp.from(Instant.now()));
            deletionStatement.executeUpdate();
        }
    }

    @Override
    public void insertPackage(@Nonnull Connection connection,
                              @Nonnull PostalPackage postalPackage) throws SQLException {
        PackageContent content = postalPackage.content();
        try (PreparedStatement statement = connection.prepareStatement(INSERT_PACKAGE)) {
            statement.setBytes(1, getBytes(postalPackage.id()));
            statement.setTimestamp(2, Timestamp.from(postalPackage.expiryDate().toInstant()));
            statement.setBoolean(3, postalPackage.isReturnPackage());
            statement.setBytes(4, getBytes(content.sender()));
            statement.setBytes(5, getBytes(content.receiver()));
            statement.setBlob(6, new ByteArrayInputStream(getBytes(content.items())));
            statement.executeUpdate();
        }
    }

    @Override
    public void removePackage(@NotNull Connection connection, @NotNull UUID packageId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_PACKAGE)) {
            statement.setBytes(1, getBytes(packageId));
            statement.executeUpdate();
        }
    }

    @Override
    @Nonnull
    public List<PostalPackage> getPackagesForReceiver(@Nonnull Connection connection, @Nonnull UUID receiver) throws SQLException {
        List<PostalPackage> packages = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(SELECT_PACKAGES_FOR_RECEIVER)) {
            statement.setBytes(1, getBytes(receiver));
            try (ResultSet resultSet = statement.executeQuery();) {
                while (resultSet.next()) {
                    PostalPackage postalPackage = readPackage(resultSet, receiver);
                    packages.add(postalPackage);
                }
            }
        }
        return packages;
    }

    @Nonnull
    private PostalPackage readPackage(@Nonnull ResultSet resultSet, @Nonnull UUID receiver) throws SQLException {
        // package_id, package_expiry_date, package_is_return, package_sender, package_content
        UUID packageId = getUUID(resultSet.getBytes(1));
        Date expiryDate = Date.from(resultSet.getTimestamp(2).toInstant());
        boolean isReturn = resultSet.getBoolean(3);
        UUID sender = getUUID(resultSet.getBytes(4));
        Blob contentBlob = resultSet.getBlob(5);
        List<ItemStack> itemStacks;
        try (InputStream inputStream = contentBlob.getBinaryStream()){
            byte[] bytes = inputStream.readAllBytes();
            itemStacks = getItemStacks(bytes);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read item stacks", ex);
        }
        PackageContent content = new PackageContent(sender, receiver, itemStacks);
        return new PostalPackage(packageId, expiryDate, content, isReturn);
    }

    @Nonnull
    private List<ItemStack> getItemStacks(@Nonnull byte[] bytes) {
        String s = new String(bytes, StandardCharsets.UTF_8);
        var builder = GsonConfigurationLoader.builder()
                .defaultOptions(options -> options.serializers(Serializers.defaults()));
        try {
            ConfigurationNode node = builder.buildAndLoadString(s);
            return node.getList(ItemStack.class, Collections.emptyList());
        } catch (ConfigurateException ex) {
            throw new IllegalStateException("Failed to deserialize item stacks!", ex);
        }
    }

    @Nonnull
    private byte[] getBytes(@Nonnull List<ItemStack> itemStacks) {
        var builder = GsonConfigurationLoader.builder()
                .defaultOptions(options -> options.serializers(Serializers.defaults()));
        var loader = builder.build();
        ConfigurationNode root = loader.createNode();
        try {
            root.setList(ItemStack.class, itemStacks);
            String s = builder.buildAndSaveString(root);
            return s.getBytes(StandardCharsets.UTF_8);
        } catch (ConfigurateException ex) {
            throw new IllegalStateException("Failed to serialize item stacks!", ex);
        }
    }

    @Nonnull
    private byte[] getBytes(@Nonnull UUID uuid) {
        long lsb = uuid.getLeastSignificantBits();
        long msb = uuid.getMostSignificantBits();
        ByteBuffer byteBuffer = ByteBuffer.allocate(16);
        byteBuffer.putLong(msb);
        byteBuffer.putLong(lsb);
        return byteBuffer.array();
    }

    @Nonnull
    private UUID getUUID(@Nonnull byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        long msb = byteBuffer.getLong();
        long lsb = byteBuffer.getLong();
        return new UUID(msb, lsb);
    }

}
