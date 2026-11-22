package com.example.cristian_malhue_benjamin_rozas_kotlin

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Pruebas locales de validarRegistro(): lógica pura de Kotlin, sin emulador ni red.
 * La función devuelve un mensaje de error, o null cuando los datos son válidos.
 */
class RegistroValidacionTest {

    /** Datos válidos por defecto: cada prueba cambia solo el dato que quiere probar. */
    private fun validar(
        nombre: String = "Ana Perez",
        correo: String = "ana@colegio.cl",
        password: String = "Clave123!",
        confirmar: String = password
    ): String? = validarRegistro(nombre, correo, password, confirmar)

    // ------------------------------------------------------------- caso válido

    @Test
    fun datosValidosNoDevuelvenError() {
        // Arrange
        val nombre = "Ana Perez"
        val correo = "ana@colegio.cl"
        // Act
        val resultado = validarRegistro(nombre, correo, "Clave123!", "Clave123!")
        // Assert
        assertNull(resultado)
    }

    // ------------------------------------------------------------------ nombre

    @Test
    fun rechazaNombreDeUnaLetra() {
        assertEquals("El nombre debe tener al menos 2 caracteres", validar(nombre = "A"))
    }

    @Test
    fun rechazaNombreDeSoloEspacios() {
        assertEquals("El nombre debe tener al menos 2 caracteres", validar(nombre = "   "))
    }

    @Test
    fun aceptaNombreDeExactamenteDosLetras() {
        assertNull(validar(nombre = "Al"))
    }

    @Test
    fun rechazaNombreConNumeros() {
        assertEquals("El nombre solo puede tener letras", validar(nombre = "Ana2"))
    }

    @Test
    fun aceptaNombreConTildesYEspacios() {
        assertNull(validar(nombre = "María Pérez"))
    }

    // ------------------------------------------------------------------ correo

    @Test
    fun rechazaDominioNoPermitido() {
        assertEquals(
            "El correo debe terminar en @gmail.com, @duocuc.cl o @colegio.cl",
            validar(correo = "ana@hotmail.com")
        )
    }

    @Test
    fun aceptaLosTresDominiosPermitidos() {
        assertNull(validar(correo = "ana@gmail.com"))
        assertNull(validar(correo = "ana@duocuc.cl"))
        assertNull(validar(correo = "ana@colegio.cl"))
    }

    @Test
    fun rechazaCorreoSinNadaAntesDelDominio() {
        assertEquals("Ingrese su correo antes del dominio", validar(correo = "@colegio.cl"))
    }

    @Test
    fun rechazaCorreoConGuionBajo() {
        assertEquals(
            "El correo solo puede tener letras, números y puntos",
            validar(correo = "ana_perez@colegio.cl")
        )
    }

    @Test
    fun rechazaCorreoQueEmpiezaConPunto() {
        assertEquals(
            "El correo no puede empezar ni terminar con punto",
            validar(correo = ".ana@colegio.cl")
        )
    }

    @Test
    fun rechazaCorreoQueTerminaConPunto() {
        assertEquals(
            "El correo no puede empezar ni terminar con punto",
            validar(correo = "ana.@colegio.cl")
        )
    }

    @Test
    fun rechazaCorreoConDosPuntosSeguidos() {
        assertEquals(
            "El correo no puede tener dos puntos seguidos",
            validar(correo = "ana..perez@colegio.cl")
        )
    }

    @Test
    fun rechazaCorreoSoloConNumeros() {
        assertEquals(
            "El correo debe tener al menos una letra antes del dominio",
            validar(correo = "12345@colegio.cl")
        )
    }

    @Test
    fun ignoraMayusculasYEspaciosAlRededorDelCorreo() {
        assertNull(validar(correo = "  ANA@Colegio.CL  "))
    }

    // -------------------------------------------------------------- contraseña

    @Test
    fun rechazaPasswordDeSieteCaracteres() {
        assertEquals(
            "La contraseña debe tener al menos 8 caracteres",
            validar(password = "Cl1234!")
        )
    }

    @Test
    fun aceptaPasswordDeExactamenteOchoCaracteres() {
        assertNull(validar(password = "Cla1234!"))
    }

    @Test
    fun rechazaPasswordSinMayuscula() {
        assertEquals(
            "La contraseña debe tener al menos 1 mayúscula",
            validar(password = "clave123!")
        )
    }

    @Test
    fun rechazaPasswordSinNumero() {
        assertEquals(
            "La contraseña debe tener al menos 1 número",
            validar(password = "Clavecita!")
        )
    }

    @Test
    fun rechazaPasswordSinCaracterEspecial() {
        assertEquals(
            "La contraseña debe tener al menos 1 carácter especial",
            validar(password = "Clave1234")
        )
    }

    @Test
    fun elEspacioNoCuentaComoCaracterEspecial() {
        assertEquals(
            "La contraseña debe tener al menos 1 carácter especial",
            validar(password = "Clave 1234")
        )
    }

    // ------------------------------------------------------------ confirmación

    @Test
    fun rechazaConfirmacionDistinta() {
        assertEquals(
            "Las contraseñas no coinciden",
            validar(password = "Clave123!", confirmar = "Clave123?")
        )
    }
}