package com.warrington.monkey.evaluator;

import com.warrington.monkey.ast.Program;
import com.warrington.monkey.lexer.Lexer;
import com.warrington.monkey.object.*;
import com.warrington.monkey.parser.Parser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.warrington.monkey.evaluator.Evaluator.NULL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

class EvaluatorTest {

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

    private static Stream<Arguments> provideReturnStatements() {
        return Stream.of(
            Arguments.of("return 10;", 10L),
            Arguments.of("return 10; 9;", 10L),
            Arguments.of("return 2 * 5; 9;", 10L),
            Arguments.of("9; return 2 * 5; 9;", 10L),
            Arguments.of(
                """
                    if (10 > 1) {
                        if (10 > 1) {
                            return 10;
                        }
                    
                        return 1;
                    }
                    """,
                10
            )
        );
    }

    private static Stream<Arguments> provideErrors() {
        return Stream.of(
            Arguments.of("5 + true;", "type mismatch: INTEGER + BOOLEAN"),
            Arguments.of("5 + true; 5;", "type mismatch: INTEGER + BOOLEAN"),
            Arguments.of("-true", "unknown operator: -BOOLEAN"),
            Arguments.of("true + false", "unknown operator: BOOLEAN + BOOLEAN"),
            Arguments.of("5; true + false; 5", "unknown operator: BOOLEAN + BOOLEAN"),
            Arguments.of("if (10 > 1) { true + false; }", "unknown operator: BOOLEAN + BOOLEAN"),
            Arguments.of("foobar", "identifier not found: foobar"),
            Arguments.of(
                """
                    if (10 > 1) {
                        if (10 > 1) {
                            return true + false;
                        }
                    
                        return 1;
                    }
                    """,
                "unknown operator: BOOLEAN + BOOLEAN"
            ),
            Arguments.of(
                """
                    "Hello" - "World"
                    """,
                "unknown operator: STRING - STRING"
            ),
            Arguments.of(
                """
                    {"name": "Monkey"}[fn(x) { x }];
                    """,
                "unusable as hash key: FUNCTION"
            )
        );
    }

    private static Stream<Arguments> provideLetStatements() {
        return Stream.of(
            Arguments.of("let a = 5; a;", 5),
            Arguments.of("let a = 5 * 5; a;", 25),
            Arguments.of("let a = 5; let b = a; b;", 5),
            Arguments.of("let a = 5; let b = a; let c = a + b + 5; c;", 15)
        );
    }

    private static Stream<Arguments> provideArrayIndexExpressions() {
        return Stream.of(
            Arguments.of("[1, 2, 3][0]", 1L),
            Arguments.of("[1, 2, 3][1]", 2L),
            Arguments.of("[1, 2, 3][2]", 3L),
            Arguments.of("let i = 0; [1][i];", 1L),
            Arguments.of("[1, 2, 3][1 + 1];", 3L),
            Arguments.of("let myArray = [1, 2, 3]; myArray[2];", 3L),
            Arguments.of("let myArray = [1, 2, 3]; myArray[0] + myArray[1] + myArray[2];", 6L),
            Arguments.of("let myArray = [1, 2, 3]; let i = myArray[0]; myArray[i]", 2L),
            Arguments.of("[1, 2, 3][3]", null),
            Arguments.of("[1, 2, 3][-1]", null)
        );
    }

    private static Stream<Arguments> provideHashIndexExpressions() {
        return Stream.of(
            Arguments.of("{\"foo\": 5}[\"foo\"]", 5L),

            Arguments.of("""
                {"foo": 5}["bar"]""", null),

            Arguments.of("""
                let key = "foo"; {"foo": 5}[key]""", 5L),

            Arguments.of("""
                {}["foo"]""", null),

            Arguments.of("""
                {5: 5}[5]
                """, 5L),

            Arguments.of("""
                {true: 5}[true]
                """, 5L),

            Arguments.of("""
                {false: 5}[false]
                """, 5L)
        );
    }

    private static Stream<Arguments> provideBuiltins() {
        return Stream.of(
            Arguments.of("len(\"\")", 0L),
            Arguments.of("len(\"four\")", 4L),
            Arguments.of("len(\"hello world\")", 11L),
            Arguments.of("len(1)", "argument to 'len' not supported, got INTEGER"),
            Arguments.of("len(\"one\", \"two\")", "wrong number of arguments. got=2, want=1"),
            Arguments.of("len([1, 2, 3])", 3L),
            Arguments.of("len([])", 0L),
            Arguments.of("let a = [1+1, 2]; len(a)", 2L),
            Arguments.of("first([10, 15, 20])", 10L),
            Arguments.of("first([])", null),
            Arguments.of("first(1)", "argument to 'first' not supported, got INTEGER"),
            Arguments.of("last([10, 15, 20])", 20L),
            Arguments.of("last([10])", 10L),
            Arguments.of("last([])", null),
            Arguments.of("last(1)", "argument to 'last' not supported, got INTEGER"),
            Arguments.of("rest([10, 15, 20])", List.of(15L, 20L)),
            Arguments.of("rest([10])", Collections.emptyList()),
            Arguments.of("rest([])", null),
            Arguments.of("rest(\"test\")", "argument to 'rest' not supported, got STRING"),

            // Test that rest does not modify original array
            Arguments.of(
                """
                    let a = [1, 2, 3];
                    
                    let b = rest(a);
                    
                    a;
                    """,
                List.of(1L, 2L, 3L)
            ),
            Arguments.of("push([], 1)", List.of(1L)),
            Arguments.of("push([1], 2)", List.of(1L, 2L)),
            Arguments.of("push(1, [2])", "first argument to 'push' must be ARRAY, got INTEGER")
        );
    }

    private static Stream<Arguments> provideFunctionApplications() {
        return Stream.of(
            Arguments.of("let identity = fn(x) { x; }; identity(5);", 5L),
            Arguments.of("let identity = fn(x) { return x; }; identity(5);", 5L),
            Arguments.of("let double = fn(x) { x * 2; }; double(5)", 10L),
            Arguments.of("let add = fn(x, y) { x + y; }; add(5, 5);", 10L),
            Arguments.of("let add = fn(x, y) { x + y; }; add(5 + 5, add(5, 5));", 20L),
            Arguments.of("fn(x) { x; }(5)", 5L)
        );
    }

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
    @MethodSource("provideArrayIndexExpressions")
    void testArrayIndexExpressions(String input, Long expected) {
        MonkeyObject evaluated = testEval(input);

        if (evaluated instanceof Int i) {
            testIntegerObject(i, expected);
        } else {
            testNullObject(evaluated);
        }
    }

    @ParameterizedTest
    @MethodSource("provideHashIndexExpressions")
    void testHashIndexExpressions(String input, Long result) {
        MonkeyObject evaluated = testEval(input);

        if (result == null) {
            testNullObject(evaluated);
        } else {
            testIntegerObject(evaluated, result);
        }
    }

    @Test
    void testHashLiterals() {
        final var input = """
            let two = "two";
             {
             "one": 10 - 9,
             two: 1 + 1,
             "thr" + "ee": 6 / 2,
             4: 4,
             true: 5,
             false: 6
             }""";

        final MonkeyObject evaluated = testEval(input);

        assertThat(evaluated)
            .isInstanceOf(Hash.class);

        final Hash evaledHash = (Hash) evaluated;

        var expected = Map.of(
            new Str("one").hashKey(), 1L,
            new Str("two").hashKey(), 2L,
            new Str("three").hashKey(), 3L,
            new Int(4).hashKey(), 4L,
            Evaluator.TRUE.hashKey(), 5L,
            Evaluator.FALSE.hashKey(), 6L
        );

        assertThat(evaledHash.pairs())
            .hasSize(expected.size());

        expected.forEach((expectedKey, expectedValue) -> {
            final var pair = evaledHash.pairs().get(expectedKey);

            assertThat(pair).isNotNull();

            testIntegerObject(pair.value(), expectedValue);
        });
    }

    @Test
    void testStringHashKey() {
        final var hello1 = new Str("Hello World");
        final var hello2 = new Str("Hello World");
        final var diff1 = new Str("My name is johnny");
        final var diff2 = new Str("My name is johnny");

        assertThat(hello1.hashKey())
            .isEqualTo(hello2.hashKey());

        assertThat(diff1.hashKey())
            .isEqualTo(diff2.hashKey());

        assertThat(hello1.hashKey())
            .isNotEqualTo(diff1.hashKey());
    }

    @Test
    void testBooleanHashKey() {
        final var True = new Bool(true);
        final var False = new Bool(false);

        assertThat(True.hashKey())
            .isEqualTo(True.hashKey());

        assertThat(False.hashKey())
            .isEqualTo(False.hashKey());

        assertThat(True.hashKey())
            .isNotEqualTo(False.hashKey());
    }

    @Test
    void testIntegerHashKey() {
        final var same1 = new Int(10);
        final var same2 = new Int(10);
        final var diff1 = new Int(28);
        final var diff2 = new Int(28);

        assertThat(same1.hashKey())
            .isEqualTo(same2.hashKey());

        assertThat(diff1.hashKey())
            .isEqualTo(diff2.hashKey());

        assertThat(same1.hashKey())
            .isNotEqualTo(diff1.hashKey());
    }

    @Test
    void testArrayLiterals() {
        final var input = "[1, 2 * 2, 3 + 3]";

        MonkeyObject evaluated = testEval(input);

        assertThat(evaluated)
            .withFailMessage("object is not Array. got=%s", evaluated.type())
            .isInstanceOf(Array.class);

        Array array = (Array) evaluated;

        assertThat(array.elements().size())
            .withFailMessage("array has wrong number of elements. got=%d", array.elements().size())
            .isEqualTo(3);

        testIntegerObject(array.elements().getFirst(), 1L);
        testIntegerObject(array.elements().get(1), 4L);
        testIntegerObject(array.elements().get(2), 6L);
    }

    @ParameterizedTest
    @MethodSource("provideBuiltins")
    void testBuiltins(String input, Object expected) {
        MonkeyObject evaluated = testEval(input);

        if (expected == null) {
            testNullObject(evaluated);

            return;
        }

        switch (expected) {
            case Long i -> testIntegerObject(evaluated, i);
            case String s -> {
                assertThat(evaluated)
                    .withFailMessage("object is not Error. got=%s", evaluated)
                    .isInstanceOf(MonkeyError.class);

                final var err = (MonkeyError) evaluated;

                assertThat(err.message())
                    .withFailMessage("wrong error message. expected=%s, got=%s", s, err.message())
                    .isEqualTo(s);
            }
            case List list -> testArrayObject(evaluated, list);
            default -> fail("Unsupported expected value");
        }
    }

    @Test
    void testStringConcatenation() {
        final var input = """
            "Hello" + " " + "World!";
            """;

        MonkeyObject evaluated = testEval(input);

        assertThat(evaluated)
            .withFailMessage("object is not Str. got=%s.".formatted(evaluated.toString()))
            .isInstanceOf(Str.class);

        final var str = (Str) evaluated;

        assertThat(str.value())
            .withFailMessage("value is not correct. want=%s, got=%s.".formatted("Hello World!", str.value()))
            .isEqualTo("Hello World!");
    }

    @Test
    void testStringLiteral() {
        final var input = "\"Hello World!\"";

        MonkeyObject evaluated = testEval(input);

        assertThat(evaluated)
            .withFailMessage("object is not Str. got=%s.".formatted(evaluated.type()))
            .isInstanceOf(Str.class);

        final var str = (Str) evaluated;

        assertThat(str.value())
            .withFailMessage("value is not correct. want=%s, got=%s.".formatted("Hello World!", str.value()))
            .isEqualTo("Hello World!");
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

    @ParameterizedTest
    @MethodSource("provideReturnStatements")
    void testReturnStatements(String input, long expected) {
        MonkeyObject evaluated = testEval(input);

        testIntegerObject(evaluated, expected);
    }

    @ParameterizedTest
    @MethodSource("provideErrors")
    void testErrorHandling(String input, String expectedMessage) {
        MonkeyObject evaluated = testEval(input);

        assertThat(evaluated)
            .withFailMessage("no error object returned. got=%s", evaluated.type())
            .isInstanceOf(MonkeyError.class);

        MonkeyError error = (MonkeyError) evaluated;

        assertThat(error.message())
            .withFailMessage("wrong error message. expected=%s, got=%s", expectedMessage, error.message())
            .isEqualTo(expectedMessage);
    }

    @ParameterizedTest
    @MethodSource("provideLetStatements")
    void testLetStatements(String input, long expected) {
        testIntegerObject(testEval(input), expected);
    }

    @Test
    void testFunctionObject() {
        var input = "fn(x) { x + 2; };";

        MonkeyObject evaluated = testEval(input);

        assertThat(evaluated)
            .withFailMessage("object is not function.  got=%s", evaluated.type())
            .isInstanceOf(MonkeyFunction.class);

        var func = (MonkeyFunction) evaluated;

        assertThat(func.parameters().size())
            .withFailMessage("function has wrong parameters. Parameters=%s", func.parameters())
            .isEqualTo(1);

        assertThat(func.parameters().getFirst().value())
            .withFailMessage("parameter is not 'x'. got=%s", func.parameters().getFirst())
            .isEqualTo("x");

        final var expectedBody = "(x + 2)";

        assertThat(func.body().toString())
            .withFailMessage("body is not %s. got=%s", expectedBody, func.body().toString())
            .isEqualTo(expectedBody);
    }

    @ParameterizedTest
    @MethodSource("provideFunctionApplications")
    void testFunctionApplication(String input, long expected) {
        testIntegerObject(testEval(input), expected);
    }

    @Test
    void testClosures() {
        var input = """
            let newAdder = fn(x) {
                fn (y) { x + y };
            };
            
            let addTwo = newAdder(2);
            addTwo(2);
            """;

        testIntegerObject(testEval(input), 4);
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

        return Evaluator.eval(program, new Environment());
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

    private void testArrayObject(MonkeyObject obj, List expected) {
        Array result = (Array) obj;

        assertThat(result.elements().size())
            .withFailMessage("wrong number of elements in array. want=%d, got=%d", expected.size(), result.elements().size())
            .isEqualTo(expected.size());

        for (int i = 0; i < expected.size(); i++) {
            testIntegerObject(result.elements().get(i), (Long) expected.get(i));
        }
    }

}
