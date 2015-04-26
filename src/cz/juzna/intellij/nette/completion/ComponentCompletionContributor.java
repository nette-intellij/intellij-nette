package cz.juzna.intellij.nette.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import cz.juzna.intellij.nette.utils.ComponentUtil;
import cz.juzna.intellij.nette.utils.PhpIndexUtil;
import cz.juzna.intellij.nette.utils.StringUtil;
import org.jetbrains.annotations.NotNull;

public class ComponentCompletionContributor extends CompletionContributor {

	public ComponentCompletionContributor() {
		extend(CompletionType.BASIC, PlatformPatterns.psiElement().withParent(StringLiteralExpression.class), new ComponentNameCompletionProvider());
	}

	private class ComponentNameCompletionProvider extends CompletionProvider<CompletionParameters> {

		@Override
		protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result) {
			PsiElement position = parameters.getPosition().getOriginalElement().getParent();
			PhpType type;
			if (position.getParent() instanceof ArrayIndex) {
				ArrayAccessExpression expr = (ArrayAccessExpression) position.getParent().getParent();
				if (!(expr.getValue() instanceof PhpTypedElement)) {
					return;
				}
				type = ((PhpTypedElement) expr.getValue()).getType();

			} else if (position.getParent().getParent() instanceof MethodReference) {
				MethodReference methodReference = (MethodReference) position.getParent().getParent();
				if (methodReference.getClassReference() == null) {
					return;
				}
				type = methodReference.getClassReference().getType();
			} else {
				return;
			}
			for (PhpClass cls : PhpIndexUtil.getClasses(type, PhpIndex.getInstance(position.getProject()))) {
				if (!ComponentUtil.isContainer(cls)) {
					continue;
				}
				for (Method method : cls.getMethods()) {
					if (!method.getName().startsWith("createComponent")) {
						continue;
					}
					String componentName = StringUtil.lowerFirst(method.getName().substring("createComponent".length()));
					LookupElementBuilder lookupElement = LookupElementBuilder.create(componentName);
					lookupElement = lookupElement.withTypeText(method.getType().toStringResolved());
					result.addElement(lookupElement);
				}
			}
		}
	}
}
