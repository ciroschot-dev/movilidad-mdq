package com.example.movilidadmdq.service;

import com.example.movilidadmdq.dto.CalculoViajeRequest;
import com.example.movilidadmdq.dto.DireccionFavoritaResponse;
import com.example.movilidadmdq.dto.OpcionTransporteResponse;
import com.example.movilidadmdq.dto.ViajeHistorialResponse;
import com.example.movilidadmdq.enums.TipoTransporte;
import com.example.movilidadmdq.exception.OperacionNoPermitidaException;
import com.example.movilidadmdq.exception.RecursoNoEncontradoException;
import com.example.movilidadmdq.model.Tarifa;
import com.example.movilidadmdq.model.Viaje;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.DistanceMatrixElement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ViajeService
{
    // === Inyecciones ===
    private final GoogleMapsService googleMapsService;
    private final WeatherService weatherService;
    private final com.example.movilidadmdq.repository.UsuarioRepository usuarioRepository;
    private final com.example.movilidadmdq.repository.ViajeRepository viajeRepository;
    private final TarifaService tarifaService;
    private final UberDeepLinkService uberDeepLinkService;
    private final DidiDeepLinkService didiDeepLinkService;

    public List<OpcionTransporteResponse> calcularViaje(CalculoViajeRequest request, Long usuarioId)
    {
        String origen = request.origen();
        String destino = request.destino();

        // 🔵 VALORES POR DEFECTO (Simulación / Fallback)
        double distanciaKm = 5.0;
        int tiempoMin = 15;

        // Asegurar que la búsqueda sea en Mar del Plata si no se especificó
        String origenFinal = normalizarDireccion(origen);
        String destinoFinal = normalizarDireccion(destino);

        System.out.println("Solicitando viaje: [" + origenFinal + "] -> [" + destinoFinal + "]");

        // Intento de obtener datos reales de Google Maps
        try
        {
            DistanceMatrix matrix = googleMapsService.obtenerDatosViaje(origenFinal, destinoFinal);
            if (esRespuestaValida(matrix))
            {
                DistanceMatrixElement element = matrix.rows[0].elements[0];

                // Convertir metros a Kilómetros y segundos a Minutos
                distanciaKm = element.distance.inMeters / 1000.0;
                tiempoMin = (int) Math.ceil(element.duration.inSeconds / 60.0);

                System.out.println("Datos REALES obtenidos: " + distanciaKm + "km, " + tiempoMin + "min");
            }
        }
        catch (Exception e)
        {
            System.err.println("Error Google Maps API: " + e.getMessage());
        }

        BigDecimal precioTaxi = calcularTaxi(distanciaKm);
        double factorClima = obtenerFactorClima();

        OpcionTransporteResponse taxi = construirTaxi(precioTaxi, tiempoMin, (long) (distanciaKm * 1000));
        OpcionTransporteResponse uber = construirUber(precioTaxi, tiempoMin, request, factorClima, (long) (distanciaKm * 1000));
        OpcionTransporteResponse didi = construirDidi(precioTaxi, tiempoMin, factorClima, (long) (distanciaKm * 1000));

        // --- GUARDAR EN BASE DE DATOS AL FINAL CON PRECIOS REALES ---
        long distanciaMetros = (long) (distanciaKm * 1000);
        guardarHistorial(origenFinal, destinoFinal, distanciaMetros, tiempoMin,
                taxi.precioMin(), uber.precioMin(), uber.precioMax(),
                didi.precioMin(), didi.precioMax(), usuarioId, request);

        List<OpcionTransporteResponse> opciones = List.of(taxi, uber, didi);

        // 💸 ordenar por precio más bajo
        return opciones.stream()
                .sorted(Comparator.comparing(OpcionTransporteResponse::precioMin))
                .toList();
    }

    private void guardarHistorial(String origen, String destino, Long distanciaMetros, int tiempoMin,
                                  BigDecimal precioTaxi, BigDecimal uberMin, BigDecimal uberMax,
                                  BigDecimal didiMin, BigDecimal didiMax, Long usuarioId, CalculoViajeRequest request)
    {
        if (usuarioId == null) return;

        try
        {
            usuarioRepository.findById(usuarioId).ifPresent(usuario ->
            {
                com.example.movilidadmdq.model.Viaje nuevoViaje = new com.example.movilidadmdq.model.Viaje();
                nuevoViaje.setOrigen(origen);
                nuevoViaje.setDestino(destino);
                nuevoViaje.setDistanciaEnMetros(distanciaMetros);
                nuevoViaje.setTiempoEstimadoMin(tiempoMin);
                nuevoViaje.setPrecioTaxi(precioTaxi);

                nuevoViaje.setPrecioUberMin(uberMin);
                nuevoViaje.setPrecioUberMax(uberMax);
                nuevoViaje.setPrecioDidiMin(didiMin);
                nuevoViaje.setPrecioDidiMax(didiMax);

                // Compatibilidad
                nuevoViaje.setPrecioMinApp(uberMin);
                nuevoViaje.setPrecioMaxApp(uberMax);

                // Guardar coordenadas y Place IDs para optimización futura
                nuevoViaje.setOrigenPlaceId(request.origenPlaceId());
                nuevoViaje.setOrigenLat(request.origenLat());
                nuevoViaje.setOrigenLng(request.origenLng());
                nuevoViaje.setDestinoPlaceId(request.destinoPlaceId());
                nuevoViaje.setDestinoLat(request.destinoLat());
                nuevoViaje.setDestinoLng(request.destinoLng());

                nuevoViaje.setUsuario(usuario);

                viajeRepository.save(nuevoViaje);
                System.out.println("Viaje guardado automaticamente en AWS para el usuario: " + usuario.getUsername());
            });
        }
        catch (Exception e)
        {
            System.err.println("Error al guardar historial: " + e.getMessage());
        }
    }

    private String normalizarDireccion(String direccion)
    {
        if (direccion == null || direccion.isBlank()) return "";
        if (direccion.toLowerCase().contains("mar del plata")) return direccion;
        return direccion + ", Mar del Plata, Argentina";
    }

    private boolean esRespuestaValida(DistanceMatrix matrix)
    {
        return matrix != null
                && matrix.rows.length > 0
                && matrix.rows[0].elements.length > 0
                && matrix.rows[0].elements[0].status.toString().equals("OK")
                && matrix.rows[0].elements[0].distance != null
                && matrix.rows[0].elements[0].duration != null;
    }

    // =========================
    // 🚕 TAXI (tarifa real)
    // =========================
    //
    // Como cobra el taxi en Mar del Plata:
    //
    //     precio_total = bajada_de_bandera + (cantidad_de_fichas * valor_de_ficha)
    //
    // - Bajada de bandera: monto fijo que se cobra al subir al taxi.
    //   Es mas cara de noche que de dia.
    // - Ficha: unidad de cobro por distancia recorrida. Cada N metros (160
    //   por defecto) suma una ficha. Si el viaje sobra aunque sea un metro,
    //   se cobra la ficha completa (por eso usamos Math.ceil mas abajo).
    // - Valor de ficha: precio de cada ficha. Tambien es mas caro de noche.
    //
    // Los 5 valores se leen de la entidad Tarifa (tabla "tarifas"), asi un
    // admin puede actualizarlos desde el endpoint PUT /admin/tarifas/taxi
    // sin necesidad de redeploy.
    //
    // Si la DB tiene esos campos en NULL (por ejemplo cuando recien se
    // agregan las columnas en una DB de produccion ya existente), se cae a
    // los valores por defecto de abajo para que la app no rompa.

    // Tarifas vigentes en Mar del Plata 2026 (fallback si la DB no tiene valores)
    private static final BigDecimal DEFAULT_BAJADA_DIA       = BigDecimal.valueOf(2250);
    private static final BigDecimal DEFAULT_BAJADA_NOCHE     = BigDecimal.valueOf(2700);
    private static final BigDecimal DEFAULT_FICHA_DIA        = BigDecimal.valueOf(150);
    private static final BigDecimal DEFAULT_FICHA_NOCHE      = BigDecimal.valueOf(180);
    private static final int        DEFAULT_METROS_POR_FICHA = 160;

    private BigDecimal calcularTaxi(double distanciaKm)
    {
        // 1. Traer la tarifa vigente del taxi desde la base de datos
        Tarifa tarifa = tarifaService.obtenerTarifaTaxi();
        boolean esNocturno = esHorarioNocturno();

        // 2. Elegir que valores aplicar segun el horario
        BigDecimal bajadaBandera = obtenerBajadaBandera(tarifa, esNocturno);
        BigDecimal valorFicha    = obtenerValorFicha(tarifa, esNocturno);
        int metrosPorFicha       = obtenerMetrosPorFicha(tarifa);

        // 3. Calcular cuantas fichas suma el viaje.
        //    Math.ceil garantiza que cualquier fraccion de ficha se cobre como ficha entera.
        double distanciaMetros = distanciaKm * 1000;
        int cantidadDeFichas   = (int) Math.ceil(distanciaMetros / metrosPorFicha);

        // 4. Precio final = bajada de bandera + (fichas * valor de cada ficha)
        BigDecimal precioPorFichas = valorFicha.multiply(BigDecimal.valueOf(cantidadDeFichas));
        return bajadaBandera.add(precioPorFichas);
    }

    private BigDecimal obtenerBajadaBandera(Tarifa tarifa, boolean esNocturno)
    {
        if (esNocturno) {
            return valorOrDefault(tarifa.getBajadaBanderaNoche(), DEFAULT_BAJADA_NOCHE);
        }
        return valorOrDefault(tarifa.getBajadaBanderaDia(), DEFAULT_BAJADA_DIA);
    }

    private BigDecimal obtenerValorFicha(Tarifa tarifa, boolean esNocturno)
    {
        if (esNocturno) {
            return valorOrDefault(tarifa.getValorFichaNoche(), DEFAULT_FICHA_NOCHE);
        }
        return valorOrDefault(tarifa.getValorFichaDia(), DEFAULT_FICHA_DIA);
    }

    private int obtenerMetrosPorFicha(Tarifa tarifa)
    {
        return tarifa.getMetrosPorFicha() != null
                ? tarifa.getMetrosPorFicha()
                : DEFAULT_METROS_POR_FICHA;
    }

    // Devuelve el valor si no es null, sino el por defecto. Sirve para que
    // calcularTaxi nunca falle si la DB todavia no tiene los campos cargados.
    private BigDecimal valorOrDefault(BigDecimal valor, BigDecimal porDefecto)
    {
        return valor != null ? valor : porDefecto;
    }

    private boolean esHorarioNocturno()
    {
        int hora = LocalTime.now().getHour();
        return hora >= 22 || hora < 6;
    }

    private OpcionTransporteResponse construirTaxi(BigDecimal precioTaxi, int tiempoMin, long distanciaMetros)
    {
        return new OpcionTransporteResponse(
                TipoTransporte.TAXI,
                precioTaxi,
                precioTaxi,
                tiempoMin,
                distanciaMetros,
                "tel:+5402234941010"
        );
    }

    // =========================
    // 🚗 UBER
    // =========================
    private OpcionTransporteResponse construirUber(BigDecimal precioTaxi, int tiempoMin, CalculoViajeRequest request, double factorClima, long distanciaMetros)
    {
        BigDecimal base = precioTaxi.multiply(BigDecimal.valueOf(0.85)); // base más barato que taxi

        double factorHorario = obtenerFactorHorario();
        double factorDemanda = obtenerFactorDemanda();

        BigDecimal precioMin = base.multiply(BigDecimal.valueOf(factorHorario * factorClima));
        BigDecimal precioMax = base.multiply(BigDecimal.valueOf(factorHorario * factorClima * factorDemanda));

        return new OpcionTransporteResponse(
                TipoTransporte.UBER,
                precioMin.setScale(2, RoundingMode.HALF_UP),
                precioMax.setScale(2, RoundingMode.HALF_UP),
                tiempoMin,
                distanciaMetros,
                uberDeepLinkService.generarUrl(request)
        );
    }

    // =========================
    // 🚙 DIDI
    // =========================
    private OpcionTransporteResponse construirDidi(BigDecimal precioTaxi, int tiempoMin, double factorClima, long distanciaMetros)
    {
        BigDecimal base = precioTaxi.multiply(BigDecimal.valueOf(0.75));

        double factorHorario = obtenerFactorHorario();
        double factorDemanda = obtenerFactorDemanda();

        BigDecimal precioMin = base.multiply(BigDecimal.valueOf(factorHorario));
        BigDecimal precioMax = base.multiply(BigDecimal.valueOf(factorHorario * factorClima * factorDemanda));

        return new OpcionTransporteResponse(
                TipoTransporte.DIDI,
                precioMin.setScale(2, RoundingMode.HALF_UP),
                precioMax.setScale(2, RoundingMode.HALF_UP),
                tiempoMin,
                distanciaMetros,
                didiDeepLinkService.generarUrl()
        );
    }

    // Los deep links de Uber y Didi viven en sus propios servicios
    // (UberDeepLinkService / DidiDeepLinkService). La URL del taxi es un "tel:"
    // estatico y va inline en construirTaxi().

    // =========================
    // 📊 FACTORES DINÁMICOS
    // =========================
    private double obtenerFactorHorario()
    {
        int hora = LocalTime.now().getHour();

        if (hora >= 7 && hora <= 9) return 1.3; // hora pico mañana
        if (hora >= 17 && hora <= 20) return 1.4; // hora pico tarde
        if (hora >= 22 || hora < 6) return 1.2; // noche

        return 1.0;
    }

    private double obtenerFactorClima()
    {
        return weatherService.obtenerFactorClima();
    }

    private double obtenerFactorDemanda()
    {
        int autosDisponibles = (int) (Math.random() * 10);

        if (autosDisponibles < 3) return 1.5;
        if (autosDisponibles < 6) return 1.2;

        return 1.0;
    }

    @Transactional
    public void toggleFavorito(Long viajeId, Long usuarioId)
    {

        // Caso 1: el viaje no existe en la DB -> 404.
        Viaje viaje = viajeRepository.findById(viajeId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Viaje no encontrado"));

        // Caso 2: el viaje existe pero es de otro usuario -> 403.
        //
        // Nota: devolver 403 (distinto del 404) revela al cliente que el viaje
        // existe. En contextos con datos sensibles eso seria una fuga de info y
        // se preferiria responder 404 tambien aca. En esta API priorizamos
        // claridad semantica: el JWT del usuario es valido, lo que falla es la
        // autorizacion a nivel recurso. "Se quien sos, pero esto no es tuyo".
        if (!viaje.getUsuario().getId().equals(usuarioId))
        {
            throw new OperacionNoPermitidaException("No tienes permiso para modificar este viaje");
        }

        viaje.setFavorito(!viaje.isFavorito());

        viajeRepository.save(viaje);
    }

    public List<Viaje> obtenerFavoritos(Long usuarioId)
    {
        return viajeRepository.findByUsuarioIdAndFavoritoTrue(usuarioId);
    }

    public List<DireccionFavoritaResponse> obtenerDireccionesFavoritas(Long usuarioId)
    {
        List<Viaje> favoritos = obtenerFavoritos(usuarioId);

        // Usamos un mapa para evitar duplicados basado en la dirección o el placeId
        java.util.Map<String, DireccionFavoritaResponse> direcciones = new java.util.LinkedHashMap<>();

        for (Viaje v : favoritos)
        {
            // Procesar Origen
            if (v.getOrigen() != null)
            {
                String key = v.getOrigenPlaceId() != null ? v.getOrigenPlaceId() : v.getOrigen();
                direcciones.putIfAbsent(key, new DireccionFavoritaResponse(
                        v.getOrigen(),
                        v.getOrigenPlaceId(),
                        v.getOrigenLat(),
                        v.getOrigenLng()
                ));
            }

            // Procesar Destino
            if (v.getDestino() != null)
            {
                String key = v.getDestinoPlaceId() != null ? v.getDestinoPlaceId() : v.getDestino();
                direcciones.putIfAbsent(key, new DireccionFavoritaResponse(
                        v.getDestino(),
                        v.getDestinoPlaceId(),
                        v.getDestinoLat(),
                        v.getDestinoLng()
                ));
            }
        }

        return new java.util.ArrayList<>(direcciones.values());
    }

    public ViajeHistorialResponse toResponse(Viaje viaje)
    {
        return new ViajeHistorialResponse(
                viaje.getId(),
                viaje.getOrigen(),
                viaje.getDestino(),
                viaje.getDistanciaEnMetros(),
                viaje.getTiempoEstimadoMin(),
                viaje.getPrecioTaxi(),
                viaje.getPrecioUberMin(),
                viaje.getPrecioUberMax(),
                viaje.getPrecioDidiMin(),
                viaje.getPrecioDidiMax(),
                viaje.getTipoElegido() != null ? viaje.getTipoElegido().name() : null,
                viaje.getFechaHora(),
                viaje.isFavorito(),
                viaje.getOrigenPlaceId(),
                viaje.getOrigenLat(),
                viaje.getOrigenLng(),
                viaje.getDestinoPlaceId(),
                viaje.getDestinoLat(),
                viaje.getDestinoLng()
        );
    }
}
