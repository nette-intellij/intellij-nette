package cz.juzna.intellij.nette.typeProvider;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.HashMap;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.FieldReference;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider2;
import cz.juzna.intellij.nette.utils.MagicFieldsUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;


public class NetteObjectFieldsTypeProvider implements PhpTypeProvider2 {

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
		HashMap<String, Collection<Method>> fields = MagicFieldsUtil.findMagicFields(field);
		if (!fields.containsKey(field.getName())) {
			return null;
		}
		for (Method method : fields.get(field.getName())) {
			if (method.getName().startsWith("get") || method.getName().startsWith("is")) {
				return "#M#C" + method.getContainingClass().getFQN() + "." + method.getName();
			}
		}

		return null;
	}

	@Override
	public Collection<? extends PhpNamedElement> getBySignature(String s, Project project) {
		PhpIndex index = PhpIndex.getInstance(project);
		return index.getBySignature(s);
	}


}
