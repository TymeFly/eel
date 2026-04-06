package com.github.tymefly.eel;

/**
 * Defines the contract for an object that represents the result of an evaluated expression.
 * As EEL expressions are loosely typed, the result may be text, a number, a logical ({@code boolean}) value,
 * or a date.
 * The client may call any of the getter methods; the {@link Result} will attempt to convert values as required.
 */
public non-sealed interface Result extends ValueReader {

}