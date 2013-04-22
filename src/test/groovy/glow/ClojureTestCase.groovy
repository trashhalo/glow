package glow

import org.junit.Test

class ClojureTestCase {

  @Clojure
  def hello() {"""
     (let [x "hello!"] x)
  """}

  @Clojure
  def helloWithArg(x) {"""
     x
  """}

  def callOut() {
    "hello!"
  }

  @Clojure
  def helloWithCallOut() {"""
    (.callOut this)
  """}

  @Clojure
  def helloWithArgAndCallOut(x) {"""
    (str x (.callOut this))
  """}

  @Clojure
  def incAll(groovyList) {"""
    (map inc groovyList)
  """}

  @Test
  void testHello() {
    assert hello().equals("hello!")
  }

  @Test
  void testHelloWithArg() {
    assert helloWithArg("hello!").equals("hello!")
  }

  @Test
  void testHelloWithCallOut() {
    assert helloWithCallOut().equals(callOut())
  }

  @Test
  void testHelloWithArgAndCallOut() {
    assert helloWithArgAndCallOut("xxx").equals("xxx"+callOut())
  }

  @Test
  void testCollectionMethodsWork(){
    assert incAll([1,2,3]) == [2,3,4]
  }
}
