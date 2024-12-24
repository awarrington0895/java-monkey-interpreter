package com.warrington.monkey.evaluator;

import com.warrington.monkey.ast.Program;
import com.warrington.monkey.lexer.Lexer;
import com.warrington.monkey.object.Bool;
import com.warrington.monkey.object.Int;
import com.warrington.monkey.object.MonkeyObject;
import com.warrington.monkey.parser.Parser;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.warrington.monkey.evaluator.Evaluator.NULL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

class EvaluatorTest {

    @ParameterizedTest
    @CsvSource({
        "5,5",
        "10,10",
        "-5,-5",
        "-10,-10",
        "5 + 5 + 5 + 5 - 10,10",
        "2 * 2 * 2 * 2 * 2,32",
        "-50 + 100 + -50,0",
        "5 * 2 + 10,20",
        "5 + 2 * 10,25",
        "20 + 2 * -10,0",
        "50 / 2 * 2 + 10,60",
        "2 * (5 + 10),30",
        "3 * 3 * 3 + 10,37",
        "3 * (3 * 3) + 10,37",
        "(5 + 10 * 2 + 15 / 3) * 2 + -10,50"
    })
    void testEvalIntegerExpression(String input, long expected) {
        MonkeyObject evaluated = testEval(input);
        testIntegerObject(evaluated, expected);
    }

    @ParameterizedTest
    @CsvSource({
        "true,true",
        "false,false",
        "1 < 2,true",
        "1 > 2,false",
        "1 < 1,false",
        "1 > 1,false",
        "1 == 1,true",
        "1 != 1,false",
        "1 == 2,false",
        "1 != 2,true",
        "true == true,true",
        "false == false,true",
        "true == false,false",
        "true != false,true",
        "false != true,true"
    })
    void testEvalBooleanExpression(String input, boolean expected) {
        MonkeyObject evaluated = testEval(input);
        testBooleanObject(evaluated, expected);
    }

    @ParameterizedTest
    @CsvSource({
        "!false,true",
        "!true,false",
        "!5,false",
        "!!true,true",
        "!!false,false",
        "!!5, true"
    })
    void testBangOperator(String input, boolean expected) {
        MonkeyObject evaluated = testEval(input);

        testBooleanObject(evaluated, expected);
    }

    private static Stream<Arguments> provideIfElseExpressions() {
        return Stream.of(
            Arguments.of("if (true) { 10 }", 10L),
            Arguments.of("if (false) { 10 }", null),
            Arguments.of("if (1) { 10 }", 10L),
            Arguments.of("if (1 < 2) { 10 }", 10L),
            Arguments.of("if (1 > 2) { 10 }", null),
            Arguments.of("if (1 > 2) { 10 } else { 20 }", 20L),
            Arguments.of("if (1 < 2) { 10 } else { 20 }", 10L)
        );
    }

    @ParameterizedTest
    @MethodSource("provideIfElseExpressions")
    void testIfElseExpressions(String input, Long expected) {
       MonkeyObject evaluated = testEval(input);

       if (expected == null) {
           testNullObject(evaluated);
       } else {
           testIntegerObject(evaluated, expected);
       }
    }

    private void testNullObject(MonkeyObject evaluated) {
        assertThat(evaluated)
            .withFailMessage("object is not NULL. got=%s", evaluated)
            .isEqualTo(NULL);
    }

    private MonkeyObject testEval(String input) {
        final var lexer = new Lexer(input);
        final var parser = new Parser(lexer);
        Program program = parser.parseProgram();

        return Evaluator.eval(program);
    }

    private void testIntegerObject(MonkeyObject obj, long expected) {
        Int result = (Int) obj;

        assertThat(result.value())
            .withFailMessage("object has wrong value. got=%d, want=%d", result.value(), expected)
            .isEqualTo(expected);
    }

    private void testBooleanObject(MonkeyObject obj, boolean expected) {
        Bool result = (Bool) obj;

        assertThat(result.value())
            .withFailMessage("object has wrong value. got=%s, want=%s", result.value(), expected)
            .isEqualTo(expected);
    }

}
