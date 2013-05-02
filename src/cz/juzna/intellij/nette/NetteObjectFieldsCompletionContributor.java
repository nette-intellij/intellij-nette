package cz.juzna.intellij.nette;

import com.intellij.codeInsight.completion.*;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.ui.RowIcon;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.HashMap;
import com.jetbrains.php.PhpIcons;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.completion.PhpLookupElement;
import com.jetbrains.php.completion.insert.PhpFunctionInsertHandler;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.MemberReference;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.jetbrains.php.lang.psi.stubs.indexes.PhpFieldIndex;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 *
 */
public class NetteObjectFieldsCompletionContributor extends CompletionContributor {
	PhpType nObjectType = new PhpType().add("Nette\\Object");


	public NetteObjectFieldsCompletionContributor() {
		extend(CompletionType.BASIC, PlatformPatterns.psiElement().withParent(MemberReference.class), new MagicFieldMemberRefCompletionProvider());
		extend(CompletionType.BASIC, PlatformPatterns.psiElement().withParent(MemberReference.class), new EventMethodMemberRefCompletionProvider());
	}

	@Override
	public void fillCompletionVariants(CompletionParameters parameters, CompletionResultSet result) {
		super.fillCompletionVariants(parameters, result);    //To change body of overridden methods use File | Settings | File Templates.
	}


	private class MagicFieldMemberRefCompletionProvider extends CompletionProvider<CompletionParameters> {
		@Override
		protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext processingContext, @NotNull CompletionResultSet results) {
			PsiElement position = parameters.getPosition().getOriginalElement().getParent();

			if (!(position instanceof MemberReference)) {
				return;
			}

			PhpType type = ((MemberReference) position).getClassReference().getType();
			PhpIndex phpIndex = PhpIndex.getInstance(position.getProject());

			if (!nObjectType.isConvertibleFrom(type, phpIndex)) {
				return;
			}

			HashMap<String, Method> magicFields = FieldFinder.findMagicFields(type, phpIndex);

			// build lookup list
			for (String fieldName : magicFields.keySet()) {
				Method method = magicFields.get(fieldName);

				PhpLookupElement item = new PhpLookupElement(fieldName, PhpFieldIndex.KEY, position.getProject(), null);
//					item.lookupString = "$" + item.lookupString;
				item.typeText = method.getType().toString();

				RowIcon icon = new RowIcon(2);
				icon.setIcon(PhpIcons.FIELD, 0);
				icon.setIcon(PhpIcons.PUBLIC, 1);
				item.icon = icon;

				results.addElement(item);
			}
		}
	}

	private class EventMethodMemberRefCompletionProvider extends CompletionProvider<CompletionParameters> {
		@Override
		protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext processingContext, @NotNull CompletionResultSet results) {
			PsiElement position = parameters.getPosition().getOriginalElement().getParent();

			if (!(position instanceof MemberReference)) {
				return;
			}

			PhpType type = ((MemberReference) position).getClassReference().getType();
			PhpIndex phpIndex = PhpIndex.getInstance(position.getProject());

			if (!nObjectType.isConvertibleFrom(type, phpIndex)) {
				return;
			}

			HashMap<String, Field> eventFields = FieldFinder.findEventFields(type, phpIndex);

			for (Map.Entry fieldEntry : eventFields.entrySet()) {
				Field field = (Field) fieldEntry.getValue();

				PhpLookupElement item = new PhpLookupElement(field.getName(), PhpFieldIndex.KEY, position.getProject(), null);

				item.typeText = "void";
				item.handler = PhpFunctionInsertHandler.getInstance();
				item.bold = true;
				item.tailText = "()";

				RowIcon icon = new RowIcon(2);
				icon.setIcon(PhpIcons.METHOD, 0);
				icon.setIcon(PhpIcons.PUBLIC, 1);
				item.icon = icon;

				results.addElement(item);
			}
		}
	}


}
