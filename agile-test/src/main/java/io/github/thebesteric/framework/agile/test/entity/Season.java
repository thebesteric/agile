package io.github.thebesteric.framework.agile.test.entity;

public enum Season {
    SPRING(1, "春天"),
    SUMMER(2, "夏天"),
    AUTUMN(3, "秋天"),
    WINTER(4, "冬天");

    private final Integer code;
    private final String name;

    Season(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
