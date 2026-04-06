package com.github.tymefly.eel.function.eel;

import com.github.tymefly.eel.EelContext;
import com.github.tymefly.eel.Metadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link EelMetadata}
 */
public class EelMetadataTest {
    private EelContext context;

    @BeforeEach
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
        assertEquals("mockedVersion", new EelMetadata().version(context), "Unexpected version");
    }

    /**
     * Unit test {@link EelMetadata#buildDate(EelContext)}
     */
    @Test
    public void test_buildDate() {
        assertEquals(EelContext.FALSE_DATE, new EelMetadata().buildDate(context), "Unexpected buildDate");
    }
}