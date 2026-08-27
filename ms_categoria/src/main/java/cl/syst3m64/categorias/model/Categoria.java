package cl.syst3m64.categorias.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "categorias")
@Schema(description = "Entidad que representa una categoría de libro")
public class Categoria {

    @Null(message = "El ID es generado automáticamente, no debe ser proporcionado")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único generado automáticamente por la BD", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede tener más de 100 caracteres")
    @Column(nullable = false, unique = true, length = 100)
    @Schema(description = "Nombre de la categoría", example = "Ciencia Ficción", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nombre;

    @Size(max = 300, message = "La descripción debe tener máximo 300 caracteres")
    @Column(length = 300)
    @Schema(description = "Descripción de la categoría", example = "Libros de ciencia ficción y tecnología")
    private String descripcion;
}
