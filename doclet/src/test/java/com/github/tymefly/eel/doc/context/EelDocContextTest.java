package com.github.tymefly.eel.doc.context;

import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import com.github.tymefly.eel.doc.config.Config;
import com.github.tymefly.eel.doc.model.ModelManager;
import com.sun.source.util.DocTrees;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link EelDocContext}
 */
public class EelDocContextTest {
    private Reporter reporter;

    private Elements elementUtils;
    private DocTrees docTrees;
    private ModelManager modelManager;
    private MockedStatic<Config> mockConfig;
    private Config config;

    private EelDocContext context;

    @BeforeEach
    public void setUp() {
        DocletEnvironment environment = mock();

        reporter = mock();
        elementUtils = mock();
        docTrees = mock();
        modelManager = mock();
        config = mock();

        when(environment.getElementUtils())
            .thenReturn(elementUtils);
        when(environment.getDocTrees())
            .thenReturn(docTrees);

        mockConfig = mockStatic(Config.class);
        mockConfig.when(Config::getInstance)
            .thenReturn(config);

        context = new EelDocContext(environment, reporter, modelManager);
    }

    @AfterEach
    public void tearDown() {
        mockConfig.close();
    }

    /**
     * Unit test {@link EelDocContext#elementUtils()}
     */
    @Test
    public void test_elementUtils() {
        assertSame(elementUtils, context.elementUtils(), "Unexpected utils");
    }

    /**
     * Unit test {@link EelDocContext#docTrees()}
     */
    @Test
    public void test_docTrees() {
        assertSame(docTrees, context.docTrees(), "Unexpected docTrees");
    }

    /**
     * Unit test {@link EelDocContext#modelManager()}
     */
    @Test
    public void test_modelManager() {
        assertSame(modelManager, context.modelManager(), "Unexpected ModelManager");
    }

    /**
     * Unit test {@link EelDocContext#note(String)}
     */
    @Test
    public void test_note() {
        context.note("Hello World");

        verify(reporter).print(Diagnostic.Kind.NOTE, "Hello World");
    }

    /**
     * Unit test {@link EelDocContext#note(String, Object...)}
     */
    @Test
    public void test_note_formatted() {
        context.note("Hello World: %d", 12);

        verify(reporter).print(Diagnostic.Kind.NOTE, "Hello World: 12");
    }

    /**
     * Unit test {@link EelDocContext#warn(String)}
     */
    @Test
    public void test_warn() {
        context.warn("Warning, there is a warning");

        verify(reporter).print(Diagnostic.Kind.WARNING, "Warning, there is a warning");
    }

    /**
     * Unit test {@link EelDocContext#warn(String, Object...)}
     */
    @Test
    public void test_warn_formatted() {
        context.warn("Warning, %s %d", "there is a warning", 123);

        verify(reporter).print(Diagnostic.Kind.WARNING, "Warning, there is a warning 123");
    }

    /**
     * Unit test {@link EelDocContext#error(String)}
     */
    @Test
    public void test_error() {
        when(config.ignoreErrors())
            .thenReturn(false);

        context.error("Error message");

        verify(reporter).print(Diagnostic.Kind.ERROR, "Error message");
    }

    /**
     * Unit test {@link EelDocContext#error(String)}
     */
    @Test
    public void test_error_lowered() {
        when(config.ignoreErrors())
            .thenReturn(true);

        context.error("Error message");

        verify(reporter).print(Diagnostic.Kind.WARNING, "Error message");
    }

    /**
     * Unit test {@link EelDocContext#error(String, Object...)}
     */
    @Test
    public void test_error_formatted() {
        when(config.ignoreErrors())
            .thenReturn(false);

        context.error("Error message %d", 1234);

        verify(reporter).print(Diagnostic.Kind.ERROR, "Error message 1234");
    }

    /**
     * Unit test {@link EelDocContext#error(String, Object...)}
     */
    @Test
    public void test_error_formatted_lowered() {
        when(config.ignoreErrors())
            .thenReturn(true);

        context.error("Error message %d", 1234);

        verify(reporter).print(Diagnostic.Kind.WARNING, "Error message 1234");
    }
}
