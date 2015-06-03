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
		typeFqn = typeFqn.toLowerCase();
		if (typeFqn.startsWith("\\")) {
			typeFqn = typeFqn.substring(1);
		}
		while (true) {
			if (cls.getFQN() == null) {
				return false;
			}
			String fqn = cls.getFQN();
			if (fqn.startsWith("\\")) {
				fqn = fqn.substring(1);
			}
			if (fqn.toLowerCase().equals(typeFqn)) {
				return true;
			}
			if (classMap.containsKey(cls.getSuperFQN())) {
				cls = classMap.get(cls.getSuperFQN());
			} else {
				cls = cls.getSuperClass();
			}
			if (cls == null || cls.getFQN() == null) {
				return false;
			}
		}
	}

}
