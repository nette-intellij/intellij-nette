package cz.juzna.intellij.nette.utils;

import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.ArrayAccessExpression;
import com.jetbrains.php.lang.psi.elements.ArrayIndex;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpTypedElement;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;


public class ComponentSearcher {

	public enum Match {EXACT, PREFIX}

	private static PhpType container = new PhpType().add("Nette\\ComponentModel\\Container");
	public static String factoryMethodPrefix = "createComponent";

	public static Collection<ComponentSearchResult> find(ComponentQuery query) {
		return findInternal(query, "");
	}

	public static Collection<Method> findMethods(ComponentQuery query) {
		Collection<ComponentSearchResult> result = find(query);
		Collection<Method> methods = new ArrayList<Method>(result.size());
		for (ComponentSearchResult searchResult : result) {
			methods.add(searchResult.getMethod());
		}
		return methods;
	}

	private static Collection<ComponentSearchResult> findInternal(ComponentQuery query, String prefix) {
		if (query.getClasses().isEmpty()) {
			return Collections.emptyList();
		}
		if (query.hasSubQuery() || query.getMatchMode() == Match.EXACT) {
			return getByName(query, prefix);
		}

		return getByPrefix(query, prefix);
	}

	private static Collection<ComponentSearchResult> getByName(ComponentQuery query, String prefix) {
		Collection<ComponentSearchResult> result1 = new ArrayList<ComponentSearchResult>();

		String methodName = factoryMethodPrefix + StringUtil.upperFirst(query.getPartName());
		String path = prefix + (prefix.isEmpty() ? "" : "-") + query.getPartName();
		for (PhpClass cls : query.getClasses()) {
			if (!isContainer(cls)) {
				continue;
			}
			Method m = query.isOnlyOwn() ? cls.findOwnMethodByName(methodName) : cls.findMethodByName(methodName);
			if (m != null) {
				result1.add(new ComponentSearchResult(path, m));
			}
		}
		if (!query.hasSubQuery()) {
			return result1;
		}
		Collection<ComponentSearchResult> result2 = new ArrayList<ComponentSearchResult>();
		if (query.isWithPath()) {
			result2.addAll(result1);
		}
		for (ComponentSearchResult searchResult : result1) {
			Method method = searchResult.getMethod();
			Collection<PhpClass> classes = PhpIndexUtil.getClasses(method, method.getProject());
			result2.addAll(findInternal(query.getSubQuery(classes), path));
		}
		return result2;
	}

	private static Collection<ComponentSearchResult> getByPrefix(ComponentQuery query, String prefix) {
		Collection<ComponentSearchResult> result = new ArrayList<ComponentSearchResult>();
		String path = prefix + (prefix.isEmpty() ? "" : "-") + query.getPartName();
		for (PhpClass cls : query.getClasses()) {
			if (!isContainer(cls)) {
				continue;
			}
			for (Method method : query.isOnlyOwn() ? Arrays.asList(cls.getOwnMethods()) : cls.getMethods()) {
				if (method.getName().startsWith(factoryMethodPrefix + StringUtil.upperFirst(query.getPartName())) && !method.getName().equals(factoryMethodPrefix)) {
					result.add(new ComponentSearchResult(path, method));
				}
			}

		}
		return result;
	}


	public static ComponentQuery createQuery(PsiElement el) {
		String componentName;
		Collection<PhpClass> classes;
		if (el instanceof ArrayAccessExpression) {
			ArrayIndex index = ((ArrayAccessExpression) el).getIndex();
			if (index == null || !(el.getFirstChild() instanceof PhpTypedElement)) {
				return new ComponentQuery("", Collections.<PhpClass>emptyList());
			}
			componentName = ElementValueResolver.resolve(index.getValue());
			classes = PhpIndexUtil.getClasses((PhpTypedElement) el.getFirstChild(), el.getProject());
		} else if (el instanceof MethodReference) {
			MethodReference methodRef = (MethodReference) el;
			if (methodRef.getClassReference() == null
					|| methodRef.getName() == null
					|| !methodRef.getName().equals("getComponent")
					|| methodRef.getParameters().length != 1) {
				return new ComponentQuery("", Collections.<PhpClass>emptyList());
			}
			componentName = ElementValueResolver.resolve(methodRef.getParameters()[0]);
			classes = PhpIndexUtil.getClasses(methodRef.getClassReference(), methodRef.getProject());
		} else {
			return new ComponentQuery("", Collections.<PhpClass>emptyList());
		}


		return new ComponentQuery(componentName, classes);
	}

	private static boolean isContainer(PhpClass csl) {
		return container.isConvertibleFrom(csl.getType(), PhpIndex.getInstance(csl.getProject()));
	}

	public static class ComponentQuery {

		private final Collection<PhpClass> classes;

		private String remainingName;

		private boolean onlyOwn = false;

		private Match matchMode = Match.EXACT;

		private boolean path = false;

		public ComponentQuery(PhpClass cls) {
			this("", Collections.singletonList(cls));
			this.match(Match.PREFIX);
		}

		public ComponentQuery(String name, Collection<PhpClass> classes) {
			this.remainingName = name;
			this.classes = classes;
		}

		public ComponentQuery filterOwn() {
			this.onlyOwn = true;

			return this;
		}

		public ComponentQuery match(Match matchMode) {
			this.matchMode = matchMode;

			return this;
		}

		public ComponentQuery withPath() {
			this.path = true;

			return this;
		}

		public String getPartName() {
			int pos = this.remainingName.indexOf("-");

			return pos == -1 ? this.remainingName : this.remainingName.substring(0, pos);
		}

		public boolean isOnlyOwn() {
			return onlyOwn;
		}

		public Match getMatchMode() {
			return matchMode;
		}

		public boolean hasSubQuery() {
			return this.remainingName.contains("-");
		}

		public ComponentQuery getSubQuery(Collection<PhpClass> classes) {
			int pos = this.remainingName.indexOf("-");
			if (pos == -1) {
				return null;
			}

			ComponentQuery query = new ComponentQuery(remainingName.substring(pos + 1), classes);
			if (this.path) {
				query.withPath();
			}
			if (this.onlyOwn) {
				query.filterOwn();
			}
			query.match(this.matchMode);

			return query;
		}

		public boolean isWithPath() {
			return path;
		}

		public Collection<PhpClass> getClasses() {
			return classes;
		}
	}

	public static class ComponentSearchResult {

		private final String path;
		private final Method method;

		public ComponentSearchResult(String path, Method method) {

			this.path = path;
			this.method = method;
		}

		public Method getMethod() {
			return method;
		}

		public String getPath() {
			return path;
		}
	}

}
