package cz.juzna.intellij.nette.utils;

import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocMethod;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class EventUtil {

	@NotNull
	public static Collection<Field> getEventDeclarations(MethodReference methodReference) {
		String eventName = methodReference.getName();
		if (eventName == null || !eventName.startsWith("on")) {
			return Collections.emptyList();
		}
		Collection<Field> fields = new ArrayList<Field>();
		for (PhpClass cls : PhpIndexUtil.getClasses(methodReference.getClassReference(), methodReference.getProject())) {
			Method method = cls.findMethodByName(eventName);
			if (method == null || method instanceof PhpDocMethod) {
				fields.add(cls.findFieldByName(eventName, false));
			}
		}

		return fields;
	}

}
