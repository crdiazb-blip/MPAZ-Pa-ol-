package cl.mpaz.panol.solicitudes.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Clock;
import java.time.Duration;
import java.time.ZoneId;

@Configuration
public class ClientesConfig {

    /** Reloj en la zona horaria del colegio. En las pruebas se reemplaza por un reloj fijo. */
    @Bean
    public Clock reloj(@Value("${panol.zona-horaria:America/Santiago}") String zona) {
        return Clock.system(ZoneId.of(zona));
    }

    @Bean("inventarioRest")
    public RestClient inventarioRest(RestClient.Builder builder, @Value("${services.inventory.url}") String url) {
        return crear(builder, url);
    }

    @Bean("auditoriaRest")
    public RestClient auditoriaRest(RestClient.Builder builder, @Value("${services.audit.url}") String url) {
        return crear(builder, url);
    }

    @Bean("notificacionesRest")
    public RestClient notificacionesRest(RestClient.Builder builder, @Value("${services.notification.url}") String url) {
        return crear(builder, url);
    }

    private RestClient crear(RestClient.Builder builder, String url) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofSeconds(3));
        fabrica.setReadTimeout(Duration.ofSeconds(8));
        return builder.baseUrl(url).requestFactory(fabrica).build();
    }
}
