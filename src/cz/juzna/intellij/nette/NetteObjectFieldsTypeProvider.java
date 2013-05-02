package cz.juzna.intellij.nette;

import com.intellij.openapi.project.DumbService;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.HashMap;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.FieldReference;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider;

import java.util.Collection;

/**
 *
 */
public class NetteObjectFieldsTypeProvider implements PhpTypeProvider {
	@Override
	public PhpType getType(PsiElement e) {
		if (DumbService.getInstance(e.getProject()).isDumb()) return null;

		if (e instanceof FieldReference) {
			FieldReference field = (FieldReference) e;
			HashMap<String, Collection<Method>> fields = FieldFinder.findMagicFields(field.getClassReference().getType(), PhpIndex.getInstance(e.getProject()));

			if (fields.containsKey(field.getName())) {
				return MagicFieldsTypesHelper.extractTypeFromMethodTypes(fields.get(field.getName()));
			}
		}

		return null;
	}
}
