/**
 * CONFIGURACIÓN Y ESTADO GLOBAL
 */
const API_BASE_URL = ENV.API_URL.replace('/viajes/calcular', ''); 
let currentUser = JSON.parse(localStorage.getItem('user')) || null;

// Referencias DOM - Secciones
const loginSection = document.getElementById('loginSection');
const appSection = document.getElementById('appSection');
const views = {
    home: document.getElementById('viewHome'),
    history: document.getElementById('viewHistory'),
    profile: document.getElementById('viewProfile')
};

// Referencias DOM - UI
const userNameDisplay = document.getElementById('userNameDisplay');
const loginForm = document.getElementById('loginForm');
const profileForm = document.getElementById('profileForm');
const listaHistorial = document.getElementById('listaHistorial');

/**
 * NAVEGACIÓN
 */
function switchView(viewName) {
    Object.keys(views).forEach(key => {
        views[key].style.display = (key === viewName) ? 'block' : 'none';
    });
    
    if (viewName === 'history') cargarHistorial();
    if (viewName === 'profile') cargarDatosPerfil();
    
    lucide.createIcons();
}

document.getElementById('btnNavHome').onclick = () => switchView('home');
document.getElementById('btnNavHistory').onclick = () => switchView('history');
document.getElementById('btnNavProfile').onclick = () => switchView('profile');
document.getElementById('btnLogout').onclick = logout;

/**
 * GESTIÓN DE USUARIO (LOGIN / PERFIL)
 */
function actualizarVista() {
    if (currentUser) {
        loginSection.style.display = 'none';
        appSection.style.display = 'block';
        userNameDisplay.innerText = currentUser.username;
        switchView('home');
    } else {
        loginSection.style.display = 'block';
        appSection.style.display = 'none';
    }
    lucide.createIcons();
}

loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    try {
        const response = await fetch(`${API_BASE_URL}/usuarios/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });
        if (!response.ok) throw new Error();
        currentUser = await response.json();
        localStorage.setItem('user', JSON.stringify(currentUser));
        actualizarVista();
    } catch { document.getElementById('loginError').style.display = 'block'; }
});

function logout() {
    currentUser = null;
    localStorage.removeItem('user');
    actualizarVista();
}

function cargarDatosPerfil() {
    document.getElementById('profileUsername').value = currentUser.username;
    document.getElementById('profileEmail').value = currentUser.email;
}

profileForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = document.getElementById('profileUsername').value;
    const email = document.getElementById('profileEmail').value;
    const password = document.getElementById('profilePassword').value;

    try {
        const response = await fetch(`${API_BASE_URL}/usuarios/${currentUser.id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, email, password })
        });
        if (!response.ok) throw new Error();
        currentUser = await response.json();
        localStorage.setItem('user', JSON.stringify(currentUser));
        alert("Perfil actualizado correctamente");
        actualizarVista();
    } catch { alert("Error al actualizar perfil"); }
});

/**
 * HISTORIAL
 */
async function cargarHistorial() {
    listaHistorial.innerHTML = '<p class="placeholder-text">Cargando historial...</p>';
    try {
        const response = await fetch(`${API_BASE_URL}/usuarios/${currentUser.id}/historial`);
        const viajes = await response.json();
        
        if (viajes.length === 0) {
            listaHistorial.innerHTML = '<p class="placeholder-text">Aún no tienes viajes registrados.</p>';
            return;
        }

        listaHistorial.innerHTML = viajes.reverse().map(v => `
            <div class="history-card">
                <div class="route">
                    <div class="point"><i data-lucide="map-pin"></i> <b>Origen:</b> ${v.origen}</div>
                    <div class="point"><i data-lucide="navigation"></i> <b>Destino:</b> ${v.destino}</div>
                </div>
                <div class="footer">
                    <span>${new Date(v.fechaHora).toLocaleDateString()} - ${v.tiempoEstimadoMin} min</span>
                    <span class="price-tag">Taxi: $${v.precioTaxi}</span>
                </div>
            </div>
        `).join('');
        lucide.createIcons();
    } catch { listaHistorial.innerHTML = '<p class="placeholder-text">Error al cargar historial.</p>'; }
}

/**
 * CALCULADORA DE VIAJES
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
            body: JSON.stringify({ origen, destino, usuarioId: currentUser.id })
        });
        const data = await response.json();
        renderizarResultados(data);
    } catch { mensajeEstado.innerText = 'Error al calcular.'; }
    finally { setLoading(false); }
});

function setLoading(isLoading) {
    btnCalcular.disabled = isLoading;
    btnCalcular.innerText = isLoading ? 'CALCULANDO...' : 'CALCULAR';
    if (isLoading) { mensajeEstado.innerText = 'Buscando opciones...'; listaResultados.innerHTML = ''; }
}

function renderizarResultados(opciones) {
    mensajeEstado.style.display = 'none';
    listaResultados.innerHTML = opciones.map(op => {
        const config = getConfig(op.tipo);
        return `
            <div class="card">
                <div class="card-left">
                    <div class="icon-box ${config.claseIcono}"><i data-lucide="${config.icono}"></i></div>
                    <div class="info">
                        <h3>${config.nombre}</h3>
                        <span><i data-lucide="clock" style="width:14px"></i> ${op.tiempoMinutos} min</span>
                    </div>
                </div>
                <div class="card-right">
                    <span class="price">${formatPrice(op.precioMin, op.precioMax)}</span>
                    <a href="${op.url}" target="_blank" class="btn-link">Elegir →</a>
                </div>
            </div>
        `;
    }).join('');
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

/**
 * AUTOCOMPLETE (Google Maps)
 */
async function initAutocomplete() {
    // Definimos el cargador dinámico (Bootstrap Loader)
    (g=>{var h,a,k,p="The Google Maps JavaScript API",c="google",l="importLibrary",q="__ib__",m=document,b=window;b=b[c]||(b[c]={});var d=b.maps||(b.maps={}),r=new Set,e=new URLSearchParams,u=()=>h||(h=new Promise(async(f,n)=>{await (a=m.createElement("script"));e.set("libraries",[...r]+"");for(k in g)e.set(k.replace(/[A-Z]/g,t=>"_"+t[0].toLowerCase()),g[k]);e.set("callback",c+".maps."+q);a.src=`https://maps.${c}apis.com/maps/api/js?`+e;d[q]=f;a.onerror=()=>h=n(Error(p+" could not load."));a.nonce=m.querySelector("script[nonce]")?.nonce||"";m.head.append(a)}));d[l]?console.warn(p+" only loads once. Re-using existing %s.",l):d[l]=(f,...n)=>r.add(f)&&u().then(()=>d[l](f,...n))})({
        key: ENV.GOOGLE_MAPS_KEY,
        v: "weekly"
    });

    try {
        const { Autocomplete } = await google.maps.importLibrary("places");
        const options = {
            componentRestrictions: { country: "ar" },
            fields: ["formatted_address", "geometry"],
            locationBias: { lat: -38.0055, lng: -57.5426 },
        };
        new Autocomplete(document.getElementById('origen'), options);
        new Autocomplete(document.getElementById('destino'), options);
        console.log("✅ Google Maps Autocomplete cargado.");
    } catch (e) {
        console.warn("Reintentando carga de Google Maps...");
        setTimeout(initAutocomplete, 500);
    }
}

// INICIALIZACIÓN
actualizarVista();
initAutocomplete();
lucide.createIcons();
