package cz.juzna.intellij.nette.typeProvider;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.ArrayAccessExpression;
import com.jetbrains.php.lang.psi.elements.ArrayIndex;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import com.jetbrains.php.lang.psi.elements.PhpTypedElement;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider2;
import cz.juzna.intellij.nette.utils.ComponentUtil;
import cz.juzna.intellij.nette.utils.ElementValueResolver;
import cz.juzna.intellij.nette.utils.PhpIndexUtil;
import cz.juzna.intellij.nette.utils.PhpPsiUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;


public class ComponentTypeProvider implements PhpTypeProvider2 {

	final static String SEPARATOR = "\u0180";
	final static String TYPE_SEPARATOR = "\u0181";

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
		String componentName;
		PhpType type;
		if (el instanceof ArrayAccessExpression) {
			if (PhpPsiUtil.isLocallyResolvableType(el)) {
				return null;
			}
			ArrayIndex index = ((ArrayAccessExpression) el).getIndex();
			if (index == null || !(el.getFirstChild() instanceof PhpTypedElement)) {
				return null;
			}
			componentName = ElementValueResolver.resolve(index.getValue());
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
			type = methodRef.getClassReference().getType();
		} else {
			return null;
		}

		return componentName + SEPARATOR + type.toString().replaceAll("\\|", TYPE_SEPARATOR);
	}

	@Override
	public Collection<? extends PhpNamedElement> getBySignature(String s, Project project) {

		String[] parts = s.split(SEPARATOR, 2);
		if (parts.length != 2) {
			return Collections.emptyList();
		}
		String componentName = parts[0];
		Collection<PhpClass> classes = PhpIndexUtil.getByType(parts[1].split(TYPE_SEPARATOR), PhpIndex.getInstance(project));
		Collection<PhpNamedElement> result = new ArrayList<PhpNamedElement>();
		for (PhpClass cls : classes) {
			if (!ComponentUtil.isContainer(cls)) {
				continue;
			}
			result.addAll(ComponentUtil.getFactoryMethodsByName(cls, componentName, false));
		}
		return result;
	}

}

