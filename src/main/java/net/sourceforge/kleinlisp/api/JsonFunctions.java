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

import net.sourceforge.kleinlisp.LispArgumentError;
import net.sourceforge.kleinlisp.LispObject;
import net.sourceforge.kleinlisp.objects.BooleanObject;
import net.sourceforge.kleinlisp.objects.DoubleObject;
import net.sourceforge.kleinlisp.objects.IntObject;
import net.sourceforge.kleinlisp.objects.ListObject;
import net.sourceforge.kleinlisp.objects.PMapObject;
import net.sourceforge.kleinlisp.objects.StringObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.pcollections.HashTreePMap;
import org.pcollections.PMap;

/**
 * JSON parsing functions for KleinLisp. Provides conversion between JSON strings and persistent
 * data structures.
 */
public class JsonFunctions {

  /**
   * Parses a JSON string and converts it to KleinLisp data structures. JSON objects become
   * persistent maps (PMapObject), JSON arrays become lists, JSON strings become StringObject, JSON
   * numbers become IntObject or DoubleObject, JSON booleans become BooleanObject, and JSON null
   * becomes NIL.
   *
   * <p>Usage: (json-parse "{\"key\": \"value\"}")
   */
  public static LispObject jsonParse(LispObject[] params) {
    if (params.length == 0) {
      throw new LispArgumentError("json-parse requires a string argument");
    }
    StringObject jsonStr = params[0].asString();
    if (jsonStr == null) {
      throw new LispArgumentError("json-parse requires a string argument");
    }

    String json = jsonStr.value().trim();
    if (json.isEmpty()) {
      throw new LispArgumentError("json-parse: empty string is not valid JSON");
    }

    try {
      if (json.startsWith("{")) {
        return parseObject(new JSONObject(json));
      } else if (json.startsWith("[")) {
        return parseArray(new JSONArray(json));
      } else {
        // Try parsing as a primitive value
        return parsePrimitive(json);
      }
    } catch (JSONException e) {
      throw new LispArgumentError("json-parse: invalid JSON - " + e.getMessage());
    }
  }

  private static LispObject parseObject(JSONObject obj) {
    PMap<LispObject, LispObject> map = HashTreePMap.empty();
    for (String key : obj.keySet()) {
      LispObject keyObj = new StringObject(key);
      LispObject valueObj = parseValue(obj.get(key));
      map = map.plus(keyObj, valueObj);
    }
    return new PMapObject(map);
  }

  private static LispObject parseArray(JSONArray arr) {
    if (arr.length() == 0) {
      return ListObject.NIL;
    }
    LispObject[] elements = new LispObject[arr.length()];
    for (int i = 0; i < arr.length(); i++) {
      elements[i] = parseValue(arr.get(i));
    }
    return ListObject.fromList(elements);
  }

  private static LispObject parseValue(Object value) {
    if (value == null || value == JSONObject.NULL) {
      return ListObject.NIL;
    } else if (value instanceof JSONObject) {
      return parseObject((JSONObject) value);
    } else if (value instanceof JSONArray) {
      return parseArray((JSONArray) value);
    } else if (value instanceof String) {
      return new StringObject((String) value);
    } else if (value instanceof Boolean) {
      return (Boolean) value ? BooleanObject.TRUE : BooleanObject.FALSE;
    } else if (value instanceof Integer) {
      return IntObject.valueOf((Integer) value);
    } else if (value instanceof Long) {
      long longVal = (Long) value;
      if (longVal >= Integer.MIN_VALUE && longVal <= Integer.MAX_VALUE) {
        return IntObject.valueOf((int) longVal);
      }
      return new DoubleObject(longVal);
    } else if (value instanceof Number) {
      double d = ((Number) value).doubleValue();
      if (d == Math.floor(d) && d >= Integer.MIN_VALUE && d <= Integer.MAX_VALUE) {
        return IntObject.valueOf((int) d);
      }
      return new DoubleObject(d);
    } else {
      return new StringObject(value.toString());
    }
  }

  private static LispObject parsePrimitive(String json) {
    // Try to parse as a JSON primitive wrapped in array
    try {
      JSONArray wrapper = new JSONArray("[" + json + "]");
      return parseValue(wrapper.get(0));
    } catch (JSONException e) {
      throw new LispArgumentError("json-parse: invalid JSON primitive - " + json);
    }
  }
}
