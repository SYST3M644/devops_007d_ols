package cl.syst3m64.categorias.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.observation.ServerRequestObservationContext;

import io.micrometer.observation.ObservationPredicate;

/**
 * EP3 - Observabilidad.
 * Prometheus consulta /actuator/prometheus cada 15 s y Kubernetes consulta
 * /actuator/health cada pocos segundos. Si se trazaran, Jaeger se llenaría de
 * "ruido" y las peticiones reales de negocio quedarían escondidas.
 * Este filtro excluye las rutas /actuator de las trazas y de la métrica
 * http.server.requests; el resto de las peticiones se observa normalmente.
 */
@Configuration(proxyBeanMethods = false)
public class ObservabilidadConfig {

    @Bean
    ObservationPredicate excluirActuatorDeLaObservabilidad() {
        return (nombre, contexto) -> {
            if (contexto instanceof ServerRequestObservationContext peticion) {
                return !peticion.getCarrier().getRequestURI().startsWith("/actuator");
            }
            return true;
        };
    }
}
