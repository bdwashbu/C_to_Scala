package tests

import org.scalatest._

class Statement extends FlatSpec with ShouldMatchers {

  "A simple statement" should "convert correctly" in {
    convertedToScala("int blah;").head should equal("var blah: Int = 0")
  }
  
  "A simple statement with custom type" should "convert correctly" in {
    convertedToScala("LATLON blah;").head should equal("var blah: LATLON = null")
  }
  
  "Two variables of a custom type being simultaneously declared" should "convert correctly" in {
    convertedToScala("LATLON x, y;").head should equal("var (x: LATLON, y: LATLON) = (null, null)")
  }
   
  "Two variables of a primitive type being simultaneously declared" should "convert correctly" in {
    convertedToScala("float x, y;").head should equal("var (x: Float, y: Float) = (0.0, 0.0)")
  }
  
  "Four variables of a primitive type being simultaneously declared" should "convert correctly" in {
    convertedToScala("float x, y, r, s;").head should equal("var (x: Float, y: Float, r: Float, s: Float) = (0.0, 0.0, 0.0, 0.0)")
  }
  
//  "Two variables of a primitive type being simultaneously declared" should "convert correctly" in {
//    convertedToScala("float x = 2.0, y = 3.0;").head should equal("var (x: Float, y: Float) = (2.0, 3.0)")
//  }
}