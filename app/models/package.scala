import play.api.data.validation.Constraints._
import play.api.data.validation.{Valid, ValidationError, Invalid, Constraint}


package object models extends Constants{

  val companyPattern = pattern("""^[a-zA-Z0-9 '&\\/]{0,105}$""".r, error="Invalid company name")

  def nonEmptyTextLisa[T](messageKey:String): Constraint[String] = Constraint[String](required) { text =>
    if (text == null) Invalid(messageKey) else if (text.trim.isEmpty) Invalid(ValidationError(messageKey)) else Valid
  }
}

trait Constants {
  val cacheKey: String = "organisationDetails"

  val company_error_key = "org.compName.mandatory"
  val ctutr_error_key =  "org.ctUtr.mandatory"
  val compLabel = "companyName"
  val utrLabel = "ctrNumber"
  val required = "constraint.required"
}
