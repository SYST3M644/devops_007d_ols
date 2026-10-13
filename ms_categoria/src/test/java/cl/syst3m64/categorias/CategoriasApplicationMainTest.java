package cl.syst3m64.categorias;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mockStatic;
import org.springframework.boot.SpringApplication;

class CategoriasApplicationMainTest {

    @Test
    @DisplayName("main() arranca la aplicación con SpringApplication.run")
    void main_debeInvocarSpringApplicationRun() {
        String[] args = {};
        try (MockedStatic<SpringApplication> spring = mockStatic(SpringApplication.class)) {
            CategoriasApplication.main(args);

            spring.verify(() -> SpringApplication.run(CategoriasApplication.class, args));
        }
    }
}
