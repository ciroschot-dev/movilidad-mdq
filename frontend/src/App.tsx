import { useEffect, useState, type FormEvent, type ReactNode } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Car, Smartphone, CreditCard, LogOut, User, Mail, LockKeyhole, History, Home, MapPin, Navigation, RefreshCw } from 'lucide-react';
import { useJsApiLoader } from '@react-google-maps/api';
import InputForm from './components/InputForm';
import ResultadoCard from './components/ResultadoCard';

const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';
const GOOGLE_MAPS_API_KEY = import.meta.env.VITE_GOOGLE_MAPS_API_KEY ?? '';
const SESSION_STORAGE_KEY = 'movilidadmdq.auth.v1';
const LIBRARIES: ('places')[] = ['places'];

interface AuthSession {
  id: number;
  username: string;
  email: string;
  token: string;
}

interface UsuarioResponse {
  id: number;
  username: string;
  email: string;
}

interface OpcionTransporteApi {
  tipo: 'TAXI' | 'UBER' | 'DIDI';
  precioMin: number;
  precioMax: number;
  tiempoMinutos: number;
  url: string;
}

interface Opcion {
  tipo: string;
  precio: string;
  tiempo: string;
  color: string;
  icon: ReactNode;
  url: string;
}

interface ViajeHistorial {
  id: number;
  origen: string;
  destino: string;
  distanciaEnMetros: number;
  tiempoEstimadoMin: number;
  precioTaxi: number;
  precioMinApp: number;
  precioMaxApp: number;
  fechaHora: string;
}

type AuthMode = 'login' | 'registro';
type AppView = 'calculo' | 'historial';

interface AppContentProps {
  isLoaded: boolean;
  loadError: Error | undefined;
}

const readStoredSession = (): AuthSession | null => {
  const rawSession = window.localStorage.getItem(SESSION_STORAGE_KEY);
  if (!rawSession) return null;

  try {
    return JSON.parse(rawSession) as AuthSession;
  } catch {
    window.localStorage.removeItem(SESSION_STORAGE_KEY);
    return null;
  }
};

const formatPrecio = (precio: number) =>
  new Intl.NumberFormat('es-AR', {
    style: 'currency',
    currency: 'ARS',
    maximumFractionDigits: 0,
  }).format(precio);

function AppContent({ isLoaded, loadError }: AppContentProps) {
  const [session, setSession] = useState<AuthSession | null>(() => readStoredSession());
  const [authMode, setAuthMode] = useState<AuthMode>('login');
  const [authForm, setAuthForm] = useState({ username: '', email: '', password: '' });
  const [activeView, setActiveView] = useState<AppView>('calculo');
  const [authLoading, setAuthLoading] = useState(false);
  const [authError, setAuthError] = useState<string | null>(null);
  const [resultados, setResultados] = useState<Opcion[] | null>(null);
  const [historial, setHistorial] = useState<ViajeHistorial[] | null>(null);
  const [historialLoading, setHistorialLoading] = useState(false);
  const [historialError, setHistorialError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const token = new URLSearchParams(window.location.search).get('token');
    if (!token) return;

    const completarSesionOAuth = async () => {
      setAuthLoading(true);
      setAuthError(null);

      try {
        const response = await fetch(`${API_URL}/usuarios/me`, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });

        if (!response.ok) throw new Error('No se pudo completar el inicio con Google.');

        const usuario: UsuarioResponse = await response.json();
        const nextSession = { ...usuario, token };
        window.localStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(nextSession));
        setSession(nextSession);
        window.history.replaceState({}, document.title, '/');
      } catch (oauthError) {
        setAuthError(oauthError instanceof Error ? oauthError.message : 'Error de autenticacion OAuth2.');
      } finally {
        setAuthLoading(false);
      }
    };

    void completarSesionOAuth();
  }, []);

  const guardarSesion = (nextSession: AuthSession) => {
    window.localStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(nextSession));
    setSession(nextSession);
    setActiveView('calculo');
    setResultados(null);
    setHistorial(null);
    setError(null);
  };

  const cerrarSesion = () => {
    window.localStorage.removeItem(SESSION_STORAGE_KEY);
    setSession(null);
    setActiveView('calculo');
    setResultados(null);
    setHistorial(null);
    setError(null);
  };

  const cargarHistorial = async () => {
    if (!session) return;

    setHistorialLoading(true);
    setHistorialError(null);

    try {
      const response = await fetch(`${API_URL}/usuarios/${session.id}/historial`, {
        headers: {
          Authorization: `Bearer ${session.token}`,
        },
      });

      if (response.status === 401 || response.status === 403) {
        cerrarSesion();
        throw new Error('Tu sesion vencio. Inicia sesion otra vez.');
      }

      if (!response.ok) throw new Error('No se pudo cargar el historial.');

      const data: ViajeHistorial[] = await response.json();
      setHistorial(data);
    } catch (historyError) {
      setHistorialError(historyError instanceof Error ? historyError.message : 'Error al cargar historial.');
      setHistorial([]);
    } finally {
      setHistorialLoading(false);
    }
  };

  useEffect(() => {
    if (session && activeView === 'historial') {
      void cargarHistorial();
    }
  }, [activeView, session?.id]);

  const handleAuthSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setAuthLoading(true);
    setAuthError(null);

    const endpoint = authMode === 'login' ? '/usuarios/login' : '/usuarios/registro';
    const payload = authMode === 'login'
      ? { username: authForm.username, password: authForm.password }
      : authForm;

    try {
      const response = await fetch(`${API_URL}${endpoint}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload),
      });

      if (!response.ok) {
        throw new Error(authMode === 'login' ? 'Usuario o contrasena incorrectos.' : 'No se pudo crear el usuario.');
      }

      const data: AuthSession = await response.json();
      guardarSesion(data);
    } catch (authSubmitError) {
      setAuthError(authSubmitError instanceof Error ? authSubmitError.message : 'Error de autenticacion.');
    } finally {
      setAuthLoading(false);
    }
  };

  const handleGoogleLogin = () => {
    window.location.href = `${API_URL}/oauth2/authorization/google`;
  };

  const handleSelectOption = (url: string) => {
    if (!url) return;

    if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('tel:')) {
      window.open(url, '_blank', 'noopener,noreferrer');
    }
  };

  const formatFecha = (fechaHora: string) =>
    new Intl.DateTimeFormat('es-AR', {
      dateStyle: 'short',
      timeStyle: 'short',
    }).format(new Date(fechaHora));

  const handleCalculate = async (origen: string, destino: string) => {
    if (!session) {
      setError('Inicia sesion para calcular y guardar tu viaje.');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const response = await fetch(`${API_URL}/viajes/calcular`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${session.token}`,
        },
        body: JSON.stringify({ origen, destino }),
      });

      if (response.status === 401 || response.status === 403) {
        cerrarSesion();
        throw new Error('Tu sesion vencio. Inicia sesion otra vez.');
      }

      if (!response.ok) throw new Error('No se pudieron calcular las opciones.');

      const data: OpcionTransporteApi[] = await response.json();

      const mappedData: Opcion[] = data.map((item) => {
        const isSamePrice = item.precioMin === item.precioMax;
        const precio = isSamePrice
          ? formatPrecio(item.precioMin)
          : `${formatPrecio(item.precioMin)} - ${formatPrecio(item.precioMax)}`;

        let config = {
          tipo: 'Taxi',
          color: 'bg-yellow-500',
          icon: <Car size={24} />,
        };

        if (item.tipo === 'UBER') {
          config = { tipo: 'Uber', color: 'bg-black', icon: <Smartphone size={24} /> };
        } else if (item.tipo === 'DIDI') {
          config = { tipo: 'Didi', color: 'bg-orange-500', icon: <Smartphone size={24} /> };
        }

        return {
          ...config,
          precio,
          tiempo: `${item.tiempoMinutos} min`,
          url: item.url,
        };
      });

      setResultados(mappedData);
    } catch (calculateError) {
      console.error('Error al calcular viaje:', calculateError);
      setError(calculateError instanceof Error ? calculateError.message : 'Ocurrio un error inesperado.');
      setResultados(null);
    } finally {
      setLoading(false);
    }
  };

  const mapReady = !GOOGLE_MAPS_API_KEY || isLoaded || Boolean(loadError);

  if (!mapReady) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center font-sans">
        <p className="text-gray-400 font-medium">Cargando aplicacion...</p>
      </div>
    );
  }

  if (!session) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4 font-sans">
        <motion.section
          initial={{ opacity: 0, y: 18 }}
          animate={{ opacity: 1, y: 0 }}
          className="w-full max-w-md rounded-3xl bg-white p-6 shadow-xl shadow-gray-200/70"
        >
          <div className="mb-6">
            <p className="text-sm font-bold uppercase tracking-[0.25em] text-gray-400">MovilidadMDQ</p>
            <h1 className="mt-2 text-3xl font-black tracking-tight text-gray-950">
              {authMode === 'login' ? 'Inicia sesion' : 'Crea tu cuenta'}
            </h1>
            <p className="mt-2 text-sm font-medium text-gray-500">
              Necesitas una sesion para calcular viajes y guardar historial en AWS.
            </p>
          </div>

          <form onSubmit={handleAuthSubmit} className="space-y-4">
            <label className="relative block">
              <User className="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" size={19} />
              <input
                value={authForm.username}
                onChange={(event) => setAuthForm((current) => ({ ...current, username: event.target.value }))}
                className="w-full rounded-2xl bg-gray-50 py-4 pl-12 pr-4 text-gray-900 outline-none transition-all focus:ring-2 focus:ring-black"
                placeholder="Usuario"
                autoComplete="username"
                required
              />
            </label>

            {authMode === 'registro' ? (
              <label className="relative block">
                <Mail className="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" size={19} />
                <input
                  value={authForm.email}
                  onChange={(event) => setAuthForm((current) => ({ ...current, email: event.target.value }))}
                  className="w-full rounded-2xl bg-gray-50 py-4 pl-12 pr-4 text-gray-900 outline-none transition-all focus:ring-2 focus:ring-black"
                  placeholder="Email"
                  type="email"
                  autoComplete="email"
                  required
                />
              </label>
            ) : null}

            <label className="relative block">
              <LockKeyhole className="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" size={19} />
              <input
                value={authForm.password}
                onChange={(event) => setAuthForm((current) => ({ ...current, password: event.target.value }))}
                className="w-full rounded-2xl bg-gray-50 py-4 pl-12 pr-4 text-gray-900 outline-none transition-all focus:ring-2 focus:ring-black"
                placeholder="Contrasena"
                type="password"
                autoComplete={authMode === 'login' ? 'current-password' : 'new-password'}
                required
              />
            </label>

            {authError ? (
              <div className="rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-semibold text-red-700">
                {authError}
              </div>
            ) : null}

            <button
              type="submit"
              disabled={authLoading}
              className="w-full rounded-2xl bg-black py-4 text-lg font-black text-white shadow-lg transition-all hover:bg-gray-800 disabled:bg-gray-400"
            >
              {authLoading ? 'Procesando...' : authMode === 'login' ? 'ENTRAR' : 'REGISTRARME'}
            </button>
          </form>

          <button
            type="button"
            onClick={handleGoogleLogin}
            className="mt-3 w-full rounded-2xl border border-gray-200 bg-white py-4 text-sm font-black text-gray-800 transition-all hover:bg-gray-50"
          >
            Continuar con Google
          </button>

          <button
            type="button"
            onClick={() => {
              setAuthMode((current) => (current === 'login' ? 'registro' : 'login'));
              setAuthError(null);
            }}
            className="mt-5 w-full text-center text-sm font-bold text-gray-500 hover:text-gray-900"
          >
            {authMode === 'login' ? 'No tienes cuenta? Registrate' : 'Ya tienes cuenta? Inicia sesion'}
          </button>
        </motion.section>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 flex justify-center p-4 sm:p-8 font-sans">
      <div className="w-full max-w-md">
        <header className="mb-8 flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-black text-gray-900 tracking-tight">MovilidadMDQ</h1>
            <p className="text-gray-500 font-medium">Hola, {session.username}</p>
          </div>
          <button
            type="button"
            onClick={cerrarSesion}
            className="flex h-12 w-12 items-center justify-center rounded-full border border-gray-100 bg-white text-gray-500 shadow-sm transition-all hover:text-gray-900"
            title="Cerrar sesion"
          >
            <LogOut size={19} />
          </button>
        </header>

        <nav className="mb-6 grid grid-cols-2 gap-3 rounded-3xl bg-white p-2 shadow-sm shadow-gray-200/60">
          <button
            type="button"
            onClick={() => setActiveView('calculo')}
            className={`flex items-center justify-center gap-2 rounded-2xl py-3 text-sm font-black transition-all ${activeView === 'calculo' ? 'bg-black text-white' : 'text-gray-500 hover:bg-gray-50 hover:text-gray-900'}`}
          >
            <Home size={17} /> Calcular
          </button>
          <button
            type="button"
            onClick={() => setActiveView('historial')}
            className={`flex items-center justify-center gap-2 rounded-2xl py-3 text-sm font-black transition-all ${activeView === 'historial' ? 'bg-black text-white' : 'text-gray-500 hover:bg-gray-50 hover:text-gray-900'}`}
          >
            <History size={17} /> Historial
          </button>
        </nav>

        {loadError ? (
          <div className="mb-4 rounded-2xl border border-yellow-200 bg-yellow-50 px-4 py-3 text-sm font-semibold text-yellow-800">
            Google Maps no cargo en el navegador. Puedes escribir las direcciones manualmente.
          </div>
        ) : null}

        {activeView === 'calculo' ? (
          <>
            <section className="bg-white rounded-3xl p-6 shadow-xl shadow-gray-200/50 mb-8">
              <InputForm onCalculate={handleCalculate} loading={loading} onInputChange={() => setError(null)} />
            </section>

            <div className="space-y-4">
              {error ? (
                <div className="rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-semibold text-red-700">
                  {error}
                </div>
              ) : null}

              <AnimatePresence>
                {loading ? (
                  <motion.div
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    exit={{ opacity: 0 }}
                    className="text-center py-12 border-2 border-dashed border-gray-200 rounded-3xl"
                  >
                    <p className="text-gray-400 font-medium">Calculando opciones...</p>
                  </motion.div>
                ) : resultados ? (
                  <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}>
                    <div className="flex items-center justify-between mb-4 px-1">
                      <h2 className="text-lg font-bold text-gray-800">Opciones disponibles</h2>
                      <span className="text-xs font-bold text-gray-400 uppercase tracking-widest flex items-center gap-1">
                        <CreditCard size={12} /> ARS
                      </span>
                    </div>
                    {resultados.map((opcion, index) => (
                      <ResultadoCard
                        key={`${opcion.tipo}-${index}`}
                        {...opcion}
                        delay={index * 0.1}
                        onClick={() => handleSelectOption(opcion.url)}
                      />
                    ))}
                  </motion.div>
                ) : (
                  <motion.div
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    className="text-center py-12 border-2 border-dashed border-gray-200 rounded-3xl"
                  >
                    <p className="text-gray-400 font-medium">Ingresa tu ruta para ver opciones</p>
                  </motion.div>
                )}
              </AnimatePresence>
            </div>
          </>
        ) : (
          <section className="space-y-4">
            <div className="flex items-center justify-between px-1">
              <div>
                <h2 className="text-xl font-black text-gray-900">Tu historial</h2>
                <p className="text-sm font-medium text-gray-500">Viajes guardados en AWS RDS</p>
              </div>
              <button
                type="button"
                onClick={cargarHistorial}
                disabled={historialLoading}
                className="flex h-11 w-11 items-center justify-center rounded-full bg-white text-gray-500 shadow-sm transition-all hover:text-gray-900 disabled:text-gray-300"
                title="Recargar historial"
              >
                <RefreshCw size={18} className={historialLoading ? 'animate-spin' : ''} />
              </button>
            </div>

            {historialError ? (
              <div className="rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-semibold text-red-700">
                {historialError}
              </div>
            ) : null}

            {historialLoading && !historial ? (
              <div className="rounded-3xl border-2 border-dashed border-gray-200 py-12 text-center text-gray-400 font-medium">
                Cargando historial...
              </div>
            ) : historial && historial.length > 0 ? (
              <div className="space-y-3">
                {historial.map((viaje) => (
                  <motion.article
                    key={viaje.id}
                    initial={{ opacity: 0, y: 12 }}
                    animate={{ opacity: 1, y: 0 }}
                    className="rounded-3xl border border-gray-100 bg-white p-5 shadow-sm shadow-gray-200/60"
                  >
                    <div className="space-y-3">
                      <div className="flex gap-3 text-sm font-semibold text-gray-700">
                        <MapPin size={18} className="mt-0.5 shrink-0 text-gray-400" />
                        <span>{viaje.origen}</span>
                      </div>
                      <div className="flex gap-3 text-sm font-semibold text-gray-700">
                        <Navigation size={18} className="mt-0.5 shrink-0 text-gray-400" />
                        <span>{viaje.destino}</span>
                      </div>
                    </div>

                    <div className="mt-4 flex items-end justify-between border-t border-gray-100 pt-4">
                      <div>
                        <p className="text-xs font-bold uppercase tracking-widest text-gray-400">{formatFecha(viaje.fechaHora)}</p>
                        <p className="mt-1 text-sm font-bold text-gray-500">{viaje.tiempoEstimadoMin} min · {(viaje.distanciaEnMetros / 1000).toFixed(1)} km</p>
                      </div>
                      <div className="text-right">
                        <p className="text-xs font-bold uppercase tracking-widest text-gray-400">Taxi</p>
                        <p className="text-lg font-black text-gray-900">{formatPrecio(viaje.precioTaxi)}</p>
                      </div>
                    </div>
                  </motion.article>
                ))}
              </div>
            ) : (
              <div className="rounded-3xl border-2 border-dashed border-gray-200 py-12 text-center">
                <p className="font-bold text-gray-500">Todavia no tenes viajes guardados.</p>
                <p className="mt-1 text-sm font-medium text-gray-400">Calcula una ruta para verla aca.</p>
              </div>
            )}
          </section>
        )}

        <footer className="mt-8 text-center">
          <p className="text-gray-400 text-sm">Precios estimados basados en la tarifa actual.</p>
        </footer>
      </div>
    </div>
  );
}

function MapEnabledApp() {
  const { isLoaded, loadError } = useJsApiLoader({
    id: 'google-map-script',
    googleMapsApiKey: GOOGLE_MAPS_API_KEY,
    version: 'beta',
    libraries: LIBRARIES,
  });

  return <AppContent isLoaded={isLoaded} loadError={loadError} />;
}

function App() {
  if (!GOOGLE_MAPS_API_KEY) {
    return <AppContent isLoaded={true} loadError={new Error('Google Maps API key missing')} />;
  }

  return <MapEnabledApp />;
}

export default App;
