package io.github.thebesteric.framework.agile.commons.util;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ConditionMatcher
 *
 * @author wangweijun
 * @version v1.0
 * @since 2025-01-23 19:38:50
 */
public class ConditionMatcher extends AbstractUtils {

    private static final String UNSUPPORTED_OPERATOR_MESSAGE = "Unsupported operator: ";

    public static boolean match(@Nullable String condition, Parameter[] parameters, Object[] arguments) {
        if (StringUtils.isBlank(condition)) {
            return true;
        }
        // 先处理括号，将表达式转换为逆波兰表达式（RPN）
        List<String> rpn = infixToRPN(condition);
        // 计算逆波兰表达式
        return evaluateRPN(rpn, parameters, arguments);
    }

    @SuppressWarnings("unchecked")
    public static String parseExpression(String expr, Parameter[] parameters, Object[] arguments) {
        if (parameters.length != arguments.length) {
            throw new IllegalArgumentException("参数描述数组和参数值数组长度必须一致");
        }
        // 定义正则表达式，用于匹配以 # 开头的占位符
        Pattern pattern = Pattern.compile("#([\\w.]+)");
        Matcher matcher = pattern.matcher(expr);

        StringBuffer sb = new StringBuffer();
        // 遍历匹配结果
        while (matcher.find()) {
            // 获取占位符名称
            String placeholder = matcher.group(1);
            // 从参数映射中获取实际参数值
            String[] paramInfo = placeholder.split("\\.");
            String paramName = paramInfo[0];
            Object value = null;
            for (int i = 0; i < parameters.length; i++) {
                // Map 类型参数
                if (parameters[i].getType() == Map.class) {
                    Map<String, Object> map = (Map<String, Object>) arguments[i];
                    String key = paramInfo[1];
                    if (map.containsKey(key)) {
                        value = map.get(key);
                    }
                }
                // 对象类型参数
                else if (parameters[i].getName().equals(paramName)) {
                    Object paramValue = arguments[i];
                    value = getNestedFieldValue(paramValue, paramInfo, 1);
                }
            }
            if (value != null) {
                // 将占位符替换为实际参数值
                matcher.appendReplacement(sb, Matcher.quoteReplacement(value.toString()));
            } else {
                // 如果参数值为空，保留占位符
                matcher.appendReplacement(sb, matcher.group());
            }
        }
        // 追加剩余的字符串
        matcher.appendTail(sb);

        // 将字符串转换为单个字符串
        String result = sb.toString();
        String[] arr = result.split("\\+");
        // 通过 stream 转换为字符串
        return Arrays.stream(arr).map(String::trim).reduce((a, b) -> a + b).orElse("");
    }

    private static boolean matchSingleCondition(@Nonnull String condition, Parameter[] parameters, Object[] arguments) {
        // 检查是否为布尔字面量
        if ("true".equalsIgnoreCase(condition) || "false".equalsIgnoreCase(condition)) {
            return Boolean.parseBoolean(condition);
        }

        // 解析 condition 表达式
        String operator = extractOperator(condition);
        String[] parts = splitByOperator(condition, operator);
        String paramExpression = parts[0].trim();
        String valueToCompare = parts[1].trim();

        // 表达式为 # 开头，则取 parameters 中对应的类型的值进行判断
        if (paramExpression.startsWith("#")) {
            String[] paramInfo = paramExpression.substring(1).split("\\.");
            String paramName = paramInfo[0];
            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i].getName().equals(paramName)) {
                    Object paramValue = arguments[i];
                    Object finalValue = getNestedFieldValue(paramValue, paramInfo, 1);
                    if (finalValue != null) {
                        return compareValues(finalValue, valueToCompare, operator);
                    }
                }
            }
        }
        // 表达式为没有以 # 开头，则直接判断字面量，如：1 == 1，或 true
        else {
            return compareValues(paramExpression, valueToCompare, operator);
        }
        return false;
    }

    /**
     * 将中缀表达式转换为逆波兰表达式（RPN）
     * 中缀表达式：(3+4)*5 对应的逆波兰表达式是：34+5*
     */
    private static List<String> infixToRPN(String condition) {
        List<String> output = new ArrayList<>();
        Deque<String> operators = new ArrayDeque<>();

        // 按逻辑运算符和括号拆分条件表达式
        List<String> tokens = splitByOperatorsAndParentheses(condition);

        for (String token : tokens) {
            if (isOperator(token)) {
                while (!operators.isEmpty() && isOperator(operators.peek()) && precedence(operators.peek()) >= precedence(token)) {
                    output.add(operators.pop());
                }
                operators.push(token);
            } else if ("(".equals(token)) {
                operators.push(token);
            } else if (")".equals(token)) {
                while (!operators.isEmpty() && !"(".equals(operators.peek())) {
                    output.add(operators.pop());
                }
                if (!operators.isEmpty() && "(".equals(operators.peek())) {
                    // 弹出左括号
                    operators.pop();
                }
            } else {
                output.add(token);
            }
        }

        while (!operators.isEmpty()) {
            output.add(operators.pop());
        }

        return output;
    }

    /**
     * 计算逆波兰表达式
     */
    private static boolean evaluateRPN(List<String> rpn, Parameter[] parameters, Object[] arguments) {
        Deque<Boolean> stack = new ArrayDeque<>();

        for (String token : rpn) {
            if (isOperator(token)) {
                boolean right = stack.pop();
                boolean left = stack.pop();
                boolean result = switch (token) {
                    case "&&", "AND" -> left && right;
                    case "||", "OR" -> left || right;
                    default -> throw new IllegalArgumentException(UNSUPPORTED_OPERATOR_MESSAGE + token);
                };
                stack.push(result);
            } else {
                boolean subResult = matchSingleCondition(token, parameters, arguments);
                stack.push(subResult);
            }
        }

        return stack.pop();
    }

    /**
     * 按逻辑运算符和括号拆分条件表达式
     */
    private static List<String> splitByOperatorsAndParentheses(String condition) {
        // 定义正则表达式，匹配 &&、||、AND、OR（不区分大小写）、括号并保留前后空格
        String regex = "\\s*(?i)(AND|OR|&&|\\|\\||\\(|\\))\\s*";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(condition);

        List<String> parts = new ArrayList<>();
        int lastIndex = 0;

        while (matcher.find()) {
            // 获取匹配到的逻辑运算符或括号前的部分
            String part = condition.substring(lastIndex, matcher.start()).trim();
            if (!part.isEmpty()) {
                parts.add(part);
            }
            // 获取匹配到的逻辑运算符或括号
            String operator = matcher.group().trim();
            parts.add(operator);
            lastIndex = matcher.end();
        }

        // 添加最后一个部分
        if (lastIndex < condition.length()) {
            String lastPart = condition.substring(lastIndex).trim();
            if (!lastPart.isEmpty()) {
                parts.add(lastPart);
            }
        }

        return parts;
    }

    /**
     * 判断是否为逻辑运算符
     */
    private static boolean isOperator(String token) {
        // 将 token 转换为大写，方便不区分大小写比较
        String upperCaseToken = token.toUpperCase();
        return "&&".equals(upperCaseToken) || "||".equals(upperCaseToken) || "AND".equals(upperCaseToken) || "OR".equals(upperCaseToken);
    }

    /**
     * 获取运算符的优先级
     */
    private static int precedence(String operator) {
        if (operator == null) {
            return 0;
        }
        // 将 operator 转换为大写，方便不区分大小写比较
        String upperCaseOperator = operator.toUpperCase();
        if ("&&".equals(upperCaseOperator) || "AND".equals(upperCaseOperator) || "||".equals(upperCaseOperator) || "OR".equals(upperCaseOperator)) {
            return 1;
        }
        return 0;
    }

    private static String extractOperator(String condition) {
        String[] operators = {"==", "!=", ">", "<", ">=", "<="};
        for (String operator : operators) {
            if (condition.contains(operator)) {
                return operator;
            }
        }
        return null;
    }

    private static String[] splitByOperator(String condition, String operator) {
        return condition.split(operator, 2);
    }

    private static boolean compareValues(Object left, String right, String operator) {
        if (left instanceof Number number && isNumeric(right)) {
            double leftValue = number.doubleValue();
            double rightValue = Double.parseDouble(right);
            return switch (operator) {
                case "==" -> leftValue == rightValue;
                case "!=" -> leftValue != rightValue;
                case ">" -> leftValue > rightValue;
                case "<" -> leftValue < rightValue;
                case ">=" -> leftValue >= rightValue;
                case "<=" -> leftValue <= rightValue;
                default -> throw new IllegalArgumentException(UNSUPPORTED_OPERATOR_MESSAGE + operator);
            };
        } else {
            return switch (operator) {
                case "==" -> left.toString().equals(right);
                case "!=" -> !left.toString().equals(right);
                default -> throw new IllegalArgumentException(UNSUPPORTED_OPERATOR_MESSAGE + operator);
            };
        }
    }

    private static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static Object getNestedFieldValue(Object obj, String[] fieldNames, int index) {
        if (obj == null || index >= fieldNames.length) {
            return obj;
        }
        try {
            Field field = obj.getClass().getDeclaredField(fieldNames[index]);
            field.setAccessible(true);
            Object fieldValue = field.get(obj);
            return getNestedFieldValue(fieldValue, fieldNames, index + 1);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return null;
        }
    }
}