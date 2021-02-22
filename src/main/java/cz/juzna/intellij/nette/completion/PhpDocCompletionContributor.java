package cz.juzna.intellij.nette.completion;


import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.PhpPsiUtil;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.GroupStatement;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import org.jetbrains.annotations.NotNull;

public class PhpDocCompletionContributor extends CompletionContributor {

	private static PhpType presenterComponent = new PhpType().add("Nette\\Application\\UI\\PresenterComponent");

	public PhpDocCompletionContributor() {
		this.extend(CompletionType.BASIC, PlatformPatterns.or(
				PlatformPatterns.psiElement().withSuperParent(3, PhpDocComment.class),
				PlatformPatterns.psiElement().withSuperParent(2, PhpDocComment.class)
		), new PhpDocCompletionProvider());
	}

	private class PhpDocCompletionProvider extends CompletionProvider<CompletionParameters> {
		@Override
		protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result) {
			PsiElement position = parameters.getPosition().getOriginalElement();
			PhpDocComment docComment = PhpPsiUtil.getParentByCondition(position, PhpDocComment.INSTANCEOF);
			if (docComment == null) {
				return;
			}
			PhpPsiElement next = docComment.getNextPsiSibling();
			if (next instanceof GroupStatement) {
				next = next.getFirstPsiChild();
			}
			if (next == null) {
				return;
			}
			Field field = (Field) PhpPsiUtil.getChildOfType(next, PhpElementTypes.CLASS_FIELD);
			if (field == null) {
				return;
			}
			if (field.getModifier().isPublic()) {
				result.addElement(LookupElementBuilder.create("inject").withBoldness(true));
				PhpClass cls = field.getContainingClass();
				if (cls != null && presenterComponent.isConvertibleFrom(cls.getType(), PhpIndex.getInstance(field.getProject()))) {
					result.addElement(LookupElementBuilder.create("persistent").withBoldness(true));
				}
			}
		}
	}
}
