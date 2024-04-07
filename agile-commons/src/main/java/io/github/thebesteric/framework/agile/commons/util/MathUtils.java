package io.github.thebesteric.framework.agile.commons.util;

import java.math.BigDecimal;

/**
 * MathUtils
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-03-11 18:44:10
 */
public class MathUtils extends AbstractUtils {
    public static String divStripTrailingZeros(Double d1, Double d2) {
        double result = d1 / d2;
        String resultStr = String.valueOf(result);
        return stripTrailingZeros(resultStr);

    }

    public static String stripTrailingZeros(String str) {
        return new BigDecimal(str).stripTrailingZeros().toPlainString();
    }
}
