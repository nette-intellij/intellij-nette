package cz.juzna.intellij.nette.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.PhpPsiUtil;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import cz.juzna.intellij.nette.dialogs.ComponentTreePopup;

public class ViewComponentTreeAction extends AnAction {

	public ViewComponentTreeAction() {
		setEnabledInModalContext(true);
	}

	@Override
	public void actionPerformed(AnActionEvent event) {
		Editor editor = event.getData(LangDataKeys.EDITOR);
		FileEditor fileEditor = event.getData(LangDataKeys.FILE_EDITOR);
		PsiFile psiFile = event.getData(LangDataKeys.PSI_FILE);
		if (editor == null || psiFile == null || fileEditor == null) {
			return;
		}
		PsiElement el = psiFile.findElementAt(editor.getCaretModel().getOffset());
		if (el == null) {
			return;
		}
		final PhpClass cls = PhpPsiUtil.getParentByCondition(el, PhpClass.INSTANCEOF);
		if (cls == null) {
			return;
		}

		ComponentTreePopup popup = new ComponentTreePopup(fileEditor, cls, editor);
		popup.show();
	}

}
