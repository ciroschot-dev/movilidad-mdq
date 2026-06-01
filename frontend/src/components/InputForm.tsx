import React, { useEffect, useRef } from 'react';
import { MapPin, Navigation, Loader2 } from 'lucide-react';
import MapView from './MapView';

interface InputFormProps {
  onCalculate: (
      origen: string,
      destino: string,
      origenLat?: number,
      origenLng?: number,
      destinoLat?: number,
      destinoLng?: number
  ) => Promise<void>;
  loading: boolean;
  onInputChange?: () => void;
}

const InputForm: React.FC<InputFormProps> = ({ onCalculate, loading, onInputChange }) => {
  const [origen, setOrigen] = React.useState('');
  const [destino, setDestino] = React.useState('');

  const [origenCoords, setOrigenCoords] = React.useState<{ lat: number; lng: number } | null>(null);
  const [destinoCoords, setDestinoCoords] = React.useState<{ lat: number; lng: number } | null>(null);

  const origenRef = useRef<HTMLInputElement>(null);
  const destinoRef = useRef<HTMLInputElement>(null);
  const onInputChangeRef = useRef(onInputChange);

  useEffect(() => {
    onInputChangeRef.current = onInputChange;
  }, [onInputChange]);

  useEffect(() => {
    const initAutocomplete = async () => {
      if (!window.google || !origenRef.current || !destinoRef.current) return;

      const { Autocomplete } = (await google.maps.importLibrary("places")) as google.maps.PlacesLibrary;

      const options: google.maps.places.AutocompleteOptions = {
        componentRestrictions: { country: "ar" },
        fields: ["formatted_address", "geometry"],
        bounds: {
          north: -37.85,
          south: -38.15,
          east: -57.45,
          west: -57.75,
        },
      };

      const autocompleteOrigen = new Autocomplete(origenRef.current, options);
      const autocompleteDestino = new Autocomplete(destinoRef.current, options);

      autocompleteOrigen.addListener("place_changed", () => {
        const place = autocompleteOrigen.getPlace();

        if (place.formatted_address) {
          setOrigen(place.formatted_address);

          if (place.geometry?.location) {
            setOrigenCoords({
              lat: place.geometry.location.lat(),
              lng: place.geometry.location.lng(),
            });
          }

          onInputChangeRef.current?.();
        }
      });

      autocompleteDestino.addListener("place_changed", () => {
        const place = autocompleteDestino.getPlace();

        if (place.formatted_address) {
          setDestino(place.formatted_address);

          if (place.geometry?.location) {
            setDestinoCoords({
              lat: place.geometry.location.lat(),
              lng: place.geometry.location.lng(),
            });
          }

          onInputChangeRef.current?.();
        }
      });
    };

    void initAutocomplete();
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!origen || !destino) return;

    await onCalculate(
        origen,
        destino,
        origenCoords?.lat,
        origenCoords?.lng,
        destinoCoords?.lat,
        destinoCoords?.lng
    );

  };
    console.log("Origen:", origenCoords);
    console.log("Destino:", destinoCoords);

  return (
      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="relative">
          <div className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 z-10">
            <MapPin size={20} />
          </div>
          <input
              ref={origenRef}
              type="text"
              placeholder="¿De dónde sales?"
              className="w-full pl-12 pr-4 py-4 bg-gray-50 border-none rounded-2xl text-gray-900 focus:ring-2 focus:ring-black transition-all outline-none text-lg"
              value={origen}
              onChange={(e) => {
                setOrigen(e.target.value);
                onInputChangeRef.current?.();
              }}
              required
          />
        </div>

        <div className="relative">
          <div className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 z-10">
            <Navigation size={20} />
          </div>
          <input
              ref={destinoRef}
              type="text"
              placeholder="¿A dónde vas?"
              className="w-full pl-12 pr-4 py-4 bg-gray-50 border-none rounded-2xl text-gray-900 focus:ring-2 focus:ring-black transition-all outline-none text-lg"
              value={destino}
              onChange={(e) => {
                setDestino(e.target.value);
                onInputChangeRef.current?.();
              }}
              required
          />
        </div>

        <MapView origen={origenCoords ?? undefined} destino={destinoCoords ?? undefined} />

        <button
            type="submit"
            disabled={loading}
            className="w-full bg-black text-white font-bold py-4 rounded-2xl text-xl shadow-lg hover:bg-gray-800 transition-all flex items-center justify-center gap-2 disabled:bg-gray-400 mt-2"
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