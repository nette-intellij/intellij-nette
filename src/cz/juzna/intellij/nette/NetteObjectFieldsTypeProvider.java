package cz.juzna.intellij.nette;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.HashMap;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.FieldReference;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider2;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;


public class NetteObjectFieldsTypeProvider implements PhpTypeProvider2 {

	static char key = '\u0223';

	@Override
	public char getKey() {
		return key;
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
		PhpIndex phpIndex = PhpIndex.getInstance(e.getProject());
		HashMap<String, Collection<Method>> fields = FieldFinder.findMagicFields(field.getClassReference().getType(), phpIndex);
		if (!fields.containsKey(field.getName())) {
			return null;
		}
		PhpType phpType = MagicFieldsTypesHelper.extractTypeFromMethodTypes(fields.get(field.getName()));
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
