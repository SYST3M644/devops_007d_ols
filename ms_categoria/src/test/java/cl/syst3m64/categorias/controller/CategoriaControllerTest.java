package cl.syst3m64.categorias.controller;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import cl.syst3m64.categorias.exception.CategoriaNotFoundException;
import cl.syst3m64.categorias.model.Categoria;
import cl.syst3m64.categorias.service.CategoriaService;

@WebMvcTest(CategoriaController.class)
@DisplayName("Tests del CategoryController con MockMvc")
public class CategoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoriaService categoriaService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("GET /api/category debe retornar 200 con la lista de category")
    void listar_debeRetornar200ConListaDeCategorias() throws Exception {
        Categoria cat = new Categoria(1L, "Ciencia Ficción", "Libros de CF");
        when(categoriaService.obtenerTodas()).thenReturn(List.of(cat));

        mockMvc.perform(get("/api/category")
            .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].nombre").value("Ciencia Ficción"));
    }

    @Test
    @DisplayName("POST /api/category debe retornar 201 con datos válidos")
    void crear_debeRetornar201_cuandoDatosValidos() throws Exception {
        Categoria request = new Categoria(null, "Terror", "Libros de terror");
        Categoria response = new Categoria(1L, "Terror", "Libros de terror");
        when(categoriaService.guardar(any(Categoria.class))).thenReturn(response);

        mockMvc.perform(post("/api/category")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.nombre").value("Terror"));
    }

    @Test
    @DisplayName("GET /api/category/nombres debe retornar 200 con las category filtradas")
    void obtenerNombres_debeRetornar200ConCategoriasFiltradas() throws Exception {
        when(categoriaService.obtenerNombres("Nuevo")).thenReturn(List.of(new Categoria(1L, "Nuevo", "Libro Nuevo")));

        mockMvc.perform(get("/api/category/nombres").param("nombre", "Nuevo"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].nombre").value("Nuevo"));
    }

    @Test
    @DisplayName("GET /api/category/{id} debe retornar 200 cuando la category existe")
    void obtenerId_debeRetornar200_cuandoExiste() throws Exception {
        when(categoriaService.obtenerPorId(1L)).thenReturn(Optional.of(new Categoria(1L, "Terror", "Libros de terror")));

        mockMvc.perform(get("/api/category/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.nombre").value("Terror"));
    }

    @Test
    @DisplayName("GET /api/category/{id} debe retornar 404 cuando la category no existe")
    void obtenerId_debeRetornar404_cuandoNoExiste() throws Exception {
        when(categoriaService.obtenerPorId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/category/99"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/category debe retornar 400 con el detalle cuando el nombre está vacío")
    void crear_debeRetornar400_cuandoNombreVacio() throws Exception {
        Categoria request = new Categoria(null, "", "Sin nombre");

        mockMvc.perform(post("/api/category")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Validation Failed"))
            .andExpect(jsonPath("$.errors.nombre").value("El nombre es obligatorio"));

        verify(categoriaService, never()).guardar(any(Categoria.class));
    }

    @Test
    @DisplayName("POST /api/category debe retornar 400 cuando el cliente envía un ID")
    void crear_debeRetornar400_cuandoSeEnviaId() throws Exception {
        Categoria request = new Categoria(5L, "Terror", "Libros de terror");

        mockMvc.perform(post("/api/category")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.id").exists());

        verify(categoriaService, never()).guardar(any(Categoria.class));
    }

    @Test
    @DisplayName("POST /api/category debe retornar 400 cuando el servicio lanza RuntimeException")
    void crear_debeRetornar400_cuandoServicioFalla() throws Exception {
        Categoria request = new Categoria(null, "Terror", "Libros de terror");
        when(categoriaService.guardar(any(Categoria.class))).thenThrow(new RuntimeException("Nombre duplicado"));

        mockMvc.perform(post("/api/category")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message").value("Nombre duplicado"));
    }

    @Test
    @DisplayName("GET /api/category/{id} debe retornar 404 con el detalle cuando se lanza CategoriaNotFoundException")
    void obtenerId_debeRetornar404ConDetalle_cuandoSeLanzaNotFound() throws Exception {
        when(categoriaService.obtenerPorId(7L)).thenThrow(new CategoriaNotFoundException(7L));

        mockMvc.perform(get("/api/category/7"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.message").value("Categoria con ID 7 no encontrada"));
    }

    @Test
    @DisplayName("PUT /api/category/{id} debe retornar 200 con la category actualizada")
    void actualizar_debeRetornar200_cuandoExiste() throws Exception {
        Categoria request = new Categoria(null, "Terror", "Actualizada");
        when(categoriaService.actualizar(eq(1L), any(Categoria.class)))
            .thenReturn(Optional.of(new Categoria(1L, "Terror", "Actualizada")));

        mockMvc.perform(put("/api/category/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.descripcion").value("Actualizada"));
    }

    @Test
    @DisplayName("PUT /api/category/{id} debe retornar 404 cuando la category no existe")
    void actualizar_debeRetornar404_cuandoNoExiste() throws Exception {
        Categoria request = new Categoria(null, "Terror", "Actualizada");
        when(categoriaService.actualizar(eq(99L), any(Categoria.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/category/99")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/category/{id} debe retornar 200 y eliminar cuando existe")
    void eliminar_debeRetornar200_cuandoExiste() throws Exception {
        when(categoriaService.obtenerPorId(1L)).thenReturn(Optional.of(new Categoria(1L, "Terror", "Libros de terror")));

        mockMvc.perform(delete("/api/category/1"))
            .andExpect(status().isOk())
            .andExpect(content().string("Category eliminada exitosamente"));

        verify(categoriaService, times(1)).eliminar(1L);
    }

    @Test
    @DisplayName("DELETE /api/category/{id} debe retornar 404 y no eliminar cuando no existe")
    void eliminar_debeRetornar404_cuandoNoExiste() throws Exception {
        when(categoriaService.obtenerPorId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/category/99"))
            .andExpect(status().isNotFound())
            .andExpect(content().string("Category no encontrada con ID: 99"));

        verify(categoriaService, never()).eliminar(99L);
    }
}
