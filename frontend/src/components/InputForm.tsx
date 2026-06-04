import React, { useEffect, useRef, useState } from 'react';
import { MapPin, Navigation, Loader2 } from 'lucide-react';
import MapView from './MapView';

export interface LugarSeleccionado {
  addressLine1: string;
  addressLine2: string;
  placeId: string;
  latitude: number;
  longitude: number;
}

interface InputFormProps {
  onCalculate: (
      origen: string,
      destino: string,
      origenPlace?: LugarSeleccionado,
      destinoPlace?: LugarSeleccionado
  ) => Promise<void>;
  loading: boolean;
  onInputChange?: () => void;
}

const InputForm: React.FC<InputFormProps> = ({ onCalculate, loading, onInputChange }) => {
  const [origen, setOrigen] = useState('');
  const [destino, setDestino] = useState('');
  const [origenPlace, setOrigenPlace] = useState<LugarSeleccionado>();
  const [destinoPlace, setDestinoPlace] = useState<LugarSeleccionado>();

  const origenRef = useRef<HTMLInputElement>(null);
  const destinoRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    const initAutocomplete = async () => {
      if (!window.google || !origenRef.current || !destinoRef.current) return;

      const { Autocomplete } = (await google.maps.importLibrary('places')) as google.maps.PlacesLibrary;

      const options: google.maps.places.AutocompleteOptions = {
        componentRestrictions: { country: 'ar' },
        fields: ['formatted_address', 'geometry', 'name', 'place_id'],
        bounds: {
          north: -37.85,
          south: -38.15,
          east: -57.45,
          west: -57.75,
        },
      };

      const autocompleteOrigen = new Autocomplete(origenRef.current, options);
      const autocompleteDestino = new Autocomplete(destinoRef.current, options);

      autocompleteOrigen.addListener('place_changed', () => {
        const place = autocompleteOrigen.getPlace();

        if (!place.formatted_address || !place.geometry?.location) return;

        const selectedPlace: LugarSeleccionado = {
          addressLine1: place.name ?? place.formatted_address,
          addressLine2: place.formatted_address,
          placeId: place.place_id ?? '',
          latitude: place.geometry.location.lat(),
          longitude: place.geometry.location.lng(),
        };

        setOrigen(place.formatted_address);
        setOrigenPlace(selectedPlace);
        onInputChange?.();
      });

      autocompleteDestino.addListener('place_changed', () => {
        const place = autocompleteDestino.getPlace();

        if (!place.formatted_address || !place.geometry?.location) return;

        const selectedPlace: LugarSeleccionado = {
          addressLine1: place.name ?? place.formatted_address,
          addressLine2: place.formatted_address,
          placeId: place.place_id ?? '',
          latitude: place.geometry.location.lat(),
          longitude: place.geometry.location.lng(),
        };

        setDestino(place.formatted_address);
        setDestinoPlace(selectedPlace);
        onInputChange?.();
      });
    };

    void initAutocomplete();
  }, [onInputChange]);

  const handleInputChange = (
      setter: (value: string) => void,
      value: string,
      clearPlace: () => void
  ) => {
    setter(value);
    clearPlace();
    onInputChange?.();
  };

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!origen || !destino) return;

    await onCalculate(origen, destino, origenPlace, destinoPlace);
  };

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
              onChange={(event) => handleInputChange(setOrigen, event.target.value, () => setOrigenPlace(undefined))}
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
              onChange={(event) => handleInputChange(setDestino, event.target.value, () => setDestinoPlace(undefined))}
              required
          />
        </div>

        <MapView 
          origen={origenPlace ? { lat: origenPlace.latitude, lng: origenPlace.longitude } : undefined}
          destino={destinoPlace ? { lat: destinoPlace.latitude, lng: destinoPlace.longitude } : undefined}
        />

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