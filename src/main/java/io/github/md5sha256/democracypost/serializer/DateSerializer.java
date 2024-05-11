package io.github.md5sha256.democracypost.serializer;

import org.spongepowered.configurate.serialize.ScalarSerializer;
import org.spongepowered.configurate.serialize.SerializationException;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.function.Predicate;

public class DateSerializer extends ScalarSerializer<Date> {

    public DateSerializer() {
        super(Date.class);
    }

    @Override
    public Date deserialize(Type type, Object obj) throws SerializationException {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Long time) {
            return new Date(time);
        } else if (obj instanceof String time) {
            if (time.isBlank()) {
                return null;
            }
            try {
                return new Date(Long.parseLong(time));
            } catch (NumberFormatException ex) {
                throw new SerializationException(ex);
            }
        }
        throw new SerializationException("Unsupported type: " + type);
    }

    @Override
    protected Object serialize(Date item, Predicate<Class<?>> typeSupported) {
        if (item == null) {
            return "";
        }
        return String.valueOf(item.getTime());
    }
}
