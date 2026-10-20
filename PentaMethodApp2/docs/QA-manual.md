# QA manual — Pentametod2 (emulador Pixel 7) · diseño "cuaderno de cálculo limpio"

Instalar: `gradlew.bat :androidApp:installDebug`.

Las medidas están en dp y los colores son los tokens de `docs/DISENO.md`. Entre paréntesis va el valor en claro / oscuro.

> Para medir: Opciones de desarrollador → "Mostrar límites de diseño". Para el oscuro: `adb shell cmd uimode night yes`. Para fuente grande: `adb shell settings put system font_scale 1.3`.

## 0. Generales (todas las pantallas)
- [ ] No hay sombras: las tarjetas tienen borde de 1dp outlineVariant (#C3CBCD / #3F484B), fondo surfaceContainerLowest (#FFFFFF / #171D1F) y radio 16.
- [ ] El fondo de pantalla es #F8FAFA / #0F1416.
- [ ] El texto de la interfaz está en **IBM Plex Sans**, y los números y fórmulas en **JetBrains Mono**.
- [ ] Ningún texto dice "Semana".
- [ ] El contenido no queda debajo de la barra de estado ni de la barra de navegación.
- [ ] No aparece ningún "▯". Algunos subíndices y superíndices (ᵢ ₖ ⁽ᵏ⁾) se ven en la fuente del sistema, un poco distintos. Es esperado (ver `docs/FUENTES.md`).

## 1. Menú
- [ ] Arriba, "PENTAMETOD2" en mono 12/600, espaciado 0.08em, color primary (#0E5E6F / #84D2E3). A la derecha, el botón ⓘ (48dp) abre "Acerca de" con el nombre, una frase y "Versión 1.0".
- [ ] El título "Métodos numéricos" es de 28/36 en 600. Debajo, "Elige un método para empezar a calcular." en 14/20 onSurfaceVariant.
- [ ] Hay tres secciones de 16/24 en 600: **Sistemas no lineales**, **Ajuste de curvas** e **Interpolación**, con 20 de separación.
- [ ] Cada tarjeta mide ≥ 72 de alto, tiene radio 16 y borde de 1dp, y van separadas 8.
- [ ] El tile es de 44×44, radio 12, fondo primaryContainer (#CDE7EC / #004E5C) y glifo mono 13/600: G(x), J⁻¹, Pₘ, βₖ, Lₖ, f[ ].
- [ ] El título de la tarjeta es de 16/22 en 500. La descripción ocupa **una línea** con "…" y el chevron › es de 24, color #6F7B7F / #9AA5A9.

## 2. Pantalla de método · estado vacío
- [ ] La barra superior mide 64: ← (48, circular), el título en 22/28 (600) y el botón ? (48), que abre la ayuda del método.
- [ ] El chip de categoría mide 32 de alto, radio 8, borde de 1dp y texto 14/500 onSurfaceVariant.
- [ ] La tarjeta "Datos" lleva "ENTRADA" (mono 12/600, primary, mayúsculas) y el título "Datos" en 18/24.
- [ ] Los campos miden 56 de alto, radio 4, borde de 1dp outline (2dp primary con foco), label flotante y texto mono 16. La separación entre campos es 20.
- [ ] Los botones miden 48 de alto, radio 24, con ícono de 18 y van con flex 1, separados 12.
  - "Cargar ejemplo" tiene borde outline y texto primary.
  - "Calcular" tiene fondo primary y texto blanco.
- [ ] El estado vacío es una caja de borde punteado de 1dp outlineVariant, radio 16. Muestra la ilustración (ejes, 4 puntos y una curva punteada primary), el texto "Escribe tus datos y pulsa **Calcular**, o carga un ejemplo." (16/24, "Calcular" en negrita) y el pie "Aquí verás la fórmula, el resumen, la gráfica y la tabla de pasos." (12/16).

## 3. Formularios (los 4 tipos)
- [ ] **Regresión polinomial:**
  - [ ] Campos "Valores x" y "Valores y"; este último con la ayuda "Usa punto decimal y separa con comas" (12, onSurfaceVariant).
  - [ ] Fila con "Grado" (96 de ancho) y "x a predecir (opcional)".
- [ ] **Interpolación** (Lagrange y Newton):
  - [ ] Campos "Nodos x", "Valores y" y "x a evaluar".
  - [ ] Segmentado Manual | Automático: 48 de alto, radio 24, borde de 1dp; el seleccionado va en primaryContainer con un check de 16.
  - [ ] "Grado" solo aparece en Automático.
- [ ] **Regresión múltiple:**
  - [ ] Segmentado k = 1…4.
  - [ ] Cuadrícula con encabezados x₁…xₖ, y (mono 13/600) y celdas de 96 con el número alineado a la derecha.
  - [ ] ⊖ (48) quita una fila sin bajar de 3; "Agregar fila" es un botón tonal (fondo primaryContainer).
  - [ ] Campo "Valores a predecir (opcional)".
- [ ] **Sistemas** (punto fijo y Newton):
  - [ ] Tarjeta "Ecuaciones" con "F(x) = 0" en mono (en punto fijo, "G(x)") y el contador "2 de 2" (mono 12) a la derecha.
  - [ ] Cada ecuación es un campo con label "F₁(x, y)" y prefijo "=" (en punto fijo, label "G₁(x, y)" y prefijo "x =") más ⊖ de 48. Las ecuaciones van separadas 16.
  - [ ] "Agregar ecuación" es tonal; con 4 ecuaciones se deshabilita.
  - [ ] Tarjeta "Parámetros": Variables | Valores iniciales x₀, luego Tolerancia | Máx. iteraciones.
  - [ ] La línea "Error: norma infinito ‖·‖∞" va en 12/16.
  - [ ] Caja informativa: fondo surfaceContainerHigh, radio 8, ícono ⓘ de 16 y texto 12/18.

## 4. Resultado · Regresión polinomial (Cargar ejemplo → Calcular)
- [ ] Mientras calcula aparece una barra de progreso fina (4dp, primary) bajo la barra superior y "Calcular" queda deshabilitado.
- [ ] La tarjeta de datos se comprime:
  - [ ] Muestra "ENTRADA", "Datos" y el botón de texto "✎ Editar".
  - [ ] Debajo, la lista mono 14: `x 1, 2, 3, 4, 5`, `y 9, 7.5, 4.2, 3, 2.1`, `Grado 2 · x a predecir 2.5`.
  - [ ] "Editar" vuelve al formulario con lo escrito.
- [ ] **EXPLICACIÓN / Fórmula:**
  - [ ] Abierta por defecto, con un encabezado de ≥ 64. El chevron gira 180° al cerrarse (150 ms).
  - [ ] "Fórmula general" y "Con tus datos" (12/16·600) van en cajas surfaceContainerHigh de radio 8, con texto mono 15/24.
  - [ ] La línea "Sistema normal: …" va en onSurfaceVariant.
  - [ ] Las líneas largas se desplazan en horizontal.
- [ ] El separador muestra "RESULTADOS", una línea de 1dp y el chip "✓ Calculado": 28 de alto, radio 8, fondo successContainer (#DDEFE0 / #1E3A25) y texto success.
- [ ] **Resumen:**
  - [ ] Filas de ≥ 40 con separador de 1dp.
  - [ ] "Ecuación" se apila (etiqueta arriba y valor mono 15 abajo).
  - [ ] Q 0.905143, R² 0.974265, S 0.672734, n 5, Grado 2.
  - [ ] La fila **P(2.5)** va destacada: fondo tertiaryContainer (#FCE3D4 / #5C2604), radio 12, ≥ 52, marcador (anillo y punto tertiary), y el valor **5.662500** en 20/28·600 color valorClave (#9A3F0F / #FFB68F).
- [ ] **Gráfica:**
  - [ ] Título "Gráfica" y debajo "Ajuste polinomial" en 12.
  - [ ] Cuadrícula de 1dp (#E1E7E8 / #2A3235). Solo hay eje X abajo y eje Y a la izquierda (#5F6B6F / #899295).
  - [ ] Números mono 11, nombre del eje "x" en cursiva al final del eje.
  - [ ] Datos: círculos azules (#0072B2 / #56B4E9) de radio 5 con halo.
  - [ ] Curva verde (#009E73 / #3DCB9E) de 2dp.
  - [ ] Predicción: anillo de radio 11, punto de 6.5, guías punteadas a los dos ejes y la etiqueta "(2.5, 5.6625)" mono 11/600 en valorClave.
  - [ ] La leyenda va centrada: ● Datos, — P(x), ◎ Predicción.
  - [ ] Al tocar aparece el recuadro con el punto más cercano.
- [ ] **Datos y sumatorias:**
  - [ ] Título y "11 columnas" (12).
  - [ ] Caja con borde de 1dp y radio 8. El encabezado mide 40, fondo surfaceContainerHigh, mono 13/600 a la derecha.
  - [ ] La **columna "i" queda fija** al desplazar y tiene borde derecho.
  - [ ] Filas de 40 con zebra (#F5F8F8 / #1B2224).
  - [ ] La fila **Σ** mide 44, fondo primaryContainer, texto 600 y borde superior de 2dp; muestra Σxᵢ = 15 y Σxᵢ² = 55.
  - [ ] Debajo, la barra de 4dp (pista de 120) y "Desliza para ver más columnas →".
- [ ] **Sistema normal:**
  - [ ] Título "Sistema normal" con "C·a = b" en mono 16 onSurfaceVariant.
  - [ ] C, b y C⁻¹ llevan corchetes de 1.5dp con patas de 6, el nombre en cursiva y números mono 14 a la derecha. Saltan de línea si no caben.
  - [ ] "a" va aparte, en una caja surfaceContainerHigh de radio 12, con valores en 600 y los rótulos "← a₀ ← a₁ ← a₂".
- [ ] **Conclusión:**
  - [ ] Tarjeta sin borde, fondo primaryContainer, radio 16, padding 20/16.
  - [ ] Ícono ✓ en círculo, "Conclusión" y el texto 16/24 con **97.43%** en negrita.
  - [ ] Barra de R² de 6dp (pista #A6CDD5 / #1E6B7A) con "R² 0 … 1" en mono 12.
- [ ] "Copiar resultados" es un botón secundario. Al pulsarlo dice "Copiado…".

## 5. Resultado · Newton-Raphson para sistemas (ejemplo)
- [ ] La entrada comprimida muestra `F₁ x^2+y^2-4 = 0`, `F₂ x-y = 0`, `x₀ (1, 1)` y `Criterio tol 1e-8 · máx 50`.
- [ ] El resumen muestra la solución **x = 1.414214, y = 1.414214** apilada e Iteraciones 5. "Convergió" es el chip verde "✓ Convergió".
- [ ] **Gráfica de error:**
  - [ ] Subtítulo "Error por iteración · Escala logarítmica".
  - [ ] El eje Y se marca con superíndices (10⁰, 10⁻³, …).
  - [ ] La serie E(k) es roja (error), línea con puntos de radio 4.5, y lleva la etiqueta "E₅ = 1.59e-12" en rojo.
  - [ ] La tolerancia es una línea verde discontinua (5-4) con el rótulo "tolerancia 1e-8" a la derecha.
- [ ] En la tabla "Iteraciones", las filas miden 36 y la columna k queda fija.
- [ ] Hay 5 acordeones "Detalle de la iteración k", contraídos. Al abrirlos muestran "Se evalúa en x⁽⁰⁾: …" y J, F(x) y Δ con corchetes.

## 6. Punto fijo divergente (G₁ = `2x+y`, G₂ = `2y+x`, x₀ = `1,1`)
- [ ] El chip del separador dice "No convergió" (errorContainer).
- [ ] **Banner de error:**
  - [ ] Fondo errorContainer (#F9DEDC / #8C1D18), sin borde, radio 16 e ícono octágono en color error.
  - [ ] Título "El método diverge o no convergió" (16/22·600) y el mensaje del método.
  - [ ] Acciones a la derecha: **Cambiar despeje** vuelve a la edición y **Probar Newton-Raphson** abre ese método.
- [ ] En el resumen, "Error final" va en rojo y "Convergió" es el chip rojo "Divergente".
- [ ] La gráfica muestra el error creciendo hasta 10¹² y la etiqueta "E₂₆ = 1.69e12".
- [ ] La **última fila** de la tabla de iteraciones tiene fondo filaUltimaError (#FCEEEE / #3B2224) y la columna E en rojo 600 (desliza a la derecha para verla).
- [ ] Error de entrada (p. ej. la ecuación `x^2+`): chip "Con errores", banner "No se pudo calcular" con "Ecuación 1: …" y la acción "Editar datos".

## 7. Aviso de mal condicionamiento (hoja "Resolver")
x = `176,168,202,138,213,159,193,122,185,153`, y = `448,556,844,427,811,398,447,154,534,313`, Grado `2`. Antes de escribir el 2, **borra el 1 que trae el campo**.
- [ ] El chip dice "Con avisos" (warningContainer #FFEFC7, texto warning).
- [ ] **Banner de aviso:**
  - [ ] Fondo warningContainer, borde de 1dp (#E5C26B / #B08A2E), radio 16 y triángulo en color warning.
  - [ ] Título "El sistema está mal condicionado (número de condición ≈ 1.37e12)…".
  - [ ] La acción **Entendido** (#6B4600 / #F5C35B) lo oculta.
- [ ] Los resultados se ven igual debajo.

## 8. Modo oscuro (repetir 1–7)
- [ ] Todos los tokens cambian a su valor oscuro.
- [ ] Los textos se leen bien sobre las tarjetas (#171D1F).
- [ ] Los chips y banners mantienen su significado (verde, ámbar, rojo).
- [ ] La gráfica usa la paleta oscura (#56B4E9, #3DCB9E, #FFB68F, error #F2B8B5, tolerancia #9BD3A6).
- [ ] (Conocido) La pantalla de arranque del sistema es clara aunque el teléfono esté en oscuro (tema del manifiesto, ver el reporte).

## 9. Fuente grande (1.3×)
- [ ] Todo el texto crece y nada se superpone.
- [ ] "Cargar ejemplo" y "Calcular" **se apilan a ancho completo**, con Calcular arriba, sin recortar el texto.
- [ ] Las tablas ensanchan sus columnas y mantienen la columna fija y la barra de desplazamiento.
- [ ] Las etiquetas de la gráfica no se cortan.
- [ ] El título largo de la barra ("Newton-Raphson para sistemas") se recorta con "…" en una línea; es esperado.

## 10. Rotación
- [ ] Con el formulario escrito, al rotar se conserva todo.
- [ ] Con resultados, al rotar se vuelve a la vista de resultados con la entrada comprimida, recalculada en segundo plano.
- [ ] El estado de la explicación (abierta o cerrada) se conserva.
- [ ] En horizontal, el contenido queda centrado con un máximo de 720 y respeta el recorte de pantalla.
- [ ] Al volver al menú y entrar otra vez al método, se conserva su estado.
