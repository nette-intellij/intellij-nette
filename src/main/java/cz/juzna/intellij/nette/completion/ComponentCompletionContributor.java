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
import cz.juzna.intellij.nette.utils.ComponentSearcher;
import cz.juzna.intellij.nette.utils.ComponentUtil;
import cz.juzna.intellij.nette.utils.PhpIndexUtil;
import cz.juzna.intellij.nette.utils.PsiUtil;
import org.jetbrains.annotations.NotNull;

public class ComponentCompletionContributor extends CompletionContributor {

	public ComponentCompletionContributor() {
		extend(CompletionType.BASIC, PlatformPatterns.psiElement().withParent(StringLiteralExpression.class), new ComponentNameCompletionProvider());
	}

	private class ComponentNameCompletionProvider extends CompletionProvider<CompletionParameters> {

		@Override
		protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result) {
			PsiElement position = parameters.getOriginalPosition();
			position = PsiUtil.getParentAtLevel(position, 3);
			if (position == null) {
				return;
			}
			ComponentSearcher.ComponentQuery query = ComponentSearcher.createQuery(position);
			query.match(ComponentSearcher.Match.PREFIX);
			String prefix = result.getPrefixMatcher().getPrefix();
			if (prefix.contains("-")) {
				prefix = prefix.substring(0, prefix.lastIndexOf("-") + 1);
			} else {
				prefix = "";
			}
			for (Method method : ComponentSearcher.findMethods(query)) {
				String componentName = ComponentUtil.methodToComponentName(method.getName());

				LookupElementBuilder lookupElement = LookupElementBuilder.create(prefix + componentName)
						.withPresentableText(componentName);
				PhpType returnType = new PhpType();
				for (PhpClass typeCls : PhpIndexUtil.getClasses(method, method.getProject())) {
					returnType.add(typeCls.getType());
				}
				lookupElement = lookupElement.withTypeText(returnType.toString()); //TODO: use toStringRelativized
				result.addElement(lookupElement);
			}
		}
	}
}
