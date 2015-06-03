package cz.juzna.intellij.nette.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import cz.juzna.intellij.nette.utils.ClassFinder;
import cz.juzna.intellij.nette.utils.ComponentUtil;
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
			if (position.getParent() == null || position.getParent().getParent() == null) {
				return;
			}
			for (Method method : ComponentUtil.getFactoryMethods(position.getParent().getParent())) {
				String componentName = StringUtil.lowerFirst(method.getName().substring("createComponent".length()));
				LookupElementBuilder lookupElement = LookupElementBuilder.create(componentName);
				PhpType returnType = new PhpType();
				for (PhpClass typeCls : ClassFinder.getFromTypedElement(method)) {
					returnType.add(typeCls.getType());
				}
				lookupElement = lookupElement.withTypeText(returnType.toString()); //TODO: use toStringRelativized
				result.addElement(lookupElement);
			}
		}
	}
}
