#Glow
##How does it work?
It uses groovy AST transformations to compile the clojure code during class initialization and then calls to the clojure
RT class to invoke the method at runtime when the method is called.

##How do I use it?


Annotate groovy methods on a class with Clojure to compile it as clojure.

```groovy
@Clojure
def sayHello(){"""
   (let [x "Hello"]
     (println x))
"""}
```

You can access method params and class methods.

```groovy
def getUser(){
    springSecurityService.principal.id
}

@Clojure
def greetUser(greeting){"""
    (println (str greeting ", " (.getUser this)))
"""}