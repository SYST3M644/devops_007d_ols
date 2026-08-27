package cl.syst3m64.categorias.exception;

public class CategoriaNotFoundException extends RuntimeException {
    public CategoriaNotFoundException(Long id) {
        super("Categoria con ID " + id + " no encontrada");
    }
}
