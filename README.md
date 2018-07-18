# C-like-Compiler
类C语言编译器demo。<br/>
定义的类C语言支持基本数据类型、类型定义、I/O、if语句、while循环、for循环、结构体、函数、注释等多种元素。<br/>
编译器实现了词法分析器、递归下降法语法分析器、LL(1)语法分析器、语义分析等功能。<br/><br/>
## 输入输出
输入文件：F:\mySource.bxc<br/>
输出文件：F:\LineList.txt 和 F:\TokenList.txt<br/>
## 编译器识别语言
### 总述

<p style="margin: 8px 0px 13.33px 24px; text-indent: 18pt;"><span style="margin: 0px; font-family: " 微软雅黑",sans-serif;"=""><font color="#595959" size="2">实验采用的<span lang="EN-US" style="margin: 0px;">BXC</span>语言为自行定义的模型语言，它是一种类<span lang="EN-US" style="margin: 0px;">C</span>的高级程序设计语言。<span lang="EN-US" style="margin: 0px;">BXC</span>语言的数据结构比较丰富，除了整型、字符型等简单数据类型外，还有数组、结构体等结构数据类型，函数不支持嵌套定义，但允许递归调用。事实上，<span lang="EN-US" style="margin: 0px;">BXC</span>语言基本上包含了高级程序设计语言的所有常用的成分。</font><span lang="EN-US" style="margin: 0px;"></span></span></p>

<p style="margin: 8px 0px 13.33px 24px; text-indent: 18pt;"><font size="2"><font color="#595959"><span lang="EN-US" style="margin: 0px; font-family: " 微软雅黑",sans-serif;"="">BXC</span><span style="margin: 0px; font-family: " 微软雅黑",sans-serif;"="">语言的一个特点为主函数必须是程序的最后一个函数，并且不能缺省。可以改进的方向有函数不必带返回值，检测<span lang="EN-US" style="margin: 0px;">return</span>语句使用的正确性，等等。<span lang="EN-US" style="margin: 0px;"></span></span></font></font></p>

### 举例

<p style="margin: 8px 0px 13.33px 24px; text-indent: 18pt;"><span style="margin: 0px; font-family: " 微软雅黑",sans-serif;"=""><font size="2"><font color="#595959">类型声明：<span lang="EN-US" style="margin: 0px;">typedef<span style="margin: 0px;">&nbsp; </span>TypeName <span style="margin: 0px;">&nbsp;</span>int ;</span></font></font></span></p>

<p style="margin: 8px 0px 13.33px 24px; text-indent: 18pt;"><span style="margin: 0px; font-family: " 微软雅黑",sans-serif;"=""><font size="2"><font color="#595959">数组声明：<span lang="EN-US" style="margin: 0px;">int[10]<span style="margin: 0px;">&nbsp; </span>VarName ;</span></font></font></span></p>

<p style="margin: 8px 0px 13.33px 24px; text-indent: 18pt;"><span style="margin: 0px; font-family: " 微软雅黑",sans-serif;"=""><font size="2"><font color="#595959">结构体声明：<span lang="EN-US" style="margin: 0px;">struct<span style="margin: 0px;">&nbsp; </span>StructName {</span></font></font></span></p>

<p style="margin: 8px 0px 13.33px 24px; text-indent: 18pt;"><font size="2"><font color="#595959"><span lang="EN-US" style="margin: 0px; font-family: " 微软雅黑",sans-serif;"=""><span style="margin: 0px;">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span>int<span style="margin: 0px;">&nbsp;
</span>VarName1 ;<span style="margin: 0px;">&nbsp; </span>/*</span><span style="margin: 0px; font-family: " 微软雅黑",sans-serif;"="">注释：变量声明<span lang="EN-US" style="margin: 0px;">*/</span></span></font></font></p>

<p style="margin: 8px 0px 13.33px 24px; text-indent: 18pt;"><span lang="EN-US" style="margin: 0px; font-family: " 微软雅黑",sans-serif;"=""><font size="2"><font color="#595959"><span style="margin: 0px;">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span>char VarName2 ;</font></font></span></p>

<p style="margin: 8px 0px 13.33px 24px; text-indent: 18pt;"><span lang="EN-US" style="margin: 0px; font-family: " 微软雅黑",sans-serif;"=""><font size="2"><font color="#595959"><span style="margin: 0px;">&nbsp; </span><span style="margin: 0px;">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
</span>}<span style="margin: 0px;">&nbsp; </span>StructVarName ;</font></font></span></p>

<p style="margin: 8px 0px 13.33px 24px; text-indent: 18pt;"><span style="margin: 0px; font-family: " 微软雅黑",sans-serif;"=""><font size="2"><font color="#595959">函数声明：<span lang="EN-US" style="margin: 0px;">int<span style="margin: 0px;">&nbsp; </span>main () { return 0 ; }<span style="margin: 0px;">&nbsp;&nbsp; </span>/* </span>返回语句<span lang="EN-US" style="margin: 0px;"> */</span></font></font></span></p>

<p style="margin: 8px 0px 13.33px 24px; text-indent: 18pt;"><font size="2"><font color="#595959"><span style="margin: 0px; font-family: " 微软雅黑",sans-serif;"="">输出语句：<span lang="EN-US" style="margin: 0px;">cout </span></span><span lang="EN-US" style="margin: 0px;"><font face="Constantia">&lt;&lt; </font></span><span lang="EN-US" style="margin: 0px; font-family: " 微软雅黑",sans-serif;"="">Expression
;<span style="margin: 0px;">&nbsp;&nbsp; </span></span><span style="margin: 0px; font-family: " 微软雅黑",sans-serif;"="">输入语句：<span lang="EN-US" style="margin: 0px;">cin </span></span><span lang="EN-US" style="margin: 0px;"><font face="Constantia">&gt;&gt;</font></span><span lang="EN-US" style="margin: 0px; font-family: " 微软雅黑",sans-serif;"="">
VarName ;</span></font></font></p>

## 语言的词法

### 字符表

<p style="margin: 8px 0px 13.33px 24px; text-indent: 18pt;"><font size="2"><font color="#595959"><span style="margin: 0px; font-family: 华文新魏;">字符表</span><font face="Constantia">
<span lang="EN-US" style="margin: 0px;"><span style="margin: 0px;">&nbsp;&nbsp;&nbsp;</span></span></font><span lang="EN-US" style="margin: 0px; font-family: Wingdings;"><span style="margin: 0px;">à</span></span><span lang="EN-US" style="margin: 0px; font-family: 宋体;"><span style="margin: 0px;">&nbsp; </span>a|b|c|d|e|f|g|h|i|j|k|l|m|n|o|p|q|r|s|t|u|v|w|x|y|z|</span></font></font></p>

<p style="margin: 8px 0px 13.33px 24px;"><span lang="EN-US" style="margin: 0px;"><font size="2"><font color="#595959"><font face="Constantia"><span style="margin: 0px;">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span><span style="margin: 0px;">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span>A|B|C|D|E|F|G|H|I|J|K|L|M|N|O|P|Q|R|S|T|U|V|W|X|Y|Z|</font></font></font></span></p>

<p style="margin: 8px 0px 13.33px 24px;"><span lang="EN-US" style="margin: 0px; font-family: 宋体;"><span style="margin: 0px;"> </span><font size="2"><font color="#595959"><span style="margin: 0px;">&nbsp;&nbsp;&nbsp; </span><span style="margin: 0px;">&nbsp;&nbsp;&nbsp; </span>0|1|2|3|4|5|6|7|8|9|</font></font></span></p>

<p style="margin: 8px 0px 13.33px 24px;"><font size="2"><font color="#595959"><span lang="EN-US" style="margin: 0px; font-family: 宋体;"><span style="margin: 0px;"> </span><span style="margin: 0px;">&nbsp;&nbsp;&nbsp; </span><span style="margin: 0px;">&nbsp;&nbsp;&nbsp; </span>+|-|*|/|&lt;|&gt;|=|(|)|[|]|{|}|.|,|;|'|EOF|</span><span style="margin: 0px; font-family: 宋体;">空白<span lang="EN-US" style="margin: 0px;"></span></span></font></font></p>

<p style="margin: 8px 0px 13.33px 24px; text-indent: 18pt;"><span style="margin: 0px; font-family: 宋体;"><font color="#595959" size="2">注：符号<span lang="EN-US" style="margin: 0px;">EOF</span>在程序中由符号<span lang="EN-US" style="margin: 0px;">#</span>实现；英文字母区分大小写；保留字只能由小写字母组成。</font><span lang="EN-US" style="margin: 0px;"></span></span></p>

## 语言的语法

### 文法符号

<div style="margin: 8px 0px 13.33px 48px; text-indent: 0cm;"><span style="margin: 0px; font-family: " 微软雅黑",sans-serif;"=""><font color="#595959" size="2"><span lang="EN-US" style="margin: 0px;">BXC</span>语言的<span lang="EN-US" style="margin: 0px;">LL</span>（<span lang="EN-US" style="margin: 0px;">1</span>）文法共有<span lang="EN-US" style="margin: 0px;">60</span>个非终极符。</font></span></div>

<p style="margin: 8px 0px 13.33px 48px;"><span style="margin: 0px; font-family: " 微软雅黑",sans-serif;"=""><font color="#595959" size="2">枚举类型<span lang="EN-US" style="margin: 0px;">LexType</span>中的<span lang="EN-US" style="margin: 0px;">ENDFILE</span>、<span lang="EN-US" style="margin: 0px;">ERROR</span>和<span lang="EN-US" style="margin: 0px;">END_POP</span>不是终极符。</font><span lang="EN-US" style="margin: 0px;"></span></span></p>

### 语法的形式定义

<p style="margin: 8px 0px 13.33px 24px; text-indent: 18pt;"><font size="2"><font color="#595959"><span style="margin: 0px; font-family: 华文新魏;"></span><span lang="EN-US" style="margin: 0px; font-family: " 微软雅黑",sans-serif;"="">LL(1)</span><span style="margin: 0px; font-family: 华文新魏;">文法在此文法上加以改进，把几乎所有不为产生式首个文法符号的终极符</span><span lang="EN-US" style="margin: 0px;"><font face="Constantia">ID</font></span><span style="margin: 0px; font-family: 华文新魏;">提取出来，单独作为一条产生式，目的是准确填入相应语法树节点的语义信息（</span><span lang="EN-US" style="margin: 0px;"><font face="Constantia">name</font></span><span style="margin: 0px; font-family: 华文新魏;">字段）。</span></font></font><span lang="EN-US" style="margin: 0px;"></span></p>

### Predict集合

<p style="margin: 8px 0px 13.33px 24px; text-indent: 18pt;"><font size="2"><font color="#595959"><span style="margin: 0px; font-family: 华文新魏;">进行递归下降法和</span><span lang="EN-US" style="margin: 0px; font-family: " 微软雅黑",sans-serif;"="">LL(1)</span><span style="margin: 0px; font-family: 华文新魏;">方法的语法分析，都需要用到</span><span lang="EN-US" style="margin: 0px;"><font face="Constantia">Predict</font></span><span style="margin: 0px; font-family: 华文新魏;">集，按照</span><span lang="EN-US" style="margin: 0px;"><font face="Constantia">BXC</font></span><span style="margin: 0px; font-family: 华文新魏;">的文法求得</span><span lang="EN-US" style="margin: 0px;"><font face="Constantia">Predict</font></span><span style="margin: 0px; font-family: 华文新魏;">集</span><span lang="EN-US" style="margin: 0px;"></span></span></font></font></p>

### LL(1)分析矩阵

<p style="margin: 8px 0px 13.33px 24px; text-indent: 18pt;"><font size="2"><font color="#595959"><span style="margin: 0px; font-family: 华文新魏;">分析矩阵的作用是帮助当前非终极符和当前输入符确定应该选择的语法规则，它的行对应非终极符，列对应终极符，矩阵的值有两种：一种是产生式编号，另外一种是错误编号。由于时间紧促，故</span><span lang="EN-US" style="margin: 0px;"><font face="Constantia">BXC</font></span><span style="margin: 0px; font-family: 华文新魏;">语言的</span><span lang="EN-US" style="margin: 0px; font-family: " 微软雅黑",sans-serif;"="">LL(1)</span><span style="margin: 0px; font-family: 华文新魏;">分析表的值只包含产生式编号信息。</span></font></font><span lang="EN-US" style="margin: 0px;"></span></p>

<p style="margin: 8px 0px 13.33px 48px; text-indent: 0cm;"><font size="2"><font color="#595959"><span lang="EN-US" style="margin: 0px; font-family: " 微软雅黑",sans-serif;"="">LL(1)</span><span style="margin: 0px; font-family: 华文新魏;">分析矩阵在程序中的实现方式为一个二维数组。行为非终极符，列为终极符</span><span lang="EN-US" style="margin: 0px;"><font face="Constantia"> + #</font></span><span style="margin: 0px; font-family: 华文新魏;">。</span></font></font><span lang="EN-US" style="margin: 0px;"></span></p>

<p style="margin: 8px 0px 13.33px 48px; text-indent: 0cm;"><font size="2"><font color="#595959"><span style="margin: 0px; font-family: " 微软雅黑",sans-serif;"="">注：</span><span lang="EN-US" style="margin: 0px;"><font face="Constantia"># </font></span><span style="margin: 0px; font-family: " 微软雅黑",sans-serif;"="">在程序中由枚举类型<span lang="EN-US" style="margin: 0px;">LexType</span>中的<span lang="EN-US" style="margin: 0px;">ENDFILE</span>实现。<span lang="EN-US" style="margin: 0px;"></span></span></font></font></p>

### 语法树节点的数据结构

<p style="margin: 8px 0px 13.33px 48px;"><font size="2"><font color="#595959"><span style="margin: 0px; font-family: 华文新魏;">为方便理解，语法树节点的数据结构借鉴了</span><span lang="EN-US" style="margin: 0px;"><font face="Constantia">SNL</font></span><span style="margin: 0px; font-family: 华文新魏;">（</span><span lang="EN-US" style="margin: 0px;"><font face="Constantia">Small Nested Language</font></span><span style="margin: 0px; font-family: 华文新魏;">）语言编译器。</span></font></font><span lang="EN-US" style="margin: 0px;"></span></p>

<p style="margin: 8px 0px 13.33px 48px;"><font size="2"><font color="#595959"><span style="margin: 0px; font-family: 华文新魏;">下表为</span><span lang="EN-US" style="margin: 0px;"><font face="Constantia">BXC</font></span><span style="margin: 0px; font-family: 华文新魏;">语言语法树节点的数据结构。</span></font></font><span lang="EN-US" style="margin: 0px;"></span></p>

<div align="center">

<table width="662" style="margin: 0px; border: medium; border-image: none; border-collapse: collapse;" border="1" cellspacing="0" cellpadding="0">
 <tbody><tr style="mso-yfti-irow:0;mso-yfti-firstrow:yes;height:3.2pt">
  <td width="76" valign="top" style="margin: 0px; padding: 0cm 5.4pt; border: 1.33px solid rgb(191, 191, 191); border-image: none; width: 56.75pt; height: 3.2pt; background-color: transparent;" colspan="4">
  <p align="center" style="margin: 8px 0px 0px; text-align: center; line-height: normal;"><span lang="EN-US" style="margin: 0px;"><font color="#595959" face="Constantia" size="2">child</font></span></p>
  </td>
  <td width="57" valign="top" style="border-width: 1.33px 1.33px 1.33px 0px; border-style: solid solid solid none; border-color: rgb(191, 191, 191) rgb(191, 191, 191) rgb(191, 191, 191) rgb(0, 0, 0); margin: 0px; padding: 0cm 5.4pt; border-image: none; width: 42.5pt; height: 3.2pt; background-color: transparent;" rowspan="2">
  <p align="center" style="margin: 8px 0px 0px; text-align: center; line-height: normal;"><span lang="EN-US" style="margin: 0px;"><font color="#595959" face="Constantia" size="2">sibling</font></span></p>
  </td>
  <td width="57" valign="top" style="border-width: 1.33px 1.33px 1.33px 0px; border-style: solid solid solid none; border-color: rgb(191, 191, 191) rgb(191, 191, 191) rgb(191, 191, 191) rgb(0, 0, 0); margin: 0px; padding: 0cm 5.4pt; border-image: none; width: 42.55pt; height: 3.2pt; background-color: transparent;" rowspan="2">
  <p style="margin: 8px 0px 0px; line-height: normal;"><span lang="EN-US" style="margin: 0px;"><font color="#595959" face="Constantia" size="2">lineno</font></span></p>
  </td>
  <td width="76" valign="top" style="border-width: 1.33px 1.33px 1.33px 0px; border-style: solid solid solid none; border-color: rgb(191, 191, 191) rgb(191, 191, 191) rgb(191, 191, 191) rgb(0, 0, 0); margin: 0px; padding: 0cm 5.4pt; border-image: none; width: 2cm; height: 3.2pt; background-color: transparent;" rowspan="2">
  <p style="margin: 8px 0px 0px; line-height: normal;"><span lang="EN-US" style="margin: 0px;"><font color="#595959" face="Constantia" size="2">nodekind</font></span></p>
  </td>
  <td width="123" valign="top" style="border-width: 1.33px 1.33px 1.33px 0px; border-style: solid solid solid none; border-color: rgb(191, 191, 191) rgb(191, 191, 191) rgb(191, 191, 191) rgb(0, 0, 0); margin: 0px; padding: 0cm 5.4pt; border-image: none; width: 92.15pt; height: 3.2pt; background-color: transparent;" colspan="3">
  <p align="center" style="margin: 8px 0px 0px; text-align: center; line-height: normal;"><span lang="EN-US" style="margin: 0px;"><font color="#595959" face="Constantia" size="2">kind</font></span></p>
  </td>
  <td width="57" valign="top" style="border-width: 1.33px 1.33px 1.33px 0px; border-style: solid solid solid none; border-color: rgb(191, 191, 191) rgb(191, 191, 191) rgb(191, 191, 191) rgb(0, 0, 0); margin: 0px; padding: 0cm 5.4pt; border-image: none; width: 42.5pt; height: 3.2pt; background-color: transparent;" rowspan="2">
  <p style="margin: 8px 0px 0px; line-height: normal;"><span lang="EN-US" style="margin: 0px;"><font color="#595959" face="Constantia" size="2">idnum</font></span></p>
  </td>
  <td width="47" valign="top" style="border-width: 1.33px 1.33px 1.33px 0px; border-style: solid solid solid none; border-color: rgb(191, 191, 191) rgb(191, 191, 191) rgb(191, 191, 191) rgb(0, 0, 0); margin: 0px; padding: 0cm 5.4pt; border-image: none; width: 35.45pt; height: 3.2pt; background-color: transparent;" rowspan="2">
  <p style="margin: 8px 0px 0px; line-height: normal;"><span lang="EN-US" style="margin: 0px;"><font color="#595959" face="Constantia" size="2">name</font></span></p>
  </td>
  <td width="47" valign="top" style="border-width: 1.33px 1.33px 1.33px 0px; border-style: solid solid solid none; border-color: rgb(191, 191, 191) rgb(191, 191, 191) rgb(191, 191, 191) rgb(0, 0, 0); margin: 0px; padding: 0cm 5.4pt; border-image: none; width: 35.45pt; height: 3.2pt; background-color: transparent;" rowspan="2">
  <p style="margin: 8px 0px 0px; line-height: normal;"><span lang="EN-US" style="margin: 0px;"><font color="#595959" face="Constantia" size="2">table</font></span></p>
  </td>
  <td width="123" valign="top" style="border-width: 1.33px 1.33px 1.33px 0px; border-style: solid solid solid none; border-color: rgb(191, 191, 191) rgb(191, 191, 191) rgb(191, 191, 191) rgb(0, 0, 0); margin: 0px; padding: 0cm 5.4pt; border-image: none; width: 92.3pt; height: 3.2pt; background-color: transparent;" colspan="2">
  <p align="center" style="margin: 8px 0px 0px; text-align: center; line-height: normal;"><span lang="EN-US" style="margin: 0px;"><font color="#595959" face="Constantia" size="2">attr</font></span></p>
  </td>
 </tr>
 <tr style="mso-yfti-irow:1;mso-yfti-lastrow:yes;height:13.5pt">
  <td width="19" valign="top" style="border-width: 0px 1.33px 1.33px; border-style: none solid solid; border-color: rgb(0, 0, 0) rgb(191, 191, 191) rgb(191, 191, 191); margin: 0px; padding: 0cm 5.4pt; border-image: none; width: 14.2pt; height: 13.5pt; background-color: transparent;">
  <p style="margin: 8px 0px 0px; line-height: normal;"><span lang="EN-US" style="margin: 0px;"><font color="#595959" face="Constantia" size="2">0</font></span></p>
  </td>
  <td width="19" valign="top" style="border-width: 0px 1.33px 1.33px 0px; border-style: none solid solid none; border-color: rgb(0, 0, 0) rgb(191, 191, 191) rgb(191, 191, 191) rgb(0, 0, 0); margin: 0px; padding: 0cm 5.4pt; width: 14.2pt; height: 13.5pt; background-color: transparent;">
  <p style="margin: 8px 0px 0px; line-height: normal;"><span lang="EN-US" style="margin: 0px;"><font color="#595959" face="Constantia" size="2">1</font></span></p>
  </td>
  <td width="19" valign="top" style="border-width: 0px 1.33px 1.33px 0px; border-style: none solid solid none; border-color: rgb(0, 0, 0) rgb(191, 191, 191) rgb(191, 191, 191) rgb(0, 0, 0); margin: 0px; padding: 0cm 5.4pt; width: 14.15pt; height: 13.5pt; background-color: transparent;">
  <p style="margin: 8px 0px 0px; line-height: normal;"><span lang="EN-US" style="margin: 0px;"><font color="#595959" face="Constantia" size="2">2</font></span></p>
  </td>
  <td width="19" valign="top" style="border-width: 0px 1.33px 1.33px 0px; border-style: none solid solid none; border-color: rgb(0, 0, 0) rgb(191, 191, 191) rgb(191, 191, 191) rgb(0, 0, 0); margin: 0px; padding: 0cm 5.4pt; width: 14.2pt; height: 13.5pt; background-color: transparent;">
  <p style="margin: 8px 0px 0px; line-height: normal;"><span lang="EN-US" style="margin: 0px;"><font color="#595959" face="Constantia" size="2">3</font></span></p>
  </td>
  <td width="38" valign="top" style="border-width: 0px 1.33px 1.33px 0px; border-style: none solid solid none; border-color: rgb(0, 0, 0) rgb(191, 191, 191) rgb(191, 191, 191) rgb(0, 0, 0); margin: 0px; padding: 0cm 5.4pt; width: 1cm; height: 13.5pt; background-color: transparent;">
  <p style="margin: 8px 0px 0px; line-height: normal;"><span lang="EN-US" style="margin: 0px;"><font color="#595959" face="Constantia" size="2">dec</font></span></p>
  </td>
  <td width="44" valign="top" style="border-width: 0px 1.33px 1.33px 0px; border-style: none solid solid none; border-color: rgb(0, 0, 0) rgb(191, 191, 191) rgb(191, 191, 191) rgb(0, 0, 0); margin: 0px; padding: 0cm 5.4pt; width: 33.05pt; height: 13.5pt; background-color: transparent;">
  <p style="margin: 8px 0px 0px; line-height: normal;"><span lang="EN-US" style="margin: 0px;"><font color="#595959" face="Constantia" size="2">stmt</font></span></p>
  </td>
  <td width="41" valign="top" style="border-width: 0px 1.33px 1.33px 0px; border-style: none solid solid none; border-color: rgb(0, 0, 0) rgb(191, 191, 191) rgb(191, 191, 191) rgb(0, 0, 0); margin: 0px; padding: 0cm 5.4pt; width: 30.75pt; height: 13.5pt; background-color: transparent;">
  <p style="margin: 8px 0px 0px; line-height: normal;"><span lang="EN-US" style="margin: 0px;"><font color="#595959" face="Constantia" size="2">exp</font></span></p>
  </td>
  <td width="85" valign="top" style="border-width: 0px 1.33px 1.33px 0px; border-style: none solid solid none; border-color: rgb(0, 0, 0) rgb(191, 191, 191) rgb(191, 191, 191) rgb(0, 0, 0); margin: 0px; padding: 0cm 5.4pt; width: 63.75pt; height: 13.5pt; background-color: transparent;">
  <p style="margin: 8px 0px 0px; line-height: normal;"><span lang="EN-US" style="margin: 0px;"><font color="#595959" face="Constantia" size="2">type_name</font></span></p>
  </td>
  <td width="38" valign="top" style="border-width: 0px 1.33px 1.33px 0px; border-style: none solid solid none; border-color: rgb(0, 0, 0) rgb(191, 191, 191) rgb(191, 191, 191) rgb(0, 0, 0); margin: 0px; padding: 0cm 5.4pt; width: 28.55pt; height: 13.5pt; background-color: transparent;">
  <p align="center" style="margin: 8px 0px 0px; text-align: center; line-height: normal;"><span lang="EN-US" style="margin: 0px;"><font color="#595959" face="Constantia" size="2">…</font></span></p>
  </td>
 </tr>
 
 <tr height="0">
  <td width="22" style="margin: 0px; border: 0px rgb(0, 0, 0); border-image: none; background-color: transparent;"></td>
  <td width="19" style="margin: 0px; border: 0px rgb(0, 0, 0); border-image: none; background-color: transparent;"></td>
  <td width="21" style="margin: 0px; border: 0px rgb(0, 0, 0); border-image: none; background-color: transparent;"></td>
  <td width="20" style="margin: 0px; border: 0px rgb(0, 0, 0); border-image: none; background-color: transparent;"></td>
  <td width="56" style="margin: 0px; border: 0px rgb(0, 0, 0); border-image: none; background-color: transparent;"></td>
  <td width="56" style="margin: 0px; border: 0px rgb(0, 0, 0); border-image: none; background-color: transparent;"></td>
  <td width="75" style="margin: 0px; border: 0px rgb(0, 0, 0); border-image: none; background-color: transparent;"></td>
  <td width="37" style="margin: 0px; border: 0px rgb(0, 0, 0); border-image: none; background-color: transparent;"></td>
  <td width="44" style="margin: 0px; border: 0px rgb(0, 0, 0); border-image: none; background-color: transparent;"></td>
  <td width="40" style="margin: 0px; border: 0px rgb(0, 0, 0); border-image: none; background-color: transparent;"></td>
  <td width="56" style="margin: 0px; border: 0px rgb(0, 0, 0); border-image: none; background-color: transparent;"></td>
  <td width="47" style="margin: 0px; border: 0px rgb(0, 0, 0); border-image: none; background-color: transparent;"></td>
  <td width="47" style="margin: 0px; border: 0px rgb(0, 0, 0); border-image: none; background-color: transparent;"></td>
  <td width="84" style="margin: 0px; border: 0px rgb(0, 0, 0); border-image: none; background-color: transparent;"></td>
  <td width="37" style="margin: 0px; border: 0px rgb(0, 0, 0); border-image: none; background-color: transparent;"></td>
 </tr>
 
</tbody></table>

</div>

<p style="margin: 8px 0px 13.33px 48px; text-indent: 0cm;"><font size="2"><font color="#595959"><span style="margin: 0px; font-family: 华文新魏;">注：</span><span lang="EN-US" style="margin: 0px;"><font face="Constantia">Java</font></span><span style="margin: 0px; font-family: 华文新魏;">中没有共用体，故成员</span><span lang="EN-US" style="margin: 0px;"><font face="Constantia">kind</font></span><span style="margin: 0px; font-family: 华文新魏;">以及</span><span lang="EN-US" style="margin: 0px;"><font face="Constantia">attr</font></span><span style="margin: 0px; font-family: 华文新魏;">使用类结构。</span><span lang="EN-US" style="margin: 0px;"><font face="Constantia">attr</font></span><span style="margin: 0px; font-family: 华文新魏;">的内部成员采用了内部类结构。</span></font></font><span lang="EN-US" style="margin: 0px;"></span></p>

## BXC语言的语义

### 符号表的组织

<p style="margin: 8px 0px 13.33px 72px; text-indent: -18pt;"><font color="#595959"><span lang="EN-US" style="margin: 0px;"><span style="margin: 0px;"><font face="Constantia" size="2">1）</font><span style="font: 7pt " normal;"="" none;="" 0px;="" roman";="" new="" times="">&nbsp;&nbsp;
</span></span></span><span style="margin: 0px; font-family: 华文新魏;"><font size="2">分层建立符号表，使各分程序的符号表项连续地排列在一起，不会被内层分程序的符号表所割裂。</font></span></font><span lang="EN-US" style="margin: 0px;"></span></p>

<p style="margin: 8px 0px 13.33px 72px; text-indent: -18pt;"><font color="#595959"><span lang="EN-US" style="margin: 0px;"><span style="margin: 0px;"><font face="Constantia" size="2">2）</font><span style="font: 7pt " normal;"="" none;="" 0px;="" roman";="" new="" times="">&nbsp; </span></span></span><span style="margin: 0px; font-family: 华文新魏;"><font size="2">建立一个分程序表，用来记录各层分程序符号表的起始位置。</font></span></font><span lang="EN-US" style="margin: 0px;"></span></p>

<p style="margin: 8px 0px 13.33px 72px; text-indent: -18pt;"><font color="#595959"><span lang="EN-US" style="margin: 0px;"><span style="margin: 0px;"><font face="Constantia" size="2">3）</font><span style="font: 7pt " normal;"="" none;="" 0px;="" roman";="" new="" times="">&nbsp;&nbsp;
</span></span></span><span style="margin: 0px; font-family: 华文新魏;"><font size="2">由于不允许函数的嵌套声明，故符号表只能有两层。</font></span></font><span lang="EN-US" style="margin: 0px;"></span></p>

<p style="margin: 8px 0px 13.33px 72px; text-indent: -18pt;"><font color="#595959"><span lang="EN-US" style="margin: 0px;"><span style="margin: 0px;"><font face="Constantia" size="2">4）</font><span style="font: 7pt " normal;"="" none;="" 0px;="" roman";="" new="" times="">&nbsp; </span></span></span><span style="margin: 0px; font-family: 华文新魏;"><font size="2">分层打印符号表。对每一个结构体类型的声明，都需要打印域表。</font></span></font><span lang="EN-US" style="margin: 0px;"></span></p>

### 符号表的实现

<p style="margin: 8px 0px 13.33px; text-indent: 36pt;"><font size="2"><font color="#595959"><span style="margin: 0px; font-family: 华文新魏;">分程序表用</span><span lang="EN-US" style="margin: 0px;"><font face="Constantia">scope</font></span><span style="margin: 0px; font-family: 华文新魏;">栈实现。</span><span lang="EN-US" style="margin: 0px;"><font face="Constantia">scope</font></span><span style="margin: 0px; font-family: 华文新魏;">栈的声明如下：</span></font></font><span lang="EN-US" style="margin: 0px;"></span></p>

<p style="margin: 0px; line-height: normal;"><span lang="EN-US" style="margin: 0px; color: rgb(63, 127, 95); font-family: Consolas; font-size: 9pt;">/* </span><span style="margin: 0px; color: rgb(63, 127, 95); font-family: 华文新魏; font-size: 9pt;">使用</span><span lang="EN-US" style="margin: 0px; color: rgb(63, 127, 95); font-family: Consolas; font-size: 9pt;">scope</span><span style="margin: 0px; color: rgb(63, 127, 95); font-family: 华文新魏; font-size: 9pt;">栈的局部符号表方法中所用到的</span><span lang="EN-US" style="margin: 0px; color: rgb(63, 127, 95); font-family: Consolas; font-size: 9pt;">scope</span><span style="margin: 0px; color: rgb(63, 127, 95); font-family: 华文新魏; font-size: 9pt;">栈</span><span lang="EN-US" style="margin: 0px; color: rgb(63, 127, 95); font-family: Consolas; font-size: 9pt;"> */</span><span lang="EN-US" style="margin: 0px; font-family: Consolas; font-size: 9pt;"></span></p>

<p style="margin: 8px 0px 13.33px;"><b><span lang="EN-US" style="margin: 0px; color: rgb(127, 0, 85); line-height: 110%; font-family: Consolas; font-size: 9pt;">public</span></b><span lang="EN-US" style="margin: 0px; color: black; line-height: 110%; font-family: Consolas; font-size: 9pt;"> ArrayList&lt;ArrayList&lt;SymbTable&gt;&gt;
</span><span lang="EN-US" style="margin: 0px; color: rgb(0, 0, 192); line-height: 110%; font-family: Consolas; font-size: 9pt;">scope</span><span lang="EN-US" style="margin: 0px; color: black; line-height: 110%; font-family: Consolas; font-size: 9pt;"> = </span><b><span lang="EN-US" style="margin: 0px; color: rgb(127, 0, 85); line-height: 110%; font-family: Consolas; font-size: 9pt;">new</span></b><span lang="EN-US" style="margin: 0px; color: black; line-height: 110%; font-family: Consolas; font-size: 9pt;">
ArrayList&lt;ArrayList&lt;SymbTable&gt;&gt;();</span></p>

<p style="margin: 8px 0px 13.33px;"><font size="2"><font color="#595959"><span lang="EN-US" style="margin: 0px;"><font face="Constantia"><span style="margin: 0px;">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </span>scope</font></span><span style="margin: 0px; font-family: 华文新魏;">栈相当于一个数组，数组的元素是</span><span lang="EN-US" style="margin: 0px;"><font face="Constantia">ArrayList&lt;SymbTable&gt;</font></span><span style="margin: 0px; font-family: 华文新魏;">，也就是符号表。</span></font></font><span lang="EN-US" style="margin: 0px;"></span></p>

<p style="margin: 8px 0px 13.33px;"><font size="2"><font color="#595959"><span lang="EN-US" style="margin: 0px;"><span style="margin: 0px;"><font face="Constantia">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; </font></span></span><span style="margin: 0px; font-family: 华文新魏;">符号表采用顺序表的数据结构存储，节点是</span><span lang="EN-US" style="margin: 0px;"><font face="Constantia">SymbTable</font></span><span style="margin: 0px; font-family: 华文新魏;">类的对象。</span></font></font><span lang="EN-US" style="margin: 0px;"></span></p>

## 主函数算法框图

<p style="margin: 8px 0px 13.33px; text-indent: 36pt;"><font size="2"><font color="#595959"><span style="margin: 0px; font-family: 华文新魏;">注：需要在语义分析开始前，检测语法树的最后一个函数是否为</span><span lang="EN-US" style="margin: 0px;"><font face="Constantia">main</font></span><span style="margin: 0px; font-family: 华文新魏;">函数。</span></font></font><span lang="EN-US" style="margin: 0px; line-height: 110%; font-family: " yahei="" microsoft="" ui",sans-serif;"=""></span></p>

