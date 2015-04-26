package cz.juzna.intellij.nette.utils;


import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;

public class ComponentUtil {

	private static PhpType container = new PhpType().add("Nette\\ComponentModel\\Container");

	public static boolean isContainer(PhpClass csl) {
		return container.isConvertibleFrom(csl.getType(), PhpIndex.getInstance(csl.getProject()));
	}

}
