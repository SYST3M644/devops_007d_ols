package cl.syst3m64.categorias.service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import cl.syst3m64.categorias.model.Categoria;
import cl.syst3m64.categorias.repository.CategoriaRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Test Unit de CategoriaService")
public class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    private Categoria categoriaEjemplo;

    @BeforeEach
    void setUp() {
        categoriaEjemplo = new Categoria(1L, "Ciencia Ficción", "Libros de CF");
    }

    @Test
    @DisplayName("obtenerTodas() retorna la lista de todas las categorías")
    void obtenerTodas_debeRetornarListaDeCategorias() {
        when(categoriaRepository.findAll()).thenReturn(List.of(categoriaEjemplo));

        List<Categoria> resultado = categoriaService.obtenerTodas();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Ciencia Ficción", resultado.get(0).getNombre());

        verify(categoriaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("obtenerTodas() debe retornar lista vacía cuando no hay categorías")
    void obtenerTodas_debeRetornarListaVacia_siNoHayCategorias() {
        when(categoriaRepository.findAll()).thenReturn(List.of());

        List<Categoria> resultado = categoriaService.obtenerTodas();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("guardar() debe persistir y retornar la categoría guardada")
    void guardar_debeRetornarCategoriaGuardada() {
        Categoria nueva = new Categoria(null, "Terror", "Libros de terror");
        Categoria guardada = new Categoria(1L, "Terror", "Libros de terror");
        when(categoriaRepository.save(nueva)).thenReturn(guardada);

        Categoria resultado = categoriaService.guardar(nueva);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Terror", resultado.getNombre());

        verify(categoriaRepository, times(1)).save(nueva);
    }
}
