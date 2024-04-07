package io.github.thebesteric.framework.agile.core.generator;

import java.util.Locale;
import java.util.UUID;

/**
 * TrackIdGenerator
 *
 * @author Eric Joe
 * @version 1.0
 * @since 2022-08-09 15:49:40
 */
public class DefaultIdGenerator implements IdGenerator {

    private DefaultIdGenerator() {
        super();
    }

    public static DefaultIdGenerator getInstance() {
        return Holder.INSTANCE.getInstance();
    }

    @Override
    public String generate() {
        return UUID.randomUUID().toString().toLowerCase(Locale.ROOT);
    }

    private enum Holder {

        // 枚举类型可以理解为一个常量，加载的时候只会实例化一次，放在方法区中
        INSTANCE;

        private final DefaultIdGenerator instance;

        Holder() {
            this.instance = new DefaultIdGenerator();
        }

        private DefaultIdGenerator getInstance() {
            return this.instance;
        }
    }
}
