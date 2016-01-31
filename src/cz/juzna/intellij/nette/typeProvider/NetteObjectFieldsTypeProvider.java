package cz.juzna.intellij.nette.typeProvider;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.PhpPsiUtil;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider2;
import cz.juzna.intellij.nette.utils.MagicFieldsUtil;
import cz.juzna.intellij.nette.utils.PhpIndexUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;


public class NetteObjectFieldsTypeProvider implements PhpTypeProvider2 {

	final static String SEPARATOR = "\u0180";
	final static String TYPE_SEPARATOR = "\u0181";

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
		if (cz.juzna.intellij.nette.utils.PhpPsiUtil.isLocallyResolvableType(e)) {
			return null;
		}


		PhpClass calledFrom = PhpPsiUtil.getParentByCondition(field, PhpClass.INSTANCEOF);
		String typeString = field.getClassReference().getType().toString().replaceAll("\\|", TYPE_SEPARATOR);
		return field.getName() + SEPARATOR + (calledFrom != null ? calledFrom.getFQN() : "") + SEPARATOR + typeString;
	}

	@Override
	public Collection<? extends PhpNamedElement> getBySignature(String s, Project project) {
		PhpIndex index = PhpIndex.getInstance(project);
		String[] parts = s.split(SEPARATOR, 3);
		if (parts.length != 3) {
			return Collections.emptyList();
		}
		String fieldName = parts[0];
		String calledFromFqn = parts[1];
		Collection<PhpClass> calledFrom = null;
		if (!calledFromFqn.equals("")) {
			calledFrom = index.getAnyByFQN(calledFromFqn);
		}
		String type = parts[2];
		Collection<PhpClass> containingClass = PhpIndexUtil.getByType(type.split(TYPE_SEPARATOR), index);

		return MagicFieldsUtil.findMagicMethods(fieldName, containingClass, calledFrom);
	}



}
