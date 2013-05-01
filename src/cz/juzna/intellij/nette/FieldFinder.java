package cz.juzna.intellij.nette;

import com.intellij.util.containers.HashMap;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;

import java.util.Collection;

public class FieldFinder {

	public static HashMap<String, Method> findMagicFields(PhpType type, PhpIndex phpIndex) {
		HashMap<String, Method> fields = new HashMap<String, Method>();

		for (String fqn : type.getTypes()) {
			Collection<PhpClass> classes = phpIndex.getClassesByFQN(fqn);
			for (PhpClass it : classes) {
				if (it == null) continue; // for sure, dunno why actually
				for (Method method : it.getMethods()) {
					String name = method.getName();
					if ( ! name.startsWith("get")) continue;

					String fieldName = lcfirst(name.substring(3));
					fields.put(fieldName, method);
				}
			}
		}
		return fields;
	}

	private static String lcfirst(String string) {
		return Character.toLowerCase(string.charAt(0)) + (string.length() > 1 ? string.substring(1) : "");
	}
}
