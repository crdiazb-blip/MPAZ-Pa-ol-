package cl.mpaz.panol.gateway.controller;

import cl.mpaz.panol.gateway.client.DownstreamClient;
import cl.mpaz.panol.gateway.client.TokenValidator;
import cl.mpaz.panol.gateway.config.RutaResolver;
import cl.mpaz.panol.gateway.exception.GatewayException;
import cl.mpaz.panol.gateway.model.UsuarioToken;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Punto de entrada único de la aplicación móvil (/api/**).
 * 1) Resuelve el microservicio destino. 2) Valida el token (salvo en el login).
 * 3) Reenvía la petición con la identidad del usuario en cabeceras X-User-*.
 */
@RestController
public class ProxyController {

    static final String PREFIJO = "/api";

    private final RutaResolver rutas;
    private final TokenValidator validador;
    private final DownstreamClient downstream;

    public ProxyController(RutaResolver rutas, TokenValidator validador, DownstreamClient downstream) {
        this.rutas = rutas;
        this.validador = validador;
        this.downstream = downstream;
    }

    @RequestMapping("/api/**")
    public ResponseEntity<byte[]> reenviar(HttpServletRequest request, @RequestBody(required = false) byte[] cuerpo) {
        String path = request.getRequestURI().substring(PREFIJO.length());
        RutaResolver.Destino destino = rutas.resolver(path)
                .orElseThrow(() -> new GatewayException(HttpStatus.NOT_FOUND, "Ruta no encontrada."));
        HttpMethod metodo = HttpMethod.valueOf(request.getMethod());

        HttpHeaders salida = new HttpHeaders();
        copiar(request, salida, HttpHeaders.CONTENT_TYPE);
        copiar(request, salida, HttpHeaders.ACCEPT);

        if (!esPublica(metodo, path)) {
            String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
            UsuarioToken usuario = validador.validar(authorization);
            agregarIdentidad(salida, usuario);
            if (path.startsWith("/auth/")) {
                salida.set(HttpHeaders.AUTHORIZATION, authorization);
            }
        }

        String query = request.getQueryString();
        URI uri = URI.create(destino.baseUrl() + destino.path() + (query != null ? "?" + query : ""));
        return downstream.enviar(metodo, uri, salida, cuerpo);
    }

    /** Única ruta sin token: el inicio de sesión. */
    static boolean esPublica(HttpMethod metodo, String path) {
        return HttpMethod.POST.equals(metodo) && "/auth/login".equals(path);
    }

    /**
     * Las cabeceras X-User-* del cliente nunca se copian (se descartan para evitar suplantación);
     * solo se envían las construidas a partir del token validado.
     */
    private void agregarIdentidad(HttpHeaders salida, UsuarioToken u) {
        salida.set("X-User-Id", String.valueOf(u.id()));
        salida.set("X-User-Rut", u.rut());
        salida.set("X-User-Rol", u.rol());
        salida.set("X-User-Nombre", codificar(u.nombre()));
        salida.set("X-User-Cargo", codificar(u.cargo()));
        salida.set("X-User-Correo", codificar(u.correo()));
    }

    private static String codificar(String valor) {
        return valor == null ? "" : URLEncoder.encode(valor, StandardCharsets.UTF_8);
    }

    private static void copiar(HttpServletRequest request, HttpHeaders salida, String nombre) {
        String valor = request.getHeader(nombre);
        if (valor != null) {
            salida.set(nombre, valor);
        }
    }
}
