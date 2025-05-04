package com.github.tymefly.eel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.Tokenizer.Terminal;
import com.github.tymefly.eel.exception.EelSemanticException;
import com.github.tymefly.eel.exception.EelSyntaxException;
import com.github.tymefly.eel.utils.StringUtils;

import static java.util.Map.entry;

/**
 * EEL Parser implementation
 */
class Parser {
    private static final Set<Token> SUBSTRING_VALUE_OP = Set.of(
        Token.MINUS,
        Token.PLUS,
        Token.NUMERIC,
        Token.LEFT_PARENTHESES,
        Token.VALUE_INTERPOLATION,
        Token.FUNCTION_INTERPOLATION,
        Token.EXPRESSION_INTERPOLATION
    );
    private static final Map<Token, Compiler.SymbolTransformation> CASE_OP = Map.ofEntries(
        entry(Token.CARET, (s, t) -> StringUtils.upperFirst(t)),
        entry(Token.ALL_UPPER, (s, t) -> t.toUpperCase()),
        entry(Token.COMMA, (s, t) -> StringUtils.lowerFirst(t)),
        entry(Token.ALL_LOWER, (s, t) -> t.toLowerCase()),
        entry(Token.TILDE, (s, t) -> StringUtils.toggleFirst(t)),
        entry(Token.ALL_TOGGLE, (s, t) -> StringUtils.toggleAll(t)));
    private static final Map<Token, CompileBinaryOp> EQUAL_OP = Map.ofEntries(
        entry(Token.EQUAL, Compiler::equal),
        entry(Token.NOT_EQUAL, Compiler::notEqual));
    private static final Map<Token, CompileBinaryOp> REL_OP = Map.ofEntries(
        entry(Token.GREATER_THAN, Compiler::greaterThan),
        entry(Token.LESS_THAN, Compiler::lessThan),
        entry(Token.GREATER_THAN_EQUAL, Compiler::greaterThenEquals),
        entry(Token.LESS_THAN_EQUAL, Compiler::lessThanEquals),
        entry(Token.IS_BEFORE, Compiler::isBefore),
        entry(Token.IS_AFTER, Compiler::isAfter));
    private static final Map<Token, CompileBinaryOp> SHIFT_OP = Map.ofEntries(
        entry(Token.LEFT_SHIFT, Compiler::leftShift),
        entry(Token.RIGHT_SHIFT, Compiler::rightShift));
    private static final Map<Token, CompileBinaryOp> ADD_OP = Map.ofEntries(
        entry(Token.PLUS, Compiler::add),
        entry(Token.MINUS, Compiler::subtract));
    private static final Map<Token, CompileBinaryOp> MUL_OP = Map.ofEntries(
        entry(Token.MULTIPLY, Compiler::multiply),
        entry(Token.DIVIDE, Compiler::divide),
        entry(Token.DIVIDE_FLOOR, Compiler::divideFloor),
        entry(Token.DIVIDE_TRUNCATE, Compiler::divideTruncate),
        entry(Token.MODULUS, Compiler::modulus));
    private static final Map<Token, CompileBinaryOp> POW_OP = Map.ofEntries(
        entry(Token.EXPONENTIATION, Compiler::power),
        entry(Token.CONCATENATE, Compiler::concatenate));
    private static final Map<Token, CompileUnaryOp> UNARY_FUNCTION = Map.ofEntries(
        entry(Token.TEXT, Compiler::callText),
        entry(Token.NUMBER, Compiler::callNumber),
        entry(Token.LOGIC, Compiler::callLogic),
        entry(Token.DATE, Compiler::callDate));
    private static final Map<Token, Boolean> LOGIC_CONSTANTS = Map.ofEntries(
        entry(Token.TRUE, true),
        entry(Token.FALSE, false));
    public static final Set<Token> VALUE_NAMES = Set.of(
        Token.IDENTIFIER,
        Token.TRUE,
        Token.FALSE,
        Token.TEXT,
        Token.NUMBER,
        Token.LOGIC,
        Token.DATE,
        Token.LOGICAL_NOT,
        Token.LOGICAL_AND,
        Token.LOGICAL_OR,
        Token.LOGICAL_XOR,
        Token.IS_BEFORE,
        Token.IS_AFTER,
        Token.IN);

    private final Tokenizer tokenizer;
    private final EelContextImpl context;
    private final Compiler compiler;
    private final List<Term> lookBacks;

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
        this.lookBacks = new ArrayList<>();

        nextText(Token.END_OF_PROGRAM);
    }


    @Nonnull
    Term parse() {
        Term result = expression(Token.END_OF_PROGRAM);

        assertToken(Token.END_OF_PROGRAM);

        return result;
    }


    @Nonnull
    private Term expression(@Nonnull Token follow) {
        Term result = null;

        while (terminal.token() != follow) {
            Term interpolateElement = interpolate(follow);

            if (result == null) {
                result = interpolateElement;
            } else {
                Term finalResult = result;
                result = compiler.concatenate(finalResult, interpolateElement);
            }

            nextText(follow);
        }

        return (result == null ? compiler.textConstant("") : result);
    }


    @Nonnull
    private Term interpolate(@Nonnull Token follow) {
        Term interpolateElement;
        Token token = terminal.token();

        if (token == Token.TEXT_LITERAL) {
            interpolateElement = text();
        } else if (token == Token.VALUE_INTERPOLATION) {
            interpolateElement = valueInterpolation(follow);
        } else if (token == Token.FUNCTION_INTERPOLATION) {
            interpolateElement = functionInterpolation(follow);
        } else if (token == Token.EXPRESSION_INTERPOLATION) {
            interpolateElement = expressionInterpolation(follow);
        } else if (token == Token.LOOK_BACK) {
            interpolateElement = lookBack(follow);
        } else {                // Syntax error
            interpolateElement = unexpected();
        }

        return interpolateElement;
    }

    @Nonnull
    private Term text() {
        return compiler.textConstant(terminal.lexeme());
    }

    @Nonnull
    private Term number() {
        return compiler.numericConstant(terminal.value());
    }

    @Nonnull
    private Term expressionInterpolation(@Nonnull Token follow) {
        Term interpolateElement;
        nextToken(follow);

        interpolateElement = expressionSequence(follow);
        assertToken(Token.RIGHT_PARENTHESES);

        return interpolateElement;
    }

    @Nonnull
    private Term valueInterpolation(@Nonnull Token follow) {
        Compiler.SymbolBuilder symbolBuilder;
        boolean takeLength;

        nextToken(follow);

        takeLength = (terminal.token() == Token.HASH);
        if (takeLength) {
            nextToken(follow);
        }

        // Doesn't have to be an identifier - symbols table entries can match a token names so adding a new operator
        // won't break existing expressions
        assertToken(VALUE_NAMES);

        symbolBuilder = compiler.read(terminal.lexeme());
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
        } else if (terminal.token() == Token.BLANK_DEFAULT) {
            symbolBuilder = symbolBuilder.withBlankDefault(valueDefault());
        } else {
            // Do nothing - the only other option is a right brace
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
        Term count;
        Term start;

        nextToken(Token.COLON);

        start = substringValue(Token.COLON);
        if (terminal.token() == Token.COLON) {
            nextToken(follow);
            count = substringValue(Token.RIGHT_BRACE);
        } else {
            count = Constant.of(Integer.MAX_VALUE);
        }

        return symbolBuilder.withTransformation((s, t) ->
            StringUtils.mid(t, start.evaluate(s).asInt(), count.evaluate(s).asInt()));
    }


    @Nonnull
    private Term substringValue(@Nonnull Token follow) {
        Term interpolateElement;
        Token token = terminal.token();

        if (SUBSTRING_VALUE_OP.contains(token)) {
            interpolateElement = precedence2(follow);
        } else {                // Syntax error
            interpolateElement = unexpected();
        }

        return interpolateElement;
    }


    @Nonnull
    private Term valueDefault() {
        nextText(Token.RIGHT_BRACE);

        return expression(Token.RIGHT_BRACE);
    }


    @Nonnull
    private Term functionInterpolation(@Nonnull Token follow) {
        Term result;

        nextToken(follow);

        if (terminal.token() == Token.IDENTIFIER) {
            String name = terminal.lexeme();

            nextToken(Token.LEFT_PARENTHESES, follow);

            result = functionCall(name, Token.RIGHT_PARENTHESES);
        } else if (UNARY_FUNCTION.containsKey(terminal.token())) {      // type conversions functions
            result = unaryFunction(follow);
        } else {
            result = unexpected();
        }

        return result;
    }


    @Nonnull
    private Term expressionSequence(@Nonnull Token follow) {
        Term result = precedence16(follow);

        while (terminal.token() == Token.SEMICOLON) {
            Term cached = compiler.cached(result);

            nextToken(follow);

            lookBacks.add(cached);
            result = precedence16(follow);
        }

        lookBacks.clear();                                  // Limit look backs to the expression interpolation block

        return result;
    }


    @Nonnull
    private Term precedence16(@Nonnull Token follow) {
        Term result = precedence15(follow);

        if (terminal.token() == Token.QUESTION_MARK) {
            nextToken(follow);

            Term first = precedence16(follow);

            assertToken(Token.COLON);
            nextToken(follow);

            result = compiler.conditional(result, first, precedence16(follow));
        }

        return result;
    }

    @Nonnull
    private Term precedence15(@Nonnull Token follow) {
        Term result = precedence14(follow);

        while (terminal.token() == Token.LOGICAL_OR) {
            nextToken(follow);
            result = compiler.logicalOr(result, precedence14(follow));
        }

        return result;
    }

    @Nonnull
    private Term precedence14(@Nonnull Token follow) {
        Term result = precedence13(follow);

        while (terminal.token() == Token.LOGICAL_XOR) {
            nextToken(follow);
            result = compiler.logicalXor(result, precedence13(follow));
        }

        return result;
    }

    @Nonnull
    private Term precedence13(@Nonnull Token follow) {
        Term result = precedence12(follow);

        while (terminal.token() == Token.LOGICAL_AND) {
            nextToken(follow);
            result = compiler.logicalAnd(result, precedence12(follow));
        }

        return result;
    }

    @Nonnull
    private Term precedence12(@Nonnull Token follow) {
        Term result;

        if (terminal.token() == Token.LOGICAL_NOT) {
            nextToken(follow);
            result = compiler.logicalNot(precedence12(follow));
        } else {
            result = precedence11(follow);
        }

        return result;
    }

    @Nonnull
    private Term precedence11(@Nonnull Token follow) {
        Term result = precedence10(follow);
        CompileBinaryOp op = EQUAL_OP.get(terminal.token());

        while (op != null) {
            nextToken(follow);
            result = op.apply(compiler, result, precedence10(follow));
            op = EQUAL_OP.get(terminal.token());
        }

        return result;
    }

    @Nonnull
    private Term precedence10(@Nonnull Token follow) {
        Term result = precedence9(follow);
        boolean done = false;

        while (!done) {
            CompileBinaryOp op = REL_OP.get(terminal.token());

            if (op != null) {
                nextToken(follow);
                result = op.apply(compiler, result, precedence9(follow));
            } else if (terminal.token() == Token.IN) {
                result = in(result, follow);
            } else {
                done = true;
            }
        }

        return result;
    }

    @Nonnull
    private Term in(@Nonnull Term left, @Nonnull Token follow) {
        nextToken(Token.LEFT_BRACE, follow);
        nextToken(follow);

        List<Term> terms = termList(follow, Token.RIGHT_BRACE);

        assertToken(Token.RIGHT_BRACE);
        nextToken(follow);

        return compiler.in(left, terms);
    }


    @Nonnull
    private Term precedence9(@Nonnull Token follow) {
        Term result = precedence8(follow);

        while (terminal.token() == Token.BITWISE_OR) {
            nextToken(follow);
            result = compiler.bitwiseOr(result, precedence8(follow));
        }

        return result;
    }

    @Nonnull
    private Term precedence8(@Nonnull Token follow) {
        Term result = precedence7(follow);

        while (terminal.token() == Token.CARET) {
            nextToken(follow);
            result = compiler.bitwiseXor(result, precedence8(follow));
        }

        return result;
    }

    @Nonnull
    private Term precedence7(@Nonnull Token follow) {
        Term result = precedence6(follow);

        while (terminal.token() == Token.BITWISE_AND) {
            nextToken(follow);
            result = compiler.bitwiseAnd(result, precedence6(follow));
        }

        return result;
    }


    @Nonnull
    private Term precedence6(@Nonnull Token follow) {
        Term result = precedence5(follow);
        CompileBinaryOp op = SHIFT_OP.get(terminal.token());

        while (op != null) {
            nextToken(follow);
            result = op.apply(compiler, result, precedence5(follow));
            op = SHIFT_OP.get(terminal.token());
        }

        return result;
    }

    @Nonnull
    private Term precedence5(@Nonnull Token follow) {
        Term result = precedence4(follow);
        CompileBinaryOp op = ADD_OP.get(terminal.token());

        while (op != null) {
            nextToken(follow);
            result = op.apply(compiler, result, precedence4(follow));
            op = ADD_OP.get(terminal.token());
        }

        return result;
    }


    @Nonnull
    private Term precedence4(@Nonnull Token follow) {
        Term result = precedence3(follow);
        CompileBinaryOp op = MUL_OP.get(terminal.token());

        while (op != null) {
            nextToken(follow);
            result = op.apply(compiler, result, precedence3(follow));
            op = MUL_OP.get(terminal.token());
        }

        return result;
    }


    @Nonnull
    private Term precedence3(@Nonnull Token follow) {
        Term result = precedence2(follow);
        CompileBinaryOp op = POW_OP.get(terminal.token());

        while (op != null) {
            nextToken(follow);
            result = op.apply(compiler, result, precedence2(follow));
            op = POW_OP.get(terminal.token());
        }

        return result;
    }


    @Nonnull
    private Term precedence2(@Nonnull Token follow) {
        Token token = terminal.token();
        Term result;

        if (token == Token.LEFT_PARENTHESES) {
            nextToken(follow);
            result = precedence16(follow);
            assertToken(Token.RIGHT_PARENTHESES);
            nextToken(follow);
        } else if (token == Token.MINUS) {
            nextToken(follow);
            result = compiler.negate(precedence2(follow));
        } else if (token == Token.PLUS) {
            nextToken(follow);
            result = precedence2(follow);
        } else if (token == Token.TILDE) {                           // Bitwise not
            nextToken(follow);
            result = compiler.bitwiseNot(precedence2(follow));
        } else if ((token == Token.SINGLE_QUOTE) || (token == Token.DOUBLE_QUOTE)) {
            result = textLiteral();
            nextToken(follow);
        } else if (token == Token.NUMERIC) {
            result = number();
            nextToken(follow);
        } else if (LOGIC_CONSTANTS.containsKey(token)) {
            result = compiler.logicConstant(LOGIC_CONSTANTS.get(token));
            nextToken(follow);
        } else if (UNARY_FUNCTION.containsKey(token)) {
            result = unaryFunction(follow);
            nextToken(follow);
        } else if (token == Token.VALUE_INTERPOLATION) {
            result = valueInterpolation(follow);
            nextToken(follow);
        } else if (token == Token.IDENTIFIER) {
            result = precedence1(follow);
        } else if (token == Token.LOOK_BACK) {
            result = lookBack(follow);
            nextToken(follow);
        } else if (token == Token.FUNCTION_INTERPOLATION) {         // legal and consistent, but pointless
            result = functionInterpolation(Token.RIGHT_PARENTHESES);
            nextToken(follow);
        } else if (token == Token.EXPRESSION_INTERPOLATION) {       // legal and consistent, but pointless
            nextToken(follow);

            result = precedence16(follow);
            nextToken(Token.RIGHT_PARENTHESES);
        } else {
            result = unexpected();
        }

        return result;
    }

    @Nonnull
    private Term textLiteral() {
        Token quote = terminal.token();
        Term result;

        nextText(quote);
        result = expression(quote);
        assertToken(quote);

        return result;
    }

    @Nonnull
    private Term lookBack(@Nonnull Token follow) {
        Term result;
        int index;

        nextToken(Token.NUMERIC, follow);

        if (!terminal.isDecimal()) {
            throw new EelSemanticException(terminal.position(), "Invalid lookBack $%s", terminal.lexeme());
        }

        index = terminal.decimal();

        nextToken(Token.RIGHT_BRACKET, follow);

        if ((index > lookBacks.size()) || (index <= 0)) {
            throw new EelSemanticException(terminal.position(), "Undefined lookBack $[%d]", index);
        }

        result = lookBacks.get(index - 1);

        return result;
    }

    @Nonnull
    private Term unaryFunction(@Nonnull Token follow) {
        Term result;
        Term operand;
        CompileUnaryOp op = UNARY_FUNCTION.get(terminal.token());

        nextToken(Token.LEFT_PARENTHESES, follow);
        nextToken(follow);
        operand = precedence16(follow);
        assertToken(Token.RIGHT_PARENTHESES);

        result = op.apply(compiler, operand);

        return result;
    }


    @Nonnull
    private Term precedence1(@Nonnull Token follow) {
        Term result;
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
    private Term isDefined(@Nonnull String identifier, @Nonnull Token follow) {
        nextToken(follow);

        return compiler.isDefined(identifier);
    }

    @Nonnull
    private Term functionCall(@Nonnull String functionName, @Nonnull Token follow) {
        Term result;
        List<Term> argumentList;

        nextToken(follow);

        argumentList = termList(follow, Token.RIGHT_PARENTHESES);

        assertToken(Token.RIGHT_PARENTHESES);

        result = context.getFunctionManager()
            .compileCall(functionName, context, argumentList);

        return result;
    }


    @Nonnull
    private List<Term> termList(@Nonnull Token follow, @Nonnull Token endToken) {
        List<Term> terms = new ArrayList<>();
        boolean more = (terminal.token() != endToken);

        while (more) {
            terms.add(precedence16(endToken));
            more = (terminal.token() == Token.COMMA);

            if (more) {
                nextToken(follow);
            }
        }

        return terms;
    }


    /**
     * Update {@link #terminal} with the next terminal from the source as a text token
     * @param follow    Next token in subexpression
     */
    private void nextText(@Nonnull Token follow) {
        terminal = tokenizer.text(follow);
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
     * Update {@link #terminal} with the next terminal from the source as an interpolation token
     * @param follow    Next token in subexpression
     */
    private void nextToken(@Nonnull Token follow) {
        terminal = tokenizer.interpolate(follow);
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
     * Ensure the current token is the one of the {@code expected} tokens
     * @param expected  The expected token
     */
    private void assertToken(@Nonnull Collection<Token> expected) {
        if (!expected.contains(terminal.token())) {
            unexpected();
        }
    }


    /**
     * Throw a standardised exception to indicate there was an unexpected token
     * @return nothing, but the calling methods need an Executor to make them compile
     * @throws EelSyntaxException everytime
     */
    @Nonnull
    private Term unexpected() throws EelSyntaxException {
        if (terminal.token() == Token.END_OF_PROGRAM) {
            throw new EelSyntaxException(terminal.position(), "Unexpected end of expression");
        } else {
            throw new EelSyntaxException(terminal.position(), "'%s' was unexpected", terminal.lexeme());
        }
    }
}
