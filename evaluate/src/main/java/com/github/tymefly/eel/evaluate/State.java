package com.github.tymefly.eel.evaluate;

/**
 * Possible states the evaluation application be in
 */
public enum State {
    EVALUATED(0),
    HELP(1),
    EXPRESSION_FAILED(2),
    BAD_COMMAND_LINE(3);

    private final int returnCode;

    State(int returnCode) {
        this.returnCode = returnCode;
    }

    /**
     * Returns the exit status of this application
     * @return the exit status of this application
     */
    int getReturnCode() {
        return returnCode;
    }
}
