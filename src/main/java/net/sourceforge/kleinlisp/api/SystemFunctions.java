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
package net.sourceforge.kleinlisp.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.sourceforge.kleinlisp.LispArgumentError;
import net.sourceforge.kleinlisp.LispEnvironment;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.objects.BooleanObject;
import net.sourceforge.kleinlisp.objects.ListObject;
import net.sourceforge.kleinlisp.objects.StringObject;
import net.sourceforge.kleinlisp.objects.VoidObject;

/** System interface functions for KleinLisp (R7RS). */
public class SystemFunctions {

  private static List<String> commandLineArgs = new ArrayList<>();

  private final LispEnvironment environment;

  public SystemFunctions(LispEnvironment environment) {
    this.environment = environment;
  }

  /** Sets the command line arguments for use by (command-line). */
  public static void setCommandLineArgs(String[] args) {
    commandLineArgs = new ArrayList<>();
    for (String arg : args) {
      commandLineArgs.add(arg);
    }
  }

  /** Returns a list of feature identifiers. (features) */
  public LispObject features(LispObject[] params) {
    if (params.length != 0) {
      throw new LispArgumentError("features: expected 0 arguments");
    }
    List<LispObject> featureList = new ArrayList<>();
    featureList.add(environment.atomOf("r7rs"));
    featureList.add(environment.atomOf("kleinlisp"));
    featureList.add(environment.atomOf("java"));

    // Add platform-specific features
    String os = System.getProperty("os.name").toLowerCase();
    if (os.contains("win")) {
      featureList.add(environment.atomOf("windows"));
    } else if (os.contains("mac")) {
      featureList.add(environment.atomOf("macosx"));
      featureList.add(environment.atomOf("posix"));
    } else if (os.contains("nix") || os.contains("nux") || os.contains("bsd")) {
      featureList.add(environment.atomOf("posix"));
    }

    return ListObject.fromList(featureList.toArray(new LispObject[0]));
  }

  /** Returns the command line arguments. (command-line) */
  public LispObject commandLine(LispObject[] params) {
    if (params.length != 0) {
      throw new LispArgumentError("command-line: expected 0 arguments");
    }
    if (commandLineArgs.isEmpty()) {
      return ListObject.NIL;
    }
    LispObject[] args = new LispObject[commandLineArgs.size()];
    for (int i = 0; i < commandLineArgs.size(); i++) {
      args[i] = new StringObject(commandLineArgs.get(i));
    }
    return ListObject.fromList(args);
  }

  /** Gets an environment variable. (get-environment-variable name) */
  public LispObject getEnvironmentVariable(LispObject[] params) {
    if (params.length != 1) {
      throw new LispArgumentError("get-environment-variable: expected 1 argument");
    }
    StringObject nameObj = params[0].asString();
    if (nameObj == null) {
      throw new LispArgumentError("get-environment-variable: expected string");
    }
    String value = System.getenv(nameObj.value());
    if (value == null) {
      return BooleanObject.FALSE;
    }
    return new StringObject(value);
  }

  /** Returns an association list of all environment variables. (get-environment-variables) */
  public LispObject getEnvironmentVariables(LispObject[] params) {
    if (params.length != 0) {
      throw new LispArgumentError("get-environment-variables: expected 0 arguments");
    }
    List<LispObject> alist = new ArrayList<>();
    for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
      LispObject pair =
          new ListObject(new StringObject(entry.getKey()), new StringObject(entry.getValue()));
      alist.add(pair);
    }
    return ListObject.fromList(alist.toArray(new LispObject[0]));
  }

  /** Exits the program. (exit) or (exit obj) */
  public LispObject exit(LispObject[] params) {
    int code = 0;
    if (params.length > 0) {
      if (params[0] instanceof BooleanObject) {
        code = ((BooleanObject) params[0]).truthiness() ? 0 : 1;
      } else if (params[0].asInt() != null) {
        code = params[0].asInt().value;
      }
    }
    System.exit(code);
    return VoidObject.VOID; // Never reached
  }

  /** Emergency exit without cleanup. (emergency-exit) or (emergency-exit obj) */
  public LispObject emergencyExit(LispObject[] params) {
    int code = 0;
    if (params.length > 0 && params[0].asInt() != null) {
      code = params[0].asInt().value;
    }
    Runtime.getRuntime().halt(code);
    return VoidObject.VOID; // Never reached
  }
}
