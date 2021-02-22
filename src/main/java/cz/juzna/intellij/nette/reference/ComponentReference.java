package cz.juzna.intellij.nette.reference;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveResult;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import cz.juzna.intellij.nette.utils.ComponentSearcher;
import cz.juzna.intellij.nette.utils.ComponentUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

public class ComponentReference extends PsiReferenceBase.Poly<PsiElement> {

	private final String path;

	public ComponentReference(@NotNull PsiElement element, String path) {
		super(element, true);
		this.path = path;
	}

	@NotNull
	@Override
	public ResolveResult[] multiResolve(boolean b) {
		PsiElement el = getElement().getParent().getParent();
		if (el == null) {
			return new ResolveResult[0];
		}
		ComponentSearcher.ComponentQuery query = ComponentSearcher.createQuery(el);
		query.withPath();
		Collection<ComponentSearcher.ComponentSearchResult> searchResults = ComponentSearcher.find(query);
		Collection<ResolveResult> results = new ArrayList<ResolveResult>(searchResults.size());
		for (final ComponentSearcher.ComponentSearchResult searchResult : searchResults) {
			if (!searchResult.getPath().equals(path)) {
				continue;
			}
			results.add(new ResolveResult() {
				@Nullable
				@Override
				public PsiElement getElement() {
					return searchResult.getMethod();
				}

				@Override
				public boolean isValidResult() {
					return true;
				}
			});
		}
		ResolveResult[] result = new ResolveResult[results.size()];

		return results.toArray(result);
	}

	@Override
	public TextRange getRangeInElement() {
		return new TextRange(path.contains("-") ? path.lastIndexOf("-") + 2 : 1, path.length() + 1);
	}

	@NotNull
	@Override
	public Object[] getVariants() {
		return new Object[0];
	}

	@Override
	public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
		String componentName = ComponentUtil.methodToComponentName(newElementName);
		if (getElement() instanceof StringLiteralExpression && componentName != null) {
			StringLiteralExpression stringLiteral = (StringLiteralExpression) getElement();
			TextRange range = getRangeInElement();
			String name = stringLiteral.getContents();
			name = (range.getStartOffset() > 1 ? name.substring(0, range.getStartOffset() - 1) : "")
					+ componentName
					+ name.substring(range.getEndOffset() - 1);
			stringLiteral.updateText(name);
			return getElement();
		}

		return super.handleElementRename(newElementName);
	}

}
