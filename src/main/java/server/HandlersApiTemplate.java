package server;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.service.spi.ServiceException;
import org.slf4j.Logger;

import java.util.Map;
import java.util.logging.LogManager;

class HandlersApiTemplate implements HandlersApi{
    protected Logger logger;
    protected Map<String,String> reqData;
    ObjectMapper apiResult;

    HandlersApiTemplate(Map<String,String> reqData){
        this.logger = LogManager.getLogManager().getLogger(this.getClass());
        this.apiResult = new ObjectMapper();
        this.reqData = reqData;

        logger.info("request data : " + this.reqData);
    }

    void executeService() {
        try {
            this.requestParamValidation();

            this.service();
        }
        catch (JsonMappingException e) {
            logger.error(e);
            this.apiResult.addProperty("resultCode", "405");
        }
        catch (ServiceException e) {
            logger.error(e);
            this.apiResult.addProperty("resultCode", "501");
        }
    }

    public ObjectMapper getApiResult() {
        return this.apiResult;
    }

    @Override
    public void requestParamValidation() throws RequestParamException {
        if (getClass().getClasses().length == 0) {
            return;
        }

    }

    public final <T extends Enum<T>> T fromValue(Class<T> paramClass, String paramValue) {
        if (paramValue == null || paramClass == null) {
            throw new IllegalArgumentException("There is no value with name '" + paramValue + " in Enum "
                    + paramClass.getClass().getName());
        }

        for (T param : paramClass.getEnumConstants()) {
            if (paramValue.equals(param.toString())) {
                return param;
            }
        }

        throw new IllegalArgumentException("There is no value with name '" + paramValue + " in Enum "
                + paramClass.getClass().getName());
    }


}
