
package com.unyaunya.minic.backend;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AsmLineTest {

    @Test
    void testAsmLineWithLabelOperandsAndComment() {
        AsmLine line = new AsmLine("START", "LD", List.of("GR1", "VAR"), "Load variable");
        String expected = "START    LD     GR1,VAR                 ; Load variable\n";
        assertEquals(expected, line.toString());
    }

    @Test
    void testAsmLineWithoutLabel() {
        AsmLine line = new AsmLine(null, "ADDA", List.of("GR1", "GR2"), null);
        String expected = "         ADDA   GR1,GR2                 \n";
        assertEquals(expected, line.toString());
    }

    @Test
    void testAsmLineWithLabelTruncation() {
        AsmLine line = new AsmLine("LONG_LABEL_NAME", "ST", List.of("GR1", "RESULT"), null);
        String expected = "LONG_LAB ST     GR1,RESULT              \n";
        assertEquals(expected, line.toString());
    }

    @Test
    void testAsmLineWithNoOperandsAndComment() {
        AsmLine line = new AsmLine("LBL", "RET", List.of(), "Return from function");
        String expected = "LBL      RET                            ; Return from function\n";
        assertEquals(expected, line.toString());
    }

    @Test
    void testAsmLinePureComment() {
        AsmLine line = new AsmLine(null, null, List.of(), "This is a comment");
        String expected = "; This is a comment\n";
        assertEquals(expected, line.toString());
    }

    @Test
    void testAsmLineEmptyComment() {
        AsmLine line = new AsmLine("LBL", "NOP", List.of(), "");
        String expected = "LBL      NOP                            \n";
        assertEquals(expected, line.toString());
    }

    @Test
    void testAsmLineEmptyOperands() {
        AsmLine line = new AsmLine("LBL", "RET", List.of(), null);
        String expected = "LBL      RET                            \n";
        assertEquals(expected, line.toString());
    }

    @Test
    void testAsmLineNullValuesEverywhere() {
        AsmLine line = new AsmLine(null, null, null, null);
        String expected = "\n"; // Pure empty line
        assertEquals(expected, line.toString());
    }

    // ✅ New tests for convenience constructors

    @Test
    void testConstructorOpcodeOnly() {
        AsmLine line = new AsmLine("RET");
        String expected = "         RET                            \n";
        assertEquals(expected, line.toString());
    }

    @Test
    void testConstructorOpcodeAndOperands() {
        AsmLine line = new AsmLine("LD", List.of("GR1", "VAR"));
        String expected = "         LD     GR1,VAR                 \n";
        assertEquals(expected, line.toString());
    }

    @Test
    void testConstructorOpcodeOperandsAndComment() {
        AsmLine line = new AsmLine("LD", List.of("GR1", "VAR"), "Load variable");
        String expected = "         LD     GR1,VAR                 ; Load variable\n";
        assertEquals(expected, line.toString());
    }
}
