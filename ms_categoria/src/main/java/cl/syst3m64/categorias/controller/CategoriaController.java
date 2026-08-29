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
@RequestMapping("/api/category")
@RequiredArgsConstructor
@Tag(name = "Category", description = "Operaciones CRUD del catálogo de category")
public class CategoriaController {
    private final CategoriaService categoriaService;

    @Operation(summary = "Listar todas las category", description = "Retorna la lista completa de category")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de category retornada correctamente",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Categoria.class)))
    })
    @GetMapping
    public List<Categoria> obtenerTodas() {
        log.info("[CategoryController] GET /api/category");
        return categoriaService.obtenerTodas();
    }

    @Operation(summary = "Buscar category por nombre", description = "Filtra category por nombre exacto")
    @GetMapping("/nombres")
    public List<Categoria> obtenerNombresCat(
        @Parameter(description = "Nombre de la category a buscar", example = "Nuevo") @RequestParam String nombre) {
        log.info("[CategoryController] GET /api/category/nombres?nombre={}", nombre);
        return categoriaService.obtenerNombres(nombre);
    }

    @Operation(summary = "Obtener category por ID", description = "Retorna la category con el ID indicado")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Category encontrada",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Categoria.class))),
        @ApiResponse(responseCode = "404", description = "Category no encontrada",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @GetMapping("/{id}")
    public ResponseEntity<Categoria> obtenerId(
        @Parameter(description = "ID de la category", example = "1", required = true) @PathVariable Long id) {
        log.info("[CategoryController] GET /api/category/{}", id);
        return categoriaService.obtenerPorId(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Crear nueva category", description = "Crea una category con los datos proporcionados")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Category creada correctamente",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Categoria.class))),
        @ApiResponse(responseCode = "400", description = "Error de validación: datos inválidos",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @PostMapping
    public ResponseEntity<Categoria> crear(@Valid @RequestBody Categoria cat) {
        log.info("[CategoryController] POST /api/category - nombre: {}", cat.getNombre());
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaService.guardar(cat));
    }

    @Operation(summary = "Actualizar category", description = "Actualiza los datos de la category con el ID indicado")
    @PutMapping("/{id}")
    public ResponseEntity<Categoria> actualizar(@PathVariable Long id, @Valid @RequestBody Categoria cat) {
        log.info("[CategoryController] PUT /api/category/{}", id);
        return categoriaService.actualizar(id, cat)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Eliminar category", description = "Elimina la category con el ID indicado")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        log.info("[CategoryController] DELETE /api/category/{}", id);
        if (categoriaService.obtenerPorId(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Category no encontrada con ID: " + id);
        }
        categoriaService.eliminar(id);
        return ResponseEntity.ok("Category eliminada exitosamente");
    }
}
