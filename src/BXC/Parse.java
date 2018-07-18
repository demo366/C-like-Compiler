/******************************************************
 	文件	parse.java
 	说明	编译器的递归下降法语法分析器实现
 	功能	构造语法树
 		进行语法错误检查
 	作者	包学超
******************************************************/

package BXC;
import BXC.Global.DecKind;
import BXC.Global.ExpKind;
import BXC.Global.ExpType;
import BXC.Global.LexType;
import BXC.Global.NodeKind;
import BXC.Global.StmtKind;
import BXC.Global.VarKind;
import BXC.Global.treeNode;

public class Parse {
	
	public static void main(String[] args)
	{
		Parse myParse = new Parse();
		myParse.run();
	}

	public static Global global = new Global();
	
	public static Scanner myScanner = new Scanner();
	
	public int line0 = 0;
	
	public Token token;
	
	public int index = 0;
	
	public String temp_name = "";
	
	/********************************************************/
	/* 	函数	syntaxError										*/
	/* 	功能	语法错误处理函数										*/
	/* 	说明	将函数参数message指定的错误信息格式化写入列表文件listing		*/
	/*		设置错误追踪标志Error为true							*/
	/********************************************************/
	public void syntaxError(String message)
	{
		global.pw.print(">>> error :   ");
		global.pw.printf("Syntax error at line %d, %s: %s",token.lineshow,message,token.Sem);
		global.pw.println();
		global.pw.flush();
		Global.Error = true;
	}
	
	/********************************************************/
	/* 	函数	match											*/
	/* 	功能	终极符匹配处理函数									*/
	/* 	说明	函数参数expected给定期望单词符号与当前单词符号token相匹配		*/
	/*      如果不匹配,则报非期望单词语法错误							*/
	/********************************************************/
	public void match(LexType expected)
	{
		if (token.Lex.equals(expected))
		{
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			line0 = token.lineshow;
		}
		else 
		{
			syntaxError("not match error");
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			System.exit(0);
		}
	}
	
	/********************************************************/
	/* 	 			以下是递归下降法产生式对应的所有函数				*/
	/********************************************************/
	public treeNode Program()
	{
		treeNode root = global.newRootNode();
		switch(token.Lex) {
		case ENDFILE:
		case TYPEDEF:
		case INT:
		case CHAR:
		case STRUCT:
		case ID:
			root.child[0] = Decpart();
			break;
		default:
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			syntaxError("unexpected token is here!");
			break;
		}
		return root;
	}
	
	public treeNode Decpart()
	{
		treeNode t = null;
		treeNode p = null;
		switch(token.Lex) {
		case TYPEDEF:
			t = TypeDecpart();
			p = DecpartMore();
			t.sibling = p;
			break;
		case INT:
		case CHAR:
		case STRUCT:
		case ID:
			t = global.newDecNode();
			TypeDef(t);
			t.lineno = line0;
			t.name[t.idnum] = token.Sem;
			t.idnum ++;
			temp_name = token.Sem;
			match(LexType.ID);
			VFDecpart(t);
			p = DecpartMore();
			t.sibling = p;
			break;
		case ENDFILE:
			break;
		default:
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			syntaxError("unexpected token is here!");
			break;
		}
		return t;
	}
	
	public treeNode DecpartMore()
	{
		treeNode t = null;
		switch(token.Lex) {
		case ENDFILE:
		case TYPEDEF:
		case INT:
		case CHAR:
		case STRUCT:
		case ID:
			t = Decpart();
			break;
		default: 
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			syntaxError("unexpected token is here!");
			break;
		}
		return t;
	}

	public void VFDecpart(treeNode t)
	{
		switch(token.Lex) {
		case SEMI:
		case COMMA:
			VarDec(t);
			break;
		case LPAREN:
			FuncDec(t);
			break;
		default:
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			syntaxError("unexpected token is here!");
			break;
		}
	}

	public treeNode TypeDecpart()
	{
		treeNode t = null;
		switch(token.Lex) {
		case TYPEDEF:
			t = global.newTypeNode();
			match(LexType.TYPEDEF);
			t.lineno = line0;
			t.name[t.idnum] = token.Sem;
			match(LexType.ID);
			t.idnum ++;
			TypeDef(t);
			match(LexType.SEMI);
			break;
		default:
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			syntaxError("unexpected token is here!");
			break;
		}
		return t;
	}

	public void TypeDef(treeNode t)
	{
		if(t != null)
		switch(token.Lex) {
		case INT:
		case CHAR:
			BaseType(t);
			ArrayMore(t);
			break;
		case STRUCT:
			StructType(t);
			break;
		case ID:
		    t.kind.dec = DecKind.IdK;
			t.attr.type_name = token.Sem;
			match(LexType.ID);
			break;
		default:
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			syntaxError("unexpected token is here!");
			break;
		}
	}

	public void BaseType(treeNode t)
	{
		switch(token.Lex) {
		case INT:
			match(LexType.INT);
            t.kind.dec = DecKind.IntK;
			break;
		case CHAR:
			match(LexType.CHAR);
            t.kind.dec = DecKind.CharK;
			break;
		default:
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			syntaxError("unexpected token is here!");
			break;
		}
	}
	
	public void ArrayMore(treeNode t)
	{
		switch(token.Lex) {
		case SEMI:
		case ID:
			break;
		case LMIDPAREN:
			match(LexType.LMIDPAREN);
			if(t != null)
			{
				t.attr.arrayAttr.size = Integer.parseInt(token.Sem);
			    t.attr.arrayAttr.childType = t.kind.dec;
	            t.kind.dec = DecKind.ArrayK;
			}
			match(LexType.INTC);
			match(LexType.RMIDPAREN);
			break;
		default:
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			syntaxError("unexpected token is here!");
			break;
		}
	}
	
	public void StructType(treeNode t)
	{
		treeNode p = null;
		switch(token.Lex) {
		case STRUCT:
			match(LexType.STRUCT);
			StructName(t);
			match(LexType.LBIGPAREN);
			p = FieldDecList();
			if(t != null)
			{
				if(p != null)
					t.child[0] = p;
				else
					syntaxError("a record body is requested!");
	            t.kind.dec = DecKind.StructK;
			}
			match(LexType.RBIGPAREN);
			break;
		default:
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			syntaxError("unexpected token is here!");
			break;
		}
	}

	public treeNode FieldDecList()
	{
		treeNode t = null;
		treeNode p = null;
		switch(token.Lex) {
		case INT:
		case CHAR:
			t = global.newDecNode();
			BaseType(t);
			ArrayMore(t);
			t.lineno = line0;
			IdList(t);
			match(LexType.SEMI);
			p = FieldDecMore();
			t.sibling = p;
			break;
		default:
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			syntaxError("unexpected token is here!");
			break;
		}
		return t;
	}

	public treeNode FieldDecMore()
	{
		treeNode t = null;
		switch(token.Lex) {
		case RBIGPAREN:
			break;
		case INT:
		case CHAR:
			t = FieldDecList();
			break;
		default:
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			syntaxError("unexpected token is here!");
			break;
		}
		return t;
	}
	
	public void IdList(treeNode t)
	{
		switch(token.Lex) {
		case ID:
			if(t != null)
			{
				t.name[t.idnum] = token.Sem;
				t.idnum ++;
			}
			match(LexType.ID);
			IdMore(t);
			break;
		default:
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			syntaxError("unexpected token is here!");
			break;
		}
	}

	public void IdMore(treeNode t)
	{
		switch(token.Lex) {
		case SEMI:
			break;
		case COMMA:
			match(LexType.COMMA);
			IdList(t);
			break;
		default:
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			syntaxError("unexpected token is here!");
			break;
		}
	}
	
	public void StructName(treeNode t)
	{
		switch(token.Lex) {
		case LBIGPAREN:
			break;
		case ID:
			match(LexType.ID);
			break;
		default:
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			syntaxError("unexpected token is here!");
			break;
		}
	}

	public void VarDec(treeNode t)
	{
		switch(token.Lex) {
		case SEMI:
		case COMMA:
			VarIdMore(t);
			match(LexType.SEMI);
			break;
		default:
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			syntaxError("unexpected token is here!");
			break;
		}
	}

	public void VarIdList(treeNode t)
	{
		switch(token.Lex) {
		case ID:
			if(t != null)
			{
				t.name[t.idnum] = token.Sem;
				t.idnum ++;
			}
			match(LexType.ID);
			VarIdMore(t);
			break;
		default:
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			syntaxError("unexpected token is here!");
			break;
		}
	}

	public void VarIdMore(treeNode t)
	{
		switch(token.Lex) {
		case SEMI:
			break;
		case COMMA:
			match(LexType.COMMA);
			VarIdList(t);
			break;
		default:
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			syntaxError("unexpected token is here!");
			break;
		}
	}
	
	public void FuncDec(treeNode t)
	{
		switch(token.Lex) {
		case LPAREN:
			t.nodeKind = NodeKind.FuncDecK;
			t.attr.funcAttr.returnType = t.kind.dec;
			t.name[0] = temp_name;
			t.idnum ++;
			match(LexType.LPAREN);
			ParamList(t);
			match(LexType.RPAREN);
			t.child[1] = global.newStmlNode();
			t.child[1].lineno = line0;
			match(LexType.LBIGPAREN);
			t.child[1].child[0] = StmList();
			match(LexType.RBIGPAREN);
			break;
		default:
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			syntaxError("unexpected token is here!");
			break;
		}
	}
	
	public void ParamList(treeNode t)
	{
		treeNode p = null;
		switch(token.Lex)
		{
		case RPAREN:
			break;
		case INT:
		case CHAR:
		case STRUCT:
		case ID:
			p = ParamDecList();
			if(t != null)
				t.child[0] = p;
			break;
		default:
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			syntaxError("unexpected token is here!");
			break;
		}
	}
	
	public treeNode ParamDecList()
	{
		treeNode t = null;
		treeNode p = null;
		switch(token.Lex) {
		case INT:
		case CHAR:
		case STRUCT:
		case ID:
			t = Param();
			p = ParamMore();
			if(t != null)
				t.sibling = p;
			break;
		default:
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			syntaxError("unexpected token is here!");
			break;
		}
		return t;
	}

	public treeNode ParamMore()
	{
		treeNode t = null;
		switch(token.Lex) {
		case RPAREN:
			break;
		case COMMA:
			match(LexType.COMMA);
			t = ParamDecList();
			break;
		default:
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			syntaxError("unexpected token is here!");
			break;
		}
		return t;
	}

	public treeNode Param()
	{
		treeNode t = null;
		switch(token.Lex) {
		case INT:
		case CHAR:
		case STRUCT:
		case ID:
			t = global.newDecNode();
			TypeDef(t);
			t.lineno = line0;
			FormName(t);
			break;
		default:
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			syntaxError("unexpected token is here!");
			break;
		}
		return t;
	}
	
	public void FormName(treeNode t)
	{
		switch(token.Lex) {
		case ID:
			if(t != null)
			{
				t.name[t.idnum] = token.Sem;
				t.idnum ++;
			}
			match(LexType.ID);
			break;
		default:
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			syntaxError("unexpected token is here!");
			break;
		}
	}

	public treeNode StmList()
	{
		treeNode t = null;
		treeNode p = null;
		switch(token.Lex) {
		case LBIGPAREN:
		case TYPEDEF:
		case INT:
		case CHAR:
		case STRUCT:
		case ID:
		case IF:
		case WHILE:
		case FOR:
		case CIN:
		case COUT:
		case RETURN:
			t = Stm();
			p = StmMore();
			if(t != null)
				t.sibling = p;
			break;
		default:
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			syntaxError("unexpected token is here!");
			break;
		}
		return t;
	}

	public treeNode StmMore()
	{
		treeNode t = null;
		switch(token.Lex) {
		case RBIGPAREN:
		case ELSE:
			break;
		case LBIGPAREN:
		case TYPEDEF:
		case INT:
		case CHAR:
		case STRUCT:
		case ID:
		case IF:
		case WHILE:
		case FOR:
		case CIN:
		case COUT:
		case RETURN:
			t = StmList();
			break;
		default:
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			syntaxError("unexpected token is here!");
			break;
		}
		return t;
	}

	public treeNode Stm()
	{
		treeNode t = null;
		switch(token.Lex) {
		case TYPEDEF:
		case INT:
		case CHAR:
		case STRUCT:
		case ID:
			t = StmDecpart();
			break;
		case IF:
			t = ConditionalStm();
			break;
		case WHILE:
		case FOR:
			t = LoopStm();
			break;
		case CIN:
			t = InputStm();
			break;
		case COUT:
			t = OutputStm();
			break;
		case RETURN:
			t = ReturnStm();
			break;
		case LBIGPAREN:
			t = global.newStmlNode();
			t.lineno = line0;
			match(LexType.LBIGPAREN);
			t.child[0] = StmList();
			match(LexType.RBIGPAREN);
			break;
		default:
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			syntaxError("unexpected token is here!");
			break;
		}
		return t;
	}

	public treeNode StmDecpart()
	{
		treeNode t = null;
		switch(token.Lex) {
		case TYPEDEF:
			t = TypeDecpart();
			break;
		case INT:
		case CHAR:
			t = global.newDecNode();
			BaseType(t);
			ArrayMore(t);
			t.lineno = line0;
	        t.name[t.idnum] = token.Sem;
			match(LexType.ID);
			t.idnum ++;
			VarDec(t);
			break;
		case STRUCT:
			t = global.newDecNode();
			StructType(t);
	        t.name[t.idnum] = token.Sem;
			match(LexType.ID);
			t.lineno = line0;
			t.idnum ++;
			VarDec(t);
			break;
		case ID:
			temp_name = token.Sem;
			match(LexType.ID);
			t = IdDecpart();
			break;
		default:
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			syntaxError("unexpected token is here!");
			break;
		}
		return t;
	}

	public treeNode IdDecpart()
	{
		treeNode t = null;
		switch(token.Lex) {
		case ASSIGN:
		case LMIDPAREN:
		case DOT:
		case LPAREN:
			t = AssCall();
			break;
		case ID:
			t = global.newDecNode();
			t.lineno = line0;
			t.kind.dec = DecKind.IdK;
			t.attr.type_name = temp_name;
		    t.name[t.idnum] = token.Sem;
			t.idnum ++;
			match(LexType.ID);
			VarDec(t);
			break;
		default:
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			syntaxError("unexpected token is here!");
			break;
		}
		return t;
	}

	public treeNode AssCall()
	{
		treeNode t = null;
		switch(token.Lex) {
		case ASSIGN:
		case LMIDPAREN:
		case DOT:
			t = AssignmentRest();
			break;
		case LPAREN:
			t = CallStmRest();
			match(LexType.SEMI);
			break;
		default:
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			syntaxError("unexpected token is here!");
			break;
		}
		return t;
	}
	
	public treeNode AssignmentRest()
	{
		treeNode t = null;
		switch(token.Lex) {
		case ASSIGN:
		case LMIDPAREN:
		case DOT:
			t = global.newStmtNode(StmtKind.AssignK);
			/*处理第一个儿子结点，为变量表达式类型节点*/
			treeNode child1 = global.newExpNode(ExpKind.VariK);
			child1.lineno = line0;
			child1.name[0] = temp_name;
			child1.idnum ++;
			VariMore(child1);
			t.child[0] = child1;
			match(LexType.ASSIGN);
			t.lineno = line0;
			t.child[1] = RightPart();
			match(LexType.SEMI);
			break;
		default:
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			syntaxError("unexpected token is here!");
			break;
		}
		return t;
	}
	
	public treeNode RightPart()
	{
		treeNode t = null;
		switch(token.Lex) {
		case LPAREN:
		case INTC:
		case ID:
			t = Exp();
			break;
		case CHARC:
			/* 创建新的ConstK表达式类型语法树节点,赋值给t */
		    t = global.newExpNode(ExpKind.ConstK);
			t.attr.expAttr.type = ExpType.Char;
			t.attr.expAttr.char_val = token.Sem.charAt(0);
			match(LexType.CHARC);
		    t.lineno = line0;
			break;
		default:
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			syntaxError("unexpected token is here!");
			break;
		}
		return t;
	}
	
	public treeNode ConditionalStm()
	{
		treeNode t = null;
		switch(token.Lex) {
		case IF:
			t = global.newStmtNode(StmtKind.IfK);
			match(LexType.IF);
			match(LexType.LPAREN);
			t.lineno = line0;
			t.child[0] = RelExp();
			match(LexType.RPAREN);
			t.child[1] = Stm();
			if(token.Lex == LexType.ELSE)
			{
				match(LexType.ELSE);
				t.child[2] = Stm();
			}
			break;
		default:
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			syntaxError("unexpected token is here!");
			break;
		}
		return t;
	}

	public treeNode LoopStm()
	{
		treeNode t = null;
		switch(token.Lex) {
		case WHILE:
			t = global.newStmtNode(StmtKind.WhileK);
			match(LexType.WHILE);
			match(LexType.LPAREN);
			t.lineno = line0;
			t.child[0] = RelExp();
			match(LexType.RPAREN);
			t.child[1] = Stm();
			break;
		case FOR:
			t = global.newStmtNode(StmtKind.ForK);
			match(LexType.FOR);
			t.lineno = line0;
			match(LexType.LPAREN);
			temp_name = token.Sem;
			match(LexType.ID);
			t.child[0] = AssCall();
			t.child[1] = RelExp();
			match(LexType.SEMI);
			temp_name = token.Sem;
			match(LexType.ID);
			t.child[2] = AssCall();
			match(LexType.RPAREN);
			t.child[3] = Stm();
			break;
		default:
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			syntaxError("unexpected token is here!");
			break;
		}
		return t;
	}

	public treeNode InputStm()
	{
		treeNode t = null;
		switch(token.Lex) {
		case CIN:
			t = global.newStmtNode(StmtKind.CinK);
			match(LexType.CIN);
			match(LexType.IN);
			Invar(t);
			match(LexType.SEMI);
			break;
		default:
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			syntaxError("unexpected token is here!");
			break;
		}
		return t;
	}
	
	public void Invar(treeNode t)
	{
		switch(token.Lex) {
		case ID:
			if(t != null)
			{
				t.name[0] = token.Sem;
		        t.idnum ++;
			}
			match(LexType.ID);
			if(t != null)
				t.lineno = line0;
			break;
		default:
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			syntaxError("unexpected token is here!");
			break;
		}
	}
	
	public treeNode OutputStm()
	{
		treeNode t = null;
		switch(token.Lex) {
		case COUT:
			t = global.newStmtNode(StmtKind.CoutK);
			match(LexType.COUT);
			match(LexType.OUT);
			t.lineno = line0;
			t.child[0] = Exp();
			match(LexType.SEMI);
			break;
		default:
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			syntaxError("unexpected token is here!");
			break;
		}
		return t;
	}

	public treeNode ReturnStm()
	{
		treeNode t = null;
		switch(token.Lex) {
		case RETURN:
			t = global.newStmtNode(StmtKind.ReturnK);
			match(LexType.RETURN);
			t.lineno = line0;
			t.child[0] = Exp();
			match(LexType.SEMI);
			break;
		default:
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			syntaxError("unexpected token is here!");
			break;
		}
		return t;
	}

	public treeNode CallStmRest()
	{
		treeNode t = null;
		switch(token.Lex) {
		case LPAREN:
			t = global.newStmtNode(StmtKind.CallK);
			match(LexType.LPAREN);
			t.lineno = line0;
			/*函数名的结点也用表达式类型结点*/
			treeNode child0 = global.newExpNode(ExpKind.VariK); 
			child0.lineno = line0;
			child0.name[0] = temp_name;
			child0.idnum ++;
			t.child[0] = child0;
			t.child[1] = ActParamList();
			match(LexType.RPAREN);
			break;
		default:
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			syntaxError("unexpected token is here!");
			break;
		}
		return t;
	}

	public treeNode ActParamList()
	{
		treeNode t = null;
		switch(token.Lex) {
		case RPAREN:
			break;
		case LPAREN:
		case INTC:
		case ID:
			t = Exp();
			if(t != null)
				t.sibling = ActParamMore();
			break;
		default:
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			syntaxError("unexpected token is here!");
			break;
		}
		return t;
	}

	public treeNode ActParamMore()
	{
		treeNode t = null;
		switch(token.Lex) {
		case RPAREN:
			break;
		case COMMA:
			match(LexType.COMMA);
			t = ActParamList();
			break;
		default:
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			syntaxError("unexpected token is here!");
			break;
		}
		return t;
	}
	
	public treeNode RelExp()
	{
		treeNode t = null;
		treeNode p = null;
		switch(token.Lex) {
		case LPAREN:
		case INTC:
		case ID:
			t = Exp();
			p = OtherRelE();
		    if(p != null) 
			{
		      p.lineno = line0;
			  p.child[0] = t;
			  /* 将新的表达式类型语法树节点p作为函数返回值t */
		      t = p;
			}
			break;
		default:
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			syntaxError("unexpected token is here!");
			break;
		}
		return t;
	}
	
	public treeNode OtherRelE()
	{
		treeNode t = null;
		switch(token.Lex) {
		case LT:
		case GT:
		case LE:
		case GE:
		case EQ:
		case NEQ:
			t = global.newExpNode(ExpKind.OpK);
			CmpOp(t);
			t.child[1] = Exp();
			break;
		default:
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			syntaxError("unexpected token is here!");
			break;
		}
		return t;
	}

	public treeNode Exp()
	{
		treeNode t = null;
		treeNode p = null;
		switch(token.Lex) {
		case LPAREN:
		case INTC:
		case ID:
			t = Term();
			p = OtherTerm();
		    if(p != null) 
			{
		      p.lineno = line0;
			  p.child[0] = t;
			  /* 将函数返回值t赋成语法树节点p */
		      t = p;
			}
			break;
		default:
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			syntaxError("unexpected token is here!");
			break;
		}
		return t;
	}

	public treeNode OtherTerm()
	{
		treeNode t = null;
		switch(token.Lex) {
		case LT:
		case GT:
		case LE:
		case GE:
		case EQ:
		case NEQ:
		case RPAREN:
		case RMIDPAREN:
		case COMMA:
		case SEMI:
			break;
		case PLUS:
		case MINUS:
			t = global.newExpNode(ExpKind.OpK);
			AddOp(t);
			t.child[1] = Exp();
			break;
		default:
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			syntaxError("unexpected token is here!");
			break;
		}
		return t;
	}

	public treeNode Term()
	{
		treeNode t = null;
		treeNode p = null;
		switch(token.Lex) {
		case LPAREN:
		case INTC:
		case ID:
			t = Factor();
			p = OtherFactor();
			if(p != null)
			{
			    p.lineno= line0;
				p.child[0] = t;
			    t = p;
			}
			break;
		default:
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			syntaxError("unexpected token is here!");
			break;
		}
		return t;
	}

	public treeNode OtherFactor()
	{
		treeNode t = null;
		switch(token.Lex) {
		case PLUS:
		case MINUS:
		case LT:
		case GT:
		case LE:
		case GE:
		case EQ:
		case NEQ:
		case RPAREN:
		case RMIDPAREN:
		case COMMA:
		case SEMI:
			break;
		case TIMES:
		case OVER:
			t = global.newExpNode(ExpKind.OpK);
			MultOp(t);
			t.child[1] = Term();
			break;
		default:
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			syntaxError("unexpected token is here!");
			break;
		}
		return t;
	}

	public treeNode Factor()
	{
		treeNode t = null;
		switch(token.Lex) {
		case LPAREN:
			match(LexType.LPAREN);
			t = Exp();
			match(LexType.RPAREN);
			break;
		case INTC:
			/* 创建新的ConstK表达式类型语法树节点,赋值给t */
		    t = global.newExpNode(ExpKind.ConstK);
		    /* 将当前单词名tokenString转换为整数并赋给语法树节点t的数值成员attr.val	*/
			t.attr.expAttr.type = ExpType.Int;
			t.attr.expAttr.int_val = Integer.parseInt(token.Sem);
			match(LexType.INTC);
		    t.lineno = line0;
			break;
		case ID:
			t = Variable();
			break;
		default:
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			syntaxError("unexpected token is here!");
			break;
		}
		return t;
	}

	public treeNode Variable()
	{
		treeNode t = null;
		switch(token.Lex) {
		case ID:
			t = global.newExpNode(ExpKind.VariK);
			t.name[0] = token.Sem;
	        t.idnum ++;
			match(LexType.ID);
			t.lineno = line0;
			VIdMore(t);
			break;
		default:
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			syntaxError("unexpected token is here!");
			break;
		}
		return t;
	}

	public void VIdMore(treeNode t)
	{
		switch(token.Lex) {
		case LMIDPAREN:
		case DOT:
		case TIMES:
		case OVER:
		case PLUS:
		case MINUS:
		case LT:
		case GT:
		case LE:
		case GE:
		case EQ:
		case NEQ:
		case RPAREN:
		case RMIDPAREN:
		case COMMA:
		case SEMI:
			VariMore(t);
			break;
		case LPAREN:
			t = CallStmRest();
			break;
		default:
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			syntaxError("unexpected token is here!");
			break;
		}
	}

	public void VariMore(treeNode t)
	{
		switch(token.Lex) {
		case ASSIGN:
		case TIMES:
		case OVER:
		case PLUS:
		case MINUS:
		case LT:
		case GT:
		case LE:
		case GE:
		case EQ:
		case NEQ:
		case RPAREN:
		case RMIDPAREN:
		case COMMA:
		case SEMI:
			break;
		case LMIDPAREN:
			match(LexType.LMIDPAREN);
			if(t != null)
			{	/*用来以后求出其表达式的值，送入用于数组下标计算*/
				t.child[0] = Exp();
				t.attr.expAttr.varKind = VarKind.ArrayMembV;
				/*此表达式为数组成员变量类型*/
				t.child[0].attr.expAttr.varKind = VarKind.IdV;
			}
			match(LexType.RMIDPAREN);
			break;
		case DOT:
			match(LexType.DOT);
			if(t != null)
			{	/*第一个儿子指向域成员变量结点*/
				t.child[0] = FieldVar();
				t.attr.expAttr.varKind = VarKind.FieldMembV;
				t.child[0].attr.expAttr.varKind = VarKind.IdV;
			}
			break;
		default:
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			syntaxError("unexpected token is here!");
			break;
		}
	}

	public treeNode FieldVar()
	{
		treeNode t = null;
		switch(token.Lex) {
		case ID:
			t = global.newExpNode(ExpKind.VariK);
			t.name[0] = token.Sem;
		    t.idnum ++;
			match(LexType.ID);
			t.lineno = line0;
			FieldVarMore(t);
			break;
		default:
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			syntaxError("unexpected token is here!");
			break;
		}
		return t;
	}
	
	public void FieldVarMore(treeNode t)
	{
		switch(token.Lex) {
		case ASSIGN:
		case TIMES:
		case OVER:
		case PLUS:
		case MINUS:
		case LT:
		case GT:
		case LE:
		case GE:
		case EQ:
		case NEQ:
		case RPAREN:
		case RMIDPAREN:
		case COMMA:
		case SEMI:
			break;
		case LMIDPAREN:
			match(LexType.LMIDPAREN);
			if(t != null)
			{	/*用来以后求出其表达式的值，送入用于数组下标计算*/
				t.child[0] = Exp();
				t.child[0].attr.expAttr.varKind = VarKind.ArrayMembV;
			}
			match(LexType.RMIDPAREN);
			break;
		default:
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			syntaxError("unexpected token is here!");
			break;
		}
	}

	public void CmpOp(treeNode t)
	{
		switch(token.Lex) {
		case LT:
			match(LexType.LT);
			if(t != null)
				t.attr.expAttr.op = LexType.LT;
			break;
		case GT:
			match(LexType.GT);
			if(t != null)
				t.attr.expAttr.op = LexType.GT;
			break;
		case LE:
			match(LexType.LE);
			if(t != null)
				t.attr.expAttr.op = LexType.LE;
			break;
		case GE:
			match(LexType.GE);
			if(t != null)
		    	t.attr.expAttr.op = LexType.GE;
			break;
		case EQ:
			match(LexType.EQ);
			if(t != null)
		    	t.attr.expAttr.op = LexType.EQ;
			break;
		case NEQ:
			match(LexType.NEQ);
			if(t != null)
		    	t.attr.expAttr.op = LexType.NEQ;
			break;
		default:
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			syntaxError("unexpected token is here!");
			break;
		}
	}
	
	public void AddOp(treeNode t)
	{
		switch(token.Lex) {
		case PLUS:
			match(LexType.PLUS);
			if(t != null)
				t.attr.expAttr.op = LexType.PLUS;
			break;
		case MINUS:
			match(LexType.MINUS);
			if(t != null)
				t.attr.expAttr.op = LexType.MINUS;
			break;
		default:
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			syntaxError("unexpected token is here!");
			break;
		}
	}

	public void MultOp(treeNode t)
	{
		switch(token.Lex) {
		case TIMES:
			match(LexType.TIMES);
			if(t != null)
				t.attr.expAttr.op = LexType.TIMES;
			break;
		case OVER:
			match(LexType.OVER);
			if(t != null)
				t.attr.expAttr.op = LexType.OVER;
			break;
		default:
			try
			{token = myScanner.tokenChain.get(index);}
			catch(IndexOutOfBoundsException e)
			{syntaxError("you must lost something");}
			index ++;
			syntaxError("unexpected token is here!");
			break;
		}
	}
	
	/********************************************************/
	/* 	函数	Parse											*/
	/* 	功能	语法分析函数										*/
	/* 	说明	该函数把词法分析程序作为子程序调用,采用递归下降法				*/
	/*		  根据产生式调用递归处理函数,函数为源程序创建语法分析树			*/
	/********************************************************/
	public treeNode run()
	{
		/* 运行词法分析器,从源文件中取得token */
		myScanner.run();
		
		/* 从文件Tokenlist中取得第一个单词,将词法信息送给token */
		try
		{token = myScanner.tokenChain.get(index);}
		catch(IndexOutOfBoundsException e)
		{syntaxError("you must lost something");}
		index ++;
		
		/* 开始调用基本语法分析处理函数,递归下降处理 */
		treeNode root = Program();
		
		/* 输出语法树 */
		global.pw.println();
		global.pw.println("-------------------- 递归下降法语法树 ---------------------");
		global.pw.println();
		global.pw.flush();
		global.printTree(root);
		
		/* 函数返回语法树根节点root */
		return root;
	}
}