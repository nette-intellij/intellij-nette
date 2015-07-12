package cz.juzna.intellij.nette.typeProvider;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.FieldReference;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider2;
import cz.juzna.intellij.nette.utils.MagicFieldsUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;


public class NetteObjectFieldsTypeProvider implements PhpTypeProvider2 {
	final static String SEPARATOR = "\u0180";

	private Collection<FieldReference> visited = new HashSet<FieldReference>();

	@Override
	public char getKey() {
		return '\u0223';
	}

	@Nullable
	@Override
	public String getType(PsiElement e) {
		if (DumbService.getInstance(e.getProject()).isDumb()) {
			return null;
		}
		if (!(e instanceof FieldReference)) {
			return null;
		}
		FieldReference field = (FieldReference) e;
		if (field.getClassReference() == null) {
			return null;
		}
		if (visited.contains(field)) {
			return null;
		}
		visited.add(field);
		Collection<Method> getters;
		try {
			getters = MagicFieldsUtil.findGetters(field);
		} finally {
			visited.remove(field);
		}
		if (getters.isEmpty()) {
			return null;
		}
		StringBuilder signature = new StringBuilder();
		for (Method method : getters) {
			signature.append("#M#C").append(method.getContainingClass().getFQN()).append(".").append(method.getName()).append(SEPARATOR);
		}

		return signature.toString();
	}

	@Override
	public Collection<? extends PhpNamedElement> getBySignature(String s, Project project) {
		PhpIndex index = PhpIndex.getInstance(project);
		Collection<PhpNamedElement> elements = new ArrayList<PhpNamedElement>();
		while (s.contains(SEPARATOR)) {
			String sig = s.substring(0, s.indexOf(SEPARATOR));
			s = s.substring(s.indexOf(SEPARATOR) + 1);
			if (!sig.isEmpty()) {
				elements.addAll(index.getBySignature(sig));
			}
		}

		return elements;
	}


}
