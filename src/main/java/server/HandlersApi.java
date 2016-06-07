package server;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.hibernate.service.spi.ServiceException;
import server.service.RequestParamException;

interface HandlersApi{
    void requestParamValidation() throws RequestParamException;
    void service() throws ServiceException;
    void executeService();
    ObjectNode getApiResult();
}
