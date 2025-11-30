
package com.unyaunya.minic.backend;

import java.util.ArrayList;
import java.util.List;

public class Casl2Builder {
    private final List<AsmLine> lines = new ArrayList<>();

    private Casl2Builder addLine(String opcode, List<String> operands) {
        lines.add(new AsmLine(null, opcode, operands, null));
        return this;
    }

    // -------------------------------
    // Assembler Instructions
    // -------------------------------
    public Casl2Builder start() { return addLine("START", List.of()); }
    public Casl2Builder start(String address) { return addLine("START", List.of(address)); }
    public Casl2Builder end() { return addLine("END", List.of()); }
    public Casl2Builder ds(int size) { return addLine("DS", List.of(String.valueOf(size))); }
    public Casl2Builder dc(String value) { return addLine("DC", List.of(value)); }
    public Casl2Builder comment(String text) { lines.add(new AsmLine(null, null, null, text)); return this; }

    // -------------------------------
    // Machine Instructions
    // -------------------------------
    public Casl2Builder ld(String reg, String addr) { return addLine("LD", List.of(reg, addr)); }
    public Casl2Builder lad(String reg, String value) { return addLine("LAD", List.of(reg, value)); }
    public Casl2Builder st(String reg, String addr) { return addLine("ST", List.of(reg, addr)); }
    public Casl2Builder adda(String r1, String r2) { return addLine("ADDA", List.of(r1, r2)); }
    public Casl2Builder addl(String r1, String r2) { return addLine("ADDL", List.of(r1, r2)); }
    public Casl2Builder suba(String r1, String r2) { return addLine("SUBA", List.of(r1, r2)); }
    public Casl2Builder subl(String r1, String r2) { return addLine("SUBL", List.of(r1, r2)); }
    public Casl2Builder and(String r1, String r2) { return addLine("AND", List.of(r1, r2)); }
    public Casl2Builder or(String r1, String r2) { return addLine("OR", List.of(r1, r2)); }
    public Casl2Builder xor(String r1, String r2) { return addLine("XOR", List.of(r1, r2)); }
    public Casl2Builder cpa(String r1, String r2) { return addLine("CPA", List.of(r1, r2)); }
    public Casl2Builder cpl(String r1, String r2) { return addLine("CPL", List.of(r1, r2)); }
    public Casl2Builder sla(String r, String count) { return addLine("SLA", List.of(r, count)); }
    public Casl2Builder sll(String r, String count) { return addLine("SLL", List.of(r, count)); }
    public Casl2Builder sra(String r, String count) { return addLine("SRA", List.of(r, count)); }
    public Casl2Builder srl(String r, String count) { return addLine("SRL", List.of(r, count)); }
    public Casl2Builder jov(String label) { return addLine("JOV", List.of(label)); }
    public Casl2Builder jpl(String label) { return addLine("JPL", List.of(label)); }
    public Casl2Builder jmi(String label) { return addLine("JMI", List.of(label)); }
    public Casl2Builder jze(String label) { return addLine("JZE", List.of(label)); }
    public Casl2Builder jnz(String label) { return addLine("JNZ", List.of(label)); }
    public Casl2Builder jump(String label) { return addLine("JUMP", List.of(label)); }
    public Casl2Builder push(String addr, String reg) { return addLine("PUSH", List.of(addr, reg)); }
    public Casl2Builder push(String addr) { return addLine("PUSH", List.of(addr)); }
    public Casl2Builder pop(String reg) { return addLine("POP", List.of(reg)); }
    public Casl2Builder call(String label) { return addLine("CALL", List.of(label)); }
    public Casl2Builder ret() { return addLine("RET", List.of()); }
    public Casl2Builder svc(String code) { return addLine("SVC", List.of(code)); }
    public Casl2Builder nop() { return addLine("NOP", List.of()); }

    // -------------------------------
    // Macro Instructions
    // -------------------------------
    public Casl2Builder in(String device, String len) { return addLine("IN", List.of(device, len)); }
    public Casl2Builder out(String device, String len) { return addLine("OUT", List.of(device, len)); }
    public Casl2Builder rpush() { return addLine("RPUSH", List.of()); }
    public Casl2Builder rpop() { return addLine("RPOP", List.of()); }

    // -------------------------------
    // Utility
    // -------------------------------

    // Add Label
    public Casl2Builder l(String lbl) {
        if (!lines.isEmpty()) {
            AsmLine last = lines.get(lines.size() - 1);
            last.setLabel(lbl);
        }
        return this;
    }

    // Add Comment
    public Casl2Builder c(String text) {
        if (!lines.isEmpty()) {
            AsmLine last = lines.get(lines.size() - 1);
            last.setComment(text);
        }
        return this;
    }

    public String build() {
        StringBuilder sb = new StringBuilder();
        for (AsmLine line : lines) {
            sb.append(line.toString());
        }
        return sb.toString();
    }
    
    public AsmLine getLastLine() {
        return lines.isEmpty() ? null : lines.get(lines.size() - 1);
    }

    public boolean lastIsRet() {
        AsmLine last = getLastLine();
        return last != null && "RET".equals(last.getOpcode());
    }
}
