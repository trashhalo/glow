#Glow
##What does it do?
Allows you to write groovy class methods in clojure.

##How does it work?
It uses groovy AST transformations to rewrite the class so that it will compile the clojure code during class 
initialization and then changes the method to call to the clojure RT class to invoke the clojure code at runtime when
the method is called.

##Why?
I like clojure but work professionally in groovy. This allows me to write some lisp without having to convert entire 
projects over. Additionally, I wanted to play with AST transformations.

##How do I use it?

Annotate groovy methods on a class with @Clojure to compile it as clojure.

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
```
