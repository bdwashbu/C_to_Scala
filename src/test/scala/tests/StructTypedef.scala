package tests

import org.scalatest._

class StructTypedef extends FlatSpec with ShouldMatchers {

  "A simple typedef" should "convert correctly" in {
    val test = """typedef struct {
                    LATLON lat ;
                    LATLON lon ;
                  } LL;"""
    
    convertedToScala(test) should equal(Array("class LL {",
                                              "var lat: LATLON = null",
                                              "var lon: LATLON = null",
                                              "}"))
  }
  
  "A simple typedef with primitives" should "convert correctly" in {
    val test = """typedef struct {
                    int lat ;
                    float lon ;
                  } LL;"""
    
    convertedToScala(test) should equal(Array("class LL {",
                                              "var lat: Int = 0",
                                              "var lon: Float = 0.0",
                                              "}"))
  }
  
  "A typedef with referenced type default" should "convert correctly" in {
    val test = """typedef int x;
                  typedef struct { 
                    x lat ;
                    float lon ;
                  } LL;"""
    
    convertedToScala(test) should equal(Array("type x = Int",
                                              "class LL {",
                                              "var lat: x = 0",
                                              "var lon: Float = 0.0",
                                              "}"))
  }
  
  "A typedef struct with an array" should "convert correctly" in {
    val test = """typedef struct {
                    LATLON lat[2048] ;
                    LATLON lon ;
                  } LL;"""
    
    convertedToScala(test) should equal(Array("class LL {",
                                              "var lat: Array[LATLON] = Array.fill(2048)(null)",
                                              "var lon: LATLON = null",
                                              "}"))
  }
  
  "A typedef struct with an array size surrounded in parenthesis" should "convert correctly" in {
    val test = """typedef struct {
                    LATLON lat[((2048))] ;
                    LATLON lon ;
                  } LL;"""
    
    convertedToScala(test) should equal(Array("class LL {",
                                              "var lat: Array[LATLON] = Array.fill(2048)(null)",
                                              "var lon: LATLON = null",
                                              "}"))
  }
  
  
  
  "A simple typedef with a pointer" should "convert correctly" in {
    val test = "typedef struct { LATLON lat; LATLON *lon;} LL;"
    
    convertedToScala(test) should equal(Array("class LL {",
                                              "var lat: LATLON = null",
                                              "var lon: Array[LATLON] = null",
                                              "}"))
  }
}