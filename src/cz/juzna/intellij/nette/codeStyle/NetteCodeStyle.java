package cz.juzna.intellij.nette.codeStyle;

import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.formatter.PhpCodeStyleSettings;
import com.jetbrains.php.lang.formatter.ui.PhpLanguageCodeStyleSettingsProvider;

public class NetteCodeStyle extends PhpLanguageCodeStyleSettingsProvider.PSR1PSR2CodeStyle {

	public NetteCodeStyle() {
		super("Nette");
	}

	@Override
	public void apply(CodeStyleSettings settings) {
		super.apply(settings);

		CommonCodeStyleSettings.IndentOptions indentOptions = settings.getIndentOptions(PhpFileType.INSTANCE);
		indentOptions.INDENT_SIZE = 4;
		indentOptions.TAB_SIZE = 4;
		indentOptions.CONTINUATION_INDENT_SIZE = 4;
		indentOptions.USE_TAB_CHARACTER = true;

		CommonCodeStyleSettings commonSettings = settings.getCommonSettings(this.getLanguage());
		commonSettings.BLANK_LINES_AFTER_CLASS_HEADER = 0;
		commonSettings.ALIGN_MULTILINE_ARRAY_INITIALIZER_EXPRESSION = false;
		commonSettings.SPACE_AFTER_TYPE_CAST = true;

		PhpCodeStyleSettings phpSettings = settings.getCustomSettings(PhpCodeStyleSettings.class);
		phpSettings.PHPDOC_BLANK_LINE_BEFORE_TAGS = false;
		phpSettings.LOWER_CASE_BOOLEAN_CONST = false;
		phpSettings.LOWER_CASE_NULL_CONST = false;
		phpSettings.UPPER_CASE_NULL_CONST = true;
		phpSettings.UPPER_CASE_BOOLEAN_CONST = true;
		phpSettings.BLANK_LINE_BEFORE_RETURN_STATEMENT = false;

	}
}
