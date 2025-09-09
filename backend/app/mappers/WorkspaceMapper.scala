package mappers

import dto.response.workspace.WorkspaceResponse
import models.entities.Workspace

object WorkspaceMapper {

    /** Map a single Workspace entity to WorkspaceResponse DTO */
    def toResponse(entity: Workspace): WorkspaceResponse =
        WorkspaceResponse(
            id = entity.id.getOrElse(0),
            name = entity.name,
            desc = entity.description
        )

    /** Map a sequence of Workspace entities to responses */
    def toResponses(entities: Seq[Workspace]): Seq[WorkspaceResponse] =
        entities.map(toResponse)
}
