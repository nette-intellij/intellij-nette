package cz.juzna.intellij.nette.utils;

import org.jetbrains.annotations.Nullable;

public class ComponentUtil {

	public static String factoryMethodPrefix = "createComponent";

	@Nullable
	public static String methodToComponentName(String methodName) {
		if (!methodName.startsWith(factoryMethodPrefix) || methodName.length() <= factoryMethodPrefix.length()) {
			return null;
		}
		return StringUtil.lowerFirst(methodName.substring(factoryMethodPrefix.length()));
	}

}
