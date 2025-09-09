package validations

import play.api.libs.json.{JsError, JsPath, JsonValidationError, Reads}

/**
  * Utility object containing custom Play JSON validators for common input
  * constraints.
  */
object CustomValidators {

  /**
    * Validates a required field from a JSON request in Play Framework.
    *
    * This method ensures:
    *   - The field exists and is not `null`.
    *   - All provided validations are applied in order.
    *   - Validation stops at the first failure (per field).
    *   - A custom transformation can be applied to the field value before validation (e.g., trimming strings).
    *
    * @param fieldName
    *   The JSON field name to validate.
    * @param requiredMessage
    *   Optional custom error message when the field is missing or `null`.
    *   If not provided, defaults to `"${fieldName} is required"`.
    * @param validations
    *   Sequence of `(predicate, errorMessage)` tuples.
    *   Each predicate is a function `(T => Boolean)` that must return `true` to pass.
    *   The first failing validation stops further checks for that field.
    * @param transform
    *   Function applied to the value before validations.
    *   For example: `_.trim` for strings. Defaults to identity.
    * @param reads
    *   Implicit Play JSON `Reads[T]` to parse the field from JSON.
    * @tparam T
    *   The Scala type of the field (String, Int, Boolean, etc.).
    *
    * @return
    *   A `Reads[T]` suitable for Play JSON combinator syntax.
    *
    * === Example ===
    * {{{
    * case class CreateUser(name: String)
    *
    * implicit val reads: Reads[CreateUser] = (
    *   validateRequiredField[String](
    *     fieldName = "name",
    *     validations = Seq(
    *       notEmpty("Name must not be empty"),
    *       minLength(3, "Name must be at least 3 characters")
    *     ),
    *     transform = _.trim
    *   )
    * )(CreateUser.apply _)
    * }}}
    */
  def validateRequiredField[T](
    fieldName: String,
    requiredMessage: String = "",
    validations: Seq[(T => Boolean, String)] = Seq.empty,
    transform: T => T = (t: T) => t
  )(implicit reads: Reads[T]): Reads[T] = {
    val finalRequiredMessage =
      if (requiredMessage.trim.nonEmpty) {
        requiredMessage
      } else {
        s"$fieldName is required"
      }

    (JsPath \ fieldName)
      .read[T]
      .orElse(
        Reads(
          _ =>
            JsError(
              JsPath \ fieldName,
              JsonValidationError(finalRequiredMessage)
          )
        )
      )
      .map(transform)
      .flatMap { value =>
        runValidations(fieldName, value, validations)
      }
  }

  /**
    * Validates an optional field from a JSON request in Play Framework.
    *
    * If the field is missing or `null`, it is accepted as `None`.
    * If present, the field is transformed and validated.
    *
    * @param fieldName
    *   The JSON field name to validate.
    * @param validations
    *   Sequence of `(predicate, errorMessage)` tuples.
    *   The first failing validation stops further checks for that field.
    * @param transform
    *   Function applied to the value before validations.
    *   For example: `_.trim` for strings. Defaults to identity.
    * @param reads
    *   Implicit Play JSON `Reads[T]` to parse the field from JSON.
    * @tparam T
    *   The Scala type of the field.
    *
    * @return
    *   A `Reads[Option[T]]` suitable for Play JSON combinator syntax.
    *
    * === Example ===
    * {{{
    * case class UpdateUser(description: Option[String])
    *
    * implicit val reads: Reads[UpdateUser] = (
    *   validateOptionalField[String](
    *     fieldName = "description",
    *     validations = Seq(
    *       minLength(10, "Description must be at least 10 characters")
    *     ),
    *     transform = _.trim
    *   )
    * )(UpdateUser.apply _)
    * }}}
    */
  def validateOptionalField[T](
    fieldName: String,
    validations: Seq[(T => Boolean, String)] = Seq.empty,
    transform: T => T = (t: T) => t
  )(implicit reads: Reads[T]): Reads[Option[T]] = {
    (JsPath \ fieldName).readNullable[T].map(_.map(transform)).flatMap {
      case Some(value) =>
        runValidations(fieldName, value, validations).map(Some(_))
      case None => Reads.pure(None)
    }
  }

  /**
    * Runs a sequence of validations for a given field value.
    * Stops at the first failure and returns the error.
    *
    * @param fieldName
    *   Name of the field (used in the error output).
    * @param value
    *   The parsed value to validate.
    * @param validations
    *   Sequence of `(predicate, errorMessage)` tuples.
    * @tparam T
    *   Type of the field.
    *
    * @return
    *   A `Reads[T]` that is either the value (if all validations pass)
    *   or a `JsError` with the corresponding message.
    */
  private def runValidations[T](
    fieldName: String,
    value: T,
    validations: Seq[(T => Boolean, String)]
  ): Reads[T] = {
    validations.foldLeft[Reads[T]](Reads.pure(value)) {
      case (acc, (test, errorMsg)) =>
        acc.flatMap { v =>
          if (test(v)) {
            Reads.pure(v)
          } else {
            Reads(
              _ => JsError(JsPath \ fieldName, JsonValidationError(errorMsg))
            )
          }
        }
    }
  }
  /**
    * Validates a required field against an enumeration.
    *
    * This method checks if the field is present, not empty, and matches one of
    * the enumeration values. If the value is invalid, it returns a custom error
    * message indicating the allowed values.
    *
    * @param field
    *   The JSON field name to validate.
    * @param enum
    *   The enumeration to validate against.
    * @param requiredMsg
    *   Custom error message when the field is missing or empty.
    * @param invalidMsgTmpl
    *   Template for the error message when the value is not a valid enum value.
    *
    * @tparam E
    *   The type of the enumeration.
    *
    * @return
    *   A `Reads[E#Value]` that validates the field against the enum.
    */
  def validateRequiredEnum[E <: Enumeration](
    field: String,
    enum: E,
    requiredMsg: String,
    invalidMsgTmpl: String
  ): Reads[E#Value] = {
    val path = JsPath \ field

    path
      .read[String]
      .filter(JsonValidationError(requiredMsg))(_.trim.nonEmpty)
      .flatMap { raw =>
        val s = raw.trim
        enum.values.find(_.toString.equalsIgnoreCase(s)) match {
          case Some(v) => Reads.pure(v)
          case None =>
            val allowed = enum.values.mkString(", ")
            val msg = invalidMsgTmpl.format(allowed)
            Reads(_ => JsError(path, JsonValidationError(msg)))
        }
      }
  }

  /**
    * Validation helper: ensures a string is not empty (after trimming).
    */
  def notEmpty(msg: String): (String => Boolean, String) =
    ((s: String) => s.trim.nonEmpty, msg)

  /**
    * Validation helper: ensures a string has at least `min` characters.
    */
  def minLength(min: Int, msg: String): (String => Boolean, String) =
    ((s: String) => s.length >= min, msg)

  def minValue(min: Int, msg: String): (Int => Boolean, String) =
    ((v: Int) => v >= min, msg)

  /**
    * Validation helper: ensures a string matches a regular expression.
    */
  def regexMatch(pattern: String, msg: String): (String => Boolean, String) =
    ((s: String) => s.matches(pattern), msg)
}
