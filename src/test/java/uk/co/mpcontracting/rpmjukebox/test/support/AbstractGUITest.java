package uk.co.mpcontracting.rpmjukebox.test.support;

import javafx.scene.Node;
import javafx.scene.control.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.mpcontracting.rpmjukebox.RpmJukebox;
import uk.co.mpcontracting.rpmjukebox.event.EventManager;
import uk.co.mpcontracting.rpmjukebox.test.javafx.GuiTest;
import uk.co.mpcontracting.rpmjukebox.view.MainPanelView;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.springframework.test.util.ReflectionTestUtils.setField;

@SpringBootTest
@RunWith(SpringRunner.class)
public abstract class AbstractGUITest extends GuiTest {

    static {
        System.setProperty("JAVAFX_HEADLESS", "true");
        System.setProperty("directory.config", ".rpmjukeboxtest");
        System.setProperty("spring.profiles.active", "test");
        setField(RpmJukebox.class, "configDirectory",
                new File(System.getProperty("user.home") + File.separator + ".rpmjukeboxtest"));
        Locale.setDefault(Locale.UK);
    }

    @Mock
    @Getter(AccessLevel.PROTECTED)
    private EventManager mockEventManager;
    private EventManager eventManager;

    @PostConstruct
    public void constructView() throws Exception {
        init(MainPanelView.class);
    }

    @Before
    public void abstractTestBefore() {
        RpmJukebox.getConfigDirectory().mkdirs();

        eventManager = EventManager.getInstance();
        setField(EventManager.class, "instance", mockEventManager);
    }

    protected void clickOnNode(String query) {
        Node node = find(query);
        boolean disabled = node.isDisabled();
        boolean visible = node.isVisible();

        try {
            node.setDisable(false);
            node.setVisible(true);

            clickOn(query);
        } finally {
            node.setDisable(disabled);
            node.setVisible(visible);
        }
    }

    protected void clickOnMenuItem(String query) {
        findMenuItemInternal(query).ifPresent(MenuItem::fire);
    }

    protected MenuItem findMenuItem(String query) {
        return findMenuItemInternal(query).orElse(null);
    }

    protected CheckMenuItem findCheckMenuItem(String query) {
        return (CheckMenuItem) findMenuItemInternal(query).orElse(null);
    }

    private Optional<MenuItem> findMenuItemInternal(String query) {
        String queryInternal = query.startsWith("#") ? query.substring(1) : query;
        MenuBar menuBar = find("#menuBar");
        List<MenuItem> menuItems = new ArrayList<>();

        menuBar.getMenus().forEach(menu -> findMenuItems(menuItems, menu));

        return menuItems.stream().filter(menuItem -> menuItem.getId().equals(queryInternal)).findFirst();
    }

    private void findMenuItems(List<MenuItem> menuItems, Menu menu) {
        menu.getItems().forEach(menuItem -> {
            if (menuItem instanceof Menu) {
                findMenuItems(menuItems, (Menu)menuItem);
            } else if (!(menuItem instanceof SeparatorMenuItem)){
                menuItems.add(menuItem);
            }
        });
    }

    @After
    @SneakyThrows
    public void abstractTestCleanup() {
        setField(EventManager.class, "instance", eventManager);

        File configDirectory = RpmJukebox.getConfigDirectory();

        if (configDirectory.exists()) {
            FileUtils.deleteDirectory(RpmJukebox.getConfigDirectory());
        }
    }
}
