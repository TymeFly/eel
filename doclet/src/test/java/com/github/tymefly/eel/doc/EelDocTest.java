package com.github.tymefly.eel.doc;

import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.Set;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

import com.github.tymefly.eel.Eel;
import com.github.tymefly.eel.Metadata;
import com.github.tymefly.eel.doc.config.Config;
import com.github.tymefly.eel.doc.context.Context;
import com.github.tymefly.eel.doc.report.Report;
import com.github.tymefly.eel.doc.scanner.RootScanner;
import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link EelDoc}
 */
public class EelDocTest {
    private EelDoc eelDoc;

    @Before
    public void setup() {
        Reporter reporterMock = mock(Reporter.class);

        eelDoc = new EelDoc();

        eelDoc.init(Locale.ENGLISH, reporterMock);
    }

    /**
     * Unit Test {@link EelDoc#getName()}
     */
    @Test
    public void test_getName() {
        Assert.assertEquals("Unexpected name", "EelDoc", eelDoc.getName());
    }


    /**
     * Unit Test {@link EelDoc#getSupportedOptions()}
     */
    @Test
    public void test_getSupportedOptions() {
        Config configMock = mock(Config.class);
        Set<? extends Doclet.Option> options = Set.of();

        when(configMock.options())
            .thenAnswer(i -> options);

        try (
            MockedStatic<Config> configStatic = mockStatic(Config.class)
        ) {
            configStatic.when(Config::getInstance)
                .thenReturn(configMock);

            Assert.assertSame("Unexpected options", options, eelDoc.getSupportedOptions());
        }
    }


    /**
     * Unit Test {@link EelDoc#getSupportedSourceVersion()}
     */
    @Test
    public void test_getSupportedSourceVersion_returnsLatest() {
        EelDoc eelDoc = new EelDoc();
        SourceVersion mockVersion = SourceVersion.RELEASE_17;

        try (
            MockedStatic<SourceVersion> sourceVersionStatic = Mockito.mockStatic(SourceVersion.class)
        ) {
            sourceVersionStatic.when(SourceVersion::latest)
                .thenReturn(mockVersion);

            Assert.assertEquals("getSupportedSourceVersion returns mocked latest version",
                mockVersion,
                eelDoc.getSupportedSourceVersion());
        }
    }


    /**
     * Unit Test {@link EelDoc#run(DocletEnvironment)}
     */
    @Test
    public void test_run() {
        Reporter reporterMock = mock(Reporter.class);
        Config configMock = mock(Config.class);
        Metadata metadataMock = mock(Metadata.class);
        DocletEnvironment environmentMock = mock(DocletEnvironment.class);

        when(configMock.version())
            .thenReturn(true);
        when(metadataMock.version())
            .thenReturn("1.2.3");
        when(metadataMock.buildDate())
            .thenReturn(ZonedDateTime.parse("2000-01-02T03:04:05Z"));

        try (
            MockedStatic<Config> configStatic = Mockito.mockStatic(Config.class);
            MockedStatic<Eel> eelStatic = Mockito.mockStatic(Eel.class)
        ) {
            configStatic.when(Config::getInstance)
                .thenReturn(configMock);
            eelStatic.when(Eel::metadata)
                .thenReturn(metadataMock);

            EelDoc eelDoc = new EelDoc();
            eelDoc.init(Locale.ENGLISH, reporterMock);

            boolean actual = eelDoc.run(environmentMock);
            Assert.assertTrue("run failed", actual);

            // Verify that reporter.print() was called via context.note()
            verify(reporterMock)
                .print(any(Diagnostic.Kind.class), contains("Eel Doclet version 1.2.3, built on 2000-01-02T03:04:05Z"));
        }
    }

    /**
     * Unit Test {@link EelDoc#run(DocletEnvironment)}
     */
    @Test
    public void test_run_withElements() {
        Reporter reporterMockLocal = mock(Reporter.class);
        DocletEnvironment environmentMockLocal = mock(DocletEnvironment.class);
        Element elementMock = mock(Element.class);
        Config configMock = mock(Config.class);

        when(configMock.version())
            .thenReturn(false);
        when(environmentMockLocal.getIncludedElements())
            .thenAnswer(i -> Set.of(elementMock));

        try (
            MockedConstruction<Report> reportConstruction = Mockito.mockConstruction(Report.class,
                (mock, context) -> doNothing().when(mock).writeReport());
            MockedStatic<Config> configStatic = mockStatic(Config.class);
            MockedStatic<RootScanner> rootScannerStatic = mockStatic(RootScanner.class)
        ) {
            configStatic.when(Config::getInstance)
                .thenReturn(configMock);

            EelDoc localDoc = new EelDoc();
            localDoc.init(Locale.ENGLISH, reporterMockLocal);

            boolean actual = localDoc.run(environmentMockLocal);
            Assert.assertTrue("run failed", actual);

            Assert.assertEquals("Report constructor called once", 1, reportConstruction.constructed().size());

            verify(reportConstruction.constructed().get(0), times(1)).writeReport();

            rootScannerStatic.verify(() -> RootScanner.run(any(Context.class), eq(elementMock)), times(1));
        }
    }
}