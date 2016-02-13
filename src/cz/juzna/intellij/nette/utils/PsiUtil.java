package cz.juzna.intellij.nette.utils;


import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

public class PsiUtil {

	@Nullable
	public static PsiElement getParentAtLevel(PsiElement element, int level) {
		for (; element != null && level > 0; level--) {
			element = element.getParent();
		}
		return element;
	}

}
