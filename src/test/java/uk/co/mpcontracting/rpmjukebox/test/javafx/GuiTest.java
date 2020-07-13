package uk.co.mpcontracting.rpmjukebox.test.javafx;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.assertj.core.util.Preconditions;
import org.junit.BeforeClass;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.testfx.framework.junit.ApplicationTest;
import uk.co.mpcontracting.rpmjukebox.javafx.AbstractFxmlView;

public class GuiTest extends ApplicationTest implements ApplicationContextAware {

    @BeforeClass
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

    public static boolean headless;
    public static String geometry;

    private ApplicationContext appCtx;
    private AbstractFxmlView viewBean;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.appCtx = applicationContext;
    }

    protected void init(Class<? extends AbstractFxmlView> viewClass) throws Exception {
        super.init();
        viewBean = appCtx.getBean(viewClass);
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
        Preconditions.checkNotNull(viewBean, "No view to set up! Have you called init() before?");

        Scene scene = viewBean.getView().getScene();

        if (scene == null) {
            stage.setScene(new Scene(viewBean.getView()));
        } else {
            stage.setScene(scene);
        }

        stage.show();
    }
}
