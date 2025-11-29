
package com.unyaunya.minic.semantics;

import java.util.Map;

import lombok.Value;

@Value
public class SemanticInfo {
    Map<String, Map<String, Symbol>> functionSymbols; // functionName -> (varName -> Symbol)
    Map<String, Integer> localSizes; // functionName -> total local size

    public SemanticInfo(Map<String, Map<String, Symbol>> functionSymbols, Map<String, Integer> localSizes) {
        this.functionSymbols = functionSymbols;
        this.localSizes = localSizes;
    }

    public Symbol getSymbol(String functionName, String varName) {
        Map<String, Symbol> localSymbols = functionSymbols.get(functionName);
        if ( localSymbols == null) {
            return null;
        } 
        Symbol symbol = localSymbols.get(varName);
        if (symbol != null) {
            return symbol;
        }
        Map<String, Symbol> globalSymbols = functionSymbols.get("_GLOBAL");
        return globalSymbols.get(varName);
    }

    public int getLocalSize(String functionName) {
        return localSizes.get(functionName);
    }
}
