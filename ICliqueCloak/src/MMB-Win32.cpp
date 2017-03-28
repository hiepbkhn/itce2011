// MMB-Win32.cpp : Defines the entry point for the console application.
//

//#include "stdafx.h"
#include "RTree.h"

#include "CompHausDis.h"

#include <fstream>
#include <iostream>

#include <map>
#include <vector>
#include <bitset>
#include <time.h>
#include <stdlib.h>
#include <string>




#ifdef _DEBUG
#define new DEBUG_NEW
#endif


// The one and only application object

//CWinApp theApp;


//using namespace std;

//Advance statement
int MaximalClique(bitset<LoadMoNumber> bt1, bitset<LoadMoNumber> bt2);
int maxNN(int n1,int n2);
void InsertAscending(vector<int> insertList,int key);
bool maintainMaximalClique(vector<bitset<LoadMoNumber>> CRSet, bitset<LoadMoNumber> clique);
Rect ComputeMBR(vector<int> result, map<int,MO> &moSet);
double averagerandom(double min,double max);
double FindMaxAmin(bitset<LoadMoNumber>  bt,map<int,MO> &moSet);
double AreaMBR(bitset<LoadMoNumber>  bt,map<int,MO> &moSet);
double AreaMBRV(vector<int> vt,map<int,MO> &moSet);
vector<int> FINDEXPIREDREQUEST(clock_t nowtime,map<int,MO> &moSet);
bool cmp(clock_t a,clock_t b);
bool CHECKAMAX(Rect CR, float area_max);
void quick_sort(vector<int> &c,int hs,int ht,map<int,MO> &moSet);
int partition(vector<int> &b,int s,int t,map<int,MO> &moSet);
void swap(int &p,int &q);
void shift(vector<clock_t> &a, int i , int m);
void heap_sort(vector<clock_t> &a , int n);  //a sort an array , n is the size of the array (numbered 0 - n -1 )
void CEHCKCDF(int cdf[], float area_temp,int size);
void CEHCKCDFK(int cdf[], float area_temp,int &count);

//Select the extension direction
void CanEnlargeDir(Rect pre_R, Rect cur_R,bool &top, bool &bottom, bool &left, bool &right);
Rect FindCloakedRegion(vector<int> CR_mo_id,map<int,MO> &moSet);
void ResultRefiment(vector<int> &CR_mo_id,map<int,MO> &moSet,Rect &CR,double area_amax,bool &succflag);

const float EPS=1e-6;

const float LEV1=0.5e6/AREA;
const float LEV2=1e6/AREA;
const float LEV3=1.5e6/AREA;
const float LEV4=2e6/AREA;
const float LEV5=2.5e6/AREA;
const float LEV6=3e6/AREA;
const float LEV7=3.5e6/AREA;
const float LEV8=4e6/AREA;
const float LEV9=5e6/AREA;
const float LEV10=6e6/AREA;
const float LEV11=7e6/AREA;
const float LEV12=8e6/AREA;
const float LEV13=9e6/AREA;
const float LEV14=10e6/AREA;
const float LEV15=11e6/AREA;
const float LEV16=12e6/AREA;//6
const float LEV17=13e6/AREA;
const float LEV18=14e6/AREA;
const float LEV19=15e6/AREA;
const float LEV20=16e6/AREA;
const float LEV21=17e6/AREA;
const float LEV22=18e6/AREA;
const float LEV23=19e6/AREA;
const float LEV24=20e6/AREA;//
const float LEV25=21e6/AREA;
const float LEV26=22e6/AREA;
const float LEV27=23e6/AREA;
const float LEV28=24e6/AREA;
const float LEV29=25e6/AREA;
const float LEV30=26e6/AREA;
const float LEV31=27e6/AREA;
const float LEV32=28e6/AREA;
const float LEV33=29e6/AREA;
const float LEV34=30e6/AREA;//
const float LEV35=31e6/AREA;//
const float LEV36=32e6/AREA;
const float LEV37=33e6/AREA;
const float LEV38=34e6/AREA;
const float LEV39=35e6/AREA;
const float LEV40=36e6/AREA;
const float LEV41=37e6/AREA;
const float LEV42=38e6/AREA;
const float LEV43=39e6/AREA;
const float LEV44=40e6/AREA;
const float LEV45=41e6/AREA;
const float LEV46=42e6/AREA;
const float LEV47=43e6/AREA;
const float LEV48=44e6/AREA;
const float LEV49=45e6/AREA;
const float LEV50=46e6/AREA;
const float LEV51=47e6/AREA;
const float LEV52=48e6/AREA;
const float LEV53=49e6/AREA;
const float LEV54=50e6/AREA;
const float LEV55=51e6/AREA;
const float LEV56=52e6/AREA;
const float LEV57=53e6/AREA;
const float LEV58=54e6/AREA;
const float LEV59=55e6/AREA;
const float LEV60=56e6/AREA;
const float LEV61=57e6/AREA;
const float LEV62=58e6/AREA;
const float LEV63=59e6/AREA;
const float LEV64=60e6/AREA;
const float LEV65=61e6/AREA;
const float LEV66=62e6/AREA;
const float LEV67=63e6/AREA;
const float LEV68=64e6/AREA;
const float LEV69=100e6/AREA;
const float LEV70=150e6/AREA;
const float LEV71=200e6/AREA;
const float LEV72=250e6/AREA;//
const float LEV73=300e6/AREA;

//HiepNH - global declaration
vector<int> vNr_temp;

bool MySearchCallback(int id, void* arg) 
{
	vNr_temp.push_back(id);	//HiepNH - added
	//printf("Hit data rect %d\n", id);
	return true; // keep going
}
//The above is the R-tree with the code


int main(int argc, char* argv[])
{
	int nRetCode = 0;

	typedef pair<int, MO> pairInsert;

	MO mo,ms;//ms currently being processed mo

	clock_t enterTime,inTime,outTime,succTime,exitTime;//enterTime into the system ; outTime determine whether time expired

	clock_t MCS_time,MCE_time;//MCS is a maintenance clique start time , MCE is the maintenance of a clique of the end of time

	double maximal_clique_maintaining_time=0;//Finally , determine the time of great use when . Longer maintenance can not be ignored. Determine whether expired , joined this time . But does not consider the time making anonymous . Only in the last re- set the time to be expired when	

	//int warmupcount=0;

	// initialize MFC and print and error on failure
	//HiepNH - commented
	//if (!AfxWinInit(::GetModuleHandle(NULL), NULL, ::GetCommandLine(), 0))
	if (1 == 0)
	{
//		// TODO: change error code to suit your needs
//		_tprintf(_T("Fatal Error: MFC initialization failed\n"));
//		nRetCode = 1;
	}
	else
	{
		// TODO: code your application's behavior here.

		//Variable Description
		//MO_Briknhoff MO_temp;

		ifstream r2FileStream, configStream;
		ofstream wFileStream;
		float deltT=0;


		RTree<int, float, 2, float> rtree;//Stored and indexed into the system mo position to facilitate future to determine the mutual positional relationship

		//The create map container object moSet, the management Pending object
		map<int,MO> moSet;
		//moCloakedSet, management has been successful anonymous object
		map<int,MO> moCloakedSet;
		//Create the bitset array of storage great group
		vector<bitset<LoadMoNumber>> CRSet;
		int maxCRSet=0;
		int maxNNcount=0;
		//Store the id of the anonymous collection of mo
		vector<int> CR_mo_id;
		//heap: id is the id , key representatives of the mo how long this mo is about to expire
		map<int,double> heap_id_key;
		vector<double> heap_key;


		//int cdf[44]={0};
		int cdf2[74]={0};
		//int cdf3[44]={0};
		//int cdf4[44]={0};
		int cdf5[74]={0};
		//int cdf6[44]={0};
		//int cdf7[44]={0};
		int cdf8[74]={0};
		int cdf9[74]={0};
		//int cdf10[13]={0};
		//int cdfelse[44]={0};*/

		int count2=0,count5=0,count8=0,count9=0,count10=0;


		//The experimental results recorded data
		int successCount=0,noJiaoCount=0,expiredCount=0,inMOCount=0;//inMOCount recorded the number of the second query request life cycle
		double TotalSuccTime=0;
		double cost=0;
		double reCloakedLevel=0;
		int ClockedSetCount=0;//The number of anonymous collection


		//enterTime=outTime=time();//Initialize the following
		double durationTime=0;//Already resides in the system time
		double ClockedTime=0; //The total time of the pure anonymous
		double worstMBR=0;


		//When new message arrives
		//int printk=5;
		configStream.open("..\\iclique.cfg",ios::in);
		string filename;
		getline(configStream, filename);
		configStream.close();
		cout<<"filename="<<filename<<endl;

		r2FileStream.open((char*)filename.c_str(),ios::in);
		//r2FileStream.open("G:\\datasize\\tdata\\k\\T_Data_k90100.txt",ios::in);
		if(!r2FileStream)
		{
			cout<<"Open failed"<<endl;

		}
		else
		{
			enterTime=clock();
			//When the arrival of a point , read out a vertex
			r2FileStream>>mo.id>>mo.x>>mo.y>>mo.timestamp>>mo.speed>>mo.k>>mo.Amin;
			//warmupcount++;
			while(!r2FileStream.eof())
			{    

				//Assume unlimited time , after the set
				mo.deltT=MaxTolerantTime;
				Rect rect(mo.x,mo.y,mo.x,mo.y);
				//anonymous request inTime said trigger the start time begins processing
				inTime=clock();
				//if(warmupcount<=1000)
				//mo.enTime=inTime+30;
				//else
				mo.enTime=inTime;


				//1.Modify the data structure
				map<int,MO>::iterator mo_exist;

				//1.0To determine location updated the anonymous successful mo, or wait for the anonymous mo . If the former, you need to update the last anonymous location
				mo_exist=moCloakedSet.find(mo.id);
				if(mo_exist!=moCloakedSet.end())
				{
					//Is anonymously success point
					mo.preCR=(*mo_exist).second.preCR;
					mo.preT=(*mo_exist).second.preT;
					mo.UpdateMMB();
				}//? if(mo_exist!=moCloakedSet.end())

				//Judge whether it is in the life cycle of the second query , if put it removed
				mo_exist=moSet.find(mo.id);
				//Determine compliance with the anonymity requirements
				if(mo_exist==moSet.end())
				{

					//1.1 will enter Mo inserted map moSet , mo not a new Moisturizer mo.id repeated Update , i.e. from loc information
					moSet.insert(pairInsert(mo.id,mo));
					//1.2 Modifications R-tree
					//no matter wheteh it is in rtree, it needs to be inserted in it
					rtree.Insert(rect.min,rect.max,mo.id);
					ms=mo;

					//1.4 Modify the regiment tree set in accordance with the algorithm , there should actually generate a single point group , but this group will disappear immediately after scanning the first edge , so dispense with this step


					//2.Correction MMB
					//Intersect the maximum range of motion and a tolerable range , do the following
					//After updating the MMB , the modified mo into the moSet in
					moSet[ms.id].mmb=ms.mmb;//This seems not necessary
					//3.Looking for ms entry by adding what new edge
					vNr_temp.clear();//First , the global variable vNr cleared
					if(ms.mmb.top_right.r==FLT_MAX)
					{
						Rect range(-FLT_MAX,-FLT_MAX,FLT_MAX,FLT_MAX);
						rtree.Search(range.min,range.max,MySearchCallback,NULL);
					}//? if(ms.mmb.top_right.x==FTL_MAX && ms.mmb.top_right.y==FTL_MAX)
					else
					{
						Rect range(ms.mmb.down_left.x-ms.mmb.down_left.r,ms.mmb.down_left.y-ms.mmb.down_left.r,
							ms.mmb.top_right.x+ms.mmb.top_right.r,ms.mmb.top_right.y+ms.mmb.top_right.r);
						rtree.Search(range.min,range.max,MySearchCallback,NULL);
						vector<int> real_vNr;
						for(int i=0;i<vNr_temp.size();i++)
						{
							double x_temp=moSet[vNr_temp[i]].x;
							double y_temp=moSet[vNr_temp[i]].y;
							if(ms.mmb.inMMB(x_temp,y_temp))
								real_vNr.push_back(vNr_temp[i]);
						}
						vNr_temp.clear();
						vNr_temp=real_vNr;
					}//?if(ms.mmb.top_right.x==FTL_MAX && ms.mmb.top_right.y==FTL_MAX) else
					if(vNr_temp.size()==1&&vNr_temp[0]==ms.id)
					{
						//This is an isolated point , there is no adjacent vertices
						//Adding a new maximal clique directly in CRSet in can
						CRSet.push_back(0);
						CRSet[CRSet.size()-1].set(ms.id);
					}
					else
					{
						//Found in the previous step the vertices of shrouded in ms.MMB , and below looking for ms is enveloped in which nodes of these nodes . Find these nodes at the same time , they inserted into a vector , sorted in ascending order in accordance with maxcliquesize
						//When the largest contiguous point where the group need to remove to directly remove at the end of the element and removal can be
						vector<int> vNrofMo;

						//Debugging programs
						/*wFileStream.open("D:\\Experiments\\MMB\\Data\\Heap.txt",ios::app);
						wFileStream<<"Trigger message is"<<endl;
						wFileStream<<ms.id<<endl;
						for(int i=0;i<vNr_temp.size();i++)
						//cout<<vNr_temp[i]<<endl;
						{   
						wFileStream<<"Time in heap:"<<endl;
						wFileStream<<vNr_temp[i]<<endl;
						wFileStream<<"*******************************************************************************************"<<endl;  
				  }
				  wFileStream.close();*/
						//Debugging programs

						for(int i=0;i<vNr_temp.size();i++)
						{
							//Whether the judge ms in the mo id vNr_temp [i] , MMB
							MO mo_temp;
							mo_temp=moSet[vNr_temp[i]];//Get id vNr_temp [i] mo
							if(ms.id!=mo_temp.id && mo_temp.mmb.inMMB(ms.x,ms.y))	  
							{
								//Two vertices contains interaction / / is no orderly insertion vNrofMo in , directly inserted , and then extract an edge can randomly select a side
								vNrofMo.push_back(mo_temp.id); //changed in 2008-3-6 edge selection strategy
							}
						}//?for(int i=0;i<vNrofMo.size();i+=)

						//Debugger uses
						MCS_time=clock();
						//Debugger uses

						//Dynamic maintenance cliques
						if (maxNNcount<vNrofMo.size()) maxNNcount=vNrofMo.size();
						//4.1 Reverse order ( in accordance with the edge of the selection rules ) turn scan by vNrofMo the vertex , which in turn scan the side due to the entry of ms
						for(int i=vNrofMo.size()-1;i>=0;i--)
						{
							if(i==vNrofMo.size()-1)
							{
								//Scan the first side ( ms vNrofMo [ i ] ) when directly increase the a maximal clqiue ( ie bitset )
								CRSet.push_back(0);
								CRSet[CRSet.size()-1].set(ms.id);//Location is set to 1 ms in bitset corresponding
								CRSet[CRSet.size()-1].set(vNrofMo[i]);//Ms neighbor vertex in the group is set to 1

								//Judgment vNrofMo [i] No added edge after (ms, vNroMo [i]) , is no longer great
								int j=0;
								while(j<CRSet.size())
								{
									if((CRSet[j].test(vNrofMo[i]))&&(CRSet[j].count()==1))
									{
										//The great group removed if no longer great , then a single point vNrofMo [i] . Interchangeable with the last group , then pop_back
										bitset<LoadMoNumber> mo_temp;
										mo_temp=CRSet[j];
										CRSet[j]=CRSet[CRSet.size()-1];
										CRSet[CRSet.size()-1]=mo_temp;
										CRSet.pop_back();
									}
									else
										j++;
								}

							}
							else
							{
								vector<bitset<LoadMoNumber>> interMC;//The maximal clique a collection of the intermediate results
								//Improve the efficiency of a can consider the point : in the bitmap indexing to accelerate find speed
								for(int j=0;j<CRSet.size();j++)
								{
									//Seeking ms where the group 's collection
									if(CRSet[j].test(ms.id))  //I.e. in a bitset sequence , ms.id is 1
									{
										//Find a ms where the group
										for(int k=0;k<CRSet.size()&&k!=j;k++)
										{
											//Find vNrofMo [i] where the group
											if(CRSet[k].test(vNrofMo[i]))
											{
												//Two collection take cross
												bitset<LoadMoNumber> mc_temp(CRSet[j]);
												mc_temp &= CRSet[k];
												interMC.push_back(mc_temp);
											}
										}//?for(int k=0;k<CRSet.size();k++)
									}//? if(CRSet[j][ms.id]==1)
								}//?for(int j=0;j<CRSet.size();j++)

								//4.2 in the result of the intersection of concentrated only reserved great group ( optimize , add a tempMC , see paper )
								int j=0;
								while(j<interMC.size())
								{
									int k=j+1;
									while(k<interMC.size())
									{
										if(interMC[j]==interMC[k])  
										{
											//The two regiments equal , keep a can the k location removed maixmal ( j has been compared in front some , if you delete j , k need to compare )
											bitset<LoadMoNumber> mo_temp;
											mo_temp=interMC[k];
											interMC[k]=interMC[interMC.size()-1];
											interMC[interMC.size()-1]=mo_temp;
											interMC.pop_back();
											//interMC.erase(interMC.begin()+j);k++ Continue to determine the following of k
										}
										else
										{
											//Ranging from two bit strings , determine which is great : the execution and operation , equal that is not great
											bitset<LoadMoNumber> mc_temp(interMC[j]);
											mc_temp&=interMC[k];
											if(mc_temp==interMC[j])
											{
												//Description interMC [ j] is not great, get rid of this clique, skip the cycle , j Do j + +
												bitset<LoadMoNumber> mo_temp;
												mo_temp=interMC[j];
												interMC[j]=interMC[interMC.size()-1];
												interMC[interMC.size()-1]=mo_temp;
												interMC.pop_back();
												break;
												//interMC.erase (interMC.begin () + j); j Do + + , and k need to re - assignment
											}
											else
											if(mc_temp==interMC[k])
											{
												//interMC [k] is not greatly
												bitset<LoadMoNumber> mo_temp;
												mo_temp=interMC[k];
												interMC[k]=interMC[interMC.size()-1];
												interMC[interMC.size()-1]=mo_temp;
												interMC.pop_back();
												//interMC.erase(interMC.begin()+k);
											}
											else
											{
												//interMC [j] and interMC [k ] does not contain each other
												k++;
											}//? if(mc_temp==interMC[k])

										}//?if(interMC[j]==interMC[k]) else
									}//?while(k<interMC.size())
									if(k>=interMC.size()) j++;
								}//?while(j<interMC.size())


								//The 4.3 last retained in interMC is a great group
								for(j=0;j<interMC.size();j++)
								{
									//The maximal clique plus ms vNrofMo [i] to form a new maximal clique
									interMC[j].set(ms.id);
									interMC[j].set(vNrofMo[i]);

									//In fact , here in an index on the map , you do not have all the scan again CRSet, directly through the index to find ms with vNrofMo [i] where the group operation can be
									//for(int k=0;k<CRSet.size();k++)
									int k=0;
									while(k<CRSet.size())
									{
										//The group contains ms or vNrofMo [i] only necessary to determine whether it is still great
										if(CRSet[k].test(ms.id) || CRSet[k].test(vNrofMo[i]))
										{
											int maxR;
											maxR=MaximalClique(interMC[j],CRSet[k]);
											if(maxR==1) 
											{
												//To determine the original the clique in CRSet whether new maximal clique interMC [j] contained
												bitset<LoadMoNumber> mo_temp;
												mo_temp=CRSet[k];
												CRSet[k]=CRSet[CRSet.size()-1];
												CRSet[CRSet.size()-1]=mo_temp;
												CRSet.pop_back();
												//CRSet.erase(CRSet.begin()+k);
											}
											else
												k++;
										}//?if(CRSet[k].test(ms.id) || CRSet[k].test(vNrofMo[i]))
										else
											k++;
									}//?while(k<CRSet.size())

									//The newly generated maximal clique into the CRSet in
									CRSet.push_back(interMC[j]);
								}//? for(int j=0;j<interMC.size();j++)
							}//? if(i==vNrofMo.size())
						}//?for(int i=vNrofMo.size();i<=0;i--)

						//Debugger uses
						/*MCE_time=clock();
						wFileStream.open("D:\\Experiments\\MMB\\Data\\MClique.txt",ios::app);
						wFileStream<<"Current mo is process is :"<<mo.id<<endl;
						wFileStream<<"Clique mainting when mo has arrving system "<<endl;
						wFileStream<<(double)(MCE_time-MCS_time)/CLOCKS_PER_SEC<<endl;
						wFileStream.close();*/
						//Debugger uses

						//5 . Looking for anonymous set
						//if(warmupcount>1000)
						//{
						vector<int> canCR;
						//5.1 Looking for ms where the clique, and in accordance clique_size descending sort
						//Index here directly from the index for ms where the group would not find a scan
						for(int i=0;i< CRSet.size();i++)
						{
							if(CRSet[i].test(ms.id))
							{
								//Ms in the group represented by CRSet [i]
								if(canCR.size()==0)
									//Find the first group contains ms
									canCR.push_back(i);
								else
								{
									//Descending order. / / / This code has not yet run into ? ? ? ?
									int k=0;
									while(k<canCR.size())
									{
										//Re- write the sort of insert Modified finishing the program . The original insert code is part of the above comments
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
										}//? if(CRSet[i].count()<CRSet[canCR[k]].count())
									}//?while(k<canCR.size())
								}//?if(canCR.size()==0) else
							}//?if(CRSet[i].test(ms.id))
						}//?for(int i=0;i<CRSet.size();i++)

						//5.2 An extract from canCR candidate group , to determine whether it can be anonymous
						bool goOn=true;
						//int CRid;
						for(int i=0;i<canCR.size()&&goOn;i++)
						{
							//revied by 2009-10-22
							//First, to determine whether there is a reasonable region , if not then do not judge k
							//vector<int> id_list;
							//Rect can_CR(0,0,0,0);
							//id_list=FindMOids(CRSet[canCR[i]]);
							//can_CR=FindCloakedRegion(id_list);
							//if(can_CR.min[0]!=0 && can_CR.min[1]!=0 && can_CR.max[0]!=0 && can_CR.max[1]!=0)
							//{
							//The following CRSet [ canCR [ i ] ] may have to be replaced id_list
							//Determine the maximum k canCR [i] No. groups with the smallest k
							int max_k=0,min_k=FLT_MAX;
							double CL_Amin,area;
							bool gt_Amin=1;
							//compute the Amin of Clique CL
							CL_Amin=FindMaxAmin(CRSet[canCR[i]],moSet);
							area=AreaMBR(CRSet[canCR[i]],moSet);
							if(area<CL_Amin) 
								gt_Amin=0;
							for(int j=0;j<LoadMoNumber&&gt_Amin;j++)
							{
								//Turn judge the bit string CRSet [ canCR first of several [ i ]] 1
								if(CRSet[canCR[i]].test(j))
								{
									//This bit is 1
									map<int,MO> ::iterator it=moSet.find(j);
									if(max_k<(*it).second.k) max_k=(*it).second.k;
									if(min_k>(*it).second.k) min_k=(*it).second.k;
								}
							}//? for(int j=0;j<LoadMoNumber;j++)

							//Determine what type of candidate
							if(gt_Amin && CRSet[canCR[i]].count()>=max_k)
							{
								//positive candidate
								goOn=false;
								//CRid=canCR[i];
								//To CR_mo_id assignment
								for(int j=0;j<LoadMoNumber;j++)
								{
									if(CRSet[canCR[i]].test(j)) 
										CR_mo_id.push_back(j);

								}
							}
							else
								if(!gt_Amin || CRSet[canCR[i]].count()<=maxNN(ms.k,min_k))
									//Not the candidate clique
								{
									//CRid=canCR[i].size();
									goOn=false;
									CR_mo_id.clear();//That there is no
								}
								else
								{
									//negative candidate clique
									vector<int> moNeg; //Stored in this candidate clique mo id
									for(int j=0;j<LoadMoNumber;j++)
									{
										if(CRSet[canCR[i]].test(j))
										{
											if(moNeg.size()==0)
												//moNeg.push_back(canCR[i]);
												moNeg.push_back(j);
											else
											{
												//Descending
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
																//To be added at the tail it_key
															{
																moNeg.push_back(j);
																break;
															}
															k++;
														}
														else
														{
															//Description ( * it_key ) second.k > = (* it_k ) . Second.k , that in the first one found than the key value small position before insertion
															moNeg.insert(moNeg.begin()+k,j);
															break;
														}
												}//?while(k<moNeg.size())
											}//?if(moNeg.size()==0) else
										}//?if(CRSet[canCR[i]].test(j))
									}//?for(int j=0;j<LoadMoNumber;j++)

									//convert negative-〉positive
									int j=0;
									while(j<moNeg.size())
									{
										map<int,MO> ::iterator mo_max_k=moSet.find(moNeg[j]);
										if(moNeg.size()>=(*mo_max_k).second.k)
										{
											//The rest of the mo as a set of anonymous return
											for(j;j<moNeg.size();j++)
												CR_mo_id.push_back(moNeg[j]);
											goOn=false;
										}
										else
										{
											//Will cause negative removed the mo
											if(j==moNeg.size()-1)
												//If the end of the element , the entire sequence of the order will not be affected
												moNeg.pop_back();
											else
											{
												moNeg.erase(moNeg.begin()+j);
											}
										}
									}//?forwhile(j<moNeg.size())
									// }//?if((CRSet[canCR[i]].count()==maxNN(ms.k,min_k))&&(CRSet[canCR[i]].count()<max_k))

								}//?if(CRSet[canCR[i]].size()<maxNN(ms.k,min_k)) else

								// }//? if(can_CR.min[0]!=0 && can_CR.min[1]!=0 && can_CR.max[0]!=0 && can_CR.max[1]!=0)
								//revised by 2009-10-22

						}//?for(int i=0;i<canCR.size()&&goOn;i++)

						//5.3 Will find the anonymous set of anonymous to set all anonymous users return . This step is actually above the to CR_mo_id assignment has been completed

						if(CR_mo_id.size()>0)
						{

							//revised by 2009-10-22
							Rect CR(0,0,0,0);
							CR=FindCloakedRegion(CR_mo_id,moSet);
							//revised by 2009-10-22
							if(CR.min[0]!=0 && CR.min[1]!=0 && CR.max[1]!=0 && CR.max[0]!=0)//revised by 2010-12-3
							{
								bool succflag=0;
								//if(CHECKAMAX(CR,AREA*AMAX)) revised by 2011-1
								if(1)
								{ succflag=1;}
								else
								{
									ResultRefiment(CR_mo_id, moSet,CR,AREA*AMAX,succflag);
								}//if(CHECKAMAX(CR,AREA*AMAX))

								if(succflag)
								{//wFileStream.open("F:\\RUC\\Experiments\\Test\\data\\trucks\\result.txt",ios::app);

									//Anonymous success record which objects
									//wFileStream<<"The number of cloaked successfully objects are ";
									//wFileStream<<CR_mo_id.size()<<endl;
									//wFileStream<<"MOs which are anonymized successfully are:"<<endl;

									succTime=clock();

									for(int i=0;i<CR_mo_id.size();i++)
									{
										//Debugger
										//wFileStream<<CR_mo_id[i]<<" "<<moSet[CR_mo_id[i]].k<<endl;
										map<int,MO>::iterator mo_temp=moSet.find(CR_mo_id[i]);
										//wFileStream<<"Timestamp is"<<" "<<(*mo_temp).second.timestamp<<endl;
										//wFileStream<<"Process time for this request require:"<<(double)(succTime-(*mo_temp).second.enTime)/CLOCKS_PER_SEC<<endl;
										//Calculate total process time of all successful request 
										TotalSuccTime=TotalSuccTime+(double)(succTime-(*mo_temp).second.enTime)/CLOCKS_PER_SEC;

										//Calculate the relative degree of anonymity
										reCloakedLevel=reCloakedLevel+double(CR_mo_id.size()/(*mo_temp).second.k);

										double area_temp=((CR.max[0]-CR.min[0])*(CR.max[1]-CR.min[1]))/AREA;
										//CEHCKCDF(cdf,area_temp,CR_mo_id.size());
										//switch(CR_mo_id.size())
										switch((*mo_temp).second.k)
										{
											case 2:
												CEHCKCDFK(cdf2,area_temp,count2);
												break;
											//case 3:
											//CEHCKCDF(cdf3,area_temp,CR_mo_id.size());
											//break;
											//case 4:
											//CEHCKCDF(cdf4,area_temp,CR_mo_id.size());
											//break;
											case 5:
												CEHCKCDFK(cdf5,area_temp,count5);
												break;
											//case 6:
											//CEHCKCDF(cdf6,area_temp,CR_mo_id.size());
											//break;
											//case 7:
											// CEHCKCDF(cdf7,area_temp,CR_mo_id.size());
											// break;
											case 8:
												CEHCKCDFK(cdf8,area_temp,count8);
												break;
											case 9:
												CEHCKCDFK(cdf9,area_temp,count9);
												break;
											//case 10:
											//CEHCKCDFK(cdf10,area_temp,count10);
											//break;
											//default:
											//CEHCKCDF(cdfelse,area_temp,CR_mo_id.size());
										}
										//Debugging programs
										//wFileStream<<"Previous ID is"<<" "<<(*mo_temp).second.preId<<endl;
										//Debugging programs
									}// for(int i=0;i<CR_mo_id.size();i++)
									//Anonymous success record which objects
									successCount=successCount+CR_mo_id.size();
									//Collection makes success anonymous number +1
									ClockedSetCount++;



									//Record time Anonymous pure Anonymous
									ClockedTime=ClockedTime+(double)(succTime-inTime)/CLOCKS_PER_SEC;

									//wFileStream<<"A single request anonymized successfully require"<<endl;
									//wFileStream<<(double)(succTime-inTime)/CLOCKS_PER_SEC<<" s"<<endl;
									//wFileStream<<endl;
									//wFileStream<<"==========================================="<<endl;
									//wFileStream.close();
									cost=cost+(CR.max[1]-CR.min[1])*(CR.max[0]-CR.min[0])*CR_mo_id.size();//revised by 2010-12-23
									if(worstMBR<(CR.max[1]-CR.min[1])*(CR.max[0]-CR.min[0])) 
										worstMBR=(CR.max[1]-CR.min[1])*(CR.max[0]-CR.min[0]);}//if(CHECKAMAX(CR,AREA*AMAX))}//if(succflag)



							}//?if(CR.min[0]!=0 && CR.min[1]!=0 && CR.max[1]!=0 && CR.max[0]!=0)
							else
							{
								//if(!CHECKAMAX(CR,AREA*AMAX)) 
								//{
								// double middlex=(CR.max[0]-CR.min[0])/2;
								//double middley=(CR.max[1]-CR.min[1])/2;;
								//wFileStream.open("F:\\RUC\\Experiments\\Test\\data\\datasize\\fail.txt",ios::app);
								//for(int k=0;k<CR_mo_id.size();k++)
								//{
								//double dist=sqrt((middlex-moSet[CR_mo_id[k]].x)*(middlex-moSet[CR_mo_id[k]].x)+(middley-moSet[CR_mo_id[k]].y)*(middley-moSet[CR_mo_id[k]].y));
								//wFileStream<<CR_mo_id[k]<<"("<<moSet[CR_mo_id[k]].k<<","<<dist<<")";
								//}
								//wFileStream<<endl;
								//wFileStream.close();
								//}//if(!CHECKAMAX(CR,AREA*AMAX))
								//else
								//{
								//wFileStream.open("F:\\RUC\\Experiments\\Test\\data\\datasize\\CRfail.txt",ios::app);
								//for(int k=0;k<CR_mo_id.size();k++)
								//wFileStream<<CR_mo_id[k]<<"("<<moSet[CR_mo_id[k]].k<<")";
								//wFileStream<<endl;
								//wFileStream.close();
								//}
								CR_mo_id.clear(); //revised by 2010-12-03 When greater than Amax time , this is not anonymous collection .
								//The setting of this step is anonymous successful mo deleted from the system in order to allow to prevent the back 6 .
							}

						}//?if(CR_mo_id.size()>0)  When input , save trouble
					}//?if(vNr_temp.size()==1&&vNr_temp[0]==ms.id)
					//}// if(warmupcount>5000)


					//6.The anonymous successful mo and mo leave system has expired
					//6.1Anonymous successful element is removed from the system
					if(CR_mo_id.size()>0)
					{
						//Anonymous successful elements need to be removed from the system
						//Sequentially extracted anonymous successful MO id
						map<int,MO>::iterator it_mo;
						Rect mo_MBR;
						for(int i=0;i<CR_mo_id.size();i++)
						{
							//6.0To element into to moCloakedSet in anonymous success
							//6.0.1 determine the minimum bounding rectangle MBR
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
							//6.0.2 j the mo into the moCloakedSet in
							it_mo=moSet.find(CR_mo_id[i]);
							//Anonymous successful moment = Trigger anonymous successful object timestamp , anonymous difference among the time required for waiting time actually contains a timestamp in the timestamp of the trigger anonymous objects with the object
							(*it_mo).second.preT=ms.timestamp;
							//(*it_mo).second.preT=(double)(succTime-inTime)/CLOCKS_PER_SEC+(*it_mo).second.timestamp;
							//Ignore the time of the anonymous
							//(*it_mo).second.preT=(*it_mo).second.timestamp;
							moCloakedSet.insert(pairInsert((*it_mo).first,(*it_mo).second));

							//6.1.1Success anonymous elements removed from heap
							//6.1.2Removed from the R-tree
							rect.min[0]=moSet[CR_mo_id[i]].x;
							rect.max[0]=moSet[CR_mo_id[i]].x;
							rect.min[1]=moSet[CR_mo_id[i]].y;
							rect.max[1]=moSet[CR_mo_id[i]].y;
							if(rtree.Remove(rect.min,rect.max,CR_mo_id[i]))
							{
								//Debugging programs
								cout<<"Failed to delete node from rtree,please enter something"<<endl;
								char pp[10];
								cin>>pp;
							}

							//Removed from CRset
							//In principle on the need to judge the group whether modified or maximal, but here I am to not judge. And other eliminated all expired mo, in batch processing the judge CR_mo_id clique whether maximal
							//for(int j=0;j<CRSet.size();j++)
							int j=0;
							while(j<CRSet.size())
							{
								if(CRSet[j].test(CR_mo_id[i]))
								{
									CRSet[j].reset(CR_mo_id[i]);
									//changed by 2008-2-24
									if(!maintainMaximalClique(CRSet,CRSet[j]))
									{
										//The longer great CRSet [j] , from CRSet the in its removal
										CRSet[j]=CRSet[CRSet.size()-1];
										CRSet.pop_back();
									}
									else
										j++;
								}
								else
									j++;

							}

							//Removed from Moset
							moSet.erase(CR_mo_id[i]);
						}//?for(int i=0;i<CR_mo_id.size();i++)

						//Before CR_mo_id cleared , set all anonymous box, all users of MBR
						for(int i=0;i<CR_mo_id.size();i++)
						{
							it_mo=moCloakedSet.find(CR_mo_id[i]);
							(*it_mo).second.preCR=mo_MBR;
						}
					}//?if(CR_mo_id.size()>0)

					//Clear the CR_mo_id
					CR_mo_id.clear();
					outTime=clock();
					//outTime-inTime is Ms the request processing time
					durationTime=(double)(outTime-inTime)/CLOCKS_PER_SEC;
					//durationTime=(double)(outTime-inTime)/(CLOCKS_PER_SEC / (double) 1000.0);

					//6.2 Remove expired elements from the data structure of the system

					//Remove expired elements from the heap
					//The pending expiration time to update the elements in the heap
					map<int,double>::iterator it;

					vector<int> expire_id;
					expire_id=FINDEXPIREDREQUEST(outTime,moSet);


					//Output expired mo
					/*if(expire_id.size()>0)
					{
					//wFileStream.open("D:\\Experiments\\Result\\Result.txt",ios::app);
					//wFileStream<<"The number of expired objects are "<<endl;
					//wFileStream<<expire_id.size()<<endl;
					//wFileStream<<"MOs which expire are :"<<endl;
					}//?if(expire_id_size()>0)*/

					for(int i=0;i<expire_id.size();i++)  
					{
						//Removed from the R-tree
						//rect.min[0]=rect.min[1]=0;
						//rect.max[0]=rect.max[1]=0;
						rect.min[0]=moSet[expire_id[i]].x;
						rect.max[0]=moSet[expire_id[i]].x;
						rect.min[1]=moSet[expire_id[i]].y;
						rect.max[1]=moSet[expire_id[i]].y;
						//rtree.Remove(rect.min,rect.max,expire_id[i]);
						if(rtree.Remove(rect.min,rect.max,expire_id[i]))
						{
							//Debugging programs
							cout<<"Failed to delete node from rtree,please enter something"<<endl;
							char pp[10];
							cin>>pp;
							cout<<expire_id[i]<<endl;
						}

						//Removed from Moset
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
									//The longer great CRSet [j] , from CRSet the in its removal
									CRSet[j]=CRSet[CRSet.size()-1];
									CRSet.pop_back();
								}
								else
									j++;
							}
							else
								j++;

						}
						//wFileStream<<expire_id[i]<<endl;

					}
					if(expire_id.size()>0)
					{
						expiredCount=expiredCount+expire_id.size();
						//wFileStream<<"-----------------------------------------------------------------"<<endl;
						// wFileStream.close();
					}

					//Debugger uses
					MCE_time=clock();
					//wFileStream.open("D:\\Experiments\\Result\\MClique.txt",ios::app);
					//wFileStream<<"Current mo is process is :"<<mo.id<<endl;
					//wFileStream<<"After anonymize and expire justifing, the clique maintaing time is(maximal_clique_maintaining_time)"<<endl;
					//wFileStream<<(double)(MCE_time-MCS_time)/CLOCKS_PER_SEC<<endl;
					//wFileStream.close();
					//Debugger uses*/

					//Debugger uses
					//MCS_time=clock();
					//Debugger uses

					//Debugger uses
					//MCE_time=clock();
					//wFileStream.open("D:\\Experiments\\MMB\\Data\\MClique.txt",ios::app);
					//wFileStream<<"Current mo is process is :"<<mo.id<<endl;
					//wFileStream<<"max_cliqueSize maintaing time is:"<<endl;
					//wFileStream<<(double)(MCE_time-MCS_time)/CLOCKS_PER_SEC<<endl;
					//wFileStream.close();
					//Debugger uses

				}//?if(ms.UpdateMMB())
				else
				{
					//This is a request, one should not appear after the number of records to read the next record can
					inMOCount++;
				}//? if(mo_exist!=moSet.end()) else

				//Debugger uses
				/*map<int,MO>::iterator ixx,iendxx;
				iendxx=moSet.end();
				wFileStream.open("D:\\Experiments\\MMB\\Data\\moSet.txt",ios::app);
				for(ixx=moSet.begin();ixx!=iendxx;ixx++)
				{
				if(ixx==moSet.begin()) 
				{
				//cout<<endl;
				//cout<<endl;
				//cout<<"Objects in moSet:"<<endl;
				//wFileStream<<endl;
				//wFileStream<<"============================================="<<endl;

				}
				//cout<<(*ixx).second.id<<" Timestamp is "<<(*ixx).second.timestamp<<endl;
				wFileStream<<(*ixx).second.id<<" Timestamp is "<<(*ixx).second.timestamp<<endl;

				}
				//cout<<"-----------------------------------------------------"<<endl;
				wFileStream<<"-----------------------------------------------------"<<endl;
				wFileStream.close();*/
				//Debugger uses


				//Read A mo
				r2FileStream>>mo.id>>mo.x>>mo.y>>mo.timestamp>>mo.speed>>mo.k>>mo.Amin;
				//cout<<mo.id<<" "<<mo.timestamp<<endl;
				//warmupcount++;
				//Other values cleared
				mo.Reset();
				if(maxCRSet<CRSet.size()) maxCRSet=CRSet.size();

				//wFileStream.open("D:\\Experiments\\Result\\mo.txt",ios::app);

				//Anonymous success record which objects
				//wFileStream<<mo.id<<" "<<mo.timestamp<<endl;
				//wFileStream.close();


				//Debugging programs
				//if(mo.timestamp==90 && mo.id==22230 )
				//{
				//char pp[10];
				//cin>>pp;
				//int pp;
				//pp=0;
				//}
				//Debugging programs
			} //?while(!rFileStream.eof())

			//When at the end of the file is reached , there object moSet , these objects are also considered into without success anonymous as well
			if(r2FileStream.eof())
			{
				//cout<<"k= "<<printk<<endl;
				cout<<"Number of mo in moSet finally: "<<moSet.size()<<endl;
				expiredCount=expiredCount+moSet.size();
				moSet.clear();
			}//?if(r2FileStream.eof())

			r2FileStream.close();

			exitTime=clock();

			cout<<"Number of mo success: "<<successCount<<endl;
			//TRACE("Number of mo success:   =   %d",successCount);   
			cout<<"Number of noJiao: "<<noJiaoCount<<endl;
			cout<<"Number of expired request: "<<expiredCount<<endl;
			cout<<"Number of un_expected request: "<<inMOCount<<endl;
			cout<<endl;
			cout<<"Total processing time is:";
			cout<<(double)(exitTime-enterTime)/CLOCKS_PER_SEC<<endl;
			//cout<<(double)(exitTime-enterTime)/(CLOCKS_PER_SEC / (double) 1000.0)<<endl;
			cout<<"Average processing time for each success request: ";
			cout<<double(TotalSuccTime/successCount)<<endl;
			cout<<"Average  anonymization time:";
			cout<<double(ClockedTime/ClockedSetCount)<<endl;
			cout<<endl;
			cout<<"Success Rate:";
			cout<<double(successCount)/double((successCount+noJiaoCount+expiredCount))<<endl;
			cout<<"Cost is:";
			cout<<cost<<endl;
			cost=cost/successCount;
			cout<<"Average  cost:"<<cost<<endl;
			cout<<"Relative space:"<<double(cost/AREA)<<endl;
			//cout<<double(cost/ClockedSetCount)<<endl;
			cout<<"Worst mbr:"<<worstMBR<<endl;
			cout<<"Number of group:";
			cout<<ClockedSetCount<<endl;
			//Successful
			cout<<endl;
			cout<<reCloakedLevel<<endl;
			cout<<"Relative Anonymized Level is :"<<double(reCloakedLevel/successCount)<<endl;

			cout<<"maximum number of bitset in CRSet:"<<maxCRSet<<endl;
			cout<<"max NN count:"<<maxNNcount<<endl;

			cout<<"Successful"<<endl;
			cout<<"==============================================="<<endl;


			/*wFileStream.open("F:\\RUC\\Experiments\\Test\\data\\trucks\\CDF1_25_3.txt",ios::trunc);
			for(int tt=0;tt<44;tt++)
			wFileStream<<(double)cdf[tt]/successCount<<endl;
			wFileStream<<"======================================"<<endl;
			wFileStream.close();

			wFileStream.open("F:\\RUC\\Experiments\\Test\\data\\trucks\\CDF1_25_3.txt",ios::app);
			for(int tt=0;tt<44;tt++)
			wFileStream<<(double)cdf2[tt]/successCount<<endl;
			wFileStream<<"======================================"<<endl;
			wFileStream.close();

			wFileStream.open("F:\\RUC\\Experiments\\Test\\data\\trucks\\CDF1_25_3.txt",ios::app);
			for(int tt=0;tt<44;tt++)
			wFileStream<<(double)cdf3[tt]/successCount<<endl;
			wFileStream<<"======================================"<<endl;
			wFileStream.close();

			wFileStream.open("F:\\RUC\\Experiments\\Test\\data\\trucks\\CDF1_25_3.txt",ios::app);
			for(int tt=0;tt<44;tt++)
			wFileStream<<(double)cdf4[tt]/successCount<<endl;
			wFileStream<<"======================================"<<endl;
			wFileStream.close();

			wFileStream.open("F:\\RUC\\Experiments\\Test\\data\\trucks\\CDF1_25_3.txt",ios::app);
			for(int tt=0;tt<44;tt++)
			wFileStream<<(double)cdf5[tt]/successCount<<endl;
			wFileStream<<"======================================"<<endl;
			wFileStream.close();

			wFileStream.open("F:\\RUC\\Experiments\\Test\\data\\trucks\\CDF1_25_3.txt",ios::app);
			for(int tt=0;tt<44;tt++)
			wFileStream<<(double)cdf6[tt]/successCount<<endl;
			wFileStream<<"======================================"<<endl;
			wFileStream.close();

			wFileStream.open("F:\\RUC\\Experiments\\Test\\data\\trucks\\CDF1_25_3.txt",ios::app);
			for(int tt=0;tt<44;tt++)
			wFileStream<<(double)cdf7[tt]/successCount<<endl;
			wFileStream<<"======================================"<<endl;
			wFileStream.close();

			wFileStream.open("F:\\RUC\\Experiments\\Test\\data\\trucks\\CDF1_25_3.txt",ios::app);
			for(int tt=0;tt<44;tt++)
			wFileStream<<(double)cdf8[tt]/successCount<<endl;
			wFileStream<<"======================================"<<endl;
			wFileStream.close();

			wFileStream.open("F:\\RUC\\Experiments\\Test\\data\\trucks\\CDF1_25_3.txt",ios::app);
			for(int tt=0;tt<44;tt++)
			wFileStream<<(double)cdf9[tt]/successCount<<endl;
			wFileStream<<"======================================"<<endl;
			wFileStream.close();

			wFileStream.open("F:\\RUC\\Experiments\\Test\\data\\trucks\\CDF1_25_3.txt",ios::app);
			for(int tt=0;tt<44;tt++)
			wFileStream<<(double)cdf10[tt]/successCount<<endl;
			wFileStream<<"======================================"<<endl;
			wFileStream.close();

			wFileStream.open("F:\\RUC\\Experiments\\Test\\data\\trucks\\CDF1_25_3.txt",ios::app);
			for(int tt=0;tt<44;tt++)
			wFileStream<<(double)cdfelse[tt]/successCount<<endl;
			wFileStream<<"======================================"<<endl;
			wFileStream.close();*/
			wFileStream.open("C:\\CDFK1_10_4.txt",ios::trunc);
			for(int tt=0;tt<74;tt++)
				wFileStream<<(double)cdf2[tt]/count2<<endl;
			wFileStream<<"======================================"<<endl;
			wFileStream.close();

			wFileStream.open("C:\\CDFK1_10_4.txt",ios::app);
			for(int tt=0;tt<74;tt++)
				wFileStream<<(double)cdf5[tt]/count5<<endl;
			wFileStream<<"======================================"<<endl;
			wFileStream.close();

			wFileStream.open("C:\\CDFK1_10_4.txt",ios::app);
			for(int tt=0;tt<74;tt++)
				wFileStream<<(double)cdf8[tt]/count8<<endl;
			wFileStream<<"======================================"<<endl;
			wFileStream.close();

			wFileStream.open("C:\\CDFK1_10_4.txt",ios::app);
			for(int tt=0;tt<74;tt++)
				wFileStream<<(double)cdf9[tt]/count9<<endl;
			wFileStream<<"======================================"<<endl;
			wFileStream.close();

		}//if(!rFileStream) else

	}//if (!AfxWinInit(::GetModuleHandle(NULL), NULL, ::GetCommandLine(), 0))

	return nRetCode;
}

//Judgment bt1 bt2, which is a great group .
//Return 1 : Description bt1 contains bt2, bt1 is a great group
//Return 2 : Description bt2 contain bt1, bt2 great group
//Return 3 : The two are not mutually included , are a great group
//Return 4 : Description of the two groups are equal
int MaximalClique(bitset<LoadMoNumber> bt1, bitset<LoadMoNumber> bt2)
{
	
	if(bt1==bt2)
	{
	  return 4;
	}
	else
	{
	  bitset<LoadMoNumber> bt_temp(bt1);
	  bt_temp&=bt2;
	  if(bt_temp==bt1)
		  return 2;
	  else
		  if(bt_temp==bt2)
			  return 1;
		  else
			  return 3;
	}
};

//The two numbers compare , and return a large number of
int maxNN(int n1,int n2)
{
  if(n1>n2) return n1;
  else return n2;
};

//Ensure the insertion of the ascending
void InsertAscending(vector<int> insertList,int key)
{
    int j=0;
    while(j<insertList.size())
	{
	  if(insertList[j]<key)
		  if(j==insertList.size()-1)
			  insertList.push_back(key);
		  else
		      j++;
	  else
		  if(j==0) 
			  insertList.push_back(key);
		  else
		      insertList.insert(insertList.begin()+j,key);
	}
};

bool maintainMaximalClique(vector<bitset<LoadMoNumber>> CRSet, bitset<LoadMoNumber> clique)
{
   //Only contains this id the clique need to determine whether remains one of the greatest
		 int j=0;
		 while(j<CRSet.size())
		 {
		     int maxR;
			 maxR=MaximalClique(CRSet[j],clique);
			 //If CRSet [k] is a subset of the originalCRSet [i] , then CRSet [k] is removed
			 if(maxR==1|| maxR==4) 
			 {
				 //To determine the original the clique in CRSet whether new maximal clique interMC [j] contained
				 return false;
				 //CRSet.erase(CRSet.begin()+k);
			 }
			 else
				 j++;
		 }

		 return true;
	
};
Rect ComputeMBR(vector<int> result,map<int,MO> &moSet)
{
  Rect rect;
  float x,y;

  rect.min[0]=moSet[result[0]].x;
  rect.max[0]=moSet[result[0]].x;
  //cout<<moSet[result[0]].x<<endl;
  //cout<<rect.min[0]<<endl;
  rect.min[1]=moSet[result[0]].y;
  rect.max[1]=moSet[result[0]].y;

  for(int i=1;i<result.size();i++)
  {
    x=moSet[result[i]].x;
	y=moSet[result[i]].y;
    if(x<rect.min[0]) rect.min[0]=x;
    if(y<rect.min[1]) rect.min[1]=y;
	if(x>rect.max[0]) rect.max[0]=x;
    if(y>rect.max[1]) rect.max[1]=y;
  }

  return rect;
};

double averagerandom(double min,double max)
{
int mininteger = (int)(min*1000000);
int maxinteger = (int)(max*1000000);
int randinteger = rand()*rand();
int diffinteger = maxinteger - mininteger;
int resultinteger = randinteger % diffinteger + mininteger;
return resultinteger/10000.0;
};

double FindMaxAmin(bitset<LoadMoNumber>  bt,map<int,MO> &moSet)
{
  double result=0;
  
  for(int i=0;i<LoadMoNumber;i++)
  {
    if(bt.test(i))
	{
	  if(result<moSet[i].Amin)
		  result=moSet[i].Amin;
	}
  }

  return result;
};

/*vector<int> FindMOids(bitset<LoadMoNumber>  bt)
{
   vector<int> result;
   for(int i=0;i<LoadMoNumber;i++)
  {
    if(bt.test(i))
	  result.push(i);
  }
   return result;
};*/

double AreaMBR(bitset<LoadMoNumber>  bt,map<int,MO> &moSet)
{
   Rect rect(FLT_MAX,FLT_MAX,0,0);

   for(int i=0;i<LoadMoNumber;i++)
   {
     if(bt.test(i))
	 {
	   if(rect.min[0]>moSet[i].x)
		   rect.min[0]=moSet[i].x;
	   if(rect.min[1]>moSet[i].y)
		   rect.min[1]=moSet[i].y;
	   if(rect.max[0]<moSet[i].x)
		   rect.max[0]=moSet[i].x;
	   if(rect.max[1]<moSet[i].y)
		   rect.max[1]=moSet[i].y;
	 }
   }

   return (rect.max[0]-rect.min[0])*(rect.max[1]-rect.min[1]);
};

double AreaMBRV(vector<int> vt,map<int,MO> &moSet)
{
   Rect rect(FLT_MAX,FLT_MAX,0,0);

   for(int i=0;i<vt.size();i++)
   {
     
	   if(rect.min[0]>moSet[vt[i]].x)
		   rect.min[0]=moSet[vt[i]].x;
	   if(rect.min[1]>moSet[vt[i]].y)
		   rect.min[1]=moSet[vt[i]].y;
	   if(rect.max[0]<moSet[vt[i]].x)
		   rect.max[0]=moSet[vt[i]].x;
	   if(rect.max[1]<moSet[vt[i]].y)
		   rect.max[1]=moSet[vt[i]].y;
	
   }

   return (rect.max[0]-rect.min[0])*(rect.max[1]-rect.min[1]);
};

bool cmp(clock_t a,clock_t b)

{

    return a>b;

};

void shift(vector<clock_t> &a, int i , int m)
{
int k , t;

    t = a[i]; k = 2 * i + 1;
    while (k < m)
    {
        if ((k < m - 1) && (a[k] < a[k+1])) k ++;
        if (t < a[k]) {a[i] = a[k]; i = k; k = 2 * i + 1;}
        else break;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    
 }
    a[i] = t;
}

void heap_sort(vector<clock_t> &a , int n)  //a sort an array , n is the size of the array (numbered 0 - n -1 )
{
int i , k;

 for (i = n/2-1; i >= 0; i --) shift(a , i , n); 
    for (i = n-1; i >= 1; i --)
    {
  k = a[0]; a[0] = a[i]; a[i] = k;
  shift(a , 0 , i);
    }

}


vector<int> FINDEXPIREDREQUEST(clock_t nowtime,map<int,MO> &moSet)
{
  vector<clock_t> heap;
  vector<int> result;
  //clock_t pre_t;

  for(map<int,MO>::iterator it=moSet.begin();it!=moSet.end();it++)
  {
    heap.push_back((*it).second.enTime);
  }

  if(heap.size()!=0)
  {
	  //sort_heap(heap.begin(),heap.end());
	  //make_heap(heap.begin(),heap.end(),cmp);
	  heap_sort(heap,heap.size());

	 
	  double temp=(double)(nowtime-heap[0])/CLOCKS_PER_SEC;
	  

	  while((MaxTolerantTime-temp)<=0 && heap.size()!=0)
	  {
		  //pre_t=heap[0];
		  for(map<int,MO>::iterator it=moSet.begin();it!=moSet.end();it++)
		  {
			if((*it).second.enTime==heap[0]) 
			{
			  bool exist=0;
			  for(int i=0;i<result.size() && !exist;i++)
				if(result[i]==(*it).first)
					exist=1;
              if(!exist)
			  {
			    result.push_back((*it).first);
			    break;
			  }
			}
		  }
		//heap.erase(heap.begin());
		//sort_heap(heap.begin(),heap.end());
		  //pop_heap(heap.begin(),heap.end());
		   heap[0]=heap[heap.size()-1];
		   heap.pop_back();
		  
		  if(heap.size()!=0)
		  {
			heap_sort(heap,heap.size());
			temp=(double)(nowtime-heap[0])/CLOCKS_PER_SEC;
		  }

		   //Debugger
		  //ofstream wFileStream;
		  //wFileStream.open("F:\\RUC\\Experiments\\Test\\data\\heap.txt",ios::app);
								
								 
		  //for(int ii=0;ii<heap.size();ii++)
		  //{
			   //wFileStream<<heap[ii]<<",";
		  //}
		  //wFileStream<<endl;
		 //wFileStream.close();
		  //end

	  }//?while((MaxTolerantTime-temp)<=0 && heap.size()!=0)
  }

  return result;
}

/*vector<int> FINDEXPIREDREQUEST(clock_t nowtime,map<int,MO> &moSet)
{
  vector<clock_t> heap;
  vector<int> result;

  for(map<int,MO>::iterator it=moSet.begin();it!=moSet.end();it++)
  {
    heap.push_back((*it).second.enTime);
  }

  if(heap.size()!=0)
  {
	  //sort_heap(heap.begin(),heap.end());
	  make_heap(heap.begin(),heap.end(),cmp);

	  double temp=(double)(nowtime-heap[0])/CLOCKS_PER_SEC;

	  while((MaxTolerantTime-temp)<=0 && heap.size()!=0)
	  {
		  for(map<int,MO>::iterator it=moSet.begin();it!=moSet.end();it++)
		  {
			if((*it).second.enTime==heap[0]) 
			{
			  bool exist=0;
			  for(int i=0;i<result.size() && !exist;i++)
				if(result[i]==(*it).first)
					exist=1;
              if(!exist)
			  {
			    result.push_back((*it).first);
			    break;
			  }
			}
		  }
		//heap.erase(heap.begin());
		//sort_heap(heap.begin(),heap.end());
		  pop_heap(heap.begin(),heap.end());
		  heap.pop_back();
		  make_heap(heap.begin(),heap.end(),cmp);
		  if(heap.size()!=0) temp=(double)(nowtime-heap[0])/CLOCKS_PER_SEC;
	  }//?while((MaxTolerantTime-temp)<=0 && heap.size()!=0)
  }

  return result;
}*/


void CanEnlargeDir(Rect pre_R, Rect cur_R,bool &top, bool &bottom, bool &left, bool &right)
{
  if(pre_R.min[0]<cur_R.min[0]) left=1;
  if(pre_R.max[0]>cur_R.max[0]) right=1;
  if(pre_R.max[1]>cur_R.max[1]) top=1;
  if(pre_R.min[1]<cur_R.min[1]) bottom=1;
}

/*Rect FindCloakedRegion(vector<int> CR_mo_id,map<int,MO> &moSet )
{
	 Rect result;
				  map<int,double,less<double>> moid_hd; //Accordance with hdist descending order storage
				  map<int,bitset<4>> moid_dir;
				  typedef pair<int,double> hd_pair;
				  typedef pair<int,bitset<4>> dir_pair;
                    //add by 2009-10-19
				  //while(CR_mo_id.size()>0)
				 // { 
				     Rect can_R=ComputeMBR(CR_mo_id,moSet); //Calculate the size of the the candidate cloaking region
					 Hausdorff haus;
                     
					 //Determine the location of each mo moment whether
					   //Calculate each user hdist ;
					 for(int i=0;i<CR_mo_id.size();i++)
					 {
						 Rect preR=moSet[CR_mo_id[i]].preCR;
						 //Judge this mo users on a moment whether security

						 if(preR.min[0]==0 && preR.min[1]==0 && preR.max[0]==0 && preR.max[1]==0)
						 {
							//Description This user safety, this is the first time CR
						 }
						 else
						 {
							 Hausdorff::Box b_pre(preR.min,preR.max),b_cur(can_R.min,can_R.max);
							 double hdist=haus.h(preR,can_R);
						     if(hdist>moSet[CR_mo_id[i]].maxHdist)
							 {
								 //说明此mo的上一个时刻不安全
							   double delta_d=hdist-moSet[CR_mo_id[i]].maxHdist;
                                 //判断扩展方向
							   bool top=0,bottom=0,left=0,right=0;
                               CanEnlargeDir(pre_R, can_R,top,bottom,left,right);
                               bitset<4> bt;
                               if(top && pre_R.max[1]-can_R.max[1]>delta_d) bt[0]=1; //top
							   if(left && can_R.min[0]-pre_R.min[0]>delta_d) bt[1]=1; //left
							   if(right && pre_R.max[0]-cur_R.max[0]>delta_d) bt[3]=1; //right
							   if(bottom && can_R.min[1]-pre_R.min[1]>delta_d) bt[4]=1; //bottom
							   moid_hd.insert(hd_pair(CR_mo_id[i],insert_dist));
							   moid_dir.insert(dir_pair(CR_mo_id[i],bt));
							 }//?if(hdist<=moSet[CR_mo_id[i]].maxHdist)
						 }//?if(preR.min[0]==0 && preR.min[1]==0 && preR.max[0]==0 && preR.max[1]==0)
					 }//?for(int i=0;i<CR_mo_id.size();i++)

					 //一次pop出delta_d最大的进行扩展，判断扩展后是否依然在mo的原MMB当中
					 map<int,double,less<double>> ::iterator itr;
					 double top_d=0,bottom_d=0,left_d=0,right_d=0;
					
					 for(itr=moid_hd.begin(),itr!=moid_hd.end();itr++)
					 {
						 double maxd=(*itr).second;;
						 //取出扩展方向
						 if(top_d>maxd && bottom_d>maxd && right_d>maxd && left_d)
							 break;
						 else
						 {
							 double check_p[2];
							 switch(moid_dir[(*itr).first])
							 {
							   case 1:
								 //top
								 check_p[0]=can_R.min[0];
								 check_p[1]=can_R.max[1]+maxd;

								 if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								 {
									 CR_mo_id.erase((*itr).first);//删除此mo作为candidate
									 continue;
								 }
							   
								 check_p[0]=can_R.max[0];
								 if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								 {
									 CR_mo_id.erase((*itr).first);//删除此mo作为candidate
									 continue;
								  }
								 if(top_d<maxd) top_d=maxd;
								 break;
							   case 2:
								   //left
								   check_p[0]=can_R.min[0]-maxd;
								   check_p[1]=can_R.min[1];
								   if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									 CR_mo_id.erase((*itr).first);//删除此mo作为candidate
									 continue;
								   }

								   check_p[1]=can_R.max[1];
								   if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									 CR_mo_id.erase((*itr).first);//删除此mo作为candidate
									 continue;
								   }
								   if(left_d<maxd) left_d=maxd;
								   break;
							   case 3:
								   //left top
								   check_p[0]=can_R.min[0]-maxd;
								   check_p[1]=can_R.max[1]+maxd;
								   if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									 CR_mo_id.erase((*itr).first);//删除此mo作为candidate
									 continue;
									}
									if(left_d<maxd) left_d=maxd;
									if(top_d<maxd) top_d=maxd;
								   break;
							   case 4:
								   //right
								   check_p[0]=can_R.max[0]+maxd;
								   check_p[1]=can_R.min[1];
								   if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									 CR_mo_id.erase((*itr).first);//删除此mo作为candidate
									 continue;
								   }

								   check_p[1]=can_R.max[1];
								   if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									 CR_mo_id.erase((*itr).first);//删除此mo作为candidate
									 continue;
								   }

								   if(right_d<maxd) right_d=0;
								   break;
							   case 5:
								   //right top
								   check_p[0]=can_R.max[0]+maxd;
								   check_p[1]=can_R.max[1]+maxd;
								   if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									 CR_mo_id.erase((*itr).first);//删除此mo作为candidate
									 continue;
								   }
								   if(right_d<maxd) right_d=maxd;
								   if(top_d<maxd) top_d=maxd;
								   break;
							   case 6:
								   //right left
								   check_p[0]=can_R.min[0]-maxd;
								   check_p[1]=can_R.min[1];
								   if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									 CR_mo_id.erase((*itr).first);//删除此mo作为candidate
									 continue;
								   }

								   check_p[1]=can_R.max[1];
								   if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									 CR_mo_id.erase((*itr).first);//删除此mo作为candidate
									 continue;
								   }

								   check_p[0]=can_R.max[0]+maxd;
								   check_p[1]=can_R.min[1];
								   if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									 CR_mo_id.erase((*itr).first);//删除此mo作为candidate
									 continue;
								   }

								   check_p[1]=can_R.max[1];
								   if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									 CR_mo_id.erase((*itr).first);//删除此mo作为candidate
									 continue;
								   }

								   if(right_d<maxd) right_d=maxd;
								   if(left_d<maxd) left_d=maxd;
								   break;
							   case 7:
								   //right left top
								   check_p[0]=can_R.max[0]+maxd;
								   check_p[1]=can_R.max[1]+maxd;
								   if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									 CR_mo_id.erase((*itr).first);//删除此mo作为candidate
									 continue;
								   }
								   check_p[0]=can_R.min[0]-maxd;
								   check_p[1]=can_R.max[1]+maxd;
								   if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									 CR_mo_id.erase((*itr).first);//删除此mo作为candidate
									 continue;
									}
								   if(right_d<maxd) right_d=maxd;
								   if(left_d<maxd) left_d=maxd;
								   if(top_d<maxd) top_d=maxd;
								   break;
							   case 8:
								   //bottom
								   checp_p[0]=can_R.min[0];
								   check_p[1]=can_R.min[1]-maxd;
									if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									 CR_mo_id.erase((*itr).first);//删除此mo作为candidate
									 continue;
								   }

									check_p[0]=can_R.max[0];
									 if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									 CR_mo_id.erase((*itr).first);//删除此mo作为candidate
									 continue;
								   }
									if(bottom_d<maxd) bottom_d=maxd;
								   break;
							   case 9:
								   //bottom top
								   check_p[0]=can_R.min[0];
								   check_p[1]=can_R.min[1]-maxd;
									if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									 CR_mo_id.erase((*itr).first);//删除此mo作为candidate
									 continue;
								   }

									check_p[0]=can_R.max[0];
									 if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									 CR_mo_id.erase((*itr).first);//删除此mo作为candidate
									 continue;
								   }

								   check_p[0]=can_R.min[0];
								   check_p[1]=can_R.max[1]+maxd;
								   if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									   CR_mo_id.erase((*itr).first);//删除此mo作为candidate
									   continue;
								   }
							   
								   check_p[0]=can_R.max[0];
								   if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									   CR_mo_id.erase((*itr).first);//删除此mo作为candidate
									   continue;
								   }
								   if(bottom_d<maxd) bottom_d=maxd;
								   if(top_d<maxd) top_d=maxd;
								   break;
							   case 10:
								   //bottom left
								   check_p[0]=can_R.min[0]-maxd;
								   check_p[1]=can_R.min[1]-maxd;
								   if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									   CR_mo_id.erase((*itr).first);//删除此mo作为candidate
									   continue;
								   }
								   if(bottom_d<maxd) bottom_d=maxd;
								   if(left_d<maxd) left_d=maxd;
								   break;
							   case 11:
								   //bottom left top
								   check_p[0]=can_R.min[0]-maxd;
								   check_p[1]=can_R.min[1]-maxd;
								   if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									   CR_mo_id.erase((*itr).first);//删除此mo作为candidate
									   continue;
								   }
								   check_p[0]=can_R.min[0]-maxd;
								   check_p[1]=can_R.max[1]+maxd;
								   if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									 CR_mo_id.erase((*itr).first);//删除此mo作为candidate
									 continue;
									}
								   if(bottom_d<maxd) bottom_d=maxd;
								   if(left_d<maxd) left_d=maxd;
								   if(top_d<maxd) top_d=maxd;
								   break;
							   case 12:
								   //bottom right
								   check_p[0]=can_R.max[0]+maxd;
								   check_p[1]=can_R.min[0]-maxd;
								   check_p[0]=can_R.min[0]-maxd;
								   check_p[1]=can_R.max[1]+maxd;
								   if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									 CR_mo_id.erase((*itr).first);//删除此mo作为candidate
									 continue;
									}
								   if(bottom_d<maxd) bottom_d=maxd;
								   break;
							   case 13:
								   //bottom right top
								   check_p[0]=can_R.max[0]+maxd;
								   check_p[1]=can_R.max[1]+maxd;
								   if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									 CR_mo_id.erase((*itr).first);//删除此mo作为candidate
									 continue;
									}
	                                
								   check_p[1]=can_R.max[1]-maxd;
								   if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									 CR_mo_id.erase((*itr).first);//删除此mo作为candidate
									 continue;
								   }
								   if(bottom_d<maxd) bottom_d=maxd;
								   if(right_d<maxd) right_d=maxd;
								   if(top_d<maxd) top_d=maxd;
								   break;
							   case 14:
								   //bottom right left
								   check_p[1]=can_R.min[1]-maxd;
								   check_p[0]=can_R.min[0]-maxd;
									if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									 CR_mo_id.erase((*itr).first);//删除此mo作为candidate
									 continue;
								   }

									check_p[0]=can_R.max[0]+maxd;
									 if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									 CR_mo_id.erase((*itr).first);//删除此mo作为candidate
									 continue;
								   }
								   if(bottom_d<maxd) bottom_d=maxd;
								   if(right_d<maxd) right_d=maxd;
								   if(left_d<maxd) left_d=maxd;
								   break;
							   case 15:
								   //bottom right left right
								   check_p[1]=can_R.min[1]-maxd;
								   check_p[0]=can_R.min[0]-maxd;
									if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									 CR_mo_id.erase((*itr).first);//删除此mo作为candidate
									 continue;
								   }

									check_p[0]=can_R.max[0]+maxd;
									 if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									 CR_mo_id.erase((*itr).first);//删除此mo作为candidate
									 continue;
								   }

								   check_p[1]=can_R.max[1]+maxd;
								   check_p[0]=can_R.min[0]-maxd;
									if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									 CR_mo_id.erase((*itr).first);//删除此mo作为candidate
									 continue;
								   }

									check_p[0]=can_R.max[0]+maxd;
									 if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									 CR_mo_id.erase((*itr).first);//删除此mo作为candidate
									 continue;
								   }
								   if(bottom_d<maxd) bottom_d=maxd;
								   if(right_d<maxd) right_d=maxd;
								   if(left_d<maxd) left_d=maxd;
								   if(top_d<maxd) top_d=maxd;

							 }//? switch(moid_dir[(*itr).first])
						    
						 }//?if(top_d>maxd && bottom_d>maxd && right_d>maxd && left_d)
						
					 }//? for(int i=0;i<CR_mo_id.size();i++)

					// if(CR_mo_id.size()!=0)
						 //result
				 // }//  while(CR_mo_id.size()>0)
				    //add by 2009-10-19



}*/

Rect FindCloakedRegion(vector<int> CR_mo_id,map<int,MO> &moSet)
{
	 Rect result(0,0,0,0);
				  map<int,double,less<double>> moid_hd; //Accordance with hdist descending order storage
				  map<int,bitset<4>> moid_dir;
				  typedef pair<int,double> hd_pair;
				  typedef pair<int,bitset<4>> dir_pair;
                    //add by 2009-10-19
				  //while(CR_mo_id.size()>0)
				 // { 
				     Rect can_R=ComputeMBR(CR_mo_id,moSet); //Calculate the size of the the candidate cloaking region of
					 //cout<<can_R.min[0]<<" "<<can_R.min[1]<<" "<<can_R.max[0]<<" "<<can_R.max[1]<<endl;
					 Hausdorff haus;
                     
					 //Determine the location of each mo moment whether
					 //Calculate each user hdist ;
					 for(int i=0;i<CR_mo_id.size();i++)
					 {
						 Rect preR=moSet[CR_mo_id[i]].preCR;
						 //Judge this mo users on a moment whether security

						 if(preR.min[0]==0 && preR.min[1]==0 && preR.max[0]==0 && preR.max[1]==0)
						 {
							//Description This user safety, this is the first time CR
							 continue;
						 }
						 else
						 {
							 Box b_pre(preR.min,preR.max),b_cur(can_R.min,can_R.max);
							 double hdist=haus.h(b_pre,b_cur);
						     if(hdist>moSet[CR_mo_id[i]].maxHdist)
							 {
								 //Illustrate this mo on a moment of insecurity
							   double delta_d=hdist-moSet[CR_mo_id[i]].maxHdist;
                                 //If the extension direction
							   bool top=0,bottom=0,left=0,right=0;
                               CanEnlargeDir(preR, can_R,top,bottom,left,right);
                               bitset<4> bt;
                               if(top)
							   {
								   if(left && sqrt((preR.min[0]-can_R.min[0])*(preR.min[0]-can_R.min[0])
									   +(preR.max[1]-can_R.max[1])*(preR.max[1]-can_R.max[1]))>delta_d) {bt[0]=1; bt[1]=1;}
								   if(right && sqrt((preR.max[0]-can_R.max[0])*(preR.max[0]-can_R.max[0])
									   +(preR.max[1]-can_R.max[1])*(preR.max[1]-can_R.max[1]))>delta_d){bt[0]=1; bt[2]=1;}
								   if(!left && !right && preR.max[1]-can_R.max[1]>delta_d) bt[0]=1; 
							   }//top bt[0]=1 bit:bottom right left top
							   if(left &&!top && !bottom && can_R.min[0]-preR.min[0]>delta_d) bt[1]=1; //left
							   if(right  &&!top && !bottom&& preR.max[0]-can_R.max[0]>delta_d) bt[2]=1; //right
							   if(bottom)
							   {
							      if(left && sqrt((preR.min[0]-can_R.min[0])*(preR.min[0]-can_R.min[0])
									   +(preR.min[1]-can_R.min[1])*(preR.min[1]-can_R.min[1]))>delta_d) {bt[0]=1; bt[1]=1;}
								  if(right && sqrt((preR.max[0]-can_R.max[0])*(preR.max[0]-can_R.max[0])
									   +(preR.min[1]-can_R.min[1])*(preR.min[1]-can_R.min[1]))>delta_d){bt[0]=1; bt[2]=1;}
								  if(!left && !right && can_R.min[1]-preR.min[1]>delta_d) bt[3]=1; //bottom
							   }
							   moid_hd.insert(hd_pair(CR_mo_id[i],delta_d));
							   moid_dir.insert(dir_pair(CR_mo_id[i],bt));
							 }//?if(hdist<=moSet[CR_mo_id[i]].maxHdist)
						 }//?if(preR.min[0]==0 && preR.min[1]==0 && preR.max[0]==0 && preR.max[1]==0)
					 }//?for(int i=0;i<CR_mo_id.size();i++)

					 //The original MMB Among time pop out delta_d maximum be extended if the extension is still in mo
					 map<int,double,less<double>> ::iterator itr;
					 double top_d=0,bottom_d=0,left_d=0,right_d=0;
					 bool flag=1;
					 for(itr=moid_hd.begin();itr!=moid_hd.end() && flag;itr++)
					 {
						 double maxd=(*itr).second;;
						 //Remove the expansion direction
						 if(top_d>maxd && bottom_d>maxd && right_d>maxd && left_d>maxd)
							 break;
						 else
						 {
							 double check_p[2];
                             unsigned long int_dir=moid_dir[(*itr).first].to_ulong();
							 switch(int_dir)
							 {
							   case 1:
								 //top
								 check_p[0]=can_R.min[0];
								 check_p[1]=can_R.max[1]+maxd;

								 if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								 {
									 flag=0;
								 }
							   
								 check_p[0]=can_R.max[0];
								 if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								 {
									 flag=0;
								  }
								 if(top_d<maxd) top_d=maxd;
								 break;
							   case 2:
								   //left
								   check_p[0]=can_R.min[0]-maxd;
								   check_p[1]=can_R.min[1];
								   if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									 flag=0;
								   }

								   check_p[1]=can_R.max[1];
								   if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									 flag=0;
								   }
								   if(left_d<maxd) left_d=maxd;
								   break;
							   case 3:
								   //left top
								   check_p[0]=can_R.min[0]-maxd;
								   check_p[1]=can_R.max[1]+maxd;
								   if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									 flag=0;
									}
									if(left_d<maxd) left_d=maxd;
									if(top_d<maxd) top_d=maxd;
								   break;
							   case 4:
								   //right
								   check_p[0]=can_R.max[0]+maxd;
								   check_p[1]=can_R.min[1];
								   if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									 flag=0;
								   }

								   check_p[1]=can_R.max[1];
								   if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									 flag=0;
								   }

								   if(right_d<maxd) right_d=0;
								   break;
							   case 5:
								   //right top
								   check_p[0]=can_R.max[0]+maxd;
								   check_p[1]=can_R.max[1]+maxd;
								   if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									 flag=0;
								   }
								   if(right_d<maxd) right_d=maxd;
								   if(top_d<maxd) top_d=maxd;
								   break;
							   case 6:
								   //right left
								   check_p[0]=can_R.min[0]-maxd;
								   check_p[1]=can_R.min[1];
								   if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									 flag=0;
								   }

								   check_p[1]=can_R.max[1];
								   if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									 flag=0;
								   }

								   check_p[0]=can_R.max[0]+maxd;
								   check_p[1]=can_R.min[1];
								   if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									 flag=0;
								   }

								   check_p[1]=can_R.max[1];
								   if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									 flag=0;
								   }

								   if(right_d<maxd) right_d=maxd;
								   if(left_d<maxd) left_d=maxd;
								   break;
							   case 7:
								   //right left top
								   check_p[0]=can_R.max[0]+maxd;
								   check_p[1]=can_R.max[1]+maxd;
								   if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									 flag=0;
								   }
								   check_p[0]=can_R.min[0]-maxd;
								   check_p[1]=can_R.max[1]+maxd;
								   if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									 flag=0;
									}
								   if(right_d<maxd) right_d=maxd;
								   if(left_d<maxd) left_d=maxd;
								   if(top_d<maxd) top_d=maxd;
								   break;
							   case 8:
								   //bottom
								   check_p[0]=can_R.min[0];
								   check_p[1]=can_R.min[1]-maxd;
									if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									 flag=0;
								   }

									check_p[0]=can_R.max[0];
									 if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									 flag=0;
								   }
									if(bottom_d<maxd) bottom_d=maxd;
								   break;
							   case 9:
								   //bottom top
								   check_p[0]=can_R.min[0];
								   check_p[1]=can_R.min[1]-maxd;
									if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									 flag=0;
								   }

									check_p[0]=can_R.max[0];
									 if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									 flag=0;
								   }

								   check_p[0]=can_R.min[0];
								   check_p[1]=can_R.max[1]+maxd;
								   if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									   flag=0;
								   }
							   
								   check_p[0]=can_R.max[0];
								   if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									   flag=0;
								   }
								   if(bottom_d<maxd) bottom_d=maxd;
								   if(top_d<maxd) top_d=maxd;
								   break;
							   case 10:
								   //bottom left
								   check_p[0]=can_R.min[0]-maxd;
								   check_p[1]=can_R.min[1]-maxd;
								   if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									   flag=0;
								   }
								   if(bottom_d<maxd) bottom_d=maxd;
								   if(left_d<maxd) left_d=maxd;
								   break;
							   case 11:
								   //bottom left top
								   check_p[0]=can_R.min[0]-maxd;
								   check_p[1]=can_R.min[1]-maxd;
								   if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									   flag=0;
								   }
								   check_p[0]=can_R.min[0]-maxd;
								   check_p[1]=can_R.max[1]+maxd;
								   if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									 flag=0;
									}
								   if(bottom_d<maxd) bottom_d=maxd;
								   if(left_d<maxd) left_d=maxd;
								   if(top_d<maxd) top_d=maxd;
								   break;
							   case 12:
								   //bottom right
								   check_p[0]=can_R.max[0]+maxd;
								   check_p[1]=can_R.min[1]-maxd;
								   if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									 flag=0;
									}
								   if(bottom_d<maxd) bottom_d=maxd;
								   if(right_d<maxd) right_d=maxd;
								   break;
							   case 13:
								   //bottom right top
								   check_p[0]=can_R.max[0]+maxd;
								   check_p[1]=can_R.max[1]+maxd;
								   if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									 flag=0;
									}
	                                
								   check_p[1]=can_R.max[1]-maxd;
								   if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									 flag=0;
								   }
								   if(bottom_d<maxd) bottom_d=maxd;
								   if(right_d<maxd) right_d=maxd;
								   if(top_d<maxd) top_d=maxd;
								   break;
							   case 14:
								   //bottom right left
								   check_p[1]=can_R.min[1]-maxd;
								   check_p[0]=can_R.min[0]-maxd;
									if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									 flag=0;
								   }

									check_p[0]=can_R.max[0]+maxd;
									 if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									 flag=0;
								   }
								   if(bottom_d<maxd) bottom_d=maxd;
								   if(right_d<maxd) right_d=maxd;
								   if(left_d<maxd) left_d=maxd;
								   break;
							   case 15:
								   //bottom right left right
								   check_p[1]=can_R.min[1]-maxd;
								   check_p[0]=can_R.min[0]-maxd;
									if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									 flag=0;
								   }

									check_p[0]=can_R.max[0]+maxd;
									 if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									 flag=0;
								   }

								   check_p[1]=can_R.max[1]+maxd;
								   check_p[0]=can_R.min[0]-maxd;
									if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									 flag=0;
								   }

									check_p[0]=can_R.max[0]+maxd;
									 if(!moSet[(*itr).first].mmb.inMMB(check_p[0],check_p[1]))
								   {
									 flag=0;
								   }
								   if(bottom_d<maxd) bottom_d=maxd;
								   if(right_d<maxd) right_d=maxd;
								   if(left_d<maxd) left_d=maxd;
								   if(top_d<maxd) top_d=maxd;

							 }//? switch(moid_dir[(*itr).first])
						    
						 }//?if(top_d>maxd && bottom_d>maxd && right_d>maxd && left_d)
						
					 }//? for(int i=0;i<CR_mo_id.size();i++)

					 if(flag)
					 {
					    result.min[0]=can_R.min[0]-left_d;
						result.min[1]=can_R.min[1]-bottom_d;
						result.max[0]=can_R.max[0]+right_d;
						result.max[1]=can_R.max[1]+top_d;
					 }
	return result;
}

bool CHECKAMAX(Rect CR, float area_max)
{
	//Greater than Amax returns 0 , less than Amax returns 1
	bool result;

	if((CR.max[1]-CR.min[1])*(CR.max[0]-CR.min[0]) > area_max)
		result=0;
	else
		result=1;

	return result;
}


void swap(int &p,int &q)
{
 int t;
 t=p;
 p=q;
 q=t;
}
int partition(vector<int> &b,int s,int t,map<int,MO> &key)//快速排序算法中的一趟划分函数的实现
{
 int i,j,temp;
 i=s;j=t;//对无序区b[s]到b[t]进行划分
 temp=b[i];//基准记录
 do
 {
  while((key[b[j]].k>key[temp].k) && (i<j))
   j--;//从右向左扫描，查找第一个关键字小于temp的记录
  if(i<j)
   swap(b[i++],b[j]);//交换b[i]和b[j];
  while((key[b[i]].k<=key[temp].k) && (i<j))
   i++;//从左向右扫描，查找第一个关键字大于temp的记录
  if(i<j)
   swap(b[j--],b[i]);//交换b[i]和b[j]
 }while(i!=j);//i=j时 一次划分结束，基准记录到达最终位置
 b[i]=temp;//最后将基准记录temp定位
 return i;
}
void quick_sort(vector<int> &c,int hs,int ht,map<int,MO> &key)
{
 int i;
 if(hs<ht)//只有一个或无记录时不须排序
 {
  i=partition(c,hs,ht,key);//对c[hs]到c[ht]进行一次划分
  quick_sort(c,hs,i-1,key);//递归处理左区间
  quick_sort(c,i+1,ht,key);//递归处理右区间
 }
}
void ResultRefiment(vector<int> &CR_mo_id, map<int, MO> &moSet,Rect &CR
		,double area_amax,bool &succflag) {
	Rect findrect;
	//To users CR_mo_id sorted according to the value of k in ascending
	quick_sort(CR_mo_id, 0, CR_mo_id.size() - 1, moSet); //Call the sort function
	while (!CHECKAMAX(CR, area_amax) && CR_mo_id.size() != 0) {
		int delid = CR_mo_id[CR_mo_id.size() - 1];
		CR_mo_id.pop_back();
		if (CR_mo_id.size() != 0) {
			int maxkid = CR_mo_id[CR_mo_id.size() - 1];
			if (CR_mo_id.size() >= moSet[maxkid].k) {
				//Description is postive results
				findrect = ComputeMBR(CR_mo_id, moSet);
				if (findrect.min[0] > CR.min[0] || findrect.min[1] > CR.min[1]
				    || findrect.max[0] < CR.max[0] || findrect.max[1] < CR.max[1]) {
					CR = FindCloakedRegion(CR_mo_id, moSet);
				} //if(findrect.min[0]>CR.min[0] || findrect.min[1]>CR.min[1] ||
			}
		} //if(CR_mo_id.size()!=0)
	} //while(CHECKAMAX())

	if (CHECKAMAX(CR, area_amax) && CR_mo_id.size() != 0)
		succflag = 1;
}

/*void CEHCKCDF(int cdf[], float area_temp,int size)
{
	
	if(area_temp>LEV43) cdf[43]=cdf[43]+size;
	//if(area_temp>LEV43 && area_temp<=LEV44) cdf[43]=cdf[43]+size;
	if(area_temp>LEV42 && area_temp<=LEV43) cdf[42]=cdf[42]+size;
	if(area_temp>LEV41 && area_temp<=LEV42) cdf[41]=cdf[41]+size;
	if(area_temp>LEV40 && area_temp<=LEV41) cdf[40]=cdf[40]+size;
	if(area_temp>LEV39 && area_temp<=LEV40) cdf[39]=cdf[39]+size;
	if(area_temp>LEV38 && area_temp<=LEV39) cdf[38]=cdf[38]+size;
	if(area_temp>LEV37 && area_temp<=LEV38) cdf[37]=cdf[37]+size;
	if(area_temp>LEV36 && area_temp<=LEV37) cdf[36]=cdf[36]+size;
	if(area_temp>LEV35 && area_temp<=LEV36) cdf[35]=cdf[35]+size;
	if(area_temp>LEV34 && area_temp<=LEV35) cdf[34]=cdf[34]+size;
	if(area_temp>LEV33 && area_temp<=LEV34) cdf[33]=cdf[33]+size;
	if(area_temp>LEV32 && area_temp<=LEV33) cdf[32]=cdf[32]+size;
	if(area_temp>LEV31 && area_temp<=LEV32) cdf[31]=cdf[31]+size;
	if(area_temp>LEV30 && area_temp<=LEV31) cdf[30]=cdf[30]+size;
	if(area_temp>LEV29 && area_temp<=LEV30) cdf[29]=cdf[29]+size;
	if(area_temp>LEV28 && area_temp<=LEV29) cdf[28]=cdf[28]+size;
	if(area_temp>LEV27 && area_temp<=LEV28) cdf[27]=cdf[27]+size;
	if(area_temp>LEV26 && area_temp<=LEV27) cdf[26]=cdf[26]+size;
	if(area_temp>LEV25 && area_temp<=LEV26) cdf[25]=cdf[25]+size;
	if(area_temp>LEV24 && area_temp<=LEV25) cdf[24]=cdf[24]+size;
	if(area_temp>LEV23 && area_temp<=LEV24) cdf[23]=cdf[23]+size;
	if(area_temp>LEV22 && area_temp<=LEV23) cdf[22]=cdf[22]+size;
	if(area_temp>LEV21 && area_temp<=LEV22) cdf[21]=cdf[21]+size;
	if(area_temp>LEV20 && area_temp<=LEV21) cdf[20]=cdf[20]+size;
	if(area_temp>LEV19 && area_temp<=LEV20) cdf[19]=cdf[19]+size;
	if(area_temp>LEV18 && area_temp<=LEV19) cdf[18]=cdf[18]+size;
	if(area_temp>LEV17 && area_temp<=LEV18) cdf[17]=cdf[17]+size;
	if(area_temp>LEV16 && area_temp<=LEV17) cdf[16]=cdf[16]+size;
	if(area_temp>LEV15 && area_temp<=LEV16) cdf[15]=cdf[15]+size;
	if(area_temp>LEV14 && area_temp<=LEV15) cdf[14]=cdf[14]+size;
	if(area_temp>LEV13 && area_temp<=LEV14) cdf[13]=cdf[13]+size;
	if(area_temp>LEV12 && area_temp<=LEV13) cdf[12]=cdf[12]+size;
	if(area_temp>LEV11 && area_temp<=LEV12) cdf[11]=cdf[11]+size;
	if(area_temp>LEV10 && area_temp<=LEV11) cdf[10]=cdf[10]+size;
	if(area_temp>LEV9 && area_temp<=LEV10) cdf[9]=cdf[9]+size;
	if(area_temp>LEV8 && area_temp<=LEV9) cdf[8]=cdf[8]+size;
	if(area_temp>LEV7 && area_temp<=LEV8) cdf[7]=cdf[7]+size;
	if(area_temp>LEV6 && area_temp<=LEV7) cdf[6]=cdf[6]+size;
	if(area_temp>LEV5 && area_temp<=LEV6) cdf[5]=cdf[5]+size;
	if(area_temp>LEV4 && area_temp<=LEV5) cdf[4]=cdf[4]+size;
	if(area_temp>LEV3 && area_temp<=LEV4) cdf[3]=cdf[3]+size;
	if(area_temp>LEV2 && area_temp<=LEV3) cdf[2]=cdf[2]+size;
	if(area_temp>LEV1 && area_temp<=LEV2) cdf[1]=cdf[1]+size;
	if(area_temp<=LEV1) cdf[0]=cdf[0]+size;
}*/

void CEHCKCDFK(int cdf[], float area_temp,int &count)
{
	count++;
	if(area_temp>LEV73) cdf[73]=cdf[73]+1;
	if(area_temp>LEV72 && area_temp<=LEV73) cdf[72]=cdf[72]+1;
	if(area_temp>LEV71 && area_temp<=LEV72) cdf[71]=cdf[71]+1;
	if(area_temp>LEV70 && area_temp<=LEV71) cdf[70]=cdf[70]+1;
	if(area_temp>LEV69 && area_temp<=LEV70) cdf[69]=cdf[69]+1;
	if(area_temp>LEV68 && area_temp<=LEV69) cdf[68]=cdf[68]+1;
	if(area_temp>LEV67 && area_temp<=LEV68) cdf[67]=cdf[67]+1;
	if(area_temp>LEV66 && area_temp<=LEV67) cdf[66]=cdf[66]+1;
	if(area_temp>LEV65 && area_temp<=LEV66) cdf[65]=cdf[65]+1;
	if(area_temp>LEV64 && area_temp<=LEV65) cdf[64]=cdf[64]+1;
	if(area_temp>LEV63 && area_temp<=LEV64) cdf[63]=cdf[63]+1;
	if(area_temp>LEV62 && area_temp<=LEV63) cdf[62]=cdf[62]+1;
	if(area_temp>LEV61 && area_temp<=LEV62) cdf[61]=cdf[61]+1;
	if(area_temp>LEV60 && area_temp<=LEV61) cdf[60]=cdf[60]+1;
	if(area_temp>LEV59 && area_temp<=LEV60) cdf[59]=cdf[59]+1;
	if(area_temp>LEV58 && area_temp<=LEV59) cdf[58]=cdf[58]+1;
	if(area_temp>LEV57 && area_temp<=LEV58) cdf[57]=cdf[57]+1;
	if(area_temp>LEV56 && area_temp<=LEV57) cdf[56]=cdf[56]+1;
	if(area_temp>LEV55 && area_temp<=LEV56) cdf[55]=cdf[55]+1;
	if(area_temp>LEV54 && area_temp<=LEV55) cdf[54]=cdf[54]+1;
	if(area_temp>LEV53 && area_temp<=LEV54) cdf[53]=cdf[53]+1;
	if(area_temp>LEV52 && area_temp<=LEV53) cdf[52]=cdf[52]+1;
	if(area_temp>LEV51 && area_temp<=LEV52) cdf[51]=cdf[51]+1;
	if(area_temp>LEV50 && area_temp<=LEV51) cdf[50]=cdf[50]+1;
	if(area_temp>LEV49 && area_temp<=LEV50) cdf[49]=cdf[49]+1;
	if(area_temp>LEV48 && area_temp<=LEV49) cdf[48]=cdf[48]+1;
	if(area_temp>LEV47 && area_temp<=LEV48) cdf[47]=cdf[47]+1;
	if(area_temp>LEV46 && area_temp<=LEV47) cdf[46]=cdf[46]+1;
	if(area_temp>LEV45 && area_temp<=LEV46) cdf[45]=cdf[45]+1;
	if(area_temp>LEV44 && area_temp<=LEV45) cdf[44]=cdf[44]+1;
	if(area_temp>LEV43 && area_temp<=LEV44) cdf[43]=cdf[43]+1;
	if(area_temp>LEV42 && area_temp<=LEV43) cdf[42]=cdf[42]+1;
	if(area_temp>LEV41 && area_temp<=LEV42) cdf[41]=cdf[41]+1;
	if(area_temp>LEV40 && area_temp<=LEV41) cdf[40]=cdf[40]+1;
	if(area_temp>LEV39 && area_temp<=LEV40) cdf[39]=cdf[39]+1;
	if(area_temp>LEV38 && area_temp<=LEV39) cdf[38]=cdf[38]+1;
	if(area_temp>LEV37 && area_temp<=LEV38) cdf[37]=cdf[37]+1;
	if(area_temp>LEV36 && area_temp<=LEV37) cdf[36]=cdf[36]+1;
	if(area_temp>LEV35 && area_temp<=LEV36) cdf[35]=cdf[35]+1;
	if(area_temp>LEV34 && area_temp<=LEV35) cdf[34]=cdf[34]+1;
	if(area_temp>LEV33 && area_temp<=LEV34) cdf[33]=cdf[33]+1;
	if(area_temp>LEV32 && area_temp<=LEV33) cdf[32]=cdf[32]+1;
	if(area_temp>LEV31 && area_temp<=LEV32) cdf[31]=cdf[31]+1;
	if(area_temp>LEV30 && area_temp<=LEV31) cdf[30]=cdf[30]+1;
	if(area_temp>LEV29 && area_temp<=LEV30) cdf[29]=cdf[29]+1;
	if(area_temp>LEV28 && area_temp<=LEV29) cdf[28]=cdf[28]+1;
	if(area_temp>LEV27 && area_temp<=LEV28) cdf[27]=cdf[27]+1;
	if(area_temp>LEV26 && area_temp<=LEV27) cdf[26]=cdf[26]+1;
	if(area_temp>LEV25 && area_temp<=LEV26) cdf[25]=cdf[25]+1;
	if(area_temp>LEV24 && area_temp<=LEV25) cdf[24]=cdf[24]+1;
	if(area_temp>LEV23 && area_temp<=LEV24) cdf[23]=cdf[23]+1;
	if(area_temp>LEV22 && area_temp<=LEV23) cdf[22]=cdf[22]+1;
	if(area_temp>LEV21 && area_temp<=LEV22) cdf[21]=cdf[21]+1;
	if(area_temp>LEV20 && area_temp<=LEV21) cdf[20]=cdf[20]+1;
	if(area_temp>LEV19 && area_temp<=LEV20) cdf[19]=cdf[19]+1;
	if(area_temp>LEV18 && area_temp<=LEV19) cdf[18]=cdf[18]+1;
	if(area_temp>LEV17 && area_temp<=LEV18) cdf[17]=cdf[17]+1;
	if(area_temp>LEV16 && area_temp<=LEV17) cdf[16]=cdf[16]+1;
	if(area_temp>LEV15 && area_temp<=LEV16) cdf[15]=cdf[15]+1;
	if(area_temp>LEV14 && area_temp<=LEV15) cdf[14]=cdf[14]+1;
	if(area_temp>LEV13 && area_temp<=LEV14) cdf[13]=cdf[13]+1;
	if(area_temp>LEV12 && area_temp<=LEV13) cdf[12]=cdf[12]+1;
	if(area_temp>LEV11 && area_temp<=LEV12) cdf[11]=cdf[11]+1;
	if(area_temp>LEV10 && area_temp<=LEV11) cdf[10]=cdf[10]+1;
	if(area_temp>LEV9 && area_temp<=LEV10) cdf[9]=cdf[9]+1;
	if(area_temp>LEV8 && area_temp<=LEV9) cdf[8]=cdf[8]+1;
	if(area_temp>LEV7 && area_temp<=LEV8) cdf[7]=cdf[7]+1;
	if(area_temp>LEV6 && area_temp<=LEV7) cdf[6]=cdf[6]+1;
	if(area_temp>LEV5 && area_temp<=LEV6) cdf[5]=cdf[5]+1;
	if(area_temp>LEV4 && area_temp<=LEV5) cdf[4]=cdf[4]+1;
	if(area_temp>LEV3 && area_temp<=LEV4) cdf[3]=cdf[3]+1;
	if(area_temp>LEV2 && area_temp<=LEV3) cdf[2]=cdf[2]+1;
	if(area_temp>LEV1 && area_temp<=LEV2) cdf[1]=cdf[1]+1;
	if(area_temp<=LEV1) cdf[0]=cdf[0]+1;
}
