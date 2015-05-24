package cz.juzna.intellij.nette.inspections;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.PhpPsiUtil;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpReturn;
import com.jetbrains.php.lang.psi.elements.PhpTypedElement;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import cz.juzna.intellij.nette.utils.ComponentUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;


public class CreateComponentReturnFormTypeInspection extends LocalInspectionTool {

	private static PhpType form = new PhpType().add("Nette\\Forms\\Form");
	private static PhpType uiForm = new PhpType().add("Nette\\Application\\UI\\Form");

	@Nullable
	@Override
	public String getStaticDescription() {
		return "Using Nette\\Forms\\Form in the presenter may not work properly. You should probably use Nette\\Application\\UI\\Form instead.";
	}

	@NotNull
	@Override
	public String getShortName() {
		return "CreateComponentReturnFormType";
	}

	@Nullable
	@Override
	public ProblemDescriptor[] checkFile(PsiFile file, InspectionManager manager, boolean isOnTheFly) {
		if (!(file instanceof PhpFile)) {
			return null;
		}
		Collection<PhpClass> classes = PhpPsiUtil.findAllClasses((PhpFile) file);
		Collection<Method> invalidReturnForms = new ArrayList<Method>();
		for (PhpClass cls : classes) {
			for (final Method method : ComponentUtil.getFactoryMethods(cls, null, true)) {
				InvalidCreateComponentMethodVisitor visitor = new InvalidCreateComponentMethodVisitor();
				method.accept(visitor);
				if (visitor.isInvalid()) {
					invalidReturnForms.add(method);
				}
			}
		}

		Collection<ProblemDescriptor> problems = new ArrayList<ProblemDescriptor>(invalidReturnForms.size());
		for (Method method : invalidReturnForms) {
			problems.add(manager.createProblemDescriptor(method, "Method should return \"Nette\\Application\\UI\\Form\" component.", true, ProblemHighlightType.WEAK_WARNING, isOnTheFly));
		}
		ProblemDescriptor[] problemsArray = new ProblemDescriptor[problems.size()];

		return problems.toArray(problemsArray);
	}



	private class InvalidCreateComponentMethodVisitor extends PsiRecursiveElementWalkingVisitor {
		private boolean invalid = false;

		@Override
		public void visitElement(PsiElement element) {
			if (element instanceof PhpReturn) {
				PsiElement el = ((PhpReturn) element).getArgument();
				if (el instanceof PhpTypedElement) {
					PhpType type = ((PhpTypedElement) el).getType();
					PhpIndex phpIndex = PhpIndex.getInstance(el.getProject());
					if (form.isConvertibleFrom(type, phpIndex) && !uiForm.isConvertibleFrom(type, phpIndex)) {
						invalid = true;
					}
				}
			} else {
				super.visitElement(element);
			}
		}

		public boolean isInvalid() {
			return invalid;
		}
	}

}
