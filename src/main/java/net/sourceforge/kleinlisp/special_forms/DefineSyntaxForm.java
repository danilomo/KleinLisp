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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import net.sourceforge.kleinlisp.LispEnvironment;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.functional.Tuple2;
import net.sourceforge.kleinlisp.macros.MacroDefinition;
import net.sourceforge.kleinlisp.macros.MacroRule;
import net.sourceforge.kleinlisp.macros.MacroTransformation;
import net.sourceforge.kleinlisp.macros.PatternMatcher;
import net.sourceforge.kleinlisp.objects.AtomObject;
import net.sourceforge.kleinlisp.objects.ListObject;
import net.sourceforge.kleinlisp.objects.VoidObject;

/**
 * @author danilo
 */
public class DefineSyntaxForm implements SpecialForm {

  private static final String SYNTAX_RULES = "syntax-rules";
  private final LispEnvironment environment;

  public DefineSyntaxForm(LispEnvironment environment) {
    this.environment = environment;
  }

  @Override
  public Supplier<LispObject> apply(LispObject t) {
    Optional<Tuple2<AtomObject, MacroDefinition>> macroDefOpt =
        Optional.ofNullable(t.asList())
            .map(l -> l.cdr())
            .flatMap(o -> o.unpack(AtomObject.class, ListObject.class))
            .map(o -> o.apply(this::parseMacroDefinition));
    Tuple2<AtomObject, MacroDefinition> macroDef = macroDefOpt.get();
    environment.registerMacro(macroDef.first(), macroDef.second());

    return () -> {
      return VoidObject.VOID;
    };
  }

  private Tuple2<AtomObject, MacroDefinition> parseMacroDefinition(
      AtomObject name, ListObject body) {
    AtomObject atom = body.car().asAtom();
    ListObject variables = body.cdr().car().asList();
    ListObject rules = body.cdr().cdr();

    MacroDefinition macro = getMacroDefinition(atom, variables, rules);

    return new Tuple2<>(name, macro);
  }

  MacroDefinition getMacroDefinition(AtomObject head, ListObject variables, ListObject rules) {
    if (head != environment.atomOf(SYNTAX_RULES)) {
      return null;
    }

    Set<AtomObject> syntaxVariables = parseSyntaxVariables(variables);

    List<MacroRule> macroRules = parseMacroRules(rules, syntaxVariables);

    return new SyntaxRulesMacro(macroRules);
  }

  private Set<AtomObject> parseSyntaxVariables(ListObject list) {
    Set<AtomObject> result = new HashSet<>();

    for (LispObject obj : list) {
      AtomObject atom = obj.asAtom();
      result.add(atom);
    }

    return result;
  }

  private List<MacroRule> parseMacroRules(ListObject rules, Set<AtomObject> syntaxVariables) {
    List<MacroRule> macroRules = new ArrayList<>();

    for (LispObject obj : rules) {
      Tuple2<ListObject, ListObject> tuple =
          Optional.ofNullable(obj.asList())
              .flatMap(o -> o.unpack(ListObject.class, ListObject.class))
              .get();

      MacroRule rule =
          new MacroRule(
              new PatternMatcher(tuple.first(), syntaxVariables),
              new MacroTransformation(tuple.second()));

      macroRules.add(rule);
    }

    return macroRules;
  }
}
