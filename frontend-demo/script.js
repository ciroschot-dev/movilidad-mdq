/**
 * CONFIGURACIÓN MODERNA (Carga asíncrona de Google Maps)
 */
async function inicializarApp() {
    // 1. Cargador dinámico de Google Maps (Bootstrap Loader)
    (g=>{var h,a,k,p="The Google Maps JavaScript API",c="google",l="importLibrary",q="__ib__",m=document,b=window;b=b[c]||(b[c]={});var d=b.maps||(b.maps={}),r=new Set,e=new URLSearchParams,u=()=>h||(h=new Promise(async(f,n)=>{await (a=m.createElement("script"));e.set("libraries",[...r]+"");for(k in g)e.set(k.replace(/[A-Z]/g,t=>"_"+t[0].toLowerCase()),g[k]);e.set("callback",c+".maps."+q);a.src=`https://maps.${c}apis.com/maps/api/js?`+e;d[q]=f;a.onerror=()=>h=n(Error(p+" could not load."));a.nonce=m.querySelector("script[nonce]")?.nonce||"";m.head.append(a)}));d[l]?console.warn(p+" only loads once. Re-using existing %s.",l):d[l]=(f,...n)=>r.add(f)&&u().then(()=>d[l](f,...n))})({
        key: ENV.GOOGLE_MAPS_KEY,
        v: "weekly"
    });

    try {
        // 2. Carga de la librería Places
        const { Autocomplete } = await google.maps.importLibrary("places");
        
        const options = {
            componentRestrictions: { country: "ar" },
            fields: ["formatted_address", "geometry"],
            locationBias: { lat: -38.0055, lng: -57.5426 },
        };

        new Autocomplete(document.getElementById('origen'), options);
        new Autocomplete(document.getElementById('destino'), options);

        lucide.createIcons();
    } catch (e) {
        console.error("Error al cargar Google Maps:", e);
    }
}

/**
 * LÓGICA DE LA APP
 */
const viajeForm = document.getElementById('viajeForm');
const btnCalcular = document.getElementById('btnCalcular');
const mensajeEstado = document.getElementById('mensajeEstado');
const listaResultados = document.getElementById('listaResultados');

viajeForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const origen = document.getElementById('origen').value;
    const destino = document.getElementById('destino').value;

    setLoading(true);

    try {
        const response = await fetch(ENV.API_URL, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ origen, destino })
        });

        if (!response.ok) throw new Error('Servidor no disponible');
        const data = await response.json();
        renderizarResultados(data);
    } catch (error) {
        mensajeEstado.innerText = 'Error al conectar con el servidor.';
        console.error(error);
    } finally {
        setLoading(false);
    }
});

function setLoading(isLoading) {
    btnCalcular.disabled = isLoading;
    btnCalcular.innerText = isLoading ? 'CALCULANDO...' : 'CALCULAR';
    if (isLoading) {
        mensajeEstado.innerText = 'Buscando opciones...';
        mensajeEstado.style.display = 'block';
        listaResultados.innerHTML = '';
    }
}

function renderizarResultados(opciones) {
    mensajeEstado.style.display = 'none';
    listaResultados.innerHTML = '';

    opciones.forEach(opcion => {
        const precioFormateado = formatPrice(opcion.precioMin, opcion.precioMax);
        const config = getConfig(opcion.tipo);
        const card = document.createElement('div');
        card.className = 'card';
        card.innerHTML = `
            <div class="card-left">
                <div class="icon-box ${config.claseIcono}"><i data-lucide="${config.icono}"></i></div>
                <div class="info">
                    <h3>${config.nombre}</h3>
                    <span><i data-lucide="clock" style="width:14px"></i> ${opcion.tiempoMinutos} min</span>
                </div>
            </div>
            <div class="card-right">
                <span class="price">${precioFormateado}</span>
                <a href="${opcion.url}" target="_blank" class="btn-link">Elegir →</a>
            </div>
        `;
        listaResultados.appendChild(card);
    });
    lucide.createIcons();
}

function formatPrice(min, max) {
    const formatter = new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS', maximumFractionDigits: 0 });
    return min === max ? formatter.format(min) : `${formatter.format(min)} - ${formatter.format(max)}`;
}

function getConfig(tipo) {
    switch (tipo) {
        case 'TAXI': return { nombre: 'Taxi', icono: 'car', claseIcono: 'taxi' };
        case 'UBER': return { nombre: 'Uber', icono: 'smartphone', claseIcono: 'uber' };
        case 'DIDI': return { nombre: 'Didi', icono: 'smartphone', claseIcono: 'didi' };
        default: return { nombre: tipo, icono: 'map-pin', claseIcono: '' };
    }
}

// Iniciar aplicación
inicializarApp();
