package nette;

import com.intellij.codeInsight.completion.*;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.HashMap;
import com.jetbrains.php.PhpIcons;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.completion.PhpLookupElement;
import com.jetbrains.php.lang.psi.elements.MemberReference;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.jetbrains.php.lang.psi.stubs.indexes.PhpFieldIndex;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public class NetteObjectFieldsCompletionContributor extends CompletionContributor {
	PhpType nObjectType = new PhpType().add("Nette\\Object");


	public NetteObjectFieldsCompletionContributor() {
		extend(CompletionType.BASIC, PlatformPatterns.psiElement().withParent(MemberReference.class), new AbcMemberRefCompletionProvider());
	}

	@Override
	public void fillCompletionVariants(CompletionParameters parameters, CompletionResultSet result) {
		super.fillCompletionVariants(parameters, result);    //To change body of overridden methods use File | Settings | File Templates.
	}


	private class AbcMemberRefCompletionProvider extends CompletionProvider<CompletionParameters> {
		@Override
		protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext processingContext, @NotNull CompletionResultSet results) {
			PsiElement position = parameters.getPosition().getOriginalElement().getParent();

			if (position instanceof MemberReference) {
				PhpType type = ((MemberReference) position).getClassReference().getType();
				PhpIndex phpIndex = PhpIndex.getInstance(position.getProject());
				if (nObjectType.isConvertibleFrom(type, phpIndex)) {
					HashMap<String, Method> fields = FieldFinder.findMagicFields(type, phpIndex);

					// build lookup list
					for (String fieldName : fields.keySet()) {
						Method method = fields.get(fieldName);

						PhpLookupElement item = new PhpLookupElement(fieldName, PhpFieldIndex.KEY, position.getProject(), null);
//						item.lookupString = "$" + item.lookupString;
						item.typeText = method.getType().toString();
						item.icon = PhpIcons.FIELD;

						results.addElement(item);
					}
				}
			}
		}
	}


}
