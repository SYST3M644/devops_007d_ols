package cl.syst3m64.categorias.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import cl.syst3m64.categorias.model.Categoria;
import cl.syst3m64.categorias.repository.CategoriaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoriaService {
    private final CategoriaRepository categoriaRepository;

    public List<Categoria> obtenerTodas() {
        log.info("[CategoriaService] Consultando todas las categorías disponibles");
        return categoriaRepository.findAll();
    }

    public List<Categoria> obtenerNombres(String nombre) {
        log.debug("[CategoriaService] Filtrando categorías por nombre: '{}'", nombre);
        return categoriaRepository.findAllNombres(nombre);
    }

    public Categoria guardar(Categoria categoria) {
        log.info("[CategoriaService] Creando categoría: {}", categoria.getNombre());
        Categoria guardadas = categoriaRepository.save(categoria);
        log.info("[CategoriaService] Categoría creada con ID: {}", guardadas.getId());
        return guardadas;
    }

    public Optional<Categoria> obtenerPorId(Long id) {
        log.info("[CategoriaService] Buscando categoría con ID: {}", id);

        Optional<Categoria> resultado = categoriaRepository.findById(id);
        if (resultado.isEmpty()) {
            log.warn("[CategoriaService] Categoría con ID {} no encontrada con el id: ", id);
        }

        return resultado;
    }

    public Optional<Categoria> actualizar(Long id, Categoria categoria) {
        log.info("[CategoriaService] Actualizando categoría ID: {}", id);
        return categoriaRepository.findById(id).map(existing -> {
            existing.setNombre(categoria.getNombre());
            existing.setDescripcion(categoria.getDescripcion());
            Categoria actualizada = categoriaRepository.save(existing);
            log.info("[CategoriaService] Categoría actualizada: {}", actualizada.getNombre());
            return actualizada;
        });
    }

    public void eliminar(Long id) {
        log.info("[CategoriaService] Eliminando categoría ID: {}", id);
        categoriaRepository.deleteById(id);
        log.info("[CategoriaService] Categoría ID {} eliminada correctamente", id);
    }
}
