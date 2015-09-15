import scala.reflect.macros.Context
import scala.language.experimental.macros
import scala.annotation.StaticAnnotation


object GenProxyMacro {

  // http://stackoverflow.com/questions/16792824/test-whether-a-method-is-defined
  def isDeferred[T](sym: T) = sym
    .asInstanceOf[scala.reflect.internal.Symbols#Symbol]
    .hasFlag(scala.reflect.internal.Flags.DEFERRED)

  def impl(c: Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    // reflection to get type of the trait to implement
    // stolen from http://imranrashid.com/posts/scala-reflection/
    val targetTrait = c.prefix.tree match {
      case Apply(Select(New(AppliedTypeTree(Ident(_), List(typ))), nme.CONSTRUCTOR), List()) => typ
    }
    //of course, 7 is not really an instance of our target trait -- but we
    // don't care; we just want it to *type check* as our target trait,
    // so we can pull out the type
    val tpe: c.universe.Type = c.typeCheck(q"(7.asInstanceOf[$targetTrait])").tpe

    // get all the methods to implement
    val methods: Iterable[c.universe.MethodSymbol] = tpe.declarations.filter(_.isMethod)
      .map(_.asMethod)
      .filter(isDeferred[c.universe.Symbol])

    val methodNames = methods.map(_.name.toString)
    val printNames = q"""
      println(..$methodNames)
      """

    def genMethods(serv: ValDef) = methods map { (m: c.universe.MethodSymbol) =>
      val methodName = m.name.toTermName
      // helpful: http://stackoverflow.com/questions/18559559/quasiquotes-for-multiple-parameters-and-parameter-lists?rq=1
      val params: List[List[ValDef]] = m.paramss.map { _ map { p =>
        q"val ${p.name.toTermName}: ${p.typeSignature}"
        // ^ is equivalent to:
        // ValDef(Modifiers(Flag.PARAM), p.name.toTermName, TypeTree(p.typeSignature), EmptyTree)
      }}
      val args: List[List[TermName]] = m.paramss.map { _ map { _.name.toTermName } }
      q"""
      def $methodName(...$params): ${m.returnType} = {
        println("ServiceProxy." + ${methodName.toString} + " - forwarding to service")
        val resp = ${serv.name}.$methodName(...$args)
        println("ServiceProxy." + ${methodName.toString} + s" - got response from service: $$resp")
        resp
      }
      """
    }

    val result = {
      annottees.map(_.tree).toList match {
        case q"class $name (..$dependencies) extends ..$parents { ..$body }" :: Nil =>
          val serviceImpl = dependencies.asInstanceOf[List[ValDef]].head
          val methods = genMethods(serviceImpl)
          q"""
            class $name (..$dependencies) extends ..$parents {
              def what: Unit = $printNames
              ..$methods
              ..$body
            }
          """
          //q"""println(${showRaw(methods)})"""  // <= uncomment this to debug AST
        case x => q"""println(${showRaw(x)})"""
      }
    }
    c.Expr[Any](result)
  }
}

class GenProxy[T] extends StaticAnnotation {
  def macroTransform(annottees: Any*) = macro GenProxyMacro.impl
}