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
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider4;
import cz.juzna.intellij.nette.utils.ComponentSearcher;
import cz.juzna.intellij.nette.utils.ElementValueResolver;
import cz.juzna.intellij.nette.utils.PhpIndexUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;


public class ComponentTypeProvider implements PhpTypeProvider4
{

	private final static String SEPARATOR = "\u0180";

	@Override
	public char getKey() {
		return '\u0222';
	}

	@Nullable
	@Override
	public PhpType getType(PsiElement el) {
		if (DumbService.getInstance(el.getProject()).isDumb()) {
			return null;
		}
		String componentName;
		PhpType type;
		if (el instanceof ArrayAccessExpression) {
			ArrayIndex index = ((ArrayAccessExpression) el).getIndex();
			if (index == null || !(el.getFirstChild() instanceof PhpTypedElement)) {
				return null;
			}
			componentName = ElementValueResolver.resolveWithoutIndex(index.getValue());
			type = ((PhpTypedElement) el.getFirstChild()).getType();
		} else if (el instanceof MethodReference) {
			MethodReference methodRef = (MethodReference) el;
			if (methodRef.getClassReference() == null
					|| methodRef.getName() == null
					|| !methodRef.getName().equals("getComponent")
					|| methodRef.getParameters().length != 1) {
				return null;
			}
			componentName = ElementValueResolver.resolveWithoutIndex(methodRef.getParameters()[0]);
			type = methodRef.getClassReference().getType();
		} else {
			return null;
		}
		if (componentName == null) {
			return null;
		}
		PhpType resultType = new PhpType();
		for (String typePart : type.getTypes()) {
			resultType.add("#" + getKey() + componentName + SEPARATOR + typePart);
		}
		return resultType;
	}

	@Override
	public @Nullable PhpType complete(String s, Project project) {
		return null;
	}

	@Override
	public Collection<? extends PhpNamedElement> getBySignature(String expression, Set<String> visited, int depth, Project project)
	{
		String[] parts = expression.split(SEPARATOR, 2);
		if (parts.length != 2) {
			return Collections.emptyList();
		}
		String componentName = parts[0];
		Collection<PhpClass> classes = PhpIndexUtil.getByType((new PhpType()).add(parts[1]), PhpIndex.getInstance(project));
		return ComponentSearcher.findMethods(new ComponentSearcher.ComponentQuery(componentName, classes));
	}

}

