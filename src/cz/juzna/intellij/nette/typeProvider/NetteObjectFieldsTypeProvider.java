package cz.juzna.intellij.nette.typeProvider;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.HashMap;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.PhpPsiUtil;
import com.jetbrains.php.lang.psi.elements.FieldReference;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider2;
import cz.juzna.intellij.nette.utils.MagicFieldsUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
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
		Method method = PhpPsiUtil.getParentByCondition(field, new Condition<PsiElement>() {
			@Override
			public boolean value(PsiElement element) {
				return element instanceof Method;
			}
		});
		String methodName = method != null ? method.getName().toLowerCase() : null;
		if (method != null
				&& (methodName.equalsIgnoreCase("get" + field.getName())
						|| methodName.equalsIgnoreCase("set" + field.getName())
						|| methodName.equalsIgnoreCase("is" + field.getName()))) {
			return null;
		}
		PhpIndex phpIndex = PhpIndex.getInstance(e.getProject());
		HashMap<String, Collection<Method>> fields = MagicFieldsUtil.findMagicFields(field.getClassReference().getType(), phpIndex);
		if (!fields.containsKey(field.getName())) {
			return null;
		}
		PhpType phpType = MagicFieldsUtil.extractTypeFromMethodTypes(fields.get(field.getName()));
		if (phpType == null) {
			return null;
		}
		return phpType.toStringResolved();
	}

	@Override
	public Collection<? extends PhpNamedElement> getBySignature(String s, Project project) {
		PhpIndex index = PhpIndex.getInstance(project);
		Collection<PhpNamedElement> result = new ArrayList<PhpNamedElement>();
		for (String type : s.split("\\|")) {
			result.addAll(index.getAnyByFQN(type));
		}


		return result;
	}


}
