package cz.juzna.intellij.nette.utils;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.codeInsight.PhpCodeInsightUtil;
import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocMethod;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ListenerGeneratorUtil {

	private static ParametersResolver[] resolvers = {new PhpDocMethodParametersResolver(), new PhpDocFieldParametersResolver()};

	public static String createListenerTemplate(Field field, PsiElement scope, @Nullable String name) {
		List<ParameterInfo> parameters = null;
		for (ParametersResolver resolver : resolvers) {
			parameters = resolver.getParameters(field);
			if (parameters != null) {
				break;
			}
		}

		StringBuilder tpl = new StringBuilder();
		if (name == null) {
			tpl.append("function (");

		} else {
			tpl.append("public function ").append(name).append("(");
		}
		if (parameters != null) {
			int i = 0;
			for (ParameterInfo parameter : parameters) {
				if (i > 0) {
					tpl.append(", ");
				}
				PhpType declaredType = parameter.getType();
				if (declaredType != null) {
					String qName = PhpCodeInsightUtil.createQualifiedName(getScopeElement(scope), declaredType.toString());
					tpl.append(qName).append(" ");
				}
				tpl.append("$").append(parameter.getName());
				i++;
			}
		}
		tpl.append("){\n\n}\n");

		return tpl.toString();
	}


	private static class ParameterInfo {
		private String name;
		private PhpType type;

		public ParameterInfo(String name, @Nullable PhpType type) {
			this.name = name;
			this.type = type;
		}

		public String getName() {
			return name;
		}

		@Nullable
		public PhpType getType() {
			return type;
		}
	}

	private interface ParametersResolver {

		@Nullable
		public List<ParameterInfo> getParameters(@NotNull Field field);

	}

	private static class PhpDocMethodParametersResolver implements ParametersResolver {

		@Nullable
		@Override
		public List<ParameterInfo> getParameters(@NotNull Field field) {
			if (field.getContainingClass() == null) {
				return null;
			}
			Method docMethod = field.getContainingClass().findMethodByName(field.getName());
			if (docMethod == null || !(docMethod instanceof PhpDocMethod)) {
				return null;
			}
			ArrayList<ParameterInfo> parameters = new ArrayList<ParameterInfo>();
			int i = 0;
			for (Parameter parameter : docMethod.getParameters()) {
				String name = parameter.getName();
				if (name.length() == 0) {
					name = "param" + i;
				}
				PhpType declaredType = parameter.getDeclaredType();
				if (declaredType.getTypes().size() != 1 || !declaredType.toString().startsWith("\\")) {
					declaredType = null;
				}
				parameters.add(new ParameterInfo(name, declaredType));
				i++;
			}

			return parameters;
		}
	}

	private static class PhpDocFieldParametersResolver implements ParametersResolver {

		@Nullable
		@Override
		public List<ParameterInfo> getParameters(@NotNull Field field) {
			if (field.getDocComment() == null || field.getDocComment().getVarTag() == null) {
				return null;
			}
			String description = field.getDocComment().getVarTag().getTagValue();
			int start = description.indexOf("(");
			int end = description.indexOf(")");
			if (start == -1 || end == -1 || start > end) {
				return null;
			}
			List<ParameterInfo> parameters = new ArrayList<ParameterInfo>();
			String parametersStr = description.substring(start + 1, end);
			int i = 0;
			for (String param : parametersStr.split(",")) {
				param = param.trim();
				String type = param.contains(" ") ? param.substring(0, param.indexOf(" ")) : null;
				type = toFqn(type, field);
				String varName = param.contains("$") ? param.substring(param.indexOf("$") + 1) : "param" + i;
				parameters.add(new ParameterInfo(varName, type != null ? (new PhpType()).add(type) : null));
				i++;
			}


			return parameters;
		}

		private static String toFqn(String name, Field field) {
			if (name == null || PhpLangUtil.isFqn(name)) {
				return name;
			}
			PhpPsiElement parent = getScopeElement(field);

			Map<String, String> aliases = PhpCodeInsightUtil.getAliasesInScope(parent);
			String[] parts = name.split("\\\\", 2);
			if (!aliases.containsKey(parts[0])) {
				return null;
			}
			parts[0] = aliases.get(parts[0]);
			return StringUtils.join(parts,"\\");
		}
	}


	@NotNull
	private static PhpPsiElement getScopeElement(PsiElement el) {
		PhpPsiElement parent = PsiTreeUtil.getParentOfType(el, PhpNamespace.class);
		if (parent == null) {
			parent = (PhpPsiElement) el.getContainingFile();
		}

		return parent;
	}
}
