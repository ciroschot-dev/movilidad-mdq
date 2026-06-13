import React, { useState, useEffect, useCallback } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { 
  ArrowLeft, 
  ClipboardList, 
  Search, 
  User, 
  MapPin, 
  Navigation, 
  Calendar, 
  Star, 
  ChevronLeft, 
  ChevronRight,
  Filter,
  X,
  Loader2,
  Car,
  Smartphone
} from 'lucide-react';

interface ViajeAudit {
  id: number;
  origen: string;
  destino: string;
  distanciaEnMetros: number;
  tiempoEstimadoMin: number;
  precioTaxi: number;
  precioUberMin: number;
  precioUberMax: number;
  precioDidiMin: number;
  precioDidiMax: number;
  tipoElegido: string | null;
  fechaHora: string;
  favorito: boolean;
  username: string;
}

interface PageResponse {
  content: ViajeAudit[];
  totalPages: number;
  totalElements: number;
  number: number;
  size: number;
}

interface AuditViewProps {
  session: {
    token: string;
  };
  onBack: () => void;
  apiUrl: string;
}

const formatPrecio = (precio: number) =>
  new Intl.NumberFormat('es-AR', {
    style: 'currency',
    currency: 'ARS',
    maximumFractionDigits: 0,
  }).format(precio);

const formatFecha = (fechaHora: string) =>
  new Intl.DateTimeFormat('es-AR', {
    dateStyle: 'short',
    timeStyle: 'short',
  }).format(new Date(fechaHora));

const AuditView: React.FC<AuditViewProps> = ({ session, onBack, apiUrl }) => {
  const [data, setData] = useState<PageResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  // Filtros
  const [filters, setFilters] = useState({
    username: '',
    origen: '',
    destino: '',
    desde: '',
    hasta: '',
    favorito: null as boolean | null,
    page: 0
  });

  const fetchAuditData = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const params = new URLSearchParams();
      if (filters.username) params.append('username', filters.username);
      if (filters.origen) params.append('origen', filters.origen);
      if (filters.destino) params.append('destino', filters.destino);
      if (filters.desde) params.append('desde', `${filters.desde}T00:00:00`);
      if (filters.hasta) params.append('hasta', `${filters.hasta}T23:59:59`);
      if (filters.favorito !== null) params.append('favorito', filters.favorito.toString());
      params.append('page', filters.page.toString());
      params.append('size', '10');
      params.append('sort', 'fechaHora,desc');

      const response = await fetch(`${apiUrl}/viajes/admin/auditoria?${params.toString()}`, {
        headers: {
          Authorization: `Bearer ${session.token}`,
        },
      });

      if (!response.ok) throw new Error('Error al cargar datos de auditoría');
      const result = await response.json();
      setData(result);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error de conexión');
    } finally {
      setLoading(false);
    }
  }, [apiUrl, filters, session.token]);

  useEffect(() => {
    const timer = setTimeout(() => {
      fetchAuditData();
    }, 400); // Debounce
    return () => clearTimeout(timer);
  }, [fetchAuditData]);

  const handleFilterChange = (key: string, value: any) => {
    setFilters(prev => ({ ...prev, [key]: value, page: 0 }));
  };

  const clearFilters = () => {
    setFilters({
      username: '',
      origen: '',
      destino: '',
      desde: '',
      hasta: '',
      favorito: null,
      page: 0
    });
  };

  return (
    <motion.section
      initial={{ opacity: 0, x: 20 }}
      animate={{ opacity: 1, x: 0 }}
      className="space-y-6"
    >
      <div className="flex items-center justify-between">
        <button
          onClick={onBack}
          className="flex items-center gap-2 text-sm font-bold text-gray-500 dark:text-gray-400 hover:text-black dark:hover:text-white transition-colors"
        >
          <ArrowLeft size={16} /> Volver
        </button>
        <div className="flex items-center gap-2 px-3 py-1 bg-indigo-50 dark:bg-indigo-900/20 text-indigo-600 dark:text-indigo-400 rounded-full text-xs font-black uppercase tracking-widest">
          <ClipboardList size={12} /> Auditoría Global
        </div>
      </div>

      {/* Filtros */}
      <div className="bg-white dark:bg-gray-900 rounded-3xl p-6 shadow-xl shadow-gray-200/50 dark:shadow-black/40 border border-transparent dark:border-gray-800">
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-xl font-black text-gray-900 dark:text-white flex items-center gap-2">
            <Filter size={20} className="text-indigo-500" /> Filtros de Auditoría
          </h2>
          <button 
            onClick={clearFilters}
            className="text-xs font-bold text-gray-400 hover:text-red-500 flex items-center gap-1 transition-colors"
          >
            <X size={14} /> Limpiar
          </button>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
          <div className="space-y-1">
            <label className="text-[10px] font-black uppercase tracking-widest text-gray-400 ml-1">Usuario</label>
            <div className="relative">
              <User className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={14} />
              <input
                type="text"
                placeholder="Username..."
                value={filters.username}
                onChange={(e) => handleFilterChange('username', e.target.value)}
                className="w-full rounded-xl bg-gray-50 dark:bg-gray-800 py-2.5 pl-9 pr-3 text-xs font-bold outline-none focus:ring-2 focus:ring-indigo-500"
              />
            </div>
          </div>

          <div className="space-y-1">
            <label className="text-[10px] font-black uppercase tracking-widest text-gray-400 ml-1">Origen</label>
            <div className="relative">
              <MapPin className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={14} />
              <input
                type="text"
                placeholder="Dirección de origen..."
                value={filters.origen}
                onChange={(e) => handleFilterChange('origen', e.target.value)}
                className="w-full rounded-xl bg-gray-50 dark:bg-gray-800 py-2.5 pl-9 pr-3 text-xs font-bold outline-none focus:ring-2 focus:ring-indigo-500"
              />
            </div>
          </div>

          <div className="space-y-1">
            <label className="text-[10px] font-black uppercase tracking-widest text-gray-400 ml-1">Destino</label>
            <div className="relative">
              <Navigation className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={14} />
              <input
                type="text"
                placeholder="Dirección de destino..."
                value={filters.destino}
                onChange={(e) => handleFilterChange('destino', e.target.value)}
                className="w-full rounded-xl bg-gray-50 dark:bg-gray-800 py-2.5 pl-9 pr-3 text-xs font-bold outline-none focus:ring-2 focus:ring-indigo-500"
              />
            </div>
          </div>

          <div className="space-y-1">
            <label className="text-[10px] font-black uppercase tracking-widest text-gray-400 ml-1">Desde</label>
            <div className="relative">
              <Calendar className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={14} />
              <input
                type="date"
                value={filters.desde}
                onChange={(e) => handleFilterChange('desde', e.target.value)}
                className="w-full rounded-xl bg-gray-50 dark:bg-gray-800 py-2.5 pl-9 pr-3 text-xs font-bold outline-none focus:ring-2 focus:ring-indigo-500"
              />
            </div>
          </div>

          <div className="space-y-1">
            <label className="text-[10px] font-black uppercase tracking-widest text-gray-400 ml-1">Hasta</label>
            <div className="relative">
              <Calendar className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={14} />
              <input
                type="date"
                value={filters.hasta}
                onChange={(e) => handleFilterChange('hasta', e.target.value)}
                className="w-full rounded-xl bg-gray-50 dark:bg-gray-800 py-2.5 pl-9 pr-3 text-xs font-bold outline-none focus:ring-2 focus:ring-indigo-500"
              />
            </div>
          </div>

          <div className="space-y-1">
            <label className="text-[10px] font-black uppercase tracking-widest text-gray-400 ml-1">Favoritos</label>
            <div className="flex gap-2 h-[38px]">
              <button
                onClick={() => handleFilterChange('favorito', filters.favorito === true ? null : true)}
                className={`flex-1 rounded-xl text-[10px] font-black uppercase tracking-widest transition-all border ${
                  filters.favorito === true 
                  ? 'bg-yellow-500 border-yellow-500 text-white' 
                  : 'bg-gray-50 dark:bg-gray-800 border-gray-100 dark:border-gray-800 text-gray-400'
                }`}
              >
                <Star size={14} className="inline mr-1" fill={filters.favorito === true ? "currentColor" : "none"} /> Sí
              </button>
              <button
                onClick={() => handleFilterChange('favorito', filters.favorito === false ? null : false)}
                className={`flex-1 rounded-xl text-[10px] font-black uppercase tracking-widest transition-all border ${
                  filters.favorito === false 
                  ? 'bg-gray-900 dark:bg-white border-gray-900 dark:border-white text-white dark:text-black' 
                  : 'bg-gray-50 dark:bg-gray-800 border-gray-100 dark:border-gray-800 text-gray-400'
                }`}
              >
                No
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Tabla de Resultados */}
      <div className="bg-white dark:bg-gray-900 rounded-3xl shadow-xl shadow-gray-200/50 dark:shadow-black/40 border border-transparent dark:border-gray-800 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-gray-50/50 dark:bg-gray-800/50 border-b border-gray-100 dark:border-gray-800">
                <th className="px-6 py-4 text-[10px] font-black uppercase tracking-widest text-gray-400">Usuario</th>
                <th className="px-6 py-4 text-[10px] font-black uppercase tracking-widest text-gray-400">Ruta</th>
                <th className="px-6 py-4 text-[10px] font-black uppercase tracking-widest text-gray-400">Fecha</th>
                <th className="px-6 py-4 text-[10px] font-black uppercase tracking-widest text-gray-400">Detalles</th>
                <th className="px-6 py-4 text-[10px] font-black uppercase tracking-widest text-gray-400">Elección</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50 dark:divide-gray-800">
              {loading && !data ? (
                <tr>
                  <td colSpan={5} className="px-6 py-12 text-center">
                    <Loader2 className="animate-spin inline-block text-indigo-500 mb-2" size={32} />
                    <p className="text-sm font-bold text-gray-400">Cargando historial global...</p>
                  </td>
                </tr>
              ) : error ? (
                <tr>
                  <td colSpan={5} className="px-6 py-12 text-center text-red-500 font-bold">
                    {error}
                  </td>
                </tr>
              ) : data?.content.length === 0 ? (
                <tr>
                  <td colSpan={5} className="px-6 py-12 text-center">
                    <p className="text-sm font-bold text-gray-400">No se encontraron registros</p>
                  </td>
                </tr>
              ) : (
                data?.content.map((viaje) => (
                  <tr key={viaje.id} className="hover:bg-gray-50/50 dark:hover:bg-gray-800/30 transition-colors">
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-2">
                        <div className="w-7 h-7 rounded-full bg-indigo-100 dark:bg-indigo-900/40 text-indigo-600 dark:text-indigo-400 flex items-center justify-center text-[10px] font-black">
                          {viaje.username?.substring(0, 2).toUpperCase()}
                        </div>
                        <span className="text-xs font-black text-gray-900 dark:text-gray-100">{viaje.username}</span>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <div className="space-y-1 max-w-xs">
                        <div className="flex items-center gap-2 text-[10px] font-bold text-gray-700 dark:text-gray-300">
                          <MapPin size={10} className="text-gray-400 shrink-0" />
                          <span className="truncate">{viaje.origen}</span>
                        </div>
                        <div className="flex items-center gap-2 text-[10px] font-bold text-gray-700 dark:text-gray-300">
                          <Navigation size={10} className="text-gray-400 shrink-0" />
                          <span className="truncate">{viaje.destino}</span>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <span className="text-[10px] font-bold text-gray-500">{formatFecha(viaje.fechaHora)}</span>
                    </td>
                    <td className="px-6 py-4">
                      <div className="space-y-0.5">
                        <p className="text-[10px] font-bold text-gray-500">{(viaje.distanciaEnMetros / 1000).toFixed(1)} km · {viaje.tiempoEstimadoMin} min</p>
                        <p className="text-[10px] font-black text-gray-900 dark:text-gray-100">Taxi: {formatPrecio(viaje.precioTaxi)}</p>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-2">
                        {viaje.tipoElegido ? (
                          <span className={`px-2 py-0.5 rounded text-[9px] font-black uppercase tracking-tighter flex items-center gap-1 ${
                            viaje.tipoElegido === 'TAXI' ? 'bg-yellow-100 text-yellow-700' :
                            viaje.tipoElegido === 'UBER' ? 'bg-black text-white' : 'bg-orange-100 text-orange-700'
                          }`}>
                            {viaje.tipoElegido === 'TAXI' ? <Car size={8} /> : <Smartphone size={8} />}
                            {viaje.tipoElegido}
                          </span>
                        ) : (
                          <span className="text-[9px] font-bold text-gray-300 italic">Sólo consulta</span>
                        )}
                        {viaje.favorito && <Star size={12} className="text-yellow-500" fill="currentColor" />}
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Paginación */}
        {data && data.totalPages > 1 && (
          <div className="px-6 py-4 bg-gray-50/50 dark:bg-gray-800/50 border-t border-gray-100 dark:border-gray-800 flex items-center justify-between">
            <span className="text-[10px] font-bold text-gray-500 uppercase tracking-widest">
              Página {data.number + 1} de {data.totalPages}
            </span>
            <div className="flex gap-2">
              <button
                disabled={data.number === 0 || loading}
                onClick={() => handleFilterChange('page', data.number - 1)}
                className="p-2 rounded-lg bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-700 text-gray-500 disabled:opacity-30 transition-all hover:bg-gray-50"
              >
                <ChevronLeft size={16} />
              </button>
              <button
                disabled={data.number === data.totalPages - 1 || loading}
                onClick={() => handleFilterChange('page', data.number + 1)}
                className="p-2 rounded-lg bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-700 text-gray-500 disabled:opacity-30 transition-all hover:bg-gray-50"
              >
                <ChevronRight size={16} />
              </button>
            </div>
          </div>
        )}
      </div>

      <div className="text-center pb-4">
        <p className="text-[10px] font-bold text-gray-400 uppercase tracking-[0.2em]">
          Mostrando {data?.totalElements || 0} registros en total
        </p>
      </div>
    </motion.section>
  );
};

export default AuditView;
