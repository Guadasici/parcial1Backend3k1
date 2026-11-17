package ar.edu.backend;

import java.util.List;

public class SolicitudCredito {

    private final String idCliente;
    private final String tipoCliente;
    private final double scoreFinanciero;
    private final String estado;
    private final String nivelMorosidad;

    private final static double TASA_BASE = 40;

    // constructor con 4 paramtetros para leer el archivo viejo
    public SolicitudCredito(String idClient, String tipoCliente, double scoreFinanciero, String estado) {

        if (idClient == null || idClient.isBlank() || tipoCliente == null || tipoCliente.isBlank())
            throw new IllegalArgumentException("El Id del Cliente como su Categorizacion (tipo) son requeridos");

        this.idCliente = idClient;
        this.tipoCliente = tipoCliente;
        this.scoreFinanciero = scoreFinanciero;
        this.estado = estado;
        this.nivelMorosidad = "NORMAL";
    }

    // constructor con 5 paramtetros y verificacion de variables, para leer archivo nuevo
    public SolicitudCredito(String idCliente, String tipoCliente, double scoreFinanciero, String estado, String nivelMorosidad) {

        if (idCliente == null || idCliente.isBlank() || tipoCliente == null || tipoCliente.isBlank() || estado == null || nivelMorosidad == null || estado.isBlank() || nivelMorosidad.isBlank())
            throw new IllegalArgumentException("El Id del Cliente, su Categorizacion (tipo), su estado o nivel de morosidad son incorrectos o tienen un error");

        this.idCliente = idCliente;
        this.tipoCliente = tipoCliente;
        this.scoreFinanciero = scoreFinanciero;
        this.estado = estado;
        this.nivelMorosidad = nivelMorosidad;
    }

    public static SolicitudCredito desdeCampos(String[] tokens) {
        // Si no son ni 5 o 6 columnas error
        if (tokens.length != 5 && tokens.length != 6)
            throw new IllegalArgumentException("Se esperan 5 o 6 columnas a procesar");

        double ingresos = Double.parseDouble(tokens[2]);
        double deudas = Double.parseDouble(tokens[3]);
        String estado = tokens[4];
        // defino x default NORMAL y si son 6 columnas se pone la morosidad adecuada
        String nivelMorosidad = "NORMAL";
        if (tokens.length == 6) {
            nivelMorosidad = tokens[5];
        }

        // si son 6 columnas se valida el estado y la morosidad
        if (tokens.length == 6) {
            // validar estado
            if (!estado.equals("APROBADO") && !estado.equals("PRE_APROBADO") && !estado.equals("RECHAZADO")){
                throw new IllegalArgumentException("Estado de solicitud desconocido");
            }
            // validar morosidad
            if (nivelMorosidad == null || nivelMorosidad.isBlank()) {
                throw new IllegalArgumentException("Morosidad incorrecta");
            }
            // verifico si es rechazada
            if (estado.equals("RECHAZADO")) {
                // si es rechazada solo puede ser en mora la morosidad
                if (!"EN_MORA".equals(nivelMorosidad)) {
                    throw new IllegalArgumentException("Solicitud Rechazada solo admite en mora");
                }
            } else {
                // si no es en mora entonces desconocido
                if (!List.of("NORMAL", "EN_MORA").contains(nivelMorosidad)){
                    throw new IllegalArgumentException("Nivel de morosidad desconocido");
                }
            }
        }
        // validar segun morosidad
        if ("NORMAL".equals(nivelMorosidad)) {
            if (ingresos < 1000 || ingresos > 15000)
                throw new IllegalArgumentException("Los ingresos mensuales no son aptos para solicitar el credito");

            if (deudas < 0 || deudas >= ingresos)
                throw new IllegalArgumentException("La deudas registradas no permiten solicitar el credito");
        }
        else if ("EN_MORA".equals(nivelMorosidad)) {
            if (ingresos <= 25000) {
                throw new IllegalArgumentException("Para clientes en mora, la morosidad debe superar los 25000");
            }
            if (deudas < 0 || deudas > ingresos * 0.12) {
                throw new IllegalArgumentException("Para clientes en mora, las deudas no peuden superar el 12% de los ingresos");
            }
        } else {
            throw new IllegalArgumentException("Nivel de morosidad invalido");
        }

        double scoreFinanciero = (1 - (deudas / ingresos )) * 100;
        return new SolicitudCredito(tokens[0], tokens[1],scoreFinanciero, estado, nivelMorosidad);

    }

    public String getIdCliente() {
        return idCliente;
    }

    public String getTipoCliente() {
        return tipoCliente;
    }

    public double getScoreFinanciero() {
        return scoreFinanciero;
    }

    public String getEstado() {
        return estado;
    }

    public double getTasaInteresAnual() {
        return SolicitudCredito.TASA_BASE + (100 - this.scoreFinanciero) * 0.8;
    }

    // se agrego getter del nuevo parametro
    public String getNivelMorosidad() {
        return nivelMorosidad;
    }
}
