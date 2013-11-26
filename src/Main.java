import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

public class Main
{
	private static final boolean DEBUG = true;
	
	public static void main(String[] args)
	{
		List<Rule> rules = new ArrayList<Rule>();
		Scanner console = new Scanner(System.in);
		
		int numRules = Integer.parseInt(console.nextLine());
		
		for (int i = 0; i < numRules; i++)
		{
			Rule rule = Rule.parse(console.nextLine());
			rules.add(rule);
		}
		
		// simulate PDA rules on each input string
		while (console.hasNextLine())
		{
			Stack<String> stack = new Stack<String>();
			stack.push("$");
			stack.push(rules.get(0).getName());
			
			String input = console.nextLine().trim().replaceAll("!", "");
			boolean accepted = isAccepted(rules, input, stack, 0);
			System.out.println(accepted ? "yes" : "no");
			
		}
		console.close();
	}
	
	private static boolean isAccepted(List<Rule> rules, String remainingInput, Stack<String> currentStack, int stepCount)
	{
		if (stepCount > 100)
		{
			return false;
		}
		String stackSymbol = currentStack.pop();
		if (DEBUG) System.out.println(String.format("Debug: (%s | %s | %s)", currentStack.toString(), stackSymbol, remainingInput));
		
		if (isVariable(stackSymbol))
		{
			// don't read an input symbol, just push the rhs of the
			// rule onto the stack. try once for each rule that matches.
			List<Rule> matchedRules = matchedRules(rules, stackSymbol);
			for (Rule rule : matchedRules)
			{
				// push rhs characters onto a new copy of the stack
				// in reverse order
				Stack<String> newStack = new Stack<String>();
				newStack.addAll(currentStack);
				
				String symbols = new StringBuilder(rule.getRhsValue()).reverse().toString();
				for (int i = 0; i < symbols.length(); i++)
				{
					newStack.add(symbols.substring(i, i + 1));
				}
				// accept if any of the subcopies accept
				if (isAccepted(rules, remainingInput, newStack, stepCount + 1))
				{
					return true;
				}
			}
			return false;
		}
		else if (isTerminal(stackSymbol))
		{
			if (stackSymbol.equals("$"))
			{
				// only accept if input string is consumed when $ is encountered
				return remainingInput.isEmpty();
			}
			else if (stackSymbol.equals("!"))
			{
				// ignore emtpy string in the stack, just eat the symbol
				return isAccepted(rules, remainingInput, currentStack, stepCount + 1);
			}
			else
			{
				// if this symbol doesn't match the next symbol in remaining input,
				// reject. otherwise discard the symbol and keep going
				String nextChar = (remainingInput.length() > 0) ? remainingInput.substring(0, 1) : "";
				if (stackSymbol.equals(nextChar))
				{
					String newInput = remainingInput.substring(1);
					return isAccepted(rules, newInput, currentStack, stepCount + 1);
				}
				else
				{
					return false;
				}
			}
		}
		return false;
	}
	
	private static boolean isVariable(String string)
	{
		if (string.equals("$") || string.equals("!") || string.equals("_"))
		{
			return false;
		}
		return (string.toUpperCase().equals(string));
	}
	
	private static boolean isTerminal(String string)
	{
		if (string.equals("$") || string.equals("!") || string.equals("_"))
		{
			return true;
		}
		return (string.toLowerCase().equals(string));
	}
	
	private static List<Rule> matchedRules(List<Rule> allRules, String variableName)
	{
		List<Rule> result = new ArrayList<Rule>();
		for (Rule rule : allRules)
		{
			if (rule.getName().equals(variableName))
			{
				result.add(rule);
			}
		}
		return result;
	}
	
	private static class Rule
	{
		private String name;
		private String rhsValue;
		
		public static Rule parse(String inputString)
		{
			String[] parts = inputString.split("->");
			String name = parts[0].trim();
			String rhsValue = parts[1].trim();
			return new Rule(name, rhsValue);
		}
		
		public Rule(String name, String rhsValue)
		{
			this.name = name;
			this.rhsValue = rhsValue;
		}
		
		public String getName()
		{
			return name;
		}
		
		public String getRhsValue()
		{
			return rhsValue;
		}
	}
}