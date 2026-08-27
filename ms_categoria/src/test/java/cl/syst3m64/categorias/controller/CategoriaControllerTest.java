package cl.syst3m64.categorias.controller;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import cl.syst3m64.categorias.model.Categoria;
import cl.syst3m64.categorias.service.CategoriaService;

@WebMvcTest(CategoriaController.class)
@DisplayName("Tests del CategoriaController con MockMvc")
public class CategoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoriaService categoriaService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("GET /api/categorias debe retornar 200 con la lista de categorías")
    void listar_debeRetornar200ConListaDeCategorias() throws Exception {
        Categoria cat = new Categoria(1L, "Ciencia Ficción", "Libros de CF");
        when(categoriaService.obtenerTodas()).thenReturn(List.of(cat));

        mockMvc.perform(get("/api/categorias")
            .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].nombre").value("Ciencia Ficción"));
    }

    @Test
    @DisplayName("POST /api/categorias debe retornar 201 con datos válidos")
    void crear_debeRetornar201_cuandoDatosValidos() throws Exception {
        Categoria request = new Categoria(null, "Terror", "Libros de terror");
        Categoria response = new Categoria(1L, "Terror", "Libros de terror");
        when(categoriaService.guardar(any(Categoria.class))).thenReturn(response);

        mockMvc.perform(post("/api/categorias")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.nombre").value("Terror"));
    }
}
