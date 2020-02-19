package org.springframework.util;

import java.util.Collection;

public class CollectionUtils {

    public CollectionUtils() { }

    public static boolean isEmpty(Collection collection) {
        return collection == null || collection.isEmpty();
    }
}
