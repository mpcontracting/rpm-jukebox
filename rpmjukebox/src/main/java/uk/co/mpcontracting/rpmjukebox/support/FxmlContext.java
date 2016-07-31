package uk.co.mpcontracting.rpmjukebox.support;

import java.util.List;

import javafx.util.Callback;
import uk.co.mpcontracting.ioc.ApplicationContext;

public class FxmlContext {

	public static void initialize(List<String> scanPaths, Object... components) {
		ApplicationContext.initialize(scanPaths, components);
	}
	
	public static Object loadFxml(String fxmlFilename) {
		return FxmlHelper.loadFxml(fxmlFilename,
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
}
