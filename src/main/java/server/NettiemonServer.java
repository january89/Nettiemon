package server;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

public class NettiemonServer {

    public static void main(String[] args) throws Exception {

        try(
                AbstractApplicationContext springContext = new AnnotationConfigApplicationContext(NettiemonServerConfig.class)
                ){


            springContext.registerShutdownHook();
            NettiemonBySpring nettiemonServer = springContext.getBean(NettiemonBySpring.class);

            nettiemonServer.start();

        }

    }

}