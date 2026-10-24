package cl.syst3m64.categorias.config;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import cl.syst3m64.categorias.model.Categoria;
import cl.syst3m64.categorias.repository.CategoriaRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Test Unit de DataInitializer")
class DataInitializerTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private DataInitializer dataInitializer;

    @Test
    @DisplayName("run() carga las 4 category preconfiguradas cuando la tabla está vacía")
    void run_debeCargarCategorias_cuandoTablaVacia() {
        when(categoriaRepository.count()).thenReturn(0L);

        dataInitializer.run();

        ArgumentCaptor<Categoria> captor = ArgumentCaptor.forClass(Categoria.class);
        verify(categoriaRepository, times(4)).save(captor.capture());
        List<String> nombres = captor.getAllValues().stream().map(Categoria::getNombre).toList();
        assertEquals(List.of("Nuevo", "Usado", "Reparado", "Digital"), nombres);
    }

    @Test
    @DisplayName("run() no carga nada cuando ya existen category")
    void run_noDebeCargarCategorias_cuandoYaExisten() {
        when(categoriaRepository.count()).thenReturn(4L);

        dataInitializer.run();

        verify(categoriaRepository, never()).save(any(Categoria.class));
    }
}
