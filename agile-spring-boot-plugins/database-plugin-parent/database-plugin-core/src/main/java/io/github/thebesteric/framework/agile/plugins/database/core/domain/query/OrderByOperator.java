package io.github.thebesteric.framework.agile.plugins.database.core.domain.query;

import lombok.Getter;

@Getter
public enum OrderByOperator {
    ASC("ASC"),
    DESC("DESC");

    private final String operator;

    OrderByOperator(String operator) {
        this.operator = operator;
    }
}
