/**
 * Support for loading localizations from properties files.
 * <p>
 * An interface must be defined that extends {@link LocalizedResources}. In the interface, each method
 * represents a localizable string. The texts in the default language (usually English) are
 * specified with {@link ui11.i18n.LocalizedResources.Text @Text}.
 * The property name is by default the same as the method name, but is customizable
 * with {@link ui11.i18n.LocalizedResources.Name @Name} and {@link ui11.i18n.LocalizedResources.Prefix @Prefix}.
 * <p>
 * Each localized text is represented as a {@linkplain LocalizedText}.
 * Because {@linkplain LocalizedText} is a {@linkplain ui11.Widget}, you can place it directly to a widget tree,
 * without needing to manually wrap inside a {@link ui11.text.Text} widget. Apart from convenience, this makes
 * available that information that where are each localizable text used, which can be useful for translation
 * guidance.
 */
package ui11.i18n;