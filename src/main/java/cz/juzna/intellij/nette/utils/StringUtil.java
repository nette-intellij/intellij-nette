package cz.juzna.intellij.nette.utils;


public class StringUtil {

	public static String lowerFirst(String string) {
		StringBuilder s = new StringBuilder();
		if (string.length() > 0) {
			s.append(Character.toLowerCase(string.charAt(0)));
		}
		if (string.length() > 1) {
			s.append(string.substring(1));
		}

		return s.toString();
	}

	public static String upperFirst(String string) {
		StringBuilder s = new StringBuilder();
		if (string.length() > 0) {
			s.append(Character.toUpperCase(string.charAt(0)));
		}
		if (string.length() > 1) {
			s.append(string.substring(1));
		}

		return s.toString();
	}
}
