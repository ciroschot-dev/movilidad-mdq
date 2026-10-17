import React, { useState } from 'react';
import { Star, Edit2, Check, X, MapPin, Loader2 } from 'lucide-react';
import type { DireccionFavorita } from '../types';

interface FavoritesViewProps {
  favoritos: DireccionFavorita[];
  onRename: (id: number, nuevoNombre: string) => Promise<void>;
  onRemove: (id: number) => Promise<void>;
  loading?: boolean;
}

const FavoritesView: React.FC<FavoritesViewProps> = ({ favoritos, onRename, onRemove, loading }) => {
  const [editingId, setEditingId] = useState<number | null>(null);
  const [editValue, setEditValue] = useState('');
  const [actionLoading, setActionLoading] = useState<number | null>(null);

  const handleStartEdit = (fav: DireccionFavorita) => {
    setEditingId(fav.id);
    setEditValue(fav.nombre || '');
  };

  const handleCancelEdit = () => {
    setEditingId(null);
    setEditValue('');
  };

  const handleSaveEdit = async (id: number) => {
    if (!editValue.trim()) return;
    setActionLoading(id);
    try {
      await onRename(id, editValue);
      setEditingId(null);
    } catch (error) {
      console.error('Error al renombrar:', error);
    } finally {
      setActionLoading(null);
    }
  };

  const handleRemove = async (id: number) => {
    if (!window.confirm('¿Estás seguro de que quieres quitar esta dirección de favoritos?')) return;
    setActionLoading(id);
    try {
      await onRemove(id);
    } catch (error) {
      console.error('Error al eliminar:', error);
    } finally {
      setActionLoading(null);
    }
  };

  if (loading && favoritos.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-12 text-gray-500">
        <Loader2 className="animate-spin mb-4" size={32} />
        <p>Cargando tus favoritos...</p>
      </div>
    );
  }

  if (favoritos.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-12 px-6 text-center">
        <div className="bg-yellow-100 dark:bg-yellow-900/30 p-4 rounded-full text-yellow-600 dark:text-yellow-400 mb-4">
          <Star size={40} />
        </div>
        <h3 className="text-xl font-bold text-gray-900 dark:text-gray-100 mb-2">No tienes direcciones favoritas</h3>
        <p className="text-gray-600 dark:text-gray-400">
          Las direcciones que marques como favoritas en tus viajes aparecerán aquí para que puedas acceder a ellas rápidamente.
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-4 animate-in fade-in slide-in-from-bottom-4 duration-500">
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-2xl font-black text-gray-900 dark:text-white uppercase tracking-tight">Mis Direcciones</h2>
        <span className="bg-yellow-100 dark:bg-yellow-900/30 text-yellow-700 dark:text-yellow-400 text-xs font-bold px-3 py-1 rounded-full uppercase">
          {favoritos.length} Guardadas
        </span>
      </div>

      <div className="grid gap-3">
        {favoritos.map((fav) => (
          <div 
            key={fav.id} 
            className="bg-white dark:bg-gray-800 rounded-2xl p-4 shadow-sm border border-gray-100 dark:border-gray-700 flex items-center gap-4 group hover:shadow-md transition-all"
          >
            <button
              onClick={() => handleRemove(fav.id)}
              disabled={actionLoading === fav.id}
              className="flex-shrink-0 text-yellow-500 hover:text-yellow-600 dark:text-yellow-400 dark:hover:text-yellow-300 transition-colors"
              title="Quitar de favoritos"
            >
              {actionLoading === fav.id ? (
                <Loader2 size={24} className="animate-spin" />
              ) : (
                <Star size={24} fill="currentColor" />
              )}
            </button>

            <div className="flex-grow overflow-hidden">
              {editingId === fav.id ? (
                <div className="flex items-center gap-2 w-full">
                  <input
                    type="text"
                    autoFocus
                    value={editValue}
                    onChange={(e) => setEditValue(e.target.value)}
                    onKeyDown={(e) => e.key === 'Enter' && handleSaveEdit(fav.id)}
                    className="flex-grow bg-gray-50 dark:bg-gray-900 border-2 border-black dark:border-white rounded-lg px-3 py-1 font-bold text-gray-900 dark:text-white outline-none"
                    placeholder="Ej: Casa, Trabajo..."
                  />
                  <button 
                    onClick={() => handleSaveEdit(fav.id)}
                    className="p-1 text-green-600 hover:bg-green-50 dark:hover:bg-green-900/20 rounded-md"
                  >
                    <Check size={20} />
                  </button>
                  <button 
                    onClick={handleCancelEdit}
                    className="p-1 text-red-600 hover:bg-red-50 dark:hover:bg-red-900/20 rounded-md"
                  >
                    <X size={20} />
                  </button>
                </div>
              ) : (
                <div className="flex flex-col">
                  <span className="font-bold text-gray-900 dark:text-gray-100 truncate flex items-center gap-2">
                    {fav.nombre || <span className="text-gray-400 dark:text-gray-500 italic font-normal text-sm">Sin nombre</span>}
                  </span>
                  <span className="text-sm text-gray-500 dark:text-gray-400 truncate flex items-center gap-1">
                    <MapPin size={12} className="shrink-0" />
                    {fav.direccion}
                  </span>
                </div>
              )}
            </div>

            {editingId !== fav.id && (
              <button
                onClick={() => handleStartEdit(fav)}
                className="flex-shrink-0 p-2 text-gray-400 hover:text-black dark:hover:text-white hover:bg-gray-100 dark:hover:bg-gray-700 rounded-xl transition-all opacity-0 group-hover:opacity-100"
                title="Renombrar"
              >
                <Edit2 size={18} />
              </button>
            )}
          </div>
        ))}
      </div>
    </div>
  );
};

export default FavoritesView;
