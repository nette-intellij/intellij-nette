package cz.juzna.intellij.nette.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.ui.RowIcon;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.HashMap;
import com.jetbrains.php.PhpIcons;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.completion.PhpLookupElement;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.MemberReference;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.jetbrains.php.lang.psi.stubs.indexes.PhpFieldIndex;
import cz.juzna.intellij.nette.utils.MagicFieldsUtil;
import cz.juzna.intellij.nette.utils.PhpIndexUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class NetteObjectFieldsCompletionContributor extends CompletionContributor {
	PhpType nObjectType = new PhpType().add("Nette\\Object");


	public NetteObjectFieldsCompletionContributor() {
		extend(CompletionType.BASIC, PlatformPatterns.psiElement().withParent(MemberReference.class), new MagicFieldMemberRefCompletionProvider());
		extend(CompletionType.BASIC, PlatformPatterns.psiElement().withParent(MemberReference.class), new EventMethodMemberRefCompletionProvider());
	}


	private class MagicFieldMemberRefCompletionProvider extends CompletionProvider<CompletionParameters> {
		@Override
		protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext processingContext, @NotNull CompletionResultSet results) {
			PsiElement position = parameters.getPosition().getOriginalElement().getParent();

			if (!(position instanceof MemberReference)) {
				return;
			}

			MemberReference classRef = (MemberReference) position;

			HashMap<String, Collection<Method>> fields = MagicFieldsUtil.findMagicFields(classRef);

			for (String fieldName : fields.keySet()) {
				PhpLookupElement item = new PhpLookupElement(fieldName, PhpFieldIndex.KEY, position.getProject(), null);

				PhpType fieldType = MagicFieldsUtil.extractTypeFromMethodTypes(fields.get(fieldName));

				if (fieldType != null) {
					item.typeText = fieldType.toStringRelativized(classRef.getNamespaceName());
				}

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

			HashMap<String, Field> eventFields = MagicFieldsUtil.findEventFields(type, phpIndex);
			Set<String> classMethods = new HashSet<String>();
			for (PhpClass cls : PhpIndexUtil.getClasses(((MemberReference) position).getClassReference(), position.getProject())) {
				for (Method method : cls.getMethods()) {
					classMethods.add(method.getName());
				}
			}
			for (Map.Entry fieldEntry : eventFields.entrySet()) {
				Field field = (Field) fieldEntry.getValue();
				if (classMethods.contains(field.getName())) {
					continue;
				}
				RowIcon icon = new RowIcon(2);
				icon.setIcon(PhpIcons.METHOD, 0);
				icon.setIcon(PhpIcons.PUBLIC, 1);
				LookupElementBuilder lookupElement = LookupElementBuilder.create(field.getName() + "()")
						.withTypeText("void")
						.withIcon(icon)
						.withInsertHandler(new InsertHandler<LookupElement>() {
							@Override
							public void handleInsert(InsertionContext insertionContext, LookupElement lookupElement) {
								insertionContext.getEditor().getCaretModel().moveCaretRelatively(-1, 0, false, false, true);
							}
						});


				results.addElement(lookupElement);
			}
		}
	}


}
