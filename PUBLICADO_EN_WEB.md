# Publicación de MJ Struct

**[Abrir la web oficial de MJ Struct](https://mjstructofficial-hub.github.io/mj-struct-web/)**

Esta es la única dirección que debes guardar o compartir. Desde la portada, usa **Abrir MapaPro** o **Abrir Transcriptor**. Ambas páginas forman parte del mismo sitio y se actualizan en su ruta habitual cuando se publica `main`.

En GitHub puedes consultar el código y esta guía. Para usar las herramientas, entra por el enlace de la web oficial.

GitHub Pages usa **Deploy from a branch**, rama `main`, carpeta `/(root)`. La configuración de Jekyll en `_config.yml` deja fuera del sitio los archivos de desarrollo que sí forman parte del repositorio público.

## Inventario del sitio

| Archivo | Contenido |
| --- | --- |
| `index.html` | Portada y formulario de contacto |
| `legal.html` | Términos y políticas |
| `MapaPro-MJ Struct/index.html` | MapaPro NEC-15 |
| `MapaPro-MJ Struct/Mjstruct.webp` | Logo de MapaPro |
| `Cubicar de vigas/index.html` | Cubicador web |
| `Transcriptor de Audio/index.html` | Transcripción de videos públicos compatibles de TikTok y enlaces directos de Wikimedia Commons |
| `assets/img/Mjstruct.png` | Logo PNG |
| `assets/img/Mjstruct.webp` | Logo WebP |
| `assets/img/og-mj-struct.png` | Tarjeta que acompaña al enlace al compartir la portada |
| `descargas/Importador.xlsm` | Descarga pública aprobada |
| `descargas/CSI-Migration-Studio.exe` | Migrador de modelos de texto de ETABS, SAP2000 y SAFE para Windows x64 |
| `sitemap.xml` | URLs principales para buscadores |

## Compartir el sitio

Comparte la portada enlazada arriba. Ahí se puede abrir MapaPro NEC-15 y el Cubicador de Vigas, descargar el Importador de Excel para ETABS y el Migrador CSI, o solicitar una cotización. El Simulador se ofrece por consulta. El Transcriptor admite videos públicos compatibles de TikTok y enlaces directos de Wikimedia Commons de hasta siete minutos.

## Organización de las carpetas

- `assets/`, `descargas/` y las páginas de MapaPro, Cubicador y Transcriptor que aparecen en el inventario se sirven desde GitHub Pages.
- El repositorio contiene también el código Android del Cubicador y el servidor local del Transcriptor. Jekyll excluye ese código de la web.
- El motor de transcripción en línea se despliega en Render desde un repositorio privado. GitHub Pages publica la interfaz que se conecta al servicio.
- Las fuentes y la compilación del Migrador CSI se guardan en `_PRIVADO_NO_PUBLICAR/Desarrollo/`. El Simulador comercial, `_work/` y `.venv/` quedan fuera del repositorio público.

## Actualizar el sitio

1. Edita en `main` los archivos del inventario. Si incorporas un recurso web, actualiza este inventario y revisa `_config.yml`.
2. Comprueba los enlaces, los archivos preparados para el commit y las exclusiones. Usa `git status --short --ignored`, `git diff --cached --name-only` y `git check-ignore -v -- _PRIVADO_NO_PUBLICAR/README_NO_PUBLICAR.md "Simulador Complexivo/index.html"`.
3. Crea el commit y ejecuta `git push origin main`. Verifica el resultado en **Settings → Pages** del repositorio.
4. Abre la URL publicada en escritorio y móvil. Comprueba MapaPro, Cubicador, una transcripción con un enlace público autorizado y la descarga de `Importador.xlsm`.

## Web3Forms

Haz una consulta de prueba desde la portada enlazada arriba. Si el formulario usa **Restrict to Domain** de Web3Forms Pro, reemplaza `localhost` por `mjstructofficial-hub.github.io`. Ese campo acepta el dominio sin `https://` ni `/mj-struct-web/`. Configúralo después de probar el formulario.

- [Configurar la fuente de publicación en GitHub Pages](https://docs.github.com/en/pages/getting-started-with-github-pages/configuring-a-publishing-source-for-your-github-pages-site)
- [Restringir Web3Forms a un dominio](https://docs.web3forms.com/getting-started/pro-features/restrict-to-domain)
