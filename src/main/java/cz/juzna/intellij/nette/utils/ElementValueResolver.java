package cz.juzna.intellij.nette.utils;

import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.Nullable;


public class ElementValueResolver {

	private PsiElement element;

	private boolean indexAccessAllowed = true;

	public ElementValueResolver(PsiElement element) {
		super();
		this.element = element;
	}

	public void disallowIndexAccess()
	{
		indexAccessAllowed = false;
	}

	@Nullable
	public String resolve() {
		try {
			return doResolve(this.element);
		} catch (UnresolvableValueException e) {
			return null;
		}
	}

	public static String resolve(PsiElement element) {
		return (new ElementValueResolver(element)).resolve();
	}

	public static String resolveWithoutIndex(PsiElement element)
	{
		ElementValueResolver resolver = new ElementValueResolver(element);
		resolver.disallowIndexAccess();

		return resolver.resolve();
	}

	private String doResolve(PsiElement element) throws UnresolvableValueException {
		if (element instanceof StringLiteralExpression) {
			return ((StringLiteralExpression) element).getContents();
		} else if (element instanceof BinaryExpression && element.getNode().getElementType().equals(PhpElementTypes.CONCATENATION_EXPRESSION)) {
			BinaryExpression binaryExpression = (BinaryExpression) element;

			return doResolve(binaryExpression.getLeftOperand()) + doResolve(binaryExpression.getRightOperand());
		} else if (element instanceof ClassConstantReference) {
			String result = tryResolveClassConstant((ClassConstantReference) element);
			if (result != null) {
				return result;
			}
		}
		throw new UnresolvableValueException();

	}

	private String tryResolveClassConstant(ClassConstantReference constantReference)
	{
		ClassReference classReference = (ClassReference) constantReference.getClassReference();
		if (classReference == null || !(constantReference.getLastChild() instanceof LeafPsiElement)) {
			return null;
		}
		String constantName = constantReference.getLastChild().getText();
		if (constantName.equals("class")) {
			return classReference.getFQN();
		}
		if (!indexAccessAllowed) {
			return null;
		}
		for (PhpClass phpClass : PhpIndexUtil.getClasses(classReference, classReference.getProject())) {
			Field constant = phpClass.findFieldByName(constantName, true);
			if (constant == null || !constant.isConstant()) {
				continue;
			}
			try {
				return doResolve(constant.getDefaultValue());
			} catch (UnresolvableValueException e) {
			}
		}
		return null;
	}

	private class UnresolvableValueException extends Exception {
	}
}

