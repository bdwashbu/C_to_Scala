package com.c2scala

import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer

class DeclaratorConverter(cTypes: HashMap[String, String], typeName: String) extends ChainListener[Unit](cTypes) {
  val directDeclarators = ListBuffer[String]()
  val initializerList = ListBuffer[String]()
  var varName = ""
  
  def showDec(declList: List[String]): String = {
    if (!declList.isEmpty) {
      if (declList.size > 1) {
        "Array.fill(" + declList.head + ")(" + showDec(declList.tail) + ")"
      } else {
        "Array.fill(" + declList.head + ")(null)"
      }
    } else {
      ""
    }
  }
  
  override def visitInitDeclarator(ctx: CParser.InitDeclaratorContext) = {
    
    super.visitInitDeclarator(ctx)
    
    if (ctx.initializer() != null && !directDeclarators.isEmpty) {
      val arrayType = directDeclarators.toList.map{x => "Array["}.reduce(_ ++ _) + typeName + directDeclarators.toList.map{x => "]"}.reduce(_ ++ _)
      results += "var " + varName + ": " + arrayType + " = Array(" + initializerList.reduce(_ ++ "," ++ _) + ")"
    } else if (!directDeclarators.isEmpty) {
      val arrayType = directDeclarators.toList.map{x => "Array["}.reduce(_ ++ _) + typeName + directDeclarators.toList.map{x => "]"}.reduce(_ ++ _)
      val value = showDec(directDeclarators.toList)
      results += "var " + varName + ": " + arrayType + " = " + value
    }
  }
  
  override def visitInitializer(ctx: CParser.InitializerContext) = {
    if (ctx.assignmentExpression != null)
      initializerList += ctx.getText
    else if (ctx.initializerList != null) {
      initializerList.clear
      super.visitInitializer(ctx)
    }
  }
  
  //convertedToScala("int blah[1][2];").head should equal("var blah: Array[Array[Int]] = Array.fill(1)(Array.fill(2)(null))")
  
  override def visitDirectDeclarator(ctx: CParser.DirectDeclaratorContext) = {
    super.visitDirectDeclarator(ctx)
    
    if (ctx.assignmentExpression() == null) {
      varName = ctx.getText
    } else if (ctx.assignmentExpression() != null) {
      directDeclarators += ctx.assignmentExpression().getText
    }
    
    
  }
  
}