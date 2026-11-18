package cl.mpaz.panol.inventario.config;

import cl.mpaz.panol.inventario.model.Categoria;
import cl.mpaz.panol.inventario.model.Equipo;
import cl.mpaz.panol.inventario.model.EstadoEquipo;
import cl.mpaz.panol.inventario.repository.EquipoRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/** Catálogo ficticio de equipos para poblar la base de datos de pruebas. */
@Configuration
public class DataInitializer {

    @Bean
    ApplicationRunner cargarCatalogo(EquipoRepository repo) {
        return args -> {
            if (repo.count() > 0) {
                return;
            }
            repo.saveAll(List.of(
                    equipo("Notebook Lenovo V15", "Lenovo", "V15", Categoria.NOTEBOOK, 10, 10, EstadoEquipo.DISPONIBLE, "Pañol - Estante A"),
                    equipo("Computador de escritorio", "HP", "ProDesk 400", Categoria.COMPUTADOR, 4, 4, EstadoEquipo.DISPONIBLE, "Pañol - Estante B"),
                    equipo("Proyector Epson X41", "Epson", "X41", Categoria.PROYECTOR, 3, 0, EstadoEquipo.MANTENIMIENTO, "Pañol - Estante C"),
                    equipo("Parlante portátil", "JBL", "Partybox 100", Categoria.PARLANTE, 2, 2, EstadoEquipo.DISPONIBLE, "Pañol - Estante C"),
                    equipo("Alargador 5 metros", "Steren", "AL-5M", Categoria.ALARGADOR, 12, 12, EstadoEquipo.DISPONIBLE, "Pañol - Cajón 1"),
                    equipo("Kit LEGO WeDo 2.0", "LEGO", "WeDo 2.0", Categoria.ROBOTICA, 6, 6, EstadoEquipo.DISPONIBLE, "Pañol - Estante D"),
                    equipo("Placa Arduino UNO", "Arduino", "UNO R3", Categoria.ROBOTICA, 8, 8, EstadoEquipo.DISPONIBLE, "Pañol - Estante D"),
                    equipo("Placa ESP32", "Espressif", "DevKit V1", Categoria.ROBOTICA, 5, 5, EstadoEquipo.DISPONIBLE, "Pañol - Estante D"),
                    equipo("Switch de red 24 puertos", "TP-Link", "TL-SG1024", Categoria.RED, 1, 0, EstadoEquipo.PRESTADO, "Sala de servidores (fija)"),
                    equipo("Router WiFi", "Mikrotik", "hAP ac2", Categoria.RED, 1, 0, EstadoEquipo.FUERA_DE_SERVICIO, "Biblioteca (fija)")
            ));
        };
    }

    private Equipo equipo(String nombre, String marca, String modelo, Categoria categoria,
                          int total, int disponible, EstadoEquipo estado, String ubicacion) {
        return Equipo.builder().nombre(nombre).marca(marca).modelo(modelo).categoria(categoria)
                .stockTotal(total).stockDisponible(disponible).estado(estado).ubicacion(ubicacion)
                .activo(true).build();
    }
}
