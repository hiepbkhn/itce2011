
/*//ʵ������һ���������Ķ���
//�ö�������ʾɭ�֣���ڵ��ʾ���ӣ��ɽڵ��ʾ�ֵܣ������ҽڵ��idֵһ��������ýڵ��idֵ
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
	//��ʼ��һ�����ڵ�
	TNode* InitTNode();
	TNode* InitTNode(int id, TNode* m_lchild, TNode* m_rsibling);
	//����һ��ĳ�ڵ��
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
	//5.1Ѱ��ms���ڵ�clique��������clique_size��������
  //����������Ļ�������ֱ�Ӵ������л�ȡms���ڵ��ţ��Ͳ���һ��һ��ɨ�������
  for(int i=0;i< CRSet.size();i++)
  {
	 if(CRSet[i].test(ms.id))
	 {
		//˵��ms��CRSet[i]����ʾ������
		if(canCR.size()==0)
		 //�ҵ��ĵ�һ������ms����
			canCR.push_back(i);
		else
		{
			//��������///��δ�����δ���е���������
			 int k=0;
			 while(k<canCR.size())
			 {
				
				   //����д��������� �޸����������ʱ��ԭ���Ĳ������������ע�͵Ĳ���
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
				   }//�� if(CRSet[i].count()<CRSet[canCR[k]].count())
				
			 }//?while(k<canCR.size())
			
		}
	 }//��if(CRSet[i].test(ms.id))
  }//��for(int i=0;i<CRSet.size();i++)

  //5.2��canCR��һ����ȡ��ѡ�ţ��ж��Ƿ������
  bool goOn=true;
  //int CRid;
  for(int i=0;i<canCR.size()&&goOn;i++)
  {
	//ȷ��canCR[i]�����е����k����Сk
	  int max_k=0,min_k=MaxPrivacyLevel;
	  for(int j=0;j<LoadMoNumber;j++)
	  {
	 //�����ж�λ��CRSet[canCR[i]]�еڼ�λΪ1
		  if(CRSet[canCR[i]].test(j))
		  {
			//��λΪ1
			  map<int,MO> ::iterator it=moSet.find(j);
			  if(max_k<(*it).second.k) max_k=(*it).second.k;
			  if(min_k>(*it).second.k) min_k=(*it).second.k;
		  }
	  }//? for(int j=0;j<LoadMoNumber;j++)
  
	  //�ж���ʲô���͵�candidate
	  if(CRSet[canCR[i]].count()>=max_k)
	  {
		//positive candidate
		  goOn=false;
		  //CRid=canCR[i];
		  //��CR_mo_id��ֵ
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
			  CR_mo_id.clear();//��ʾû��
		  }
		  else
		  {
			  if((CRSet[canCR[i]].count()==maxNN(ms.k,min_k))&&(CRSet[canCR[i]].count()<max_k))
			  {
				//Not the candidate clique
				goOn=false;
				CR_mo_id.clear();//��ʾû��
			  }
			  else
			  {
				 //negative candidate clique
				 vector<int> moNeg; //���Դ���ڴ�candidate clique�е�����mo��id
				 for(int j=0;j<LoadMoNumber;j++)
				 {
				   if(CRSet[canCR[i]].test(j))
				   {
					 if(moNeg.size()==0)
						 //moNeg.push_back(canCR[i]);
						 moNeg.push_back(j);
					 else
					 {
						 //����
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
										//˵��Ҫ��β�������it_key
									{
										moNeg.push_back(j);
										break;
									}
									k++;
							   }
							   else
							   {
								   //˵��(*it_key).second.k>=(*it_k).second.k,�����ҵ��ĵ�һ����keyֵС��λ��ǰ����
									moNeg.insert(moNeg.begin()+k,j);
									break;
							   }
						  }//?while(k<moNeg.size())
					 }//?if(moNeg.size()==0) else
				   }//?if(CRSet[canCR[i]].test(j))
				 }//?for(int j=0;j<LoadMoNumber;j++)

				 //��negative-��positive
				 int j=0;
				 while(j<moNeg.size())
				 //for(int j=0;j<moNeg.size();j++)
				 {
					 map<int,MO> ::iterator mo_max_k=moSet.find(moNeg[j]);
					 if(moNeg.size()>=(*mo_max_k).second.k)
					 {
					   //ʣ�µ�mo������Ϊ����������
						 for(j;j<moNeg.size();j++)
							 CR_mo_id.push_back(moNeg[j]);
						 goOn=false;
					 }
					 else
					 {
					   //�����negative��moȥ��
						 if(j==moNeg.size()-1)
							 //����ĩβԪ�أ�����������е�˳�򲻻���Ӱ��
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

  //5.3 �ҵ��������󣬽������������û��������ء���һ��ʵ�����������CR_mo_id��ֵ��ʱ���Ѿ������
  if(CR_mo_id.size()>0)
  {
  wFileStream.open("D:\\Experiments\\MMB\\Data\\Result.txt",ios::app);

   //��¼��Щ���������ɹ�
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
	 //����total process time of all successful request 
	 TotalSuccTime=TotalSuccTime+(double)(succTime-(*mo_temp).second.enTime)/CLOCKS_PER_SEC;

	 //���Գ�����
      //wFileStream<<"Previous ID is"<<" "<<(*mo_temp).second.preId<<endl;
	 //���Գ�����
  }
  //��¼��Щ���������ɹ�
  successCount=successCount+CR_mo_id.size();

  wFileStream<<"A single request anonymized successfully require"<<endl;
  wFileStream<<(double)(succTime-inTime)/CLOCKS_PER_SEC<<" s"<<endl;

  //wFileStream<<(double)(succTime-inTime)/(CLOCKS_PER_SEC / (double) 1000.0)<<" ms"<<endl;
  wFileStream<<"==========================================="<<endl;
  wFileStream.close();
  }//?if(CR_mo_id.size()>0)  ��ʱ�����룬ʡ���鷳
}//?if(vNr_temp.size()==1&&vNr_temp[0]==ms.id)

//6.�����ɹ���mo���Ѿ����ڵ�mo leave system
//6.1��ϵͳ��ȥ�������ɹ���Ԫ��
if(CR_mo_id.size()>0)
{
 //˵���������ɹ���Ԫ��,��Ҫ�����ϵͳ��ȥ��
   //������ȡ�����ɹ���mo��id
   map<int,MO>::iterator it_mo;
   Rect mo_MBR;
   for(int i=0;i<CR_mo_id.size();i++)
   {
	  //6.0�������ɹ���Ԫ�ط��뵽moCloakedSet��
		  //6.0.1ȷ����С�߽����MBR
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
		   //6.0.2j��mo����moCloakedSet�� 
	   it_mo=moSet.find(CR_mo_id[i]);
	   //�����ɹ���ʱ��=���������ɹ��Ķ����ʱ���,��������ʱ��ȴ�ʱ���ʵ�����Ѿ������ڴ������������ʱ�����ö��������ʱ����Ĳ��
       //(*it_mo).second.preT=(double)(succTime-inTime)/CLOCKS_PER_SEC+ms.timestamp;
	   (*it_mo).second.preT=ms.timestamp;
	   //(*it_mo).second.preT=(double)(succTime-inTime)/CLOCKS_PER_SEC+(*it_mo).second.timestamp;
	   //��������ʱ�����
	   //(*it_mo).second.preT=(*it_mo).second.timestamp;
	   moCloakedSet.insert(pairInsert((*it_mo).first,(*it_mo).second));
	   
	  //6.1.1��heap��ȥ���ɹ�������Ԫ��
		 //�ҳ�����Ӧ��heap�еļ�������ʱ��
	  double delTime=heap_id_key[CR_mo_id[i]];
		 //��map heap_id_key�н�CR_mo_id[i]ȥ��
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
		//��heap_id_key�н���Ӧ��id��ʱ�����
	  heap_id_key.erase(CR_mo_id[i]);

	   //6.1.2��R-tree��ȥ��
	  rect.min[0]=moSet[CR_mo_id[i]].x;
	  rect.max[0]=moSet[CR_mo_id[i]].x;
	  rect.min[1]=moSet[CR_mo_id[i]].y;
	  rect.max[1]=moSet[CR_mo_id[i]].y;
	  if(rtree.Remove(rect.min,rect.max,CR_mo_id[i]))
	  {
	     //���Գ�����
		  cout<<"Failed to delete node from rtree,please enter something"<<endl;
		  char pp[10];
		  cin>>pp;
	  }

	   //��CRset��ȥ��
	   //ԭ������Ҫ�ж��޸ĺ�����Ƿ���maximal�������������Ȳ��жϡ������������е��ѹ��ڵ�mo�������������ж�CR_mo_id�е�clique�Ƿ�maximal
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
			     //CRSet[j]���ټ������CRSet�н���ȥ��
                 CRSet[j]=CRSet[CRSet.size()-1];
				 CRSet.pop_back();
			   }
			   else
				   j++;
		   }
		   else
			   j++;

	   }

	   //��Moset��ȥ��
		   moSet.erase(CR_mo_id[i]);
   }//?for(int i=0;i<CR_mo_id.size();i++)

   //��CR_mo_id����ǰ�����������������������û���MBR
	for(int i=0;i<CR_mo_id.size();i++)
   {
	  it_mo=moCloakedSet.find(CR_mo_id[i]);
	  (*it_mo).second.preCR=mo_MBR;
   }
}//��if(CR_mo_id.size()>0)

//��CR_mo_id����
CR_mo_id.clear();


outTime=clock();
//outTime-inTime��ms��request�����ʱ��
durationTime=(double)(outTime-inTime)/CLOCKS_PER_SEC;
//durationTime=(double)(outTime-inTime)/(CLOCKS_PER_SEC / (double) 1000.0);

//6.2��ϵͳ�����ݽṹ��ȥ�����ڵ�Ԫ��

//��heap��ȡ���ѹ��ڵ�Ԫ��
//����heap�е�Ԫ�صĴ�����ʱ��
map<int,double>::iterator it;



vector<int> expire_id;
for(it=heap_id_key.begin();it!=heap_id_key.end();it++)
{
//(*it).second=(*it).second-durationTime;//��Ҫ����
//maximal_clique_maintaining_time������һ�����������ά��CRSet��ʹ�õ�ʱ��
(*it).second=(*it).second-durationTime-maximal_clique_maintaining_time; //changed in 2008-2-22
moSet[(*it).first].deltT=(*it).second;//����moSet����Ӧ������
if((*it).second<0)
   //˵������
{
   expire_id.push_back((*it).first);
}
}//��for(it=heap_id_key.begin();it!=heap_id_key.end();it++)



//������ڵ�mo
if(expire_id.size()>0)
{
wFileStream.open("D:\\Experiments\\MMB\\Data\\Result.txt",ios::app);
wFileStream<<"The number of expired objects are "<<endl;
wFileStream<<expire_id.size()<<endl;
wFileStream<<"MOs which expire are :"<<endl;
}//?if(expire_id_size()>0)

for(int i=0;i<expire_id.size();i++)  
{
//��map heap_id_key��ȥ����һ���ڵ�mo id
heap_id_key.erase(expire_id[i]);

//��R-tree��ȥ��
rect.min[0]=moSet[expire_id[i]].x;
rect.max[0]=moSet[expire_id[i]].x;
rect.min[1]=moSet[expire_id[i]].y;
rect.max[1]=moSet[expire_id[i]].y;
//rtree.Remove(rect.min,rect.max,expire_id[i]);
if(rtree.Remove(rect.min,rect.max,expire_id[i]))
  {
	 //���Գ�����
	  cout<<"Failed to delete node from rtree,please enter something"<<endl;
	  char pp[10];
	  cin>>pp;
	  cout<<expire_id[i]<<endl;
  }

//��Moset��ȥ��
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
			 //CRSet[j]���ټ������CRSet�н���ȥ��
			 CRSet[j]=CRSet[CRSet.size()-1];
			 CRSet.pop_back();
		   }
		   else
			   j++;
	   }
	   else
		   j++;

   }

//���Գ�����
//if(expire_id[i]==

wFileStream<<expire_id[i]<<endl;

}
if(expire_id.size()>0)
{
expiredCount=expiredCount+expire_id.size();
wFileStream<<"-----------------------------------------------------------------"<<endl;
wFileStream.close();
}

//���vector<int> heap_key�й��ڵ�ʱ��
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
 i++; //��������㣬��i��λ�ò��䣬����ȥ��Ԫ����Ҫ��checkһ��
}//?while(i<heap_key.size())
*/
		     

		    

		  