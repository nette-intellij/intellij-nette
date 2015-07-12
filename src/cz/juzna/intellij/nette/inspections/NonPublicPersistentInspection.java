package cz.juzna.intellij.nette.inspections;

import com.jetbrains.php.lang.psi.elements.Field;

public class NonPublicPersistentInspection extends NonPublicFieldInspection {

	@Override
	protected String getProblemDescription() {
		return "@persistent property must be public";
	}

	@Override
	protected String getFixName() {
		return "Make @persistent property public";
	}

	@Override
	protected boolean isInvalid(Field field) {
		return !field.getModifier().isPublic()
				&& field.getDocComment() != null
				&& field.getDocComment().getText().contains("@persistent");
	}

}
