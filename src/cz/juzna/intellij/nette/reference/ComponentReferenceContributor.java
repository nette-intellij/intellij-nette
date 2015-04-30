package cz.juzna.intellij.nette.reference;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.Method;
import cz.juzna.intellij.nette.utils.ComponentUtil;
import org.jetbrains.annotations.NotNull;

public class ComponentReferenceContributor extends PsiReferenceContributor {

	@Override
	public void registerReferenceProviders(PsiReferenceRegistrar psiReferenceRegistrar) {
		psiReferenceRegistrar.registerReferenceProvider(PlatformPatterns.psiElement(PhpElementTypes.STRING), new PsiReferenceProvider() {
			@NotNull
			@Override
			public PsiReference[] getReferencesByElement(PsiElement psiElement, ProcessingContext processingContext) {
				if (psiElement.getParent() == null || psiElement.getParent().getParent() == null) {
					return new PsiReference[0];
				}
				Method[] factoryMethods = ComponentUtil.getFactoryMethods(psiElement.getParent().getParent(), true);
				if (factoryMethods.length == 0) {
					return new PsiReference[0];
				}
				return new PsiReference[]{new ComponentReference(psiElement)};
			}
		});
	}
}
