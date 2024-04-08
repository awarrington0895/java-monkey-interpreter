package com.warrington.ast;

interface Node {
    String tokenLiteral();

    String toString();
}

interface Expression extends Node {
}
