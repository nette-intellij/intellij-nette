package cz.juzna.intellij.nette.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.ui.RowIcon;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.PhpIcons;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.PhpPresentationUtil;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import cz.juzna.intellij.nette.utils.ElementValueResolver;
import cz.juzna.intellij.nette.utils.PhpIndexUtil;
import org.jetbrains.annotations.NotNull;


public class CompilerExtensionCompletionContributor extends CompletionContributor {

	private static PhpType serviceDefinition = new PhpType().add("Nette\\DI\\ServiceDefinition");

	public CompilerExtensionCompletionContributor() {
		extend(CompletionType.BASIC, PlatformPatterns.psiElement().withParent(StringLiteralExpression.class), new SetupMethodCompletionProvider());
	}

	private class SetupMethodCompletionProvider extends CompletionProvider<CompletionParameters> {

		@Override
		protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result) {
			PsiElement position = parameters.getPosition().getOriginalElement().getParent();
			if (!(position.getParent().getParent() instanceof MethodReference)) {
				return;
			}
			MethodReference methodReference = (MethodReference) position.getParent().getParent();
			PhpIndex phpIndex = PhpIndex.getInstance(position.getProject());
			if (methodReference.getName() == null
					|| !methodReference.getName().equals("addSetup")
					|| methodReference.getParameters().length == 0
					|| methodReference.getParameters()[0] != position) {
				return;
			}
			boolean ok = false;
			for (PhpClass cls : PhpIndexUtil.getClasses(methodReference.getClassReference().getType(), phpIndex)) {
				if (serviceDefinition.isConvertibleFrom(cls.getType(), phpIndex)) {
					ok = true;
					break;
				}
			}
			if (!ok) {
				return;
			}
			MethodReference setClass = methodReference;
			do {
				if (setClass.getChildren().length != 2 || !(setClass.getChildren()[0] instanceof MethodReference)) {
					return;
				}
				setClass = (MethodReference) setClass.getChildren()[0];
				if (setClass.getName() == null) {
					return;
				}
			} while (!setClass.getName().equals("setClass"));
			if (setClass.getParameters().length == 0) {
				return;
			}
			String className = ElementValueResolver.resolve(setClass.getParameters()[0]);
			if (className == null) {
				return;
			}
			for (PhpClass cls : phpIndex.getClassesByFQN(className)) {
				for (Method method : cls.getMethods()) {
					if (!method.getAccess().isPublic()) {
						return;
					}
					RowIcon icon = new RowIcon(2);
					icon.setIcon(PhpIcons.METHOD, 0);
					icon.setIcon(PhpIcons.PUBLIC, 1);
					LookupElement lookupElement = LookupElementBuilder.create(method.getName())
							.withIcon(icon)
							.withTailText(PhpPresentationUtil.formatParameters(null, method.getParameters()).toString());
					result.addElement(lookupElement);
				}
			}
		}
	}
}
