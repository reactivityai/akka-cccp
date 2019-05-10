package ai.reactivity.cccp.akka

import ai.reactivity.cccp.akka.messages.out.Job
import akka.actor.Props
import org.json4s.JsonAST.JValue

trait Resolver {
  def resolve(job: Job): Option[Props] = resolve(job.name, job.params)

  def resolve(name: String, params: JValue): Option[Props]
}
