package com.warrington.monkey.ast;

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

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        statements.forEach(stmt -> builder.append(stmt.toString()));

        return builder.toString();
    }

    public void addStatement(Statement statement) {
        statements.add(statement);
    }

    public List<Statement> getStatements() {
        return statements;
    }
}