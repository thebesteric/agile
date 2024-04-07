package io.github.thebesteric.framework.agile.commons.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * IOUtils
 *
 * @author Eric Joe
 * @version 1.0
 */
public class IOUtils extends AbstractUtils {

    public static byte[] toByteArray(InputStream in) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            org.apache.commons.io.IOUtils.copy(in, bos);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static byte[] toByteArray(Object obj) {
        if (obj == null) return null;
        String jsonStr = JsonUtils.toJson(obj);
        return jsonStr.getBytes(StandardCharsets.UTF_8);
    }

}
