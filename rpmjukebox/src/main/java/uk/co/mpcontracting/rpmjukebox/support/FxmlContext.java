package uk.co.mpcontracting.rpmjukebox.support;

import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.util.Callback;
import uk.co.mpcontracting.ioc.ApplicationContext;

public class FxmlContext implements Constants {

	public static void initialize(List<String> scanPaths, Object... components) {
		ApplicationContext.initialize(scanPaths, components);
	}
	
	public static Object loadFxml(String fxmlFilename) {
		return loadFxml(fxmlFilename, clazz -> ApplicationContext.getBean(clazz));
	}

	private static Object loadFxml(String fxmlFilename, Callback<Class<?>, Object> callback) {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(FxmlContext.class.getResource("/fxml/" + fxmlFilename));
			loader.setResources(ResourceBundle.getBundle(I18N_MESSAGE_BUNDLE));

			if (callback != null) {
				loader.setControllerFactory(callback);
			}

			return loader.load(FxmlContext.class.getResourceAsStream("/fxml/" + fxmlFilename));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static <T> T lookup(Node parent, String id, Class<T> clazz) {
		return clazz.cast(parent.lookup("#" + id));
	}
}
