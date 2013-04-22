package glow

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.syntax.Token
import org.codehaus.groovy.syntax.Types
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
class ClojureAstTransformation implements ASTTransformation {
  private static final String CLOJURE_VARIABLE_NAME = '$cljstr'

  public void visit(ASTNode[] nodes, SourceUnit sourceUnit) {
    nodes.findAll { it instanceof MethodNode }.each { MethodNode method ->
      compileClojureCode(method)
      method.setCode(RTVarInvoke(method.declaringClass.name, method.name, method.parameters))
    }
  }

  private void compileClojureCode(MethodNode method) {
    String clazz = method.declaringClass.name
    String methodName = method.name
    String value = method.code.statements.first().expression.value
    String params = method.parameters*.name.join(",")
    if (params.size() > 0) {
      params = "," + params
    }
    if (method.name.equals("namespace")) { //This is the namespace method so add the namespace
      createNamespace(method.declaringClass, clazz, value)
    } else {
      String clojureCode = "(defn $methodName [this$params] $value)"
      DeclarationExpression clojureBlock = findClojureBlock(method.declaringClass, method)
      clojureBlock.rightExpression = new ConstantExpression(clojureBlock.rightExpression.value + clojureCode.toString())
    }
  }

  /**
   * Create the (ns entry in the clojure blob
   * @param classNode
   * @param clazz
   * @param value
   * @return
   */
  private Expression createNamespace(ClassNode classNode, String clazz, String value) {
    String clojureCode = "(ns $clazz $value)"
    def cljVar = new DeclarationExpression(new VariableExpression(CLOJURE_VARIABLE_NAME), Token.newSymbol(Types.ASSIGN, -1, -1), new ConstantExpression(clojureCode))
    classNode.addObjectInitializerStatements(block([
        cljVar,
        classRT,
        call(classCompiler, "load", [
            new ConstructorCallExpression(new ClassNode(StringReader), new ArgumentListExpression(new VariableExpression(CLOJURE_VARIABLE_NAME)))
        ])
    ]))
    cljVar
  }

  /**
   * Look for the variable declaration for the blob of clojure code se we can add more clojure to it. In the event
   * the class doesnt have a namespace block create one for it.
   * @param node
   * @param method
   * @return
   */
  private findClojureBlock(ClassNode node, MethodNode method) {
    def exp = node.objectInitializerStatements.findAll {
      it instanceof BlockStatement
    }*.statements.flatten().find {
      it instanceof ExpressionStatement &&
          it.expression instanceof DeclarationExpression &&
          it.expression.variableExpression.name.equals(CLOJURE_VARIABLE_NAME)
    }?.expression
    if (!exp) {
      return createNamespace(node, node.name, method.name == "namespace" ? method.code.statements.first()?.expression?.value.toString() : null)
    } else {
      exp
    }
  }

  /**
   * Looks up the precompiled function in clojure and calls it passing the parameters
   * @param clazz
   * @param fieldName
   * @param args
   * @return
   */
  private Statement RTVarInvoke(clazz, fieldName, args = []) {
    def params = args.collect { arg ->
      new VariableExpression(arg.name)
    }
    params.add(0, new VariableExpression("this"))
    block([ret(call(call(classForName('clojure.lang.RT'), 'var', [str(clazz), str(fieldName)]), 'invoke', params))])
  }

  private getClassCompiler() {
    classForName('clojure.lang.Compiler')
  }

  private getClassRT() {
    classForName('clojure.lang.RT')
  }

  /** HELPER METHODS **/

  private call(object, method, args = new ArgumentListExpression()) {
    return new MethodCallExpression(object, method, args instanceof ArgumentListExpression ? args : new ArgumentListExpression(args))
  }

  private classForName(String name) {
    new StaticMethodCallExpression(new ClassNode(Class), "forName", new ArgumentListExpression(new ConstantExpression(name)))
  }

  private block(exps) {
    def block = new BlockStatement()
    exps.each {
      block.addStatement(it instanceof Expression ? new ExpressionStatement(it) : it)
    }
    block
  }

  private ret(exp) {
    new ReturnStatement(exp)
  }

  private str(val) {
    return new ConstantExpression(val)
  }


}