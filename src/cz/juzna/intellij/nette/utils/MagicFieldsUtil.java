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
import java.util.Map;

public class MagicFieldsUtil {

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

	public static Collection<Method> findGetters(FieldReference fieldReference) {
		Map<String, PhpClass> classesInFile = cz.juzna.intellij.nette.utils.PhpPsiUtil.getClassesInFile(fieldReference);
		Collection<Method> methods = new ArrayList<Method>();
		for (PhpClass cls : ClassFinder.getFromMemberReference(fieldReference)) {
			if (!isNetteObject(cls, classesInFile)) {
				continue;
			}
			Method method = cls.findMethodByName("get" + StringUtil.upperFirst(fieldReference.getName()));
			if (method == null) {
				method = cls.findMethodByName("is" + StringUtil.upperFirst(fieldReference.getName()));
			}
			if (method == null) {
				continue;
			}
			Field field = cls.findFieldByName(fieldReference.getName(), false);
			PhpClass containingClass = PhpPsiUtil.getParentByCondition(fieldReference, PhpClass.INSTANCEOF);
			if ((field == null || !isAccessible(field, containingClass)) && isAccessible(method, containingClass)) {
				methods.add(method);
			}
		}
		return methods;
	}

	@NotNull
	public static HashMap<String, Collection<Method>> findMagicFields(MemberReference reference) {

		HashMap<String, Collection<Method>> fields = new HashMap<String, Collection<Method>>();
		Map<String, PhpClass> classesInFile = cz.juzna.intellij.nette.utils.PhpPsiUtil.getClassesInFile(reference);
		PhpClass containingClass = PhpPsiUtil.getParentByCondition(reference, PhpClass.INSTANCEOF);
		for (PhpClass cls : ClassFinder.getFromMemberReference(reference)) {
			if (!isNetteObject(cls, classesInFile)) {
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
		for (PhpClass cls : ClassFinder.getClasses(type, phpIndex)) {
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


	private static boolean isNetteObject(PhpClass cls, Map<String, PhpClass> classMap) {
		return cz.juzna.intellij.nette.utils.PhpPsiUtil.isTypeOf(cls, "Nette\\Object", classMap);
	}

	private static boolean isAccessible(PhpClassMember member, @Nullable PhpClass accessClass) {
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


	private static boolean classesEqual(@Nullable PhpClass one, @Nullable PhpClass another) {
		return one != null && another != null
				&& one.getFQN() != null && another.getFQN() != null
				&& one.getFQN().equals(another.getFQN());
	}

}
