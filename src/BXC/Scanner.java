/******************************************************
 	文件	scanner.java
 	说明	编译器的词法扫描器实现
 	功能	生成token序列,包含单词所在行号、单词类型和语义信息
 		进行词法错误检查
******************************************************/

package BXC;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import BXC.Global.LexType;

/* 单词的类型，包括词法信息和语义信息 */
class Token
{
	public int lineshow;
	public LexType Lex;
    public String Sem;
}

public class Scanner {
	
	public static void main(String[] args)
	{
		Scanner myScanner = new Scanner();
		myScanner.run();
	}

	public static Global global = new Global();
	
	/* 文件输入流 */
	public BufferedReader reader = null;

	/* 源程序追踪标志,如果该标志为TRUE,语法分析时将源程序行号写入列表文件listing */
	public boolean EchoSource = true;
	
	/* 顺序表类型 */
	public ArrayList<Token> tokenChain = new ArrayList<Token>();

	/************************************************************
		函数	getNextChar											   
	   	功能	取得下一非空字符									   
	   	说明	该函数从输入缓冲区lineBuf中取得下一个非空字符		       
	   		如果lineBuf中的字串已经读完,则从源代码文件中读入一新行
	************************************************************/
	public char getNextChar()
	{
		/* 当前代码输入行缓冲器lineBuf已经耗尽 */
		if (!(Global.linepos < Global.bufsize))
		{
			/* 源代码行号lineno加1 */
			Global.lineno++;
		    
		    /* 从源文件source中读入BUFLEN-2(254)个字符到行缓冲区lineBuf中
			   fgets在的lineBuf末尾保留换行符.并在末尾加了一个NULL字符表示结束 */
			try {Global.lineBuf = reader.readLine();}catch (IOException e) {}
			if (Global.lineBuf != null)
			{ 	 
			  /* 如果源文件追踪标志EchoSource为TRUE                               
			                 将源程序行号lineno及行内容lineBuf在词法扫描时写入列表文件listing */
			  if (EchoSource)
			  {
				  global.pw.printf("%4d: ", Global.lineno);
				  global.pw.println(Global.lineBuf);
				  global.pw.flush();
			  }
			  
			  /* 取得当前输入源代码行的实际长度,送给变量bufsize */
			  Global.bufsize = Global.lineBuf.length();
			  if(Global.bufsize == 0) getNextChar();
			  
			  /* 输入行缓冲区lineBuf中当前字符位置linepos指向lineBuf开始位置 */
			  Global.linepos = 0;

			  /* 取得输入行缓冲区lineBuf中下一字符 */
		      return Global.lineBuf.charAt(Global.linepos++);

		    }
		    else
			{ 
			  /* 未能成功读入新的代码行,fget函数返回值为NULL *
			   * 已经到源代码文件末尾,设置EOF_flag标志为TRUE */
			  Global.EOF_flag = true;
			  
		   	  /* 函数返回EOF */
			  return '#';
		    }
		}

		/* 行输入缓冲区lineBuf中字符还未读完,直接取其中下一字符,函数返回所取字符 */
		else
			return Global.lineBuf.charAt(Global.linepos++);
	}

	/************************************************************
		函数	ungetNextChar											
		功能	字符回退函数									
		说明	该过程在行输入缓冲区lineBuf中回退一个字符		
			用于超前读字符后不匹配时候的回退				
	************************************************************/
	public static void ungetNextChar()
	{
	  /* 如果EOF_flag标志为FALSE,不是处于源文件末尾	  *
	   * 输入行缓冲区lineBuf中当前字符位置linepos减1 */
	  if (!Global.EOF_flag) Global.linepos-- ;
	}
	
	/************************************************************
	 	函数	ChainToFile
 		功能	将链表中的Token结点依次存入文件中
 		说明	用到了顺序表类ArrayList
	************************************************************/
	public void ChainToFile()
	{
		/*创建一个新的文件"TokenList",以存储Token序列*/
		String Tokenlist = "F:\\TokenList.txt";	//Token输出文件
		File fp = new File(Tokenlist);
		
		BufferedWriter TokenWriter = null;
		try
		{
			if(fp.exists())
				fp.delete();
			fp.createNewFile();
			TokenWriter = new BufferedWriter(new FileWriter(fp, true));
		} 
		catch (IOException e)
		{
			JOptionPane.showMessageDialog(null, "Cannot open file Tokenlist!\n");
			Global.Error = true;
			System.exit(0);
		}
		PrintWriter pw = new PrintWriter(TokenWriter);
		
		/*从表头到表尾，依次将所有的Token写入文件*/ 
		for (int i=0; i<tokenChain.size(); i++)
		{
			pw.printf("%4d  %10s  ", tokenChain.get(i).lineshow,tokenChain.get(i).Lex);
			pw.println(tokenChain.get(i).Sem);
			pw.flush();
		}

		try {
			TokenWriter.flush();
			pw.close();
			TokenWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/************************************************************
	 	函数	getTokenlist						   			    
	 	功能	 词法扫描器基本函数：取得单词函数										
	 	说明 	函数从源文件字符串序列中获取所有Token序列 		
			使用确定性有限自动机DFA,采用直接转向法    		
			超前读字符,对保留字采用查表方式识别
			产生词法错误时候,仅仅略过产生错误的字符,不加改正
	************************************************************/
	public void getTokenlist()
	{  
	   /*存放当前的Token*/
	   Token currentToken = new Token();
	
	   do
	   {  /* 当前状态标志state,始终都是以START作为开始 */
	      Global.StateType state = Global.StateType.START;

	  	 /* tokenString用于保存标识符和保留字单词的词元,长度41 */
	  	  String tokenString = "";

	      /* tokenString的存储标志save,整数类型						*
	       * 决定当前识别字符是否存入当前识别单词词元存储区tokenString */
	      boolean save = true;
	
	      /* 当前确定性有限自动机DFA状态state不是完成状态DONE */
	      while (state != Global.StateType.DONE)
	   
		  { 
	        /* 从源代码文件中获取下一个字符,送入变量c作为当前字符 */
	    	 char c = getNextChar();
	
	    	/* 当前正识别字符的存储标志save初始为TRUE */
	         save = true;					
	
	         switch (state)
			 {
		       /* 当前DFA状态state为开始状态START,DFA处于当前单词开始位置 */
	            case START:	
	         	   
		 		   /* 当前字符c为字母,当前DFA状态state设置为标识符状态INID *
		 			* 确定性有限自动机DFA处于标识符类型单词中              */
		 	        if (Character.isAlphabetic(c))
		 	             state = Global.StateType.INID;			
		
		           /* 当前字符c为数字,当前DFA状态state设置为数字状态INNUM *
			        * 确定性有限自动机DFA处于数字类型单词中               */
		 	        else if (Character.isDigit(c))				
			     		 state = Global.StateType.INNUMBER;			
		
			       /* 当前字符c为冒号,当前DFA状态state设置为赋值状态INASSIGN *
				    * 确定性有限自动机DFA处于赋值类型单词中				   */
		            else if (c == '=')
		                 state = Global.StateType.INASSIGN;		
		  		 
			       /* 当前字符c为<,当前DFA状态state设置为输入状态*/
			       /* INCIN，确定性有限自动机DFA处于输入类型单词中*/                         
				    else if (c == '<')
				         state = Global.StateType.INCIN;
		 	        
				    else if (c == '>')
				         state = Global.StateType.INCOUT;
                    
				    else if (c == '/')
				    {
		                 save = false;
				         state = Global.StateType.INCOMMENT;
				    }
                    
		 	       /* 当前字符c为单引号,当前DFA状态state设置为字符标志状态*/
				    else if (c == '\'')
				    {
						 save = false;
				         state = Global.StateType.INCHAR;
				    }
		 	        
				   /* 当前字符c为空白(空格,制表符,换行符),字符存储标志save设置为FALSE *
				    * 当前字符为分隔符,不需要产生单词,无须存储                        */
		            else if ((c == ' ') || (c == '\t') || (c == '\n'))
		            	 save = false;

		 	       /* 当前字符c为其它字符,当前DFA状态state设置为完成状态DONE *
		 	        * 确定性有限自动机DFA处于单词的结束位置,需进一步分类处理 */
		            else
		 			{
		 				 state = Global.StateType.DONE;
		                 switch (c)
		 				 {
		 			      /* 当前字符c为EOF,字符存储标志save设置为FALSE,无需存储     *
		 			       * 当前识别单词返回值currentToken设置为文件结束单词ENDFILE */
		 		            case '#':
		                    save = false;
		                    currentToken.Lex = LexType.ENDFILE;
		                    break;					

		 			      /* 当前字符c为"=",当前识别单词返回值currentToken设置为等号单词EQ */
		                    case '.':
		                    currentToken.Lex = LexType.DOT;
		                    break;

		 			      /* 当前字符c为"<",当前识别单词返回值currentToken设置为小于单词LT */
		                    case ',':
		                    currentToken.Lex = LexType.COMMA;
		                    break;
		                    
		 			      /* 当前字符c为";",当前识别单词返回值currentToken设置为分号单词SEMI */
		                    case ';':
		                    currentToken.Lex = LexType.SEMI;
		                    break;

		 			      /* 当前字符c为"+",当前识别单词返回值currentToken设置为加号单词PLUS */
		                    case '+':
		                    currentToken.Lex = LexType.PLUS;
		                    break;

		 			      /* 当前字符c为"-",当前识别单词返回值currentToken设置为减号单词MINUS */
		                    case '-':
		                    currentToken.Lex = LexType.MINUS;
		                    break;
		                    
		 		       	  /* 当前字符c为"*",当前识别单词返回值currentToken设置为乘号单词TIMES */
		                    case '*':
		                    currentToken.Lex = LexType.TIMES;
		                    break;

			 			  /* 当前字符c为"(",当前识别单词返回值currentToken设置为左括号单词LPAREN */
			                case '(':
			                currentToken.Lex = LexType.LPAREN;
			                break;
			                
			 			  /* 当前字符c为")",当前识别单词返回值currentToken设置为右括号单词RPAREN */
			                case ')':
			                currentToken.Lex = LexType.RPAREN;
			                break;
   		 
		 		          /* 当前字符c为"[",当前识别单词返回值currentToken设置为左中括号单词LMIDPAREN */
		 		            case '[':
		 			        currentToken.Lex = LexType.LMIDPAREN;
		 			        break;
		     		 
		 			      /* 当前字符c为"]",当前识别单词返回值currentToken设置为右中括号单词RMIDPAREN */
		 			        case ']':
		 			        currentToken.Lex = LexType.RMIDPAREN;
		 			        break;
		 			   		 
			 		      /* 当前字符c为"{",当前识别单词返回值currentToken设置为左中括号单词LMIDPAREN */
			 		        case '{':
			 		        currentToken.Lex = LexType.LBIGPAREN;
			 			    break;
			     		 
			 		      /* 当前字符c为"}",当前识别单词返回值currentToken设置为右中括号单词RMIDPAREN */
			 		        case '}':
			 		        currentToken.Lex = LexType.RBIGPAREN;
					        break;
		     
		 			      /* 当前字符c为其它字符,当前识别单词返回值currentToken设置为错误单词ERROR */
		                    default:
		                    currentToken.Lex = LexType.ERROR;
		                    Global.Error = true;
		                    break;
		 				 }
		 			}
		        break;						
		 	    /********** 当前状态为开始状态START的处理结束 **********/
		        
	            case INID:
	       		/* 当前字符c不是字母,则在输入行缓冲区源中回退一个字符		 			*
	       		 * 字符存储标志设置为FALSE,当前DFA状态state设置为DONE,标识符单词识别完成 *
	       		 * 当前识别单词返回值currentToken设置为标识符单词ID               */
	            	if(!(Character.isAlphabetic(c)||Character.isDigit(c)))
	            	{
	                    ungetNextChar();
	                    save = false;
	                    state = Global.StateType.DONE;
	                    currentToken.Lex = LexType.ID;
	            	}
	            break;
	            
	            case INNUMBER:
	       	 	/* 当前字符c不是数字,则在输入行缓冲区源中回退一个字符					*
	       	 	 * 字符存储标志设置为FALSE,当前DFA状态state设置为DONE,数字单词识别完成	*
	       	 	 * 当前识别单词返回值currentToken设置为数字单词NUM           	*/
		            if (!Character.isDigit(c))
		            { 
			            ungetNextChar();
			            save = false;
			            state = Global.StateType.DONE;
			            currentToken.Lex = LexType.INTC;
		            }
	            break;

	     	   /* 当前DFA状态state为赋值状态INASSIGN,确定性有限自动机DFA处于赋值单词位置 */
	            case INASSIGN:

	            	/* 当前DFA状态state设置为完成状态DONE,赋值单词结束 */
	            	state = Global.StateType.DONE;				

		     		 /* 当前字符c为"=",当前识别单词返回值currentToken设置为赋值单词ASSIGN */
		     		if (c == '=')
		                currentToken.Lex = LexType.EQ;
	
		     		 /* 当前字符c为其它字符,即":"后不是"=",在输入行缓冲区中回退一个字符       *
		     		  * 字符存储状态save设置为FALSE,当前识别单词返回值currentToken设置为ERROR */
		            else
		            {
		                ungetNextChar();
		                save = false;
		                currentToken.Lex = LexType.ASSIGN;
		            }
	            break;
	            
	            case INCIN:

	            	/* 当前DFA状态state设置为完成状态DONE,赋值单词结束 */
	            	state = Global.StateType.DONE;				

		     		 /* 当前字符c为"=",当前识别单词返回值currentToken设置为赋值单词ASSIGN */
		     		if (c == '=')
		                currentToken.Lex = LexType.LE;

		     		else if (c == '>')
		                currentToken.Lex = LexType.NEQ;
		     		
		     		else if (c == '<')
		                currentToken.Lex = LexType.OUT;
	
		     		 /* 当前字符c为其它字符,在输入行缓冲区中回退一个字符       *
		     		  * 字符存储状态save设置为FALSE,当前识别单词返回值currentToken设置为ERROR */
		            else
		            {
		                ungetNextChar();
		                save = false;
		                currentToken.Lex = LexType.LT;
		            }
	            break;
	            
	            case INCOUT:

	            	/* 当前DFA状态state设置为完成状态DONE,赋值单词结束 */
	            	state = Global.StateType.DONE;				

		     		 /* 当前字符c为"=",当前识别单词返回值currentToken设置为赋值单词ASSIGN */
		     		if (c == '=')
		                currentToken.Lex = LexType.GE;

		     		else if (c == '>')
		                currentToken.Lex = LexType.IN;
	
		     		 /* 当前字符c为其它字符,在输入行缓冲区中回退一个字符       *
		     		  * 字符存储状态save设置为FALSE,当前识别单词返回值currentToken设置为ERROR */
		            else
		            {
		                ungetNextChar();
		                save = false;
		                currentToken.Lex = LexType.GT;
		            }
	            break;

	     	   /* 当前DFA状态state为注释状态INCOMMENT,确定性有限自动机DFA处于注释位置 */
	            case INCOMMENT:

	            	if(c == '*')
	            	{
		                save = false;
				        state = Global.StateType.COMBEGIN;
	            	}
	            	else
		            {
		                ungetNextChar();
			            state = Global.StateType.DONE;
		                currentToken.Lex = LexType.OVER;
		            }
	            break;

	            case COMBEGIN:

	       		 	/* 当前字符存储状态save设置为FALSE,注释中内容不生成单词,无需存储 */
	                save = false;				

		     		/* 当前字符c为EOF,当前DFA状态state设置为完成状态DONE,当前单词识别结束 *
		     		 * 当前识别单词返回值currentToken设置为文件结束单词ENDFILE      */
	            	if (c == '#')
		            {
		            	state = Global.StateType.DONE;
		                currentToken.Lex = LexType.ENDFILE;
		            }
	            	else if(c == '*')
		            	state = Global.StateType.COMEND;
	            	
	            break;
	            	
	            case COMEND:
	            	
	            	save = false;
	            	if (c == '#')
		            {
		            	state = Global.StateType.DONE;
		                currentToken.Lex = LexType.ENDFILE;
		            }
		     		/* 当前字符c为"}",注释结束.当前DFA状态state设置为开始状态START */
		            else if(c == '/')
		            	state = Global.StateType.START;
		            else
		            	state = Global.StateType.COMBEGIN;
	            break;
	            

	        	/*当前DFA状态state为字符标志状态INCHAR,确定有限自动机处于字符标志状态*/
	            case INCHAR:					

		             if (Character.isAlphabetic(c)||Character.isDigit(c))
		    		 { 
		            	 int c1=getNextChar();
		                 if (c1 =='\'')
		    			 {
		                	 save = true;
		                	 state = Global.StateType.DONE;
		                	 currentToken.Lex = LexType.CHARC;
		    			 }
		    		     else
		    			 {
		    		    	 ungetNextChar();
		                     ungetNextChar();
		                     state = Global.StateType.DONE;
		                     currentToken.Lex = LexType.ERROR;
		    				 Global.Error = true;
		    			 }
		    		 }
		    		 else
		    		 {      
		    			 ungetNextChar();
		                 state = Global.StateType.DONE;
		                 currentToken.Lex = LexType.ERROR;
		    			 Global.Error = true;
		    		 }
	    		break;
	            
	            /* 当前DFA状态state为完成状态DONE,确定性有限自动机DFA处于单词结束位置 */
	            case DONE:	break;

	     	    /* 当前DFA状态state为其它状态,此种情况不应发生 */
	            default:
	
	       		 /* 当前DFA状态state设置为完成状态DONE			*
	       		  * 当前识别单词返回值currentToken设置为错误单词ERROR*/
	                Global.Error = true;
	                state = Global.StateType.DONE;
	                currentToken.Lex = LexType.ERROR;
	                break;
	         }
	         /*************** 分类判断处理结束 *******************/
	       	 /* 当前字符存储状态save为TRUE,且当前正识别单词已经识别部分未超过单词最大长度 *
	       	  * 将当前字符c写入当前正识别单词词元存储区tokenString				*/
	         if (save)
	        	 tokenString += c;

       		 /* 当前单词currentToken为标识符单词类型,查看其是否为保留字单词 */
	       	 if (state == Global.StateType.DONE && currentToken.Lex == LexType.ID)
	       		 currentToken.Lex = global.reservedLookup(tokenString);
	      }
	      /**************** 循环处理结束 ********************/
	      /*将行号信息存入Token*/
	      currentToken.lineshow = Global.lineno;
	      /*将单词的语义信息存入Token*/
	      currentToken.Sem = tokenString;
	      /*将已处理完的当前Token存入链表的Token部分*/
	      tokenChain.add(Global.Tokennum, currentToken);
	      Global.Tokennum++;   /*Token总数目加1*/
	      LexType temp = currentToken.Lex;
	      currentToken = new Token();
	      currentToken.Lex = temp;
	    }
	    /* 直到处理完表示文件结束的Token:ENDFILE，说明处理完所有的Token*/
	    /* 并存入了链表中，循环结束*/
	    while ((currentToken.Lex)!=LexType.ENDFILE);
	    /*将由chainHead指向的Token链表存入文件"Tokenlist"中*/
	    ChainToFile();
	}
	
	public void run()
	{
		global.pw.println("------------------------- 源程序 --------------------------");
		global.pw.println();
		global.pw.flush();

		//创建一个词法分析类对象,需要 "路径名"+"文件名"+"后缀名"
		Object input = JOptionPane.showInputDialog(null, "请输入源文件路径名：", "BXC", 1,
				null, null, "F:\\mySource.bxc");
		if(input==null)
			System.exit(0);
		else
			try {reader = new BufferedReader(new FileReader(new File(input.toString())));}
			catch (FileNotFoundException e) 
			{JOptionPane.showMessageDialog(null, "路径名不正确，文件不存在！", "BXC", 2);System.exit(0);}
		
		getTokenlist();
	}
}