package org.example.simulations;

import org.example.domain.Component;
import org.example.domain.gates.AndGate;
import org.example.domain.gates.NotGate;
import org.example.domain.gates.OrGate;
import org.example.domain.io.LED;
import org.example.domain.io.Switch;

import java.util.HashMap;
import java.util.Map;

public class BooleanExpressionGenerator {

    private Map<Component, String> memo = new HashMap<>();

    public String generateExpression(Component c) {

        // Memoize to avoid repeated work
        if (memo.containsKey(c)) return memo.get(c);

        if (c instanceof Switch sw) {
            String varName = sw.getLabel() != null ? sw.getLabel() : "IN_" + sw.hashCode();
            memo.put(c, varName);
            return varName;
        }

        if (c instanceof LED led) {
            String inputExpr = generateExpression(led.getInputs().get(0).getParent());
            memo.put(c, inputExpr);
            return inputExpr;
        }

        if (c instanceof AndGate g) {
            String a = generateExpression(g.getInputs().get(0).getParent());
            String b = generateExpression(g.getInputs().get(1).getParent());
            String expr = "(" + a + " AND " + b + ")";
            memo.put(c, expr);
            return expr;
        }

        if (c instanceof OrGate g) {
            String a = generateExpression(g.getInputs().get(0).getParent());
            String b = generateExpression(g.getInputs().get(1).getParent());
            String expr = "(" + a + " OR " + b + ")";
            memo.put(c, expr);
            return expr;
        }

        if (c instanceof NotGate g) {
            String a = generateExpression(g.getInputs().get(0).getParent());
            String expr = "(NOT " + a + ")";
            memo.put(c, expr);
            return expr;
        }

        return "UNKNOWN";
    }
}