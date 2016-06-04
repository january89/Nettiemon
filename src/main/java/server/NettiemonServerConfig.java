package server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import java.net.InetSocketAddress;

@Configuration
@ComponentScan("java.server")
@PropertySource("classpath:NettiemonServer.properties")
public class NettiemonServerConfig {

    @Value("${boss.thread.count}")
    private int bossCount;

    @Value("${worker.thread.count}")
    private int workerCount;

    @Value("${tcp.port}")
    private int tcpPort;

    public int getBossCount(){ return bossCount; }
    public int getWorkerCount(){ return workerCount; }
    public int getTcpPort(){ return tcpPort; }

    @Bean(name = "tcpSocketAddress")
    public InetSocketAddress tcpPort(){ return new InetSocketAddress(tcpPort); }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer(){ return new PropertySourcesPlaceholderConfigurer(); }

}
