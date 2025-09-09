package dto.response.column
import play.api.libs.json.{Json, OFormat}

case class ColumnSummariesResponse(id: Int, name: String, position: Int)

object ColumnSummariesResponse {
  implicit val columnSummariesFmt: OFormat[ColumnSummariesResponse] =
    Json.format[ColumnSummariesResponse]
}
