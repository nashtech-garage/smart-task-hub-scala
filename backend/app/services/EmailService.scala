package services

import play.api.Configuration
import play.api.libs.mailer.{Email, MailerClient}

import javax.inject.{Inject, Singleton}

@Singleton
class EmailService @Inject()(mailerClient: MailerClient,
                             config: Configuration) {
  val clientUrl = config.get[String]("client.url")

  def sendInviteToWorkspaceEmail(to: String, workspaceId:Int): Unit = {
    val link = clientUrl + "/workspace/boards/" + workspaceId
    val email = Email(
      subject = s"Invitation in to workspace",
      from = "invitation-do-not-reply@smarttaskhub.com",
      to = Seq(to),
      bodyText = Some(
        s"Hi,\n\nYou’ve been invited to join their workspace.\n\nClick the link below to accept the invitation:\n$link\n\nThanks!"
      ),
      bodyHtml = Some(s"""
           |<html>
           |  <div>
           |    <p>Hi,</p>
           |    <p>You’ve been invited to join there workspace.</p>
           |    <p><a href="$link">Go to workspace</a></p>
           |    <p>Thanks!</p>
           |  </div>
           |</html>
           |""".stripMargin)
    )
    mailerClient.send(email)
  }
}
