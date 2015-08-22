
trait Service {
  def x(n: Int): String
  def y: Unit
  def z(n: Int, s: String): List[String]
}

class ServiceImpl extends Service {
  def x(n: Int) = {
    println("ServiceImpl.x")
    "hello" + n
  }

  def y = println("ServiceImpl.y")

  def z(n: Int, s: String) = {
    List.fill(n)(s)
  }
}

object Test extends App {

  @GenProxy[Service] class ServiceProxy(serviceImpl: ServiceImpl) extends Service

  val serviceImpl = new ServiceImpl

  val proxy = new ServiceProxy(serviceImpl)

  println("CALLING proxy.x(1)")
  val respx = proxy.x(1)
  println("RECEIVED: " + respx)
  println()
  println("CALLING proxy.y")
  val respy = proxy.y
  println("RECEIVED: " + respy)
  println()
  println("CALLING proxy.z(3, \"hi\")")
  val respz = proxy.z(3, "hi")
  println("RECEIVED: " + respz)

}