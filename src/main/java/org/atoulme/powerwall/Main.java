package org.atoulme.powerwall;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http.HttpClientConfigurer;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;

public class Main {

    public static void main(String[] args) throws Exception {

        CamelContext context = new DefaultCamelContext();
        context.getRegistry().bind("myHttpClientConfigurer", createHttpClientConfigurer());
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() {
                from("cron:tab?schedule=0/2 * * * * ?")
                        .to("https://tesla.home.local/api/meters/aggregates?httpClientConfigurer=myHttpClientConfigurer")
                        .unmarshal().json(JsonLibrary.Jackson)
                        .removeHeaders(".*").
                        to("splunk-hec:localhost:8088/76fe15c3-b827-4690-9f99-4135054a6dd3?skipTlsVerify=true&index=powerwall").end();
            }
        });

        Thread close = new Thread(context::stop);
        Runtime.getRuntime().addShutdownHook(close);

        context.start();
    }

    private static HttpClientConfigurer createHttpClientConfigurer() {
        return clientBuilder -> {
            try {
                SSLContextBuilder builder = new SSLContextBuilder();
                builder.loadTrustMaterial(null, (chain, authType) -> true);
                SSLConnectionSocketFactory sslsf = new
                        SSLConnectionSocketFactory(builder.build(), NoopHostnameVerifier.INSTANCE);
                clientBuilder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).setSSLSocketFactory(sslsf).setConnectionManager(
                        new PoolingHttpClientConnectionManager(
                                RegistryBuilder.<ConnectionSocketFactory>create()
                                        .register("http", PlainConnectionSocketFactory.INSTANCE)
                                        .register("https", sslsf).build()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
}
