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
package net.sourceforge.kleinlisp.special_forms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import net.sourceforge.kleinlisp.LispEnvironment;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.evaluator.Evaluator;
import net.sourceforge.kleinlisp.functional.Tuple2;
import net.sourceforge.kleinlisp.macros.MacroDefinition;
import net.sourceforge.kleinlisp.macros.MacroRule;
import net.sourceforge.kleinlisp.macros.MacroTransformation;
import net.sourceforge.kleinlisp.macros.PatternMatcher;
import net.sourceforge.kleinlisp.objects.AtomObject;
import net.sourceforge.kleinlisp.objects.ListObject;

/**
 * Implements let-syntax for local macro definitions.
 *
 * <p>Syntax: (let-syntax ((name transformer) ...) body ...)
 *
 * <p>The transformers are syntax-rules forms. The macros are visible only within the body. This
 * implements hygienic macros - the macro expansion uses the lexical environment at definition time.
 *
 * @author danilo
 */
public class LetSyntaxForm implements SpecialForm {

  private static final String SYNTAX_RULES = "syntax-rules";
  private final Evaluator evaluator;
  private final LispEnvironment environment;

  public LetSyntaxForm(Evaluator evaluator, LispEnvironment environment) {
    this.evaluator = evaluator;
    this.environment = environment;
  }

  @Override
  public Supplier<LispObject> apply(LispObject t) {
    ListObject form = t.asList();
    FormErrors.assertMinArgs("let-syntax", form, 2);

    ListObject list = form.cdr();
    LispObject bindingsObj = list.car();
    ListObject body = list.cdr();

    // Parse macro bindings
    Map<AtomObject, MacroDefinition> localMacros = new HashMap<>();
    if (bindingsObj.asList() != null) {
      for (LispObject binding : bindingsObj.asList()) {
        Tuple2<AtomObject, MacroDefinition> macroDef = parseMacroBinding(binding);
        if (macroDef != null) {
          localMacros.put(macroDef.first(), macroDef.second());
        }
      }
    }

    // Expand the body with the local macros (combined with global macros)
    LispObject expandedBody = expandWithLocalMacros(body, localMacros);

    // Compile and evaluate the expanded body
    List<Supplier<LispObject>> bodySuppliers = new ArrayList<>();
    if (expandedBody.asList() != null) {
      for (LispObject expr : expandedBody.asList()) {
        bodySuppliers.add(expr.accept(evaluator));
      }
    }

    return () -> {
      LispObject result = ListObject.NIL;
      for (Supplier<LispObject> supplier : bodySuppliers) {
        result = supplier.get();
      }
      return result;
    };
  }

  private Tuple2<AtomObject, MacroDefinition> parseMacroBinding(LispObject binding) {
    ListObject tuple = binding.asList();
    if (tuple == null || tuple.length() < 2) {
      return null;
    }

    AtomObject name = tuple.car().asAtom();
    ListObject transformerForm = tuple.cdr().car().asList();
    if (name == null || transformerForm == null) {
      return null;
    }

    MacroDefinition macro = parseSyntaxRules(transformerForm);
    if (macro == null) {
      return null;
    }

    return new Tuple2<>(name, macro);
  }

  private MacroDefinition parseSyntaxRules(ListObject form) {
    AtomObject head = form.car().asAtom();
    if (head == null || !head.toString().equals(SYNTAX_RULES)) {
      return null;
    }

    ListObject variables = form.cdr().car().asList();
    ListObject rules = form.cdr().cdr();

    Set<AtomObject> syntaxVariables = parseSyntaxVariables(variables);
    List<MacroRule> macroRules = parseMacroRules(rules, syntaxVariables);

    return new SyntaxRulesMacro(macroRules);
  }

  private Set<AtomObject> parseSyntaxVariables(ListObject list) {
    Set<AtomObject> result = new HashSet<>();
    if (list != null) {
      for (LispObject obj : list) {
        AtomObject atom = obj.asAtom();
        if (atom != null) {
          result.add(atom);
        }
      }
    }
    return result;
  }

  private List<MacroRule> parseMacroRules(ListObject rules, Set<AtomObject> syntaxVariables) {
    List<MacroRule> macroRules = new ArrayList<>();

    for (LispObject obj : rules) {
      Optional<Tuple2<ListObject, ListObject>> tupleOpt =
          Optional.ofNullable(obj.asList())
              .flatMap(o -> o.unpack(ListObject.class, ListObject.class));

      if (tupleOpt.isPresent()) {
        Tuple2<ListObject, ListObject> tuple = tupleOpt.get();
        MacroRule rule =
            new MacroRule(
                new PatternMatcher(tuple.first(), syntaxVariables),
                new MacroTransformation(tuple.second()));
        macroRules.add(rule);
      }
    }

    return macroRules;
  }

  private LispObject expandWithLocalMacros(
      LispObject body, Map<AtomObject, MacroDefinition> localMacros) {
    // Create a combined macro map with local macros shadowing global ones
    Map<AtomObject, MacroDefinition> combinedMacros = environment.getMacroTable();

    // Add local macros (these take precedence over global ones)
    combinedMacros.putAll(localMacros);

    // Expand the body
    return environment.expandMacrosWithTable(body, combinedMacros);
  }
}
