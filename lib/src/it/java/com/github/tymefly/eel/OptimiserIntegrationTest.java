package com.github.tymefly.eel;

import java.time.Duration;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.exception.EelConvertException;
import com.github.tymefly.eel.exception.EelSyntaxException;
import com.github.tymefly.eel.exception.EelUnknownSymbolException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import uk.org.webcompere.systemstubs.rules.SystemErrRule;
import uk.org.webcompere.systemstubs.rules.SystemOutRule;

/**
 * Basic set of integration Tests
 */
public class OptimiserIntegrationTest {
    @Rule
    public SystemOutRule stdOut = new SystemOutRule();

    @Rule
    public SystemErrRule stdErr = new SystemErrRule();

    private EelContext context;


    @Before
    public void setUp() {
        context = EelContext.factory()
            .withTimeout(Duration.ofSeconds(30))
            .build();
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_failOptimisation() {
        Eel eel = Eel.compile(context, "$( 'illogical' ? 1 : 0 )");

        EelConvertException actual = Assert.assertThrows(EelConvertException.class, eel::evaluate);

        Assert.assertEquals("Unexpected message", "Can not convert 'illogical' from String to Logic", actual.getMessage());
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_dont_optimise_compilation_errors() {
        // These expressions are all missing closing brackets.
        // After the optimiser fails the rest of the exception must still be validated.
        dont_optimise_compilation_errors_helper("$( 'invalid' ? 1 : 0 ");
        dont_optimise_compilation_errors_helper("$( 'invalid' = true ");            // expression is valid
        dont_optimise_compilation_errors_helper("$( 'invalid != true ");            // expression is valid
        dont_optimise_compilation_errors_helper("$( 'invalid' > 2 ");
        dont_optimise_compilation_errors_helper("$( 'invalid' < 2 ");
        dont_optimise_compilation_errors_helper("$( 'invalid' >= 2 ");
        dont_optimise_compilation_errors_helper("$( 'invalid' <= 2 ");
        dont_optimise_compilation_errors_helper("$( 'invalid' isBefore 2 ");
        dont_optimise_compilation_errors_helper("$( 'invalid' isAfter 2 ");
        dont_optimise_compilation_errors_helper("$( -'invalid' ");
        dont_optimise_compilation_errors_helper("$( 'invalid' + 2");
        dont_optimise_compilation_errors_helper("$( 'invalid' - 2");
        dont_optimise_compilation_errors_helper("$( 'invalid' * 2");
        dont_optimise_compilation_errors_helper("$( 'invalid' / 2");
        dont_optimise_compilation_errors_helper("$( 'invalid' // 2");
        dont_optimise_compilation_errors_helper("$( 'invalid' -/ 2");
        dont_optimise_compilation_errors_helper("$( 'invalid' % 2");
        dont_optimise_compilation_errors_helper("$( 'invalid' ** 2");
        dont_optimise_compilation_errors_helper("$( not 'invalid' ");
        dont_optimise_compilation_errors_helper("$( 'invalid' and 2 ");
        dont_optimise_compilation_errors_helper("$( 'invalid' or 2 ");
        dont_optimise_compilation_errors_helper("$( 'invalid' xor 2 ");
        dont_optimise_compilation_errors_helper("$( ~'invalid' ");
        dont_optimise_compilation_errors_helper("$( 'invalid' & 2 ");
        dont_optimise_compilation_errors_helper("$( 'invalid' | 2 ");
        dont_optimise_compilation_errors_helper("$( 'invalid' ^ 2 ");
        dont_optimise_compilation_errors_helper("$( 'invalid' << 2 ");
        dont_optimise_compilation_errors_helper("$( 'invalid' >> 2 ");
        dont_optimise_compilation_errors_helper("$( 'something' ~> 2 ");                // expression is valid
    }

    private void dont_optimise_compilation_errors_helper(@Nonnull String expression) {
        Assert.assertThrows(expression, EelSyntaxException.class, () -> Eel.compile(context, expression));
    }

    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_dont_optimise_away_failure() {
        dont_optimise_away_failure_helper("$( 0 + ${unknown} )");
        dont_optimise_away_failure_helper("$( ${unknown} + 0 )");
        dont_optimise_away_failure_helper("$( ${unknown} - 0 )");
        dont_optimise_away_failure_helper("$( 1 * ${unknown} )");
        dont_optimise_away_failure_helper("$( ${unknown} * 1 )");
        dont_optimise_away_failure_helper("$( ${unknown} / 1 )");
        dont_optimise_away_failure_helper("$( ${unknown} ** 1 )");
        dont_optimise_away_failure_helper("$( ${unknown} and false )");
        dont_optimise_away_failure_helper("$( ${unknown} or true )");
        dont_optimise_away_failure_helper("$( 0 & ${unknown} )");
        dont_optimise_away_failure_helper("$( ${unknown} & 0 )");
        dont_optimise_away_failure_helper("$( 0 | ${unknown} )");
        dont_optimise_away_failure_helper("$( ${unknown} | 0 )");
        dont_optimise_away_failure_helper("$( 0 << ${unknown} )");
        dont_optimise_away_failure_helper("$( ${unknown} << 0 )");
        dont_optimise_away_failure_helper("$( 0 >> ${unknown} )");
        dont_optimise_away_failure_helper("$( ${unknown} >> 0 )");
        dont_optimise_away_failure_helper(" '' ~> $( ${unknown} )");
        dont_optimise_away_failure_helper("$( ${unknown} ~> '' )");
    }

    private void dont_optimise_away_failure_helper(@Nonnull String expression) {
        Eel eel = Eel.compile(context, expression);                 // Don't fail at compile time

        EelUnknownSymbolException actual =
            Assert.assertThrows(expression, EelUnknownSymbolException.class, eel::evaluate);

        Assert.assertEquals(expression + ": Unexpected message", "Unknown variable 'unknown'", actual.getMessage());
    }


    /**
     * Integration test {@link Eel}
     */
    @Test
    public void test_optimise_away_short_circuit_failure() {
        optimise_away_short_circuit_failure_helper("$( false and ${unknown} )", false);
        optimise_away_short_circuit_failure_helper("$( true or ${unknown} )", true);
    }

    private void optimise_away_short_circuit_failure_helper(@Nonnull String expression, boolean expected) {
        Eel eel = Eel.compile(context, expression);                 // Don't fail at compile time
        Result actual = eel.evaluate();                             // Don't fail at run time either

        Assert.assertEquals(expression + ": Unexpected result", expected, actual.asLogic());
    }
}
