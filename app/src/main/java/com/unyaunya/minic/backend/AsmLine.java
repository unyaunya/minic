
package com.unyaunya.minic.backend;

import java.util.Collections;
import java.util.List;

import lombok.Getter;

@Getter
class AsmLine {
    private String label;    // up to 8 chars
    private String opcode;   // CASL2 mnemonic
    private List<String> operands; // 0, 1, or 2 operands
    private String comment;

    AsmLine(String opcode, List<String> operands, String comment) {
        this(null, opcode, operands, comment);
    }

    AsmLine(String label, String opcode, List<String> operands) {
        this(label, opcode, operands, null);
    }

    AsmLine(String opcode, List<String> operands) {
        this(null, opcode, operands, null);
    }

    AsmLine(String opcode) {
        this(null, opcode, null, null);
    }

    AsmLine(String label, String opcode, List<String> operands, String comment) {
        this.setLabel(label);
        this.label = (label != null && label.length() > 8) ? label.substring(0, 8) : label;
        this.opcode = opcode != null ? opcode : "";
        this.operands = operands != null ? operands : Collections.emptyList();
        this.setComment(comment);
    }

    public void setLabel(String label) {
        if (label == null || label.isEmpty()) {
            this.label = null;
        } else {
            this.label = (label.length() > 8) ? label.substring(0, 8).toUpperCase() : label.toUpperCase();
        }
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        String ops = String.join(",", operands);

        // Pure comment line: no opcode and no operands
        if ((opcode.isEmpty()) && ops.isEmpty()) {
            if (comment != null) {
                return String.format("; %s%n", comment);
            } else {
                return String.format("%n");
            }
        } else {
            // CASL2 columns: label(8), space, opcode(6), space, operands(24), comment
            return String.format("%-8s %-6s %-24s%s%n",
                    label != null ? label : "",
                    opcode,
                    ops,
                    (comment != null && !comment.isEmpty()) ? "; " + comment : "");
        }
    }
}
