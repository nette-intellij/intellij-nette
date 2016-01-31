package cz.juzna.intellij.nette.utils;


import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class ComponentUtil {

	private static PhpType container = new PhpType().add("Nette\\ComponentModel\\Container");
	public static String factoryMethodPrefix = "createComponent";

	public static boolean isContainer(PhpClass csl) {
		return container.isConvertibleFrom(csl.getType(), PhpIndex.getInstance(csl.getProject()));
	}

	@NotNull
	public static Method[] getFactoryMethods(PsiElement el) {
		return getFactoryMethods(el, false);
	}

	@NotNull
	public static Method[] getFactoryMethods(PsiElement el, boolean onlyWithName) {
		String componentName = null;
		Collection<PhpClass> classes = null;
		if (el instanceof ArrayAccessExpression) {
			ArrayIndex index = ((ArrayAccessExpression) el).getIndex();
			if (index == null || !(el.getFirstChild() instanceof PhpTypedElement)) {
				return new Method[0];
			}
			componentName = ElementValueResolver.resolve(index.getValue());
			classes = PhpIndexUtil.getClasses((PhpTypedElement) el.getFirstChild(), el.getProject());
		} else if (el instanceof MethodReference) {
			MethodReference methodRef = (MethodReference) el;
			if (methodRef.getClassReference() == null
					|| methodRef.getName() == null
					|| !methodRef.getName().equals("getComponent")
					|| methodRef.getParameters().length != 1) {
				return new Method[0];
			}
			componentName = ElementValueResolver.resolve(methodRef.getParameters()[0]);
			classes = PhpIndexUtil.getClasses(methodRef.getClassReference(), methodRef.getProject());
		}
		if (classes == null || classes.isEmpty() || (componentName == null && onlyWithName)) {
			return new Method[0];
		}
		Collection<Method> methods = new ArrayList<Method>();
		for (PhpClass currentClass : classes) {
			if (!isContainer(currentClass)) {
				continue;
			}
			methods.addAll(getFactoryMethodsByName(currentClass, onlyWithName ? componentName : null, false));
		}
		Method[] result = new Method[methods.size()];

		return methods.toArray(result);
	}

	@NotNull
	public static Collection<Method> getFactoryMethods(@NotNull PhpClass cls, String componentName) {
		return getFactoryMethods(cls, componentName, false);
	}

	@NotNull
	public static Collection<Method> getFactoryMethods(@NotNull PhpClass cls, String componentName, boolean onlyOwn) {
		if (!isContainer(cls)) {
			return Collections.emptyList();
		}

		return getFactoryMethodsByName(cls, componentName, onlyOwn);
	}

	public static Collection<Method> getFactoryMethodsByName(@NotNull PhpClass cls, String componentName, boolean onlyOwn) {
		Collection<Method> methods = new ArrayList<Method>();
		if (componentName != null) {
			String method = factoryMethodPrefix + StringUtil.upperFirst(componentName);
			Method m = cls.findMethodByName(method);
			if (m != null) {
				methods.add(m);
			}
		} else {
			Method[] classMethods;
			if (!onlyOwn) {
				Collection<Method> tmpMethods = cls.getMethods();
				classMethods = new Method[tmpMethods.size()];
				tmpMethods.toArray(classMethods);
			} else {
				classMethods = cls.getOwnMethods();
			}
			for (Method method : classMethods) {
				if (method.getName().startsWith(factoryMethodPrefix) && method.getName().length() > factoryMethodPrefix.length()) {
					methods.add(method);
				}
			}
		}
		return methods;
	}

	@Nullable
	public static String methodToComponentName(String methodName) {
		if (!methodName.startsWith(factoryMethodPrefix) || methodName.length() <= factoryMethodPrefix.length()) {
			return null;
		}
		return StringUtil.lowerFirst(methodName.substring(factoryMethodPrefix.length()));
	}

}
