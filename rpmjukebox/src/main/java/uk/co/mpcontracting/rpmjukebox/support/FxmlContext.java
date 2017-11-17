package uk.co.mpcontracting.rpmjukebox.support;

import java.io.IOException;
import java.util.ResourceBundle;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.util.Callback;

@Component
public class FxmlContext implements ApplicationContextAware, Constants {

	private static ApplicationContext applicationContext;
	
	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		applicationContext = context;
	}
	
	public static Object loadFxml(String fxmlFilename) {
		return loadFxml(fxmlFilename, clazz -> applicationContext.getBean(clazz));
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
	
	public static <T> T getBean(Class<T> beanClass) {
		return applicationContext.getBean(beanClass);
	}
	
	public static <T> T lookup(Node parent, String id, Class<T> clazz) {
		return clazz.cast(parent.lookup("#" + id));
	}
}
