package cz.juzna.intellij.nette.utils;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.HashMap;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.completion.PhpVariantsUtil;
import com.jetbrains.php.completion.UsageContext;
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
			ArrayList<PhpNamedElement> elements = new ArrayList<PhpNamedElement>();
//			elements.add(method); //todo: probably causes recursion, find better way to check visibility
			Field field = cls.findFieldByName(fieldReference.getName(), false);
			if (field != null) {
				elements.add(field);
			}
			Collection<String> accessible = findAccessibleMembers(cls, fieldReference, elements);

			if (!accessible.contains(fieldReference.getName())/* && accessible.contains(method.getName())*/) {
				methods.add(method);
			}
		}
		return methods;
	}

	@NotNull
	public static HashMap<String, Collection<Method>> findMagicFields(MemberReference reference) {

		HashMap<String, Collection<Method>> fields = new HashMap<String, Collection<Method>>();
		Map<String, PhpClass> classesInFile = cz.juzna.intellij.nette.utils.PhpPsiUtil.getClassesInFile(reference);
		for (PhpClass cls : ClassFinder.getFromMemberReference(reference)) {
			if (!isNetteObject(cls, classesInFile)) {
				continue;
			}
			Collection<String> accessibleFields = null;
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
				if (accessibleFields == null) {
					accessibleFields = findAccessibleFields(cls, reference);
				}
				if (accessibleFields.contains(fieldName)) {
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

	private static Collection<String> findAccessibleFields(PhpClass cls, MemberReference reference) {
		return findAccessibleMembers(cls, reference, cls.getFields());
	}

	private static Collection<String> findAccessibleMembers(PhpClass cls, MemberReference reference, Collection<? extends PhpNamedElement> elements) {

		PhpClass containingClass = PhpPsiUtil.getParentByCondition(reference, PhpClass.INSTANCEOF);
		Collection<String> members = new ArrayList<String>();
		UsageContext usageContext = new UsageContext(PhpModifier.State.PARENT);
		usageContext.setTargetObjectClass(cls);
		if (containingClass != null) {
			usageContext.setClassForAccessFilter(containingClass);
		}

		for (LookupElement el : PhpVariantsUtil.getLookupItems(elements, false, usageContext)) {
			PsiElement psiEl = el.getPsiElement();
			if (psiEl instanceof PhpNamedElement) {
				members.add(((PhpNamedElement) psiEl).getName());
			}
		}
		return members;
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

}
