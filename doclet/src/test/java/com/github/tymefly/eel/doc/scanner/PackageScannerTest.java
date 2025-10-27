package com.github.tymefly.eel.doc.scanner;

import com.github.tymefly.eel.doc.model.GroupGenerator;
import com.github.tymefly.eel.doc.source.Source;
import com.sun.source.util.DocTreePath;
import org.junit.Test;
import org.mockito.MockedConstruction;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link PackageScanner}
 */
public class PackageScannerTest {

    /**
     * Unit test for {@link PackageScanner#run(Source, GroupGenerator)}
     */
    @Test
    public void test_run() {
        Source source = mock();
        GroupGenerator model = mock();
        DocTreePath docPath = mock();

        when(source.docTreePath())
            .thenReturn(docPath);

        try (
            MockedConstruction<PackageScanner> mocked = mockConstruction(PackageScanner.class)
        ) {
            PackageScanner.run(source, model);

            PackageScanner scannerMock = mocked.constructed().get(0);

            verify(scannerMock).scan(docPath, null);
        }
    }
}
