import { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Car, Smartphone, CreditCard } from 'lucide-react';
import { useJsApiLoader } from '@react-google-maps/api';
import InputForm from './components/InputForm';
import ResultadoCard from './components/ResultadoCard';

const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';
const GOOGLE_MAPS_API_KEY = import.meta.env.VITE_GOOGLE_MAPS_API_KEY ?? '';

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
  icon: React.ReactNode;
  url: string;
}

const LIBRARIES: ("drawing" | "geometry" | "localContext" | "visualization" | "marker")[] = ["marker"];

function App() {
  const { isLoaded } = useJsApiLoader({
    id: 'google-map-script',
    googleMapsApiKey: GOOGLE_MAPS_API_KEY,
    version: "beta",
    libraries: LIBRARIES,
  });

  const [resultados, setResultados] = useState<Opcion[] | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSelectOption = (url: string) => {
    if (!url) return;

    if (
      url.startsWith("http://") ||
      url.startsWith("https://") ||
      url.startsWith("tel:")
    ) {
      window.open(url, "_blank", "noopener,noreferrer");
    }
  };

  const formatPrecio = (precio: number) =>
    new Intl.NumberFormat('es-AR', {
      style: 'currency',
      currency: 'ARS',
      maximumFractionDigits: 0,
    }).format(precio);

  const handleCalculate = async (origen: string, destino: string) => {
    setLoading(true);
    setError(null);

    try {
      const response = await fetch(`${API_URL}/viajes/calcular`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ origen, destino }),
      });

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
          icon: <Car size={24} />
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
    } catch (error) {
      console.error("Error al calcular viaje:", error);
      setError(error instanceof Error ? error.message : 'Ocurrió un error inesperado.');
      setResultados(null);
    } finally {
      setLoading(false);
    }
  };

  if (!isLoaded) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center font-sans">
        <p className="text-gray-400 font-medium">Cargando aplicación...</p>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 flex justify-center p-4 sm:p-8 font-sans">
      <div className="w-full max-w-md">
        {/* Header */}
        <header className="mb-8 flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-black text-gray-900 tracking-tight">MovilidadMDQ</h1>
            <p className="text-gray-500 font-medium">Compara y viaja mejor</p>
          </div>
          <div className="w-12 h-12 bg-white rounded-full shadow-sm flex items-center justify-center border border-gray-100">
            <span className="text-xl">🇦🇷</span>
          </div>
        </header>

        {/* Input Form Section */}
        <section className="bg-white rounded-3xl p-6 shadow-xl shadow-gray-200/50 mb-8">
          <InputForm onCalculate={handleCalculate} loading={loading} onInputChange={() => setError(null)} />
        </section>

        {/* Results Section */}
        <div className="space-y-4">
          {error && (
            <div className="rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-semibold text-red-700">
              {error}
            </div>
          )}

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
              <motion.div
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                exit={{ opacity: 0 }}
              >
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

        {/* Footer info */}
        <footer className="mt-8 text-center">
          <p className="text-gray-400 text-sm">
            Precios estimados basados en la tarifa actual.
          </p>
        </footer>
      </div>
    </div>
  );
}

export default App;
