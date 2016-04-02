package cz.juzna.intellij.nette.utils;

import com.intellij.util.containers.HashMap;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.PhpPsiUtil;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

public class MagicFieldsUtil {

	private static PhpType netteObject = new PhpType().add("Nette\\Object");

	@Nullable
	public static PhpType extractTypeFromMethodTypes(@NotNull Collection<Method> types) {
		PhpType fieldType = new PhpType();

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

			for (String type : methodType.getTypes()) {
				if (type.startsWith("#")) {
					for (PhpNamedElement el : PhpIndex.getInstance(method.getProject()).getBySignature(type)) {
						fieldType.add(el.getType());
					}
				} else {
					fieldType.add(type);
				}
			}
		}

		return fieldType;
	}


	@NotNull
	public static HashMap<String, Collection<Method>> findMagicFields(MemberReference reference) {

		HashMap<String, Collection<Method>> fields = new HashMap<String, Collection<Method>>();
		PhpClass containingClass = PhpPsiUtil.getParentByCondition(reference, PhpClass.INSTANCEOF);
		for (PhpClass cls : PhpIndexUtil.getClasses(reference.getClassReference(), reference.getProject())) {
			if (!isNetteObject(cls)) {
				continue;
			}
			Collection<Method> methods = cls.getMethods();
			if (methods.isEmpty()) {
				continue;
			}
			Collection<Field> classFields = null;
			for (Method method : methods) {
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
				if (!isAccessible(method, containingClass)) {
					continue;
				}
				if (classFields == null) {
					classFields = cls.getFields();
				}
				boolean isFieldAccessible = false;
				for (Field field : classFields) {
					if (!field.getName().equals(fieldName)) {
						continue;
					}
					if (isAccessible(field, containingClass)) {
						isFieldAccessible = true;
					} else {
						isFieldAccessible = false;
						break; //there is at least one not accessible field
					}
				}
				if (isFieldAccessible) {
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
		for (PhpClass cls : PhpIndexUtil.getByType(type, phpIndex)) {
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

	public static Collection<PhpNamedElement> findMagicMethods(@NotNull  String fieldName, @NotNull  Collection<PhpClass> containingClass, @Nullable  Collection<PhpClass> calledFrom)
	{
		Collection<PhpNamedElement> result = new ArrayList<PhpNamedElement>();

		for (PhpClass cls : containingClass) {
			Field field = cls.findFieldByName(fieldName, false);
			if (field != null && (!MagicFieldsUtil.isNetteObject(cls) || MagicFieldsUtil.isAccessible(field, calledFrom))) {
				result.add(field);
			} else if (MagicFieldsUtil.isNetteObject(cls)) {
				for (String prefix : new String[]{"get", "is"}) {
					String methodName = prefix + StringUtil.upperFirst(fieldName);
					Method method = cls.findMethodByName(methodName);
					if (method != null && MagicFieldsUtil.isAccessible(method, calledFrom)) {
						result.add(method);
						break;
					}
				}
			}
		}

		return result;
	}

	public static boolean isAccessible(PhpClassMember member, @Nullable Collection<PhpClass> accessClasses)
	{
		if (accessClasses == null) {
			return isAccessible(member, (PhpClass) null);
		}
		for (PhpClass cls : accessClasses) {
			if (isAccessible(member, cls)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isAccessible(PhpClassMember member, @Nullable PhpClass accessClass) {
		PhpClass elementClass = member.getContainingClass();
		if (classesEqual(elementClass, accessClass)) {
			return true;
		}
		PhpModifier modifier = member.getModifier();
		if (modifier.isPublic()) {
			return true;
		}
		if (accessClass == null || elementClass == null) {
			return false;
		}
		for (PhpClass contextClass = accessClass; contextClass != null; contextClass = contextClass.getSuperClass()) {
			if (elementClass.isTrait()) {
				for (PhpClass traitClass : contextClass.getTraits()) {
					if (classesEqual(elementClass, traitClass)) {
						return true;
					}

				}
			} else if (classesEqual(elementClass, contextClass)) {
				return true;
			}
			if (modifier.isPrivate()) {
				break;
			}
		}

		return false;
	}

	private static boolean isNetteObject(PhpClass cls) {
		return netteObject.isConvertibleFrom(cls.getType(), PhpIndex.getInstance(cls.getProject()));
	}

	private static boolean classesEqual(@Nullable PhpClass one, @Nullable PhpClass another) {
		return one != null && another != null
				&& one.getFQN().equals(another.getFQN());
	}

}
