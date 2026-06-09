package com.example.movilidadmdq.service;

import org.springframework.stereotype.Service;

/**
 * Genera la URL para abrir Didi.
 * Por ahora devuelve el sitio oficial porque Didi todavia no expone un esquema
 * de deep link estable para Argentina con origen y destino precargados. Cuando
 * esten disponibles, este servicio aceptara el CalculoViajeRequest y armara la
 * URL completa, sin que ViajeService tenga que enterarse del cambio.
 */
@Service
public class DidiDeepLinkService
{

    public String generarUrl()
    {
        return "https://www.didiglobal.com/";
    }
}
