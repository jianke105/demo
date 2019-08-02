package com.boge.demo.commons;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11AprProtocol;
import org.apache.coyote.http11.Http11NioProtocol;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.server.WebServerFactoryCustomizerBeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * 定制化内嵌tomcat参数,与nginx保持keeplive，以提高访问相应速度，降低重复建连connection的系统消耗
 *
 * @author: create by boge
 * @version: v1.0
 * @description: com.boge.demo
 * @date:2019/7/11
 */
@Component
public class webServerConfig implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {
    @Override
    public void customize(ConfigurableWebServerFactory factory) {
        ((TomcatServletWebServerFactory) factory).addConnectorCustomizers(new TomcatConnectorCustomizer() {
            @Override
            public void customize(Connector connector) {
                Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();
                //KeepAlive连接超时时间
                protocol.setKeepAliveTimeout(30000);
                //最大请求
                protocol.setMaxKeepAliveRequests(10000);
            }
        });
    }
}
