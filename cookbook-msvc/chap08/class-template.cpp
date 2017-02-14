#include <iostream>
#include <string>

using namespace std;

template<typename T>
class TreeNode {
public:
	TreeNode(const T& val) : val_(val), left_(NULL), right_(NULL) {}
	~TreeNode() {
		delete left_;
		delete right_;
	}

	const T& getVal() const { return(val_); }

	void setVal(const T& val) { val_ = val; }

	void addChild(TreeNode<T>* p) {
		const T& other = p->getVal();

		// debug
		cout << other << " " << val_ << endl;

		if (other > val_) {
			if (right_)
				right_->addChild(p);
			else
				right_ = p;
		}
		else {
			if (left_)
				left_->addChild(p);
			else
				left_ = p;
		}
		//debug
		cout << "finished" << endl;

	}
	const TreeNode<T>* getLeft() { return(left_); }
	const TreeNode<T>* getRight() { return(right_); }

private:
	T val_;
	TreeNode<T>* left_;
	TreeNode<T>* right_;
};

int main() {

	// exception code c0000374
	string a("frank");
	a = a + "bc";
	TreeNode<string> node1(a);
	TreeNode<string> node2("larry");
	TreeNode<string> node3("will");
	node1.addChild(&node2);
	node1.addChild(&node3);

	//
	//TreeNode<int> node1(7);
	//TreeNode<int> node2(11);
	//TreeNode<int> node3(3);
	//TreeNode<int> node4(5);
	//node1.addChild(&node2);
	//node1.addChild(&node3);
	//node1.addChild(&node4);

}
