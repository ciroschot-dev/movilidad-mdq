import { useState, type FormEvent } from 'react';
import { motion } from 'framer-motion';
import { User, Mail, LockKeyhole, Save, ArrowLeft, Trash2, AlertTriangle } from 'lucide-react';
import type { AuthSession } from '../types';

interface ProfileViewProps {
  session: AuthSession;
  onUpdate: (newSession: AuthSession) => void;
  onBack: () => void;
  onLogout: () => void;
  apiUrl: string;
}

export default function ProfileView({ session, onUpdate, onBack, onLogout, apiUrl }: ProfileViewProps) {
  const [form, setForm] = useState({
    username: session.username,
    email: session.email,
    password: '',
  });
  const [loading, setLoading] = useState(false);
  const [deleteLoading, setDeleteLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setLoading(true);
    setError(null);
    setSuccess(false);

    try {
      const response = await fetch(`${apiUrl.replace(/\/+$/, "")}/usuarios/${session.id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${session.token}`,
        },
        body: JSON.stringify({
          username: form.username,
          email: form.email,
          password: form.password || null,
        }),
      });

      if (!response.ok) {
        throw new Error('No se pudo actualizar el perfil.');
      }

      const updatedUser = await response.json();
      // Actualizamos la sesión manteniendo el token que no viene en el PUT
      const nextSession = { ...session, ...updatedUser };
      onUpdate(nextSession);
      
      // Guardar también en localStorage para que persista al recargar
      window.localStorage.setItem('movilidadmdq.auth.v1', JSON.stringify(nextSession));
      
      setSuccess(true);
      setForm((curr) => ({ ...curr, password: '' }));
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al actualizar perfil.');
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteAccount = async () => {
    const confirmed = window.confirm(
      '¿Estás seguro de que quieres eliminar tu cuenta? Esta acción es permanente y borrará todo tu historial de viajes.'
    );

    if (!confirmed) return;

    setDeleteLoading(true);
    setError(null);

    try {
      const response = await fetch(`${apiUrl.replace(/\/+$/, "")}/usuarios/${session.id}`, {
        method: 'DELETE',
        headers: {
          Authorization: `Bearer ${session.token}`,
        },
      });

      if (!response.ok) {
        throw new Error('No se pudo eliminar la cuenta.');
      }

      // Si se borró con éxito, limpiamos sesión y redirigimos
      onLogout();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al eliminar la cuenta.');
      setDeleteLoading(false);
    }
  };

  return (
    <motion.section
      initial={{ opacity: 0, x: 20 }}
      animate={{ opacity: 1, x: 0 }}
      exit={{ opacity: 0, x: -20 }}
      className="space-y-6"
    >
      <div className="flex items-center gap-4">
        <button
          onClick={onBack}
          className="flex h-11 w-11 items-center justify-center rounded-full bg-white dark:bg-gray-900 text-gray-500 dark:text-gray-400 shadow-sm transition-all hover:text-gray-900 dark:hover:text-white border border-transparent dark:border-gray-800"
          title="Volver"
        >
          <ArrowLeft size={20} />
        </button>
        <div>
          <h2 className="text-2xl font-black text-gray-900 dark:text-white tracking-tight">Editar Perfil</h2>
          <p className="text-sm font-medium text-gray-500 dark:text-gray-400">Actualiza tu información personal</p>
        </div>
      </div>

      <div className="rounded-3xl bg-white dark:bg-gray-900 p-6 shadow-xl shadow-gray-200/50 dark:shadow-black/40 border border-transparent dark:border-gray-800">
        <form onSubmit={handleSubmit} className="space-y-4">
          <label className="block space-y-2">
            <span className="text-xs font-bold uppercase tracking-widest text-gray-400 dark:text-gray-500 px-1">Usuario</span>
            <div className="relative">
              <User className="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 dark:text-gray-500" size={19} />
              <input
                value={form.username}
                onChange={(e) => setForm({ ...form, username: e.target.value })}
                className="w-full rounded-2xl bg-gray-50 dark:bg-gray-800 py-4 pl-12 pr-4 text-gray-900 dark:text-gray-100 outline-none transition-all focus:ring-2 focus:ring-black dark:focus:ring-white placeholder:text-gray-400"
                placeholder="Nuevo usuario"
                required
              />
            </div>
          </label>

          <label className="block space-y-2">
            <span className="text-xs font-bold uppercase tracking-widest text-gray-400 dark:text-gray-500 px-1">Email</span>
            <div className="relative">
              <Mail className="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 dark:text-gray-500" size={19} />
              <input
                value={form.email}
                onChange={(e) => setForm({ ...form, email: e.target.value })}
                className="w-full rounded-2xl bg-gray-50 dark:bg-gray-800 py-4 pl-12 pr-4 text-gray-900 dark:text-gray-100 outline-none transition-all focus:ring-2 focus:ring-black dark:focus:ring-white placeholder:text-gray-400"
                placeholder="Nuevo email"
                type="email"
                required
              />
            </div>
          </label>

          <label className="block space-y-2">
            <span className="text-xs font-bold uppercase tracking-widest text-gray-400 dark:text-gray-500 px-1">Nueva Contraseña (opcional)</span>
            <div className="relative">
              <LockKeyhole className="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 dark:text-gray-500" size={19} />
              <input
                value={form.password}
                onChange={(e) => setForm({ ...form, password: e.target.value })}
                className="w-full rounded-2xl bg-gray-50 dark:bg-gray-800 py-4 pl-12 pr-4 text-gray-900 dark:text-gray-100 outline-none transition-all focus:ring-2 focus:ring-black dark:focus:ring-white placeholder:text-gray-400"
                placeholder="Dejar en blanco para no cambiar"
                type="password"
              />
            </div>
          </label>

          {error && (
            <div className="rounded-2xl border border-red-200 dark:border-red-900/30 bg-red-50 dark:bg-red-900/20 px-4 py-3 text-sm font-semibold text-red-700 dark:text-red-400">
              {error}
            </div>
          )}

          {success && (
            <div className="rounded-2xl border border-green-200 dark:border-green-900/30 bg-green-50 dark:bg-green-900/20 px-4 py-3 text-sm font-semibold text-green-700 dark:text-green-400">
              ¡Perfil actualizado con éxito!
            </div>
          )}

          <button
            type="submit"
            disabled={loading}
            className="flex w-full items-center justify-center gap-2 rounded-2xl bg-black dark:bg-white py-4 text-lg font-black text-white dark:text-black shadow-lg transition-all hover:bg-gray-800 dark:hover:bg-gray-200 disabled:bg-gray-400"
          >
            {loading ? 'Guardando...' : (
              <>
                <Save size={20} /> GUARDAR CAMBIOS
              </>
            )}
          </button>
        </form>
      </div>

      {/* Sección Eliminar Cuenta */}
      <div className="rounded-3xl border border-red-100 dark:border-red-900/30 bg-red-50/50 dark:bg-red-900/10 p-6">
        <div className="mb-4 flex items-center gap-2 text-red-600 dark:text-red-400">
          <AlertTriangle size={20} />
          <h3 className="text-lg font-black tracking-tight">Eliminar cuenta</h3>
        </div>
        <p className="mb-6 text-sm font-medium text-gray-600 dark:text-gray-400">
          Una vez que elimines tu cuenta, no hay vuelta atrás. Se borrarán todos tus viajes guardados y preferencias.
        </p>
        <button
          onClick={handleDeleteAccount}
          disabled={deleteLoading}
          className="flex w-full items-center justify-center gap-2 rounded-2xl border-2 border-red-200 dark:border-red-900/50 bg-white dark:bg-gray-900 py-4 text-sm font-black text-red-600 dark:text-red-400 transition-all hover:bg-red-50 dark:hover:bg-red-900/20 hover:border-red-300 dark:hover:border-red-800 disabled:opacity-50"
        >
          {deleteLoading ? 'Eliminando...' : (
            <>
              <Trash2 size={18} /> ELIMINAR MI CUENTA PERMANENTEMENTE
            </>
          )}
        </button>
      </div>
    </motion.section>
  );
}
