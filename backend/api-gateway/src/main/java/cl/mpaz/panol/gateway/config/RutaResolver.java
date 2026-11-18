package cl.mpaz.panol.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Decide a qué microservicio va cada petición según el primer segmento de la ruta:
 * /api/equipos/3 -> inventory-service /equipos/3. Cualquier otro prefijo (por ejemplo /internal)
 * no tiene ruta y responde 404, así los endpoints internos nunca quedan expuestos.
 */
@Component
@ConfigurationProperties(prefix = "gateway")
public class RutaResolver {

    private Map<String, String> rutas = new HashMap<>();

    public Map<String, String> getRutas() {
        return rutas;
    }

    public void setRutas(Map<String, String> rutas) {
        this.rutas = rutas;
    }

    public record Destino(String baseUrl, String path) {
    }

    /** path llega sin el prefijo /api, por ejemplo "/equipos/3". */
    public Optional<Destino> resolver(String path) {
        if (path == null || !path.startsWith("/") || path.length() < 2) {
            return Optional.empty();
        }
        String sinPrimeraBarra = path.substring(1);
        int fin = sinPrimeraBarra.indexOf('/');
        String segmento = fin < 0 ? sinPrimeraBarra : sinPrimeraBarra.substring(0, fin);
        String base = rutas.get(segmento);
        if (base == null) {
            return Optional.empty();
        }
        return Optional.of(new Destino(base, path));
    }
}
