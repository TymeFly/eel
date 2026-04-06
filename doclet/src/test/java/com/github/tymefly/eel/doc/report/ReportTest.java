package com.github.tymefly.eel.doc.report;
import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

import javax.annotation.Nonnull;

import com.github.tymefly.eel.doc.config.Config;
import com.github.tymefly.eel.doc.context.EelDocContext;
import com.github.tymefly.eel.doc.model.GroupModel;
import com.github.tymefly.eel.doc.model.ModelManager;
import com.github.tymefly.eel.doc.utils.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link Report}
 */
public class ReportTest {

    @TempDir
    File temporaryFolder;

    private EelDocContext context;
    private Config config;
    private File targetDirectory;

    private Report report;

    @BeforeEach
    public void setup() {
        context = mock(EelDocContext.class);
        config = mock(Config.class);
        targetDirectory = new File(temporaryFolder, "report-output");
        targetDirectory.mkdirs();

        when(config.targetDirectory())
            .thenReturn(targetDirectory);
        when(config.docEncoding())
            .thenReturn("UTF-8");

        report = new Report(context);
    }

    /**
     * Unit test {@link Report#writeReport()}
     */
    @Test
    public void test_writeReport_happyPath() {
        try (
            MockedStatic<Config> configurationMock = mockStatic(Config.class);
            MockedStatic<FileUtils> fileUtilsMock = mockStatic(FileUtils.class)
        ) {
            ModelManager modelManager = mock(ModelManager.class);
            GroupModel groupModel = mock(GroupModel.class);

            configurationMock.when(Config::getInstance).thenReturn(config);

            when(groupModel.name())
                .thenReturn("MathFunctions");
            when(groupModel.fileName())
                .thenReturn("math.html");
            when(modelManager.groups())
                .thenReturn(List.of(groupModel));
            when(context.modelManager())
                .thenReturn(modelManager);

            report.writeReport();

            // Verify HTML files
            verifyFileWrite(fileUtilsMock, new File(targetDirectory, "index.html"));
            verifyFileWrite(fileUtilsMock, new File(targetDirectory, "_index.html"));
            verifyFileWrite(fileUtilsMock, new File(targetDirectory, "math.html"));

            // Verify resource copying
            String[] resources = {"main.css", "eel-doc.js", "up.png", "up-hover.png", "icon.png"};
            for (var resourceName : resources) {
                File resourceTarget = new File(new File(targetDirectory, "resource"), resourceName);
                fileUtilsMock.verify(() -> FileUtils.copyResource(resourceName, resourceTarget), times(1));
            }

            verify(context).note(Mockito.contains("Writing to"), any());
        }
    }

    /**
     * Unit test {@link Report#writeReport()}
     */
    @Test
    public void test_writePage_nonNullPage() {
        try (
            MockedStatic<Config> configurationMock = mockStatic(Config.class);
            MockedStatic<FileUtils> fileUtilsMock = mockStatic(FileUtils.class)
        ) {
            AbstractPage page = mock(AbstractPage.class);

            configurationMock.when(Config::getInstance)
                .thenReturn(config);

            when(page.buildPage())
                .thenReturn("<html>content</html>");

            report.writePage(page, "test.html");

            verifyFileWrite(fileUtilsMock, new File(targetDirectory, "test.html"));
        }
    }

    /**
     * Unit test {@link Report#writeReport()}
     */
    @Test
    public void test_writePage_nullPage() {
        try (
            MockedStatic<Config> configurationMock = mockStatic(Config.class);
            MockedStatic<FileUtils> fileUtilsMock = mockStatic(FileUtils.class)
        ) {
            AbstractPage page = mock(AbstractPage.class);

            configurationMock.when(Config::getInstance)
                .thenReturn(config);
            when(page.buildPage())
                .thenReturn(null);

            report.writePage(page, "test.html");

            fileUtilsMock.verifyNoInteractions();
        }
    }

    private void verifyFileWrite(@Nonnull MockedStatic<FileUtils> fileUtilsMock, @Nonnull File file) {
        fileUtilsMock.verify(() ->
            FileUtils.write(eq(file), any(String.class), any(Charset.class)), times(1));
    }
}
