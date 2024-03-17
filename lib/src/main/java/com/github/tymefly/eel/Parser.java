package com.github.tymefly.eel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.Tokenizer.Mode;
import com.github.tymefly.eel.Tokenizer.Terminal;
import com.github.tymefly.eel.exception.EelSyntaxException;
import com.github.tymefly.eel.utils.BigDecimals;
import com.github.tymefly.eel.utils.StringUtils;

import static java.util.Map.entry;

/**
 * EEL Parser implementation
 */
class Parser {
    private static final BigDecimal MAX_INTEGER = BigDecimal.valueOf(Integer.MAX_VALUE);

    private static final Set<Token> FOLLOW_FULL_EXPRESSION = Set.of(
        Token.END_OF_PROGRAM,
        Token.RIGHT_BRACE);
    private static final Map<Token, Compiler.SymbolTransformation> CASE_OP = Map.ofEntries(
        entry(Token.CARET, StringUtils::upperFirst),
        entry(Token.ALL_UPPER, String::toUpperCase),
        entry(Token.COMMA, StringUtils::lowerFirst),
        entry(Token.ALL_LOWER, String::toLowerCase),
        entry(Token.TILDE, StringUtils::toggleFirst),
        entry(Token.ALL_TOGGLE, StringUtils::toggleAll));
    private static final Map<Token, CompileBinaryOp> REL_OP = Map.ofEntries(
        entry(Token.EQUAL, Compiler::equal),
        entry(Token.NOT_EQUAL, Compiler::notEqual),
        entry(Token.GREATER_THAN, Compiler::greaterThan),
        entry(Token.LESS_THAN, Compiler::lessThan),
        entry(Token.GREATER_THAN_EQUAL, Compiler::greaterThenEquals),
        entry(Token.LESS_THAN_EQUAL, Compiler::lessThanEquals),
        entry(Token.IS_BEFORE, Compiler::isBefore),
        entry(Token.IS_AFTER, Compiler::isAfter));
    private static final Map<Token, CompileBinaryOp> SHIFT_OP = Map.ofEntries(
        entry(Token.LEFT_SHIFT, Compiler::leftShift),
        entry(Token.RIGHT_SHIFT, Compiler::rightShift),
        entry(Token.LOGICAL_OR, Compiler::logicalOr),
        entry(Token.BITWISE_OR, Compiler::bitwiseOr));
    private static final Map<Token, CompileBinaryOp> ADD_OP = Map.ofEntries(
        entry(Token.PLUS, Compiler::plus),
        entry(Token.MINUS, Compiler::minus),
        entry(Token.CARET, Compiler::bitwiseXor));
    private static final Map<Token, CompileBinaryOp> MUL_OP = Map.ofEntries(
        entry(Token.MULTIPLY, Compiler::multiply),
        entry(Token.DIVIDE, Compiler::divide),
        entry(Token.DIVIDE_FLOOR, Compiler::divideFloor),
        entry(Token.DIVIDE_TRUNCATE, Compiler::divideTruncate),
        entry(Token.MODULUS, Compiler::modulus),
        entry(Token.LOGICAL_AND, Compiler::logicalAnd),
        entry(Token.BITWISE_AND, Compiler::bitwiseAnd));
    private static final Map<Token, CompileBinaryOp> POW_OP = Map.ofEntries(
        entry(Token.EXPONENTIATION, Compiler::power),
        entry(Token.CONCATENATE, Compiler::concatenate));
    private static final Map<Token, Boolean> LOGIC_CONSTANTS = Map.ofEntries(
        entry(Token.TRUE, true),
        entry(Token.FALSE, false));

    private final Tokenizer tokenizer;
    private final EelContextImpl context;
    private final Compiler compiler;
    private final Executor empty;

    private Terminal terminal;


    /**
     * Constructor
     * @param context       The EEL Context for the generated code
     * @param tokenizer     Reads the source expression represented as a sequence of {@link Terminal} symbols
     * @param compiler      The compiler that will generate the required code in the current context
     */
    Parser(@Nonnull EelContextImpl context, @Nonnull Tokenizer tokenizer, @Nonnull Compiler compiler) {
        this.tokenizer = tokenizer;
        this.context = context;
        this.compiler = compiler;
        this.empty = compiler.textConstant("");

        nextToken(Mode.INTERPOLATE);
    }


    @Nonnull
    Executor parse() {
        Executor result = fullExpression(Mode.INTERPOLATE);

        assertToken(Token.END_OF_PROGRAM);

        return result;
    }


    @Nonnull
    private Executor fullExpression(@Nonnull Mode mode) {
        Executor result = empty;

        while (!FOLLOW_FULL_EXPRESSION.contains(terminal.token())) {
            Executor interpolateElement;

            if (terminal.token() == Token.STRING) {
                interpolateElement = string();
            } else if (terminal.token() == Token.VALUE_INTERPOLATION) {
                interpolateElement = value();
            } else if (terminal.token() == Token.EXPRESSION_INTERPOLATION) {
                nextToken(Mode.EXPRESSION);

                interpolateElement = calculation();
                assertToken(Token.RIGHT_PARENTHESES);
            } else if (terminal.token() == Token.FUNCTION_INTERPOLATION) {
                interpolateElement = functionInterpolation();
            } else {                // should not happen
                interpolateElement = unexpected();
            }

            if (result == empty) {
                result = interpolateElement;
            } else {
                Executor finalResult = result;
                result = compiler.concatenate(finalResult, interpolateElement);
            }

            nextToken(mode);
        }

        return result;
    }

    @Nonnull
    private Executor string() {
        return compiler.textConstant(terminal.lexeme());
    }


    @Nonnull
    private Executor value() {
        Compiler.SymbolBuilder symbolBuilder;
        boolean takeLength;

        nextToken(Mode.EXPRESSION);

        takeLength = (terminal.token() == Token.HASH);
        if (takeLength) {
            nextToken(Mode.EXPRESSION);
        }

        assertToken(Token.IDENTIFIER);

        symbolBuilder = compiler.readSymbol(terminal.lexeme());
        nextToken(Mode.EXPRESSION);

        if (terminal.token() == Token.COLON) {
            symbolBuilder = valueSubstring(symbolBuilder);
        }

        symbolBuilder = valueCaseChange(symbolBuilder);

        if (takeLength) {
            symbolBuilder = symbolBuilder.withTransformation(Compiler.SymbolTransformation.LENGTH);
        }

        if (terminal.token() == Token.MINUS) {
            symbolBuilder = symbolBuilder.withDefault(valueDefault());
        }

        assertToken(Token.RIGHT_BRACE);

        return symbolBuilder.build();
    }

    @Nonnull
    private Compiler.SymbolBuilder valueCaseChange(@Nonnull Compiler.SymbolBuilder symbolBuilder) {
        boolean done;

        do {
            Token token = terminal.token();
            Compiler.SymbolTransformation caseOp = CASE_OP.get(token);
            done = (caseOp == null);

            if (!done) {
                symbolBuilder = symbolBuilder.withTransformation(caseOp);

                nextToken(Mode.EXPRESSION);
            }
        } while (!done);

        return symbolBuilder;
    }

    @Nonnull
    private Compiler.SymbolBuilder valueSubstring(@Nonnull Compiler.SymbolBuilder symbolBuilder) {
        int count;
        int start = readInteger();

        nextToken(Mode.EXPRESSION, Token.COLON);
        count = readInteger();

        if (((long) start + (long) count) > Integer.MAX_VALUE) {
            throw new EelSyntaxException(terminal.position(), "count is out of range");
        }

        nextToken(Mode.EXPRESSION);

        return symbolBuilder.withTransformation(t -> StringUtils.mid(t, start, count));
    }

    @Nonnull
    private Executor valueDefault() {
        nextToken(Mode.INTERPOLATE_BLOCK);

        return fullExpression(Mode.INTERPOLATE_BLOCK);
    }


    @Nonnull
    private Executor functionInterpolation() {
        nextToken(Mode.EXPRESSION, Token.IDENTIFIER);

        String name = terminal.lexeme();

        nextToken(Mode.EXPRESSION, Token.LEFT_PARENTHESES);

        return functionCall(name);
    }


    @Nonnull
    private Executor calculation() {
        Executor result = expression();

        if (terminal.token() == Token.QUESTION_MARK) {
            nextToken(Mode.EXPRESSION);

            Executor first = expression();

            assertToken(Token.COLON);
            nextToken(Mode.EXPRESSION);

            result = compiler.conditional(result, first, expression());
        }

        return result;
    }


    @Nonnull
    private Executor expression() {
        Executor result = simpleExpression();
        CompileBinaryOp relOp = REL_OP.get(terminal.token());

        while (relOp != null) {
            nextToken(Mode.EXPRESSION);
            result = relOp.apply(compiler, result, simpleExpression());
            relOp = REL_OP.get(terminal.token());
        }

        return result;
    }


    @Nonnull
    private Executor simpleExpression() {
        Executor result = term();
        CompileBinaryOp shiftOp = SHIFT_OP.get(terminal.token());

        while (shiftOp != null) {
            nextToken(Mode.EXPRESSION);
            result = shiftOp.apply(compiler, result, term());
            shiftOp = SHIFT_OP.get(terminal.token());
        }

        return result;
    }


    @Nonnull
    private Executor term() {
        Executor result = simpleTerm();
        CompileBinaryOp addOp = ADD_OP.get(terminal.token());

        while (addOp != null) {
            nextToken(Mode.EXPRESSION);
            result = addOp.apply(compiler, result, simpleTerm());
            addOp = ADD_OP.get(terminal.token());
        }

        return result;
    }


    @Nonnull
    private Executor simpleTerm() {
        Executor result = factor();
        CompileBinaryOp mulOp = MUL_OP.get(terminal.token());

        while (mulOp != null) {
            nextToken(Mode.EXPRESSION);
            result = mulOp.apply(compiler, result, factor());
            mulOp = MUL_OP.get(terminal.token());
        }

        return result;
    }


    @Nonnull
    private Executor factor() {
        Executor result = simpleFactor();
        CompileBinaryOp powOp = POW_OP.get(terminal.token());

        while (powOp != null) {
            nextToken(Mode.EXPRESSION);
            result = powOp.apply(compiler, result, simpleFactor());
            powOp = POW_OP.get(terminal.token());
        }

        return result;
    }


    // Suspend Checkstyle rule MethodLength for 70 lines:
    @Nonnull
    private Executor simpleFactor() {
        Executor result;
        boolean negate = terminal.token() == Token.MINUS;

        if (negate || (terminal.token() == Token.PLUS)) {
            nextToken(Mode.EXPRESSION);
        }

        if (terminal.token() == Token.LOGICAL_NOT) {
            nextToken(Mode.EXPRESSION);
            result = compiler.logicalNot(simpleFactor());
        } else if (terminal.token() == Token.TILDE) {                   // Bitwise not
            nextToken(Mode.EXPRESSION);
            result = compiler.bitwiseNot(simpleFactor());
        } else if (terminal.token() == Token.STRING) {
            result = compiler.textConstant(terminal.lexeme());
            nextToken(Mode.EXPRESSION);
        } else if (terminal.token() == Token.NUMBER) {
            result = compiler.numericConstant(terminal.value());
            nextToken(Mode.EXPRESSION);
        } else if (LOGIC_CONSTANTS.containsKey(terminal.token())) {
            result = compiler.logicConstant(LOGIC_CONSTANTS.get(terminal.token()));
            nextToken(Mode.EXPRESSION);
        } else if (terminal.token() == Token.VALUE_INTERPOLATION) {
            result = value();
            nextToken(Mode.EXPRESSION);
        } else if (terminal.token() == Token.IDENTIFIER) {
            result = identifier();
        } else if (terminal.token() == Token.LEFT_PARENTHESES) {
            nextToken(Mode.EXPRESSION);
            result = calculation();
            assertToken(Token.RIGHT_PARENTHESES);
            nextToken(Mode.EXPRESSION);
        } else if (terminal.token() == Token.FUNCTION_INTERPOLATION) {
            result = functionInterpolation();               // Can nest function interpolation, but it is pointless!
            nextToken(Mode.EXPRESSION, Token.RIGHT_PARENTHESES);
        } else {
            result = unexpected();
        }

        if (negate) {
            result = compiler.negate(result);
        }

        return result;
    }


    @Nonnull
    private Executor identifier() {
        Executor result;
        String name = terminal.lexeme();

        nextToken(Mode.EXPRESSION);

        if (terminal.token() == Token.LEFT_PARENTHESES) {
            result = functionCall(name);

            nextToken(Mode.EXPRESSION);
        } else if (terminal.token() == Token.QUESTION_MARK) {
            result = isDefined(name);
        } else {
            result = unexpected();
        }

        return result;
    }

    @Nonnull
    private Executor isDefined(@Nonnull String identifier) {
        nextToken(Mode.EXPRESSION);

        return compiler.isDefined(identifier);
    }

    @Nonnull
    private Executor functionCall(@Nonnull String functionName) {
        Executor result;
        List<Executor> argumentList;

        nextToken(Mode.EXPRESSION);

        argumentList = argumentList();

        assertToken(Token.RIGHT_PARENTHESES);

        result = context.getFunctionManager()
            .compileCall(functionName, context, argumentList);

        return result;
    }


    @Nonnull
    private List<Executor> argumentList() {
        List<Executor> arguments = new ArrayList<>();
        boolean more = (terminal.token() != Token.RIGHT_PARENTHESES) && (terminal.token() != Token.END_OF_PROGRAM);

        while (more) {
            arguments.add(calculation());
            more = terminal.token() == Token.COMMA;

            if (more) {
                nextToken(Mode.EXPRESSION);
            }
        }

        return arguments;
    }


    /**
     * Read the next token, ensuring it's a {@link Token#NUMBER} that holds an integer value
     * @return the integer value of the token
     */
    private int readInteger() {
        nextToken(Mode.EXPRESSION, Token.NUMBER);

        BigDecimal value = terminal.value();

        if (terminal.isFractional()) {
            throw new EelSyntaxException(terminal.position(), "'%s' has unexpected fractional part", terminal.lexeme());
        }

        if (BigDecimals.gt(value, MAX_INTEGER)) {
            throw new EelSyntaxException(terminal.position(), "'%s' is out of range", terminal.lexeme());
        }

        return value.intValue();
    }

    /**
     * Ensure the current token is the one that is {@code expected}
     * @param expected  The expected token
     */
    private void assertToken(@Nonnull Token expected) {
        if (terminal.token() != expected) {
            unexpected();
        }
    }

    /**
     * Read the next terminal from the source in to {@link #terminal}
     * @param mode      Read mode
     */
    private void nextToken(@Nonnull Mode mode) {
        terminal = tokenizer.next(mode);
    }

    /**
     * Read the next terminal from the source in to {@link #terminal} and assert that it's the {@code expected} type.
     * @param mode      Read mode
     * @param expected  The expected token
     */
    private void nextToken(@Nonnull Mode mode, @Nonnull Token expected) {
        nextToken(mode);
        assertToken(expected);
    }


    /**
     * Throw a standardised exception to indicate there was an unexpected token
     * @return nothing, but the calling methods need an Executor to make them compile
     * @throws EelSyntaxException everytime
     */
    @Nonnull
    private Executor unexpected() throws EelSyntaxException {
        if (terminal.token() == Token.END_OF_PROGRAM) {
            throw new EelSyntaxException(terminal.position(), "Unexpected end of expression");
        } else {
            throw new EelSyntaxException(terminal.position(), "'%s' was unexpected", terminal.lexeme());
        }
    }
}
