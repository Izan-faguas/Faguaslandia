// theme.js
(function(){
  const THEME_KEY = 'theme';

  // Aplica el tema al <html> (documentElement)
  function applyTheme(theme) {
    if (!theme || theme === 'default') {
      // elimina el atributo para usar :root (fallback)
      document.documentElement.removeAttribute('data-theme');
    } else {
      document.documentElement.setAttribute('data-theme', theme);
    }
  }

  // Lee tema guardado (si existe)
  const saved = localStorage.getItem(THEME_KEY);

  // Aplica inmediatamente (antes de DOMContentLoaded si es posible)
  if (saved) {
    applyTheme(saved);
  }

  // Cuando el DOM esté listo, sincroniza el select (si existe) y añade listener
  document.addEventListener('DOMContentLoaded', () => {
    const selector = document.getElementById('themeSelector');

    // Si no hay tema guardado, intenta detectar preferencia del sistema (opcional)
    if (!saved) {
      // ejemplo: usar prefers-color-scheme si quieres
      // const prefersDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
      // if (prefersDark) applyTheme('neon'); // opcional
    }

    // Sincroniza selector si está presente
    if (selector) {
      const current = localStorage.getItem(THEME_KEY) || 'default';
      selector.value = current;

      selector.addEventListener('change', (e) => {
        const theme = e.target.value;
        if (theme === 'default') localStorage.removeItem(THEME_KEY);
        else localStorage.setItem(THEME_KEY, theme);
        applyTheme(theme);
      });
    }
  });

})();
