# Diseño "cuaderno de cálculo limpio": dónde se cambia cada token

Todo el tema vive en `shared/src/commonMain/kotlin/com/example/pentametod2/ui/tema/`. Los componentes de `ui/` no tienen colores ni medidas sueltas: los leen de aquí.

## Colores → `ui/tema/Color.kt`

| Token | Dónde |
|---|---|
| primary, onPrimary, primaryContainer, onPrimaryContainer | `EsquemaClaro` / `EsquemaOscuro` (ColorScheme de M3) |
| tertiary, tertiaryContainer, onTertiaryContainer | ídem |
| background, surface, onSurface, onSurfaceVariant | ídem |
| surfaceContainerLowest (tarjetas) | ídem |
| surfaceContainerHigh (cajas de fórmula, encabezado de tabla) | ídem |
| outline, outlineVariant | ídem |
| error, errorContainer, onErrorContainer | ídem |
| warning, warningContainer, borde/texto/botón del banner de aviso | `ExtendidosClaro` / `ExtendidosOscuro` (`ColoresExtendidos`) |
| success, successContainer | ídem |
| valorClave | ídem |
| filaAlterna, filaUltimaError | ídem |
| pistaR2 | ídem |
| chevronMenu | ídem |
| Gráfica: cuadrícula, ejes, datos, curva, predicción, serie4, serie5, errorSerie, tolerancia | `ExtendidosClaro.grafica` / `ExtendidosOscuro.grafica` (`ColoresGrafica`) |

- **Uso en el código:** `MaterialTheme.colorScheme.primary`, `MaterialTheme.extendidos.warning` y `MaterialTheme.extendidos.grafica.datos`.
- **Colores derivados:** los que no venían en el diseño llevan el comentario `// derivado` en el código.
- **Contraste:** el test `TemaTest.contrasteDeColoresDerivados` exige 4.5:1 en texto y 3:1 en bordes.

## Tipografía → `ui/tema/Tipografia.kt`

| Token | Dónde |
|---|---|
| Fuente de interfaz (IBM Plex Sans) | `fuenteInterfaz()` |
| Fuente de fórmulas y números (JetBrains Mono) | `fuenteMono()` (único punto) |
| headlineMedium 28/36·600, titleLarge 22/28·600, titleMedium 18/24·600, titleSmall 16/22·500, bodyLarge 16/24, bodyMedium 14/20, bodySmall 12/16, labelLarge 14/20·600 | `tipografia(sans)` |
| Etiqueta de sección 12/16·600 0.06em, marca "PENTAMETOD2" 0.08em, fórmula 15/24·500, tabla 13/18 tnum, valor clave 20/28·600, valor 15/22, gráfica 11 | `estilosMono(mono)` → `MaterialTheme.mono.*` |
| Texto de fórmula | `TextoFormula(...)`: todo texto de fórmula pasa por aquí |

Los archivos de fuente y su licencia están en `docs/FUENTES.md`.

## Formas → `ui/tema/Forma.kt`

| Radio | Uso | Objeto |
|---|---|---|
| 4 | campos | `Formas.campo` |
| 8 | chips, cajas de fórmula, tablas, caja informativa | `Formas.chip` |
| 12 | tile del menú, fila destacada, caja del vector a | `Formas.tile` |
| 16 | tarjetas, banners, estado vacío | `Formas.tarjeta` |
| 24 | botones, segmentado | `Formas.boton` |

`FormasMaterial` pasa los mismos radios a las Shapes de M3.

## Espaciado y medidas → `ui/tema/Espaciado.kt` (`object Dimens`)

| Medida | Valor |
|---|---|
| Escala de espaciado (`e4` … `e48`) | 4, 8, 12, 16, 20, 24, 32, 48 |
| Margen lateral, padding de tarjeta | 16 |
| Separación entre tarjetas | 12 |
| Separación entre campos / entre ecuaciones | 20 / 16 |
| Ancho máximo del contenido | 720 |
| Barra superior | 64 |
| Fila del menú | ≥ 72 (tile de 44) |
| Área táctil, botón | 48 |
| Campo de texto | 56 |
| Chip de estado / de categoría | 28 / 32 |
| Filas de tabla | 40 (encabezado 40, Σ 44, tabla de iteraciones 36), columna mínima 64 |
| Filas de resumen | 40 (destacada ≥ 52) |
| Encabezado de la explicación | ≥ 64 |
| Bordes | 1 (foco 2) |
| Íconos | 24 (18 en botones, 14 en chips, 16 en la caja informativa) |

## Íconos → `ui/tema/Iconos.kt`

- **Definición:** `Iconos.<nombre>` es un `DefIcono` con los paths SVG del diseño (viewBox 24, trazo 1.75 o 1.9 en botones, caps y joins redondeados).
- **Uso:** se dibujan con `Icono(def, descripcion, ...)`.
- **Otros dibujos:**
  - El marcador de predicción está en `ui/componentes/Resumen.kt` (`marcadorPrediccion`).
  - La ilustración del estado vacío está en `ui/resultados/MetodoScreen.kt` (`IlustracionVacia`).

## Componentes → `ui/componentes/`

| Archivo | Contenido |
|---|---|
| `Contenedores.kt` | `TarjetaContorno`, `EtiquetaSeccion`, `TituloTarjeta`, `CajaInfo`, `ContenidoCentrado` |
| `Controles.kt` | `CampoTexto` (prefijo "=" y multilínea), `BotonPrimario`, `BotonSecundario`, `BotonTexto`, `BotonTonal`, `Segmentado`, `FilaDoble` |
| `Estado.kt` | `ChipEstado`, `BannerEstado` |
| `Resumen.kt` | `FilaResumen` (regla de apilar: `debeApilar`, > 24 caracteres), `FilaDestacada`, `MarcadorPrediccion` |
| `Barra.kt` | `BarraSuperior` (64dp, atrás, ayuda, barra de progreso de 4dp) |

## Pantallas

| Pantalla | Archivo |
|---|---|
| Menú | `ui/navegacion/MenuScreen.kt` (secciones, glifos `glifoMetodo`, "Acerca de") |
| Pantalla de método (Edición / Resultado, estado vacío, ayuda) | `ui/resultados/MetodoScreen.kt` |
| Formularios | `ui/formularios/Formularios.kt` |
| Entrada comprimida | `ui/formularios/ResumenEntrada.kt` |
| Resultados (explicación, separador, banners, resumen, tablas, matrices, conclusión, grupos) | `ui/resultados/SalidaView.kt` |
| Reglas puras de los resultados | `ui/resultados/VistaSalida.kt` |
| Gráfica | `ui/grafica/GraficaView.kt` (dibujo) y `ui/grafica/Escalas.kt` (escalas, colores por rol `rolesSeries`, etiquetas) |

## Fuera de `ui/` (cambios mínimos de la Fase 4B)

| Cambio | Dónde |
|---|---|
| `ItemResumen.destacado`, `BloqueTexto.progreso`, `Serie.discontinua` | `model/Salida.kt` |
| `MetodoInfo.seccion` y las secciones del menú | `catalogo/Entradas.kt` (`object Secciones`) |
| Descripciones del menú | `catalogo/Catalogo.kt` |
| Ícono de la app | `androidApp/src/main/res/` (ver `docs/APK.md`) |
