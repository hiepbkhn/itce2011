
SRC_DIR := src
TEST_DIR := test

MAIN_PROG := Graph

TEST_NAME := set-cover


BOOST_INCLUDE := E:/caidat/Cpp/boost_1_63_0
BOOST_LIB := E:/caidat/Cpp/boost_1_63_0/stage/lib

BOOST_LIBS := -lboost_system-mgw51-mt-s-1_63 -lboost_filesystem-mgw51-mt-s-1_63
#BOOST_LIBS := -lboost_program_options-mgw51-mt-s-1_63

RM := rm -f

all: 	
	@echo 'Compiling $(MAIN_PROG) program...';

	# for C++11 (Debug: -O0) (Release: -O2 -s -DNDEBUG)
	g++ -std=c++11 -Wall -c -O2 -s -DNDEBUG $(SRC_DIR)/*.cpp;
	g++ -o bin/$(MAIN_PROG).exe map_loader.o geom_util.o tuple.o $(MAIN_PROG).o;
	

	@echo 'Done';
	
exp:
	@echo 'Compiling $(TEST_NAME) program...';
	# for C++11 (Debug: -O0) (Release: -O2 -s -DNDEBUG)
	g++ -std=c++11 -Wall -c -O2 -s -DNDEBUG $(TEST_DIR)/$(TEST_NAME).cpp;
	g++ -o bin/$(TEST_NAME).exe $(TEST_NAME).o;
	
	@echo 'Done';

# 
clean:	
	@echo 'Cleaning...';
	-$(RM) out/$(TEST_NAME).o out/$(TEST_NAME).exe;
	@echo 'Done';

	