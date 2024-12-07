package uk.co.mpcontracting.rpmjukebox.test.javafx;

// Adapted from https://github.com/roskenet/springboot-javafx-test to use JUnit 5

import de.felixroske.jfxsupport.AbstractFxmlView;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.testfx.framework.junit5.ApplicationTest;
import java.util.Objects;

import static java.util.Objects.requireNonNullElseGet;

public abstract class GuiTest extends ApplicationTest implements ApplicationContextAware {

  private ApplicationContext applicationContext;
  private AbstractFxmlView viewBean;

  @BeforeAll
  public static void setHeadlessMode() {
    String headlessProp = System.getProperty("JAVAFX_HEADLESS", "true");
    boolean headless = Boolean.parseBoolean(headlessProp);
    String geometryProp = System.getProperty("JAVAFX_GEOMETRY", "1600x1200-32");

    if (headless) {
      System.setProperty("testfx.robot", "glass");
      System.setProperty("testfx.headless", "true");
      System.setProperty("prism.order", "sw");
      System.setProperty("java.awt.headless", "true");
      System.setProperty("headless.geometry", geometryProp);
    } else {
      System.setProperty("java.awt.headless", "false");
    }
  }

  @Override
  public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  protected void init(Class<? extends AbstractFxmlView> viewClass) throws Exception {
    super.init();
    viewBean = applicationContext.getBean(viewClass);
  }

  protected void init(final AbstractFxmlView viewBean) throws Exception {
    super.init();
    this.viewBean = viewBean;
  }

  public <T extends Node> T find(final String query) {
    return lookup(query).query();
  }

  @Override
  public void start(Stage stage) {
    Objects.requireNonNull(viewBean, "No view to set up! Have you called init() before?");

    Scene scene = viewBean.getView().getScene();
    stage.setScene(requireNonNullElseGet(scene, () -> new Scene(viewBean.getView())));
    stage.show();
  }
}
