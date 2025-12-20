
package com.unyaunya.minic.backend;

import java.util.ArrayList;
import java.util.List;

import com.unyaunya.minic.MinicException;

public class Casl2Builder {
    public static final int MAX_LABEL_LEN = 8;
    public static final String GR0 = "GR0";
    public static final String GR1 = "GR1";
    public static final String GR2 = "GR2";
    public static final String GR3 = "GR3";
    public static final String GR4 = "GR4";
    public static final String GR5 = "GR5";
    public static final String GR6 = "GR6";
    public static final String GR7 = "GR7";

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
    public Casl2Builder comment(String fmt, Object... args) { return comment(String.format(fmt, args));}
    // -------------------------------
    // Machine Instructions
    // -------------------------------
    public Casl2Builder ld(String r1, String r2) { return addLine("LD", List.of(r1, r2)); }
    public Casl2Builder ld(String r, String adr, String x) { return addLine("LD", List.of(r, adr, x)); }
    public Casl2Builder ld(String r, int adr, String x) { return ld(r, Integer.toString(adr), x); }
    public Casl2Builder lad(String r, String adr) { return addLine("LAD", List.of(r, adr)); }
    public Casl2Builder lad(String r, String adr, String x) { return addLine("LAD", List.of(r, adr, x)); }
    public Casl2Builder lad(String r, int adr) { return lad(r, String.valueOf(adr)); }
    public Casl2Builder lad(String r, int adr, String x) { return lad(r, String.valueOf(adr), x); }
    public Casl2Builder st(String r, String adr) { return addLine("ST", List.of(r, adr)); }
    public Casl2Builder st(String r, String adr, String x) { return addLine("ST", List.of(r, adr, x)); }
    public Casl2Builder st(String r, int adr, String x) { return st(r,  Integer.toString(adr), x); }
    public Casl2Builder adda(String r1, String r2) { return addLine("ADDA", List.of(r1, r2)); }
    public Casl2Builder adda(String r1, int n) { return addLine("ADDA", List.of(r1, literal(n))); }
    public Casl2Builder addl(String r1, String r2) { return addLine("ADDL", List.of(r1, r2)); }
    public Casl2Builder suba(String r1, String r2) { return addLine("SUBA", List.of(r1, r2)); }
    public Casl2Builder suba(String r1, int n) { return addLine("SUBA", List.of(r1, literal(n))); }
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
    public Casl2Builder jpl(String adr) { return addLine("JPL", List.of(adr)); }
    public Casl2Builder jmi(String adr) { return addLine("JMI", List.of(adr)); }
    public Casl2Builder jze(String adr) { return addLine("JZE", List.of(adr)); }
    public Casl2Builder jnz(String adr) { return addLine("JNZ", List.of(adr)); }
    public Casl2Builder jump(String adr) { return addLine("JUMP", List.of(adr)); }
    public Casl2Builder push(String adr, String reg) { return addLine("PUSH", List.of(adr, reg)); }
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

    private String literal(int n) {
        return String.format("=%d", n);
    }

    public static class Casl2LabelGenerator {
        private final String description;
        private final String prefix;
        private final int limit;
        private int id=0;

        public Casl2LabelGenerator(String prefix, String description) {
            this.description = description;
            this.prefix = prefix;
            if (this.prefix.length() >= MAX_LABEL_LEN) {
                throw new MinicException(String.format("'%s' is too long for label prefix.", this.prefix));
            }
            this.limit = (int)Math.pow(10, MAX_LABEL_LEN - this.prefix.length());
        }

        public String getNewLabel() {
            this.id += 1;
            if (this.id >= this.limit) {
                throw new MinicException(String.format("The number of labels for %s exceeds the limit of %d.", this.description, this.limit-1));
            }
            String numPart = Integer.toString(this.limit + this.id).substring(1);
            return this.prefix + numPart;
        }
    }
}
