package ar.edu.backend;

import com.sun.source.tree.BreakTree;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CarteraCreditos {
    private final List<SolicitudCredito> creditos;

    public CarteraCreditos(List<SolicitudCredito> creditos) {
        this.creditos = List.copyOf(creditos);
    }

    public List<SolicitudCredito> filtrar(Predicate<SolicitudCredito> criterio) {
        return this.creditos.stream().filter(criterio).toList();
    }

    // AGREGADO
    public int cantidadCreditos() {
        return this.creditos.size();
    }

    /**
     * Metodo que determina o calcula cual es la tasa de interes anual promedio que
     * se agregara a todos
     * los creditos que se encuentran en cartera
     *
     * @return Valor promedio
     */
    public double tasaInteresPromedio() {
        return this.creditos.stream()
                .mapToDouble(SolicitudCredito::getTasaInteresAnual)
                .average()
                .orElse(0);
    }

    /**
     * Metodo que determinar cual es la tasa de interes maxima que se ha cobrado por
     * cada tipo de cliente
     * que se contiene en la cartera de creditos
     */
    // Metodo para determinar la tasa de interes máx q se ha cobrado x cada
    // tipo de cliente q se contiene en la cartera de creditos
    public Map<String, Double> maximaTasaInteresPorTipo() {
        return this.creditos.stream()
                .collect(Collectors.groupingBy(
                        SolicitudCredito::getTipoCliente,
                        Collectors.collectingAndThen(
                                Collectors.mapping(
                                        SolicitudCredito::getTasaInteresAnual,
                                        Collectors.maxBy(Comparator.naturalOrder())),
                                optionalMax -> optionalMax.orElse(0.0))));

    }

    // AGREGADO
    // Metodo q cuenta la cant. de solicitudes aceptadas x cada nivel de
    // morosidad. Si esta vacia retorna un mapa vacio.
    public Map<String, Long> conteoPorNivelMorosidad() {
        return this.creditos.stream()
                .collect(Collectors.groupingBy(
                        SolicitudCredito::getNivelMorosidad,
                        Collectors.counting()));
    }

    // AGREGADO
    // Metodo q cuenta la cant. de solicitudes aceptadas x cada tipo de cliente
    public Map<String, Long> conteoPorTipoCliente() {
        return  this.creditos.stream()
                .collect(Collectors.groupingBy(
                        SolicitudCredito::getTipoCliente,
                        Collectors.counting()));
    }


    /**
     * Metodo que informa cual es la tasa promedio que se cobra, cual es la tasa
     * maxima de cada tipo de cliente
     * que se contiene y la cantidad de creditos cuya tasa de interes, en base a su
     * score financiero es mayor un 60%
     */
    public String informe() {
        String cadena = "Total Creditos: %d | Tasa Interes Promedio: %.2f | Maximos Tipo de Cliente: %s | Cantidad Cliente 60%%: %d";
        // AGREGADO solo total
        int total = this.cantidadCreditos();
        double ti = this.tasaInteresPromedio();
        int cantidad = this.filtrar(sc -> sc.getTasaInteresAnual() > 60).size();
        Map<String, String> tiposFormateados = maximaTasaInteresPorTipo().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> String.format("%.2f", e.getValue()),
                        (newKey, newValue) -> newKey,
                        LinkedHashMap::new));

        // AGREGADO solo total, y los this. algo mas
        return cadena.formatted(total, ti, tiposFormateados, this.conteoPorNivelMorosidad(), this.conteoPorTipoCliente(), cantidad);

    }

}
