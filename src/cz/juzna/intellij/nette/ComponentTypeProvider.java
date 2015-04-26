package cz.juzna.intellij.nette;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider2;
import cz.juzna.intellij.nette.utils.ElementValueResolver;
import cz.juzna.intellij.nette.utils.PhpIndexUtil;
import cz.juzna.intellij.nette.utils.StringUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;


public class ComponentTypeProvider implements PhpTypeProvider2 {

	private static PhpType container = new PhpType().add("Nette\\ComponentModel\\Container");

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

		PhpType type = null;
		String componentName = null;

		if (el instanceof ArrayAccessExpression) {
			ArrayIndex index = ((ArrayAccessExpression) el).getIndex();
			if (index == null || !(el.getFirstChild() instanceof PhpTypedElement)) {
				return null;
			}
			componentName = ElementValueResolver.resolve(index.getValue());
			if (componentName == null) {
				return null;
			}
			type = ((PhpTypedElement) el.getFirstChild()).getType();

		} else if (el instanceof MethodReference) {
			MethodReference methodRef = (MethodReference) el;
			if (methodRef.getClassReference() == null
					|| methodRef.getName() == null
					|| !methodRef.getName().equals("getComponent")
					|| methodRef.getParameters().length != 1) {
				return null;
			}
			componentName = ElementValueResolver.resolve(methodRef.getParameters()[0]);
			if (componentName == null) {
				return null;
			}
			type = methodRef.getClassReference().getType();
		}
		if (type == null) {
			return null;
		}
		for (PhpClass currentClass : PhpIndexUtil.getClasses(type, PhpIndex.getInstance(el.getProject()))) {
			if (!isSubclassOfContainer(currentClass)) {
				continue;
			}
			String method = "createComponent" + StringUtil.upperFirst(componentName);
			Method m = currentClass.findMethodByName(method);
			if (m != null) {
				return currentClass.getFQN() + "." + method;
			}
		}

		return null;
	}

	@Override
	public Collection<? extends PhpNamedElement> getBySignature(String s, Project project) {

		int dot = s.lastIndexOf('.');
		String method = s.substring(dot + 1);

		Collection<PhpClass> classes = PhpIndex.getInstance(project).getClassesByFQN(s.substring(0, dot));
		Collection<PhpNamedElement> result = new ArrayList<PhpNamedElement>();
		for (PhpClass cls : classes) {
			if (!isSubclassOfContainer(cls)) {
				continue;
			}
			Method m = cls.findMethodByName(method);
			if (m != null) {
				result.addAll(PhpIndexUtil.getClasses(m.getType(), PhpIndex.getInstance(project)));
			}
		}
		return result;
	}

	private boolean isSubclassOfContainer(PhpClass checkedClass) {
		while ((checkedClass = checkedClass.getSuperClass()) != null) {
			if (container.isConvertibleFrom(checkedClass.getType(), PhpIndex.getInstance(checkedClass.getProject()))) {
				return true;
			}
		}

		return false;
	}

}

