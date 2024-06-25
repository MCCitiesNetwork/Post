package io.github.md5sha256.democracypost;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.UUID;

class TestMariaSchema {

    @Test
    void test() {
        UUID uuid = UUID.randomUUID();
        byte[] bytes = getBytes(uuid);
        Assertions.assertEquals(16, bytes.length);
        UUID deserialized = getUUID(bytes);
        Assertions.assertEquals(uuid, deserialized);
    }

    private UUID getUUID(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        long msb = byteBuffer.getLong();
        long lsb = byteBuffer.getLong();
        return new UUID(msb, lsb);
    }

    private byte[] getBytes(UUID uuid) {
        long lsb = uuid.getLeastSignificantBits();
        long msb = uuid.getMostSignificantBits();
        ByteBuffer byteBuffer = ByteBuffer.allocate(16);
        byteBuffer.putLong(msb);
        byteBuffer.putLong(lsb);
        return byteBuffer.array();
    }

}
