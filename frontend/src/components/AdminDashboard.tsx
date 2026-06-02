import React, { useState } from 'react';
import { motion } from 'framer-motion';
import { Save, AlertCircle, CheckCircle2, ArrowLeft, Settings } from 'lucide-react';

interface AdminDashboardProps {
  session: {
    token: string;
  };
  onBack: () => void;
  apiUrl: string;
}

const AdminDashboard: React.FC<AdminDashboardProps> = ({ session, onBack, apiUrl }) => {
  const [formData, setFormData] = useState({
    precioBase: '',
    precioPorKm: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

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
          className="flex items-center gap-2 text-sm font-bold text-gray-500 hover:text-black transition-colors"
        >
          <ArrowLeft size={16} /> Volver
        </button>
        <div className="flex items-center gap-2 px-3 py-1 bg-purple-50 text-purple-600 rounded-full text-xs font-black uppercase tracking-widest">
          <Settings size={12} /> Panel Admin
        </div>
      </div>

      <div className="bg-white rounded-3xl p-6 shadow-xl shadow-gray-200/50">
        <h2 className="text-xl font-black text-gray-900 mb-2">Configuración de Tarifa</h2>
        <p className="text-sm font-medium text-gray-500 mb-6">
          Ajusta los valores base del Taxi para toda la plataforma.
        </p>

        <form onSubmit={handleSubmit} className="space-y-5">
          <div className="space-y-2">
            <label className="text-sm font-bold text-gray-700 ml-1">Precio Base (Bajada de bandera)</label>
            <div className="relative">
              <span className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 font-bold">$</span>
              <input
                type="number"
                step="0.01"
                required
                value={formData.precioBase}
                onChange={(e) => setFormData({ ...formData, precioBase: e.target.value })}
                className="w-full rounded-2xl bg-gray-50 py-4 pl-8 pr-4 text-gray-900 font-bold outline-none transition-all focus:ring-2 focus:ring-black"
                placeholder="Ej: 2250"
              />
            </div>
          </div>

          <div className="space-y-2">
            <label className="text-sm font-bold text-gray-700 ml-1">Precio por Kilómetro</label>
            <div className="relative">
              <span className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 font-bold">$</span>
              <input
                type="number"
                step="0.01"
                required
                value={formData.precioPorKm}
                onChange={(e) => setFormData({ ...formData, precioPorKm: e.target.value })}
                className="w-full rounded-2xl bg-gray-50 py-4 pl-8 pr-4 text-gray-900 font-bold outline-none transition-all focus:ring-2 focus:ring-black"
                placeholder="Ej: 937.50"
              />
            </div>
          </div>

          {error && (
            <div className="flex items-center gap-2 rounded-2xl border border-red-200 bg-red-50 p-4 text-sm font-semibold text-red-700">
              <AlertCircle size={18} />
              {error}
            </div>
          )}

          {success && (
            <div className="flex items-center gap-2 rounded-2xl border border-green-200 bg-green-50 p-4 text-sm font-semibold text-green-700">
              <CheckCircle2 size={18} />
              ¡Tarifa actualizada correctamente!
            </div>
          )}

          <button
            type="submit"
            disabled={loading}
            className="w-full flex items-center justify-center gap-2 rounded-2xl bg-black py-4 text-lg font-black text-white shadow-lg transition-all hover:bg-gray-800 disabled:bg-gray-400"
          >
            <Save size={20} />
            {loading ? 'Guardando...' : 'GUARDAR CAMBIOS'}
          </button>
        </form>
      </div>

      <div className="rounded-3xl bg-amber-50 border border-amber-100 p-5">
        <h3 className="text-sm font-black text-amber-800 uppercase tracking-widest mb-2 flex items-center gap-2">
          <AlertCircle size={14} /> Nota importante
        </h3>
        <p className="text-sm font-medium text-amber-700 leading-relaxed">
          Recuerda que el sistema aplica automáticamente un <b>20% de recargo</b> sobre estos valores durante el horario nocturno (22:00 a 06:00).
        </p>
      </div>
    </motion.section>
  );
};

export default AdminDashboard;
