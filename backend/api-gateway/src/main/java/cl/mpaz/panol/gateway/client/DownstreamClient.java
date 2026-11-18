package cl.mpaz.panol.gateway.client;

import cl.mpaz.panol.gateway.exception.GatewayException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.net.URI;

/** Reenvía la petición al microservicio y devuelve su respuesta tal cual (incluidos los errores 4xx/5xx). */
@Component
public class DownstreamClient {

    private final RestClient rest;

    public DownstreamClient(RestClient rest) {
        this.rest = rest;
    }

    public ResponseEntity<byte[]> enviar(HttpMethod metodo, URI uri, HttpHeaders cabeceras, byte[] cuerpo) {
        try {
            return rest.method(metodo).uri(uri)
                    .headers(h -> h.addAll(cabeceras))
                    .body(cuerpo == null ? new byte[0] : cuerpo)
                    .exchange((req, res) -> {
                        byte[] datos = res.getBody().readAllBytes();
                        ResponseEntity.BodyBuilder b = ResponseEntity.status(res.getStatusCode());
                        MediaType tipo = res.getHeaders().getContentType();
                        if (tipo != null) {
                            b.contentType(tipo);
                        }
                        return b.body(datos);
                    });
        } catch (ResourceAccessException e) {
            throw new GatewayException(HttpStatus.SERVICE_UNAVAILABLE, "El servicio solicitado no está disponible.");
        }
    }
}
