package cz.juzna.intellij.nette.utils;

import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeSignatureKey;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

public class ClassFinder {

	private final PhpIndex phpIndex;

	private final Collection<PhpClass> classes = new HashSet<PhpClass>();

	private final Map<String, PhpClass> classesInFile;

	private final Collection<String> visited = new HashSet<String>();

	private ClassFinder(PhpIndex phpIndex) {
		this.phpIndex = phpIndex;
		this.classesInFile = Collections.emptyMap();
	}

	private ClassFinder(PhpIndex phpIndex, Map<String, PhpClass> classesInFile) {
		this.phpIndex = phpIndex;
		this.classesInFile = classesInFile;
	}

	private Collection<PhpClass> find(PhpType type) {
		appendClasses(type, null);

		return classes;
	}

	private void appendClasses(PhpType type, PhpClass containingClass) {
		for (String className : type.getTypes()) {
			if (classesInFile.containsKey(className)) {
				classes.add(classesInFile.get(className));
			} else if (className.startsWith("#")) {
				if (visited.contains(className)) {

				} else if (PhpTypeSignatureKey.CLASS.is(className.charAt(2)) && !classesInFile.containsKey(className.substring(2))) {
					classes.add(classesInFile.get(className.substring(2)));
				} else {
					visited.add(className);
					for (PhpNamedElement el : phpIndex.getBySignature(className)) {
						appendClasses(el.getType(), el instanceof PhpClassMember ? ((PhpClassMember) el).getContainingClass() : null);
					}
				}

			} else if (className.equals("$this")) {
				if (containingClass != null) {
					classes.add(containingClass);
				}
			} else {
				classes.addAll(phpIndex.getAnyByFQN(className));
			}
		}
	}

	public static Collection<PhpClass> getFromMemberReference(MemberReference reference) {
		if (reference.getClassReference() == null) {
			return Collections.emptyList();
		}
		ClassFinder finder = new ClassFinder(PhpIndex.getInstance(reference.getProject()), PhpPsiUtil.getClassesInFile(reference));

		return finder.find(reference.getClassReference().getType());
	}

	public static Collection<PhpClass> getFromTypedElement(PhpTypedElement el) {
		if (!(el instanceof PsiElement)) {
			return Collections.emptyList();
		}
		PhpType type = el.getType();
		if (type.isEmpty() || type.toString().trim().equals("")) {
			return Collections.emptyList();
		}
		ClassFinder finder = new ClassFinder(PhpIndex.getInstance(((PsiElement) el).getProject()), PhpPsiUtil.getClassesInFile((PsiElement) el));

		return finder.find(el.getType());
	}

	public static Collection<PhpClass> getClasses(PhpType type, PhpIndex index) {
		ClassFinder finder = new ClassFinder(index);

		return finder.find(type);
	}

}
