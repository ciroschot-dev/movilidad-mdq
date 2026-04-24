import React, { useState } from 'react';
import { MapPin, Navigation, Loader2 } from 'lucide-react';

interface InputFormProps {
  onCalculate: (origen: string, destino: string) => void;
  onInputChange?: () => void;
}

const InputForm: React.FC<InputFormProps> = ({ onCalculate, onInputChange }) => {
  const [origen, setOrigen] = useState('');
  const [destino, setDestino] = useState('');
  const [loading, setLoading] = useState(false);

  const handleInputChange = (setter: (val: string) => void, value: string) => {
    setter(value);
    if (onInputChange) onInputChange();
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!origen || !destino) return;
    
    setLoading(true);
    // Simular carga visual del botón interno
    setTimeout(() => {
      onCalculate(origen, destino);
      setLoading(false);
    }, 1200);
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div className="relative">
        <div className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400">
          <MapPin size={20} />
        </div>
        <input
          type="text"
          placeholder="¿De dónde sales?"
          className="w-full pl-12 pr-4 py-4 bg-gray-50 border-none rounded-2xl text-gray-900 focus:ring-2 focus:ring-black transition-all outline-none text-lg"
          value={origen}
          onChange={(e) => handleInputChange(setOrigen, e.target.value)}
          required
        />
      </div>

      <div className="relative">
        <div className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400">
          <Navigation size={20} />
        </div>
        <input
          type="text"
          placeholder="¿A dónde vas?"
          className="w-full pl-12 pr-4 py-4 bg-gray-50 border-none rounded-2xl text-gray-900 focus:ring-2 focus:ring-black transition-all outline-none text-lg"
          value={destino}
          onChange={(e) => handleInputChange(setDestino, e.target.value)}
          required
        />
      </div>

      <button
        type="submit"
        disabled={loading}
        className="w-full bg-black text-white font-bold py-4 rounded-2xl text-xl shadow-lg hover:bg-gray-800 transition-all flex items-center justify-center gap-2 disabled:bg-gray-400"
      >
        {loading ? (
          <>
            <Loader2 className="animate-spin" />
            CALCULANDO...
          </>
        ) : (
          'CALCULAR'
        )}
      </button>
    </form>
  );
};

export default InputForm;
