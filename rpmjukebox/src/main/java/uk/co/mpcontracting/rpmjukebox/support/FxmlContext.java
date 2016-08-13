package uk.co.mpcontracting.rpmjukebox.support;

import java.io.IOException;
import java.util.List;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.util.Callback;
import uk.co.mpcontracting.ioc.ApplicationContext;

public class FxmlContext {

	public static void initialize(List<String> scanPaths, Object... components) {
		ApplicationContext.initialize(scanPaths, components);
	}
	
	public static Object loadFxml(String fxmlFilename) {
		return loadFxml(fxmlFilename,
			new Callback<Class<?>, Object>() {
				@Override
				public Object call(Class<?> clazz) {
					return ApplicationContext.getBean(clazz);
				}
			}
		);
	}

	public static <T> T getBean(Class<T> beanClass) {
		return ApplicationContext.getBean(beanClass);
	}

	private static Object loadFxml(String fxmlFilename, Callback<Class<?>, Object> callback) {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(FxmlContext.class.getResource("/fxml/" + fxmlFilename));

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
