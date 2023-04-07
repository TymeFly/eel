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
    private static final Map<Token, CompileVariableOp> CASE_OP = Map.ofEntries(
        entry(Token.POWER, StringUtils::upperFirst),
        entry(Token.ALL_UPPER, String::toUpperCase),
        entry(Token.COMMA, StringUtils::lowerFirst),
        entry(Token.ALL_LOWER, String::toLowerCase),
        entry(Token.TOGGLE, StringUtils::toggleFirst),
        entry(Token.ALL_TOGGLE, StringUtils::toggleAll));
    private static final Map<Token, CompileBinaryOp> REL_OP = Map.ofEntries(
        entry(Token.EQUAL, Compiler::equal),
        entry(Token.NOT_EQUAL, Compiler::notEqual),
        entry(Token.GREATER_THAN, Compiler::greaterThan),
        entry(Token.LESS_THAN, Compiler::lessThan),
        entry(Token.GREATER_THAN_EQUAL, Compiler::greaterThenEquals),
        entry(Token.LESS_THAN_EQUAL, Compiler::lessThanEquals));
    private static final Map<Token, CompileBinaryOp> SHIFT_OP = Map.ofEntries(
        entry(Token.LEFT_SHIFT, Compiler::leftShift),
        entry(Token.RIGHT_SHIFT, Compiler::rightShift),
        entry(Token.LOGICAL_OR, Compiler::logicalOr),
        entry(Token.SHORT_CIRCUIT_OR, Compiler::shortCircuitOr),
        entry(Token.BITWISE_OR, Compiler::bitwiseOr));
    private static final Map<Token, CompileBinaryOp> ADD_OP = Map.ofEntries(
        entry(Token.PLUS, Compiler::plus),
        entry(Token.MINUS, Compiler::minus),
        entry(Token.BITWISE_XOR, Compiler::bitwiseXor));
    private static final Map<Token, CompileBinaryOp> MUL_OP = Map.ofEntries(
        entry(Token.MULTIPLY, Compiler::multiply),
        entry(Token.DIVIDE, Compiler::divide),
        entry(Token.MODULUS, Compiler::modulus),
        entry(Token.LOGICAL_AND, Compiler::logicalAnd),
        entry(Token.SHORT_CIRCUIT_AND, Compiler::shortCircuitAnd),
        entry(Token.BITWISE_AND, Compiler::bitwiseAnd));
    private static final Map<Token, CompileBinaryOp> POW_OP = Map.ofEntries(
        entry(Token.POWER, Compiler::power),
        entry(Token.CONCATENATE, Compiler::concatenate));
    private static final Map<Token, CompileUnaryOp> CONVERT_OP = Map.ofEntries(
        entry(Token.CONVERT_TO_TEXT, Compiler::toText),
        entry(Token.CONVERT_TO_NUMBER, Compiler::toNumber),
        entry(Token.CONVERT_TO_LOGIC, Compiler::toLogic),
        entry(Token.CONVERT_TO_DATE, Compiler::toDate));

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

        while ((terminal.token() != Token.END_OF_PROGRAM) && (terminal.token() != Token.RIGHT_BRACE)){
            Executor interpolateElement;

            if (terminal.token() == Token.STRING) {
                interpolateElement = string();
            } else if (terminal.token() == Token.VARIABLE_EXPANSION) {
                interpolateElement = variable();
            } else if (terminal.token() == Token.EXPRESSION_EXPANSION) {
                nextToken(Mode.EXPRESSION);

                interpolateElement = calculation();
                assertToken(Token.RIGHT_PARENTHESES);
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
    private Executor variable() {
        nextToken(Mode.EXPRESSION);

        CompileVariableOp caseOp;
        boolean takeLength = (terminal.token() == Token.HASH);

        if (takeLength) {
            nextToken(Mode.EXPRESSION);
        }

        assertToken(Token.IDENTIFIER);

        String name = terminal.lexeme();
        nextToken(Mode.EXPRESSION);

        caseOp = CASE_OP.get(terminal.token());
        if (caseOp != null) {
            nextToken(Mode.EXPRESSION);
        }

        Executor defaultValue = (terminal.token() == Token.MINUS ? variableDefault() : null);
        Executor result = compiler.readVariable(name, defaultValue, caseOp, takeLength);

        assertToken(Token.RIGHT_BRACE);

        return result;
    }


    @Nonnull
    private Executor variableDefault() {
        nextToken(Mode.INTERPOLATE_BLOCK);

        return fullExpression(Mode.INTERPOLATE_BLOCK);
    }


    @Nonnull
    private Executor calculation() {
        Executor result = expression();

        if (terminal.token() == Token.QUESTION_MARK) {
            nextToken(Mode.EXPRESSION);

            Executor first = expression();

            assertToken(Token.COLON);
            nextToken(Mode.EXPRESSION);

            result = compiler.condition(result, first, expression());
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
        } else if (terminal.token() == Token.BITWISE_NOT) {
            nextToken(Mode.EXPRESSION);
            result = compiler.bitwiseNot(simpleFactor());
        } else if (CONVERT_OP.containsKey(terminal.token())) {
            result = convert(CONVERT_OP.get(terminal.token()));
        } else if (terminal.token() == Token.NUMBER) {
            result = compiler.numericConstant(terminal.value());
            nextToken(Mode.EXPRESSION);
        } else if (terminal.token() == Token.STRING) {
            result = compiler.textConstant(terminal.lexeme());
            nextToken(Mode.EXPRESSION);
        } else if (terminal.token() == Token.TRUE) {
            result = compiler.logicConstant(true);
            nextToken(Mode.EXPRESSION);
        } else if (terminal.token() == Token.FALSE) {
            result = compiler.logicConstant(false);
            nextToken(Mode.EXPRESSION);
        } else if (terminal.token() == Token.PI) {
            result = compiler.pi();
            nextToken(Mode.EXPRESSION);
        } else if (terminal.token() == Token.E) {
            result = compiler.e();
            nextToken(Mode.EXPRESSION);
        } else if (terminal.token() == Token.VARIABLE_EXPANSION) {
            result = variable();
            nextToken(Mode.EXPRESSION);
        } else if (terminal.token() == Token.IDENTIFIER) {
            result = identifier();
        } else if (terminal.token() == Token.LEFT_PARENTHESES) {
            nextToken(Mode.EXPRESSION);
            result = calculation();
            assertToken(Token.RIGHT_PARENTHESES);
            nextToken(Mode.EXPRESSION);
        } else {
            result = unexpected();
        }

        if (negate) {
            result = compiler.negate(result);
        }

        return result;
    }


    @Nonnull
    private Executor convert(@Nonnull CompileUnaryOp convertOp) {
        nextToken(Mode.EXPRESSION, Token.LEFT_PARENTHESES);
        nextToken(Mode.EXPRESSION);

        Executor value = calculation();

        assertToken(Token.RIGHT_PARENTHESES);
        nextToken(Mode.EXPRESSION);

        return convertOp.apply(compiler, value);
    }


    @Nonnull
    private Executor identifier() {
        Executor result;
        String name = terminal.lexeme();

        nextToken(Mode.EXPRESSION);

        if (terminal.token() == Token.LEFT_PARENTHESES) {
            result = functionCall(name);
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
        nextToken(Mode.EXPRESSION);

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
     * @return nothing, but the calling methods typically need Executor to make them compile
     * @throws EelSyntaxException everytime
     */
    @Nonnull
    private Executor unexpected() throws EelSyntaxException {
        throw new EelSyntaxException(terminal.position(), "'%s' was unexpected", terminal.lexeme());
    }
}
