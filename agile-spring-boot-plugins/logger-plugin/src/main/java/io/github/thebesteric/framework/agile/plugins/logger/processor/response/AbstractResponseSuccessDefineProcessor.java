package io.github.thebesteric.framework.agile.plugins.logger.processor.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.thebesteric.framework.agile.commons.util.CollectionUtils;
import io.github.thebesteric.framework.agile.plugins.logger.config.AgileLoggerProperties;

/**
 * AbstractResponseCodeReturnedProcessor
 * <p>Customize Code and value returned successfully when HttpStatus is 200
 *
 * @author Eric Joe
 * @since 1.0
 */
public abstract class AbstractResponseSuccessDefineProcessor implements ResponseSuccessDefineProcessor {

    protected AgileLoggerProperties.Logger.ResponseSuccessDefine responseSuccessDefine;

    protected AbstractResponseSuccessDefineProcessor(AgileLoggerProperties.Logger.ResponseSuccessDefine responseSuccessDefine) {
        this.responseSuccessDefine = responseSuccessDefine;
    }

    /**
     * process exception message
     * <p>Customize Code and value returned successfully when HttpStatus is 200
     * <p>If no exception occurs, return null
     *
     * @param resultJsonNode JsonNode
     * @param result         Object
     * @return String
     */
    protected abstract String doProcessor(JsonNode resultJsonNode, Object result);

    @Override
    public AgileLoggerProperties.Logger.ResponseSuccessDefine getResponseSuccessDefine() {
        return this.responseSuccessDefine;
    }

    @Override
    public void setResponseSuccessDefine(AgileLoggerProperties.Logger.ResponseSuccessDefine responseSuccessDefine) {
        this.responseSuccessDefine = responseSuccessDefine;
    }

    @Override
    public String processor(Object result) throws JsonProcessingException {
        // Result converts to JsonNode
        JsonNode resultJsonNode = getResultJsonNode(result);
        return doProcessor(resultJsonNode, result);
    }

    /**
     * Gets the default response codes and message fields
     *
     * @return {@link AgileLoggerProperties.Logger.ResponseSuccessDefine}
     */
    public static AgileLoggerProperties.Logger.ResponseSuccessDefine getDefaultResponseSuccessDefine() {
        AgileLoggerProperties.Logger.ResponseSuccessDefine responseSuccessDefine = new AgileLoggerProperties.Logger.ResponseSuccessDefine();
        responseSuccessDefine.setCodeFields(CollectionUtils.createList(new AgileLoggerProperties.Logger.ResponseSuccessDefine.CodeField("code", 200)));
        responseSuccessDefine.setMessageFields(CollectionUtils.createList("message", "msg"));
        return responseSuccessDefine;
    }
}
