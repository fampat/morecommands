package com.mrnobody.morecommands.command.server;

import net.minecraft.command.ICommandSender;

import com.mrnobody.morecommands.core.MoreCommands.ServerType;
import com.mrnobody.morecommands.command.Command;
import com.mrnobody.morecommands.command.ServerCommand;
import com.mrnobody.morecommands.wrapper.CommandException;
import com.mrnobody.morecommands.wrapper.CommandSender;

@Command(
		description="command.calc.description",
		example="command.calc.example",
		name="calc",
		syntax="command.calc.syntax",
		videoURL="command.calc.videoURL"
		)
public class CommandCalc extends ServerCommand {
	private static enum Operation {
		MODULO {
			@Override double perform(double op1, double op2) {return op1 % op2;}
			@Override String[] split(String calc) {return calc.split("%");}
			@Override Operation next() {return MULTIPLY;}
		},
		MULTIPLY {
			@Override double perform(double op1, double op2) {return op1 * op2;}
			@Override String[] split(String calc) {return calc.split("\\*");}
			@Override Operation next() {return DIVIDE;}
		},
		DIVIDE {
			@Override double perform(double op1, double op2) {return op1 / op2;}
			@Override String[] split(String calc) {return calc.split("/");}
			@Override Operation next() {return POWER;}
		},
		POWER {
			@Override double perform(double op1, double op2) {return Math.pow(op1, op2);}
			@Override String[] split(String calc) {return calc.split("\\^");}
			@Override Operation next() {return null;}
		};
		
		public static final Operation FIRST = MODULO;
		
		abstract double perform(double op1, double op2);
		abstract String[] split(String calc);
		abstract Operation next();
	}
  
	@Override
	public String getName() {
		return "calc";
	}
  
	@Override
	public String getUsage() {
		return "command.calc.syntax";
	}
  
	@Override
	public void execute(CommandSender sender, String[] params) throws CommandException {
		String calc = ""; for (String param : params) calc = calc + " " + param;
		try {sender.sendLangfileMessage("command.calc.result", parseCalculation(calc));}
		catch (NumberFormatException nfe) {throw new CommandException("command.calc.failure", sender, nfe.getMessage());}
	}
  
	private double parseCalculation(String calc) throws NumberFormatException {
		char[] check = calc.toCharArray();
    
		for(char ch : check) {
			if (!Character.isDigit(ch) && 
					ch != '(' && ch != ')' && 
					ch != '+' && ch != '*' && 
					ch != '-' && ch != '/' &&
					ch != '.' && ch != ',' &&
					ch != '^' && ch != ' '
					 && ch != '%') throw new NumberFormatException("Invalid calculation");
			}
		
		calc = calc.replace(",", ".").replace(" ", "");
		
		return this.parseBrackets(calc);
 	}
  
	private double parseBrackets(String calc) throws NumberFormatException {
		calc = calc.replace(")(", ")*(");
		
		for (int j = 0; j <= 9; j++)
			calc = calc.replace(")" + j, ")*" + j).replace(j + "(", j + "*(");

		char[] chars = calc.toCharArray();
		boolean bracketOpen = false;
		int bracketCount = 0;
		String expr = "";
		String parsedExpr = "";
		double result = 0.0;
		
		if (calc.contains("(") || calc.contains(")")) {
			for (char ch : chars) {
				if (ch == ')') {
					bracketCount--;
					
					if (bracketCount == 0) {
						bracketOpen = false;
						
						if (!expr.isEmpty()) {
							parsedExpr += this.parseBrackets(expr);
							expr = "";
						}
					}
				}
				
				if (bracketOpen) expr += Character.toString(ch);
				else if (ch != '(' && ch != ')') parsedExpr += Character.toString(ch);
				
				if (ch == '(') {bracketOpen = bracketCount == 0 ? true : bracketOpen; bracketCount++;}
			}
			
			parsedExpr = parsedExpr.replace("+-", "-").replace("-+", "-").replace("--", "+").replace("++", "+");
			result = this.parseFromLeftToRight(parsedExpr);
		}
		else result = this.parseFromLeftToRight(calc);
		
		return result;
	}
  
	private double parseFromLeftToRight(String calc) throws NumberFormatException {
		char[] chars = calc.toCharArray();
		
		String expr = "0";
		double result = 0.0;
		char operator = '+';
		boolean valid = true;
		
		if (calc.contains("+") || calc.contains("-")) {
			if (chars[0] == '-') operator = '-';
			
			for (char ch : chars) {
				if ((ch == '+' || ch == '-') && valid) {
					if (operator == '+') result += this.parseOperands(expr);
					if (operator == '-') result -= this.parseOperands(expr);
					
					if (ch == '+') operator = '+';
					if (ch == '-') operator = '-';
					
					expr = "";
				}
				else {expr += Character.toString(ch); valid = !(ch == '*' || ch == '/' || ch == '^');}
			}
			
			if (operator == '+') result += this.parseOperands(expr);
			if (operator == '-') result -= this.parseOperands(expr);
		}
		else result = this.parseOperands(calc);
		
		return result;
	}
  
	private double parseOperands(String calc) throws NumberFormatException {
		return parseOperands(calc, Operation.FIRST);
	}
  
	private double parseOperands(String calc, Operation operation) throws NumberFormatException {
		if (operation == null) return Double.parseDouble(calc);
		
		String[] operands = operation.split(calc);
		double result = parseOperands(operands[0], operation.next());
		
		for (int i = 1; i < operands.length; i++)
			result = operation.perform(result, parseOperands(operands[i], operation.next()));
		
		return result;
	}
	
	@Override
	public boolean canSenderUse(ICommandSender sender) {
		return true;
	}
	
	@Override
	public Requirement[] getRequirements() {
		return new Requirement[0];
	}
  
	@Override
	public ServerType getAllowedServerType() {
		return ServerType.ALL;
	}
	
	@Override
	public int getPermissionLevel() {
		return 0;
	}
}
