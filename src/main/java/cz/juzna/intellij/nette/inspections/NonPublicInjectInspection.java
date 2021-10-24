package cz.juzna.intellij.nette.inspections;

import com.jetbrains.php.lang.psi.elements.Field;

public class NonPublicInjectInspection extends NonPublicFieldInspection {

	@Override
	protected String getProblemDescription() {
		return "@inject property must be public";
	}

	@Override
	protected String getFixName() {
		return "Make @inject property public";
	}

	@Override
	protected boolean isInvalid(Field field) {
		return !field.getModifier().isPublic()
				&& field.getDocComment() != null
				&& field.getDocComment().getText().contains("@inject");
	}

}
