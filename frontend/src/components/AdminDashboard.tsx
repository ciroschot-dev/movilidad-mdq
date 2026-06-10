import React, { useState } from 'react';
import { motion } from 'framer-motion';
import { Save, AlertCircle, CheckCircle2, ArrowLeft, Settings, Search, UserMinus, User } from 'lucide-react';

interface AdminDashboardProps {
  session: {
    token: string;
  };
  onBack: () => void;
  apiUrl: string;
}

interface UsuarioEncontrado {
  id: number;
  username: string;
  email: string;
  role: string;
}

const AdminDashboard: React.FC<AdminDashboardProps> = ({ session, onBack, apiUrl }) => {
  const [formData, setFormData] = useState({
    precioBase: '',
    precioPorKm: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  // Estados para búsqueda de usuario
  const [searchQuery, setSearchQuery] = useState('');
  const [usuarioEncontrado, setUsuarioEncontrado] = useState<UsuarioEncontrado | null>(null);
  const [searchLoading, setSearchLoading] = useState(false);
  const [searchError, setSearchError] = useState<string | null>(null);
  const [deleteLoading, setDeleteLoading] = useState(false);

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!searchQuery.trim()) return;

    setSearchLoading(true);
    setSearchError(null);
    setUsuarioEncontrado(null);

    try {
      const response = await fetch(`${apiUrl}/usuarios/buscar?query=${encodeURIComponent(searchQuery)}`, {
        headers: {
          Authorization: `Bearer ${session.token}`,
        },
      });

      if (response.status === 404) {
        throw new Error('Usuario no encontrado');
      }

      if (!response.ok) {
        throw new Error('Error al buscar usuario');
      }

      const data = await response.json();
      setUsuarioEncontrado(data);
    } catch (err) {
      setSearchError(err instanceof Error ? err.message : 'Error de conexión');
    } finally {
      setSearchLoading(false);
    }
  };

  const handleDeleteUser = async () => {
    if (!usuarioEncontrado) return;

    const mensaje = `¿Estás seguro de que quieres eliminar permanentemente la cuenta de "${usuarioEncontrado.username}"? Esta acción no se puede deshacer.`;
    if (!window.confirm(mensaje)) return;

    setDeleteLoading(true);
    try {
      const response = await fetch(`${apiUrl}/usuarios/${usuarioEncontrado.id}`, {
        method: 'DELETE',
        headers: {
          Authorization: `Bearer ${session.token}`,
        },
      });

      if (!response.ok) {
        throw new Error('No se pudo eliminar el usuario');
      }

      alert('Usuario eliminado correctamente');
      setUsuarioEncontrado(null);
      setSearchQuery('');
    } catch (err) {
      alert(err instanceof Error ? err.message : 'Error al eliminar');
    } finally {
      setDeleteLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setSuccess(false);

    try {
      const response = await fetch(`${apiUrl}/admin/tarifas/taxi`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${session.token}`,
        },
        body: JSON.stringify({
          precioBase: parseFloat(formData.precioBase),
          precioPorKm: parseFloat(formData.precioPorKm),
        }),
      });

      if (!response.ok) {
        throw new Error('No se pudo actualizar la tarifa. Verifica tus permisos.');
      }

      setSuccess(true);
      setTimeout(() => setSuccess(false), 3000);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al conectar con el servidor');
    } finally {
      setLoading(false);
    }
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
        <div className="flex items-center gap-2 px-3 py-1 bg-purple-50 dark:bg-purple-900/20 text-purple-600 dark:text-purple-400 rounded-full text-xs font-black uppercase tracking-widest">
          <Settings size={12} /> Panel Admin
        </div>
      </div>

      <div className="bg-white dark:bg-gray-900 rounded-3xl p-6 shadow-xl shadow-gray-200/50 dark:shadow-black/40 border border-transparent dark:border-gray-800">
        <h2 className="text-xl font-black text-gray-900 dark:text-white mb-2">Configuración de Tarifa</h2>
        <p className="text-sm font-medium text-gray-500 dark:text-gray-400 mb-6">
          Ajusta los valores base del Taxi para toda la plataforma.
        </p>

        <form onSubmit={handleSubmit} className="space-y-5">
          <div className="space-y-2">
            <label className="text-sm font-bold text-gray-700 dark:text-gray-300 ml-1">Precio Base (Bajada de bandera)</label>
            <div className="relative">
              <span className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 dark:text-gray-500 font-bold">$</span>
              <input
                type="number"
                step="0.01"
                required
                value={formData.precioBase}
                onChange={(e) => setFormData({ ...formData, precioBase: e.target.value })}
                className="w-full rounded-2xl bg-gray-50 dark:bg-gray-800 py-4 pl-8 pr-4 text-gray-900 dark:text-gray-100 font-bold outline-none transition-all focus:ring-2 focus:ring-black dark:focus:ring-white"
                placeholder="Ej: 2250"
              />
            </div>
          </div>

          <div className="space-y-2">
            <label className="text-sm font-bold text-gray-700 dark:text-gray-300 ml-1">Precio por Kilómetro</label>
            <div className="relative">
              <span className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 dark:text-gray-500 font-bold">$</span>
              <input
                type="number"
                step="0.01"
                required
                value={formData.precioPorKm}
                onChange={(e) => setFormData({ ...formData, precioPorKm: e.target.value })}
                className="w-full rounded-2xl bg-gray-50 dark:bg-gray-800 py-4 pl-8 pr-4 text-gray-900 dark:text-gray-100 font-bold outline-none transition-all focus:ring-2 focus:ring-black dark:focus:ring-white"
                placeholder="Ej: 937.50"
              />
            </div>
          </div>

          {error && (
            <div className="flex items-center gap-2 rounded-2xl border border-red-200 dark:border-red-900/30 bg-red-50 dark:bg-red-900/20 px-4 py-3 text-sm font-semibold text-red-700 dark:text-red-400">
              <AlertCircle size={18} />
              {error}
            </div>
          )}

          {success && (
            <div className="flex items-center gap-2 rounded-2xl border border-green-200 dark:border-green-900/30 bg-green-50 dark:bg-green-900/20 px-4 py-3 text-sm font-semibold text-green-700 dark:text-green-400">
              <CheckCircle2 size={18} />
              ¡Tarifa actualizada correctamente!
            </div>
          )}

          <button
            type="submit"
            disabled={loading}
            className="w-full flex items-center justify-center gap-2 rounded-2xl bg-black dark:bg-white py-4 text-lg font-black text-white dark:text-black shadow-lg transition-all hover:bg-gray-800 dark:hover:bg-gray-200 disabled:bg-gray-400"
          >
            <Save size={20} />
            {loading ? 'Guardando...' : 'GUARDAR CAMBIOS'}
          </button>
        </form>
      </div>

      {/* Gestión de Usuarios */}
      <div className="bg-white dark:bg-gray-900 rounded-3xl p-6 shadow-xl shadow-gray-200/50 dark:shadow-black/40 border border-transparent dark:border-gray-800">
        <h2 className="text-xl font-black text-gray-900 dark:text-white mb-2">Gestión de Usuarios</h2>
        <p className="text-sm font-medium text-gray-500 dark:text-gray-400 mb-6">
          Busca y elimina cuentas de usuarios que infrinjan las normas.
        </p>

        <form onSubmit={handleSearch} className="space-y-4">
          <div className="relative">
            <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" size={18} />
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Username o Email del usuario..."
              className="w-full rounded-2xl bg-gray-50 dark:bg-gray-800 py-4 pl-12 pr-4 text-gray-900 dark:text-gray-100 font-bold outline-none transition-all focus:ring-2 focus:ring-black dark:focus:ring-white"
            />
            <button
              type="submit"
              disabled={searchLoading || !searchQuery.trim()}
              className="absolute right-2 top-1/2 -translate-y-1/2 bg-black dark:bg-white text-white dark:text-black px-4 py-2 rounded-xl text-xs font-black uppercase tracking-wider disabled:bg-gray-400"
            >
              {searchLoading ? '...' : 'BUSCAR'}
            </button>
          </div>
        </form>

        {searchError && (
          <div className="mt-4 flex items-center gap-2 text-sm font-bold text-red-500">
            <AlertCircle size={16} /> {searchError}
          </div>
        )}

        {usuarioEncontrado && (
          <motion.div
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            className="mt-6 p-4 rounded-2xl border-2 border-gray-100 dark:border-gray-800 bg-gray-50/50 dark:bg-gray-800/30"
          >
            <div className="flex flex-col sm:flex-row items-center justify-between gap-4">
              <div className="flex items-center gap-3 w-full sm:w-auto">
                <div className="flex-shrink-0 w-10 h-10 rounded-full bg-black dark:bg-white flex items-center justify-center text-white dark:text-black">
                  <User size={20} />
                </div>
                <div className="min-w-0 flex-1">
                  <h3 className="font-black text-gray-900 dark:text-white truncate text-sm">{usuarioEncontrado.username}</h3>
                  <p className="text-[10px] font-bold text-gray-500 truncate">{usuarioEncontrado.email}</p>
                  <span className={`inline-flex mt-1 px-1.5 py-0.5 rounded text-[9px] font-black uppercase tracking-tighter ${
                    usuarioEncontrado.role === 'ADMIN' 
                      ? 'bg-purple-100 dark:bg-purple-900/40 text-purple-600 dark:text-purple-400' 
                      : 'bg-gray-200 dark:bg-gray-700 text-gray-600 dark:text-gray-400'
                  }`}>
                    {usuarioEncontrado.role}
                  </span>
                </div>
              </div>
              
              <div className="w-full sm:w-auto flex-shrink-0">
                <button
                  onClick={handleDeleteUser}
                  disabled={deleteLoading || usuarioEncontrado.role === 'ADMIN'}
                  className="w-full sm:w-auto flex items-center justify-center gap-1.5 bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 px-3 py-2 rounded-lg text-[10px] font-black uppercase tracking-widest hover:bg-red-100 dark:hover:bg-red-900/40 transition-all disabled:opacity-50 border border-red-100 dark:border-red-900/30"
                >
                  <UserMinus size={14} />
                  {deleteLoading ? '...' : 'ELIMINAR'}
                </button>
              </div>
            </div>
          </motion.div>
        )}
      </div>

      <div className="rounded-3xl bg-amber-50 dark:bg-amber-900/10 border border-amber-100 dark:border-amber-900/30 p-5">
        <h3 className="text-sm font-black text-amber-800 dark:text-amber-500 uppercase tracking-widest mb-2 flex items-center gap-2">
          <AlertCircle size={14} /> Nota importante
        </h3>
        <p className="text-sm font-medium text-amber-700 dark:text-amber-400/80 leading-relaxed">
          Recuerda que el sistema aplica automáticamente un <b>20% de recargo</b> sobre estos valores durante el horario nocturno (22:00 a 06:00).
        </p>
      </div>
    </motion.section>
  );
};

export default AdminDashboard;
