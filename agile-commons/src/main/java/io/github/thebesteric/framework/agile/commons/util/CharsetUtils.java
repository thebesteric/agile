package io.github.thebesteric.framework.agile.commons.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;

/**
 * CharsetUtils
 *
 * @author Eric Joe
 * @version 1.0
 */
public class CharsetUtils extends AbstractUtils {

    public static final String ISO_8859_1 = "ISO-8859-1";
    public static final String UTF_8 = "UTF-8";
    public static final String GBK = "GBK";
    public static final Charset CHARSET_ISO_8859_1 = StandardCharsets.ISO_8859_1;
    public static final Charset CHARSET_UTF_8 =  StandardCharsets.UTF_8;
    public static final Charset CHARSET_GBK;

    static {
        Charset _CHARSET_GBK = null;
        try {
            _CHARSET_GBK = Charset.forName("GBK");
        } catch (UnsupportedCharsetException ignore) {
        }
        CHARSET_GBK = _CHARSET_GBK;
    }

    public static Charset charset(String charsetName) throws UnsupportedCharsetException {
        return StringUtils.isEmpty(charsetName) ? Charset.defaultCharset() : Charset.forName(charsetName);
    }

    public static Charset parse(String charsetName) {
        return parse(charsetName, Charset.defaultCharset());
    }

    public static Charset parse(String charsetName, Charset defaultCharset) {
        if (StringUtils.isEmpty(charsetName)) {
            return defaultCharset;
        } else {
            Charset result;
            try {
                result = Charset.forName(charsetName);
            } catch (UnsupportedCharsetException var4) {
                result = defaultCharset;
            }
            return result;
        }
    }

}
