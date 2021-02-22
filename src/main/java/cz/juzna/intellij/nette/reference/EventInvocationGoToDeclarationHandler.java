package cz.juzna.intellij.nette.reference;

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandlerBase;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import cz.juzna.intellij.nette.utils.EventUtil;
import org.jetbrains.annotations.Nullable;


public class EventInvocationGoToDeclarationHandler extends GotoDeclarationHandlerBase {
	@Nullable
	@Override
	public PsiElement getGotoDeclarationTarget(PsiElement psiElement, Editor editor) {
		if (!(psiElement instanceof LeafPsiElement) || !(psiElement.getParent() instanceof MethodReference)) {
			return null;
		}
		for (Field field : EventUtil.getEventDeclarations((MethodReference) psiElement.getParent())) {
			return field;
		}
		return null;
	}
}
