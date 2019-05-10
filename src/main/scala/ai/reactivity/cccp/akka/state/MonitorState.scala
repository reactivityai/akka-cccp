package ai.reactivity.cccp.akka.state

case class MonitorState(procs: Map[Int, Process]) {
  lazy val nextPid: Int = if (procs.isEmpty) 1 else procs.keys.max + 1
}

object MonitorState {
  lazy val empty: MonitorState = MonitorState(procs = Map.empty)
}