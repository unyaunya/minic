
package com.unyaunya.minic.backend;

import com.unyaunya.minic.frontend.*;
import com.unyaunya.minic.semantics.SemanticInfo;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Casl2EmitterTest {

    //@Test
    void testEmitSimpleProgram() {
        // Program: int main() { return 1 + 2; }
        IntLit one = new IntLit(1);
        IntLit two = new IntLit(2);
        Binary addExpr = new Binary(Binary.Op.ADD, one, two);
        ReturnStmt retStmt = new ReturnStmt(addExpr);

        Block body = new Block();
        body.getStatements().add(retStmt);

        FunctionDecl mainFunc = new FunctionDecl(
                new TypeSpec(BaseType.INT, 0, null),
                "MAIN",
                List.of(),
                body
        );

        Program program = new Program();
        program.getFunctions().add(mainFunc);

        Casl2Emitter emitter = new Casl2Emitter();
        SemanticInfo semanticInfo = new SemanticInfo(null, null);
        String actual = emitter.emit(program, semanticInfo, 256);

        String expected =
                "PRG      START                          ; Program start\n" +
                "; Function: MAIN\n" +
                "MAIN     PUSH   0,GR8                   \n" +
                "         LAD    GR8,0,GR8               \n" +
                "         LAD    GR1,1                   \n" +
                "         PUSH   GR1                     \n" +
                "         LAD    GR1,2                   \n" +
                "         POP    GR2                     \n" +
                "         ADDA   GR1,GR2                 \n" +
                "; Epilogue\n" +
                "         POP    GR8                     \n" +
                "         RET                            \n" +
                "STACK    DS     256                     \n" +
                "         END      \n";

        assertEquals(expected, actual);
    }

    //@Test
    void testEmitGlobalVariable() {
        // Program: int X; int main() { X = 42; return X; }
        GlobalDecl globalX = new GlobalDecl(new TypeSpec(BaseType.INT, 0, null), "X");
        Program program = new Program();
        program.getGlobals().add(globalX);

        IntLit fortyTwo = new IntLit(42);
        LvVar lvX = new LvVar("X");
        Assign assign = new Assign(lvX, fortyTwo);
        VarRef varRef = new VarRef("X");
        ReturnStmt retStmt = new ReturnStmt(varRef);

        Block body = new Block();
        body.getStatements().add(assign);
        body.getStatements().add(retStmt);

        FunctionDecl mainFunc = new FunctionDecl(
                new TypeSpec(BaseType.INT, 0, null),
                "MAIN",
                List.of(),
                body
        );
        program.getFunctions().add(mainFunc);

        Casl2Emitter emitter = new Casl2Emitter();
        SemanticInfo semanticInfo = new SemanticInfo(null, null);
        String actual = emitter.emit(program, semanticInfo, 256);

        String expected =
                "START    \n" +
                "X        DS     1                       \n" +
                "         LAD    GR8,STACK               \n" +
                "MAIN     \n" +
                "; Prologue\n" +
                "         PUSH   0,GR8                   \n" +
                "         LAD    GR8,0,GR8               \n" +
                "         LAD    GR1,42                  \n" +
                "         ST     GR1,X                   \n" +
                "         LD     GR1,X                   \n" +
                "; Epilogue\n" +
                "         POP    GR8                     \n" +
                "         RET                             \n" +
                "END      \n" +
                "STACK    DS     256                     \n";

        assertEquals(expected, actual);
    }

    //@Test
    void testEmitIfStatement() {
        // Program: if (1) return 2; else return 3;
        IntLit cond = new IntLit(1);
        ReturnStmt thenRet = new ReturnStmt(new IntLit(2));
        ReturnStmt elseRet = new ReturnStmt(new IntLit(3));

        Block thenBlock = new Block();
        thenBlock.getStatements().add(thenRet);

        Block elseBlock = new Block();
        elseBlock.getStatements().add(elseRet);

        IfStmt ifStmt = new IfStmt(cond, thenBlock, elseBlock);

        Block body = new Block();
        body.getStatements().add(ifStmt);

        FunctionDecl mainFunc = new FunctionDecl(
                new TypeSpec(BaseType.INT, 0, null),
                "MAIN",
                List.of(),
                body
        );
        Program program = new Program();
        program.getFunctions().add(mainFunc);

        Casl2Emitter emitter = new Casl2Emitter();
        SemanticInfo semanticInfo = new SemanticInfo(null, null);
        String actual = emitter.emit(program, semanticInfo, 256);

        String expected =
                "START    \n" +
                "         LAD    GR8,STACK               \n" +
                "MAIN     \n" +
                "; Prologue\n" +
                "         PUSH   0,GR8                   \n" +
                "         LAD    GR8,0,GR8               \n" +
                "         LAD    GR1,1                   \n" +
                "         JZE    ELSE_0                  \n" +
                "         LAD    GR1,2                   \n" +
                "         RET                             \n" +
                "         JUMP   ENDIF_1                 \n" +
                "ELSE_0   \n" +
                "         LAD    GR1,3                   \n" +
                "         RET                             \n" +
                "ENDIF_1  \n" +
                "; Epilogue\n" +
                "         POP    GR8                     \n" +
                "         RET                             \n" +
                "END      \n" +
                "STACK    DS     256                     \n";

        assertEquals(expected, actual);
    }
}
