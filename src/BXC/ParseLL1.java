/******************************************************
 	文件	parseLL1.java
 	说明	编译器的LL1语法分析器实现
 	功能	构造语法树
 		进行语法错误检查
 	作者	包学超
******************************************************/

package BXC;
import java.util.Stack;
import BXC.Global.DecKind;
import BXC.Global.ExpKind;
import BXC.Global.ExpType;
import BXC.Global.LexType;
import BXC.Global.NLexType;
import BXC.Global.NodeKind;
import BXC.Global.StmtKind;
import BXC.Global.VarKind;
import BXC.Global.treeNode;

public class ParseLL1 {

	public static void main(String[] args)
	{
		ParseLL1 myParseLL1 = new ParseLL1();
		myParseLL1.run();
	}

	public static Global global = new Global();
	
	public static Scanner myScanner = new Scanner();

	/* 实现LL1分析用的分析栈，存放的是终极符和非终极符 */
	class StackNode {
		/* flag为0，表示栈中内容为非终极符, flag为1，表示栈中内容为终极符 */
		boolean flag;
		NLexType nLexVar; /* 非终极符部分 */
		LexType tLexVar; /* 终极符部分 */
	}
	public Stack<StackNode> stack = new Stack<StackNode>();

	/* 为建立语法树所设的指针栈 */
	public Stack<treeNode> stackPA = new Stack<treeNode>();

	/* 输入流 */
	public Token token;
	
	public int index = 0;

	public String temp_name;
	
	public boolean isForLoop;
	/********************************************************/
	/* 	函数	push											*/
	/* 	功能	压栈到stack										*/
	/* 	说明	使用了函数重载										*/
	/********************************************************/
	void push(NLexType j) {
		StackNode p = new StackNode();
		p.nLexVar = j;
		p.flag = false;
		stack.push(p);
	}
	void push(LexType j) {
		StackNode p = new StackNode();
		p.tLexVar = j;
		p.flag = true;
		stack.push(p);
	}
	
	/********************************************************/
	/* 	函数	syntaxError										*/
	/* 	功能	语法错误处理函数										*/
	/* 	说明	将函数参数message指定的错误信息格式化写入列表文件listing		*/
	/*		设置错误追踪标志Error为true							*/
	/********************************************************/
	public void syntaxError(String message)
	{
		if(token==null) token=new Token();
		global.pw.print(">>> error :   ");
		global.pw.printf("Syntax error at line %d, %s: %s",token.lineshow,message,token.Sem);
		global.pw.println();
		global.pw.flush();
		Global.Error = true;
		System.exit(0);
	}

	/********************************************************/
	/* 	函数	gettoken					     				*/
	/* 	功能	从Token序列中取出一个Token	                        */										
	/* 	说明	从文件中存的Token序列中依次取一个单词，作为当前单词.      		*/
	/********************************************************/
	public void getToken()
	{
		try
		{token = myScanner.tokenChain.get(index);}
		catch(IndexOutOfBoundsException e)
		{syntaxError("you must lost something");}
		index ++;
		Global.lineno = token.lineshow;
	}

	/********************************************************/
	/* 	 			以下是LL(1)产生式对应的所有函数					*/
	/********************************************************/
	void process0()
	{
		push(NLexType.Decpart);
	}
	void process1()
	{
		push(NLexType.DecpartMore);
		push(NLexType.TypeDecpart);
	}
	void process2()
	{
		push(NLexType.DecpartMore);
		push(NLexType.VFDecpart);
		push(NLexType.VFName);
		push(NLexType.TypeDef);
	}
	void process101()
	{
		push(LexType.ID);
		
		treeNode t = stackPA.lastElement();
		t.nodeKind = NodeKind.DecK;
		t.lineno = Global.lineno;
		t.name[t.idnum] = token.Sem;
		t.idnum ++;
	}
	void process3()
	{
		stackPA.pop();	//此时栈为空，节点全部弹出
	}
	void process4()
	{
		push(NLexType.Decpart);

		treeNode t = stackPA.pop();
		t.sibling = global.newFuncNode();
		stackPA.push(t.sibling);	//切换至下一条声明类型节点
	}
	void process5()
	{
		push(NLexType.VarDec);
	}
	void process6()
	{
		push(NLexType.FuncDec);
	}
	void process7()
	{
		push(LexType.SEMI);
		push(NLexType.TypeDef);
		push(LexType.ID);
		push(LexType.TYPEDEF);

		treeNode t = stackPA.lastElement();
		t.nodeKind = NodeKind.TypeK;
		t.lineno = Global.lineno;
		t.name[t.idnum] = myScanner.tokenChain.get(index).Sem;
		t.idnum ++;
	}
	void process8()
	{
		push(NLexType.ArrayMore);
		push(NLexType.BaseType);
	}
	void process9()
	{
		push(NLexType.StructType);
	}
	void process10()
	{
		push(LexType.ID);
		
		treeNode t = stackPA.lastElement();
	    t.kind.dec = DecKind.IdK;
		t.attr.type_name = token.Sem;
	}
	void process11()
	{
		push(LexType.INT);
		
		treeNode t = stackPA.lastElement();
        t.kind.dec = DecKind.IntK;
	}
	void process12()
	{
		push(LexType.CHAR);

		treeNode t = stackPA.lastElement();
        t.kind.dec = DecKind.CharK;
	}
	void process13()
	{}
	void process14()
	{
		push(LexType.RMIDPAREN);
		push(LexType.INTC);
		push(LexType.LMIDPAREN);

		treeNode t = stackPA.lastElement();
		t.attr.arrayAttr.size = Integer.parseInt(myScanner.tokenChain.get(index).Sem);
		t.attr.arrayAttr.childType = t.kind.dec;
        t.kind.dec = DecKind.ArrayK;
	}
	void process15()
	{
		push(LexType.RBIGPAREN);
		push(NLexType.FieldDecList);
		push(LexType.LBIGPAREN);
		push(NLexType.StructName);
		push(LexType.STRUCT);

		treeNode t = stackPA.lastElement();
        t.kind.dec = DecKind.StructK;
		t.child[0] = global.newDecNode();
		stackPA.push(t.child[0]);	//压入结构体域声明类型节点
	}
	void process16()
	{
		push(NLexType.FieldDecMore);
		push(LexType.SEMI);
		push(NLexType.IdList);
		push(NLexType.ArrayMore);
		push(NLexType.BaseType);

		treeNode t = stackPA.lastElement();
		t.nodeKind = NodeKind.DecK;
		t.lineno = Global.lineno;
	}
	void process17()
	{
		stackPA.pop();	//弹出结构体域声明类型节点
	}
	void process18()
	{
		push(NLexType.FieldDecList);
		
		treeNode t = stackPA.pop();
		t.sibling = global.newDecNode();
		stackPA.push(t.sibling);	//切换至下一条结构体域声明类型节点
	}
	void process19()
	{
		push(NLexType.IdMore);
		push(LexType.ID);
		
		treeNode t = stackPA.lastElement();
		t.name[t.idnum] = token.Sem;
		t.idnum ++;
	}
	void process20()
	{}
	void process21()
	{
		push(NLexType.IdList);
		push(LexType.COMMA);
	}
	void process22()
	{}
	void process23()
	{
		push(LexType.ID);

		treeNode t = stackPA.lastElement();
		t.name[t.idnum] = token.Sem;
		t.idnum ++;
	}
	void process24()
	{
		push(LexType.SEMI);
		push(NLexType.VarIdMore);
	}
	void process26()
	{}
	void process27()
	{
		push(NLexType.VarIdList);
		push(LexType.COMMA);
	}
	void process25()
	{
		push(NLexType.VarIdMore);
		push(LexType.ID);
		
		treeNode t = stackPA.lastElement();
		t.name[t.idnum] = token.Sem;
		t.idnum ++;
	}
	void process28()
	{
		push(LexType.RBIGPAREN);
		push(NLexType.StmList);
		push(LexType.LBIGPAREN);
		push(LexType.RPAREN);
		push(NLexType.ParamList);
		push(LexType.LPAREN);

		treeNode t = stackPA.lastElement();
		t.nodeKind = NodeKind.FuncDecK;
		t.attr.funcAttr.returnType = t.kind.dec;
		t.child[1] = global.newStmlNode();
		t.child[1].lineno = Global.lineno;
		t.child[1].child[0] = global.newRootNode();
		t.child[0] = global.newDecNode();
		stackPA.push(t.child[1].child[0]);	//压入函数体的语句部分的第一个节点
		stackPA.push(t.child[0]);	//压入第一个参数表节点
	}
	void process29()
	{
		stackPA.pop();	//参数表为空时，弹出参数表节点
		treeNode t = stackPA.pop();
		stackPA.lastElement().child[0] = null;
		stackPA.push(t);
	}
	void process30()
	{
		push(NLexType.ParamDecList);
	}
	void process31()
	{
		push(NLexType.ParamMore);
		push(NLexType.Param);
	}
	void process32()
	{
		stackPA.pop();	//参数表不为空时，弹出参数表节点
	}
	void process33()
	{
		push(NLexType.ParamDecList);
		push(LexType.COMMA);
		
		treeNode t = stackPA.pop();
		t.sibling = global.newDecNode();
		stackPA.push(t.sibling);	//切换至下一参数表节点
	}
	void process34()
	{
		push(NLexType.FormName);
		push(NLexType.TypeDef);
	}
	void process35()
	{
		push(LexType.ID);

		treeNode t = stackPA.lastElement();
		t.name[t.idnum] = token.Sem;
		t.idnum ++;
	}
	void process36()
	{
		push(NLexType.StmMore);
		push(NLexType.Stm);
	}
	void process37()
	{
		stackPA.pop();	//弹出函数体的语句部分节点
	}
	void process38()
	{
		push(NLexType.StmList);

		treeNode t = stackPA.pop();
		t.sibling = global.newRootNode();
		stackPA.push(t.sibling);	//切换至函数体的语句部分的下一节点
	}
	void process39()
	{
		push(NLexType.StmDecpart);
	}
	void process40()
	{
		push(NLexType.ConditionalStm);
	}
	void process41()
	{
		push(NLexType.LoopStm); 
	}
	void process42()
	{
		push(NLexType.InputStm);
	}
	void process43()
	{
		push(NLexType.OutputStm);
	}
	void process44()
	{
		push(NLexType.ReturnStm);
	}
	void process45()
	{
		push(LexType.RBIGPAREN);
		push(NLexType.StmList);
		push(LexType.LBIGPAREN);

		treeNode t = stackPA.lastElement();
		t.nodeKind = NodeKind.StmLK;
		t.child[0] = global.newRootNode();
		stackPA.push(t.child[0]);	//压入语句集合（StmLK）的第一个成员语句（StmtK）节点
	}
	void process46()
	{
		push(NLexType.TypeDecpart);
	}
	void process47()
	{
		push(NLexType.VarDec);
		push(NLexType.Name1);
		push(NLexType.ArrayMore);
		push(NLexType.BaseType);

		treeNode t = stackPA.lastElement();
		t.nodeKind = NodeKind.DecK;
		t.lineno = Global.lineno;
	}
	void process102()
	{
		push(LexType.ID);

		treeNode t = stackPA.lastElement();
		t.name[t.idnum] = token.Sem;
		t.idnum ++;
	}
	void process48()
	{
		push(NLexType.VarDec);
		push(NLexType.Name2);
		push(NLexType.StructType);

		treeNode t = stackPA.lastElement();
		t.nodeKind = NodeKind.DecK;
		t.lineno = Global.lineno;
	}
	void process103()
	{
		push(LexType.ID);

		treeNode t = stackPA.lastElement();
		t.name[t.idnum] = token.Sem;
		t.idnum ++;
	}
	void process49()
	{
		push(NLexType.IdDecpart);
		push(LexType.ID);
		
		temp_name = token.Sem;
	}
	void process50()
	{
		push(NLexType.AssCall);
	}
	void process51()
	{
		push(NLexType.VarDec);
		push(LexType.ID);
		
		treeNode t = stackPA.lastElement();
		t.nodeKind = NodeKind.DecK;
		t.kind.dec = DecKind.IdK;
		t.attr.type_name = temp_name;
		t.lineno = Global.lineno;
        t.name[t.idnum] = token.Sem;
		t.idnum ++;
	}
	void process52()
	{
		push(NLexType.AssignmentRest);
	}
	void process53()
	{
		push(LexType.SEMI);
		push(NLexType.CallStmRest);
	}
	void process54()
	{
		push(LexType.SEMI);
		push(LexType.END_POP);	//赋值右式节点完成，需要弾出
		push(NLexType.RightPart);
		push(LexType.ASSIGN);
		push(LexType.END_POP);	//赋值左式节点完成，需要弾出
		push(NLexType.VariMore);

		treeNode t = stackPA.lastElement();
		t.nodeKind = NodeKind.StmtK;
		t.kind.stmt = StmtKind.AssignK;
		t.lineno = Global.lineno;
		/*处理第一个儿子结点，为变量表达式类型节点*/
		t.child[0] = global.newExpNode(ExpKind.VariK);
		t.child[0].lineno = Global.lineno;
		if (isForLoop)
		{
			t.child[0].name[0] = t.name[0];
			isForLoop = false;
		}
		else
			t.child[0].name[0] = temp_name;
		t.child[0].idnum ++;
		t.child[1] = global.newRootNode();
		stackPA.push(t.child[1]);	//压入赋值右式节点
		stackPA.push(t.child[0]);	//压入赋值左式节点
	}
	void process55()
	{
		push(NLexType.Exp);
	}
	void process56()
	{
		push(LexType.CHARC);
		
		treeNode t = stackPA.lastElement();
	    t.nodeKind = NodeKind.ExpK;
	    t.kind.exp = ExpKind.ConstK;
	    if(t.attr.expAttr.varKind == null)
	    	t.attr.expAttr.varKind = VarKind.IdV;
	    if(t.attr.expAttr.type == null)
	    	t.attr.expAttr.type = ExpType.Char;
		t.attr.expAttr.char_val = token.Sem.charAt(0);
	    t.lineno = Global.lineno;
	}
	void process57()
	{
		push(NLexType.ElseStm);
		push(LexType.END_POP);	//IF(0) 1 ELSE 2 中的1部分完成，需要弾出
		push(NLexType.Stm);
		push(LexType.RPAREN);
		push(LexType.END_POP);	//IF(0) 1 ELSE 2 中的0部分完成，需要弾出
		push(NLexType.RelExp);
		push(LexType.LPAREN);
		push(LexType.IF);

		treeNode t = stackPA.lastElement();
		t.nodeKind = NodeKind.StmtK;
		t.kind.stmt = StmtKind.IfK;
		t.lineno = Global.lineno;
		t.child[1] = global.newRootNode();
		t.child[0] = global.newRootNode();
		stackPA.push(t.child[1]);	//IF(0) 1 ELSE 2 中的1部分
		stackPA.push(t.child[0]);	//IF(0) 1 ELSE 2 中的0部分
	}
	void process99()
	{}
	void process100()
	{
		push(LexType.END_POP);	//IF(0) 1 ELSE 2 中的2部分完成，需要弾出
		push(NLexType.Stm);
		push(LexType.ELSE);
		
		treeNode t = stackPA.lastElement();
		t.child[2] = global.newRootNode();
		stackPA.push(t.child[2]);	//IF(0) 1 ELSE 2 中的2部分
	}
	void process58()
	{
		push(LexType.END_POP);	//WHILE(0) 1 中的1部分完成，需要弾出
		push(NLexType.Stm);
		push(LexType.RPAREN);
		push(LexType.END_POP);	//WHILE(0) 1 中的0部分完成，需要弾出
		push(NLexType.RelExp);
		push(LexType.LPAREN);
		push(LexType.WHILE);

		treeNode t = stackPA.lastElement();
		t.nodeKind = NodeKind.StmtK;
		t.kind.stmt = StmtKind.WhileK;
		t.lineno = Global.lineno;
		t.child[0] = global.newRootNode();
		t.child[1] = global.newRootNode();
		stackPA.push(t.child[1]);	//WHILE(0) 1 中的1部分
		stackPA.push(t.child[0]);	//WHILE(0) 1 中的0部分
	}
	void process59()
	{
		push(LexType.END_POP);	//FOR(0 1 2) 3 中的3部分完成，需要弾出
		push(NLexType.Stm);
		push(LexType.RPAREN);
		push(LexType.END_POP);	//FOR(0 1 2) 3 中的2部分完成，需要弾出
		push(NLexType.AssCall);
		push(NLexType.Name3);
		push(LexType.SEMI);
		push(LexType.END_POP);	//FOR(0 1 2) 3 中的1部分完成，需要弾出
		push(NLexType.RelExp);
		push(LexType.END_POP);	//FOR(0 1 2) 3 中的0部分完成，需要弾出
		push(NLexType.AssCall);
		push(LexType.ID);
		push(LexType.LPAREN);
		push(LexType.FOR);
		
		treeNode t = stackPA.lastElement();
		t.nodeKind = NodeKind.StmtK;
		t.kind.stmt = StmtKind.ForK;
		t.lineno = Global.lineno;
		t.child[0] = global.newRootNode();
		t.child[0].name[0] = myScanner.tokenChain.get(index+1).Sem;
		isForLoop = true;
		t.child[1] = global.newRootNode();
		t.child[2] = global.newRootNode();
		t.child[3] = global.newRootNode();
		stackPA.push(t.child[3]);	//FOR(0 1 2) 3 中的3部分
		stackPA.push(t.child[2]);	//FOR(0 1 2) 3 中的2部分
		stackPA.push(t.child[1]);	//FOR(0 1 2) 3 中的1部分
		stackPA.push(t.child[0]);	//FOR(0 1 2) 3 中的0部分
	}
	void process104()
	{
		push(LexType.ID);

		temp_name = token.Sem;
	}
	void process60()
	{
		push(LexType.SEMI);
		push(NLexType.Invar);
		push(LexType.IN);
		push(LexType.CIN);

		treeNode t = stackPA.lastElement();
		t.nodeKind = NodeKind.StmtK;
		t.kind.stmt = StmtKind.CinK;
	}
	void process61()
	{
		push(LexType.ID);

		treeNode t = stackPA.lastElement();
		t.name[0] = token.Sem;
        t.idnum ++;
		t.lineno = Global.lineno;
	}
	void process62()
	{
		push(LexType.SEMI);
		push(LexType.END_POP);	//输出表达式处理完成，需要弾出
		push(NLexType.Exp);
		push(LexType.OUT);
		push(LexType.COUT);

		treeNode t = stackPA.lastElement();
		t.nodeKind = NodeKind.StmtK;
		t.kind.stmt = StmtKind.CoutK;
		t.lineno = Global.lineno;
		t.child[0] = global.newRootNode();
		stackPA.push(t.child[0]);	//压入输出表达式（ExpK）
	}
	void process63()
	{
		push(LexType.SEMI);
		push(LexType.END_POP);	//返回表达式处理完成，需要弾出
		push(NLexType.Exp);
		push(LexType.RETURN);
		
		treeNode t = stackPA.lastElement();
		t.nodeKind = NodeKind.StmtK;
		t.kind.stmt = StmtKind.ReturnK;
		t.lineno = Global.lineno;
		t.child[0] = global.newRootNode();
		stackPA.push(t.child[0]);	//压入返回表达式（ExpK）
	}
	void process64()
	{
		push(LexType.RPAREN);
		push(NLexType.ActParamList);
		push(LexType.LPAREN);

		treeNode t = stackPA.lastElement();
		t.nodeKind = NodeKind.StmtK;
		t.kind.stmt = StmtKind.CallK;
		t.lineno = Global.lineno;
		/*函数名的结点也用表达式类型结点*/
		t.child[0] = global.newExpNode(ExpKind.VariK);
		t.child[0].lineno = Global.lineno;
		t.child[0].name[0] = temp_name;
		t.child[0].idnum ++;
		t.child[1] = global.newRootNode();
		stackPA.push(t.child[1]);	//压入第一个实参表节点
	}
	void process65()
	{
		stackPA.pop();	//实参表为空时，弹出实参表节点
		stackPA.lastElement().child[1] = null;
	}
	void process66()
	{
		push(NLexType.ActParamMore);
		push(NLexType.Exp);
	}
	void process67()
	{
		stackPA.pop();	//实参表不为空时，弹出实参表节点
	}
	void process68()
	{
		push(NLexType.ActParamList);
		push(LexType.COMMA);
		
		treeNode t = stackPA.pop();
		t.sibling = global.newRootNode();
		stackPA.push(t.sibling);	//切换至下一实参表节点
	}
	void process69()
	{
		push(NLexType.OtherRelE);
		push(NLexType.Exp);

		treeNode t = global.newRootNode();
		stackPA.push(t);	//压入左表达式节点
	}
	void process70()
	{
		push(LexType.END_POP);	//弹出右表达式节点
		push(NLexType.Exp);
		push(NLexType.CmpOp);

		treeNode l = stackPA.pop();	//左表达式处理完成，弹出节点
		treeNode t = stackPA.pop();	//弹出比较符节点
		t.nodeKind = NodeKind.ExpK;
		t.kind.exp = ExpKind.OpK;
		if(t.attr.expAttr.varKind == null)
			t.attr.expAttr.varKind = VarKind.IdV;
	    if(t.attr.expAttr.type == null)
	    	t.attr.expAttr.type = ExpType.Void;
	    t.lineno = Global.lineno;
		t.child[0] = l;
		t.child[1] = global.newRootNode();
		stackPA.push(t.child[1]);	//压入右表达式节点
		stackPA.push(t);	//压入比较符节点
	}
	void process71()
	{
		push(NLexType.OtherTerm);
		push(NLexType.Term);
	}
	void process72()
	{}
	void process73()
	{
		push(LexType.END_POP);	//弹出右项节点
		push(NLexType.Exp);
		push(NLexType.AddOp);

		treeNode l = stackPA.pop();	//左项处理完成，弹出节点
		treeNode t = global.newRootNode();	//加减法运算符节点
		t.nodeKind = NodeKind.ExpK;
		t.kind.exp = ExpKind.OpK;
		if(t.attr.expAttr.varKind == null)
			t.attr.expAttr.varKind = VarKind.IdV;
	    if(t.attr.expAttr.type == null)
	    	t.attr.expAttr.type = ExpType.Void;
	    t.lineno = Global.lineno;
	    treeNode a = stackPA.lastElement(); //改变右式（a）指针值，使指向运算符节点
	    if(a.kind.stmt==StmtKind.CoutK||a.kind.stmt==StmtKind.ReturnK)
	    	a.child[0] = t;
	    else
	    	a.child[1] = t;
		t.child[0] = l;
		t.child[1] = global.newRootNode();
		stackPA.push(t.child[1]);	//压入右项节点
		stackPA.push(t);	//压入加减法运算符节点
	}
	void process74()
	{
		push(NLexType.OtherFactor);
		push(NLexType.Factor);
	}
	void process75()
	{}
	void process76()
	{
		push(LexType.END_POP);	//弹出右因子节点
		push(NLexType.Term);
		push(NLexType.MultOp);

		treeNode l = stackPA.pop();	//左因子处理完成，弹出节点
		treeNode t = global.newRootNode();	//乘除法运算符节点
		t.nodeKind = NodeKind.ExpK;
		t.kind.exp = ExpKind.OpK;
		if(t.attr.expAttr.varKind == null)
			t.attr.expAttr.varKind = VarKind.IdV;
	    if(t.attr.expAttr.type == null)
	    	t.attr.expAttr.type = ExpType.Void;
	    t.lineno = Global.lineno;
	    treeNode a = stackPA.lastElement(); //改变右式（a）指针值，使指向运算符节点
	    if(a.kind.stmt==StmtKind.CoutK||a.kind.stmt==StmtKind.ReturnK)
	    	a.child[0] = t;
	    else
	    	a.child[1] = t;
		t.child[0] = l;
		t.child[1] = global.newRootNode();
		stackPA.push(t.child[1]);	//压入右因子节点
		stackPA.push(t);	//压入乘除法运算符节点
	}
	void process77()
	{
		push(LexType.RPAREN);
		push(NLexType.Exp);
		push(LexType.LPAREN);
	}
	void process78()
	{
		push(LexType.INTC);

		treeNode t = stackPA.lastElement();
		/* 创建新的ConstK表达式类型语法树节点,赋值给t */
		t.nodeKind = NodeKind.ExpK;
		t.kind.exp = ExpKind.ConstK;
	    /* 将当前单词名tokenString转换为整数并赋给语法树节点t的数值成员attr.val	*/
		if(t.attr.expAttr.varKind == null)
			t.attr.expAttr.varKind = VarKind.IdV;
	    if(t.attr.expAttr.type == null)
	    	t.attr.expAttr.type = ExpType.Int;
		t.attr.expAttr.int_val = Integer.parseInt(token.Sem);
	    t.lineno = Global.lineno;
	}
	void process79()
	{
		push(NLexType.Variable);
	}
	void process80()
	{
		push(NLexType.VIdMore);
		push(LexType.ID);

		treeNode t = stackPA.lastElement();
		t.nodeKind = NodeKind.ExpK;
		t.kind.exp = ExpKind.VariK;
		if(t.attr.expAttr.varKind == null)
			t.attr.expAttr.varKind = VarKind.IdV;
	    if(t.attr.expAttr.type == null)
	    	t.attr.expAttr.type = ExpType.Void;
		t.name[0] = token.Sem;
        t.idnum ++;
		t.lineno = Global.lineno;
	}
	void process81()
	{
		push(NLexType.VariMore);
	}
	void process82()
	{
		push(NLexType.CallStmRest);
	}
	void process83()
	{}
	void process84()
	{
		push(LexType.RMIDPAREN);
		push(LexType.END_POP);	//弹出数组成员表达式节点
		push(NLexType.Exp);
		push(LexType.LMIDPAREN);

		treeNode t = stackPA.lastElement();
		t.attr.expAttr.varKind = VarKind.ArrayMembV;
		/*此表达式为数组成员变量类型*/
		t.child[0] = global.newRootNode();
		t.child[0].attr.expAttr.varKind = VarKind.IdV;
		/*用来以后求出其表达式的值，送入用于数组下标计算*/
		stackPA.push(t.child[0]);	//压入数组成员表达式节点
	}
	void process85()
	{
		push(LexType.END_POP);	//弹出结构体成员表达式节点
		push(NLexType.FieldVar);
		push(LexType.DOT);

		treeNode t = stackPA.lastElement();
		t.attr.expAttr.varKind = VarKind.FieldMembV;
		t.child[0] = global.newRootNode();
		t.child[0].attr.expAttr.varKind = VarKind.IdV;
		/*第一个儿子指向域成员变量结点*/
		stackPA.push(t.child[0]);	//压入结构体成员表达式节点
	}
	void process86()
	{
		push(NLexType.FieldVarMore);
		push(LexType.ID);

		treeNode t = stackPA.lastElement();
		t.nodeKind = NodeKind.ExpK;
		t.kind.exp = ExpKind.VariK;
		if(t.attr.expAttr.varKind == null)
			t.attr.expAttr.varKind = VarKind.IdV;
	    if(t.attr.expAttr.type == null)
	    	t.attr.expAttr.type = ExpType.Void;
		t.name[0] = token.Sem;
	    t.idnum ++;
		t.lineno = Global.lineno;
	}
	void process87()
	{}
	void process88()
	{
		push(LexType.RMIDPAREN);
		push(LexType.END_POP);	//弹出结构体数组成员表达式节点
		push(NLexType.Exp);
		push(LexType.LMIDPAREN);

		treeNode t = stackPA.lastElement();
		t.child[0] = global.newRootNode();
		t.child[0].attr.expAttr.varKind = VarKind.ArrayMembV;
		stackPA.push(t.child[0]);	//压入结构体数组成员表达式节点
	}
	void process89()
	{
		push(LexType.LT);

		/* 处理完比较符后，调换比较符和右表达式节点顺序 */
		treeNode t = stackPA.pop();	//弹出比较符节点
		treeNode r = stackPA.pop();	//弹出右表达式节点
		t.attr.expAttr.op = LexType.LT;
		stackPA.push(t);	//压入比较符节点
		stackPA.push(r);	//压入右表达式节点
	}
	void process90()
	{
		push(LexType.GT);

		/* 处理完比较符后，调换比较符和右表达式节点顺序 */
		treeNode t = stackPA.pop();	//弹出比较符节点
		treeNode r = stackPA.pop();	//弹出右表达式节点
		t.attr.expAttr.op = LexType.GT;
		stackPA.push(t);	//压入比较符节点
		stackPA.push(r);	//压入右表达式节点
	}
	void process91()
	{
		push(LexType.LE);

		/* 处理完比较符后，调换比较符和右表达式节点顺序 */
		treeNode t = stackPA.pop();	//弹出比较符节点
		treeNode r = stackPA.pop();	//弹出右表达式节点
		t.attr.expAttr.op = LexType.LE;
		stackPA.push(t);	//压入比较符节点
		stackPA.push(r);	//压入右表达式节点
	}
	void process92()
	{
		push(LexType.GE);

		/* 处理完比较符后，调换比较符和右表达式节点顺序 */
		treeNode t = stackPA.pop();	//弹出比较符节点
		treeNode r = stackPA.pop();	//弹出右表达式节点
		t.attr.expAttr.op = LexType.GE;
		stackPA.push(t);	//压入比较符节点
		stackPA.push(r);	//压入右表达式节点
	}
	void process93()
	{
		push(LexType.EQ);

		/* 处理完比较符后，调换比较符和右表达式节点顺序 */
		treeNode t = stackPA.pop();	//弹出比较符节点
		treeNode r = stackPA.pop();	//弹出右表达式节点
		t.attr.expAttr.op = LexType.EQ;
		stackPA.push(t);	//压入比较符节点
		stackPA.push(r);	//压入右表达式节点
	}
	void process94()
	{
		push(LexType.NEQ);

		/* 处理完比较符后，调换比较符和右表达式节点顺序 */
		treeNode t = stackPA.pop();	//弹出比较符节点
		treeNode r = stackPA.pop();	//弹出右表达式节点
		t.attr.expAttr.op = LexType.NEQ;
		stackPA.push(t);	//压入比较符节点
		stackPA.push(r);	//压入右表达式节点
	}
	void process95()
	{
		push(LexType.PLUS);

		/* 处理完加减法运算符后，调换加减法运算符和右项节点顺序 */
		treeNode t = stackPA.pop();	//弹出加减法运算符节点
		treeNode r = stackPA.pop();	//弹出右项节点
		t.attr.expAttr.op = LexType.PLUS;
		stackPA.push(t);	//压入加减法运算符节点
		stackPA.push(r);	//压入右项节点
	}
	void process96()
	{
		push(LexType.MINUS);

		/* 处理完加减法运算符后，调换加减法运算符和右项节点顺序 */
		treeNode t = stackPA.pop();	//弹出加减法运算符节点
		treeNode r = stackPA.pop();	//弹出右项节点
		t.attr.expAttr.op = LexType.MINUS;
		stackPA.push(t);	//压入加减法运算符节点
		stackPA.push(r);	//压入右项节点
	}
	void process97()
	{
		push(LexType.TIMES);

		/* 处理完乘除法运算符后，调换乘除法运算符和右因子节点顺序 */
		treeNode t = stackPA.pop();	//弹出乘除法运算符节点
		treeNode r = stackPA.pop();	//弹出右因子节点
		t.attr.expAttr.op = LexType.TIMES;
		stackPA.push(t);	//压入乘除法运算符节点
		stackPA.push(r);	//压入右因子节点
	}
	void process98()
	{
		push(LexType.OVER);

		/* 处理完乘除法运算符后，调换乘除法运算符和右因子节点顺序 */
		treeNode t = stackPA.pop();	//弹出乘除法运算符节点
		treeNode r = stackPA.pop();	//弹出右因子节点
		t.attr.expAttr.op = LexType.OVER;
		stackPA.push(t);	//压入乘除法运算符节点
		stackPA.push(r);	//压入右因子节点
	}

	/********************************************************/
	/* 	函数	predict											*/
	/* 	功能	选择产生式函数							  			*/
	/********************************************************/
	public void predict(int num)
	{
		switch(num)
		{
	      case 0:     process0();	break;
	      case 1:     process1();	break;
	      case 2:     process2();	break;
	      case 3:     process3();	break;
	      case 4:     process4();   break;
		  case 5:	  process5();   break;
	      case 6:	  process6();	break;
	      case 7:	  process7();	break;
	      case 8:	  process8();	break;
		  case 9:	  process9();   break;
	      case 10:	  process10();	break;
	      case 11:	  process11();	break;
	      case 12:	  process12();	break;
	      case 13:	  process13();	break;
	      case 14:	  process14();	break;
	      case 15:	  process15();	break;
	      case 16:	  process16();	break;
	      case 17:	  process17();	break;
	      case 18:	  process18();	break;
	      case 19:	  process19();	break;
	      case 20:	  process20();	break;
	      case 21:	  process21();	break;
		  case 22:	  process22();  break;
	      case 23:	  process23();  break;
	      case 24:	  process24();	break;
		  case 25:	  process25();  break;
	      case 26:	  process26();  break;
	      case 27:	  process27();  break;
	      case 28:	  process28();  break;
	      case 29:	  process29();	break;
	      case 30:	  process30();	break;
		  case 31:	  process31();  break;
	      case 32:	  process32();  break;
	      case 33:	  process33();	break;
		  case 34:	  process34();  break;
	      case 35:	  process35();  break;
		  case 36:	  process36();  break;
	      case 37:	  process37();  break;
	      case 38:	  process38();	break;
	      case 39:	  process39();	break;
	      case 40:	  process40();  break;
	      case 41:	  process41();  break;
	      case 42:	  process42();	break;
		  case 43:	  process43();  break;
	      case 44:	  process44();  break;
	      case 45:	  process45();	break; 
		  case 46:    process46();  break;
	      case 47:	  process47();	break;
	      case 48:	  process48();  break;
	      case 49:	  process49();  break;
	      case 50:	  process50();	break;
		  case 51:	  process51();  break;
	      case 52:	  process52();	break;
	      case 53:	  process53();	break;
	      case 54:	  process54();  break;
	      case 55:	  process55();  break;
	      case 56:	  process56();	break;
		  case 57:	  process57();  break;
	      case 58:	  process58();	break;
	      case 59:	  process59();	break;
	      case 60:	  process60();	break;
	      case 61:	  process61();	break;
	      case 62:	  process62();	break;
	      case 63:	  process63();	break;
	      case 64:	  process64();	break;
	      case 65:	  process65();	break;
	      case 66:	  process66();	break;
	      case 67:	  process67();	break;
	      case 68:	  process68();	break;
		  case 69:    process69();  break;
		  case 70:    process70();  break;
	      case 71:	  process71();	break;
	      case 72:	  process72();	break;
	      case 73:	  process73();  break;
	      case 74:	  process74();  break;
	      case 75:	  process75();  break;
		  case 76:	  process76();  break;
	      case 77:	  process77();	break;
		  case 78:    process78();  break;
	      case 79:    process79();  break;
		  case 80:	  process80();  break;
	      case 81:	  process81();  break;
	      case 82:	  process82();	break;
	      case 83:	  process83();  break;
	      case 84:	  process84();  break;
	      case 85:	  process85();	break;
		  case 86:	  process86();  break;
	      case 87:	  process87();  break;
	      case 88:	  process88();	break;
		  case 89:	  process89();  break;
	      case 90:	  process90();	break;
	      case 91:	  process91();	break;
	      case 92:	  process92();	break;
	      case 93:	  process93();	break;
	      case 94:	  process94();	break;
	      case 95:	  process95();	break;
	      case 96:	  process96();	break;
	      case 97:    process97();  break;
		  case 98:    process98();  break;
	      case 99:    process99();  break;
		  case 100:   process100(); break;
		  case 101:   process101(); break;
		  case 102:   process102(); break;
		  case 103:   process103(); break;
		  case 104:   process104(); break;
		  default:    syntaxError("unexpected token");
	   }
	}   

	/********************************************************/
	/* 	函数	run												*/
	/* 	功能	LL1语法分析主函数						    		*/
	/********************************************************/
	public treeNode run() {

		/* 运行词法分析器,从源文件中取得token */
		myScanner.run();

		global.CreatLL1Table();
		
		/* 指向整个语法树根节点的指针，由它得到语法树 */
		treeNode root = global.newRootNode();
		root.child[0] = global.newFuncNode();

		/* 从这里开始进行语法分析和语法树的生成 */
		push(NLexType.Program);
		stackPA.push(root.child[0]);

		/* 取一个token */
		getToken();

		while (!stack.isEmpty()) {
			/* 检测终极符是否匹配 */
			if (stack.lastElement().flag)/* 读栈顶标志，看是终极符还是非终极符 */
			{
				LexType stacktopT = stack.lastElement().tLexVar;
				if (stacktopT == LexType.END_POP) {
					stackPA.pop();
					stack.pop();
				} else if (stacktopT == token.Lex) {
					getToken();/* 取下一个token */
					stack.pop();
				} else
					syntaxError("unexpected token");
			} else {
				/* 根据非终极符和栈中符号进行预测 */
				NLexType stacktopN = stack.lastElement().nLexVar;
				stack.pop();
				predict(Global.LL1Table[stacktopN.ordinal()][token.Lex.ordinal()]);
			}
		}

		if(token.Lex != LexType.ENDFILE)
			syntaxError("Code ends before file");

		/* 输出语法树 */
		global.pw.println();
		global.pw.println("----------------------- LL(1)语法树 -----------------------");
		global.pw.println();
		global.pw.flush();
		global.printTree(root);
		
		return root;
	}
}