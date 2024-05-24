package com.github.tymefly.eel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.Tokenizer.Mode;
import com.github.tymefly.eel.Tokenizer.Terminal;
import com.github.tymefly.eel.exception.EelSyntaxException;
import com.github.tymefly.eel.utils.StringUtils;

import static java.util.Map.entry;

/**
 * EEL Parser implementation
 */
class Parser {
    private static final Map<Token, Compiler.SymbolTransformation> CASE_OP = Map.ofEntries(
        entry(Token.CARET, (s, t) -> StringUtils.upperFirst(t)),
        entry(Token.ALL_UPPER, (s, t) -> t.toUpperCase()),
        entry(Token.COMMA, (s, t) -> StringUtils.lowerFirst(t)),
        entry(Token.ALL_LOWER, (s, t) -> t.toLowerCase()),
        entry(Token.TILDE, (s, t) -> StringUtils.toggleFirst(t)),
        entry(Token.ALL_TOGGLE, (s, t) -> StringUtils.toggleAll(t)));
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

        nextToken(Mode.INTERPOLATE, Token.END_OF_PROGRAM);
    }


    @Nonnull
    Executor parse() {
        Executor result = fullExpression(Token.END_OF_PROGRAM);

        assertToken(Token.END_OF_PROGRAM);

        return result;
    }


    @Nonnull
    private Executor fullExpression(@Nonnull Token follow) {
        Executor result = null;

        while (terminal.token() != follow) {
            Executor interpolateElement = interpolate(follow);

            if (result == null) {
                result = interpolateElement;
            } else {
                Executor finalResult = result;
                result = compiler.concatenate(finalResult, interpolateElement);
            }

            nextToken(Mode.INTERPOLATE, follow);
        }

        return (result == null ? compiler.textConstant("") : result);
    }


    @Nonnull
    private Executor interpolate(@Nonnull Token follow) {
        Executor interpolateElement;

        if (terminal.token() == Token.STRING) {
            interpolateElement = string();
        } else if (terminal.token() == Token.NUMBER) {
            interpolateElement = number();
        } else if (terminal.token() == Token.VALUE_INTERPOLATION) {
            interpolateElement = value(follow);
        } else if (terminal.token() == Token.EXPRESSION_INTERPOLATION) {
            nextToken(Mode.EXPRESSION, follow);

            interpolateElement = calculation(follow);
            assertToken(Token.RIGHT_PARENTHESES);
        } else if (terminal.token() == Token.FUNCTION_INTERPOLATION) {
            interpolateElement = functionInterpolation(follow);
        } else {                // Syntax error
            interpolateElement = unexpected();
        }

        return interpolateElement;
    }

    @Nonnull
    private Executor string() {
        return compiler.textConstant(terminal.lexeme());
    }

    @Nonnull
    private Executor number() {
        return compiler.numericConstant(terminal.value());
    }


    @Nonnull
    private Executor value(@Nonnull Token follow) {
        Compiler.SymbolBuilder symbolBuilder;
        boolean takeLength;

        nextToken(follow);

        takeLength = (terminal.token() == Token.HASH);
        if (takeLength) {
            nextToken(follow);
        }

        assertToken(Token.IDENTIFIER);

        symbolBuilder = compiler.readSymbol(terminal.lexeme());
        nextToken(follow);

        if (terminal.token() == Token.COLON) {
            symbolBuilder = valueSubstring(symbolBuilder, follow);
        }

        symbolBuilder = valueCaseChange(symbolBuilder, follow);

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
    private Compiler.SymbolBuilder valueCaseChange(@Nonnull Compiler.SymbolBuilder symbolBuilder,
                                                   @Nonnull Token follow) {
        boolean done;

        do {
            Token token = terminal.token();
            Compiler.SymbolTransformation caseOp = CASE_OP.get(token);
            done = (caseOp == null);

            if (!done) {
                symbolBuilder = symbolBuilder.withTransformation(caseOp);

                nextToken(follow);
            }
        } while (!done);

        return symbolBuilder;
    }

    @Nonnull
    private Compiler.SymbolBuilder valueSubstring(@Nonnull Compiler.SymbolBuilder symbolBuilder,
                                                  @Nonnull Token follow) {
        Executor count;
        Executor start;

        nextToken(Mode.INTERPOLATE, Token.COLON);

        start = interpolate(Token.COLON);
        nextToken(Token.COLON, follow);
        nextToken(follow);

        count = interpolate(Token.RIGHT_BRACE);
        nextToken(follow);

        return symbolBuilder.withTransformation((s, t) ->
            StringUtils.mid(t, start.execute(s).asInteger(), count.execute(s).asInteger()));
    }

    @Nonnull
    private Executor valueDefault() {
        nextToken(Mode.INTERPOLATE, Token.RIGHT_BRACE);

        return fullExpression(Token.RIGHT_BRACE);
    }


    @Nonnull
    private Executor functionInterpolation(@Nonnull Token follow) {
        nextToken(Token.IDENTIFIER, follow);

        String name = terminal.lexeme();

        nextToken(Token.LEFT_PARENTHESES, follow);

        return functionCall(name, Token.RIGHT_PARENTHESES);
    }


    @Nonnull
    private Executor calculation(@Nonnull Token follow) {
        Executor result = expression(follow);

        if (terminal.token() == Token.QUESTION_MARK) {
            nextToken(follow);

            Executor first = expression(follow);

            assertToken(Token.COLON);
            nextToken(follow);

            result = compiler.conditional(result, first, expression(follow));
        }

        return result;
    }


    @Nonnull
    private Executor expression(@Nonnull Token follow) {
        Executor result = simpleExpression(follow);
        CompileBinaryOp relOp = REL_OP.get(terminal.token());

        while (relOp != null) {
            nextToken(follow);
            result = relOp.apply(compiler, result, simpleExpression(follow));
            relOp = REL_OP.get(terminal.token());
        }

        return result;
    }


    @Nonnull
    private Executor simpleExpression(@Nonnull Token follow) {
        Executor result = term(follow);
        CompileBinaryOp shiftOp = SHIFT_OP.get(terminal.token());

        while (shiftOp != null) {
            nextToken(follow);
            result = shiftOp.apply(compiler, result, term(follow));
            shiftOp = SHIFT_OP.get(terminal.token());
        }

        return result;
    }


    @Nonnull
    private Executor term(@Nonnull Token follow) {
        Executor result = simpleTerm(follow);
        CompileBinaryOp addOp = ADD_OP.get(terminal.token());

        while (addOp != null) {
            nextToken(follow);
            result = addOp.apply(compiler, result, simpleTerm(follow));
            addOp = ADD_OP.get(terminal.token());
        }

        return result;
    }


    @Nonnull
    private Executor simpleTerm(@Nonnull Token follow) {
        Executor result = factor(follow);
        CompileBinaryOp mulOp = MUL_OP.get(terminal.token());

        while (mulOp != null) {
            nextToken(follow);
            result = mulOp.apply(compiler, result, factor(follow));
            mulOp = MUL_OP.get(terminal.token());
        }

        return result;
    }


    @Nonnull
    private Executor factor(@Nonnull Token follow) {
        Executor result = simpleFactor(follow);
        CompileBinaryOp powOp = POW_OP.get(terminal.token());

        while (powOp != null) {
            nextToken(follow);
            result = powOp.apply(compiler, result, simpleFactor(follow));
            powOp = POW_OP.get(terminal.token());
        }

        return result;
    }


    @Nonnull
    private Executor simpleFactor(@Nonnull Token follow) {
        Executor result;

        if (terminal.token() == Token.MINUS) {
            nextToken(follow);
            result = compiler.negate(simpleFactor(follow));
        } else if (terminal.token() == Token.PLUS) {
            nextToken(follow);
            result = simpleFactor(follow);
        } else if (terminal.token() == Token.LOGICAL_NOT) {
            nextToken(follow);
            result = compiler.logicalNot(simpleFactor(follow));
        } else if (terminal.token() == Token.TILDE) {                           // Bitwise not
            nextToken(follow);
            result = compiler.bitwiseNot(simpleFactor(follow));
        } else if (terminal.token() == Token.STRING) {
            result = string();
            nextToken(follow);
        } else if (terminal.token() == Token.NUMBER) {
            result = number();
            nextToken(follow);
        } else if (LOGIC_CONSTANTS.containsKey(terminal.token())) {
            result = compiler.logicConstant(LOGIC_CONSTANTS.get(terminal.token()));
            nextToken(follow);
        } else if (terminal.token() == Token.VALUE_INTERPOLATION) {
            result = value(follow);
            nextToken(follow);
        } else if (terminal.token() == Token.IDENTIFIER) {
            result = identifier(follow);
        } else if (terminal.token() == Token.LEFT_PARENTHESES) {
            nextToken(follow);
            result = calculation(follow);
            assertToken(Token.RIGHT_PARENTHESES);
            nextToken(follow);
        } else if (terminal.token() == Token.FUNCTION_INTERPOLATION) {
            result = functionInterpolation(follow);                             // legal and consistent, but pointless
            nextToken(Token.RIGHT_PARENTHESES, follow);
        } else {
            result = unexpected();
        }

        return result;
    }


    @Nonnull
    private Executor identifier(@Nonnull Token follow) {
        Executor result;
        String name = terminal.lexeme();

        nextToken(follow);

        if (terminal.token() == Token.LEFT_PARENTHESES) {
            result = functionCall(name, follow);

            nextToken(follow);
        } else if (terminal.token() == Token.QUESTION_MARK) {
            result = isDefined(name, follow);
        } else {
            result = unexpected();
        }

        return result;
    }

    @Nonnull
    private Executor isDefined(@Nonnull String identifier, @Nonnull Token follow) {
        nextToken(follow);

        return compiler.isDefined(identifier);
    }

    @Nonnull
    private Executor functionCall(@Nonnull String functionName, @Nonnull Token follow) {
        Executor result;
        List<Executor> argumentList;

        nextToken(follow);

        argumentList = argumentList(follow);

        assertToken(Token.RIGHT_PARENTHESES);

        result = context.getFunctionManager()
            .compileCall(functionName, context, argumentList);

        return result;
    }


    @Nonnull
    private List<Executor> argumentList(@Nonnull Token follow) {
        List<Executor> arguments = new ArrayList<>();
        boolean more = (terminal.token() != Token.RIGHT_PARENTHESES);

        while (more) {
            arguments.add(calculation(Token.RIGHT_PARENTHESES));
            more = terminal.token() == Token.COMMA;

            if (more) {
                nextToken(follow);
            }
        }

        return arguments;
    }


    /**
     * Read the next terminal from the source in to {@link #terminal}. Mode is {@link Mode#EXPRESSION}
     * @param follow    Next token in subexpression
     */
    private void nextToken(@Nonnull Token follow) {
        nextToken(Mode.EXPRESSION, follow);
    }

    /**
     * Read the next terminal from the source in to {@link #terminal}
     * @param mode      Read mode
     * @param follow    Next token in subexpression
     */
    private void nextToken(@Nonnull Mode mode, @Nonnull Token follow) {
        terminal = tokenizer.next(mode, follow.lexeme().charAt(0));
    }

    /**
     * Read the next terminal from the source in to {@link #terminal} and assert that it's the {@code expected} type.
     * @param expected  The expected token
     * @param follow    Next token in subexpression
     */
    private void nextToken(@Nonnull Token expected, @Nonnull Token follow) {
        nextToken(follow);
        assertToken(expected);
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
