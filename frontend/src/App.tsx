import { useEffect, useState, useRef, type FormEvent, type ReactNode } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Car, Smartphone, CreditCard, LogOut, User, Mail, LockKeyhole, History, Home, MapPin, Navigation, RefreshCw, Trash2, Repeat, Settings, Star, Sun, Moon } from 'lucide-react';
import { useJsApiLoader } from '@react-google-maps/api';
import InputForm, { type LugarSeleccionado } from './components/InputForm';
import ResultadoCard from './components/ResultadoCard';
import ProfileView from './components/ProfileView';
import AdminDashboard from './components/AdminDashboard';
import FavoritesView from './components/FavoritesView';
import type { AuthSession, DireccionFavorita } from './types';

const API_URL = (import.meta.env.VITE_API_URL ?? '/api').replace(/\/$/, '');
const OAUTH_BASE_URL = (import.meta.env.VITE_OAUTH_BASE_URL ?? 'https://movilidadmdq.ddns.net').replace(/\/$/, '');
const GOOGLE_MAPS_API_KEY = import.meta.env.VITE_GOOGLE_MAPS_API_KEY ?? '';
const SESSION_STORAGE_KEY = 'movilidadmdq.auth.v1';
const THEME_STORAGE_KEY = 'movilidadmdq.theme.v1';
const LIBRARIES: ('places' | 'geocoding')[] = ['places', 'geocoding'];

const getApiUrl = (path: string) => `${API_URL}${path}`;

interface UsuarioResponse {
  id: number;
  username: string;
  email: string;
  role: 'USER' | 'ADMIN';
}

interface OpcionTransporteApi {
  tipo: 'TAXI' | 'UBER' | 'DIDI';
  precio: number;
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
  precioUber: number;
  precioDidi: number;
  fechaHora: string;
  favorito: boolean;
  origenPlaceId?: string;
  origenLat?: number;
  origenLng?: number;
  destinoPlaceId?: string;
  destinoLat?: number;
  destinoLng?: number;
}

interface ViajeFrecuente {
    origen: string;
    destino: string;
    cantidad: number;
}

type AuthMode = 'login' | 'registro';
type AppView = 'calculo' | 'historial' | 'favoritos' | 'perfil' | 'admin';

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
  const [theme, setTheme] = useState<'light' | 'dark'>(() => {
    const stored = window.localStorage.getItem(THEME_STORAGE_KEY);
    if (stored === 'dark' || stored === 'light') return stored;
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
  });
  const [authLoading, setAuthLoading] = useState(false);
  const [authError, setAuthError] = useState<string | null>(null);
  const [resultados, setResultados] = useState<Opcion[] | null>(null);
  const [historial, setHistorial] = useState<ViajeHistorial[] | null>(null);
  const [favoritos, setFavoritos] = useState<DireccionFavorita[] | null>(null);
  const [viajeFrecuente, setViajeFrecuente] = useState<ViajeFrecuente | null>(null);
  const [historialLoading, setHistorialLoading] = useState(false);
  const [historialError, setHistorialError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [formInitialData, setFormInitialData] = useState<{
    origen: string;
    destino: string;
    origenPlace?: LugarSeleccionado;
    destinoPlace?: LugarSeleccionado;
  } | null>(null);

  const resultsRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!loading && resultados && resultados.length > 0) {
      // Usamos un pequeño delay para que la animación de Framer Motion no interfiera
      // y aseguramos que el scroll ocurra cuando el componente esté plenamente montado.
      const timer = setTimeout(() => {
        resultsRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' });
      }, 100);
      return () => clearTimeout(timer);
    }
  }, [resultados, loading]);

  useEffect(() => {
    if (theme === 'dark') {
      document.documentElement.classList.add('dark');
      document.documentElement.style.colorScheme = 'dark';
    } else {
      document.documentElement.classList.remove('dark');
      document.documentElement.style.colorScheme = 'light';
    }
    window.localStorage.setItem(THEME_STORAGE_KEY, theme);
  }, [theme]);

  const toggleTheme = () => setTheme((curr) => (curr === 'light' ? 'dark' : 'light'));

  useEffect(() => {
    const token = new URLSearchParams(window.location.search).get('token');
    if (!token) return;

    const completarSesionOAuth = async () => {
      setAuthLoading(true);
      setAuthError(null);

      try {
        const response = await fetch(getApiUrl('/usuarios/me'), {
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
    // Forzar limpieza de la URL y recarga para evitar que queden rastros de tokens viejos
    window.location.href = window.location.origin + window.location.pathname;
  };

  const cargarHistorial = async () => {
    if (!session) return;

    setHistorialLoading(true);
    setHistorialError(null);

    try {
      const response = await fetch(getApiUrl(`/usuarios/${session.id}/historial`), {
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

    const cargarViajeFrecuente = async () => {
        if (!session) return;

        try {
            const response = await fetch(getApiUrl(`/usuarios/${session.id}/viaje-frecuente`), {
                headers: {
                    Authorization: `Bearer ${session.token}`,
                },
            });

            if (response.status === 204) {
                setViajeFrecuente(null);
                return;
            }

            if (response.status === 401 || response.status === 403) {
                cerrarSesion();
                return;
            }

            if (!response.ok) return;

            const data: ViajeFrecuente = await response.json();
            setViajeFrecuente(data);
        } catch {
            setViajeFrecuente(null);
        }
    };

    const cargarFavoritos = async () => {
        if (!session) return;

        try {
            const response = await fetch(getApiUrl('/viajes/direcciones-favoritas'), {
                headers: {
                    Authorization: `Bearer ${session.token}`,
                },
            });

            if (response.status === 401 || response.status === 403) {
                cerrarSesion();
                return;
            }

            if (!response.ok) return;

            const data: DireccionFavorita[] = await response.json();
            setFavoritos(data);
        } catch (error) {
            console.error('Error al cargar favoritos:', error);
        }
    };

    const borrarViaje = async (viajeId: number) => {
        if (!session) return;

        try {
            const response = await fetch(getApiUrl(`/usuarios/${session.id}/historial/${viajeId}`), {
                method: 'DELETE',
                headers: {
                    Authorization: `Bearer ${session.token}`,
                },
            });

            if (response.status === 401 || response.status === 403) {
                cerrarSesion();
                throw new Error('Tu sesion vencio. Inicia sesion otra vez.');
            }

            if (!response.ok) throw new Error('No se pudo borrar el viaje.');

            setHistorial((current) => current?.filter((viaje) => viaje.id !== viajeId) ?? []);
            void cargarViajeFrecuente();
        } catch (deleteError) {
            setHistorialError(deleteError instanceof Error ? deleteError.message : 'Error al borrar viaje.');
        }
    };

    const repetirViaje = (
        origen: string,
        destino: string,
        origenPlace?: LugarSeleccionado,
        destinoPlace?: LugarSeleccionado
    ) => {
        setFormInitialData({ origen, destino, origenPlace, destinoPlace });
        setActiveView('calculo');
        void handleCalculate(origen, destino, origenPlace, destinoPlace);
    };

    const toggleFavorito = async (viajeId: number) => {
        if (!session) return;

        try {
            const response = await fetch(getApiUrl(`/viajes/${viajeId}/favorito`), {
                method: 'PUT',
                headers: {
                    Authorization: `Bearer ${session.token}`,
                },
            });

            if (response.status === 401 || response.status === 403) {
                cerrarSesion();
                return;
            }

            if (!response.ok) throw new Error('No se pudo cambiar el estado de favorito.');

            // Actualización optimista del historial
            setHistorial((current) =>
                current?.map((viaje) =>
                    viaje.id === viajeId ? { ...viaje, favorito: !viaje.favorito } : viaje
                ) ?? []
            );

            // Recargamos el pool de direcciones favoritas para asegurar consistencia
            void cargarFavoritos();
        } catch (error) {
            console.error('Error al cambiar favorito:', error);
        }
    };

    const renombrarFavorito = async (id: number, nuevoNombre: string) => {
        if (!session) return;

        try {
            const response = await fetch(getApiUrl(`/viajes/direcciones-favoritas/${id}`), {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${session.token}`,
                },
                body: JSON.stringify({ nombre: nuevoNombre }),
            });

            if (!response.ok) throw new Error('No se pudo renombrar el favorito.');

            // Actualización optimista
            setFavoritos((current) =>
                current?.map((fav) => (fav.id === id ? { ...fav, nombre: nuevoNombre } : fav)) ?? []
            );
        } catch (error) {
            console.error('Error al renombrar favorito:', error);
            throw error;
        }
    };

    const eliminarFavorito = async (id: number) => {
        if (!session) return;

        try {
            const response = await fetch(getApiUrl(`/viajes/direcciones-favoritas/${id}`), {
                method: 'DELETE',
                headers: {
                    Authorization: `Bearer ${session.token}`,
                },
            });

            if (!response.ok) throw new Error('No se pudo eliminar el favorito.');

            // Actualización optimista
            setFavoritos((current) => current?.filter((fav) => fav.id !== id) ?? []);
        } catch (error) {
            console.error('Error al eliminar favorito:', error);
            throw error;
        }
    };

  useEffect(() => {
    if (session && activeView === 'historial') {
      void cargarHistorial();
    }
  }, [activeView, session?.id]);

  useEffect(() => {
      if (session && (activeView === 'calculo' || activeView === 'favoritos')) {
          void cargarViajeFrecuente();
          void cargarFavoritos();
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
      const response = await fetch(getApiUrl(endpoint), {
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
    window.location.href = `${OAUTH_BASE_URL}/oauth2/authorization/google`;
  };

  const handleSelectOption = (url: string) => {
    if (!url) return;

    if (url.startsWith("uber://")) {
      const isMobile = /Android|iPhone|iPad|iPod/i.test(navigator.userAgent);

      if (isMobile) {
        window.location.href = url;
      } else {
        // Fallback desktop
        window.open("https://m.uber.com/", "_blank", "noopener,noreferrer");
      }
      return;
    }

    if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("tel:")) {
      window.open(url, "_blank", "noopener,noreferrer");
    }
  };

  const formatFecha = (fechaHora: string) =>
    new Intl.DateTimeFormat('es-AR', {
      dateStyle: 'short',
      timeStyle: 'short',
    }).format(new Date(fechaHora));

  const handleCalculate = async (
      origen: string,
      destino: string,
      origenPlace?: LugarSeleccionado,
      destinoPlace?: LugarSeleccionado
  ) => {
    if (!session) {
      setError('Inicia sesion para calcular y guardar tu viaje.');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const response = await fetch(getApiUrl('/viajes/calcular'), {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${session.token}`,
        },
        body: JSON.stringify({
          origen,
          destino,
          origenAddressLine1: origenPlace?.addressLine1,
          origenAddressLine2: origenPlace?.addressLine2,
          origenPlaceId: origenPlace?.placeId,
          origenLat: origenPlace?.latitude,
          origenLng: origenPlace?.longitude,
          destinoAddressLine1: destinoPlace?.addressLine1,
          destinoAddressLine2: destinoPlace?.addressLine2,
          destinoPlaceId: destinoPlace?.placeId,
          destinoLat: destinoPlace?.latitude,
          destinoLng: destinoPlace?.longitude,
        }),
      });

      if (response.status === 401 || response.status === 403) {
        cerrarSesion();
        throw new Error('Tu sesion vencio. Inicia sesion otra vez.');
      }

      if (!response.ok) {
        throw new Error('No se pudieron calcular las opciones.');
      }

      const data: OpcionTransporteApi[] = await response.json();

      const mappedData: Opcion[] = data.map((item) => {
        const precio = formatPrecio(item.precio);

        let config = {
          tipo: 'Taxi',
          color: 'bg-yellow-500',
          icon: <Car size={24} />,
        };

        if (item.tipo === 'UBER') {
          config = {
            tipo: 'Uber',
            color: 'bg-black',
            icon: <Smartphone size={24} />,
          };
        } else if (item.tipo === 'DIDI') {
          config = {
            tipo: 'Didi',
            color: 'bg-orange-500',
            icon: <Smartphone size={24} />,
          };
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
      <div className="min-h-screen bg-gray-50 dark:bg-gray-950 flex items-center justify-center font-sans">
        <p className="text-gray-400 font-medium">Cargando aplicacion...</p>
      </div>
    );
  }

  if (!session) {
    return (
      <div className="min-h-screen bg-gray-50 dark:bg-gray-950 flex items-center justify-center p-4 font-sans">
        <motion.section
          initial={{ opacity: 0, y: 18 }}
          animate={{ opacity: 1, y: 0 }}
          className="w-full max-w-md rounded-3xl bg-white dark:bg-gray-900 p-6 shadow-xl shadow-gray-200/70 dark:shadow-black/50"
        >
          <div className="mb-6">
            <p className="text-sm font-bold uppercase tracking-[0.25em] text-gray-400">MovilidadMDQ</p>
            <h1 className="mt-2 text-3xl font-black tracking-tight text-gray-950 dark:text-gray-100">
              {authMode === 'login' ? 'Inicia sesion' : 'Crea tu cuenta'}
            </h1>
            <p className="mt-2 text-sm font-medium text-gray-500 dark:text-gray-400">
              Necesitas una sesion para calcular viajes y guardar historial en AWS.
            </p>
          </div>

          <form onSubmit={handleAuthSubmit} className="space-y-4">
            <label className="relative block">
              <User className="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" size={19} />
              <input
                value={authForm.username}
                onChange={(event) => setAuthForm((current) => ({ ...current, username: event.target.value }))}
                className="w-full rounded-2xl bg-gray-50 dark:bg-gray-800 py-4 pl-12 pr-4 text-gray-900 dark:text-gray-100 outline-none transition-all focus:ring-2 focus:ring-black dark:focus:ring-white"
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
                  className="w-full rounded-2xl bg-gray-50 dark:bg-gray-800 py-4 pl-12 pr-4 text-gray-900 dark:text-gray-100 outline-none transition-all focus:ring-2 focus:ring-black dark:focus:ring-white"
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
                className="w-full rounded-2xl bg-gray-50 dark:bg-gray-800 py-4 pl-12 pr-4 text-gray-900 dark:text-gray-100 outline-none transition-all focus:ring-2 focus:ring-black dark:focus:ring-white"
                placeholder="Contrasena"
                type="password"
                autoComplete={authMode === 'login' ? 'current-password' : 'new-password'}
                required
              />
            </label>

            {authError ? (
              <div className="rounded-2xl border border-red-200 bg-red-50 dark:bg-red-900/30 px-4 py-3 text-sm font-semibold text-red-700 dark:text-red-400">
                {authError}
              </div>
            ) : null}

            <button
              type="submit"
              disabled={authLoading}
              className="w-full rounded-2xl bg-black dark:bg-white py-4 text-lg font-black text-white dark:text-black shadow-lg transition-all hover:bg-gray-800 dark:hover:bg-gray-200 disabled:bg-gray-400"
            >
              {authLoading ? 'Procesando...' : authMode === 'login' ? 'ENTRAR' : 'REGISTRARME'}
            </button>
          </form>

          <button
            type="button"
            onClick={handleGoogleLogin}
            className="mt-3 w-full rounded-2xl border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 py-4 text-sm font-black text-gray-800 dark:text-gray-200 transition-all hover:bg-gray-50 dark:hover:bg-gray-700"
          >
            Continuar con Google
          </button>

          <button
            type="button"
            onClick={() => {
              setAuthMode((current) => (current === 'login' ? 'registro' : 'login'));
              setAuthError(null);
            }}
            className="mt-5 w-full text-center text-sm font-bold text-gray-500 dark:text-gray-400 hover:text-gray-900 dark:hover:text-gray-100"
          >
            {authMode === 'login' ? 'No tienes cuenta? Registrate' : 'Ya tienes cuenta? Inicia sesion'}
          </button>
        </motion.section>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-950 flex justify-center p-4 sm:p-8 font-sans transition-colors duration-300">
      <div className="w-full max-w-md">
        <header className="mb-8 flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-black text-gray-900 dark:text-white tracking-tight">MovilidadMDQ</h1>
            <p className="text-gray-500 dark:text-gray-400 font-medium">
              Hola, {session.username}
            </p>
          </div>
          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={() => setActiveView('perfil')}
              className="flex h-12 w-12 items-center justify-center rounded-full border border-gray-100 dark:border-gray-800 bg-white dark:bg-gray-900 text-gray-500 dark:text-gray-400 shadow-sm transition-all hover:text-gray-900 dark:hover:text-white"
              title="Mi perfil"
            >
              <User size={19} />
            </button>
            <button
              type="button"
              onClick={toggleTheme}
              className="flex h-12 w-12 items-center justify-center rounded-full border border-gray-100 dark:border-gray-800 bg-white dark:bg-gray-900 text-gray-500 dark:text-gray-400 shadow-sm transition-all hover:text-gray-900 dark:hover:text-white"
              title={theme === 'light' ? 'Activar modo oscuro' : 'Activar modo claro'}
            >
              {theme === 'light' ? <Moon size={19} /> : <Sun size={19} />}
            </button>
            <button
              type="button"
              onClick={cerrarSesion}
              className="flex h-12 w-12 items-center justify-center rounded-full border border-gray-100 dark:border-gray-800 bg-white dark:bg-gray-900 text-gray-500 dark:text-gray-400 shadow-sm transition-all hover:text-gray-900 dark:hover:text-white"
              title="Cerrar sesion"
            >
              <LogOut size={19} />
            </button>
          </div>
        </header>

        <nav className={`mb-6 grid ${(session.role === 'ADMIN' || session.username === 'admin') ? 'grid-cols-4' : 'grid-cols-3'} gap-3 rounded-3xl bg-white dark:bg-gray-900 p-2 shadow-sm shadow-gray-200/60 dark:shadow-black/40`}>
          <button
            type="button"
            onClick={() => setActiveView('calculo')}
            className={`flex items-center justify-center gap-2 rounded-2xl py-3 text-sm font-black transition-all ${activeView === 'calculo' ? 'bg-black dark:bg-white text-white dark:text-black' : 'text-gray-500 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-gray-800 hover:text-gray-900 dark:hover:text-white'}`}
          >
            <Home size={17} /> Calcular
          </button>
          <button
            type="button"
            onClick={() => setActiveView('historial')}
            className={`flex items-center justify-center gap-2 rounded-2xl py-3 text-sm font-black transition-all ${activeView === 'historial' ? 'bg-black dark:bg-white text-white dark:text-black' : 'text-gray-500 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-gray-800 hover:text-gray-900 dark:hover:text-white'}`}
          >
            <History size={17} /> Historial
          </button>
          <button
            type="button"
            onClick={() => setActiveView('favoritos')}
            className={`flex items-center justify-center gap-2 rounded-2xl py-3 text-sm font-black transition-all ${activeView === 'favoritos' ? 'bg-black dark:bg-white text-white dark:text-black' : 'text-gray-500 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-gray-800 hover:text-gray-900 dark:hover:text-white'}`}
          >
            <Star size={17} /> Favoritos
          </button>
          {(session.role === 'ADMIN' || session.username === 'admin') && (
            <button
              type="button"
              onClick={() => setActiveView('admin')}
              className={`flex items-center justify-center gap-2 rounded-2xl py-3 text-sm font-black transition-all ${activeView === 'admin' ? 'bg-black dark:bg-white text-white dark:text-black' : 'text-gray-500 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-gray-800 hover:text-gray-900 dark:hover:text-white'}`}
            >
              <Settings size={17} /> Admin
            </button>
          )}
        </nav>

        {loadError ? (
          <div className="mb-4 rounded-2xl border border-yellow-200 dark:border-yellow-900 bg-yellow-50 dark:bg-yellow-900/20 px-4 py-3 text-sm font-semibold text-yellow-800 dark:text-yellow-200">
            Google Maps no cargo en el navegador. Puedes escribir las direcciones manualmente.
          </div>
        ) : null}

        {activeView === 'perfil' ? (
          <ProfileView
            session={session}
            onUpdate={setSession}
            onBack={() => setActiveView('calculo')}
            onLogout={cerrarSesion}
            apiUrl={API_URL}
          />
        ) : activeView === 'admin' ? (
          <AdminDashboard
            session={session}
            onBack={() => setActiveView('calculo')}
            apiUrl={API_URL}
          />
        ) : activeView === 'favoritos' ? (
          <FavoritesView 
            favoritos={favoritos || []}
            onRename={renombrarFavorito}
            onRemove={eliminarFavorito}
          />
        ) : activeView === 'calculo' ? (
          <>
              {viajeFrecuente ? (
                  <button
                      type="button"
                      onClick={() => repetirViaje(viajeFrecuente.origen, viajeFrecuente.destino)}
                      className="mb-4 w-full rounded-3xl border border-blue-100 dark:border-blue-900 bg-blue-50 dark:bg-blue-900/20 p-4 text-left shadow-sm transition-all hover:bg-blue-100 dark:hover:bg-blue-900/40"
                  >
                      <p className="text-xs font-black uppercase tracking-widest text-blue-500 dark:text-blue-400">
                          Viaje frecuente
                      </p>
                      <p className="mt-1 text-sm font-bold text-gray-900 dark:text-white">
                          {viajeFrecuente.origen} → {viajeFrecuente.destino}
                      </p>
                      <p className="mt-1 text-xs font-semibold text-gray-500 dark:text-gray-400">
                          Lo hiciste {viajeFrecuente.cantidad} veces. Toca para repetirlo.
                      </p>
                  </button>
              ) : null}

              <section className="bg-white dark:bg-gray-900 rounded-3xl p-6 shadow-xl shadow-gray-200/50 dark:shadow-black/40 mb-8">
                  <InputForm 
                    onCalculate={handleCalculate} 
                    loading={loading} 
                    onInputChange={() => setError(null)} 
                    favoritos={favoritos || []}
                    initialData={formInitialData || undefined}
                  />
              </section>

            <div className="space-y-4">
              {error ? (
                <div className="rounded-2xl border border-red-200 dark:border-red-900 bg-red-50 dark:bg-red-900/20 px-4 py-3 text-sm font-semibold text-red-700 dark:text-red-400">
                  {error}
                </div>
              ) : null}

              <AnimatePresence>
                {loading ? (
                  <motion.div
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    exit={{ opacity: 0 }}
                    className="text-center py-12 border-2 border-dashed border-gray-200 dark:border-gray-800 rounded-3xl"
                  >
                    <p className="text-gray-400 font-medium">Calculando opciones...</p>
                  </motion.div>
                ) : resultados ? (
                  <motion.div ref={resultsRef} initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}>
                    <div className="flex items-center justify-between mb-4 px-1">
                      <h2 className="text-lg font-bold text-gray-800 dark:text-gray-200">Opciones disponibles</h2>
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
                    className="text-center py-12 border-2 border-dashed border-gray-200 dark:border-gray-800 rounded-3xl"
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
                <h2 className="text-xl font-black text-gray-900 dark:text-white">Tu historial</h2>
                <p className="text-sm font-medium text-gray-500 dark:text-gray-400">Viajes guardados en AWS RDS</p>
              </div>
              <button
                type="button"
                onClick={cargarHistorial}
                disabled={historialLoading}
                className="flex h-11 w-11 items-center justify-center rounded-full bg-white dark:bg-gray-900 text-gray-500 dark:text-gray-400 shadow-sm transition-all hover:text-gray-900 dark:hover:text-white disabled:text-gray-300"
                title="Recargar historial"
              >
                <RefreshCw size={18} className={historialLoading ? 'animate-spin' : ''} />
              </button>
            </div>


            {historialError ? (
              <div className="rounded-2xl border border-red-200 dark:border-red-900 bg-red-50 dark:bg-red-900/20 px-4 py-3 text-sm font-semibold text-red-700 dark:text-red-400">
                {historialError}
              </div>
            ) : null}

            {historialLoading && !historial ? (
              <div className="rounded-3xl border-2 border-dashed border-gray-200 dark:border-gray-800 py-12 text-center text-gray-400 font-medium">
                Cargando historial...
              </div>
            ) : historial && historial.length > 0 ? (
              <div className="space-y-3">
                {historial.map((viaje) => (
                        <motion.article
                            key={viaje.id}
                            initial={{ opacity: 0, y: 12 }}
                            animate={{ opacity: 1, y: 0 }}
                            className="rounded-3xl border border-gray-100 dark:border-gray-800 bg-white dark:bg-gray-900 p-5 shadow-sm shadow-gray-200/60 dark:shadow-black/40"
                        >
                            <div className="space-y-3">
                                <div className="flex gap-3 text-sm font-semibold text-gray-700 dark:text-gray-300">
                                    <MapPin size={18} className="mt-0.5 shrink-0 text-gray-400" />
                                    <span>{viaje.origen}</span>
                                </div>
                                <div className="flex gap-3 text-sm font-semibold text-gray-700 dark:text-gray-300">
                                    <Navigation size={18} className="mt-0.5 shrink-0 text-gray-400" />
                                    <span>{viaje.destino}</span>
                                </div>
                            </div>

                            <div className="mt-4 flex items-end justify-between border-t border-gray-100 dark:border-gray-800 pt-4">
                                <div>
                                    <p className="text-xs font-bold uppercase tracking-widest text-gray-400">
                                        {formatFecha(viaje.fechaHora)}
                                    </p>
                                    <p className="mt-1 text-sm font-bold text-gray-500 dark:text-gray-400">
                                        {viaje.tiempoEstimadoMin} min · {(viaje.distanciaEnMetros / 1000).toFixed(1)} km
                                    </p>
                                </div>

                                <div className="flex flex-col items-end gap-3">
                                    <button
                                        type="button"
                                        onClick={() => toggleFavorito(viaje.id)}
                                        className={`flex h-8 w-8 items-center justify-center rounded-full transition-all ${
                                            viaje.favorito
                                            ? 'bg-yellow-50 dark:bg-yellow-900/30 text-yellow-500 shadow-sm border border-yellow-100 dark:border-yellow-900'
                                            : 'bg-gray-50 dark:bg-gray-800 text-gray-300 dark:text-gray-600 hover:text-gray-400 dark:hover:text-gray-500'
                                        }`}
                                        title={viaje.favorito ? "Quitar de favoritos" : "Marcar como favorito"}
                                    >
                                        <Star size={16} fill={viaje.favorito ? "currentColor" : "none"} />
                                    </button>

                                    <div className="flex flex-col items-end space-y-1">
                                      <div className="flex items-center gap-2">
                                        <span className="text-[10px] font-black uppercase tracking-tighter text-gray-400">Taxi</span>
                                        <span className="text-sm font-bold text-gray-900 dark:text-white">{formatPrecio(viaje.precioTaxi)}</span>
                                      </div>
                                      <div className="flex items-center gap-2">
                                        <span className="text-[10px] font-black uppercase tracking-tighter text-blue-500">Uber</span>
                                        <span className="text-sm font-bold text-gray-900 dark:text-white">{formatPrecio(viaje.precioUber)}</span>
                                      </div>
                                      <div className="flex items-center gap-2">
                                        <span className="text-[10px] font-black uppercase tracking-tighter text-orange-500">Didi</span>
                                        <span className="text-sm font-bold text-gray-900 dark:text-white">{formatPrecio(viaje.precioDidi)}</span>
                                      </div>
                                    </div>
                                </div>
                                </div>

                          <div className="mt-4 grid grid-cols-2 gap-2">
                            <button
                                type="button"
                                onClick={() => repetirViaje(
                                    viaje.origen, 
                                    viaje.destino,
                                    viaje.origenPlaceId ? {
                                        addressLine1: viaje.origen,
                                        addressLine2: viaje.origen,
                                        placeId: viaje.origenPlaceId,
                                        latitude: viaje.origenLat ?? 0,
                                        longitude: viaje.origenLng ?? 0
                                    } : undefined,
                                    viaje.destinoPlaceId ? {
                                        addressLine1: viaje.destino,
                                        addressLine2: viaje.destino,
                                        placeId: viaje.destinoPlaceId,
                                        latitude: viaje.destinoLat ?? 0,
                                        longitude: viaje.destinoLng ?? 0
                                    } : undefined
                                )}
                                className="flex items-center justify-center gap-2 rounded-2xl bg-black dark:bg-white px-3 py-3 text-sm font-black text-white dark:text-black transition-all hover:bg-gray-800 dark:hover:bg-gray-200"
                            >
                                <Repeat size={16} />
                                Repetir
                            </button>

                            <button
                                type="button"
                                onClick={() => borrarViaje(viaje.id)}
                                className="flex items-center justify-center gap-2 rounded-2xl border border-red-100 dark:border-red-900 bg-red-50 dark:bg-red-900/20 px-3 py-3 text-sm font-black text-red-600 dark:text-red-400 transition-all hover:bg-red-100 dark:hover:bg-red-900/40"
                            >
                                <Trash2 size={16} />
                                Borrar
                            </button>
                          </div>
                        </motion.article>
                    ))}
              </div>
            ) : (
              <div className="rounded-3xl border-2 border-dashed border-gray-200 dark:border-gray-800 py-12 text-center">
                <p className="font-bold text-gray-500 dark:text-gray-400">Todavia no tenes viajes guardados.</p>
                <p className="mt-1 text-sm font-medium text-gray-400 dark:text-gray-500">Calcula una ruta para verla aca.</p>
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
