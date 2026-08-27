package cl.syst3m64.categorias.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.syst3m64.categorias.model.Categoria;
import cl.syst3m64.categorias.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Slf4j
@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
@Tag(name = "Categorías", description = "Operaciones CRUD del catálogo de categorías")
public class CategoriaController {
    private final CategoriaService categoriaService;

    @Operation(summary = "Listar todas las categorías", description = "Retorna la lista completa de categorías")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de categorías retornada correctamente",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Categoria.class)))
    })
    @GetMapping
    public List<Categoria> obtenerTodas() {
        log.info("[CategoriaController] GET /api/categorias");
        return categoriaService.obtenerTodas();
    }

    @Operation(summary = "Buscar categorías por nombre", description = "Filtra categorías por nombre exacto")
    @GetMapping("/nombres")
    public List<Categoria> obtenerNombresCat(
        @Parameter(description = "Nombre de la categoría a buscar", example = "Nuevo") @RequestParam String nombre) {
        log.info("[CategoriaController] GET /api/categorias/nombres?nombre={}", nombre);
        return categoriaService.obtenerNombres(nombre);
    }

    @Operation(summary = "Obtener categoría por ID", description = "Retorna la categoría con el ID indicado")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Categoría encontrada",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Categoria.class))),
        @ApiResponse(responseCode = "404", description = "Categoría no encontrada",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @GetMapping("/{id}")
    public ResponseEntity<Categoria> obtenerId(
        @Parameter(description = "ID de la categoría", example = "1", required = true) @PathVariable Long id) {
        log.info("[CategoriaController] GET /api/categorias/{}", id);
        return categoriaService.obtenerPorId(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Crear nueva categoría", description = "Crea una categoría con los datos proporcionados")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Categoría creada correctamente",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Categoria.class))),
        @ApiResponse(responseCode = "400", description = "Error de validación: datos inválidos",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @PostMapping
    public ResponseEntity<Categoria> crear(@Valid @RequestBody Categoria cat) {
        log.info("[CategoriaController] POST /api/categorias - nombre: {}", cat.getNombre());
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaService.guardar(cat));
    }

    @Operation(summary = "Actualizar categoría", description = "Actualiza los datos de la categoría con el ID indicado")
    @PutMapping("/{id}")
    public ResponseEntity<Categoria> actualizar(@PathVariable Long id, @Valid @RequestBody Categoria cat) {
        log.info("[CategoriaController] PUT /api/categorias/{}", id);
        return categoriaService.actualizar(id, cat)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Eliminar categoría", description = "Elimina la categoría con el ID indicado")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        log.info("[CategoriaController] DELETE /api/categorias/{}", id);
        if (categoriaService.obtenerPorId(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Categoria no encontrada con ID: " + id);
        }
        categoriaService.eliminar(id);
        return ResponseEntity.ok("Categoria eliminada exitosamente");
    }
}
