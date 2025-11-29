
package com.unyaunya.minic.backend;

import java.util.ArrayList;
import java.util.List;

public class Casl2Builder {
    private final List<AsmLine> lines = new ArrayList<>();

    private Casl2Builder addLine(String label, String opcode, List<String> operands, String comment) {
        lines.add(new AsmLine(label, opcode, operands, comment));
        return this;
    }

    // -------------------------------
    // Assembler Instructions
    // -------------------------------
    public Casl2Builder start(String address) {
        return addLine(null, "START", List.of(address), null);
    }

    public Casl2Builder start(String address, String label, String comment) {
        return addLine(label, "START", List.of(address), comment);
    }

    public Casl2Builder end() {
        return addLine(null, "END", List.of(), null);
    }

    public Casl2Builder end(String label, String comment) {
        return addLine(label, "END", List.of(), comment);
    }

    public Casl2Builder ds(String label, int size) {
        return addLine(label, "DS", List.of(String.valueOf(size)), null);
    }

    public Casl2Builder ds(String label, int size, String comment) {
        return addLine(label, "DS", List.of(String.valueOf(size)), comment);
    }

    public Casl2Builder dc(String label, String value) {
        return addLine(label, "DC", List.of(value), null);
    }

    public Casl2Builder dc(String label, String value, String comment) {
        return addLine(label, "DC", List.of(value), comment);
    }

    // -------------------------------
    // Machine Instructions (example subset)
    // -------------------------------
    public Casl2Builder lad(String reg, String value) {
        return addLine(null, "LAD", List.of(reg, value), null);
    }

    public Casl2Builder lad(String reg, String value, String label, String comment) {
        return addLine(label, "LAD", List.of(reg, value), comment);
    }

    public Casl2Builder ld(String reg, String addr) {
        return addLine(null, "LD", List.of(reg, addr), null);
    }

    public Casl2Builder ld(String reg, String addr, String label, String comment) {
        return addLine(label, "LD", List.of(reg, addr), comment);
    }

    public Casl2Builder adda(String reg1, String reg2) {
        return addLine(null, "ADDA", List.of(reg1, reg2), null);
    }

    public Casl2Builder adda(String reg1, String reg2, String label, String comment) {
        return addLine(label, "ADDA", List.of(reg1, reg2), comment);
    }

    // -------------------------------
    // Macro Instructions
    // -------------------------------
    public Casl2Builder in(String device) {
        return addLine(null, "IN", List.of(device), null);
    }

    public Casl2Builder in(String device, String label, String comment) {
        return addLine(label, "IN", List.of(device), comment);
    }

    public Casl2Builder out(String device) {
        return addLine(null, "OUT", List.of(device), null);
    }

    public Casl2Builder out(String device, String label, String comment) {
        return addLine(label, "OUT", List.of(device), comment);
    }

    // -------------------------------
    // Utility
    // -------------------------------
    public Casl2Builder comment(String text) {
        return addLine(null, null, List.of(), text);
    }

    public String build() {
        StringBuilder sb = new StringBuilder();
        for (AsmLine line : lines) {
            sb.append(line.toString());
        }
        return sb.toString();
    }
}
