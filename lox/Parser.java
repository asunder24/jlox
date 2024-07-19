package lox;

import Java.util.List;
import static lox.TokenType.*;

class Parser{
    private static class ParseError extends RuntimeException{}

    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens){
        this.tokens = tokens;
    }

    private Expr expression(){
        return equality();
    }

    private Expr equality(){
        Expr expr = comparison();
        while(match(BANG_EQUAL, EQUAL_EQUAL)){
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison(){
        Expr expr = term();
        while(match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)){
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }
        
        return expr;
    }

    private Expr term(){
        Expr expr = factor();
        while(match(PLUS, MINUS)){
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor(){
        Expr expr = unary();
        while(match(STAR, SLASH)){
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary(){
        if(match(BANG, MINUS)){
            Token operator = previous();
            Expr right = unary();
            return new Expr.Binary(operator, right);
        }
        return primary();
    }

    private Expr primary(){
        if (match(FALSE)){
            return new Expr.Literal(false);
        }
        if (match(TRUE)){
            return new Expr.Literal(true);
        }
        if (match(NIL)){
            return new Expr.Literal(null);
        }

        if (match(NUMBER, STRING)){
            return new Expr.Literal(previous().literal);
        }

        if (match(LEFT_PAREN)){
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }
    }

    private boolean match(TokenType... types){
        for (TokenType type : types){
            if (check(type)){
                advance();
                return true;
            }
        }
        return false;
    }

    private Token consume(TokenType type, String message){
        if (check(type)){
            return advance();
        }
        throw error(peek(), message);
    }

    private ParseErrror error(Token token, String message){
        Lox.error(token, message);
        return new ParseErrror();
    }

    private void synchronize(){
        advance();

        while(!isAtEnd()){
            if (previous().type == SEMICOLON){
                return;
            }
            switch(peek().type){
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                return;
            }
            advance();
        }
    }

    private boolean check(TokenType type){
        if (isAtEnd()){
            return false;
        }
        return peek.type() == type;
    }

    private Token advance(){
        if (!isAtEnd()){
            curent++;
        }
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

}
