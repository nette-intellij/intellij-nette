package cz.juzna.intellij.nette.utils;

import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;

import java.util.HashMap;
import java.util.Map;

public class PhpPsiUtil {

	public static Map<String, PhpClass> getClassesInFile(PsiElement el) {
		Map<String, PhpClass> classesInFile = new HashMap<String, PhpClass>();
		if (el.getContainingFile() instanceof PhpFile) {
			for (PhpClass cls : com.jetbrains.php.lang.psi.PhpPsiUtil.findAllClasses((PhpFile) el.getContainingFile())) {
				classesInFile.put(cls.getFQN(), cls);
			}
		}

		return classesInFile;
	}

	public static boolean isTypeOf(PhpClass cls, String typeFqn, Map<String, PhpClass> classMap) {
		if (cls.getFQN() == null) {
			return false;
		}
		if (typeEquals(typeFqn, cls.getFQN())) {
			return true;
		}
		while (true) {
			String fqn = cls.getSuperFQN();
			if (fqn == null) {
				return false;
			}
			if (typeEquals(typeFqn, fqn)) {
				return true;
			}
			if (classMap.containsKey(fqn)) {
				cls = classMap.get(cls.getSuperFQN());
			} else {
				cls = cls.getSuperClass();
			}
			if (cls == null || cls.getFQN() == null) {
				return false;
			}
		}
	}

	private static boolean typeEquals(String type1, String type2)
	{
		type1 = type1.toLowerCase();
		type2 = type2.toLowerCase();
		if (type1.startsWith("\\")) {
			type1 = type1.substring(1);
		}
		if (type2.startsWith("\\")) {
			type2 = type2.substring(1);
		}

		return type1.equals(type2);
	}

}
