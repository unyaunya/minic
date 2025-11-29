
package com.unyaunya.minic.backend;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Casl2BuilderTest {

    @Test
    void testAssemblerInstructions() {
        Casl2Builder builder = new Casl2Builder()
                .start("100", "MAIN", "Program start")
                .end("END_LABEL", "Program end")
                .ds("VAR", 10, "Reserve 10 words")
                .dc("CONST", "5", "Constant value");

        String expected =
                "MAIN     START  100                      ; Program start\n" +
                "END_LABEL END                             ; Program end\n" +
                "VAR      DS     10                       ; Reserve 10 words\n" +
                "CONST    DC     5                        ; Constant value\n";

        assertEquals(expected, builder.build());
    }

    @Test
    void testMachineInstructionsWithLabelAndComment() {
        Casl2Builder builder = new Casl2Builder()
                .lad("GR1", "10", "LOAD10", "Load 10 into GR1")
                .adda("GR1", "GR2", "ADD_LABEL", "Add GR2 to GR1")
                .ret();

        String expected =
                "LOAD10   LAD    GR1,10                   ; Load 10 into GR1\n" +
                "ADD_LABEL ADDA   GR1,GR2                 ; Add GR2 to GR1\n" +
                "         RET                             \n";

        assertEquals(expected, builder.build());
    }

    @Test
    void testMacroInstructions() {
        Casl2Builder builder = new Casl2Builder()
                .in("1", null, "Input string")
                .out("2", null, "Output string")
                .rpush()
                .rpop();

        String expected =
                "         IN     1                        ; Input string\n" +
                "         OUT    2                        ; Output string\n" +
                "         RPUSH                           \n" +
                "         RPOP                            \n";

        assertEquals(expected, builder.build());
    }

    @Test
    void testMixedProgram() {
        Casl2Builder builder = new Casl2Builder()
                .start("100", "MAIN", "Start of program")
                .comment("Initialize registers")
                .lad("GR1", "10")
                .lad("GR2", "20")
                .adda("GR1", "GR2")
                .ret()
                .end();

        String expected =
                "MAIN     START  100                      ; Start of program\n" +
                "; Initialize registers\n" +
                "         LAD    GR1,10                   \n" +
                "         LAD    GR2,20                   \n" +
                "         ADDA   GR1,GR2                  \n" +
                "         RET                             \n" +
                "         END                             \n";

        assertEquals(expected, builder.build());
    }
}
