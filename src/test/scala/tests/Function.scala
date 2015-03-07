package tests

import org.scalatest._
import com.c2scala.CParser
import com.c2scala.CLexer

import com.c2scala.CConverter
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeAdaptor;
import org.antlr.runtime.tree.TreeAdaptor;
import org.antlr.runtime.Token;

class Function extends FlatSpec with ShouldMatchers {

  "A simple function" should "convert correctly" in {

    convertedToScala("int blah() {}").head should equal("def blah(): Int = {}")
  }
  
//  "A simple function with a parameter" should "convert correctly" in {
//
//    convertedToScala("int blah(int x) {}").head should equal("def blah(x: Int) = {}")
//  }
}