package server;

import io.netty.handler.codec.json.JsonObjectDecoder;
import org.hibernate.service.spi.ServiceException;

interface HandlersApi{
    void requestParamValidation() throws RequestParamException;
    void service() throws ServiceException;
    void executeService();
}
