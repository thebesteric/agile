package io.github.thebesteric.framework.agile.plugins.logger.processor.response.impl;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.thebesteric.framework.agile.commons.util.ExceptionUtils;
import io.github.thebesteric.framework.agile.commons.util.LoggerPrinter;
import io.github.thebesteric.framework.agile.plugins.logger.config.AgileLoggerProperties;
import io.github.thebesteric.framework.agile.plugins.logger.processor.response.AbstractResponseSuccessDefineProcessor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * DefaultResponseSuccessDefineProcessorProcessor
 * <p>Customize Code and value returned successfully when HttpStatus is 200
 *
 * @author Eric Joe
 * @since 1.0
 */
@Slf4j
public class DefaultResponseSuccessDefineProcessorProcessor extends AbstractResponseSuccessDefineProcessor {

    private static final LoggerPrinter loggerPrinter = LoggerPrinter.newInstance();

    public DefaultResponseSuccessDefineProcessorProcessor() {
        super(DefaultResponseSuccessDefineProcessorProcessor.getDefaultResponseSuccessDefine());
    }

    @Override
    public String doProcessor(JsonNode resultJsonNode, Object result) {
        try {
            // Get code fields
            List<AgileLoggerProperties.Logger.ResponseSuccessDefine.CodeField> codeFields = this.responseSuccessDefine.getCodeFields();

            // Check for a match. If it matches, the program is executing normally
            String codeValue = null;
            for (AgileLoggerProperties.Logger.ResponseSuccessDefine.CodeField codeField : codeFields) {
                String codeName = codeField.getName();
                JsonNode jsonCodeField = getJsonNodeField(resultJsonNode, codeName);
                // Meet the expected results
                if (jsonCodeField != null) {
                    codeValue = jsonCodeField.asText();
                    if (codeValue.equals(String.valueOf(codeField.getValue()))) {
                        // Passed successfully
                        return null;
                    }
                }
            }

            // If the code does not match, look for the error message response field
            List<String> messageFields = this.responseSuccessDefine.getMessageFields();
            for (String messageField : messageFields) {
                JsonNode jsonMessageField = getJsonNodeField(resultJsonNode, messageField);
                // Return the error messages
                if (jsonMessageField != null) {
                    return jsonMessageField.asText();
                }
            }

            // If code has a value, it indicates an exception, otherwise it indicates normal execution
            return codeValue;

        } catch (Exception ex) {
            loggerPrinter.debug(ex.getMessage());
            return ExceptionUtils.getSimpleMessage(ex);
        }
    }
}
