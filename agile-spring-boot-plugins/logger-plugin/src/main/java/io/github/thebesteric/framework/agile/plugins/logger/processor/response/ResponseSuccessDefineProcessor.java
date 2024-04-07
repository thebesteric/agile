package io.github.thebesteric.framework.agile.plugins.logger.processor.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.thebesteric.framework.agile.commons.util.JsonUtils;
import io.github.thebesteric.framework.agile.plugins.logger.config.AgileLoggerProperties;

/**
 * ResponseSuccessDefineProcessor
 * <p>Customize Code and value returned successfully when HttpStatus is 200
 *
 * @author Eric Joe
 * @version 1.0
 */
public interface ResponseSuccessDefineProcessor {

    /**
     * process exception message
     * <p>Customize Code and value returned successfully when HttpStatus is 200
     * <p>If no exception occurs, return null
     *
     * @param result result
     * @return exception message
     */
    String processor(Object result) throws JsonProcessingException;


    AgileLoggerProperties.Logger.ResponseSuccessDefine getResponseSuccessDefine();

    void setResponseSuccessDefine(AgileLoggerProperties.Logger.ResponseSuccessDefine responseSuccessDefine);

    /**
     * Get JsonNode for result
     *
     * @param result Object
     * @return JsonNode
     * @throws JsonProcessingException exception
     */
    default JsonNode getResultJsonNode(Object result) throws JsonProcessingException {
        if (result == null) return null;
        String resultJsonStr = JsonUtils.mapper.writeValueAsString(result);
        return JsonUtils.mapper.readTree(resultJsonStr);
    }

    /**
     * Gets the specified fields from JsonNode
     *
     * @param resultJsonNode  JsonNode
     * @param fieldExpression String
     * @return JsonNode
     */
    default JsonNode getJsonNodeField(JsonNode resultJsonNode, String fieldExpression) {
        if (resultJsonNode == null) {
            return null;
        }
        String[] fields = fieldExpression.split("\\.");
        JsonNode jsonCodeField = null;
        for (String field : fields) {
            jsonCodeField = resultJsonNode.get(field);
            if (jsonCodeField != null) {
                break;
            }
        }
        return jsonCodeField;
    }
}
