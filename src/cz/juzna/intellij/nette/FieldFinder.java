package cz.juzna.intellij.nette;

import com.intellij.util.containers.HashMap;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import cz.juzna.intellij.nette.utils.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

public class FieldFinder {
	@NotNull
	public static HashMap<String, Collection<Method>> findMagicFields(PhpType type, PhpIndex phpIndex) {
		HashMap<String, Collection<Method>> fields = new HashMap<String, Collection<Method>>();

		for (String fqn : type.getTypes()) {
			Collection<PhpClass> classes = phpIndex.getClassesByFQN(fqn);
			for (PhpClass it : classes) {
				if (it == null) continue; // for sure, dunno why actually
				for (Method method : it.getMethods()) {
					String name = method.getName();

					String fieldName;

					if (name.startsWith("get") && name.length() > 3) {
						fieldName = StringUtil.lowerFirst(name.substring(3));
					} else if (name.startsWith("is") && name.length() > 2) {
						fieldName = StringUtil.lowerFirst(name.substring(2));
					} else if (name.startsWith("set") && name.length() > 3 && method.getParameters().length == 1) {
						fieldName = StringUtil.lowerFirst(name.substring(3));
					} else {
						continue;
					}

					Collection<Method> fieldMethods = fields.get(fieldName);

					if (fieldMethods == null) {
						fieldMethods = new ArrayList<Method>();
						fields.put(fieldName, fieldMethods);
					}

					fieldMethods.add(method);
				}
			}
		}
		return fields;
	}

	@NotNull
	public static HashMap<String, Field> findEventFields(PhpType type, PhpIndex phpIndex) {
		HashMap<String, Field> fields = new HashMap<String, Field>();

		for (String fqn : type.getTypes()) {
			Collection<PhpClass> classes = phpIndex.getClassesByFQN(fqn);
			for (PhpClass it : classes) {
				if (it == null) {
					continue;
				}

				for (Field field : it.getFields()) {
					String name = field.getName();
					if (name.length() <= 2 || !name.startsWith("on")) { // we want only "onEvent"
						continue;
					}

					fields.put(name, field);
				}
			}
		}

		return fields;
	}

}
