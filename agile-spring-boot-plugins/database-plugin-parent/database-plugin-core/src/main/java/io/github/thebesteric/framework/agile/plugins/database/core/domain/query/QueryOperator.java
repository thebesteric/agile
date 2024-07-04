package io.github.thebesteric.framework.agile.plugins.database.core.domain.query;

import lombok.Getter;

@Getter
public enum QueryOperator {
    EQUAL("="),
    NOT_EQUAL("!="),
    GREATER_THAN(">"),
    LESS_THAN("<"),
    GREATER_THAN_OR_EQUAL(">="),
    LESS_THAN_OR_EQUAL("<="),
    LIKE("LIKE"),
    NOT_LIKE("NOT LIKE"),
    IN("IN"),
    NOT_IN("NOT IN"),
    BETWEEN("BETWEEN"),
    NOT_BETWEEN("NOT BETWEEN");

    private final String operator;

    QueryOperator(String operator) {
        this.operator = operator;
    }
}
