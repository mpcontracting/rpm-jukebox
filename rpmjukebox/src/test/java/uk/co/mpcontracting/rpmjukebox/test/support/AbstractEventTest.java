package uk.co.mpcontracting.rpmjukebox.test.support;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;

import lombok.SneakyThrows;
import uk.co.mpcontracting.rpmjukebox.RpmJukebox;

public abstract class AbstractEventTest extends AbstractTest {

    @Before
    @Override
    public void abstractTestBefore() {

    }

    @After
    @Override
    @SneakyThrows
    public void abstractTestCleanup() {
        File configDirectory = RpmJukebox.getConfigDirectory();

        if (configDirectory.exists()) {
            FileUtils.deleteDirectory(RpmJukebox.getConfigDirectory());
        }
    }
}
