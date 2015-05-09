package cz.juzna.intellij.nette.typeProvider;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider2;
import cz.juzna.intellij.nette.utils.ComponentUtil;
import cz.juzna.intellij.nette.utils.PhpIndexUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;


public class ComponentTypeProvider implements PhpTypeProvider2 {

	@Override
	public char getKey() {
		return '\u0222';
	}

	@Nullable
	@Override
	public String getType(PsiElement el) {
		if (DumbService.getInstance(el.getProject()).isDumb()) {
			return null;
		}
		for (Method method : ComponentUtil.getFactoryMethods(el, true)) {
			if (method.getContainingClass() != null) {
				return method.getContainingClass().getFQN() + "." + method.getName();
			}
		}
		return null;
	}

	@Override
	public Collection<? extends PhpNamedElement> getBySignature(String s, Project project) {

		int dot = s.lastIndexOf('.');
		String method = s.substring(dot + 1);

		Collection<PhpClass> classes = PhpIndex.getInstance(project).getAnyByFQN(s.substring(0, dot));
		Collection<PhpNamedElement> result = new ArrayList<PhpNamedElement>();
		for (PhpClass cls : classes) {
			if (!ComponentUtil.isContainer(cls) && !cls.isTrait()) {
				continue;
			}
			Method m = cls.findMethodByName(method);
			if (m != null) {
				result.addAll(PhpIndexUtil.getClasses(m.getType(), PhpIndex.getInstance(project)));
			}
		}
		return result;
	}

}

