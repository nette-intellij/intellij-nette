package cz.juzna.intellij.nette.actions;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.actions.CodeInsightAction;
import com.intellij.lang.LanguageCodeInsightActionHandler;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import cz.juzna.intellij.nette.utils.ClassFinder;
import cz.juzna.intellij.nette.utils.ListenerGeneratorUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;


public class GenerateInlineListenerAction extends CodeInsightAction {

	private static LanguageCodeInsightActionHandler HANDLER = new GenerateInlineListenerHandler();

	@Override
	protected boolean isValidForFile(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
		return !DumbService.getInstance(file.getProject()).isDumb() &&
				file.getLanguage().is(PhpLanguage.INSTANCE) && HANDLER.isValidFor(editor, file);
	}

	@NotNull
	@Override
	protected CodeInsightActionHandler getHandler() {
		return HANDLER;
	}

	private static class GenerateInlineListenerHandler implements LanguageCodeInsightActionHandler {
		@Override
		public boolean isValidFor(Editor editor, PsiFile psiFile) {
			PsiElement el = psiFile.findElementAt(editor.getCaretModel().getOffset());

			return getField(el) != null;
		}

		@Override
		public void invoke(@NotNull final Project project, @NotNull Editor editor, @NotNull final PsiFile psiFile) {
			PsiElement el = psiFile.findElementAt(editor.getCaretModel().getOffset());

			Field field = getField(el);
			if (field == null) {
				return;
			}
			String method = ListenerGeneratorUtil.createListenerTemplate(field, el, null);
			final PhpPsiElement fc = PhpPsiElementFactory.createFromText(project, Function.class, method);

			final AssignmentExpression expr = getAssignment(el);
			if (expr == null) {
				return;
			}
			ApplicationManager.getApplication().runWriteAction(new Runnable() {
				@Override
				public void run() {
					expr.getLastChild().replace(fc);
					PsiElement el = expr.getLastChild();
					CodeStyleManager.getInstance(project)
							.reformatText(psiFile, el.getTextOffset(), el.getTextOffset() + el.getTextLength());
				}
			});
		}

		@Override
		public boolean startInWriteAction() {
			return false;
		}

		private static Field getField(PsiElement el) {
			FieldReference ref = getFieldReference(el);
			if (ref == null) {
				return null;
			}
			if (ref.getName() == null || !ref.getName().startsWith("on") || ref.getClassReference() == null) {
				return null;
			}
			PhpIndex phpIndex = PhpIndex.getInstance(el.getProject());
			Collection<PhpClass> classes = ClassFinder.getFromMemberReference(ref);
			if (classes.size() > 0) {
				return classes.iterator().next().findFieldByName(ref.getName(), false);
			}
			return null;
		}

		private static FieldReference getFieldReference(PsiElement el) {
			AssignmentExpression expr = getAssignment(el);

			if (expr == null ||
					!(expr.getFirstChild() instanceof ArrayAccessExpression)
					|| !(expr.getFirstChild().getFirstChild() instanceof FieldReference)
					) {
				return null;
			}
			return (FieldReference) expr.getFirstChild().getFirstChild();
		}

		private static AssignmentExpression getAssignment(PsiElement el) {
			if (el instanceof LeafPsiElement && el.getText().equals(";")
					&& el.getPrevSibling() instanceof PsiWhiteSpace
					&& el.getPrevSibling().getPrevSibling() instanceof AssignmentExpression) {
				return (AssignmentExpression) el.getPrevSibling().getPrevSibling();
			}
			if (el instanceof PsiWhiteSpace
					&& el.getPrevSibling() instanceof Statement
					&& el.getPrevSibling().getFirstChild() instanceof AssignmentExpression
					&& el.getPrevSibling().getFirstChild().getLastChild() instanceof PsiErrorElement) {
				return (AssignmentExpression) el.getPrevSibling().getFirstChild();
			}
			return null;
		}
	}
}
