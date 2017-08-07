import play.api.data.Form
import play.api.data.Forms.{mapping, number, optional, text}

case class Questionnaire(
                          easyToUse: Option[Int],
                          satisfactionLevel: Option[Int],
                          whyGiveThisRating: Option[String],
                          referer: Option[String]
                        )

object Questionnaire {
  val maxStringLength = 1200
  val maxOptionSize = 4
  val maxBooleanOptionSize = 2
  val form = Form(mapping(
    "easyToUse" -> optional(number(0, maxOptionSize)),
    "satisfactionLevel" -> optional(number(0, maxOptionSize)),
    "whyGiveThisRating" -> optional(text(maxLength = maxStringLength)),
    "referer" -> optional(text))(Questionnaire.apply)(Questionnaire.unapply)
  )
}
