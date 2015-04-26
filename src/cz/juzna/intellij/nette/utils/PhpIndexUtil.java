package cz.juzna.intellij.nette.utils;


import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpClassMember;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;

import java.util.ArrayList;
import java.util.Collection;

public class PhpIndexUtil {

	public static Collection<PhpClass> getClasses(PhpType type, PhpIndex phpIndex) {
		return getClasses(type, phpIndex, null);
	}

	public static Collection<PhpClass> getClasses(PhpType type, PhpIndex phpIndex, PhpClass containingClass) {
		Collection<PhpClass> classes = new ArrayList<PhpClass>();
		for (String className : type.getTypes()) {
			if (className.startsWith("#")) {
				classes.addAll(getBySignature(className, phpIndex));
			} else if (className.equals("$this")) {
				if (containingClass != null) {
					classes.add(containingClass);
				}
			} else {
				classes.addAll(phpIndex.getClassesByFQN(className));
			}
		}

		return classes;
	}

	private static Collection<PhpClass> getBySignature(String sig, PhpIndex phpIndex) {

		Collection<PhpClass> classes = new ArrayList<PhpClass>();
		for (PhpNamedElement el : phpIndex.getBySignature(sig)) {
			classes.addAll(getClasses(el.getType(), phpIndex, el instanceof PhpClassMember ? ((PhpClassMember) el).getContainingClass() : null));
		}

		return classes;
	}

}
