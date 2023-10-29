package com.warrington.ast;

import java.util.ArrayList;
import java.util.List;

public class Program implements Node {
    private final List<Statement> statements = new ArrayList<>();

    @Override
    public String tokenLiteral() {
        if (statements.size() > 0) {
            return statements.get(0).tokenLiteral();
        } else {
            return "";
        }
    }

    public List<Statement> getStatements() {
        return statements;
    }
}