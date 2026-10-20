package com.numsolve.domain.model

/**
 * Categoría de formulario dinámico que requiere cada método.
 * La UI usa esta categoría para decidir qué campos mostrar.
 */
enum class InputCategory {
    INTERVAL,       // a, b            -> Bisección, Regula Falsi
    INITIAL_POINT,  // x0               -> Newton-Raphson, Punto Fijo
    TWO_POINTS      // x0, x1           -> Secante
}

enum class NumericMethod(
    val displayName: String,
    val shortLabel: String,
    val inputCategory: InputCategory,
    val formulaLatexLike: String,
) {
    BISECTION(
        displayName = "Bisección",
        shortLabel = "Bisección",
        inputCategory = InputCategory.INTERVAL,
        formulaLatexLike = "mi = (a+b)/2",
    ),
    REGULA_FALSI(
        displayName = "Regla Falsa",
        shortLabel = "Regla Falsa",
        inputCategory = InputCategory.INTERVAL,
        formulaLatexLike = "mi = b-f(b)*(a−b)/(f(a)−f(b))",
    ),
    FIXED_POINT(
        displayName = "Punto Fijo",
        shortLabel = "Punto Fijo",
        inputCategory = InputCategory.INITIAL_POINT,
        formulaLatexLike = "g(x) = x−f(x)/f'(x0) → x(i+1) = g(xi)",
    ),
    SECANT(
        displayName = "Secante",
        shortLabel = "Secante",
        inputCategory = InputCategory.TWO_POINTS,
        formulaLatexLike = "x(i+1) = xi−f(xi)*(xi−x(i-1))/(f(xi)−f(x(i-1)))",
    ),
    NEWTON_RAPHSON(
        displayName = "Newton-Raphson",
        shortLabel = "Newton-Raphson",
        inputCategory = InputCategory.INITIAL_POINT,
        formulaLatexLike = "x(i+1) = xi−f(xi)/f'(xi)",

    );

    companion object {
        val all: List<NumericMethod> = entries
    }
}