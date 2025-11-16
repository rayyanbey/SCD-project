package org.example.simulations;

import javax.script.ScriptException;
import java.util.Map;

public class BooleanEvaluator {

    /**
     * Evaluates a boolean expression using input values.
     * Example expression: (A AND B) OR NOT C
     */
    public boolean evaluate(String expression, Map<String, Boolean> inputs) {

        String expr = expression.toUpperCase();

        // Replace variables with true/false
        for (String key : inputs.keySet()) {
            expr = expr.replace(key.toUpperCase(), inputs.get(key).toString());
        }

        // Replace logical operators
        expr = expr.replace("AND", "&&")
                .replace("OR", "||")
                .replace("NOT", "!")
                .replace("XOR", "^");

        try {
            return evalJavaBoolean(expr);
        } catch (Exception e) {
            throw new RuntimeException("Invalid boolean expression: " + expression);
        }
    }

    // Simple safe evaluation of Java boolean expression
    private boolean evalJavaBoolean(String expr) throws InstantiationException, IllegalAccessException, ScriptException {
        return javax.script.ScriptEngineManager.class
                .newInstance()
                .getEngineByName("JavaScript")
                .eval(expr)
                .equals(Boolean.TRUE);
    }
}