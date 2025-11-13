package services

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.verify
import play.api.libs.mailer.Email
import play.api.Configuration

class EmailServiceSpec extends AnyWordSpec with Matchers with MockitoSugar {
  "EmailService#sendInviteToWorkspaceEmail" should {
    "compose and send an invite email with correct recipient, subject and workspace link" in {
      val mailer = mock[play.api.libs.mailer.MailerClient]
      val config = Configuration("client.url" -> "http://example.com")
      val service = new services.EmailService(mailer, config)

      service.sendInviteToWorkspaceEmail("user@example.com", 42)

      val captor = ArgumentCaptor.forClass(classOf[Email])
      verify(mailer).send(captor.capture())

      val email = captor.getValue
      email.to shouldBe Seq("user@example.com")
      email.subject shouldBe "Invitation in to workspace"
      email.from shouldBe "invitation-do-not-reply@smarttaskhub.com"
      email.bodyText.isDefined shouldBe true
      email.bodyText.get should include ("http://example.com/workspace/boards/42")
      email.bodyHtml.isDefined shouldBe true
      email.bodyHtml.get should include ("""<a href="http://example.com/workspace/boards/42">Go to workspace</a>""")
    }
  }
}