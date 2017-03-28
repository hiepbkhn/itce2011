
/*//实际上是一个二叉树的定义
//用二叉树表示森林，左节点表示孩子，由节点表示兄弟，并且右节点的id值一定大于其该节点的id值
struct TNode
{
   int mo_id;
   TNode *lchild, *rsibling;
};

class Bi_Tree
{
public:
	TNode *root;

public:
	Bi_Tree();
	~Bi_Tree();
	//初始化一个树节点
	TNode* InitTNode();
	TNode* InitTNode(int id, TNode* m_lchild, TNode* m_rsibling);
	//插入一个某节点的
};

Bi_Tree::Bi_Tree()
{
  root=new TNode();
  root->mo_id=0;
  root->lchild=0;
  root->rsibling=0;
};

Bi_Tree::~Bi_Tree()
{
  delete root;
};

TNode* Bi_Tree::InitTNode()
{
   TNode* node=new TNode;
   return node;
};

TNode* Bi_Tree::InitTNode(int id, TNode *m_lchild, TNode *m_rsibling)
{
    TNode* node=new TNode;

	node->mo_id=id;
	node->lchild=m_lchild;
	node->rsibling=m_rsibling;

	return node;
}
*/


/*  vector<int> canCR;
	//5.1寻找ms所在的clique，并按照clique_size降序排序
  //如果有索引的话，这里直接从索引中获取ms所在的团，就不用一个一个扫描的找了
  for(int i=0;i< CRSet.size();i++)
  {
	 if(CRSet[i].test(ms.id))
	 {
		//说明ms在CRSet[i]所表示的团中
		if(canCR.size()==0)
		 //找到的第一个包含ms的团
			canCR.push_back(i);
		else
		{
			//降序排序。///这段代码尚未运行到？？？？
			 int k=0;
			 while(k<canCR.size())
			 {
				
				   //重新写的排序插入 修改于整理程序时。原来的插入代码是上面注释的部分
				   if(CRSet[i].count()<CRSet[canCR[k]].count())
				   {
					   if(k==canCR.size()-1)
					   {
				         canCR.push_back(i);  
					     break;
					   }
					   else
						   k++;
				   }
				   else
				   {
				      canCR.insert(canCR.begin()+k,i);
					  break;
				   }//？ if(CRSet[i].count()<CRSet[canCR[k]].count())
				
			 }//?while(k<canCR.size())
			
		}
	 }//？if(CRSet[i].test(ms.id))
  }//？for(int i=0;i<CRSet.size();i++)

  //5.2从canCR中一次提取候选团，判断是否可匿名
  bool goOn=true;
  //int CRid;
  for(int i=0;i<canCR.size()&&goOn;i++)
  {
	//确定canCR[i]号团中的最大k与最小k
	  int max_k=0,min_k=MaxPrivacyLevel;
	  for(int j=0;j<LoadMoNumber;j++)
	  {
	 //依次判断位串CRSet[canCR[i]]中第几位为1
		  if(CRSet[canCR[i]].test(j))
		  {
			//此位为1
			  map<int,MO> ::iterator it=moSet.find(j);
			  if(max_k<(*it).second.k) max_k=(*it).second.k;
			  if(min_k>(*it).second.k) min_k=(*it).second.k;
		  }
	  }//? for(int j=0;j<LoadMoNumber;j++)
  
	  //判断是什么类型的candidate
	  if(CRSet[canCR[i]].count()>=max_k)
	  {
		//positive candidate
		  goOn=false;
		  //CRid=canCR[i];
		  //给CR_mo_id赋值
		  for(int j=0;j<LoadMoNumber;j++)
		  {
			if(CRSet[canCR[i]].test(j)) CR_mo_id.push_back(j);

		  }
	  }
	  else
		  if(CRSet[canCR[i]].count()<maxNN(ms.k,min_k))
		   //Not the candidate clique
		  {
		   //CRid=canCR[i].size();
			  goOn=false;
			  CR_mo_id.clear();//表示没有
		  }
		  else
		  {
			  if((CRSet[canCR[i]].count()==maxNN(ms.k,min_k))&&(CRSet[canCR[i]].count()<max_k))
			  {
				//Not the candidate clique
				goOn=false;
				CR_mo_id.clear();//表示没有
			  }
			  else
			  {
				 //negative candidate clique
				 vector<int> moNeg; //用以存放在此candidate clique中的所有mo的id
				 for(int j=0;j<LoadMoNumber;j++)
				 {
				   if(CRSet[canCR[i]].test(j))
				   {
					 if(moNeg.size()==0)
						 //moNeg.push_back(canCR[i]);
						 moNeg.push_back(j);
					 else
					 {
						 //降序
						 int k=0;
						 map<int,MO> ::iterator it_key=moSet.find(j);
						 while(k<moNeg.size())
						 {
							map<int,MO> ::iterator it_k=moSet.find(moNeg[k]);
							if((k==0)&&((*it_key).second.k>(*it_k).second.k))
							{
								moNeg.insert(moNeg.begin(),j);
								break;
							}
							else
							   if((*it_key).second.k<(*it_k).second.k)
							   {
									if(k==moNeg.size()-1)
										//说明要在尾部加入此it_key
									{
										moNeg.push_back(j);
										break;
									}
									k++;
							   }
							   else
							   {
								   //说明(*it_key).second.k>=(*it_k).second.k,即在找到的第一个比key值小的位置前插入
									moNeg.insert(moNeg.begin()+k,j);
									break;
							   }
						  }//?while(k<moNeg.size())
					 }//?if(moNeg.size()==0) else
				   }//?if(CRSet[canCR[i]].test(j))
				 }//?for(int j=0;j<LoadMoNumber;j++)

				 //将negative-〉positive
				 int j=0;
				 while(j<moNeg.size())
				 //for(int j=0;j<moNeg.size();j++)
				 {
					 map<int,MO> ::iterator mo_max_k=moSet.find(moNeg[j]);
					 if(moNeg.size()>=(*mo_max_k).second.k)
					 {
					   //剩下的mo可以作为匿名集返回
						 for(j;j<moNeg.size();j++)
							 CR_mo_id.push_back(moNeg[j]);
						 goOn=false;
					 }
					 else
					 {
					   //将造成negative的mo去掉
						 if(j==moNeg.size()-1)
							 //若是末尾元素，则对整个序列的顺序不会有影响
							 moNeg.pop_back();
						 else
						 {
							 moNeg.erase(moNeg.begin()+j);
						 }
					   
					 }
				 }//?for(int j=0;j<moNeg.size();j++)
			  }//?if((CRSet[canCR[i]].count()==maxNN(ms.k,min_k))&&(CRSet[canCR[i]].count()<max_k))
	        
		  }//?if(CRSet[canCR[i]].size()<maxNN(ms.k,min_k)) else
	       
  }//?for(int i=0;i<canCR.size()&&goOn;i++)

  //5.3 找到匿名集后，将匿名集所有用户匿名返回。这一步实际上在上面给CR_mo_id赋值的时候已经完成了
  if(CR_mo_id.size()>0)
  {
  wFileStream.open("D:\\Experiments\\MMB\\Data\\Result.txt",ios::app);

   //记录哪些对象匿名成功
  wFileStream<<"The number of cloaked successfully objects are "<<endl;
  wFileStream<<CR_mo_id.size()<<endl;
  wFileStream<<"MOs which are anonymized successfully are:"<<endl;
 
  succTime=clock();

  for(int i=0;i<CR_mo_id.size();i++)
  {
	 wFileStream<<CR_mo_id[i]<<" ";//<<endl;
	 map<int,MO>::iterator mo_temp=moSet.find(CR_mo_id[i]);
	 wFileStream<<"Timestamp is"<<" "<<(*mo_temp).second.timestamp<<endl;
	 wFileStream<<"Process time for this request require:"<<(double)(succTime-(*mo_temp).second.enTime)/CLOCKS_PER_SEC<<endl;
	 //计算total process time of all successful request 
	 TotalSuccTime=TotalSuccTime+(double)(succTime-(*mo_temp).second.enTime)/CLOCKS_PER_SEC;

	 //调试程序用
      //wFileStream<<"Previous ID is"<<" "<<(*mo_temp).second.preId<<endl;
	 //调试程序用
  }
  //记录哪些对象匿名成功
  successCount=successCount+CR_mo_id.size();

  wFileStream<<"A single request anonymized successfully require"<<endl;
  wFileStream<<(double)(succTime-inTime)/CLOCKS_PER_SEC<<" s"<<endl;

  //wFileStream<<(double)(succTime-inTime)/(CLOCKS_PER_SEC / (double) 1000.0)<<" ms"<<endl;
  wFileStream<<"==========================================="<<endl;
  wFileStream.close();
  }//?if(CR_mo_id.size()>0)  有时才输入，省得麻烦
}//?if(vNr_temp.size()==1&&vNr_temp[0]==ms.id)

//6.匿名成功的mo和已经过期的mo leave system
//6.1从系统中去掉匿名成功的元素
if(CR_mo_id.size()>0)
{
 //说明有匿名成功的元素,需要把其从系统中去除
   //依次提取匿名成功的mo的id
   map<int,MO>::iterator it_mo;
   Rect mo_MBR;
   for(int i=0;i<CR_mo_id.size();i++)
   {
	  //6.0将匿名成功的元素放入到moCloakedSet中
		  //6.0.1确定最小边界矩形MBR
	   if(i==0)
	   {
		   mo_MBR.min[0]=moSet[CR_mo_id[i]].x;
		   mo_MBR.min[1]=moSet[CR_mo_id[i]].y;
		   mo_MBR.max[0]=moSet[CR_mo_id[i]].x;
		   mo_MBR.max[1]=moSet[CR_mo_id[i]].y;
	   }
	   else
	   {
		   if(mo_MBR.min[0]>moSet[CR_mo_id[i]].x)
			   mo_MBR.min[0]=moSet[CR_mo_id[i]].x;
		   if(mo_MBR.min[1]>moSet[CR_mo_id[i]].y)
			   mo_MBR.min[1]=moSet[CR_mo_id[i]].y;
		   if(mo_MBR.max[0]<moSet[CR_mo_id[i]].x)
			   mo_MBR.max[0]=moSet[CR_mo_id[i]].x;
		   if(mo_MBR.max[1]<moSet[CR_mo_id[i]].y)
			   mo_MBR.max[1]=moSet[CR_mo_id[i]].y;
	   }
		   //6.0.2j将mo放入moCloakedSet中 
	   it_mo=moSet.find(CR_mo_id[i]);
	   //匿名成功的时刻=触发匿名成功的对象的时间戳,匿名所需时间等待时间等实际上已经包含在触发匿名对象的时间戳与该对象提出的时间戳的差当中
       //(*it_mo).second.preT=(double)(succTime-inTime)/CLOCKS_PER_SEC+ms.timestamp;
	   (*it_mo).second.preT=ms.timestamp;
	   //(*it_mo).second.preT=(double)(succTime-inTime)/CLOCKS_PER_SEC+(*it_mo).second.timestamp;
	   //将匿名的时间忽略
	   //(*it_mo).second.preT=(*it_mo).second.timestamp;
	   moCloakedSet.insert(pairInsert((*it_mo).first,(*it_mo).second));
	   
	  //6.1.1从heap中去除成功匿名的元素
		 //找出它对应于heap中的即将过期时间
	  double delTime=heap_id_key[CR_mo_id[i]];
		 //从map heap_id_key中将CR_mo_id[i]去除
	  int j;
	  for(j=0;j<heap_key.size();j++)
	  {
		if(abs(heap_key[j]-delTime)<=1e-5)
			break;
	  }
	 
	  double delete_temp;
	  delete_temp=heap_key[j];
	  heap_key[j]=heap_key[heap_key.size()-1];
	  heap_key[heap_key.size()-1]=delete_temp;
	  heap_key.pop_back();
	  //heap_key.erase(it);
		//从heap_id_key中将对应的id与时间清除
	  heap_id_key.erase(CR_mo_id[i]);

	   //6.1.2从R-tree中去除
	  rect.min[0]=moSet[CR_mo_id[i]].x;
	  rect.max[0]=moSet[CR_mo_id[i]].x;
	  rect.min[1]=moSet[CR_mo_id[i]].y;
	  rect.max[1]=moSet[CR_mo_id[i]].y;
	  if(rtree.Remove(rect.min,rect.max,CR_mo_id[i]))
	  {
	     //调试程序用
		  cout<<"Failed to delete node from rtree,please enter something"<<endl;
		  char pp[10];
		  cin>>pp;
	  }

	   //从CRset中去除
	   //原则上需要判断修改后的团是否还是maximal，但是这里我先不判断。等消除掉所有的已过期的mo后，在批量处理判断CR_mo_id中的clique是否maximal
	   //for(int j=0;j<CRSet.size();j++)
	  j=0;
	  while(j<CRSet.size())
	  {
		   if(CRSet[j].test(CR_mo_id[i]))
		   {
			   CRSet[j].reset(CR_mo_id[i]);
		       //changed by 2008-2-24
			   if(!maintainMaximalClique(CRSet,CRSet[j]))
			   {
			     //CRSet[j]不再极大，则从CRSet中将其去除
                 CRSet[j]=CRSet[CRSet.size()-1];
				 CRSet.pop_back();
			   }
			   else
				   j++;
		   }
		   else
			   j++;

	   }

	   //从Moset中去除
		   moSet.erase(CR_mo_id[i]);
   }//?for(int i=0;i<CR_mo_id.size();i++)

   //将CR_mo_id清零前，设置所有匿名框中所有用户的MBR
	for(int i=0;i<CR_mo_id.size();i++)
   {
	  it_mo=moCloakedSet.find(CR_mo_id[i]);
	  (*it_mo).second.preCR=mo_MBR;
   }
}//？if(CR_mo_id.size()>0)

//将CR_mo_id清零
CR_mo_id.clear();


outTime=clock();
//outTime-inTime是ms该request处理的时间
durationTime=(double)(outTime-inTime)/CLOCKS_PER_SEC;
//durationTime=(double)(outTime-inTime)/(CLOCKS_PER_SEC / (double) 1000.0);

//6.2从系统的数据结构中去掉过期的元素

//从heap中取出已过期的元素
//更新heap中的元素的待过期时间
map<int,double>::iterator it;



vector<int> expire_id;
for(it=heap_id_key.begin();it!=heap_id_key.end();it++)
{
//(*it).second=(*it).second-durationTime;//需要更新
//maximal_clique_maintaining_time是在上一次匿名处理后，维护CRSet所使用的时间
(*it).second=(*it).second-durationTime-maximal_clique_maintaining_time; //changed in 2008-2-22
moSet[(*it).first].deltT=(*it).second;//更新moSet中相应的寿命
if((*it).second<0)
   //说明过期
{
   expire_id.push_back((*it).first);
}
}//？for(it=heap_id_key.begin();it!=heap_id_key.end();it++)



//输出过期的mo
if(expire_id.size()>0)
{
wFileStream.open("D:\\Experiments\\MMB\\Data\\Result.txt",ios::app);
wFileStream<<"The number of expired objects are "<<endl;
wFileStream<<expire_id.size()<<endl;
wFileStream<<"MOs which expire are :"<<endl;
}//?if(expire_id_size()>0)

for(int i=0;i<expire_id.size();i++)  
{
//从map heap_id_key中去除掉一过期的mo id
heap_id_key.erase(expire_id[i]);

//从R-tree中去除
rect.min[0]=moSet[expire_id[i]].x;
rect.max[0]=moSet[expire_id[i]].x;
rect.min[1]=moSet[expire_id[i]].y;
rect.max[1]=moSet[expire_id[i]].y;
//rtree.Remove(rect.min,rect.max,expire_id[i]);
if(rtree.Remove(rect.min,rect.max,expire_id[i]))
  {
	 //调试程序用
	  cout<<"Failed to delete node from rtree,please enter something"<<endl;
	  char pp[10];
	  cin>>pp;
	  cout<<expire_id[i]<<endl;
  }

//从Moset中去除
moSet.erase(expire_id[i]);



  int j=0;
  while(j<CRSet.size())
  {
	   if(CRSet[j].test(expire_id[i]))
	   {
		   CRSet[j].reset(expire_id[i]);
		   //changed by 2008-2-24
		   if(!maintainMaximalClique(CRSet,CRSet[j]))
		   {
			 //CRSet[j]不再极大，则从CRSet中将其去除
			 CRSet[j]=CRSet[CRSet.size()-1];
			 CRSet.pop_back();
		   }
		   else
			   j++;
	   }
	   else
		   j++;

   }

//调试程序用
//if(expire_id[i]==

wFileStream<<expire_id[i]<<endl;

}
if(expire_id.size()>0)
{
expiredCount=expiredCount+expire_id.size();
wFileStream<<"-----------------------------------------------------------------"<<endl;
wFileStream.close();
}

//清除vector<int> heap_key中过期的时间
int i=0;
while(i<heap_key.size())
//for(int i=0;i<heap_key.size();i++)
{
//heap_key[i]=heap_key[i]-durationTime;
heap_key[i]=heap_key[i]-durationTime-maximal_clique_maintaining_time;
if(heap_key[i]<0) 
{
double temp;
temp=heap_key[i];
heap_key[i]=heap_key[heap_key.size()-1];
heap_key[heap_key.size()-1]=temp;
heap_key.pop_back();
}
else
 i++; //如果大于零，则i的位置不变，换过去的元素需要再check一下
}//?while(i<heap_key.size())
*/
		     

		    

		  