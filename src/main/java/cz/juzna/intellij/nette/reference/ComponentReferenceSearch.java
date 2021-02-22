package cz.juzna.intellij.nette.reference;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.PsiSearchHelper;
import com.intellij.psi.search.UsageSearchContext;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Processor;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import cz.juzna.intellij.nette.utils.ComponentSearcher;
import cz.juzna.intellij.nette.utils.ComponentUtil;
import org.jetbrains.annotations.NotNull;

public class ComponentReferenceSearch extends QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters> {

	@Override
	public void processQuery(ReferencesSearch.@NotNull SearchParameters searchParameters, @NotNull Processor<? super PsiReference> processor) {
		if (!(searchParameters.getElementToSearch() instanceof Method)) {
			return;
		}
		final Method method = (Method) searchParameters.getElementToSearch();
		String[] componentName = new String[1];
		ApplicationManager.getApplication().runReadAction(() -> {
			componentName[0] = ComponentUtil.methodToComponentName(method.getName());
		});
		if (componentName[0] == null) {
			return;
		}

		PsiSearchHelper.getInstance(method.getProject())
				.processElementsWithWord((psiElement, i) -> {
					if (!(psiElement instanceof StringLiteralExpression)) {
						return true;
					}
					PsiElement el = psiElement.getParent().getParent();
					ComponentSearcher.ComponentQuery query = ComponentSearcher.createQuery(el);
					query.withPath();
					for (ComponentSearcher.ComponentSearchResult result : ComponentSearcher.find(query)) {
						if (result.getMethod() == method) {
							processor.process(new ComponentReference(psiElement, result.getPath()));
						}

					}

					return true;
				}, searchParameters.getScopeDeterminedByUser(), componentName[0], UsageSearchContext.IN_STRINGS, true);
	}
}
