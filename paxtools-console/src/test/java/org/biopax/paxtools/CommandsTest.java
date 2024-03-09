package org.biopax.paxtools;

import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.Model;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import static org.junit.jupiter.api.Assertions.*;

@Disabled
public class CommandsTest {

  static Model m;

  static {
    try {
      m = new SimpleIOHandler().convertFromOWL(new GZIPInputStream(
        new FileInputStream("~/pc14test.owl.gz"))
      );
    } catch (IOException e) {
      fail("Cannot import test data file: {}", e);
    }
  }

  @Test
  public void mapUriToIds() throws IOException {
    Commands.mapUriToIds(m, System.out);
  }

  @Test
  void summarize() throws IOException {
    Commands.summarize(m, System.out);
  }

}