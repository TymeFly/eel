package com.github.tymefly.eel;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link EelLambdaImpl}
 */
public class EelLambdaImplTest {
    private Executor executor;
    private SymbolsTable symbolsTable;
    private Value value;

    private EelLambdaImpl lambda;


    @Before
    public void setUp() {
        executor = mock(Executor.class);
        symbolsTable = mock (SymbolsTable.class);
        value = mock(Value.class);

        when(executor.execute(any(SymbolsTable.class)))
            .thenReturn(value);

        lambda = new EelLambdaImpl(executor, symbolsTable);
    }

    /**
     * Unit test {@link EelLambdaImpl#get()}
     */
    @Test
    public void test_execute() {
        ArgumentCaptor<SymbolsTable> captor = ArgumentCaptor.forClass(SymbolsTable.class);

        EelValue actual = lambda.get();

        verify(executor).execute(captor.capture());

        Assert.assertSame("Unexpected Value returned", value, actual);
        Assert.assertSame("Unexpected SymbolsTable", symbolsTable, captor.getValue());
    }
}