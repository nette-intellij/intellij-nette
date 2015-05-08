package cz.juzna.intellij.nette.utils;

import com.intellij.util.containers.HashMap;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.Parameter;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

public class MagicFieldsUtil {

	@Nullable
	public static PhpType extractTypeFromMethodTypes(@NotNull Collection<Method> types) {
		PhpType fieldType = null;

		for (Method method : types) {
			PhpType methodType;
			if (method.getName().startsWith("set")) {
				Parameter methodParam = method.getParameters()[0];
				if (!methodParam.getDeclaredType().isEmpty()) {
					methodType = methodParam.getDeclaredType();
				} else if (methodParam.getDocTag() != null && !methodParam.getDocTag().getType().isEmpty()) {
					methodType = methodParam.getDocTag().getType();
				} else {
					continue;
				}
			} else {
				methodType = method.getType();
			}

			fieldType = fieldType == null ? methodType : PhpType.or(fieldType, methodType);
		}

		return fieldType;
	}

	@NotNull
	public static HashMap<String, Collection<Method>> findMagicFields(PhpType type, PhpIndex phpIndex) {
		HashMap<String, Collection<Method>> fields = new HashMap<String, Collection<Method>>();
		for (PhpClass cls : PhpIndexUtil.getClasses(type, phpIndex)) {
			for (Method method : cls.getMethods()) {
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

		return fields;
	}

	@NotNull
	public static HashMap<String, Field> findEventFields(PhpType type, PhpIndex phpIndex) {
		HashMap<String, Field> fields = new HashMap<String, Field>();
		for (PhpClass cls : PhpIndexUtil.getClasses(type, phpIndex)) {
			for (Field field : cls.getFields()) {
				String name = field.getName();
				if (name.length() <= 2 || !name.startsWith("on")) { // we want only "onEvent"
					continue;
				}
				fields.put(name, field);
			}
		}

		return fields;
	}

}
