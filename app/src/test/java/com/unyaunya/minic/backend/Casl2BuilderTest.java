
package com.unyaunya.minic.backend;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Casl2BuilderTest {

    @Test
    void testAssemblerInstructionsWithLabelAndComment() {
        Casl2Builder builder = new Casl2Builder()
                .start("100").l("MAIN").c("Program start")
                .end().l("END_LABEL").c("Program end")
                .ds(10).l("VAR").c("Reserve 10 words")
                .dc("5").l("CONST").c("Constant value");

        String expected =
                "MAIN     START  100                     ; Program start\n" +
                "END_LABE END                            ; Program end\n" +
                "VAR      DS     10                      ; Reserve 10 words\n" +
                "CONST    DC     5                       ; Constant value\n";

        assertEquals(expected, builder.build());
    }

    @Test
    void testMachineInstructionsWithLabelAndComment() {
        Casl2Builder builder = new Casl2Builder()
                .lad("GR1", "10").l("LOAD10").c("Load 10 into GR1")
                .adda("GR1", "GR2").l("ADD_LABEL").c("Add GR2 to GR1")
                .ret().c("Return from subroutine");

        String expected =
                "LOAD10   LAD    GR1,10                  ; Load 10 into GR1\n" +
                "ADD_LABE ADDA   GR1,GR2                 ; Add GR2 to GR1\n" +
                "         RET                            ; Return from subroutine\n";

        assertEquals(expected, builder.build());
    }

    @Test
    void testMacroInstructionsWithLabelAndComment() {
        Casl2Builder builder = new Casl2Builder()
                .rpush().l("SAVE_REGS").c("Save all registers")
                .rpop().l("RESTORE_REGS").c("Restore all registers")
                .in("1", "20").l("INPUT").c("Read input")
                .out("2", "20").l("OUTPUT").c("Write output");

        String expected =
                "SAVE_REG RPUSH                          ; Save all registers\n" +
                "RESTORE_ RPOP                           ; Restore all registers\n" +
                "INPUT    IN     1,20                    ; Read input\n" +
                "OUTPUT   OUT    2,20                    ; Write output\n";

        assertEquals(expected, builder.build());
    }

    @Test
    void testMixedProgram() {
        Casl2Builder builder = new Casl2Builder()
                .start("100").l("MAIN").c("Start of program")
                .comment("Initialize registers")
                .lad("GR1", "10").c("Load 10")
                .lad("GR2", "20").c("Load 20")
                .adda("GR1", "GR2").c("Add GR2 to GR1")
                .rpush().c("Save registers")
                .rpop().c("Restore registers")
                .end().l("END_LABEL").c("Program end");

        String expected =
                "MAIN     START  100                     ; Start of program\n" +
                "; Initialize registers\n" +
                "         LAD    GR1,10                  ; Load 10\n" +
                "         LAD    GR2,20                  ; Load 20\n" +
                "         ADDA   GR1,GR2                 ; Add GR2 to GR1\n" +
                "         RPUSH                          ; Save registers\n" +
                "         RPOP                           ; Restore registers\n" +
                "END_LABE END                            ; Program end\n";

        assertEquals(expected, builder.build());
    }
}
