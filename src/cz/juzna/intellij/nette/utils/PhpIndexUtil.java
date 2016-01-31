package cz.juzna.intellij.nette.utils;


import com.intellij.openapi.project.Project;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import com.jetbrains.php.lang.psi.elements.PhpTypedElement;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

public class PhpIndexUtil {

	public static Collection<PhpClass> getClasses(PhpTypedElement element, Project project) {
		PhpIndex phpIndex = PhpIndex.getInstance(project);
		return getByType(element.getType(), phpIndex);
	}

	private static Collection<PhpClass> getBySignature(String sig, PhpIndex phpIndex) {
		Collection<PhpClass> classes = new ArrayList<PhpClass>();
		for (PhpNamedElement el : phpIndex.getBySignature(sig)) {
			classes.addAll(getByType(el.getType(), phpIndex));
		}

		return classes;
	}

	public static Collection<PhpClass> getByType(PhpType type, PhpIndex phpIndex) {
		Set<String> types = type.getTypes();
		return getByType(types.toArray(new String[types.size()]), phpIndex);
	}

	public static Collection<PhpClass> getByType(String[] types, PhpIndex phpIndex) {
		Collection<PhpClass> classes = new ArrayList<PhpClass>();
		for (String className : types) {
			if (className.equals("?")) {
				//do nothing
			} else if (className.startsWith("#")) {
				classes.addAll(getBySignature(className, phpIndex));
			} else {
				classes.addAll(phpIndex.getClassesByFQN(className));
			}
		}

		return classes;

	}
}
