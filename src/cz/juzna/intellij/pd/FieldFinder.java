package cz.juzna.intellij.pd;

import com.intellij.util.containers.HashMap;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;

import java.util.Collection;

public class FieldFinder {

	public static HashMap<String, PhpType> findMagicFields(PhpType type, PhpIndex phpIndex) {
		HashMap<String, PhpType> fields = new HashMap<String, PhpType>();

		for (String fqn : type.getTypes()) {
			Collection<PhpClass> classes = phpIndex.getClassesByFQN(fqn);
			for (PhpClass it : classes) {
				if (it == null) continue; // for sure, dunno why actually
				
				for (Field field : it.getFields()) {
					if (field.getModifier().isProtected()) {
						fields.put(field.getName(), field.getType());
					}
				}
			}
		}
		return fields;
	}

	private static String lcfirst(String string) {
		return Character.toLowerCase(string.charAt(0)) + (string.length() > 1 ? string.substring(1) : "");
	}
}
