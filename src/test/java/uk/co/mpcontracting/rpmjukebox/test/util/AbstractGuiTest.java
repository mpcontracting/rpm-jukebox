package uk.co.mpcontracting.rpmjukebox.test.util;

import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_CLASS;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.co.mpcontracting.rpmjukebox.RpmJukebox;
import uk.co.mpcontracting.rpmjukebox.event.EventAwareObject;
import uk.co.mpcontracting.rpmjukebox.event.EventProcessor;
import uk.co.mpcontracting.rpmjukebox.test.javafx.GuiTest;
import uk.co.mpcontracting.rpmjukebox.view.MainPanelView;

@Slf4j
@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = BEFORE_CLASS)
public abstract class AbstractGuiTest extends GuiTest {

  static {
    String testConfigDirectory = ".sudokutest";

    System.setProperty("JAVAFX_HEADLESS", "true");
    System.setProperty("directory.config", testConfigDirectory);
    System.setProperty("spring.profiles.active", "test");
    Locale.setDefault(Locale.UK);

    ReflectionTestUtils.setField(
        RpmJukebox.class,
        "configDirectory",
        new File(System.getProperty("user.home") + File.separator + testConfigDirectory));
  }

  @Autowired
  private EventProcessor eventProcessor;

  @Mock
  @Getter(AccessLevel.PROTECTED)
  private EventProcessor mockEventProcessor;

  @SneakyThrows
  @PostConstruct
  protected void postConstruct() {
    init(MainPanelView.class);
  }

  @BeforeEach
  public void abstractTestBefore() {
    deleteConfigDirectory();
    createConfigDirectory();
  }

  @AfterEach
  public void abstractTestCleanup() {
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

  protected void setField(Object object, Object field) {
    TestHelper.setField(object, field);
  }

  protected void setField(Object object, String fieldName, Object field) {
    TestHelper.setField(object, fieldName, field);
  }

  protected void setField(Class<?> targetClass, String fieldName, Object field) {
    TestHelper.setField(targetClass, fieldName, field);
  }

  protected <T> T getField(Object object, Class<T> clazz) {
    return TestHelper.getField(object, clazz);
  }

  protected <T> T getField(Object object, String fieldName, Class<T> clazz) {
    return TestHelper.getField(object, fieldName, clazz);
  }

  protected <T> T getField(Class<?> targetClass, String fieldName, Class<T> clazz) {
    return TestHelper.getField(targetClass, fieldName, clazz);
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
        findMenuItems(menuItems, (Menu) menuItem);
      } else if (!(menuItem instanceof SeparatorMenuItem)) {
        menuItems.add(menuItem);
      }
    });
  }
}