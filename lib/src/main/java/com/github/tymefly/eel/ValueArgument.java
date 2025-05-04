package com.github.tymefly.eel;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.exception.EelConvertException;

/**
 * {@link Value} adaptor for {@link Term}s that are passed to functions. These are only evaluated when requested
 * <br>
 * <b>Note:</b> Because the symbols table must be known when this object is created these objects cannot be reused
 * across invocations
 */
final class ValueArgument extends AbstractValue {
    private final Term term;
    private final SymbolsTable symbolsTable;

    private Value result;


    ValueArgument(@Nonnull Term term, @Nonnull SymbolsTable symbolsTable) {
        this.term = term;
        this.symbolsTable = symbolsTable;
    }

    @Nonnull
    @Override
    public Type getType() {
        return evaluate().getType();
    }

    @Nonnull
    @Override
    public String asText() {
        return evaluate().asText();
    }

    @Nonnull
    @Override
    public BigDecimal asNumber() throws EelConvertException {
        return evaluate().asNumber();
    }

    @Override
    public boolean asLogic() throws EelConvertException {
        return evaluate().asLogic();
    }

    @Nonnull
    @Override
    public ZonedDateTime asDate() {
        return evaluate().asDate();
    }

    @Nonnull
    @Override
    public File asFile() throws IOException {
        return FileFactory.from(asText());
    }

    @Nonnull
    @Override
    public Value evaluate(@Nonnull SymbolsTable symbols) {
        assert (symbols == symbolsTable);

        return evaluate();
    }


    @Nonnull
    private Value evaluate() {
        if (result == null) {
            result = term.evaluate(symbolsTable);
        }

        return result;
    }
}
