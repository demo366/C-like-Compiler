/*******************************************************
	文件	analyze.java
	说明	编译器的语义分析器实现
	功能	构造符号表和信息表,采用统一的符号表结构,标记位kind区分不同种类
		进行语义错误检查
*******************************************************/

package WSL;
import java.util.ArrayList;
import BXC.Global.DecKind;
import BXC.Global.NodeKind;
import BXC.Global.StmtKind;
import BXC.Global.VarKind;
import BXC.Global.treeNode;
import BXC.Global;
import BXC.Parse;
import BXC.ParseLL1;

public class Analyze {

	public static void main(String[] args) {
		Analyze myAnalyze = new Analyze();
		myAnalyze.run();
	}

	public static Global global = new Global();

	public static Parse myParse = new Parse();
	public static ParseLL1 myParseLL1 = new ParseLL1();

	public Analyze() {
		/* scope栈的层数 */
		Level = -1;
	}

	/**********************************************
	  			语义分析需要用到的类型及变量定义
	**********************************************/

	/* 标识符的类型 */
	public enum IdKind {
		typeKind, varKind, funcKind
	};

	/* 使用scope栈的局部符号表方法中所用到的scope栈 */
	public ArrayList<ArrayList<SymbTable>> scope = new ArrayList<ArrayList<SymbTable>>();
	
	/* scope栈的层数0或1 */
	public int Level;

	/* 在同层的变量偏移 */
	public int Off;

	/* 记录当前层的displayOff */
	public int savedOff;
	
	/* 语义分析追踪标志,如果该标志为TRUE,将符号表插入和查找写入列表文件listing */
	public boolean TraceTable = true;

	public boolean TraceCode = true;

	/**********************************************
						类型内部表示
	**********************************************/

	/* 类型的枚举定义 */
	public enum TypeKind {
		intTy, charTy, boolTy, arrayTy, structTy
	};

	/* 数组类型结构定义 */
	public class ArrayAttr {
		public TypeIR indexTy; /* 指向数组下标类型的内部表示 */
		public TypeIR elemTy; /* 指向数组元素类型的内部表示 */
		public int size; /* 记录数组类型的大小 */
	}

	/* 结构体类型单元结构定义 */
	public class FieldChain {
		public String id; /* 变量名 */
		public int off; /* 所在记录中的偏移 */
		public TypeIR unitType; /* 域中成员的类型 */
	}

	/* 类型的内部结构定义 */
	public class TypeIR {
		
		public class Attr {
			public ArrayAttr arrayAttr = new ArrayAttr();
			public ArrayList<FieldChain> structBody = new ArrayList<FieldChain>();/*记录类型中的域链*/
		}

		public int size; /* 类型所占空间大小 */
		public TypeKind kind;
		public Attr more = new Attr(); /* 扩展部分,不同类型有不同的属性 */
	}

	/* 形参表的结构定义 */
	public class ParamTable {
		public SymbTable table; /* 指向该形参所在符号表中的地址入口 */
	}

	/* 标识符的属性结构定义 */
	public class AttributeIR {
		public class Attr {
			public class VarAttr {
				public int level; /* 该变量的层数 */
				public int off; /* 该变量的偏移 */
			}

			public class FuncAttr {
				public int level; /* 该过程的层数 */
				public ArrayList<ParamTable> param = new ArrayList<ParamTable>(); /* 参数表 */
				public int mOff; /* 过程活动记录的大小 */
				public int nOff; /* sp到display表的偏移量 */
			}

			public VarAttr varAttr = new VarAttr(); /* 变量标识符的属性 */
			public FuncAttr funcAttr = new FuncAttr(); /* 过程名标识符的属性 */
		}

		public TypeIR idtype; /* 指向标识符的类型内部表示 */
		public IdKind kind; /* 标识符的类型 */
		public Attr more = new Attr(); /* 扩展部分,标识符的不同类型有不同的属性 */
	}
	
	/* 符号表的结构定义 */
	public class SymbTable {
		public String idName = "";
		public AttributeIR attrIR = new AttributeIR();
	}

	/*********************************************************
	 						符号表相关操作
	*********************************************************/

	/********************************************************/
	/* 	函数 	PrintFieldTable 								*/
	/* 	功能	打印纪录类型的域表 									*/
	/* 	说明 													*/
	/********************************************************/
	public void PrintFieldChain(SymbTable table) {
		ArrayList<FieldChain> fieldchain = table.attrIR.idtype.more.structBody;
		global.pw.println();
		global.pw.printf("----------------------- 域表:%s -----------------------",table.idName);
		global.pw.println();
		global.pw.println();
		for (int i=0; i < fieldchain.size(); i++) {
			/* 输出标识符名字 */
			global.pw.printf("%10s:   ", fieldchain.get(i).id);
			/* 输出标识符的类型信息 */
			switch (fieldchain.get(i).unitType.kind) {
			case intTy:
				global.pw.print("intTy     ");
				break;
			case charTy:
				global.pw.print("charTy    ");
				break;
			case arrayTy:
				global.pw.print("arrayTy   ");
				break;
			case structTy:
				global.pw.print("structTy  ");
				break;
			default:
				global.pw.print("error type");
				break;
			}
			global.pw.printf("Off = %d", fieldchain.get(i).off);
			global.pw.println();
		}
		global.pw.flush();
	}

	/********************************************************/
	/* 	函数 	PrintOneLayer									*/
	/* 	功能 	打印符号表的一层 										*/
	/* 	说明 	有符号表打印函数PrintSymbTable调用						*/
	/********************************************************/
	public void PrintOneLayer(int level) {
		ArrayList<SymbTable> t = scope.get(level);
		global.pw.println();
		global.pw.printf("---------------------- 第%d层的符号表 ----------------------",level);
		global.pw.println();
		global.pw.println();

		for (int i=0; i<t.size(); i++) {
			/* 输出标识符名字 */
			global.pw.printf("%10s:   ",t.get(i).idName);
			AttributeIR Attrib = t.get(i).attrIR;
			/* 输出标识符的类型信息，过程标识符除外 */
			if (Attrib.idtype != null) /* 过程标识符 */
				switch (Attrib.idtype.kind) {
				case intTy:		global.pw.print("intTy     ");	break;
				case charTy: 	global.pw.print("charTy    "); break;
				case arrayTy: 	global.pw.print("arrayTy   "); break;
				case structTy:	global.pw.print("structTy  ");	break;
				default:		global.pw.print("error type!  ");
				}
			/* 输出标识符的类别，并根据不同类型输出不同其它属性 */
			switch (Attrib.kind) {
			case typeKind:
				global.pw.print("typekind  ");
				break;
			case varKind:
				global.pw.print("varkind   ");
				global.pw.printf("Level = %d  ", Attrib.more.varAttr.level);
				global.pw.printf("Offset = %d  ", Attrib.more.varAttr.off);
				break;
			case funcKind:
				global.pw.print("funckind  ");
				global.pw.printf("Level = %d  ", Attrib.more.funcAttr.level);
				global.pw.printf("nOff = %d  ", Attrib.more.funcAttr.nOff);
				break;
			default:
				global.pw.print("error  ");
			}
			global.pw.println();
		}
		global.pw.flush();
	}

	/********************************************************/
	/* 	函数	PrintSymbTable									*/
	/* 	功能 	打印生成的符号表										*/
	/* 	说明													*/
	/********************************************************/
	public void PrintSymbTable() {
		/* 层数从0开始 */
		for(int level=0;level<scope.size()&&level<2;level++)
			PrintOneLayer(level);
	}

	/********************************************************/
	/* 	函数	EnterNextLevel 									*/
	/* 	功能	创建空符号表 										*/
	/* 	说明	当进入一个新的局部化单位时，调用本子程序 						*/
	/* 	功能	建立一个空符号表table，层数加1，偏移初始化为0 				*/
	/********************************************************/
	public void EnterNextLevel() {
		Level ++;	/* 层数加一 */
		Off = 0;	/* 偏移初始化 */
		scope.add(new ArrayList<SymbTable>());
	}

	/********************************************************/
	/* 	函数	ReturnLastLevel 								*/
	/* 	功能 	撤销当前符号表 										*/
	/* 	说明	退出一个局部化区时，调用本子程序。 							*/
	/* 	功能	层数减1，并撤销当前符号表 								*/
	/********************************************************/
	public void ReturnLastLevel() {
		Level --;
	}

	/********************************************************/
	/* 	函数 	enterTable 										*/
	/* 	功能 	登记标识符和属性										*/
	/* 	说明 	1.把标识符id和属性attrIR登记到符号表中，并返回登记项的地址。		*/
	/* 		2.检查在本层中是否有重复声明错误，如果id项已存在则返回true，		*/
	/* 		        无错返回false。 									*/
	/********************************************************/
	public SymbTable enterTable(int lineno, String id, AttributeIR attrIR) {

		boolean isRedeclare = false;	/* false表示无重复声明错误 */
		SymbTable table = new SymbTable();
		
		/* 检查重复定义错误 */
		if (scope.get(Level).isEmpty()) {
			//table.attrIR.kind = IdKind.typeKind;
			scope.get(Level).add(table);
		} else {
			/* 在该层符号表内检查是否有重复定义错误 */
			for (int i=0; i<scope.get(Level).size(); i++)
				if (id.equals(scope.get(Level).get(i).idName)) {
					Global.Error = true;
					isRedeclare = true;
				}
			if (isRedeclare)
				ErrorPrompt(lineno, id, "is declared repeatedly!");
			else {
				//table.attrIR.kind = IdKind.typeKind;
				scope.get(Level).add(table);
			}
		}
		
		/* 将标识符名和属性登记到表中 */
		table.idName = id;
		table.attrIR = attrIR;

		return table;
	}

	/********************************************************/
	/* 	函数	FindEntry 										*/
	/* 	功能	寻找表项地址										*/
	/* 	说明	对给定的标识符id (id为字符串类型) 求出其表项地址, 			*/
	/* 		并在entry的实参单元中返回表项地址。如果符号表里没 				*/
	/* 		有所找的id项,则返回present为0,则函数中的参数entry 			*/
	/* 		赋值为指向该表项地址的指针;否则,present赋值为1。 				*/
	/********************************************************/
	public SymbTable findTable(int lineno, String id) {
		
		boolean isFind = false;
		int lev = Level, index = 0;

		/* 如果在本层中没有查到，则转到上一个局部化区域中继续查找 */
		for (lev=Level; lev>=0&&!isFind; lev--)
			for (index=0; index<scope.get(lev).size()&&!isFind; index++)
				if (id.equals(scope.get(lev).get(index).idName))
					isFind = true; /* 如果找到相同名字的标识符，则返回TRUE */
		
		if (!isFind)
			ErrorPrompt(lineno, id, "is not be declared!");

		return scope.get(lev+1).get(index-1);
	}

	/***********************************************************/
	/* 函数名 BaseType */
	/* 功 能 创建当前空类型内部表示 */
	/* 说 明 参数为类型，函数返回该类型的内部表示的地址 */
	/***********************************************************/
	public TypeIR BaseTypeToPtr(TypeKind kind) {
		/* 内存中动态申请分配单元， 返回指向该单元的类型内部表示类型指针t */
		TypeIR ptr = new TypeIR();
		switch (kind) {
		case intTy:
		case charTy:
		case boolTy:
			ptr.kind = kind;
			ptr.size = 1;
			break;
		case arrayTy:
			ptr.kind = kind;
			break;
		case structTy:
			ptr.kind = kind;
			break;
		}
		return ptr;
	}

	/***********************************************************/
	/* 函数名 ErrorPrompt */
	/* 功 能 错误提示 */
	/* 说 明 在输出文件中显示错误提示，并给全局量Error赋值为1 */
	/***********************************************************/
	public void ErrorPrompt(int line, String name, String message) {
		global.pw.print(">>> error :   ");
		global.pw.printf("Analyze error at line %d, %s %s",line,name,message);
		global.pw.println();
		global.pw.flush();
		Global.Error = true;
		System.exit(0);
	}
	
	/*************************************************************
	  						语义分析函数实现 
	*************************************************************/
	
	/************************ 类型的语义分析 **************************/
	
	/************************************************************/
	/* 	函数 	DecToPtr 											*/
	/* 	功能 	该函数用来完成类型分析的工作 									*/
	/* 	说明 	处理语法树的当前结点类型。构造出当前类型的内部表 					*/
	/* 		示，并将其地址返回给Ptr类型内部表示的地址. 						*/
	/************************************************************/
	public TypeIR DecToPtr(treeNode t, DecKind deckind) {
		TypeIR Ptr = null;
		switch (deckind) {
		case IntK:
			Ptr = BaseTypeToPtr(TypeKind.intTy);
			break; /* 类型为整数类型 */
		case CharK:
			Ptr = BaseTypeToPtr(TypeKind.charTy);
			break; /* 类型为字符类型 */
		case ArrayK:
			Ptr = arrayType(t);
			break; /* 类型为数组类型 */
		case StructK:
			Ptr = structType(t);
			break; /* 类型为结构体类型 */
		case IdK:
			Ptr = nameType(t);
			break; /* 类型为自定义标识符 */
		}
		return Ptr;
	}

    /************************************************************/
    /* 	函数  	arrayType                                        	*/
    /* 	功能  	该函数处理数组类型的内部表示                     							*/
    /* 	说明  	类型为数组类型时，需要检查下标是否合法。         						*/
    /************************************************************/
	public TypeIR arrayType(treeNode t) {
		
		TypeIR Ptr0 = null;
		TypeIR Ptr1 = null;
		TypeIR Ptr = null;

		/* 调用类型分析函数，处理下标类型 */
		Ptr0 = DecToPtr(t, DecKind.IntK);
		/* 调用类型分析函数，处理元素类型 */
		Ptr1 = DecToPtr(t, t.attr.arrayAttr.childType);
		/* 指向一新创建的类型信息表 */
		Ptr = BaseTypeToPtr(TypeKind.arrayTy);
		/* 计算本类型长度 */
		Ptr.size =(t.attr.arrayAttr.size)*(Ptr1.size);
		/* 填写其他信息 */
		Ptr.more.arrayAttr.indexTy = Ptr0;
		Ptr.more.arrayAttr.elemTy = Ptr1;
		Ptr.more.arrayAttr.size = t.attr.arrayAttr.size;
		
		return Ptr;
	}

	/************************************************************/
	/* 	函数	structType */
	/* 	功能 	该函数处理记录类型的内部表示 */
	/* 	说明 	类型为记录类型时，是由记录体组成的。其内部节点需 */
	/* 		要包括3个信息:一是空间大小size；二是类型种类标志 */
	/* 		structTy;三是体部分的节点地址body。记录类型中的 */
	/* 		域名都是标识符的定义性出现，因此需要记录其属性。 */
	/************************************************************/
	public TypeIR structType(treeNode t) {

		ArrayList<FieldChain> body = new ArrayList<FieldChain>();
		int off = 0,size = 0;
		t = t.child[0]; /* 从语法数的儿子节点读取域信息 */
		while (t != null) /* 循环处理 */
		{
			/* 填写ptr2指向的内容节点,此处循环是处理此种情况int a,b; */
			for (int i = 0; i < t.idnum; i++) {
				/* 申请新的域类型单元结构Ptr2 */
				FieldChain Fc = new FieldChain();

				/* 填写Ptr2的各个成员内容 */
				Fc.id = t.name[i];
				Fc.off = off + size;
				Fc.unitType = DecToPtr(t, t.kind.dec);

				off = Fc.off;
				size = Fc.unitType.size;
				body.add(Fc);
			}
			/* 处理完同类型的变量后，取语法树的兄弟节点 */
			t = t.sibling;
		}

		/* 处理记录类型内部结构 */
		
		/* 新建结构类型的节点 */
		TypeIR Ptr = BaseTypeToPtr(TypeKind.structTy);
		/* 取Ptr2的off为最后整个记录的size */
		Ptr.size = off + size;
		/* 将域链链入记录类型的body部分 */
		Ptr.more.structBody = body;

		return Ptr;
	}

	/************************************************************/
	/* 	函数	nameType */
	/* 	功能	该函数用来在符号表中寻找已定义的类型名字 */
	/* 	说明	调用寻找表项地址函数FindEntry，返回找到的表项地址 */
	/* 		指针entry。如果present为FALSE，则发生无声明错误。 */
	/* 		如果符号表中的该标识符的属性信息不是类型，则非类 */
	/* 		型标识符。该函数返回指针指向符号表中的该标识符的 */
	/* 		类型内部表示。 */
	/************************************************************/
	public TypeIR nameType(treeNode t) {

		TypeIR Ptr = null;
		
		/* 类型标识符也需要往前层查找 */
		SymbTable table = findTable(t.lineno, t.attr.type_name);
		
		/* 检查该标识符是否为类型标识符 */
		if (table.attrIR.kind == IdKind.typeKind)
			Ptr = table.attrIR.idtype;
		else
			ErrorPrompt(t.lineno, t.attr.type_name, "used before typed!");
		
		return Ptr;
	}

	/************************ 声明的语义分析 **************************/

	/************************************************************/
	/* 	函数 	TypeDecPart */
	/* 	功能 	该函数处理类型声明的语义分析 */
	/* 	说明 	遇到类型T时，构造其内部节点TPtr；对于"typedef idname T"构 */
	/* 		造符号表项；检查本层类型声明中是否有重复定义错误. */
	/************************************************************/
	public void TypeDecPart(treeNode t) {

		AttributeIR attrIr = new AttributeIR();
		attrIr.kind = IdKind.typeKind;	/* 声明的符号表属性 */

		/* 调用记录属性函数，返回是否重复声明错和入口地址 */
		SymbTable table = enterTable(t.lineno, t.name[0], attrIr);

		table.attrIR.idtype = DecToPtr(t, t.kind.dec);
	}

	/************************************************************/
	/* 	函数	VarDecPart */
	/* 	功能	该函数处理变量声明的语义分析 */
	/* 	说明 	当遇到变量表识符id时，把id登记到符号表中；检查重 */
	/* 		复性定义；遇到类型时，构造其内部表示。 */
	/************************************************************/
	public void VarDecPart(treeNode t) {

		for (int i = 0; i < t.idnum; i++) {
			AttributeIR attrIr = new AttributeIR();
			attrIr.kind = IdKind.varKind;	/* 变量的符号表属性 */
			
			attrIr.idtype = DecToPtr(t, t.kind.dec);
			attrIr.more.varAttr.level = Level;
			/* 计算值参的偏移 */
			attrIr.more.varAttr.off = Off;
			Off = Off + attrIr.idtype.size;

			/* 登记该变量的属性及名字,并返回其类型内部指针 */
			t.table[i] = enterTable(t.lineno, t.name[i], attrIr);
		}
		/* 记录此时偏移，用于下面填写过程信息表的nOff信息 */
		savedOff = Off;
	}

	/************************************************************/
	/* 函数名FuncDecPart */
	/* 功 能 该函数处理过程声明的语义分析 */
	/* 说 明 在当前层符号表中填写过程标识符的属性；在新层符号 */
	/* 表中填写形参标识符的属性。 */
	/************************************************************/
	public void FuncDecPart(treeNode t) {
		
		AttributeIR attrIr = new AttributeIR();
		attrIr.kind = IdKind.funcKind;	/* 函数的符号表属性 */
		attrIr.idtype = DecToPtr(t, t.kind.dec);
		attrIr.more.funcAttr.level = 1;
		
		/* 登记函数的符号表项 */
		SymbTable table = enterTable(t.lineno, t.name[0], attrIr);
		/* 处理形参声明表 */
		t.table[0] = table;

		/* t.child[0]是函数的参数部分 */
		table.attrIR.more.funcAttr.param = ParaDecList(t.child[0]);
		table.attrIR.more.funcAttr.nOff = savedOff;
		savedOff = 0;	/* 用过即清 */
		/* 过程活动记录的长度等于nOff加上display表的长度,diplay表的长度等于过程所在层数(也就是1)加1 */
		table.attrIR.more.funcAttr.mOff = table.attrIR.more.funcAttr.nOff + 2;

		/* t.child[1].child[0]是函数的语句体部分 */
		Body(t.child[1].child[0]);

		/* 函数部分结束，删除进入形参时，新建立的符号表 */
		if (Level != -1)
			ReturnLastLevel();/* 结束当前scope */
	}
	
	/************************************************************/
	/* 函数名 ParaDecList */
	/* 功 能 该函数处理函数头中的参数声明的语义分析 */
	/* 说 明 在新的符号表中登记所有形参的表项，构造形参表项的 */
	/* 地址表，并有para指向其。 */
	/************************************************************/
	public ArrayList<ParamTable> ParaDecList(treeNode t) {

		ArrayList<ParamTable> list = new ArrayList<ParamTable>();
		EnterNextLevel(); /* 进入新的局部化区 */
		Off = 10; /* 子程序中的变量初始偏移设为10 */
		
		if (t != null) {
			/*变量声明部分*/
			while(t!=null) {
				VarDecPart(t);
				t = t.sibling;
			}
			
			for (int i=0;i<scope.get(Level).size();i++) {
				/* 构造形参符号表，并使其连接至符号表的param项 */
				ParamTable Pt = new ParamTable();
				Pt.table = scope.get(Level).get(i);
				list.add(Pt);
			}
		}
		return list; /* 返回形参符号表的头指针 */
	}

	/******************* 执行体部分的语义分析 *********************/

	/************************************************************/
	/* 函数名 Body */
	/* 功 能 该函数处理执行体部分的语义分析 */
	/* 说 明 TINY编译系统的执行体部分即为语句序列，故只需处理 */
	/* 语句序列部分。 */
	/************************************************************/
	public void Body(treeNode t) {
		while (t != null) {
			switch (t.nodeKind) {
			case StmLK:
				treeNode p = t.child[0];
				while (p != null) {
					Statement(p); /* 调用语句状态处理函数 */
					p = p.sibling;
				}
				break;
			case StmtK:
				Statement(t); /* 调用语句状态处理函数 */
				break;
			case TypeK:
				TypeDecPart(t);
				break;
			case DecK:
				VarDecPart(t);
				break;
			default:
				ErrorPrompt(t.lineno, "", "no this node kind in body list!");
				break;
			}
			t = t.sibling; /* 依次读入语法树语句序列的兄弟节点 */
		}
	}

	/************************************************************/
	/* 函数名 statement */
	/* 功 能 该函数处理语句状态 */
	/* 说 明 根据语法树节点中的kind项判断应该转向处理哪个语句 */
	/* 类型函数。 */
	/************************************************************/
	public void Statement(treeNode t) {
		switch (t.kind.stmt) {
		case AssignK:
			AssignStmt(t);
			break;
		case CallK:
			CallStmt(t);
			break;
		case IfK:
			IfStmt(t);
			break;
		case WhileK:
			WhileStmt(t);
			break;
		case ForK:
			ForStmt(t);
			break;
		case CinK:
			CinStmt(t);
			break;
		case CoutK:
			CoutStmt(t);
			break;
		case ReturnK:
			ReturnStmt(t);
			break;
		default:
			ErrorPrompt(t.lineno, "", "statement type error!");
			break;
		}
	}

	/************************************************************/
	/* 函数名 Expr */
	/* 功 能 该函数处理表达式的分析 */
	/* 说 明 表达式语义分析的重点是检查运算分量的类型相容性， */
	/* 求表达式的类型。其中参数Ekind用来表示实参是变参 */
	/* 还是值参。 */
	/************************************************************/
	public TypeIR Expr(treeNode t) {
		
		TypeIR Eptr0 = null;
		TypeIR Eptr1 = null;
		TypeIR Eptr = null;
		
		if (t != null)
			switch (t.kind.exp) {
			case ConstK:
				switch (t.attr.expAttr.type) {
				case Int:
					Eptr = DecToPtr(t, DecKind.IntK);
					Eptr.kind = TypeKind.intTy;
					break;
				case Char:
					Eptr = DecToPtr(t, DecKind.CharK);
					Eptr.kind = TypeKind.charTy;
					break;
				default:
					ErrorPrompt(t.lineno, "", "this type is not for const!");
					break;
				}
				break;
			case VariK:
				/* Var = id的情形 */
				if (t.child[0] == null) {
					/* 在符号表中查找此标识符 */
					SymbTable table = findTable(t.lineno ,t.name[0]);
					t.table[0] = table;

					if (table.attrIR.kind != IdKind.varKind) {
						ErrorPrompt(t.lineno, t.name[0], "is not variable error!");
						Eptr = null;
					} else
						Eptr = table.attrIR.idtype;
				} 
				else if (t.attr.expAttr.varKind == VarKind.ArrayMembV)/* Var = Var0[E]的情形 */
						Eptr = arrayVar(t);
				else if (t.attr.expAttr.varKind == VarKind.FieldMembV)/* Var = Var0.id的情形 */
						Eptr = structVar(t);
				break;
			case OpK:
				/* 递归调用儿子节点 */
				Eptr0 = Expr(t.child[0]);
				if (Eptr0 == null)
					return null;
				Eptr1 = Expr(t.child[1]);
				if (Eptr1 == null)
					return null;
				
				/* 类型判别 */
				if (Eptr0.kind==Eptr1.kind)
					switch (t.attr.expAttr.op) {
					case LT:
					case GT:
					case LE:
					case GE:
					case EQ:
					case NEQ:
						Eptr = BaseTypeToPtr(TypeKind.boolTy);
						break; /* 条件表达式 */
					case PLUS:
					case MINUS:
					case TIMES:
					case OVER:
						Eptr = BaseTypeToPtr(TypeKind.intTy);
						break; /* 算数表达式 */
					default:
						break;
					}
				else
					ErrorPrompt(t.lineno, "", "operator is not compat!");
				break;
			}
		return Eptr;
	}

	/************************************************************/
	/* 函数名 arrayVar */
	/* 功 能 该函数处理数组变量的下标分析 */
	/* 说 明 检查var := var0[E]中var0是不是数组类型变量，E是不 */
	/* 是和数组的下标变量类型匹配。 */
	/************************************************************/
	public TypeIR arrayVar(treeNode t) {

		TypeIR Eptr = null;

		/* 在符号表中查找此标识符 */
		SymbTable table = findTable(t.lineno, t.name[0]);
		t.table[0] = table;
		
		/* Var0不是变量 */
		if (table.attrIR.kind != IdKind.varKind)
			ErrorPrompt(t.lineno, t.name[0], "is not variable error!");
		/* Var0不是数组类型变量 */
		else if (table.attrIR.idtype != null)
			if (table.attrIR.idtype.kind != TypeKind.arrayTy)
				ErrorPrompt(t.lineno, t.name[0], "is not array variable error !");
			else {
				/* 检查E的类型是否与下标类型相符 */
				TypeIR Eptr0 = table.attrIR.idtype.more.arrayAttr.indexTy;
				if (Eptr0 == null)
					return null;
				TypeIR Eptr1 = Expr(t.child[0]);// intPtr;
				if (Eptr1 == null)
					return null;
				if (Eptr0.kind==Eptr1.kind)
					Eptr = table.attrIR.idtype.more.arrayAttr.elemTy;
				else
					ErrorPrompt(t.lineno, "", "array member type must be int!");
			}
		return Eptr;
	}

	/************************************************************/
	/* 函数名 recordVar */
	/* 功 能 该函数处理记录变量中域的分析 */
	/* 说 明 检查var:=var0.id中的var0是不是记录类型变量，id是 */
	/* 不是该记录类型中的域成员。 */
	/************************************************************/
	public TypeIR structVar(treeNode t) {
		
		boolean isFind = false;
		TypeIR Eptr = null;
		FieldChain currentP = null;

		/* 在符号表中查找此标识符 */
		SymbTable table = findTable(t.lineno, t.name[0]);
		t.table[0] = table;
		
		/* Var0不是变量 */
		if (table.attrIR.kind != IdKind.varKind)
			ErrorPrompt(t.lineno, t.name[0], "is not a variable name!");
		/* Var0不是记录类型变量 */
		else if (table.attrIR.idtype.kind != TypeKind.structTy)
			ErrorPrompt(t.lineno, t.name[0], "is not a struct variable name!");
		/* 检查id是否是合法域名 */
		else {
			for (int i=0;i<table.attrIR.idtype.more.structBody.size()&&!isFind;i++) {
				currentP = table.attrIR.idtype.more.structBody.get(i);
				isFind = t.child[0].name[0].equals(currentP.id);
				/* 如果相等 */
				if (isFind)
					Eptr = currentP.unitType;
			}
			if (!isFind) {
				ErrorPrompt(t.child[0].lineno, t.child[0].name[0], "is not field type!");
				Eptr = null;
			}
			/* 如果id是数组变量,Var = Var0.id[]的情形 */
			else if (t.child[0].child[0] != null)
			{
				t = t.child[0];
				/* id不是数组类型变量 */
				if (Eptr.kind != TypeKind.arrayTy)
					ErrorPrompt(t.lineno, t.name[0], "is not array variable error !");
				else {
					/* 检查E的类型是否与下标类型相符 */
					TypeIR Eptr0 = Eptr.more.arrayAttr.indexTy;
					if (Eptr0 == null)
						return null;
					TypeIR Eptr1 = Expr(t.child[0]);// intPtr;
					if (Eptr1 == null)
						return null;
					if (Eptr0.kind == Eptr1.kind)
						Eptr = Eptr.more.arrayAttr.elemTy;
					else
						ErrorPrompt(t.lineno, "", "array member type must be int!");
				}
			}
		}
		return Eptr;
	}

	/************************************************************/
	/* 函数名 assignstatement */
	/* 功 能 该函数处理赋值语句分析 */
	/* 说 明 赋值语句的语义分析的重点是检查赋值号两端分量的类 */
	/* 型相容性。 */
	/************************************************************/
	public void AssignStmt(treeNode t) {
		
		SymbTable table = new SymbTable();
		TypeIR Eptr = null;
		treeNode child1 = t.child[0];
		treeNode child2 = t.child[1];
		
		if (child1.child[0] == null) {
			/* 在符号表中查找此标识符 */
			table = findTable(t.lineno, child1.name[0]);

			if (table.attrIR.kind != IdKind.varKind) {
				ErrorPrompt(child1.lineno, child1.name[0],
						"is not variable error!");
				Eptr = null;
			} else {
				Eptr = table.attrIR.idtype;
				child1.table[0] = table;
			}
		} else
		{	/* Var0[E]的情形 */
			if (child1.attr.expAttr.varKind == VarKind.ArrayMembV)
				Eptr = arrayVar(child1);
			else /* Var0.id的情形 */
			if (child1.attr.expAttr.varKind == VarKind.FieldMembV)
				Eptr = structVar(child1);
		}
		if (Eptr != null) {
			if ((t.nodeKind == NodeKind.StmtK)
					&& (t.kind.stmt == StmtKind.AssignK)) {
				/* 检查是不是赋值号两侧 类型等价 */
				if (Expr(child2).kind!=Eptr.kind)
					ErrorPrompt(t.lineno, "", "assign expression's type is not compat!");
			}
			/* 本语言的局限性：赋值语句中不能出现函数调用 */
		}
	}

	/************************************************************/
	/* 函数名 callstatement */
	/* 功 能 该函数处理函数调用语句分析 */
	/* 说 明 函数调用语句的语义分析首先检查符号表求出其属性中 */
	/* 的Param部分（形参符号表项地址表），并用它检查形参 */
	/* 和实参之间的对应关系是否正确。 */
	/************************************************************/
	public void CallStmt(treeNode t) {
		
		treeNode p = null;

		/* 用id检查整个符号表 */
		SymbTable table = findTable(t.lineno, t.child[0].name[0]);
		t.child[0].table[0] = table;

		/* id不是函数名 */
		if (table.attrIR.kind != IdKind.funcKind)
			ErrorPrompt(t.lineno, t.name[0], "is not function name!");
		else/* 形实参匹配 */
		{
			p = t.child[1];
			/* paramP指向形参符号表的一员*/
			ParamTable paramP;
			for (int i=0; i<table.attrIR.more.funcAttr.param.size(); i++) {
				/*实参个数少于形参*/
				if(p == null)
					ErrorPrompt(t.child[1].lineno, "", "param num is not match!");
				paramP = table.attrIR.more.funcAttr.param.get(i);
				SymbTable paraTable = paramP.table;
				TypeIR Etp = Expr(p);/* 实参 */
				/* 参数类型不匹配 */
				if (paraTable.attrIR.idtype.kind != Etp.kind)
					ErrorPrompt(p.lineno, "", "param type is not match!");
				p = p.sibling;
			}
			/*形参个数少于实参*/
			if (p != null)
				ErrorPrompt(t.child[1].lineno, "", "param num is not match!");
		}
	}

	/************************************************************/
	/* 函数名 ifstatement */
	/* 功 能 该函数处理条件语句分析 */
	/* 说 明 分析语法树的三个儿子节点 */
	/************************************************************/
	public void IfStmt(treeNode t) {
		TypeIR Etp = Expr(t.child[0]);
		if (Etp != null)
			/* 处理条件表达式 */
			if (Etp.kind != TypeKind.boolTy)
				ErrorPrompt(t.lineno, "", "condition expressrion error!"); /* 逻辑表达式错误 */
			else {
				/* 处理then语句序列部分 */
				Body(t.child[1]);
				/* 处理else语句序列部分 */
				Body(t.child[2]);
			}
	}

	/************************************************************/
	/* 函数名 whilestatement */
	/* 功 能 该函数处理循环语句分析 */
	/* 说 明 分析语法树的两个儿子节点 */
	/************************************************************/
	public void WhileStmt(treeNode t) {
		TypeIR Etp = Expr(t.child[0]);
		if (Etp != null)
			/* 处理条件表达式部分 */
			if (Etp.kind != TypeKind.boolTy)
				ErrorPrompt(t.lineno, "", "condition expression error!"); /* 逻辑表达式错误 */
			else /* 处理循环部分 */
				Body(t.child[1]);
	}
	
	/************************************************************/
	/* 函数名 forstatement */
	/* 功 能 该函数处理循环语句分析 */
	/* 说 明 分析语法树的两个儿子节点 */
	/************************************************************/
	public void ForStmt(treeNode t) {
		TypeIR Etp = Expr(t.child[1]);
		if (Etp != null)
			/* 处理条件表达式部分 */
			if (Etp.kind != TypeKind.boolTy)
				ErrorPrompt(t.lineno, "", "condition expression error!"); /* 逻辑表达式错误 */
			else {
				/* 处理初始化部分 */
				Body(t.child[0]);
				/* 处理循环控制部分 */
				Body(t.child[2]);
				/* 处理循环体部分 */
				Body(t.child[3]);
			}
	}

	/************************************************************/
	/* 函数名 readstatement */
	/* 功 能 该函数处理输入语句分析 */
	/* 说 明 分析语法树节点，检查变量有无声明和是否为变量错误 */
	/************************************************************/
	public void CinStmt(treeNode t) {
		SymbTable table = findTable(t.lineno, t.name[0]);
		t.table[0] = table;

		/* 不是变量标识符错误 */
		if (table.attrIR.kind != IdKind.varKind)
			ErrorPrompt(t.lineno, t.name[0], "is not var name!");
	}

	/************************************************************/
	/* 函数名 writestatement */
	/* 功 能 该函数处理输出语句分析 */
	/* 说 明 分析输出语句中的表达式是否合法 */
	/************************************************************/
	public void CoutStmt(treeNode t) {
		TypeIR Etp = Expr(t.child[0]);
		if (Etp != null)
			/* 如果表达式类型为bool类型，报错 */
			if (Etp.kind == TypeKind.boolTy)
				ErrorPrompt(t.lineno, "", "exprssion type error!");
	}

	/************************************************************/
	/* 函数名 returnstatement */
	/* 功 能 该函数处理函数返回语句分析 */
	/* 说 明 分析函数返回语句是否在主程序中出现 */
	/************************************************************/
	public void ReturnStmt(treeNode t) {
		if (Level == 0)
			/* 如果返回语句出现在函数体外，报错 */
			ErrorPrompt(t.lineno, "", "return statement error!");
	}

	/************************************************************/
	/* 函数名 analyze */
	/* 功 能 该函数处理总的语义分析 */
	/* 说 明 对语法树进行分析 */
	/************************************************************/
	public void run() {
		
		treeNode p = null;
	//	treeNode root = myParse.run();
		treeNode root = myParseLL1.run();
		
		/* 检测语法树的最后一个函数是否为main函数  */
		if(root.child[0] != null)
			for(p = root.child[0]; p.sibling!=null; p = p.sibling);
		else
			ErrorPrompt(0, "", "no main function in syntax tree!");

			if(!p.name[0].equals("main"))
				ErrorPrompt(p.lineno, "", "no main function in syntax tree!");
		
		/* 创建符号表 */
		EnterNextLevel();

		/* 从语法树第一条语句开始遍历 */
		p = root.child[0];
		while (p != null) {
			switch (p.nodeKind) {
			case TypeK:
				TypeDecPart(p);
				break;
			case DecK:
				VarDecPart(p);
				break;
			case FuncDecK:
				FuncDecPart(p);
				break;
			default:
				ErrorPrompt(p.lineno, "", "no this node kind in syntax tree!");
				break;
			}
			p = p.sibling;/* 循环处理 */
		}

		/* 输出各层符号表 */
		PrintSymbTable();

		/* 输出所有域表 */
		for(int i=0;i<scope.size();i++)
			for(int j=0;j<scope.get(i).size();j++)
				if(scope.get(i).get(j).attrIR.kind==IdKind.typeKind)
					if(scope.get(i).get(j).attrIR.idtype.kind==TypeKind.structTy)
						PrintFieldChain(scope.get(i).get(j));
		
		/* 撤销符号表 */
		if (Level != -1)
			ReturnLastLevel();
	}
}