import { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Car, Smartphone, CreditCard } from 'lucide-react';
import InputForm from './components/InputForm';
import ResultadoCard from './components/ResultadoCard';

interface Opcion {
  tipo: string;
  precio: string;
  tiempo: string;
  color: string;
  icon: JSX.Element;
  url: string;
}

function App() {
  const [resultados, setResultados] = useState<Opcion[] | null>(null);

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

  const handleCalculate = async (origen: string, destino: string) => {
    console.log("🚀 Fetch resultados", { origen, destino });
    try {
      const response = await fetch('http://localhost:8080/viajes/calcular', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ origen, destino }),
      });

      if (!response.ok) throw new Error('Error en la petición');

      const data = await response.json();

      const mappedData: Opcion[] = data.map((item: any) => {
        const isSamePrice = item.precioMin === item.precioMax;
        const precio = isSamePrice 
          ? `${item.precioMin}` 
          : `${item.precioMin} - ${item.precioMax}`;

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
    }
  };

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
          <InputForm onCalculate={handleCalculate} />
        </section>

        {/* Results Section */}
        <div className="space-y-4">
          <AnimatePresence>
            {resultados ? (
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
