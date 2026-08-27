package cl.syst3m64.categorias.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cl.syst3m64.categorias.model.Categoria;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria,Long> {
    //SELECT * FROM Categoria WHERE nombre = 'nombre'
    @Query("SELECT c FROM Categoria c WHERE c.nombre=:nombre")
    List<Categoria> findAllNombres(@Param("nombre") String nombre);

    //SELECT * FROM Categoria WHERE nombre LIKE %nombre%
    //validar case sensitive (mayusculas y minusculas)

}
