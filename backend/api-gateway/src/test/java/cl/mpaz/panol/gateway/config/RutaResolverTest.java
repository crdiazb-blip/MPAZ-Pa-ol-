package cl.mpaz.panol.gateway.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RutaResolverTest {

    private final RutaResolver resolver = new RutaResolver();

    @BeforeEach
    void preparar() {
        resolver.setRutas(Map.of(
                "auth", "http://auth:8081",
                "equipos", "http://inventory:8082",
                "solicitudes", "http://request:8083"));
    }

    @Test
    void enrutaPorElPrimerSegmentoConservandoElPath() {
        RutaResolver.Destino d = resolver.resolver("/equipos/3/estado").orElseThrow();

        assertEquals("http://inventory:8082", d.baseUrl());
        assertEquals("/equipos/3/estado", d.path());
    }

    @Test
    void enrutaElLogin() {
        assertEquals("http://auth:8081", resolver.resolver("/auth/login").orElseThrow().baseUrl());
    }

    @Test
    void losEndpointsInternosNoTienenRuta() {
        assertTrue(resolver.resolver("/internal/equipos/reservar").isEmpty());
    }

    @Test
    void unPrefijoDesconocidoNoTieneRuta() {
        assertTrue(resolver.resolver("/otra-cosa").isEmpty());
    }

    @Test
    void rutasVaciasONulasNoTienenDestino() {
        assertTrue(resolver.resolver("/").isEmpty());
        assertTrue(resolver.resolver("").isEmpty());
        assertTrue(resolver.resolver(null).isEmpty());
    }
}
