package com.c2scala

import scala.collection.mutable.ListBuffer

import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.Token
import scala.collection.mutable.HashMap

case class Typedef(name: String, expr: String)

class TypedefConverter(cTypes: HashMap[String, String]) extends ChainListener[Unit](cTypes) {
  val typeSpecs = ListBuffer[CParser.TypeSpecifierContext]()
  
  var struct: Struct = null
  var typedef: Typedef = null
  var enumeration: Enumeration = null
    
  override def visitDeclaration(ctx: CParser.DeclarationContext) = {
    typeSpecs.clear

    super.visitDeclaration(ctx)
    
    if (struct != null) {
      var result = "class " + struct.name + " {\n"
      if (!struct.structDecl.isEmpty) {
        result += struct.structDecl.map("  " + _).reduce{_ + "\n" + _}
      }
      result += "\n}"
      results += result
    } else if (enumeration != null) {
        results += "type " + enumeration.name + " = Int"
        enumeration.enumerators.foreach{enum =>
          results += ("val " + enum.name + ": " + enumeration.name + " = " + enum.expression)
      }
    } else if (typedef != null) {
      results += "type " + typedef.name + " = " + typedef.expr + "\n"
      cTypes += typedef.name -> typedef.expr
    } 
  }
  
  override def visitDirectDeclarator(ctx: CParser.DirectDeclaratorContext) = {
    typedef = Typedef(ctx.getText, "Array[" + typeSpecs(0).getText + "]")
    super.visitDirectDeclarator(ctx)
  }
  
  override def visitTypeSpecifier(ctx: CParser.TypeSpecifierContext) = {
    super.visitTypeSpecifier(ctx)
    if (ctx.getText != "unsigned" && ctx.getText != "signed") {
      typeSpecs += ctx
    }
    
    if (typeSpecs.size == 2) {
      typedef = Typedef(typeSpecs(1).getText, translateTypeSpec(typeSpecs(0)))
    }
    
  }
    
  override def visitEnumSpecifier(ctx: CParser.EnumSpecifierContext) = {
    enumeration = new EnumConverter(cTypes).visit(ctx)
  }
  
  override def visitFunctionDefinition(ctx: CParser.FunctionDefinitionContext) = {
    results ++= new FunctionConverter(cTypes, true).visit(ctx)
    super.visitFunctionDefinition(ctx)
  }
  
  override def visitStructOrUnionSpecifier(ctx: CParser.StructOrUnionSpecifierContext) = {
    struct = new StructConverter(cTypes).visit(ctx)
  }
}