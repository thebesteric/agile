package io.github.thebesteric.framework.agile.core.matcher.clazz;

@FunctionalInterface
public interface ClassMatcher {
    boolean matcher(Class<?> clazz);
}
