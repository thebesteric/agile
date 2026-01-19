package io.github.thebesteric.framework.agile.commons.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapLikeType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JsonUtils
 *
 * @author Eric Joe
 * @version 1.0
 * @since 2022-07-27 12:17:15
 */
public class JsonUtils extends AbstractUtils {

    private static final LoggerPrinter loggerPrinter = LoggerPrinter.newInstance();

    private static final Pattern PATTERN = Pattern.compile("\\s*|\t|\r|\n");

    public static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
                .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
                .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
                .configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .configure(SerializationFeature.FAIL_ON_SELF_REFERENCES, false)
                // Fix Java 8 date/time type `java.time.LocalDateTime` not supported by default
                .registerModule(new JavaTimeModule())
                .setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
                .setSerializationInclusion(JsonInclude.Include.ALWAYS);
    }

    public static <T> T toObject(String jsonStr, Class<T> clazz) {
        if (jsonStr == null) {
            return null;
        }
        try {
            return MAPPER.readValue(jsonStr, clazz);
        } catch (Exception e) {
            loggerPrinter.error(ExceptionUtils.getSimpleMessage(e));
        }
        return null;
    }

    public static <T> T toObject(Object obj, Class<T> clazz) {
        return toObject(toJson(obj), clazz);
    }

    public static <T> List<T> toList(String jsonStr, Class<T> clazz) {
        if (jsonStr == null) {
            return Collections.emptyList();
        }
        try {
            CollectionType listType = MAPPER.getTypeFactory().constructCollectionType(ArrayList.class, clazz);
            return MAPPER.readValue(jsonStr, listType);
        } catch (IOException e) {
            loggerPrinter.error(ExceptionUtils.getSimpleMessage(e));
        }
        return Collections.emptyList();
    }

    public static <T> List<T> toList(Object obj, Class<T> clazz) {
        return toList(toJson(obj), clazz);
    }

    public static <K, V> Map<K, V> toMap(String jsonStr, Class<K> key, Class<V> value) {
        if (jsonStr == null) {
            return Collections.emptyMap();
        }
        try {
            MapLikeType mapType = MAPPER.getTypeFactory().constructMapLikeType(Map.class, key, value);
            return MAPPER.readValue(jsonStr, mapType);
        } catch (IOException e) {
            loggerPrinter.error(ExceptionUtils.getSimpleMessage(e));
        }
        return Collections.emptyMap();
    }

    public static <K, V> Map<K, V> toMap(Object obj, Class<K> key, Class<V> value) {
        return toMap(toJson(obj), key, value);
    }

    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            loggerPrinter.error(ExceptionUtils.getSimpleMessage(e));
        }
        return null;
    }

    public static JsonNode toJsonNode(String jsonStr) {
        JsonNode jsonNode;
        try {
            jsonNode = MAPPER.readTree(jsonStr);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return jsonNode;
    }

    public static JsonNode toJsonNode(Object obj) {
        return toJsonNode(toJson(obj));
    }

    public static String formatJson(String str) {
        Matcher matcher = PATTERN.matcher(str);
        str = matcher.replaceAll("");
        str = str.replaceAll("\"(\\w+)\"", "$1");
        str = str.replace("\"", "");
        str = str.replace("'", "");
        str = str.replace(":", "\":\"");
        str = str.replace(",", "\",\"");
        str = str.replace("[", "[\"");
        str = str.replace("]", "\"]");
        str = str.replace("\"[", "[");
        str = str.replace("]\"", "]");
        str = str.replace("{", "{\"");
        str = str.replace("}", "\"}");
        str = str.replace("\"{", "{");
        str = str.replace("}\"", "}");
        str = str.replace("[\"{]", "[{");
        str = str.replace("}\"]", "}]");
        str = str.replace("{\"[", "{[");
        str = str.replace("]\"}", "]}");
        str = str.replace("http\":\"", "http:");
        str = str.replace("https\":\"", "https:");
        str = str.replace("ftp\":\"", "ftp:");
        return str;
    }

}
