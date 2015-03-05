package com.c2scala

import scala.collection.mutable.ListBuffer



class CConverter extends CBaseListener {
  var isWithinStruct = false
  var declarationHasStruct = false
  var declarationHasTypedefStruct = false
  val structDeclarations = ListBuffer[String]()
  var specifierQualifierLevel = 0
  var currentTypeName = ""
  val results = ListBuffer[String]()
  var isTypeEnum = false
  var isWithinFunction = false
  var hasTypedefName = false
  
  val typedefNames = ListBuffer[String]()
  var latestStorageSpecifier = ""
  var latestTypeSpecifier = ""
  var latestDirectDeclarator = ""
  
  var latestArraySize = 0
  var isArray = false
  val enumerations = ListBuffer[enumerator]()
  
  var latestStructDecName = ""
  var islatestStructDecArray = false
  
  case class enumerator(constant: String, expression: String)
  
  def convertTypeName(varName: String, typeName: String) = {
    if (varName == "type") {
      typeName.toLowerCase()
    } else {
      varName
    }
  }
  
  def convertTypeSpecifier(typeSpecifier: String) = typeSpecifier match {
    case "char" => "Char"
    case "float" => "Float"
    case "long" => "Long"
    case "short" => "Short"
    case "int" => "Integer"
    case _ => typeSpecifier
  }
  
  def getTypeDefault(typeSpecifier: String) = typeSpecifier match {
    case "char" | "long" | "short" | "int" => "0"
    case "float" | "double" => "0.0"
    case _ => "null"
  }
  
  override def enterSpecifierQualifierList(ctx: CParser.SpecifierQualifierListContext) = {
    specifierQualifierLevel += 1
  }
  
  override def exitSpecifierQualifierList(ctx: CParser.SpecifierQualifierListContext) = {
    specifierQualifierLevel -= 1
  }
  
  override def enterEnumerator(ctx: CParser.EnumeratorContext) = {
    if (ctx.enumerationConstant() != null && ctx.constantExpression() != null) {
      enumerations += enumerator(ctx.enumerationConstant().getText, ctx.constantExpression().getText)
    }
  }
  
  override def enterInitDeclaratorList(ctx: CParser.InitDeclaratorListContext) = {
    isWithinFunction = true
  }
  
  override def enterDirectDeclarator(ctx: CParser.DirectDeclaratorContext) = {
    isArray = true
    islatestStructDecArray = true
    latestDirectDeclarator = ctx.getText
  }
  
  override def exitPrimaryExpression(ctx: CParser.PrimaryExpressionContext) = {
    if (ctx.expression() == null) { // is this the bottom of the tree?!
      latestArraySize = if (ctx.getText.contains("0x")) {
        Integer.getInteger(ctx.getText.drop(2), 16)
      } else if (ctx.getText forall Character.isDigit) {
          ctx.getText.toInt
      } else {
        0
      }
    }
  }

  override def enterEnumSpecifier(ctx: CParser.EnumSpecifierContext) = {
    isTypeEnum = true
  }
   
  override def exitTypedefName(ctx: CParser.TypedefNameContext) = {
    if (!isWithinStruct) {
      typedefNames += ctx.Identifier().getText
    }
    
    hasTypedefName = true
    latestStructDecName = ctx.Identifier().getText
  }
  
  override def enterStructDeclaration(ctx: CParser.StructDeclarationContext) = {
    latestStructDecName = ""
    islatestStructDecArray = false
  }
  
  override def exitStructDeclaration(ctx: CParser.StructDeclarationContext) = {
      if (islatestStructDecArray && latestArraySize != 0) {
        structDeclarations += "var " + latestDirectDeclarator + ": Array[" + convertTypeSpecifier(currentTypeName) + "]" + " = Array.fill(" + latestArraySize + ")(" + getTypeDefault(currentTypeName) + ")"//type " + latestDirectDeclarator + " = Array[" + typedefNames(0) + "]\n"
      } else if (islatestStructDecArray && latestArraySize == 0) {
        structDeclarations += "var " + latestDirectDeclarator + ": Array[" + convertTypeSpecifier(currentTypeName) + "]" + " = null"//type " + latestDirectDeclarator + " = Array[" + typedefNames(0) + "]\n"
      } else if (currentTypeName != "") {
        structDeclarations += "var " + convertTypeName(latestStructDecName, currentTypeName) + ": " + convertTypeSpecifier(currentTypeName) + " = " + getTypeDefault(currentTypeName)
      }
  }
  
  override def enterTypeSpecifier(ctx: CParser.TypeSpecifierContext) = {
    hasTypedefName = false
  }
  
  override def exitTypeSpecifier(ctx: CParser.TypeSpecifierContext) = {
    if (!hasTypedefName)
      latestTypeSpecifier = ctx.getText
      
      if (specifierQualifierLevel == 1) {
        currentTypeName = ctx.getText
      } 
  }
  
  override def enterDeclaration(ctx: CParser.DeclarationContext) = {
    latestStorageSpecifier = ""
    latestTypeSpecifier = ""
    currentTypeName = ""
    declarationHasStruct = false
    isTypeEnum = false
    isWithinFunction = false
    isArray = false
    typedefNames.clear
    enumerations.clear
  }
  
  override def exitDeclaration(ctx: CParser.DeclarationContext) = {
    if (declarationHasStruct && !isWithinStruct) {
      var result = "class " + typedefNames(0) + " {\n"
      //structDeclarations.foreach(println)
      if (!structDeclarations.isEmpty) {
        result += structDeclarations.map("  " + _).reduce{_ + "\n" + _}
      }
      result += "\n}"
      results += result
    } else if (isArray && typedefNames.size == 1) {
      results += "type " + latestDirectDeclarator + " = Array[" + typedefNames(0) + "]\n"
    } else if (!isTypeEnum && !isWithinFunction && latestStorageSpecifier != "extern") {
      if (typedefNames.size == 1) {
        results += "type " + typedefNames(0) + " = " + convertTypeSpecifier(latestTypeSpecifier) + "\n"
      } else if (typedefNames.size == 2) {
        results += "type " + typedefNames(1) + " = " + typedefNames(0) + "\n"
      }
    } else if (isTypeEnum && !enumerations.isEmpty) {
      results += "type " + typedefNames(0) + " = Int"
      enumerations.foreach{enum =>
        results += ("val " + enum.constant + ": " + typedefNames(0) + " = " + enum.expression)
      }
    }
    
    declarationHasStruct = false
  }
  
  override def enterStructOrUnionSpecifier(ctx: CParser.StructOrUnionSpecifierContext) = {
    isWithinStruct = true
    declarationHasStruct = true
    structDeclarations.clear
  }
  
  override def exitStructOrUnionSpecifier(ctx: CParser.StructOrUnionSpecifierContext) = {
    isWithinStruct = false
  }
  
  override def enterStorageClassSpecifier(ctx: CParser.StorageClassSpecifierContext) = {
    //println("ENTERING TYPEDEF: " +ctx.getText)
    latestStorageSpecifier = ctx.getText
  }
  
  override def exitStorageClassSpecifier(ctx: CParser.StorageClassSpecifierContext) = {
    //println("LEAVING TYPEDEF ")
  }
}