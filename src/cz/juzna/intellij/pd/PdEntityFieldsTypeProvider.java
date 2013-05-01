package cz.juzna.intellij.pd;

import com.intellij.openapi.project.DumbService;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.HashMap;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.FieldReference;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider;


public class PdEntityFieldsTypeProvider implements PhpTypeProvider {
	@Override
	public PhpType getType(PsiElement e) {
		if (DumbService.getInstance(e.getProject()).isDumb()) return null;
		if (e instanceof FieldReference) {
			FieldReference field = (FieldReference) e;
			HashMap<String, PhpType> fields = FieldFinder.findMagicFields(field.getClassReference().getType(), PhpIndex.getInstance(e.getProject()));
			if (fields.containsKey(field.getName())) {
				PhpType type = fields.get(field.getName());
				return type;
			}
		}

		return null;
	}
}
