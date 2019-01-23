package com.symmetrylabs.util;

import java.util.Objects;

public final class ClassUtils {

    public static <AbstractType, ConcreteType extends AbstractType> AbstractType tryCreateObject(Class<ConcreteType> type) {
        Objects.requireNonNull(type);
        try {
            return type.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <AbstractType, ConcreteType extends AbstractType> AbstractType tryCreateIfNull(AbstractType obj, Class<ConcreteType> type) {
        return obj != null ? obj : tryCreateObject(type);
    }

    public static <SourceType, DestinationType extends SourceType> DestinationType tryCast(SourceType obj, Class<DestinationType> type) {
        try {
            return type.cast(obj);
        } catch (ClassCastException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <SourceType, DestinationType extends SourceType> DestinationType tryCastOrCreate(SourceType obj, Class<DestinationType> type) {
        return tryCast(tryCreateIfNull(obj, type), type);
    }

}
