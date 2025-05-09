package com.github.tymefly.eel;

/**
 * Defines the contract for an object that represents the result of an evaluated expression.
 * As EEL expressions are loosely typed, the result could be text, a number, logic (boolean) value or a date.
 * The client is free to use the most appropriate type; the Result will attempt to convert values as required.
 */
public non-sealed interface Result extends ValueReader {

}
