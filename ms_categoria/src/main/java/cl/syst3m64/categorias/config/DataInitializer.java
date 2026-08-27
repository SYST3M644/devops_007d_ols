package cl.syst3m64.categorias.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import cl.syst3m64.categorias.model.Categoria;
import cl.syst3m64.categorias.repository.CategoriaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final CategoriaRepository categoriaRepository;

    @Override
    public void run(String... args){
        if(categoriaRepository.count() > 0){
            log.info("Categorias cargadas. No se ejecuta el archivo");
            return;
        }
        log.info("Cargando categorias preconfiguradas...");
        categoriaRepository.save(new Categoria(null,"Nuevo","Libro Nuevo"));
        categoriaRepository.save(new Categoria(null,"Usado","Libro Usado"));
        categoriaRepository.save(new Categoria(null,"Reparado","Libro Reparado"));
        categoriaRepository.save(new Categoria(null,"Digital","Libro Digital")); 
        log.info("Categorias preconfiguradas cargadas exitosamente");

    }

}
