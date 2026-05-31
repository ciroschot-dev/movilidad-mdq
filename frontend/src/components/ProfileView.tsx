import { useState, type FormEvent } from 'react';
import { motion } from 'framer-motion';
import { User, Mail, LockKeyhole, Save, ArrowLeft } from 'lucide-react';

interface AuthSession {
  id: number;
  username: string;
  email: string;
  token: string;
}

interface ProfileViewProps {
  session: AuthSession;
  onUpdate: (newSession: AuthSession) => void;
  onBack: () => void;
  apiUrl: string;
}

export default function ProfileView({ session, onUpdate, onBack, apiUrl }: ProfileViewProps) {
  const [form, setForm] = useState({
    username: session.username,
    email: session.email,
    password: '',
  });
  const [loading, setLoading] = useState(false);
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
          className="flex h-11 w-11 items-center justify-center rounded-full bg-white text-gray-500 shadow-sm transition-all hover:text-gray-900"
          title="Volver"
        >
          <ArrowLeft size={20} />
        </button>
        <div>
          <h2 className="text-2xl font-black text-gray-900 tracking-tight">Editar Perfil</h2>
          <p className="text-sm font-medium text-gray-500">Actualiza tu información personal</p>
        </div>
      </div>

      <div className="rounded-3xl bg-white p-6 shadow-xl shadow-gray-200/50">
        <form onSubmit={handleSubmit} className="space-y-4">
          <label className="block space-y-2">
            <span className="text-xs font-bold uppercase tracking-widest text-gray-400 px-1">Usuario</span>
            <div className="relative">
              <User className="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" size={19} />
              <input
                value={form.username}
                onChange={(e) => setForm({ ...form, username: e.target.value })}
                className="w-full rounded-2xl bg-gray-50 py-4 pl-12 pr-4 text-gray-900 outline-none transition-all focus:ring-2 focus:ring-black"
                placeholder="Nuevo usuario"
                required
              />
            </div>
          </label>

          <label className="block space-y-2">
            <span className="text-xs font-bold uppercase tracking-widest text-gray-400 px-1">Email</span>
            <div className="relative">
              <Mail className="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" size={19} />
              <input
                value={form.email}
                onChange={(e) => setForm({ ...form, email: e.target.value })}
                className="w-full rounded-2xl bg-gray-50 py-4 pl-12 pr-4 text-gray-900 outline-none transition-all focus:ring-2 focus:ring-black"
                placeholder="Nuevo email"
                type="email"
                required
              />
            </div>
          </label>

          <label className="block space-y-2">
            <span className="text-xs font-bold uppercase tracking-widest text-gray-400 px-1">Nueva Contraseña (opcional)</span>
            <div className="relative">
              <LockKeyhole className="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" size={19} />
              <input
                value={form.password}
                onChange={(e) => setForm({ ...form, password: e.target.value })}
                className="w-full rounded-2xl bg-gray-50 py-4 pl-12 pr-4 text-gray-900 outline-none transition-all focus:ring-2 focus:ring-black"
                placeholder="Dejar en blanco para no cambiar"
                type="password"
              />
            </div>
          </label>

          {error && (
            <div className="rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-semibold text-red-700">
              {error}
            </div>
          )}

          {success && (
            <div className="rounded-2xl border border-green-200 bg-green-50 px-4 py-3 text-sm font-semibold text-green-700">
              ¡Perfil actualizado con éxito!
            </div>
          )}

          <button
            type="submit"
            disabled={loading}
            className="flex w-full items-center justify-center gap-2 rounded-2xl bg-black py-4 text-lg font-black text-white shadow-lg transition-all hover:bg-gray-800 disabled:bg-gray-400"
          >
            {loading ? 'Guardando...' : (
              <>
                <Save size={20} /> GUARDAR CAMBIOS
              </>
            )}
          </button>
        </form>
      </div>
    </motion.section>
  );
}
