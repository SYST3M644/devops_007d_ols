package cl.syst3m64.categorias.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.server.observation.ServerRequestObservationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationPredicate;

class ObservabilidadConfigTest {

    private final ObservationPredicate filtro = new ObservabilidadConfig().excluirActuatorDeLaObservabilidad();

    private ServerRequestObservationContext peticion(String uri) {
        return new ServerRequestObservationContext(
            new MockHttpServletRequest("GET", uri), new MockHttpServletResponse());
    }

    @Test
    @DisplayName("Las rutas /actuator no se trazan (health checks y scraping de Prometheus)")
    void excluyeRutasDeActuator() {
        assertFalse(filtro.test("http.server.requests", peticion("/actuator/health/liveness")));
        assertFalse(filtro.test("http.server.requests", peticion("/actuator/prometheus")));
    }

    @Test
    @DisplayName("Las rutas de negocio sí se trazan")
    void mantieneRutasDeNegocio() {
        assertTrue(filtro.test("http.server.requests", peticion("/api/category")));
    }

    @Test
    @DisplayName("Las observaciones que no son peticiones HTTP no se filtran")
    void noFiltraOtrasObservaciones() {
        assertTrue(filtro.test("otra.observacion", new Observation.Context()));
    }
}
