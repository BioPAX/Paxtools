package org.biopax.paxtools.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class HGNCTest {

  // Example lines to test:
  // HGNC ID	Approved symbol	Previous symbols	NCBI Gene ID
  // HGNC:5	A1BG		1
  // HGNC:29	ABCA1	ABC1, HDLDT1	19
  // HGNC:20293	ABHD13	C13orf6	84945
  // ...

  @ParameterizedTest
  @CsvSource(textBlock = """
      5, A1BG
      HGNC:5, A1BG
      A1BG, A1BG
      a1bg, A1BG
      ABCA1, ABCA1
      HGNC:29, ABCA1
      C13orf6, ABHD13
      20293, ABHD13
      foo,
      ,
      """
  )
  void getSymbolByHgncIdOrSym(String input, String expected) {
    assertEquals(expected, HGNC.getSymbolByHgncIdOrSym(input));
  }

  @ParameterizedTest
  @CsvSource(textBlock = """
      A1BG, HGNC:5
      a1bg, HGNC:5
      C13orf6, HGNC:20293
      ABHD13, HGNC:20293
      foobar,
       , 
      """
  )
  //"a1bg, HGNC:5" passes because when no match found by "a1bg", it then tries the uppercase value as well...
  void getHgncId(String input, String expected) {
    assertEquals(expected, HGNC.getHgncId(input));
  }

  @ParameterizedTest
  @CsvSource(textBlock = """
      A1BG, 1
      a1bg, 1
      C13orf6, 84945
      ABHD13, 84945
      foo,
      ,
      """
  )
  void getGeneId(String input, String expected) {
    assertEquals(expected, HGNC.getGeneId(input));
  }

  @ParameterizedTest
  @CsvSource(textBlock = """
      1, A1BG
      84945, ABHD13
      19, ABCA1
      0,
      ,
      """
  )
  void getSymbolByGeneId(String input, String expected) {
    assertEquals(expected, HGNC.getSymbolByGeneId(input));
  }

  @ParameterizedTest
  @CsvSource(textBlock = """
      1, true
      84945, true
      19, true
      0, false
      , false
      """
  )
  void containsGeneId(String input, Boolean expected) {
    assertEquals(expected, HGNC.containsGeneId(input));
  }
}