package edu.comillas.icai.gitt.pat.spring.PistaPadel.Controlador;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TareasProgramadas {

    private static final Logger logger = LoggerFactory.getLogger(TareasProgramadas.class);

    // Diario a las 2:00 (simulación)
    @Scheduled(cron = "0 0 2 * * *")
    public void recordatorios2AM() {
        logger.info("TAREA 2:00 -> Simulación envío recordatorios de reservas");
    }

    // Día 1 de cada mes a las 9:00 (simulación)
    @Scheduled(cron = "0 0 9 1 * *")
    public void infoMensualDia1() {
        logger.info("TAREA día 1 -> Simulación envío info mensual");
    }
}
