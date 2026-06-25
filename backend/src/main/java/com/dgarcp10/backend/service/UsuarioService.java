package com.dgarcp10.backend.service;
import com.dgarcp10.backend.model.Usuario;
import com.dgarcp10.backend.repository.UsuarioRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;

@Service
public class UsuarioService {
    
    private final UsuarioRepository repo;
    private final BCryptPasswordEncoder encoder;
    public UsuarioService(UsuarioRepository repo, BCryptPasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }
    public Usuario crear(Usuario usuario) {
        usuario.setPasswordHash(encoder.encode(usuario.getPasswordHash()));
        usuario.setCreadoEn(Instant.now());
        return repo.save(usuario);
    }
    public Usuario obtenerPorId(Long id) {
        return repo.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + id));
    }
    public List<Usuario> listarTodos() {
        return repo.findAll();
    }
    public Usuario actualizar(Long id, Usuario datos) {
        Usuario existente = obtenerPorId(id);
        existente.setUsername(datos.getUsername());
        existente.setEmail(datos.getEmail());
        existente.setNombre(datos.getNombre());
        existente.setApellido(datos.getApellido());
        existente.setTelefono(datos.getTelefono());
        existente.setAvatarPath(datos.getAvatarPath());
        existente.setRol(datos.getRol());
        existente.setPasswordTemporal(datos.getPasswordTemporal());
        if (datos.getPasswordHash() != null && !datos.getPasswordHash().isBlank()) {
            existente.setPasswordHash(encoder.encode(datos.getPasswordHash()));
        }
        return repo.save(existente);
    }
    public void eliminar(Long id) {
        if (!repo.existsById(id)) {
            throw new RuntimeException("Usuario no encontrado: " + id);
        }
        repo.deleteById(id);
    }
}