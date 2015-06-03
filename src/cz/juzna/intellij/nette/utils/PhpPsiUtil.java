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
}
