import React, { useEffect, useRef, useState, useCallback } from 'react';
import { MapPin, Navigation, Loader2, Star, Search } from 'lucide-react';
import MapView from './MapView';

export interface LugarSeleccionado {
  addressLine1: string;
  addressLine2: string;
  placeId: string;
  latitude: number;
  longitude: number;
}

interface DireccionFavorita {
  direccion: string;
  placeId: string;
  lat: number;
  lng: number;
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
  favoritos: DireccionFavorita[];
}

interface Prediction {
  description: string;
  placeId: string;
}

const InputForm: React.FC<InputFormProps> = ({ onCalculate, loading, onInputChange, favoritos }) => {
  const [origen, setOrigen] = useState('');
  const [destino, setDestino] = useState('');
  const [origenPlace, setOrigenPlace] = useState<LugarSeleccionado>();
  const [destinoPlace, setDestinoPlace] = useState<LugarSeleccionado>();

  const [origenSuggestions, setOrigenSuggestions] = useState<Prediction[]>([]);
  const [destinoSuggestions, setDestinoSuggestions] = useState<Prediction[]>([]);
  const [showOrigenSuggestions, setShowOrigenSuggestions] = useState(false);
  const [showDestinoSuggestions, setShowDestinoSuggestions] = useState(false);

  const autocompleteService = useRef<google.maps.places.AutocompleteService | null>(null);
  const placesService = useRef<google.maps.places.PlacesService | null>(null);
  const mapRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const initServices = async () => {
      if (!window.google) return;
      const { AutocompleteService, PlacesService } = (await google.maps.importLibrary('places')) as google.maps.PlacesLibrary;
      autocompleteService.current = new AutocompleteService();
      
      // PlacesService necesita un elemento del DOM aunque no lo usemos para mostrar el mapa
      const dummyDiv = document.createElement('div');
      placesService.current = new PlacesService(dummyDiv);
    };
    void initServices();
  }, []);

  const getPredictions = useCallback(async (input: string, setter: (p: Prediction[]) => void) => {
    if (!input || input.length < 3 || !autocompleteService.current) {
      setter([]);
      return;
    }

    try {
      const response = await autocompleteService.current.getPlacePredictions({
        input,
        componentRestrictions: { country: 'ar' },
        locationRestriction: {
          north: -37.85,
          south: -38.15,
          east: -57.45,
          west: -57.75,
        },
      });
      
      setter(response.predictions.map(p => ({
        description: p.description,
        placeId: p.place_id
      })));
    } catch (e) {
      console.error('Error fetching predictions', e);
    }
  }, []);

  const fetchPlaceDetails = (placeId: string): Promise<LugarSeleccionado> => {
    return new Promise((resolve, reject) => {
      if (!placesService.current) return reject('Service not loaded');
      
      placesService.current.getDetails({ placeId, fields: ['formatted_address', 'geometry', 'name', 'place_id'] }, (place, status) => {
        if (status === google.maps.places.PlacesServiceStatus.OK && place && place.geometry?.location) {
          resolve({
            addressLine1: place.name ?? place.formatted_address ?? '',
            addressLine2: place.formatted_address ?? '',
            placeId: place.place_id ?? '',
            latitude: place.geometry.location.lat(),
            longitude: place.geometry.location.lng(),
          });
        } else {
          reject(status);
        }
      });
    });
  };

  const handleFavoriteSelect = (fav: DireccionFavorita, type: 'origen' | 'destino') => {
    const { direccion, placeId, lat, lng } = fav;

    if (type === 'origen') {
      setOrigen(direccion);
      setShowOrigenSuggestions(false);
      
      if (placeId && lat && lng) {
          setOrigenPlace({
              addressLine1: direccion,
              addressLine2: direccion,
              placeId: placeId,
              latitude: lat,
              longitude: lng
          });
      } else {
          void handlePredictionSelect({ description: direccion, placeId: '' }, 'origen');
      }
    } else {
      setDestino(direccion);
      setShowDestinoSuggestions(false);
      
      if (placeId && lat && lng) {
          setDestinoPlace({
              addressLine1: direccion,
              addressLine2: direccion,
              placeId: placeId,
              latitude: lat,
              longitude: lng
          });
      } else {
          void handlePredictionSelect({ description: direccion, placeId: '' }, 'destino');
      }
    }
    onInputChange?.();
  };

  const handlePredictionSelect = async (prediction: Prediction, type: 'origen' | 'destino') => {
    if (type === 'origen') {
      setOrigen(prediction.description);
      setShowOrigenSuggestions(false);
    } else {
      setDestino(prediction.description);
      setShowDestinoSuggestions(false);
    }

    try {
      let placeDetails: LugarSeleccionado;
      if (prediction.placeId) {
        placeDetails = await fetchPlaceDetails(prediction.placeId);
      } else {
        // Fallback: si es un favorito guardado solo como texto, buscamos su primera coincidencia en Google
        if (!autocompleteService.current) return;
        const resp = await autocompleteService.current.getPlacePredictions({ input: prediction.description });
        if (resp.predictions.length > 0) {
          placeDetails = await fetchPlaceDetails(resp.predictions[0].place_id);
        } else return;
      }

      if (type === 'origen') {
        setOrigenPlace(placeDetails);
      } else {
        setDestinoPlace(placeDetails);
      }
      onInputChange?.();
    } catch (e) {
      console.error('Error fetching details', e);
    }
  };

  const handleInputChange = (
      type: 'origen' | 'destino',
      value: string
  ) => {
    if (type === 'origen') {
      setOrigen(value);
      setOrigenPlace(undefined);
      setShowOrigenSuggestions(true);
      void getPredictions(value, setOrigenSuggestions);
    } else {
      setDestino(value);
      setDestinoPlace(undefined);
      setShowDestinoSuggestions(true);
      void getPredictions(value, setDestinoSuggestions);
    }
    onInputChange?.();
  };

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!origen || !destino) return;
    await onCalculate(origen, destino, origenPlace, destinoPlace);
  };

  // Filtrar favoritos según lo que escribe el usuario
  const filteredFavorites = (type: 'origen' | 'destino') => {
    const input = type === 'origen' ? origen : destino;
    if (!input) return [];
    
    return favoritos.filter(f => 
      f.direccion.toLowerCase().includes(input.toLowerCase())
    ).slice(0, 5); // Mostrar máximo 5
  };

  const renderDropdown = (type: 'origen' | 'destino') => {
    const favs = filteredFavorites(type);
    const preds = type === 'origen' ? origenSuggestions : destinoSuggestions;
    const isVisible = type === 'origen' ? showOrigenSuggestions : showDestinoSuggestions;

    if (!isVisible || (favs.length === 0 && preds.length === 0)) return null;

    return (
      <div className="absolute left-0 right-0 top-full mt-2 z-50 bg-white rounded-2xl shadow-2xl border border-gray-100 overflow-hidden max-h-72 overflow-y-auto">
        {favs.map((fav, i) => (
          <button
            key={`fav-${i}`}
            type="button"
            onClick={() => handleFavoriteSelect(fav, type)}
            className="w-full flex items-center gap-3 px-5 py-4 text-left hover:bg-yellow-50 transition-colors border-b border-gray-50 last:border-0"
          >
            <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-yellow-100 text-yellow-600">
              <Star size={16} fill="currentColor" />
            </div>
            <div className="flex flex-col overflow-hidden">
              <span className="font-bold text-gray-900 truncate">
                {fav.direccion}
              </span>
              <span className="text-xs font-bold text-yellow-600 uppercase tracking-widest">Favorito</span>
            </div>
          </button>
        ))}
        {preds.map((p, i) => (
          <button
            key={`pred-${i}`}
            type="button"
            onClick={() => handlePredictionSelect(p, type)}
            className="w-full flex items-center gap-3 px-5 py-4 text-left hover:bg-gray-50 transition-colors border-b border-gray-50 last:border-0"
          >
            <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-gray-100 text-gray-400">
              <Search size={16} />
            </div>
            <span className="text-sm font-semibold text-gray-600 truncate">{p.description}</span>
          </button>
        ))}
      </div>
    );
  };

  return (
      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="relative">
          <div className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 z-10">
            <MapPin size={20} />
          </div>
          <input
              type="text"
              placeholder="¿De dónde sales?"
              className="w-full pl-12 pr-4 py-4 bg-gray-50 border-none rounded-2xl text-gray-900 focus:ring-2 focus:ring-black transition-all outline-none text-lg"
              value={origen}
              onChange={(e) => handleInputChange('origen', e.target.value)}
              onFocus={() => setShowOrigenSuggestions(true)}
              onBlur={() => setTimeout(() => setShowOrigenSuggestions(false), 200)}
              required
          />
          {renderDropdown('origen')}
        </div>

        <div className="relative">
          <div className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 z-10">
            <Navigation size={20} />
          </div>
          <input
              type="text"
              placeholder="¿A dónde vas?"
              className="w-full pl-12 pr-4 py-4 bg-gray-50 border-none rounded-2xl text-gray-900 focus:ring-2 focus:ring-black transition-all outline-none text-lg"
              value={destino}
              onChange={(e) => handleInputChange('destino', e.target.value)}
              onFocus={() => setShowDestinoSuggestions(true)}
              onBlur={() => setTimeout(() => setShowDestinoSuggestions(false), 200)}
              required
          />
          {renderDropdown('destino')}
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

