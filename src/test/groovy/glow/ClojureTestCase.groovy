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
}
