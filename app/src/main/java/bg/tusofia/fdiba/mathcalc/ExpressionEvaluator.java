package bg.tusofia.fdiba.mathcalc;

import org.mariuszgromada.math.mxparser.Expression;

import java.io.IOException;

class ExpressionEvaluator {
    private String expression;

    public ExpressionEvaluator(String expression) {
        this.expression = expression;
    }

    public String evaluate() throws IOException {
        if (this.expression == null || "".equals(this.expression)) {
            throw new IOException("The expression must not be null or empty string");
        }

        Expression expression = new Expression(this.expression);
        return String.valueOf(expression.calculate());
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }
}
