package io.github.thebesteric.framework.agile.core.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Pair
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-05-16 19:24:55
 */
@Data
@NoArgsConstructor
public class Pair<K, V> {
    private K key;
    private V value;

    private Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public static <K, V> Pair<K, V> of(K key, V value) {
        return new Pair<>(key, value);
    }
}
