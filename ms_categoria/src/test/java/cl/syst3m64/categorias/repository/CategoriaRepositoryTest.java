package cl.syst3m64.categorias.repository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import cl.syst3m64.categorias.model.Categoria;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Test del repositorio CategoriaRepository en memoria")
public class CategoriaRepositoryTest {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Categoria categoriaEjemplo;

    @BeforeEach
    void setUp() {
        categoriaEjemplo = entityManager.persist(new Categoria(null, "Ciencia Ficción", "Libros de CF"));
        entityManager.flush();
    }

    @Test
    @DisplayName("findAll() retorna todas las categorías persistidas")
    void findAll_debeRetornarTodasLasCategorias() {
        List<Categoria> resultado = categoriaRepository.findAll();

        assertNotNull(resultado);
        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.size());
    }

    @Test
    @DisplayName("findById() retorna la categoría cuando existe")
    void findById_debeRetornarCategoria_cuandoExiste() {
        Optional<Categoria> resultado = categoriaRepository.findById(categoriaEjemplo.getId());

        assertTrue(resultado.isPresent());
        assertEquals("Ciencia Ficción", resultado.get().getNombre());
    }

    @Test
    @DisplayName("findById() retorna vacío cuando no existe")
    void findById_debeRetornarVacio_cuandoNoExiste() {
        Optional<Categoria> resultado = categoriaRepository.findById(999L);

        assertFalse(resultado.isPresent());
    }

    @Test
    @DisplayName("findAllNombres() retorna categorías que coinciden con el nombre")
    void findAllNombres_debeRetornarCategoriasConNombreCoincidente() {
        List<Categoria> resultado = categoriaRepository.findAllNombres("Ciencia Ficción");

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Ciencia Ficción", resultado.get(0).getNombre());
    }

    @Test
    @DisplayName("findAllNombres() retorna lista vacía cuando no hay coincidencias")
    void findAllNombres_debeRetornarListaVacia_sinCoincidencias() {
        List<Categoria> resultado = categoriaRepository.findAllNombres("Terror");

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }
}
