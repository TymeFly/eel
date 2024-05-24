package com.github.tymefly.eel.function.eel;

import com.github.tymefly.eel.EelContext;
import com.github.tymefly.eel.Metadata;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link EelMetadata}
 */
public class EelMetadataTest {
    private EelContext context;

    @Before
    public void setUp() {
        Metadata info = mock();

        context = spy(EelContext.factory().build());

        when(context.metadata())
            .thenReturn(info);
        when(info.version())
            .thenReturn("mockedVersion");
        when(info.buildDate())
            .thenReturn(EelContext.FALSE_DATE);
    }


    /**
     * Unit test {@link EelMetadata#version(EelContext)}
     */
    @Test
    public void test_version() {
        Assert.assertEquals("Unexpected version", "mockedVersion", new EelMetadata().version(context));
    }

    /**
     * Unit test {@link EelMetadata#buildDate(EelContext)}
     */
    @Test
    public void test_buildDate() {
        Assert.assertEquals("Unexpected buildDate", EelContext.FALSE_DATE, new EelMetadata().buildDate(context));
    }
}