package cl.mpaz.panol.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class RestClientConfig {

    /** Cliente HTTP del JDK: a diferencia de HttpURLConnection, soporta el método PATCH. */
    @Bean
    public RestClient restClient(RestClient.Builder builder,
                                 @Value("${gateway.timeout-segundos:15}") long timeoutSegundos) {
        HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
        JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(http);
        fabrica.setReadTimeout(Duration.ofSeconds(timeoutSegundos));
        return builder.requestFactory(fabrica).build();
    }
}
