package cz.juzna.intellij.nette.reference;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import cz.juzna.intellij.nette.utils.ComponentSearcher;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

public class ComponentReferenceContributor extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(PsiReferenceRegistrar psiReferenceRegistrar) {
        psiReferenceRegistrar.registerReferenceProvider(
                PlatformPatterns.psiElement(PhpElementTypes.STRING).withLanguage(PhpLanguage.INSTANCE),
                new PsiReferenceProvider() {
                    @NotNull
                    @Override
                    public PsiReference[] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {
                        if (psiElement.getParent() == null || psiElement.getParent().getParent() == null) {
                            return new PsiReference[0];
                        }
                        PsiElement el = psiElement.getParent().getParent();
                        ComponentSearcher.ComponentQuery query = ComponentSearcher.createQuery(el);
                        query.withPath();
                        Collection<ComponentSearcher.ComponentSearchResult> result = ComponentSearcher.find(query);
                        if (result.size() == 0) {
                            return new PsiReference[0];
                        }
                        Collection<PsiReference> refs = new ArrayList<>(result.size());
                        for (ComponentSearcher.ComponentSearchResult searchResult : result) {
                            refs.add(new ComponentReference(psiElement, searchResult.getPath()));
                        }

                        return refs.toArray(new PsiReference[0]);
                    }
                });
    }
}
