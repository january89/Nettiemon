package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.hibernate.service.spi.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.service.RequestParamException;

import java.util.Map;

class HandlersApiTemplate implements HandlersApi{

    protected Logger logger;
    protected Map<String,String> reqData;
    protected ObjectNode apiResult;


    HandlersApiTemplate(Map<String,String> reqData){
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.apiResult = new ObjectMapper().createObjectNode();
        this.reqData = reqData;

        logger.info("request data : " + this.reqData);
    }

    public void executeService(){
        try {

            this.requestParamValidation();

            this.service();

        }
        catch ( ServiceException | RequestParamException e ) {
            logger.warn(e.toString());
            this.apiResult.put("resultCode", "405");
        }
    }

    public ObjectNode getApiResult() {
        return this.apiResult;
    }

    @Override
    public void requestParamValidation() throws RequestParamException {
        if (getClass().getClasses().length == 0) {
            return;
        }
    }

    @Override
    public void service() throws ServiceException {

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
