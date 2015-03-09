package com.c2scala

import scala.collection.mutable.ListBuffer
import scala.collection.mutable.HashMap

class DeclarationConverter(cTypes: HashMap[String, String]) extends ChainListener[Unit](cTypes) {

  val typedefNames = ListBuffer[String]()
  val directDeclarators = ListBuffer[String]()
  val explicitInitValues = ListBuffer[String]()
  var isTypedef = false
  var hasStorageSpecifier = false
  var latestTypeSpec: CParser.TypeSpecifierContext = null
    
  override def visitDeclaration(ctx: CParser.DeclarationContext) = {
    isTypedef = false
    latestTypeSpec = null
    hasStorageSpecifier = false
    typedefNames.clear
    
    super.visitDeclaration(ctx)
    
    if (isTypedef) {
      val typedefConverter = new TypedefConverter(cTypes)
      typedefConverter.visitDeclaration(ctx)
      results ++= typedefConverter.results
    } else if (!hasStorageSpecifier) {
      if (directDeclarators.size > 1) {
        val typeName = if (!typedefNames.isEmpty) typedefNames(0) else translateTypeSpec(latestTypeSpec)
        val decl = "(" + directDeclarators.map(_ + ": " + typeName).reduce(_ + ", " + _) + ")"
        val baseTypeDefault = getTypeDefault(cTypes.withDefaultValue(typeName)(typeName))
        val defaults: String = "(" + directDeclarators.zipWithIndex.map{ case (decl, index) =>
          if (index < explicitInitValues.size) {
            explicitInitValues(index)
          } else {
            baseTypeDefault
          }
        }.reduce(_ + ", " + _) + ")"
        results += "var " + decl + " = " + defaults + "\n"
      } else if (typedefNames.size == 1) {
        val baseTypeDefault = getTypeDefault(cTypes.withDefaultValue(latestTypeSpec.getText)(latestTypeSpec.getText))
        results += "var " + typedefNames(0) + ": " + translateTypeSpec(latestTypeSpec) + " = " + baseTypeDefault + "\n"
      } else if (typedefNames.size == 2) {
        val baseTypeDefault = getTypeDefault(cTypes.withDefaultValue(typedefNames(1))(typedefNames(1)))
        results += "var " + typedefNames(1) + ": " + typedefNames(0) + " = " + baseTypeDefault + "\n"
      }
    } 
  }
  
  override def visitInitDeclaratorList(ctx: CParser.InitDeclaratorListContext) = {
    directDeclarators.clear
    super.visitInitDeclaratorList(ctx)
  }
  
  override def visitInitializer(ctx: CParser.InitializerContext) = {
    explicitInitValues += ctx.getText
  }
  
  override def visitDirectDeclarator(ctx: CParser.DirectDeclaratorContext) = {
    directDeclarators += ctx.getText
    super.visitDirectDeclarator(ctx)
  }
     
  override def visitTypedefName(ctx: CParser.TypedefNameContext) = {
    typedefNames += ctx.Identifier().getText
  }
    
  override def visitTypeSpecifier(ctx: CParser.TypeSpecifierContext) = {
    super.visitTypeSpecifier(ctx)
    if (ctx.typedefName() == null) {
      latestTypeSpec = ctx
    }
  }
  
  override def visitFunctionDefinition(ctx: CParser.FunctionDefinitionContext) = {
    results ++= new FunctionConverter(cTypes).visitFunctionDefinition(ctx)
    super.visitFunctionDefinition(ctx)
  }
 
  override def visitStorageClassSpecifier(ctx: CParser.StorageClassSpecifierContext) = {
    hasStorageSpecifier = true
    if (ctx.getText == "typedef") {
      isTypedef = true
    }
  }

}