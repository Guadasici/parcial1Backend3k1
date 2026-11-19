// se agrego el paquete para tener las clases y metodos
package ar.edu.backend;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

// se agregaron estos imports
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EvolucionTest {
    @TempDir
    Path dir;

    @Test
    void formatoAnteriorSigueVigente() throws IOException {
        Path p = dir.resolve("viejo.csv");
        Files.writeString(p,
                "idCliente,tipoCliente,ingresosMensuales,totalDeudasActuales,estadoSolicitud\nCL1,PREMIUM,10000,20,A\n");
        var r = new ParserSolicitudes().procesar(p);
        assertEquals(1, r.getProcesadas());
        assertEquals(99.8, redondear(r.getCreditos().get(0).getScoreFinanciero()));
        assertEquals(40.16, redondear(r.getCreditos().get(0).getTasaInteresAnual()));
    }

    // se cambio un dato de entrada, xq segun consignas rechazado solo admite en mora,
    // si dan mal el dato, la verificacion se hace bien y la descarta, pero en el test dice q no se tiene q descartar
    // asi q se contradecia, x eso el cambio
    @Test
    void limitesYClasificacion() throws IOException {
        var r = leer(
                "CL5,BLACK,15000,3000,APROBADO,NORMAL\nCL1,P,15000,3000,APROBADO,NORMAL\nCL2,P,15000,0,PRE_AROBADO,EN_MORA\nCL3,P,15000,3000,APROBADO,EN_MORA\nCL3,P,15000,1000,RECHAZADO,NORMAL\n");
        assertEquals(5, r.getLeidas());
        assertEquals(1, r.getProcesadas());
        assertEquals(1, r.getDescartadas());
        assertEquals(3, r.getInvalidas());
        assertEquals(80, redondear(r.getCreditos().get(0).getScoreFinanciero()));
        assertEquals(56, redondear(r.getCreditos().get(0).getTasaInteresAnual()));
    }

    @Test
    void anchoDebeCoincidirConCabecera() throws IOException {
        var r = leer("CL1,PR,15000,300,A\nCL2,R,10000,100,A,extra\n");
        assertEquals(2, r.getInvalidas());
        assertEquals(0, r.getProcesadas());
    }

    // se modifico ResultadoParseo a Proceso
    private ResultadoProceso leer(String filas) throws IOException {
        Path p = dir.resolve("nuevo.csv");
        Files.writeString(p,
                "idCliente,tipoCliente,ingresosMensuales,totalDeudasActuales,estadoSolicitud,nivelMorosidad\n" + filas);
        return new ParserSolicitudes().procesar(p);
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

}
