package cz.juzna.intellij.nette.dialogs;

import com.intellij.ide.structureView.StructureViewModelBase;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase;
import com.intellij.ide.structureView.newStructureView.StructureViewComponent;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.MnemonicHelper;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.psi.PsiElement;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.tree.TreeUtil;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import cz.juzna.intellij.nette.utils.ComponentUtil;
import cz.juzna.intellij.nette.utils.PhpIndexUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.ArrayList;
import java.util.Collection;

public class ComponentTreePopup {

	private final FileEditor fileEditor;
	private final PhpClass cls;
	private final Editor editor;

	public ComponentTreePopup(FileEditor fileEditor, PhpClass cls, Editor editor) {
		this.fileEditor = fileEditor;
		this.cls = cls;
		this.editor = editor;
	}

	public void show() {
		PsiTreeElementBase<PsiElement> root = new ComponentTreeElement(cls);
		final StructureViewComponent structureView = new StructureViewComponent(fileEditor, new StructureViewModelBase(cls.getContainingFile(), editor, root), cls.getProject(), true);
		structureView.setFocusable(true);
		structureView.setAutoscrolls(false);

		MnemonicHelper.init(structureView);
		final JBPopup popup = JBPopupFactory.getInstance().createComponentPopupBuilder(structureView, (JComponent) null)
				.setTitle("Component tree")
				.setResizable(true)
				.setModalContext(false)
				.setFocusable(true)
				.setRequestFocus(true)
				.setMovable(true)
				.setBelongsToGlobalPopupStack(true)
				.setCancelKeyEnabled(true)
				.createPopup();
		popup.showInFocusCenter();
		popup.setMinimumSize(new Dimension(Math.max(structureView.getPreferredSize().width, JBUI.scale(350)) + 10, popup.getSize().height));
		SwingUtilities.windowForComponent(popup.getContent()).addWindowFocusListener(new WindowFocusListener() {
			public void windowGainedFocus(WindowEvent var1) {
			}

			public void windowLostFocus(WindowEvent var1) {
				popup.cancel();
			}
		});

		ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
			public void run() {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						structureView.getTreeBuilder().queueUpdate().doWhenDone(new Runnable() {
							public void run() {
								SwingUtilities.invokeLater(new Runnable() {
									public void run() {
										if (!popup.isDisposed()) {
											TreeUtil.ensureSelection(structureView.getTree());
											IdeFocusManager.getInstance(cls.getProject()).requestFocus(structureView.getTree(), true);
											for (int i = 1; i <= structureView.getTree().getRowCount(); i++) {
												structureView.getTree().collapseRow(i);
											}
										}
									}
								});
							}
						});
					}
				});
			}
		});
	}

	private class ComponentTreeElement extends PsiTreeElementBase<PsiElement> {

		private boolean expandClass;

		public ComponentTreeElement(PsiElement element) {
			super(element);
			expandClass = true;
		}

		public ComponentTreeElement(PsiElement element, boolean expandClass) {
			super(element);
			this.expandClass = expandClass;
		}


		@NotNull
		@Override
		public Collection<StructureViewTreeElement> getChildrenBase() {
			Collection<StructureViewTreeElement> children = new ArrayList<StructureViewTreeElement>();
			if (expandClass && getElement() instanceof PhpClass) {
				for (Method method : ComponentUtil.getFactoryMethods((PhpClass) getElement(), null)) {
					children.add(new ComponentTreeElement(method));
				}
			} else if (getElement() instanceof Method) {
				Method method = (Method) getElement();
				for (PhpClass cls : PhpIndexUtil.getClasses(method, method.getProject())) {
					children.add(new ComponentTreeElement(cls, false));
					for (Method m : ComponentUtil.getFactoryMethods(cls, null)) {
						children.add(new ComponentTreeElement(m));
					}
				}
			}

			return children;
		}

		@Nullable
		@Override
		public String getPresentableText() {
			if (getElement() instanceof PhpClass) {
				return ((PhpClass) getElement()).getPresentableFQN();
			} else if (getElement() instanceof Method) {
				return ComponentUtil.methodToComponentName(((Method) getElement()).getName());
			}
			return null;
		}

		@NotNull
		@Override
		public ItemPresentation getPresentation() {
			return super.getPresentation();
		}
	}
}
