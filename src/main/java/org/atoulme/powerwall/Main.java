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
        String teslaHost = System.getenv().get("TESLA_HOST");
        String splunkUrl = System.getenv().get("SPLUNK_URL");
        String splunkToken = System.getenv().get("SPLUNK_TOKEN");
        String splunkIndex = System.getenv().get("SPLUNK_INDEX");

        CamelContext context = new DefaultCamelContext();
        context.getRegistry().bind("myHttpClientConfigurer", createHttpClientConfigurer());
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() {
                from("cron:tab?schedule=0/2 * * * * ?")
                        .to("https://" + teslaHost + "/api/meters/aggregates?httpClientConfigurer=myHttpClientConfigurer")
                        .unmarshal().json(JsonLibrary.Jackson)
                        .removeHeaders(".*").
                        to(String.format("%s/%s?skipTlsVerify=true&index=%s", splunkUrl, splunkToken, splunkIndex)).end();
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
