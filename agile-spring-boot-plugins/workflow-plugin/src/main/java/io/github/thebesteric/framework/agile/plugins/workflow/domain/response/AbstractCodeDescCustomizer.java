package io.github.thebesteric.framework.agile.plugins.workflow.domain.response;

import io.github.thebesteric.framework.agile.commons.util.ReflectUtils;
import io.github.thebesteric.framework.agile.core.domain.BaseEnum;
import io.github.thebesteric.framework.agile.core.domain.Pair;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * AbstractCodeDescCustomizer
 *
 * @author wangweijun
 * @version v1.0
 * @since 2025-01-06 17:02:44
 */
@Slf4j
public abstract class AbstractCodeDescCustomizer {

    @SuppressWarnings("unchecked")
    public Map<String, String> toMap(Integer code, String desc) {
        Map<String, String> map = new HashMap<>();
        List<Field> declaredFields = ReflectUtils.getFields(this.getClass(), field -> !ReflectUtils.isStatic(field) && !ReflectUtils.isFinal(field));
        for (Field declaredField : declaredFields) {
            try {
                declaredField.setAccessible(true);
                Pair<Integer, String> pair = (Pair<Integer, String>) declaredField.get(this);
                if (Objects.equals(code, pair.getKey())) {
                    map.put(BaseEnum.CODE_KEY, String.valueOf(pair.getKey()));
                    map.put(BaseEnum.DESC_KEY, pair.getValue());
                    break;
                }
            } catch (IllegalAccessException e) {
                log.warn(e.getMessage(), e);
            }
        }
        if (map.isEmpty()) {
            map.put(BaseEnum.CODE_KEY, String.valueOf(code));
            map.put(BaseEnum.DESC_KEY, desc);
        }
        return map;
    }

}
