### A fork of https://github.com/scalamacros/sbt-example-paradise showing a more complex macro for code generation

This is a macro annotation that will generate a wrapper implementing all the interfaces defined in a trait and proxying these calls to another instance.

See https://github.com/jolleon/sbt-example-paradise/blob/master/core/src/main/scala/Test.scala for example usage.

```
jules:~/dev/sbt-example-paradise (master) sbt run
[info] Loading project definition from /Users/jules/dev/sbt-example-paradise/project
[info] Set current project to root (in build file:/Users/jules/dev/sbt-example-paradise/)
[info] Running Test 
CALLING proxy.x(1)
ServiceProxy.x - forwarding to service
ServiceImpl.x
ServiceProxy.x - got response from service: hello1
RECEIVED: hello1

CALLING proxy.y
ServiceProxy.y - forwarding to service
ServiceImpl.y
ServiceProxy.y - got response from service: ()
RECEIVED: ()

CALLING proxy.z(3, "hi")
ServiceProxy.z - forwarding to service
ServiceProxy.z - got response from service: List(hi, hi, hi)
RECEIVED: List(hi, hi, hi)
[success] Total time: 0 s, completed Aug 21, 2015 7:26:35 PM
```