package cz.juzna.intellij.nette;

import com.intellij.openapi.project.DumbService;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.HashMap;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.FieldReference;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;

/**
 *
 */
public class NetteObjectFieldsTypeProvider implements com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider {
	@Override
	public PhpType getType(PsiElement e) {
		if (DumbService.getInstance(e.getProject()).isDumb()) return null;
		if (e instanceof FieldReference) {
			FieldReference field = (FieldReference) e;
			HashMap<String, Method> fields = FieldFinder.findMagicFields(field.getClassReference().getType(), PhpIndex.getInstance(e.getProject()));
			if (fields.containsKey(field.getName())) {
				PhpType type = fields.get(field.getName()).getType();
				return type;
			}
		}

		return null;
	}
}
