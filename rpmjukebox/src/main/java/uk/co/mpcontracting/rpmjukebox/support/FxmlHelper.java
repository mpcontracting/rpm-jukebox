package uk.co.mpcontracting.rpmjukebox.support;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.util.Callback;

public abstract class FxmlHelper {
	
	private FxmlHelper() {}

	public static Object loadFxml(String fxmlFilename) {
		return loadFxml(fxmlFilename, null);
	}

	public static Object loadFxml(String fxmlFilename, Callback<Class<?>, Object> callback) {
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
