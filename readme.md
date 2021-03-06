#Glow
##What does it do?
Allows you to write groovy class methods in clojure.

##How does it work?
It uses [groovy AST transformations](http://groovy.codehaus.org/Local+AST+Transformations) to rewrite the class so that it will compile the clojure code during class
initialization and then changes the method to call to the clojure RT class to invoke the clojure code at runtime when
the method is called.

##Why?
I like clojure but work professionally in groovy. This allows me to write some lisp without having to convert entire 
projects over. Additionally, I wanted to play with AST transformations.

##How do I use it?
1. [Add my maven repo to your project](https://github.com/trashhalo/maven-repo/blob/master/readme.md)
2. Add glow as a compile dependency
```
compile 'trashhalo:glow:0.1-SNAPSHOT'
```

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

You can also fiddle with the namespace definition for the class by naming a clojure method 'namespace'.
Be sure make this the first class method.

```groovy
@Clojure
def namespace(){"""
    (:require [clojure.string :as string])
"""}
  
@Clojure
def makeItBig(x){"""
    (string/capitalize x)
"""}

```
