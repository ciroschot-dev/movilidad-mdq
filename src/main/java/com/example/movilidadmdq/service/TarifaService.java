package com.example.movilidadmdq.service;

import com.example.movilidadmdq.dto.TarifaRequest;
import com.example.movilidadmdq.enums.TipoTransporte;
import com.example.movilidadmdq.exception.RecursoNoEncontradoException;
import com.example.movilidadmdq.model.Tarifa;
import com.example.movilidadmdq.repository.TarifaRepository;
import org.springframework.stereotype.Service;

/* 
   CLASE: TarifaService
   
   Este servicio es el encargado de gestionar la configuración de precios para el 
   servicio de Taxi en Mar del Plata.
   
   SU FUNCIÓN:
   - Recuperar los valores vigentes desde la base de datos para que el 'ViajeService' 
     pueda calcular los presupuestos.
   - Procesar las actualizaciones enviadas por el Administrador, aplicando cambios 
     parciales (si el Admin solo cambia un valor, el resto permanece intacto).
*/
@Service
public class TarifaService
{
    private final TarifaRepository tarifaRepository;

    public TarifaService(TarifaRepository tarifaRepository)
    {
        this.tarifaRepository = tarifaRepository;
    }

    /* 
       MÉTODO: obtenerTarifaTaxi
       Recupera la configuración actual de tarifas para el taxi desde la base de datos.
       Si no existen tarifas cargadas, lanza una excepción 'RecursoNoEncontradoException'
       que el 'GlobalExceptionHandler' traduce a una respuesta de error adecuada.
    */
    public Tarifa obtenerTarifaTaxi()
    {
        return tarifaRepository.findByTipoTransporte(TipoTransporte.TAXI)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontro la tarifa del taxi"));
    }

    /* 
       MÉTODO: actualizarTarifaTaxi
       Aplica los cambios enviados por el Administrador.
       
       LÓGICA DE ACTUALIZACIÓN PARCIAL:
       El método verifica si cada campo del request es distinto de 'null'. 
       Esto permite que el Administrador pueda actualizar solo un campo específico 
       (ej: valor de la ficha diurna) sin tener que enviar nuevamente todos los otros valores.
    */
    public Tarifa actualizarTarifaTaxi(TarifaRequest request)
    {
        Tarifa tarifaTaxi = tarifaRepository.findByTipoTransporte(TipoTransporte.TAXI)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontro la tarifa de taxi"));

        // Verificación campo a campo para permitir actualizaciones parciales.
        if (request.getBajadaBanderaDia() != null) tarifaTaxi.setBajadaBanderaDia(request.getBajadaBanderaDia());
        if (request.getBajadaBanderaNoche() != null) tarifaTaxi.setBajadaBanderaNoche(request.getBajadaBanderaNoche());
        if (request.getValorFichaDia() != null) tarifaTaxi.setValorFichaDia(request.getValorFichaDia());
        if (request.getValorFichaNoche() != null) tarifaTaxi.setValorFichaNoche(request.getValorFichaNoche());
        if (request.getMetrosPorFicha() != null) tarifaTaxi.setMetrosPorFicha(request.getMetrosPorFicha());

        // Persiste los cambios actualizados en la base de datos MySQL.
        return tarifaRepository.save(tarifaTaxi);
    }
}
