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
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
class ClojureAstTransformation implements ASTTransformation {

  public void visit(ASTNode[] nodes, SourceUnit sourceUnit) {
    nodes.findAll { it instanceof MethodNode }.each { MethodNode method ->
      GString fieldName = "\$cljgen_${method.name}"
      method.declaringClass.addObjectInitializerStatements(compileClojureCode(method))
      method.setCode(callToField(method.declaringClass.name, method.name, method.parameters))
    }
  }

  private Statement compileClojureCode(MethodNode method) {
    String clazz = method.declaringClass.name
    String methodName = method.name
    String value = method.code.statements.first().expression.value
    String params = method.parameters*.name.join(",")
    if(params.size()>0){
      params = ","+params
    }
    String clojureCode = """
      (ns $clazz)
      (defn $methodName [this$params] $value)
    """
    block([
        classRT,
        call(classCompiler, "load", [
            new ConstructorCallExpression(new ClassNode(StringReader), new ConstantExpression(clojureCode.toString()))
        ])
    ])

  }

  private Statement callToField(clazz, fieldName, args=[]) {
    def params = args.collect{arg->
      new VariableExpression(arg.name)
    }
    params.add(0,new VariableExpression("this"))
    block([ret(call(call(classForName('clojure.lang.RT'), 'var', [str(clazz), str(fieldName)]), 'invoke',params))])
  }

  private getClassCompiler() {
    classForName('clojure.lang.Compiler')
  }

  private getClassRT() {
    classForName('clojure.lang.RT')
  }

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