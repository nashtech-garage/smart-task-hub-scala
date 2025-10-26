package constants


object CacheKeys {

  /**
   * Cache key format: user_project_{userId}_{projectId}
   */
  def userProjectKey(userId: Int, projectId: Int): String =
    s"user_project_${userId}_$projectId"

}