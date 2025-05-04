package com.github.tymefly.eel.evaluate;

import javax.annotation.Nonnull;

/**
 * Possible states the evaluation application be in
 */
public enum State {
    EVALUATED("Successfully evaluated EEL expression", 0),              // 0 => success
    HELP("Help page requested", 1),                                     // 1 -> 9 => terminated for expected reasons
    VERSION("Version information requested", 2),
    BAD_COMMAND_LINE("Invalid command line options passed", 10),        // 10 + => error conditions;
    EXPRESSION_FAILED("EEL expression failed to evaluate", 11),
    SCRIPT_NOT_FOUND("Script file can not be read", 12);

    private final String description;
    private final int returnCode;

    State(@Nonnull String description, int returnCode) {
        this.description = description;
        this.returnCode = returnCode;
    }

    /**
     * Returns a human-readable description of this state
     * @return a human-readable description of this state
     */
    @Nonnull
    String description() {
        return description;
    }

    /**
     * Returns the exit status of this application
     * @return the exit status of this application
     */
    int getReturnCode() {
        return returnCode;
    }
}
