package cl.mpaz.panol.auth.config;

import cl.mpaz.panol.auth.model.Rol;
import cl.mpaz.panol.auth.model.Usuario;
import cl.mpaz.panol.auth.repository.UsuarioRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Usuarios ficticios de prueba (Ley 19.628 y 21.719: no se usan datos reales). */
@Configuration
public class DataInitializer {

    @Bean
    ApplicationRunner cargarUsuarios(UsuarioRepository repo, PasswordEncoder encoder) {
        return args -> {
            crear(repo, encoder, "11111111-1", "Encargado TI Demo", "Encargado TI",
                    "ti.demo@mpaz.test", "admin123", Rol.ADMIN);
            crear(repo, encoder, "12345678-5", "Docente Demo", "Docente",
                    "docente.demo@mpaz.test", "1234", Rol.SOLICITANTE);
            crear(repo, encoder, "22222222-2", "Inspector Demo", "Inspector",
                    "inspector.demo@mpaz.test", "1234", Rol.SOLICITANTE);
            crear(repo, encoder, "13579246-2", "UTP Demo", "UTP",
                    "utp.demo@mpaz.test", "1234", Rol.SOLICITANTE);
        };
    }

    private void crear(UsuarioRepository repo, PasswordEncoder encoder, String rut, String nombre,
                       String cargo, String correo, String clave, Rol rol) {
        if (repo.existsByRut(rut)) {
            return;
        }
        repo.save(Usuario.builder()
                .rut(rut).nombre(nombre).cargo(cargo).correo(correo)
                .passwordHash(encoder.encode(clave)).rol(rol).activo(true)
                .build());
    }
}
