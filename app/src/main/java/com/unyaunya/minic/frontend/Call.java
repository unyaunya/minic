package com.unyaunya.minic.frontend;
import lombok.Getter;

import com.unyaunya.minic.Location;
import java.util.List;



@Getter
public class Call extends ExprNode implements Stmt {
    String name;
    List<Expr> args;

    public Call(Location location, String name, List<Expr> args) {
        super(location);
        this.name = name;
        this.args = args;
    }

    public String toString() {
        return String.format("%s(%s)", name, String.join(", ", args.stream().map(Object::toString).toList()));
    }
}
