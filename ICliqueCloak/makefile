
SRC_DIR := src

TEST_NAME := MMB-Win32


BOOST_INCLUDE := E:/caidat/Cpp/boost_1_63_0
BOOST_LIB := E:/caidat/Cpp/boost_1_63_0/stage/lib

BOOST_LIBS := -lboost_system-mgw51-mt-s-1_63 -lboost_filesystem-mgw51-mt-s-1_63
#BOOST_LIBS := -lboost_program_options-mgw51-mt-s-1_63

# GMP
GMP_INCLUDE := F:/caidat/Cpp/gmp-5.1.3
GMP_LIB := F:/caidat/Cpp/gmp-5.1.3/.libs
GMP_LIBS := -lgmp

RM := rm -f

all: 	
	@echo 'Compiling $(TEST_NAME) program...';

	# for C++11 (Debug: -O0) (Release: -O2 -s -DNDEBUG)
	g++ -m32 -std=c++11 -Wall -c -O2 -s -DNDEBUG $(SRC_DIR)/*.cpp;
	g++ -m32 -o bin/$(TEST_NAME).exe *.o;
	

	@echo 'Done';
	

# 
clean:	
	@echo 'Cleaning...';
	-$(RM) out/$(TEST_NAME).o out/$(TEST_NAME).exe;
	@echo 'Done';

	