/*
 * MIT License
 *
 * Copyright (c) 2018 Danilo Oliveira
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.sourceforge.kleinlisp.parser;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import net.sourceforge.kleinlisp.LispEnvironment;
import net.sourceforge.kleinlisp.LispException;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.Parser;

/**
 * @author daolivei
 */
public class CUPParser implements Parser {

  @Override
  public void parse(String expression, LispEnvironment env, Consumer<LispObject> consumer) {
    parser p = null;

    try {
      ByteArrayInputStream in = new ByteArrayInputStream(expression.getBytes());
      p = new parser(new LexicalAnalyzer(in)).withEnvironment(env).withConsumer(consumer);

      p.parse();
    } catch (LispException ex) {
      throw ex;
    } catch (Exception ex) {
      ex.printStackTrace();
      throw new RuntimeException(ex);
    }
  }

  @Override
  public void parse(Path path, LispEnvironment env, Consumer<LispObject> consumer) {
    parser p = null;

    try {
      BufferedInputStream in = new BufferedInputStream(Files.newInputStream(path));
      p =
          new parser(new LexicalAnalyzer(in))
              .withEnvironment(env)
              .withSourceFile(path.toAbsolutePath().toString())
              .withConsumer(consumer);

      p.parse();
    } catch (LispException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }
}
