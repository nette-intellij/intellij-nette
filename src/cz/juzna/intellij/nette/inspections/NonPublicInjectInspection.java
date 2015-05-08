package cz.juzna.intellij.nette.inspections;

import com.intellij.codeInspection.*;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpModifierList;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;


public class NonPublicInjectInspection extends LocalInspectionTool {


	@Nullable
	@Override
	public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull InspectionManager manager, boolean isOnTheFly) {

		NonPublicFieldVisitor visitor = new NonPublicFieldVisitor(manager, isOnTheFly);
		file.accept(visitor);
		ProblemDescriptor[] problems = new ProblemDescriptor[visitor.getProblems().size()];

		return visitor.getProblems().toArray(problems);
	}

	private class NonPublicFieldVisitor extends PsiRecursiveElementWalkingVisitor {
		private final InspectionManager inspectionManager;
		private final boolean isOnTheFly;
		private Collection<ProblemDescriptor> problems = new ArrayList<ProblemDescriptor>();

		public NonPublicFieldVisitor(InspectionManager inspectionManager, boolean isOnTheFly) {
			this.inspectionManager = inspectionManager;
			this.isOnTheFly = isOnTheFly;
		}

		public Collection<ProblemDescriptor> getProblems() {
			return problems;
		}

		@Override
		public void visitElement(PsiElement element) {
			if (element instanceof Field && isInvalid((Field) element)) {
				problems.add(inspectionManager.createProblemDescriptor(element, "@inject property must be public", new NonPublicPropertyFix(), ProblemHighlightType.ERROR, isOnTheFly));
			}
			super.visitElement(element);
		}

		private boolean isInvalid(Field field) {
			return !field.getModifier().isPublic()
					&& field.getDocComment() != null
					&& field.getDocComment().getText().contains("@inject");
		}

	}

	private class NonPublicPropertyFix implements LocalQuickFix {
		@Nls
		@NotNull
		@Override
		public String getName() {
			return "Make @inject property public";
		}

		@NotNull
		@Override
		public String getFamilyName() {
			return "Nette";
		}

		@Override
		public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
			if (!(problemDescriptor.getStartElement() instanceof Field)) {
				return;
			}
			Field field = (Field) problemDescriptor.getStartElement();
			PhpPsiElement element = ((PhpPsiElement) field.getParent()).getFirstPsiChild();
			if (element == null || !(element instanceof PhpModifierList)) {
				return;
			}
			for (ASTNode node : element.getNode().getChildren(PhpTokenTypes.tsMODIFIERS)) {
				if (node.getElementType() == PhpTokenTypes.kwPRIVATE || node.getElementType() == PhpTokenTypes.kwPROTECTED) {
					node.getTreeParent().replaceChild(node, PhpPsiElementFactory.createFromText(project, PhpTokenTypes.kwPUBLIC, "public").getNode());
					break;
				}
			}

		}
	}

}
