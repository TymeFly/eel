package com.github.tymefly.eel.doc.context;

import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import com.github.tymefly.eel.doc.config.Config;
import com.github.tymefly.eel.doc.model.ModelManager;
import com.sun.source.util.DocTrees;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link Context}
 */
public class ContextTest {
    private DocletEnvironment environment;
    private Reporter reporter;

    private Elements elementUtils;
    private DocTrees docTrees;
    private MockedStatic<ModelManager> mockModelManager;
    private ModelManager modelManager;
    private MockedStatic<Config> mockConfig;
    private Config config;

    private Context context;

    @Before
    public void setUp() {
        environment = mock();
        reporter = mock();
        elementUtils = mock();
        docTrees = mock();
        modelManager = mock();
        config = mock();

        when(environment.getElementUtils())
            .thenReturn(elementUtils);
        when(environment.getDocTrees())
            .thenReturn(docTrees);

        mockModelManager = mockStatic(ModelManager.class);
        mockModelManager.when(() -> ModelManager.getInstance())
            .thenReturn(modelManager);

        mockConfig = mockStatic(Config.class);
        mockConfig.when(() -> Config.getInstance())
            .thenReturn(config);

        context = new Context(environment, reporter);
    }


    @After
    public void tearDown() {
        mockModelManager.close();
        mockConfig.close();
    }

    /**
     * Unit test {@link Context#elementUtils()}
     */
    @Test
    public void test_elementUtils() {
        Assert.assertSame("Unexpected utils", context.elementUtils(), elementUtils);
    }

    /**
     * Unit test {@link Context#docTrees()}
     */
    @Test
    public void test_docTrees() {
        Assert.assertSame("Unexpected docTrees", context.docTrees(), docTrees);
    }

    /**
     * Unit test {@link Context#modelManager()}
     */
    @Test
    public void test_modelManager() {
        Assert.assertSame("Unexpected ModelManager", context.modelManager(), modelManager);
    }


    /**
     * Unit test {@link Context#note(String)}
     */
    @Test
    public void test_note() {
        context.note("Hello World");

        verify(reporter).print(Diagnostic.Kind.NOTE, "Hello World");
    }

    /**
     * Unit test {@link Context#note(String, Object...)}
     */
    @Test
    public void test_note_formatted() {
        context.note("Hello World: %d", 12);

        verify(reporter).print(Diagnostic.Kind.NOTE, "Hello World: 12");
    }


    /**
     * Unit test {@link Context#warn(String)}
     */
    @Test
    public void test_warn() {
        context.warn("Warning, there is a warning");

        verify(reporter).print(Diagnostic.Kind.WARNING, "Warning, there is a warning");
    }

    /**
     * Unit test {@link Context#warn(String, Object...)}
     */
    @Test
    public void test_warn_formatted() {
        context.warn("Warning, %s %d", "there is a warning", 123);

        verify(reporter).print(Diagnostic.Kind.WARNING, "Warning, there is a warning 123");
    }


    /**
     * Unit test {@link Context#error(String)}
     */
    @Test
    public void test_error() {
        when(config.ignoreErrors())
            .thenReturn(false);

        context.error("Error message");

        verify(reporter).print(Diagnostic.Kind.ERROR, "Error message");
    }

    /**
     * Unit test {@link Context#error(String)}
     */
    @Test
    public void test_error_lowered() {
        when(config.ignoreErrors())
            .thenReturn(true);

        context.error("Error message");

        verify(reporter).print(Diagnostic.Kind.WARNING, "Error message");
    }

    /**
     * Unit test {@link Context#error(String, Object...)}
     */
    @Test
    public void test_error_formatted() {
        when(config.ignoreErrors())
            .thenReturn(false);

        context.error("Error message %d", 1234);

        verify(reporter).print(Diagnostic.Kind.ERROR, "Error message 1234");
    }

    /**
     * Unit test {@link Context#error(String, Object...)}
     */
    @Test
    public void test_error_formatted_lowered() {
        when(config.ignoreErrors())
            .thenReturn(true);

        context.error("Error message %d", 1234);

        verify(reporter).print(Diagnostic.Kind.WARNING, "Error message 1234");
    }
}