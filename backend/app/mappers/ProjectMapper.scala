package mappers

object ProjectMapper {

  def toProjectResponse(entity: models.entities.Project): dto.response.project.ProjectResponse =
    dto.response.project.ProjectResponse(
      id = entity.id.getOrElse(0),
      name = entity.name,
      status = entity.status
    )

}
