/******************************************************
 	文件	Global.java
 	说明	编译器的各个组件将会使用到的全局变量及公共函数
 	功能	定义文件输出流
 		包括词法分析部分
 		包括递归下降法语法分析部分
 		包括LL(1)语法分析部分
 	作者	包学超
******************************************************/

package BXC;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import WSL.Analyze.SymbTable;

public class Global {

	/************************ 文件输出流 ****************************/
	public String list = "F:\\LineList.txt";	//列表输出文件
	public File ListFile = new File(list);
	public BufferedWriter writer = null;
	{try {
		if(ListFile.exists())
			ListFile.delete();
		ListFile.createNewFile();
		writer = new BufferedWriter(new FileWriter(ListFile, true));
	}catch (IOException e) {}}
	public PrintWriter pw = new PrintWriter(writer);

	/************************************************************
								词法分析部分
	*************************************************************/
	
	/*************** 词法分析器确定性有限自动机DFA的状态类型   *****************
	 	Start 开始状态;		InID 标识符状态;    	InNumber 数字状态;
	 	InAssign 赋值状态;		InCin 输入状态;		InCout 输出状态;
	 	InComment 注释状态;	ComBegin 注释开始;		ComEnd 注释结束;
	 	InChar 字符状态;		Error 出错状态;		Done 完成状态;
	*************************************************************/
	public enum StateType
	{
		START,		INID,		INNUMBER,
		INASSIGN,	INCIN,		INCOUT,
		INCOMMENT,	COMBEGIN,	COMEND,
		INCHAR,		ERROR,		DONE
	};
	
	/************************* 单词的词法类型 *************************/
	/*所有终极符*/
	public enum LexType
	{
		/* 簿记单词符号 */
	    ENDFILE,	ERROR,
		/* 保留字	总计：11	*/
	    TYPEDEF,	STRUCT,		IF,			ELSE,		WHILE,
	    FOR,		CIN,		COUT,		RETURN,		INT,	CHAR,
		/* 多字符单词符号 */
	    ID,			INTC,		CHARC,
	    /*特殊符号 */
		ASSIGN,		EQ,			LT,			LE,			GT,
		GE,			NEQ,		PLUS,		MINUS,/*减*/	TIMES,
		OVER,/*除*/	LPAREN,		RPAREN,		DOT,		SEMI,
		COMMA,		LMIDPAREN,	RMIDPAREN,	LBIGPAREN,	RBIGPAREN,
		IN,			OUT,
		/* 语法分析中的弾栈信号 */
		END_POP
	};

	/* 源代码清单的行号 */
	public static int lineno = 0;
	
	/*Token序列中的token数目*/
	public static int Tokennum = 0;
	
	/* lineBuf为当前输入代码行缓冲区 */
	public static String lineBuf = null;
	
	/* linepos为在代码缓冲区LineBuf中的当前字符位置,初始为0 */
	public static int linepos = 0;
	
	/* bufsize为当前缓冲器中所存字串大小 */
	public static int bufsize = 0;
	
	/* EOF_flag当为文件尾时,改变函数ungetNextChar功能 */
	public static boolean EOF_flag = false;

	/* 错误追踪标志,如果该标志为TRUE,防止错误产生时进一步传递错误  */
	public static boolean Error = false;
	
	/* 定义保留字数量常量MAXRESERVED为11 */
	public static final int MAXRESERVED = 11;

	/************************ 保留字查找表 ***************************/
	class reservedWord
	{
		public String  str;
	 	public LexType tok;
	 	public reservedWord(String s,LexType t){str = s;tok = t;}
	}
	public static reservedWord[] reservedWords = new reservedWord[MAXRESERVED];
	{
		reservedWords[0] = new reservedWord("typedef",LexType.TYPEDEF);
		reservedWords[1] = new reservedWord("struct",LexType.STRUCT);
		reservedWords[2] = new reservedWord("if",LexType.IF);
		reservedWords[3] = new reservedWord("else",LexType.ELSE);
		reservedWords[4] = new reservedWord("while",LexType.WHILE);
		reservedWords[5] = new reservedWord("for",LexType.FOR);
		reservedWords[6] = new reservedWord("cin",LexType.CIN);
		reservedWords[7] = new reservedWord("cout",LexType.COUT);
		reservedWords[8] = new reservedWord("return",LexType.RETURN);
		reservedWords[9] = new reservedWord("int",LexType.INT);
		reservedWords[10]= new reservedWord("char",LexType.CHAR);
	}
	
	/************************************************************
		函数	reservedLookup								      
		功能	保留字查找函数									  
		说明	使用线性查找,查看一个标识符是否是保留字			  
			标识符如果在保留字表中则返回相应单词,否则返回单词ID 
	/***********************************************************/
	public LexType reservedLookup (String s)
	{
	  /* 在保留字表中查找,MAXRESERVED已经定义为8,为保留字数 */
		for (int i=0;i<MAXRESERVED;i++)
			/* 线性查保留字表,察看函数参数s指定标识符是否在表中 *
			 * 当两字符串匹配的时候,函数strcmp返回值为0(FALSE)	*/
			if (reservedWords[i].str.equals(s))
				/* 字符串s与保留字表中某一表项匹配,函数返回对应保留字单词 */
				return reservedWords[i].tok;
	  
	  /* 字符串s未在保留字表中找到,函数返回标识符单词ID */
	  return LexType.ID;
	}
	
	/*************************************************************
	  						递归下降法语法分析部分
	*************************************************************/

	/**************** 语法树节点结构   ***************/
	/* 语法树根节点RootK,声明类型节点DecK,标志子结点都是类型声明的结点TypeK,
	 * 标志子结点都是变量声明的结点VarK,函数声明结点FuncDecK,
	 * 语句序列节点StmLK,语句声明结点StmtK,表达式结点ExpK*/
	public enum NodeKind{RootK,DecK,TypeK,FuncDecK,StmLK,StmtK,ExpK};

	/*声明类型Deckind 类型的枚举定义：
	  数组类型ArrayK,字符类型CharK,
	  整数类型IntegerK,记录类型RecordK,
	  以类型标识符作为类型的IdK*/ 
	public enum DecKind{IntK,CharK,ArrayK,StructK,IdK};

	/* 变量类型VarKind类型的枚举定义:           *
	 * 标识符IdV,数组成员ArrayMembV,域成员FieldMembV*/
	public enum VarKind{IdV,ArrayMembV,FieldMembV}; 

	/* 语句类型StmtKind类型的枚举定义:			*
	 * 判断类型IfK,循环类型WhileK				*
	 * 赋值类型AssignK,读类型ReadK           *
	 * 写类型WriteK，函数调用类型CallK          */
	public enum StmtKind{IfK,WhileK,ForK,AssignK,CinK,CoutK,CallK,ReturnK};

	/* 表达式类型ExpKind类型的枚举定义:         *
	 * 操作类型OpK,常数类型ConstK,变量类型VarK */
	public enum ExpKind{OpK,ConstK,VariK};

	/* 类型检查ExpType类型的枚举定义:           *
	 * 空Void,整数类型Integer,字符类型Char    */ 
	public enum ExpType{Void,Int,Char};
	
	public class treeNode {
		public treeNode child[] = new treeNode[4];	/* 子节点指针	*/
		public treeNode sibling;	/* 兄弟节点指针	*/
		public int lineno;			/* 源代码行号	*/
		public NodeKind nodeKind;	/* 节点类型		*/
		
		public class Kind
		{
			public DecKind dec;
			public StmtKind stmt;
			public ExpKind exp;
		}
		public Kind kind = new Kind();  /* 具体类型 	*/
		
		public int idnum;         		/* 相同类型的变量个数 */ 
		public String name[] = new String[10];		 /* 标识符的名称  */
		public SymbTable table[] = new SymbTable[10];/* 与标志符对应的符号表地址，在语义分析阶段填入*/

		public class Attr
		{
			public class ArrayAttr
			{
				public int size;			 /* 数组大小     */
				public DecKind childType;/* 数组的子类型 */
			}
			public ArrayAttr arrayAttr = new ArrayAttr(); /* 数组属性     */

			public class FuncAttr
			{	
				public DecKind returnType;     /* 过程的返回类型*/
			}
			public FuncAttr funcAttr = new FuncAttr();    /* 过程属性      */ 
				
			public class ExpAttr
			{
				public LexType op; 		 	/* 表达式的操作符	*/
				public int int_val; 		/* 表达式的值	*/
				public char char_val; 		/* 表达式的值	*/
				public VarKind varKind;  	/* 变量的类别    	*/
				public ExpType type; 	 	/* 用于类型检查  	*/
			}
			public ExpAttr expAttr = new ExpAttr();		  /* 表达式属性    */
			
			public String type_name; /* 类型名是标识符  */
		}
		public Attr attr=new Attr(); /* 属性	     */
	}

	/* 变量indentno在函数printTree中用于存储当前子树缩进格数,初始为0		*/
	public static int indentno = 0;
	
	/********************************************************/
	/* 函数名 printSpaces										*/
	/* 功  能 空格打印函数											*/
	/* 说  明 该函数打印指定数量空格,用于表示子树缩进						*/
	/********************************************************/
	public void printSpaces()
	{
	  /* 按给定缩进量indentno打印空格进行缩进	*
	   * 其中缩进量indentno总能保持非负		*/
		for (int i=0;i<indentno;i++)
			pw.print(" ");
	}
	
	/********************************************************/
	/* 函数名 printTab                                     	*/
	/* 功  能 打印空格                                        								*/
	/* 说  明 在输出文件中打印个数为参数tabnum的空格          					*/
	/********************************************************/
	public void printTab(int tabnum)
	{
		for(int i=0;i<tabnum;i++)
			pw.print(" ");
	}

	/********************************************************/
	/* 函数名 printTree                             		    */
	/* 功  能 把语法树输出，显示在listing文件中         						*/
	/* 说  明 该函数运用了宏来定义增量减量的缩进          						*/
	/********************************************************/
	public void printTree(treeNode tree)
	{	
		/* 增量缩进宏,每次进入语法树节点都进行增量缩进 */
		indentno += 4;
		
		/* 函数参数给定语法树节点指针tree非null(空) */
		while (tree != null)
		{		
			/*打印行号*/
			if(tree.lineno==0)
				printTab(9);
			else
			    switch(tree.lineno / 10)
				{
					case 0:
						pw.print("line:");
						pw.print(tree.lineno);
						printTab(3);
						break;
					case 1:
					case 2:
					case 3:
					case 4:
					case 5:
					case 6:
					case 7:
					case 8:
					case 9:
						pw.print("line:");
						pw.print(tree.lineno);
						printTab(2);
						break;
					default:
						pw.print("line:");
						pw.print(tree.lineno);
						printTab(1);
				}

		    /* 调用函数printSpaces,打印相应的空格,进行缩进 */ 
		    printSpaces();
		    
		    switch (tree.nodeKind)
		    {
		    case RootK :
				pw.print("RootK  ");
		    	break;
			case DecK:
				pw.print("DecK  ");
				if(tree.kind.dec != null)
				switch(tree.kind.dec)
				{
				case  ArrayK:
					pw.print("ArrayK  ");
					pw.print(tree.attr.arrayAttr.size+"  ");
					if (tree.attr.arrayAttr.childType == DecKind.CharK)
						pw.print("CharK  ");
					else if( tree.attr.arrayAttr.childType == DecKind.IntK)
						pw.print("IntK  ");
					break;
				case  CharK:
					pw.print("CharK  ");
					break;
				case  IntK:
					pw.print("IntK  ");
					break;
				case  StructK:
					pw.print("StructK  ");
					break;
				case  IdK:
					pw.print("IdK  ");
					pw.print(tree.attr.type_name+"  ");
					break;
				default: 
					pw.print("error0!");
					Global.Error = true;
				};
				if (tree.idnum !=0)
					for (int i=0 ; i < (tree.idnum);i++)
					{
						pw.print(tree.name[i]+"  ");
					}
				else  
				{
					pw.print("wrong!no var!\n");
					Global.Error = true;	
				}
				break;
			case TypeK:
				pw.print("TypeK  ");
				switch(tree.kind.dec)
				{
				case  ArrayK:
					pw.print("ArrayK  ");
					pw.print(tree.attr.arrayAttr.size+"  ");
					if (tree.attr.arrayAttr.childType == DecKind.CharK)
						pw.print("CharK  ");
					else if( tree.attr.arrayAttr.childType == DecKind.IntK)
						pw.print("IntK  ");
					break;
				case  CharK:
					pw.print("CharK  ");
					break;
				case  IntK:
					pw.print("IntK  ");
					break;
				case  StructK:
					pw.print("StructK  ");
					break;
				case  IdK:
					pw.print("IdK  ");
					pw.print(tree.attr.type_name+"  ");
					break;
				default: 
					pw.print("error1!");
					Global.Error = true;
				};
				if (tree.idnum !=0)
					for (int i=0 ; i < (tree.idnum);i++)
					{
						pw.print(tree.name[i]+"  ");
					}
				else  
				{
					pw.print("wrong!no typename!\n");
					Global.Error = true;	
				}
				break;
			case FuncDecK: 
				pw.print("FuncDecK  ");
				pw.print(tree.attr.funcAttr.returnType+"  ");
				pw.print(tree.name[0]+"  ");
				if(tree.table[0]!=null)
				{
					pw.print(tree.table[0].attrIR.more.funcAttr.mOff+"  ");
					pw.print(tree.table[0].attrIR.more.funcAttr.nOff+"  ");
					pw.print(tree.table[0].attrIR.more.funcAttr.level+"  ");
				}
				break;
			case StmLK:
				pw.print("StmLk  ");
				break;
			case StmtK:
				pw.print("StmtK  ");
				switch (tree.kind.stmt)
				{
				case IfK:
					pw.print("If  ");
					break;
				case WhileK:
					pw.print("While  ");
					break;
				case ForK:
					pw.print("For  ");
					break;
				case AssignK:
					pw.print("Assign  ");
					break;
				case CinK:
					pw.print("Cin  ");
					pw.print(tree.name[0]+"  ");
					if(tree.table[0]!=null)
					{
						pw.print(tree.table[0].attrIR.more.varAttr.off+"  ");
						pw.print(tree.table[0].attrIR.more.varAttr.level+"  ");
					}
					break;
				case CoutK:
					pw.print("Cout  ");
					break;
				case CallK:
					pw.print("Call  ");
					pw.print(tree.name[0]+"  ");
					break;
				case ReturnK:
					pw.print("Return  ");
					break;
				default: 
					pw.print("error2!");
					Global.Error = true;
				}
				break;
			case ExpK: 
				pw.print("ExpK  ");
				switch (tree.kind.exp)
				{
				case OpK:
					pw.print("Op  ");
					switch(tree.attr.expAttr.op)
					{
					case LT:   
						pw.print("<  "); 
						break;      
					case GT:   
						pw.print(">  "); 
						break;      
					case LE:   
						pw.print("<=  "); 
						break;      
					case GE:   
						pw.print(">=  "); 
						break;      
					case EQ:
						pw.print("==  "); 
						break;
					case NEQ:
						pw.print("<>  "); 
						break;
					case PLUS: 
						pw.print("+  "); 
						break;   
					case MINUS:
						pw.print("-  "); 
						break;
					case TIMES:
						pw.print("*  "); 
						break;  
					case OVER:
						pw.print("/  "); 
						break;  
					default: 
						pw.print("error3!");
						Global.Error = true;
					}
					
					if(tree.attr.expAttr.varKind==VarKind.ArrayMembV)
					{
						pw.print("ArrayMember  ");
						pw.print(tree.name[0]+"  ");
					}
					break;
				case ConstK:
					pw.print("Const  ");
					switch(tree.attr.expAttr.varKind)
					{
					case IdV:
						pw.print("Id  ");
						switch (tree.attr.expAttr.type) {
						case Int:
							pw.print("IntC  ");
							break;
						case Char:
							pw.print("CharC  ");
							break;
						default:
							pw.print("undefined  ");
							break;
						}
						if(tree.name[0]!="")
							pw.print(tree.name[0]+"  ");
						break;
					case FieldMembV:
						pw.print("FieldMember  ");
						if(tree.name[0]!="")
							pw.print(tree.name[0]+"  ");
						break;
					case ArrayMembV:
						pw.print("ArrayMember  ");
						if(tree.name[0]!="")
							pw.print(tree.name[0]+"  ");
						break;
					default: 
						pw.print("var type error!");
						Global.Error = true;
					}
					if(tree.attr.expAttr.type == ExpType.Int)		
						pw.print(tree.attr.expAttr.int_val+"  ");
					else if(tree.attr.expAttr.type == ExpType.Char)		
						pw.print(tree.attr.expAttr.char_val+"  ");
					break;
				case VariK:
					pw.print("Vari  ");
					switch(tree.attr.expAttr.varKind)
					{
					case IdV:
						pw.print("Id  ");
						pw.print(tree.name[0]+"  ");
						break;
					case FieldMembV:
						pw.print("FieldMember  ");
						pw.print(tree.name[0]+"  ");
						break;
					case ArrayMembV:
						pw.print("ArrayMember  ");
						pw.print(tree.name[0]);
						break;
					default: 
						pw.print("var type error!");
						Global.Error = true;
					}
					if(tree.table[0]!=null)
					{
						pw.print(tree.table[0].attrIR.more.varAttr.off+"  ");
						pw.print(tree.table[0].attrIR.more.varAttr.level+"  ");
					}
					break;
				default: 
					pw.print("error4!");
					Global.Error = true;
				}
				break;
			default: 
				pw.print("error5!");
				  Global.Error = true;
		    }
		   
		    pw.println();
		    pw.flush();
			/* 对语法树结点tree的各子结点递归调用printTree过程 *
			 * 缩进写入列表文件listing						   */
			for (int i=0;i<tree.child.length;i++)
		        printTree(tree.child[i]);
		
			/* 对语法树结点tree的兄弟节点递归调用printTree过程 *
			 * 缩进写入列表文件listing						   */ 
			tree = tree.sibling;			
		}

		/* 减量缩进宏,每次退出语法树节点时减量缩进 */
		indentno -= 4;							
	}
	
	/********************************************************/
	/*				以下是创建语法树所用的各类节点的申请 				*/
	/********************************************************/
	
	/********************************************************/
	/* 函数名 newRootNode										*/
	/* 功  能 创建语法树根节点函数			        				*/
	/* 说  明 该函数为语法树创建一个新的根结点      							*/
	/*        并将语法树节点成员初始化								*/
	/********************************************************/
	public treeNode newRootNode()
	{
		  /* 在内存中动态申请分配单元，返回指向该单元的语法树结点类型指针t */
		  treeNode t = new treeNode();
		    
		  /* 指定新语法树节点t成员:结点类型nodekind为语句类型ProK */
		  t.nodeKind = NodeKind.RootK;
		    
		  /* 指定新语法树节点t成员:源代码行号lineno为全局变量lineno */
		  t.lineno = lineno;
		
		  for(int i=0;i<t.name.length;i++) 
			  t.name[i] = "";
		  
		  /* 函数返回语法树根节点指针t */
		  return t;
	}
	
	/********************************************************/
	/* 函数名 newDecANode										*/	
	/* 功  能 创建声明语法树节点函数,没有指明具体的节点声明					*/
	/*        类型,在语法树的第二层			  			        */
	/* 说  明 该函数为语法树创建一个新的结点      	     					*/
	/*        并将语法树节点成员初始化								*/
	/********************************************************/
	public treeNode newDecANode(NodeKind kind)
	{
		/* 在内存中动态申请分配单元，返回指向该单元的语法树结点类型指针t */
		treeNode t = new treeNode();
		
		/* 指定新语法树节点t成员:结点类型nodekind为参数kind */
		t.nodeKind = kind;
		
		/* 指定新语法树节点t成员:源代码行号lineno为全局变量lineno */
		t.lineno = lineno;

		for(int i=0;i<t.name.length;i++) 
			t.name[i] = "";
		
		/* 函数返回语法树根节点指针t */
		return t;
	}

	/********************************************************/
	/* 函数名 newTypeNode										*/
	/* 功  能 类型标志语法树节点创建函数								*/
	/* 说  明 该函数为语法树创建一个新的类型标志结点，						*/
	/*        表示在它之下的声明都为类型声明，        						*/
	/*        并将语法树节点的成员初始化								*/
	/********************************************************/
	public treeNode newTypeNode()
	{ 
		/* 在内存中动态申请分配单元，返回指向该单元的语法树结点类型指针t */
		treeNode t = new treeNode();

		/* 指定新语法树节点t成员: 结点类型nodekind为表达式类型ExpK */
		t.nodeKind = NodeKind.TypeK;
	
		/* 指定新语法树节点t成员: 源代码行号lineno为全局变量lineno */
	    t.lineno = lineno;

		/*初始化符号表地址指针*/
		for(int i=0;i<t.name.length;i++) 
			t.name[i] = "";
		
	  	/* 函数返回表达式类型语法树结点指针t */
	  	return t;
	}

	/********************************************************/
	/* 函数名 newDecNode										*/	
	/* 功  能 创建声明类型语法树节点函数								*/
	/* 说  明 该函数为语法树创建一个新的声明类型结点						*/
	/*        并将语法树节点成员初始化								*/
	/********************************************************/
	public treeNode newDecNode()
	{
		/* 在内存中动态申请分配单元，返回指向该单元的语法树结点类型指针t */
		treeNode t = new treeNode();

		/* 指定新语法树节点t成员:结点类型nodekind为语句类型DecK*/
		t.nodeKind = NodeKind.DecK;
	    
		/* 指定新语法树节点t成员:源代码行号lineno为全局变量lineno */
		t.lineno = lineno;

		for(int i=0;i<t.name.length;i++) 
			t.name[i] = "";
	
		/* 函数返回声明类型语法树节点指针t */
		return t;
	}
	
	/********************************************************/
	/* 函数名 newFuncNode										*/	
	/* 功  能 创建函数类型语法树节点函数								*/
	/* 说  明 该函数为语法树创建一个新的函数类型结点						*/
	/*        并将语法树节点成员初始化								*/
	/********************************************************/
	public treeNode newFuncNode()
	{
		/* 在内存中动态申请分配单元，返回指向该单元的语法树结点类型指针t */
		treeNode t = new treeNode();

		/* 指定新语法树节点t成员:结点类型nodekind为语句类型ProcDecK */
		t.nodeKind = NodeKind.FuncDecK;
	
		/* 指定新语法树节点t成员:源代码行号lineno为全局变量lineno */
		t.lineno = lineno;

		for(int i=0;i<t.name.length;i++) 
			t.name[i] = "";

		/* 函数返回语句类型语法树节点指针t */
		return t;
	}
	
	/********************************************************/
	/* 函数名 newStmlNode										*/	
	/* 功  能 创建语句标志类型语法树节点函数								*/
	/* 说  明 该函数为语法树创建一个新的语句标志类型结点						*/	
	/*        并将语法树节点成员初始化								*/
	/********************************************************/
	public treeNode newStmlNode()
	{
		/* 在内存中动态申请分配单元，返回指向该单元的语法树结点类型指针t */
		treeNode t = new treeNode();

		/* 指定新语法树节点t成员:结点类型nodekind为语句类型StmLK */
		t.nodeKind = NodeKind.StmLK;
	
	    /* 指定新语法树节点t成员:源代码行号lineno为全局变量lineno */
		t.lineno = lineno;

		for(int i=0;i<10;i++)
			t.name[i] = "";

		/*函数返回语句类型语法树节点指针t*/ 
		return t;
	}
	
	/********************************************************/
	/* 函数名 newStmtNode										*/
	/* 功  能 创建语句类型语法树节点函数								*/
	/* 说  明 该函数为语法树创建一个新的语句类型结点						*/
	/*        并将语法树节点成员初始化								*/
	/********************************************************/
	public treeNode newStmtNode(StmtKind kind)
	{
		/* 在内存中动态申请分配单元，返回指向该单元的语法树结点类型指针t */
		treeNode t = new treeNode();
	    
		/* 指定新语法树节点t成员:结点类型nodekind为语句类型StmtK */
		t.nodeKind = NodeKind.StmtK;
	
		/* 指定新语法树节点t成员:语句类型kind.stmt为函数给定参数kind */
	    t.kind.stmt = kind;
	    
		/* 指定新语法树节点t成员:源代码行号lineno为全局变量lineno */
		t.lineno = lineno;

		for(int i=0;i<10;i++)
			t.name[i] = "";

		/* 函数返回语句类型语法树节点指针t */
		return t;
	}
	
	/********************************************************/
	/* 函数名 newExpNode										*/
	/* 功  能 表达式类型语法树节点创建函数								*/
	/* 说  明 该函数为语法树创建一个新的表达式类型结点						*/
	/*        并将语法树节点的成员初始化								*/
	/********************************************************/
	public treeNode newExpNode(ExpKind kind)
	{ 
		/* 内存中动态申请分配单元，返回指向该单元的语法树节点类型指针t */
		treeNode t = new treeNode();

		/* 指定新语法树节点t成员: 结点类型nodekind为表达式类型ExpK */
		t.nodeKind = NodeKind.ExpK;
	
		/* 指定新语法树节点t成员: 表达式类型kind.exp为函数给定参数kind */
	  	t.kind.exp = kind;
	
		/* 指定新语法树节点t成员: 源代码行号lineno为全局变量lineno */
	    t.lineno = lineno;
	
		/* 指定新语法树节点t成员: 表达式为变量类型时的变量类型varkind *
		/* 为IdV.*/
		t.attr.expAttr.varKind = VarKind.IdV;
	
		/* 指定新语法树节点t成员: 类型检查类型type为Void */
	 	t.attr.expAttr.type = ExpType.Void;
	
	    for(int i=0;i<10;i++)
			t.name[i] = "";
	
	    /* 函数返回表达式类型语法树结点指针t */
	    return t;
	}
	
	/*************************************************************
	  						LL(1)语法分析部分
	*************************************************************/

	/*所有非终极符，其各自含义可参考LL1文法*/
	public enum NLexType
	{
	  Program,	      Decpart,	      	DecpartMore,	VFDecpart,
	  TypeDecpart,    TypeDef,			BaseType,	    ArrayMore,
	  StructType,     FieldDecList,   	FieldDecMore,	IdList,
	  IdMore,		  StructName,	  	VarDec,	      	VarIdList,
	  VarIdMore,	  FuncDec,			ParamList,		ParamDecList,
	  ParamMore,      Param,		    FormName,		StmList,
	  StmMore,        Stm,				StmDecpart,		IdDecpart,
	  AssCall,		  AssignmentRest,	RightPart,		ConditionalStm,
	  ElseStm,		  LoopStm,		  	InputStm,		Invar,
	  OutputStm,	  ReturnStm,	  	CallStmRest,	ActParamList,
	  ActParamMore,	  RelExp,		 	OtherRelE,		Exp,
	  OtherTerm,	  Term,          	OtherFactor,	Factor,
	  Variable,		  VIdMore,		 	VariMore,		FieldVar,
	  FieldVarMore,	  CmpOp,		  	AddOp,        	MultOp,
	  VFName,		  Name1,			Name2,			Name3
	};

	/*非终极符的总数*/
	public static final int NLEXNUM = 60;

	/*终极符的总数*/
	public static final int LEXNUM = 42;

	/*LL1分析表*/
	public static int LL1Table[][] = new int[NLEXNUM][];

	/********************************************************/
	/* 函数名  CreatLL1Table									*/
	/* 功  能  创建LL1分析表										*/
	/* 说  明  初始数组（表）中的每一项都为0；根据LL1文法   					*/
	/*         给数组赋值（填表）；填好后，若值为0，					*/
	/*         表示无产生式可选，其他，为选中的产生式  					*/
	/********************************************************/
	public void CreatLL1Table()
	{
		/*初始化LL1表元素*/
		for (int i=0;i<NLEXNUM;i++)
		{
			LL1Table[i] = new int[LEXNUM];
			for (int j=0;j<LEXNUM;j++)
				LL1Table[i][j] = -1;	/* -1代表错误 */
		}

		LL1Table[NLexType.Program.ordinal()][LexType.ENDFILE.ordinal()]	 		= 0;
		LL1Table[NLexType.Program.ordinal()][LexType.ID.ordinal()] 				= 0;
		LL1Table[NLexType.Program.ordinal()][LexType.TYPEDEF.ordinal()] 		= 0;
		LL1Table[NLexType.Program.ordinal()][LexType.INT.ordinal()] 			= 0;
		LL1Table[NLexType.Program.ordinal()][LexType.CHAR.ordinal()] 			= 0;
		LL1Table[NLexType.Program.ordinal()][LexType.STRUCT.ordinal()] 			= 0;
		
		LL1Table[NLexType.Decpart.ordinal()][LexType.ENDFILE.ordinal()] 		= 3;
		LL1Table[NLexType.Decpart.ordinal()][LexType.ID.ordinal()] 				= 2;
		LL1Table[NLexType.Decpart.ordinal()][LexType.TYPEDEF.ordinal()] 		= 1;
		LL1Table[NLexType.Decpart.ordinal()][LexType.INT.ordinal()] 			= 2;
		LL1Table[NLexType.Decpart.ordinal()][LexType.CHAR.ordinal()] 			= 2;
		LL1Table[NLexType.Decpart.ordinal()][LexType.STRUCT.ordinal()] 			= 2;

		LL1Table[NLexType.DecpartMore.ordinal()][LexType.ENDFILE.ordinal()] 	= 3;
		LL1Table[NLexType.DecpartMore.ordinal()][LexType.ID.ordinal()] 			= 4;
		LL1Table[NLexType.DecpartMore.ordinal()][LexType.TYPEDEF.ordinal()] 	= 4;
		LL1Table[NLexType.DecpartMore.ordinal()][LexType.INT.ordinal()] 		= 4;
		LL1Table[NLexType.DecpartMore.ordinal()][LexType.CHAR.ordinal()] 		= 4;
		LL1Table[NLexType.DecpartMore.ordinal()][LexType.STRUCT.ordinal()] 		= 4;
		
		LL1Table[NLexType.VFDecpart.ordinal()][LexType.SEMI.ordinal()]			= 5;
		LL1Table[NLexType.VFDecpart.ordinal()][LexType.COMMA.ordinal()] 		= 5;
		LL1Table[NLexType.VFDecpart.ordinal()][LexType.LPAREN.ordinal()]		= 6;
		
		LL1Table[NLexType.TypeDecpart.ordinal()][LexType.TYPEDEF.ordinal()]		= 7;

		LL1Table[NLexType.TypeDef.ordinal()][LexType.ID.ordinal()]				= 10;
		LL1Table[NLexType.TypeDef.ordinal()][LexType.INT.ordinal()]				= 8;
		LL1Table[NLexType.TypeDef.ordinal()][LexType.CHAR.ordinal()]			= 8;
		LL1Table[NLexType.TypeDef.ordinal()][LexType.STRUCT.ordinal()]			= 9;
		
		LL1Table[NLexType.BaseType.ordinal()][LexType.INT.ordinal()]			= 11;
		LL1Table[NLexType.BaseType.ordinal()][LexType.CHAR.ordinal()]			= 12;

		LL1Table[NLexType.ArrayMore.ordinal()][LexType.ID.ordinal()]			= 13;
		LL1Table[NLexType.ArrayMore.ordinal()][LexType.LMIDPAREN.ordinal()]		= 14;
		LL1Table[NLexType.ArrayMore.ordinal()][LexType.SEMI.ordinal()]			= 13;

		LL1Table[NLexType.StructType.ordinal()][LexType.STRUCT.ordinal()] 		= 15;

		LL1Table[NLexType.FieldDecList.ordinal()][LexType.INT.ordinal()]		= 16;
		LL1Table[NLexType.FieldDecList.ordinal()][LexType.CHAR.ordinal()] 		= 16;
		
		LL1Table[NLexType.FieldDecMore.ordinal()][LexType.INT.ordinal()]		= 18;
		LL1Table[NLexType.FieldDecMore.ordinal()][LexType.CHAR.ordinal()] 		= 18;
		LL1Table[NLexType.FieldDecMore.ordinal()][LexType.RBIGPAREN.ordinal()] 	= 17;

		LL1Table[NLexType.IdList.ordinal()][LexType.ID.ordinal()]				= 19;

		LL1Table[NLexType.IdMore.ordinal()][LexType.SEMI.ordinal()]				= 20;
		LL1Table[NLexType.IdMore.ordinal()][LexType.COMMA.ordinal()] 			= 21;

		LL1Table[NLexType.StructName.ordinal()][LexType.ID.ordinal()]			= 23;
		LL1Table[NLexType.StructName.ordinal()][LexType.LBIGPAREN.ordinal()] 	= 22;

		LL1Table[NLexType.VarDec.ordinal()][LexType.SEMI.ordinal()]				= 24;
		LL1Table[NLexType.VarDec.ordinal()][LexType.COMMA.ordinal()] 			= 24;

		LL1Table[NLexType.VarIdList.ordinal()][LexType.ID.ordinal()]			= 25;

		LL1Table[NLexType.VarIdMore.ordinal()][LexType.SEMI.ordinal()]			= 26;
		LL1Table[NLexType.VarIdMore.ordinal()][LexType.COMMA.ordinal()] 		= 27;

		LL1Table[NLexType.FuncDec.ordinal()][LexType.LPAREN.ordinal()]			= 28;

		LL1Table[NLexType.ParamList.ordinal()][LexType.ID.ordinal()] 			= 30;
		LL1Table[NLexType.ParamList.ordinal()][LexType.INT.ordinal()] 			= 30;
		LL1Table[NLexType.ParamList.ordinal()][LexType.CHAR.ordinal()] 			= 30;
		LL1Table[NLexType.ParamList.ordinal()][LexType.STRUCT.ordinal()] 		= 30;
		LL1Table[NLexType.ParamList.ordinal()][LexType.RPAREN.ordinal()] 		= 29;

		LL1Table[NLexType.ParamDecList.ordinal()][LexType.ID.ordinal()] 		= 31;
		LL1Table[NLexType.ParamDecList.ordinal()][LexType.INT.ordinal()] 		= 31;
		LL1Table[NLexType.ParamDecList.ordinal()][LexType.CHAR.ordinal()] 		= 31;
		LL1Table[NLexType.ParamDecList.ordinal()][LexType.STRUCT.ordinal()] 	= 31;

		LL1Table[NLexType.ParamMore.ordinal()][LexType.RPAREN.ordinal()] 		= 32;
		LL1Table[NLexType.ParamMore.ordinal()][LexType.COMMA.ordinal()] 		= 33;

		LL1Table[NLexType.Param.ordinal()][LexType.ID.ordinal()] 				= 34;
		LL1Table[NLexType.Param.ordinal()][LexType.INT.ordinal()] 				= 34;
		LL1Table[NLexType.Param.ordinal()][LexType.CHAR.ordinal()] 				= 34;
		LL1Table[NLexType.Param.ordinal()][LexType.STRUCT.ordinal()] 			= 34;

		LL1Table[NLexType.FormName.ordinal()][LexType.ID.ordinal()] 			= 35;

		LL1Table[NLexType.StmList.ordinal()][LexType.ID.ordinal()] 				= 36;
		LL1Table[NLexType.StmList.ordinal()][LexType.TYPEDEF.ordinal()] 		= 36;
		LL1Table[NLexType.StmList.ordinal()][LexType.INT.ordinal()] 			= 36;
		LL1Table[NLexType.StmList.ordinal()][LexType.CHAR.ordinal()] 			= 36;
		LL1Table[NLexType.StmList.ordinal()][LexType.STRUCT.ordinal()] 			= 36;
		LL1Table[NLexType.StmList.ordinal()][LexType.LBIGPAREN.ordinal()] 		= 36;
		LL1Table[NLexType.StmList.ordinal()][LexType.IF.ordinal()] 				= 36;
		LL1Table[NLexType.StmList.ordinal()][LexType.WHILE.ordinal()] 			= 36;
		LL1Table[NLexType.StmList.ordinal()][LexType.FOR.ordinal()] 			= 36;
		LL1Table[NLexType.StmList.ordinal()][LexType.CIN.ordinal()] 			= 36;
		LL1Table[NLexType.StmList.ordinal()][LexType.COUT.ordinal()] 			= 36;
		LL1Table[NLexType.StmList.ordinal()][LexType.RETURN.ordinal()] 			= 36;

		LL1Table[NLexType.StmMore.ordinal()][LexType.ID.ordinal()] 				= 38;
		LL1Table[NLexType.StmMore.ordinal()][LexType.TYPEDEF.ordinal()] 		= 38;
		LL1Table[NLexType.StmMore.ordinal()][LexType.INT.ordinal()] 			= 38;
		LL1Table[NLexType.StmMore.ordinal()][LexType.CHAR.ordinal()] 			= 38;
		LL1Table[NLexType.StmMore.ordinal()][LexType.STRUCT.ordinal()] 			= 38;
		LL1Table[NLexType.StmMore.ordinal()][LexType.LBIGPAREN.ordinal()] 		= 38;
		LL1Table[NLexType.StmMore.ordinal()][LexType.RBIGPAREN.ordinal()] 		= 37;
		LL1Table[NLexType.StmMore.ordinal()][LexType.IF.ordinal()] 				= 38;
		LL1Table[NLexType.StmMore.ordinal()][LexType.WHILE.ordinal()] 			= 38;
		LL1Table[NLexType.StmMore.ordinal()][LexType.FOR.ordinal()] 			= 38;
		LL1Table[NLexType.StmMore.ordinal()][LexType.CIN.ordinal()] 			= 38;
		LL1Table[NLexType.StmMore.ordinal()][LexType.COUT.ordinal()] 			= 38;
		LL1Table[NLexType.StmMore.ordinal()][LexType.RETURN.ordinal()] 			= 38;

		LL1Table[NLexType.Stm.ordinal()][LexType.ID.ordinal()] 					= 39;
		LL1Table[NLexType.Stm.ordinal()][LexType.TYPEDEF.ordinal()] 			= 39;
		LL1Table[NLexType.Stm.ordinal()][LexType.INT.ordinal()] 				= 39;
		LL1Table[NLexType.Stm.ordinal()][LexType.CHAR.ordinal()] 				= 39;
		LL1Table[NLexType.Stm.ordinal()][LexType.STRUCT.ordinal()] 				= 39;
		LL1Table[NLexType.Stm.ordinal()][LexType.LBIGPAREN.ordinal()] 			= 45;
		LL1Table[NLexType.Stm.ordinal()][LexType.IF.ordinal()] 					= 40;
		LL1Table[NLexType.Stm.ordinal()][LexType.WHILE.ordinal()] 				= 41;
		LL1Table[NLexType.Stm.ordinal()][LexType.FOR.ordinal()] 				= 41;
		LL1Table[NLexType.Stm.ordinal()][LexType.CIN.ordinal()] 				= 42;
		LL1Table[NLexType.Stm.ordinal()][LexType.COUT.ordinal()] 				= 43;
		LL1Table[NLexType.Stm.ordinal()][LexType.RETURN.ordinal()] 				= 44;

		LL1Table[NLexType.StmDecpart.ordinal()][LexType.ID.ordinal()] 			= 49;
		LL1Table[NLexType.StmDecpart.ordinal()][LexType.TYPEDEF.ordinal()] 		= 46;
		LL1Table[NLexType.StmDecpart.ordinal()][LexType.INT.ordinal()] 			= 47;
		LL1Table[NLexType.StmDecpart.ordinal()][LexType.CHAR.ordinal()] 		= 47;
		LL1Table[NLexType.StmDecpart.ordinal()][LexType.STRUCT.ordinal()] 		= 48;

		LL1Table[NLexType.IdDecpart.ordinal()][LexType.ID.ordinal()] 			= 51;
		LL1Table[NLexType.IdDecpart.ordinal()][LexType.LMIDPAREN.ordinal()] 	= 50;
		LL1Table[NLexType.IdDecpart.ordinal()][LexType.LPAREN.ordinal()] 		= 50;
		LL1Table[NLexType.IdDecpart.ordinal()][LexType.ASSIGN.ordinal()] 		= 50;
		LL1Table[NLexType.IdDecpart.ordinal()][LexType.DOT.ordinal()] 			= 50;

		LL1Table[NLexType.AssCall.ordinal()][LexType.LMIDPAREN.ordinal()] 		= 52;
		LL1Table[NLexType.AssCall.ordinal()][LexType.LPAREN.ordinal()] 			= 53;
		LL1Table[NLexType.AssCall.ordinal()][LexType.ASSIGN.ordinal()] 			= 52;
		LL1Table[NLexType.AssCall.ordinal()][LexType.DOT.ordinal()] 			= 52;

		LL1Table[NLexType.AssignmentRest.ordinal()][LexType.LMIDPAREN.ordinal()]= 54;
		LL1Table[NLexType.AssignmentRest.ordinal()][LexType.ASSIGN.ordinal()] 	= 54;
		LL1Table[NLexType.AssignmentRest.ordinal()][LexType.DOT.ordinal()] 		= 54;

		LL1Table[NLexType.RightPart.ordinal()][LexType.ID.ordinal()] 			= 55;
		LL1Table[NLexType.RightPart.ordinal()][LexType.INTC.ordinal()] 			= 55;
		LL1Table[NLexType.RightPart.ordinal()][LexType.LPAREN.ordinal()] 		= 55;
		LL1Table[NLexType.RightPart.ordinal()][LexType.CHARC.ordinal()] 		= 56;

		LL1Table[NLexType.ConditionalStm.ordinal()][LexType.IF.ordinal()] 		= 57;

		LL1Table[NLexType.ElseStm.ordinal()][LexType.ID.ordinal()] 				= 99;
		LL1Table[NLexType.ElseStm.ordinal()][LexType.TYPEDEF.ordinal()] 		= 99;
		LL1Table[NLexType.ElseStm.ordinal()][LexType.INT.ordinal()] 			= 99;
		LL1Table[NLexType.ElseStm.ordinal()][LexType.CHAR.ordinal()] 			= 99;
		LL1Table[NLexType.ElseStm.ordinal()][LexType.STRUCT.ordinal()] 			= 99;
		LL1Table[NLexType.ElseStm.ordinal()][LexType.RBIGPAREN.ordinal()] 		= 99;
		LL1Table[NLexType.ElseStm.ordinal()][LexType.IF.ordinal()] 				= 99;
		LL1Table[NLexType.ElseStm.ordinal()][LexType.WHILE.ordinal()] 			= 99;
		LL1Table[NLexType.ElseStm.ordinal()][LexType.FOR.ordinal()] 			= 99;
		LL1Table[NLexType.ElseStm.ordinal()][LexType.CIN.ordinal()] 			= 99;
		LL1Table[NLexType.ElseStm.ordinal()][LexType.COUT.ordinal()] 			= 99;
		LL1Table[NLexType.ElseStm.ordinal()][LexType.RETURN.ordinal()] 			= 99;
		LL1Table[NLexType.ElseStm.ordinal()][LexType.ELSE.ordinal()] 			= 100;

		LL1Table[NLexType.LoopStm.ordinal()][LexType.WHILE.ordinal()] 			= 58;
		LL1Table[NLexType.LoopStm.ordinal()][LexType.FOR.ordinal()] 			= 59;

		LL1Table[NLexType.InputStm.ordinal()][LexType.CIN.ordinal()] 			= 60;

		LL1Table[NLexType.Invar.ordinal()][LexType.ID.ordinal()] 				= 61;

		LL1Table[NLexType.OutputStm.ordinal()][LexType.COUT.ordinal()] 			= 62;

		LL1Table[NLexType.ReturnStm.ordinal()][LexType.RETURN.ordinal()] 		= 63;

		LL1Table[NLexType.CallStmRest.ordinal()][LexType.LPAREN.ordinal()] 		= 64;

		LL1Table[NLexType.ActParamList.ordinal()][LexType.ID.ordinal()] 		= 66;
		LL1Table[NLexType.ActParamList.ordinal()][LexType.INTC.ordinal()] 		= 66;
		LL1Table[NLexType.ActParamList.ordinal()][LexType.LPAREN.ordinal()] 	= 66;
		LL1Table[NLexType.ActParamList.ordinal()][LexType.RPAREN.ordinal()] 	= 65;

		LL1Table[NLexType.ActParamMore.ordinal()][LexType.COMMA.ordinal()] 		= 68;
		LL1Table[NLexType.ActParamMore.ordinal()][LexType.RPAREN.ordinal()] 	= 67;

		LL1Table[NLexType.RelExp.ordinal()][LexType.ID.ordinal()] 				= 69;
		LL1Table[NLexType.RelExp.ordinal()][LexType.INTC.ordinal()] 			= 69;
		LL1Table[NLexType.RelExp.ordinal()][LexType.LPAREN.ordinal()] 			= 69;

		LL1Table[NLexType.OtherRelE.ordinal()][LexType.LT.ordinal()] 			= 70;
		LL1Table[NLexType.OtherRelE.ordinal()][LexType.GT.ordinal()] 			= 70;
		LL1Table[NLexType.OtherRelE.ordinal()][LexType.LE.ordinal()] 			= 70;
		LL1Table[NLexType.OtherRelE.ordinal()][LexType.GE.ordinal()] 			= 70;
		LL1Table[NLexType.OtherRelE.ordinal()][LexType.EQ.ordinal()] 			= 70;
		LL1Table[NLexType.OtherRelE.ordinal()][LexType.NEQ.ordinal()] 			= 70;

		LL1Table[NLexType.Exp.ordinal()][LexType.ID.ordinal()] 					= 71;
		LL1Table[NLexType.Exp.ordinal()][LexType.INTC.ordinal()] 				= 71;
		LL1Table[NLexType.Exp.ordinal()][LexType.LPAREN.ordinal()] 				= 71;

		LL1Table[NLexType.OtherTerm.ordinal()][LexType.RMIDPAREN.ordinal()] 	= 72;
		LL1Table[NLexType.OtherTerm.ordinal()][LexType.SEMI.ordinal()] 			= 72;
		LL1Table[NLexType.OtherTerm.ordinal()][LexType.COMMA.ordinal()] 		= 72;
		LL1Table[NLexType.OtherTerm.ordinal()][LexType.RPAREN.ordinal()] 		= 72;
		LL1Table[NLexType.OtherTerm.ordinal()][LexType.LT.ordinal()] 			= 72;
		LL1Table[NLexType.OtherTerm.ordinal()][LexType.GT.ordinal()] 			= 72;
		LL1Table[NLexType.OtherTerm.ordinal()][LexType.LE.ordinal()] 			= 72;
		LL1Table[NLexType.OtherTerm.ordinal()][LexType.GE.ordinal()] 			= 72;
		LL1Table[NLexType.OtherTerm.ordinal()][LexType.EQ.ordinal()] 			= 72;
		LL1Table[NLexType.OtherTerm.ordinal()][LexType.NEQ.ordinal()] 			= 72;
		LL1Table[NLexType.OtherTerm.ordinal()][LexType.PLUS.ordinal()] 			= 73;
		LL1Table[NLexType.OtherTerm.ordinal()][LexType.MINUS.ordinal()] 		= 73;

		LL1Table[NLexType.Term.ordinal()][LexType.ID.ordinal()] 				= 74;
		LL1Table[NLexType.Term.ordinal()][LexType.INTC.ordinal()] 				= 74;
		LL1Table[NLexType.Term.ordinal()][LexType.LPAREN.ordinal()] 			= 74;

		LL1Table[NLexType.OtherFactor.ordinal()][LexType.RMIDPAREN.ordinal()] 	= 75;
		LL1Table[NLexType.OtherFactor.ordinal()][LexType.SEMI.ordinal()] 		= 75;
		LL1Table[NLexType.OtherFactor.ordinal()][LexType.COMMA.ordinal()] 		= 75;
		LL1Table[NLexType.OtherFactor.ordinal()][LexType.RPAREN.ordinal()] 		= 75;
		LL1Table[NLexType.OtherFactor.ordinal()][LexType.LT.ordinal()] 			= 75;
		LL1Table[NLexType.OtherFactor.ordinal()][LexType.GT.ordinal()] 			= 75;
		LL1Table[NLexType.OtherFactor.ordinal()][LexType.LE.ordinal()] 			= 75;
		LL1Table[NLexType.OtherFactor.ordinal()][LexType.GE.ordinal()] 			= 75;
		LL1Table[NLexType.OtherFactor.ordinal()][LexType.EQ.ordinal()] 			= 75;
		LL1Table[NLexType.OtherFactor.ordinal()][LexType.NEQ.ordinal()] 		= 75;
		LL1Table[NLexType.OtherFactor.ordinal()][LexType.PLUS.ordinal()] 		= 75;
		LL1Table[NLexType.OtherFactor.ordinal()][LexType.MINUS.ordinal()] 		= 75;
		LL1Table[NLexType.OtherFactor.ordinal()][LexType.TIMES.ordinal()] 		= 76;
		LL1Table[NLexType.OtherFactor.ordinal()][LexType.OVER.ordinal()] 		= 76;

		LL1Table[NLexType.Factor.ordinal()][LexType.ID.ordinal()] 				= 79;
		LL1Table[NLexType.Factor.ordinal()][LexType.INTC.ordinal()] 			= 78;
		LL1Table[NLexType.Factor.ordinal()][LexType.LPAREN.ordinal()] 			= 77;

		LL1Table[NLexType.Variable.ordinal()][LexType.ID.ordinal()] 			= 80;

		LL1Table[NLexType.VIdMore.ordinal()][LexType.LMIDPAREN.ordinal()] 		= 81;
		LL1Table[NLexType.VIdMore.ordinal()][LexType.RMIDPAREN.ordinal()] 		= 81;
		LL1Table[NLexType.VIdMore.ordinal()][LexType.SEMI.ordinal()] 			= 81;
		LL1Table[NLexType.VIdMore.ordinal()][LexType.COMMA.ordinal()] 			= 81;
		LL1Table[NLexType.VIdMore.ordinal()][LexType.LPAREN.ordinal()] 			= 82;
		LL1Table[NLexType.VIdMore.ordinal()][LexType.RPAREN.ordinal()] 			= 81;
		LL1Table[NLexType.VIdMore.ordinal()][LexType.DOT.ordinal()] 			= 81;
		LL1Table[NLexType.VIdMore.ordinal()][LexType.LT.ordinal()] 				= 81;
		LL1Table[NLexType.VIdMore.ordinal()][LexType.GT.ordinal()] 				= 81;
		LL1Table[NLexType.VIdMore.ordinal()][LexType.LE.ordinal()] 				= 81;
		LL1Table[NLexType.VIdMore.ordinal()][LexType.GE.ordinal()] 				= 81;
		LL1Table[NLexType.VIdMore.ordinal()][LexType.EQ.ordinal()] 				= 81;
		LL1Table[NLexType.VIdMore.ordinal()][LexType.NEQ.ordinal()] 			= 81;
		LL1Table[NLexType.VIdMore.ordinal()][LexType.PLUS.ordinal()] 			= 81;
		LL1Table[NLexType.VIdMore.ordinal()][LexType.MINUS.ordinal()] 			= 81;
		LL1Table[NLexType.VIdMore.ordinal()][LexType.TIMES.ordinal()] 			= 81;
		LL1Table[NLexType.VIdMore.ordinal()][LexType.OVER.ordinal()] 			= 81;

		LL1Table[NLexType.VariMore.ordinal()][LexType.LMIDPAREN.ordinal()] 		= 84;
		LL1Table[NLexType.VariMore.ordinal()][LexType.RMIDPAREN.ordinal()] 		= 83;
		LL1Table[NLexType.VariMore.ordinal()][LexType.SEMI.ordinal()] 			= 83;
		LL1Table[NLexType.VariMore.ordinal()][LexType.COMMA.ordinal()] 			= 83;
		LL1Table[NLexType.VariMore.ordinal()][LexType.RPAREN.ordinal()] 		= 83;
		LL1Table[NLexType.VariMore.ordinal()][LexType.ASSIGN.ordinal()] 		= 83;
		LL1Table[NLexType.VariMore.ordinal()][LexType.DOT.ordinal()] 			= 85;
		LL1Table[NLexType.VariMore.ordinal()][LexType.LT.ordinal()] 			= 83;
		LL1Table[NLexType.VariMore.ordinal()][LexType.GT.ordinal()] 			= 83;
		LL1Table[NLexType.VariMore.ordinal()][LexType.LE.ordinal()] 			= 83;
		LL1Table[NLexType.VariMore.ordinal()][LexType.GE.ordinal()] 			= 83;
		LL1Table[NLexType.VariMore.ordinal()][LexType.EQ.ordinal()] 			= 83;
		LL1Table[NLexType.VariMore.ordinal()][LexType.NEQ.ordinal()] 			= 83;
		LL1Table[NLexType.VariMore.ordinal()][LexType.PLUS.ordinal()] 			= 83;
		LL1Table[NLexType.VariMore.ordinal()][LexType.MINUS.ordinal()] 			= 83;
		LL1Table[NLexType.VariMore.ordinal()][LexType.TIMES.ordinal()] 			= 83;
		LL1Table[NLexType.VariMore.ordinal()][LexType.OVER.ordinal()] 			= 83;

		LL1Table[NLexType.FieldVar.ordinal()][LexType.ID.ordinal()] 			= 86;

		LL1Table[NLexType.FieldVarMore.ordinal()][LexType.LMIDPAREN.ordinal()] 	= 88;
		LL1Table[NLexType.FieldVarMore.ordinal()][LexType.RMIDPAREN.ordinal()] 	= 87;
		LL1Table[NLexType.FieldVarMore.ordinal()][LexType.SEMI.ordinal()] 		= 87;
		LL1Table[NLexType.FieldVarMore.ordinal()][LexType.COMMA.ordinal()] 		= 87;
		LL1Table[NLexType.FieldVarMore.ordinal()][LexType.RPAREN.ordinal()] 	= 87;
		LL1Table[NLexType.FieldVarMore.ordinal()][LexType.ASSIGN.ordinal()] 	= 87;
		LL1Table[NLexType.FieldVarMore.ordinal()][LexType.LT.ordinal()] 		= 87;
		LL1Table[NLexType.FieldVarMore.ordinal()][LexType.GT.ordinal()] 		= 87;
		LL1Table[NLexType.FieldVarMore.ordinal()][LexType.LE.ordinal()] 		= 87;
		LL1Table[NLexType.FieldVarMore.ordinal()][LexType.GE.ordinal()] 		= 87;
		LL1Table[NLexType.FieldVarMore.ordinal()][LexType.EQ.ordinal()] 		= 87;
		LL1Table[NLexType.FieldVarMore.ordinal()][LexType.NEQ.ordinal()] 		= 87;
		LL1Table[NLexType.FieldVarMore.ordinal()][LexType.PLUS.ordinal()] 		= 87;
		LL1Table[NLexType.FieldVarMore.ordinal()][LexType.MINUS.ordinal()] 		= 87;
		LL1Table[NLexType.FieldVarMore.ordinal()][LexType.TIMES.ordinal()] 		= 87;
		LL1Table[NLexType.FieldVarMore.ordinal()][LexType.OVER.ordinal()] 		= 87;

		LL1Table[NLexType.CmpOp.ordinal()][LexType.LT.ordinal()] 				= 89;
		LL1Table[NLexType.CmpOp.ordinal()][LexType.GT.ordinal()] 				= 90;
		LL1Table[NLexType.CmpOp.ordinal()][LexType.LE.ordinal()] 				= 91;
		LL1Table[NLexType.CmpOp.ordinal()][LexType.GE.ordinal()] 				= 92;
		LL1Table[NLexType.CmpOp.ordinal()][LexType.EQ.ordinal()] 				= 93;
		LL1Table[NLexType.CmpOp.ordinal()][LexType.NEQ.ordinal()] 				= 94;

		LL1Table[NLexType.AddOp.ordinal()][LexType.PLUS.ordinal()] 				= 95;
		LL1Table[NLexType.AddOp.ordinal()][LexType.MINUS.ordinal()] 			= 96;

		LL1Table[NLexType.MultOp.ordinal()][LexType.TIMES.ordinal()] 			= 97;
		LL1Table[NLexType.MultOp.ordinal()][LexType.OVER.ordinal()] 			= 98;

		LL1Table[NLexType.VFName.ordinal()][LexType.ID.ordinal()] 				= 101;
		
		LL1Table[NLexType.Name1.ordinal()][LexType.ID.ordinal()] 				= 102;

		LL1Table[NLexType.Name2.ordinal()][LexType.ID.ordinal()] 				= 103;
		
		LL1Table[NLexType.Name3.ordinal()][LexType.ID.ordinal()] 				= 104;
	}
}