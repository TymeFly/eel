package com.github.tymefly.eel;

import java.util.function.Function;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link FunctionalResourceImpl}
 */
public class FunctionalResourceImplTest {
    private EelContextImpl context;
    private FunctionalResourceImpl functionalResource;


    @Before
    public void setUp() {
        context = mock();

        when(context.getResource(any(Class.class), anyString(), any(Function.class)))
            .thenReturn("myResource");

        functionalResource = new FunctionalResourceImpl(context, getClass());
    }

    /**
     * Unit test {@link FunctionalResourceImpl#getResource(String, Function)}
     */
    @Test
    public void test_getResource() {
        Function<String, String> constructor = n -> "???";

        String actual = functionalResource.getResource("myName", constructor);

        Assert.assertEquals("Unexpected resource", "myResource", actual);
        verify(context).getResource(getClass(), "myName", constructor);
    }
}