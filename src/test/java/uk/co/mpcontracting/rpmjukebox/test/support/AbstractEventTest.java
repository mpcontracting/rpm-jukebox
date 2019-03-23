package uk.co.mpcontracting.rpmjukebox.test.support;

import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import uk.co.mpcontracting.rpmjukebox.RpmJukebox;

import java.io.File;

public abstract class AbstractEventTest extends AbstractGUITest {

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
