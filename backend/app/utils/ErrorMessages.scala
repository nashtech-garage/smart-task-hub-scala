package utils

import play.api.i18n.Messages

/**
  * Utility object for generating error messages used in validation.
  *
  * This object provides methods to create standardized error messages
  * for various validation scenarios, such as required fields, empty values,
  * and minimum length violations.
  * for example:
  * {{{
  *    ErrorMessages.required("Workspace name")
  * }}}
  * will return a localized error message for a required field.
  */
object ErrorMessages {
  def required(field: String)(implicit messages: Messages): String =
    messages("validate.required", field)

  def empty(field: String)(implicit messages: Messages): String =
    messages("validate.empty", field)

  def tooShort(field: String, min: Int)(implicit messages: Messages): String =
    messages("validate.tooShort", field, min)

  def invalidEnum(field: String, allowed: Iterable[String])(implicit messages: Messages): String =
    messages("validate.invalidEnum", field, allowed.mkString(", "))

  def minValue(field: String, min: Int)(implicit messages: Messages): String =
    messages("validate.minValue", field, min)
}
