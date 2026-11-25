# Publicación de MJ Struct

GitHub Pages usa **Deploy from a branch**, rama `main`, carpeta `/(root)`. La configuración de Jekyll en `_config.yml` deja fuera del sitio los archivos de desarrollo que sí forman parte del repositorio público.

- Repositorio de publicación: [mjstructofficial-hub/mj-struct-web](https://github.com/mjstructofficial-hub/mj-struct-web).
- Dirección de GitHub Pages: [mjstructofficial-hub.github.io/mj-struct-web](https://mjstructofficial-hub.github.io/mj-struct-web/).

## Inventario del sitio

| Archivo | Contenido |
| --- | --- |
| `index.html` | Portada y formulario de contacto |
| `legal.html` | Términos y políticas |
| `MapaPro-MJ Struct/index.html` | MapaPro NEC-15 |
| `MapaPro-MJ Struct/Mjstruct.webp` | Logo de MapaPro |
| `Cubicar de vigas/index.html` | Cubicador web |
| `Transcriptor de Audio/index.html` | Demostración del Transcriptor; la transcripción requiere el servidor local |
| `assets/img/Mjstruct.png` | Logo PNG |
| `assets/img/Mjstruct.webp` | Logo WebP |
| `descargas/Importador.xlsm` | Descarga pública aprobada |

## Actualizar el sitio

1. Edita en `main` los archivos del inventario. Si incorporas un recurso web, actualiza este inventario y revisa `_config.yml`.
2. Comprueba los enlaces, los archivos preparados para el commit y las exclusiones. Usa `git status --short --ignored`, `git diff --cached --name-only` y `git check-ignore -v -- _PRIVADO_NO_PUBLICAR/README_NO_PUBLICAR.md "Simulador Complexivo/index.html"`.
3. Crea el commit y ejecuta `git push origin main`. Verifica el resultado en **Settings → Pages** del repositorio.
4. Abre la URL publicada en escritorio y móvil. Comprueba MapaPro, Cubicador, la demostración del Transcriptor y la descarga de `Importador.xlsm`.

## Web3Forms

Haz una consulta de prueba desde `https://mjstructofficial-hub.github.io/mj-struct-web/`. Si el formulario usa **Restrict to Domain** de Web3Forms Pro, reemplaza `localhost` por `mjstructofficial-hub.github.io`. Ese campo acepta el dominio sin `https://` ni `/mj-struct-web/`. Configúralo después de probar el formulario.

- [Configurar la fuente de publicación en GitHub Pages](https://docs.github.com/en/pages/getting-started-with-github-pages/configuring-a-publishing-source-for-your-github-pages-site)
- [Restringir Web3Forms a un dominio](https://docs.web3forms.com/getting-started/pro-features/restrict-to-domain)
