package uk.co.mpcontracting.rpmjukebox.test.util;

import static uk.co.mpcontracting.rpmjukebox.test.util.TestHelper.setField;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import javafx.scene.Node;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.co.mpcontracting.rpmjukebox.RpmJukebox;
import uk.co.mpcontracting.rpmjukebox.event.EventAwareObject;
import uk.co.mpcontracting.rpmjukebox.event.EventProcessor;
import uk.co.mpcontracting.rpmjukebox.test.javafx.GuiTest;
import uk.co.mpcontracting.rpmjukebox.view.MainPanelView;

@Slf4j
@SpringBootTest
@ExtendWith(MockitoExtension.class)
public abstract class AbstractGuiTest extends GuiTest {

  static {
    System.setProperty("JAVAFX_HEADLESS", "true");
    System.setProperty("directory.config", ".rpmjukeboxtest");
    System.setProperty("spring.profiles.active", "test");
    setField(RpmJukebox.class, "configDirectory",
        new File(System.getProperty("user.home") + File.separator + ".rpmjukeboxtest"));
    Locale.setDefault(Locale.UK);
  }

  @Autowired
  private EventProcessor eventProcessor;

  @Mock
  @Getter(AccessLevel.PROTECTED)
  private EventProcessor mockEventProcessor;

  @PostConstruct
  public void constructView() throws Exception {
    init(MainPanelView.class);
  }

  @BeforeEach
  void abstractTestBefore() {
    deleteConfigDirectory();
    createConfigDirectory();
  }

  @AfterEach
  @SneakyThrows
  void abstractTestCleanup() {
    deleteConfigDirectory();
  }

  protected void setupMockEventProcessor(EventAwareObject eventAwareObject) {
    setField(eventAwareObject, mockEventProcessor);
  }

  protected void restoreEventProcessor(EventAwareObject eventAwareObject) {
    setField(eventAwareObject, eventProcessor);
  }

  private void createConfigDirectory() {
    if (!RpmJukebox.getConfigDirectory().mkdirs()) {
      log.warn("Unable to create directories - {}", RpmJukebox.getConfigDirectory());
    }
  }

  @SneakyThrows
  private void deleteConfigDirectory() {
    File configDirectory = RpmJukebox.getConfigDirectory();

    if (configDirectory.exists()) {
      FileUtils.deleteDirectory(configDirectory);
    }
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
}