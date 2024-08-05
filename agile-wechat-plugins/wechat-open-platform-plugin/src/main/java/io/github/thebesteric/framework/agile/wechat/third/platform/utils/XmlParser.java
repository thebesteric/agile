package io.github.thebesteric.framework.agile.wechat.third.platform.utils;

import io.github.thebesteric.framework.agile.commons.util.AbstractUtils;
import io.github.thebesteric.framework.agile.wechat.third.platform.domain.xml.AbstractEvent;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;

import java.io.StringReader;

/**
 * XmlParser
 *
 * @author wangweijun
 * @version v1.0
 * @since 2024-07-29 11:45:02
 */
public class XmlParser extends AbstractUtils {
    @SuppressWarnings("unchecked")
    public static <T extends AbstractEvent> T parse(String xmlStr, Class<T> xmlClass) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(xmlClass);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (T) unmarshaller.unmarshal(new StringReader(xmlStr));
    }
}
