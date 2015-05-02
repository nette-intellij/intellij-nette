package cz.juzna.intellij.nette.reference;

import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.PsiSearchHelper;
import com.intellij.psi.search.TextOccurenceProcessor;
import com.intellij.psi.search.UsageSearchContext;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Processor;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import cz.juzna.intellij.nette.utils.ComponentUtil;

import java.util.Arrays;


public class ComponentReferenceSearch extends QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters> {


	@Override
	public void processQuery(ReferencesSearch.SearchParameters searchParameters, final Processor<PsiReference> processor) {
		if (!(searchParameters.getElementToSearch() instanceof Method)) {
			return;
		}
		final Method method = (Method) searchParameters.getElementToSearch();
		String componentName = ComponentUtil.methodToComponentName(method.getName());
		if (componentName == null) {
			return;
		}
		PsiSearchHelper.SERVICE.getInstance(method.getProject())
				.processElementsWithWord(new TextOccurenceProcessor() {
					@Override
					public boolean execute(PsiElement psiElement, int i) {
						if (!(psiElement instanceof StringLiteralExpression)) {
							return true;
						}
						PsiElement el = psiElement.getParent().getParent();
						Method[] methods = ComponentUtil.getFactoryMethods(el);
						if (Arrays.asList(methods).contains(method)) {
							processor.process(new ComponentReference(psiElement));
						}

						return true;
					}
				}, searchParameters.getScopeDeterminedByUser(), componentName, UsageSearchContext.IN_STRINGS, true);
	}
}
