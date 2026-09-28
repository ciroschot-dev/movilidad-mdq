# 🧭 Cómo trabajamos: Jira + GitHub

> Para Ciro y Tiago. La regla de oro: **todo arranca en Jira y todo lleva la clave de la tarea** (ej. `MDQ-16`).
> Con la clave, Jira muestra solo la rama, los commits y el PR adentro de la tarea. Sin la clave, no se entera.

## 🌿 Las ramas del repo

| Rama | Para qué | Quién la toca |
|---|---|---|
| `main` | 🚀 Producción. Lo que está acá es lo que usa la gente | Solo al cerrar un sprint, con PR desde `dev` |
| `dev` | 🧪 Integración. Acá se juntan las tareas terminadas | Con PR desde cada rama de tarea |
| `feat/MDQ-16-login` | 🛠️ Una tarea o historia | La persona asignada |
| `fix/MDQ-30-tildes` | 🐛 Un error | La persona asignada |

❌ Nadie hace commit directo a `main` ni a `dev`.

## 🔁 El ciclo de una tarea, paso a paso

**1. 📋 Jira: elegir la tarea**
- Abrí el tablero del sprint: https://cirschot.atlassian.net/jira/software/projects/MDQ/boards/1
- Tomá una tarea **asignada a vos** y arrastrala a **"En curso"**.
- Leé la descripción: dice qué tiene que pasar (la regla) y cómo se prueba (el escenario).

**2. 🌿 GitHub: crear la rama desde `dev`**
```bash
git checkout dev
git pull
git checkout -b feat/MDQ-16-login
```
💡 El nombre siempre es `tipo/CLAVE-descripcion-corta`. Tipos: `feat` (algo nuevo), `fix` (error), `chore` (configuración), `docs` (documentación).

**3. 💾 Commits con la clave adelante**
```bash
git commit -m "feat(MDQ-16): pantalla de login con usuario y contraseña"
```
✅ Commits chicos y seguidos. Cada uno con la clave.

**4. 📬 Pull Request a `dev`**
```bash
git push -u origin feat/MDQ-16-login
gh pr create --base dev --title "feat(MDQ-16): iniciar y cerrar sesión"
```
- ⚠️ La base es **`dev`**, nunca `main`.
- En Jira, pasá la tarea a **"En revisión"** si existe la columna (si no, dejala en "En curso").

**5. 👀 Revisión del compañero**
- El otro prueba el escenario de la tarea en su máquina y aprueba el PR (o deja comentarios).
- Con la aprobación se hace **merge a `dev`** y se borra la rama.

**6. ✅ Jira: cerrar la tarea**
- Arrastrá la tarea a **"Hecho"**.

## 🏷️ Dónde va la clave (`MDQ-16`) y qué engancha cada una

| Dónde la ponés | Ejemplo | Qué muestra Jira | ¿Hace falta? |
|---|---|---|---|
| 🌿 Nombre de la rama | `feat/MDQ-16-login` | La rama, dentro de la tarea | ✅ Sí |
| 📬 Título del PR | `feat(MDQ-16): iniciar sesión` | El PR y si ya se mergeó | ✅ Sí |
| 💾 Mensaje del commit | `feat(MDQ-16): agrego el botón` | Ese commit en la lista | 👍 Recomendado |

💡 Si la rama se llama `feat/MDQ-16-login` y hacés un commit que dice solo "agrego botón", Jira igual
muestra la rama y el PR (lo importante), pero ese commit no aparece en la lista.

⚠️ Si te olvidás la clave en la rama **y** en el PR, esa tarea queda vacía en Jira: nadie ve en qué
andás. Se arregla renombrando la rama o agregando la clave al título del PR.

## 🏁 Al cerrar el sprint (cada 2 semanas)

1. 🔀 PR de `dev` → `main` con título `release: sprint N`.
2. 🚀 Al mergear, se publica solo: el backend en Render y el front en Vercel.
3. 📱 Probar la app en producción, en el celular.
4. 📋 En Jira: **"Completar sprint"**. Lo que no se terminó pasa al sprint siguiente.

## 🐛 Si aparece un error que no estaba en Jira

1. Crear un issue tipo **Error** en Jira (título + cómo reproducirlo).
2. Seguir el mismo ciclo con una rama `fix/MDQ-XX-...`.

## ⚡ Ejemplo completo

> Tiago toma **MDQ-16 "RF02 — Iniciar y cerrar sesión"**:
> la arrastra a *En curso* → `git checkout -b feat/MDQ-16-login` desde `dev` →
> commits `feat(MDQ-16): ...` → PR a `dev` → Ciro lo prueba y aprueba → merge →
> Tiago pasa MDQ-16 a *Hecho*. En Jira, MDQ-16 muestra la rama, los 4 commits y el PR.
