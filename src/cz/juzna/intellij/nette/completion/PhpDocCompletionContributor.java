package cz.juzna.intellij.nette.completion;


import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocPsiElement;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.PhpPsiUtil;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.GroupStatement;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;

public class PhpDocCompletionContributor extends CompletionContributor {

	public PhpDocCompletionContributor() {
		this.extend(CompletionType.BASIC, PlatformPatterns.or(
				PlatformPatterns.psiElement().withSuperParent(3, PhpDocComment.class),
				PlatformPatterns.psiElement().withSuperParent(2, PhpDocComment.class)
		), new PhpDocCompletionProvider());
	}

	private class PhpDocCompletionProvider extends CompletionProvider<CompletionParameters> {
		@Override
		protected void addCompletions(CompletionParameters parameters, ProcessingContext context, CompletionResultSet result) {
			PsiElement position = parameters.getPosition().getOriginalElement();
			PhpDocComment docComment = PhpPsiUtil.getParentByCondition(position, PhpDocComment.INSTANCEOF);
			if (docComment == null) {
				return;
			}
			PhpPsiElement next = docComment.getNextPsiSibling();
			if (next instanceof GroupStatement) {
				next = next.getFirstPsiChild();
			}
			Field field = (Field) PhpPsiUtil.getChildOfType(next, PhpElementTypes.CLASS_FIELD);
			if (field == null) {
				return;
			}
			if (field.getModifier().isPublic()) {
				result.addElement(LookupElementBuilder.create("inject").withBoldness(true));
			}
		}
	}
}
