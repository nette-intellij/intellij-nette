package cz.juzna.intellij.nette.reference;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveResult;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import cz.juzna.intellij.nette.utils.ComponentUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

public class ComponentReference extends PsiReferenceBase.Poly<PsiElement> {

	public ComponentReference(@NotNull PsiElement element) {
		super(element);
	}

	@NotNull
	@Override
	public ResolveResult[] multiResolve(boolean b) {
		if (getElement().getParent().getParent() == null) {
			return new ResolveResult[0];
		}
		Method[] factoryMethods = ComponentUtil.getFactoryMethods(getElement().getParent().getParent(), true);
		Collection<ResolveResult> results = new ArrayList<ResolveResult>(factoryMethods.length);
		for (final Method method : factoryMethods) {
			results.add(new ResolveResult() {
				@Nullable
				@Override
				public PsiElement getElement() {
					return method;
				}

				@Override
				public boolean isValidResult() {
					return true;
				}
			});
		}
		ResolveResult[] result = new ResolveResult[results.size()];

		return results.toArray(result);
	}

	@NotNull
	@Override
	public Object[] getVariants() {
		return new Object[0];
	}

	@Override
	public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
		String componentName = ComponentUtil.methodToComponentName(newElementName);
		if (getElement() instanceof StringLiteralExpression && componentName != null) {
			((StringLiteralExpression) getElement()).updateText(componentName);
			return getElement();
		}

		return super.handleElementRename(newElementName);
	}

}
