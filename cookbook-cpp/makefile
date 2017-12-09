
SRC_DIR := benchmark
#SRC_DIR := src-container
#SRC_DIR := src-algorithm
#SRC_DIR := src-other
#SRC_DIR := boost
#SRC_DIR := gmp
#SRC_DIR := parallel
#SRC_DIR := matio

INCLUDE_DIR := header
# benchmark
TEST_NAME := matrix-mul

#
#TEST_NAME := basic-seq-technique
#TEST_NAME := stack-queue-test
#TEST_NAME := user-def-obj
#TEST_NAME := map-test
#TEST_NAME := map-complex
#TEST_NAME := use-map
#TEST_NAME := multimap-test
#TEST_NAME := set-test
#TEST_NAME := cout-overload
#TEST_NAME := vector-test
#TEST_NAME := map-simple
#TEST_NAME := priority-queue

#TEST_NAME := permutation-test
#TEST_NAME := unique-test
#TEST_NAME := less-test
#TEST_NAME := for-each-test
#TEST_NAME := binder-test
#TEST_NAME := new-algo-test
#TEST_NAME := vector-set-op
#TEST_NAME := vector-unordered-set-op
#TEST_NAME := unique-test

#TEST_NAME := random-test
#TEST_NAME := regex-test
#TEST_NAME := system-call
#TEST_NAME := io-test
#TEST_NAME := output-param

#TEST_NAME := single-core
#TEST_NAME := multi-thread
#TEST_NAME := multi-worker
#TEST_NAME := thread-demo

# Boost
#TEST_NAME := ex_lambda
#TEST_NAME := ex_filesize
#TEST_NAME := ex_prog_options
#TEST_NAME := ex_shared_mem
#TEST_NAME := ex_shared_mem_windows
#TEST_NAME := ex_managed_shared_mem
#TEST_NAME := ex_managed_shared_containers
#TEST_NAME := ex_thread
#TEST_NAME := ex_thread_interrupt
#TEST_NAME := ex_mutex
#TEST_NAME := ex_shared_lock
#TEST_NAME := ex_condition_variable
#TEST_NAME := ex_thread_local
#TEST_NAME := ex_future_promise

# GMP
#TEST_NAME := gmp-demo

# MATIO
#TEST_NAME := test_mat


#BOOST_INCLUDE := E:/caidat/Cpp/boost_1_46_1
#BOOST_LIB := E:/caidat/Cpp/boost_1_46_1/lib

BOOST_INCLUDE := F:/caidat/Cpp/boost_1_57_0_mingw_x64/include/boost-1_57
BOOST_LIB := F:/caidat/Cpp/boost_1_57_0_mingw_x64/lib-mgw51/

#BOOST_INCLUDE := E:/caidat/Cpp/boost_1_63_0
#BOOST_LIB := E:/caidat/Cpp/boost_1_63_0/stage/lib

#BOOST_LIBS := -lboost_system-mgw51-mt-s-1_63 -lboost_filesystem-mgw51-mt-s-1_63
#BOOST_LIBS := -lboost_program_options-mgw51-mt-s-1_63
#BOOST_LIBS := -lboost_thread-mgw51-mt-1_57 -lboost_chrono-mgw51-mt-1_57 -lboost_system-mgw51-mt-1_57
BOOST_LIBS := -lboost_thread-mgw51-mt-1_57 -lboost_system-mgw51-mt-1_57

# GMP
GMP_INCLUDE := F:/caidat/Cpp/gmp-5.1.3
GMP_LIB := F:/caidat/Cpp/gmp-5.1.3/.libs
GMP_LIBS := -lgmp

# MATIO
MATIO_INCLUDE := F:/caidat/Cpp/matio-1.5.10/src
MATIO_LIB := F:/caidat/Cpp/matio-1.5.10/src/.libs
MATIO_LIBS := -lmatio

#
RM := rm -f

all: 	
	@echo 'Compiling $(TEST_NAME) example...';
	# benchmark
	g++ -std=c++11 -Wall -c -O3 -s -DNDEBUG -o out/$(TEST_NAME).o $(SRC_DIR)/$(TEST_NAME).cpp;
	g++ -o out/$(TEST_NAME).exe out/$(TEST_NAME).o;
	
	#
#	g++ -Wall -c -DDEBUG -O0 -o out/$(TEST_NAME).o $(SRC_DIR)/$(TEST_NAME).cpp;
#	g++ -o out/$(TEST_NAME).exe out/$(TEST_NAME).o;
	
	# for C++11 (Debug: -O0)
#	g++ -std=c++11 -Wall -c -O0 -o out/$(TEST_NAME).o $(SRC_DIR)/$(TEST_NAME).cpp;
#	g++ -o out/$(TEST_NAME).exe out/$(TEST_NAME).o;
	
	# for C++11 (Release: -O2 -s -DNDEBUG)
#	g++ -std=c++11 -Wall -c -O2 -s -DNDEBUG -o out/$(TEST_NAME).o $(SRC_DIR)/$(TEST_NAME).cpp;
#	g++ -o out/$(TEST_NAME).exe out/$(TEST_NAME).o;

	# for BOOST examples only (-DDEBUG)  
#	g++ -std=c++11 -I"$(BOOST_INCLUDE)" -Wall -c -O2 -s -DNDEBUG -o out/$(TEST_NAME).o $(SRC_DIR)/$(TEST_NAME).cpp;
#	g++ -o out/$(TEST_NAME).exe out/$(TEST_NAME).o;
	
	# for BOOST with compiled libs (e.g. program_options)
#	g++ -std=c++11 -I"$(BOOST_INCLUDE)" -Wall -c -O2 -s -DNDEBUG -o out/$(TEST_NAME).o $(SRC_DIR)/$(TEST_NAME).cpp;
#	g++ -o out/$(TEST_NAME).exe out/$(TEST_NAME).o -L"$(BOOST_LIB)" ${BOOST_LIBS}


	# GMP
#	g++ -I"$(GMP_INCLUDE)" -Wall -c -O0 -o out/$(TEST_NAME).o $(SRC_DIR)/$(TEST_NAME).cpp;
#	g++ -o out/$(TEST_NAME).exe out/$(TEST_NAME).o -L"$(GMP_LIB)" ${GMP_LIBS}

	# MATIO
#	gcc -I${MATIO_INCLUDE} -Wall -c -O2 -s -DNDEBUG -o out/$(TEST_NAME).o $(SRC_DIR)/$(TEST_NAME).c;
#	gcc -o out/$(TEST_NAME).exe out/$(TEST_NAME).o -L"$(MATIO_LIB)" ${MATIO_LIBS}


	@echo 'Done';
	

# http://www.codeproject.com/Articles/84461/MinGW-Static-and-Dynamic-Libraries
static-lib:
	gcc -c test/add.c -o out/add.o
	ar rcs out/libadd.a out/add.o
	
static-link:
	gcc -c test/main.c -o out/main.o
	gcc -o out/mainStatic.exe out/main.o -Lout/ -ladd	
	
dynamic-lib:
	gcc -c test/add.c -o out/add.o
	gcc -shared -o out/libadd.so out/add.o
	
dynamic-link:	
	gcc -o out/mainDynamic.exe test/main.c out/libadd.so	
	
#	
test:
	gcc -c test/main.c -o out/main.o
	gcc -o out/main.exe out/main.o	
	
# http://bisqwit.iki.fi/story/howto/openmp/
mandelbrot-fractal:
	g++ -fopenmp openmp/mandelbrot-fractal.cpp -o out/mandelbrot-fractal.exe
mandelbrot-fractal-no-mp:	
	g++ openmp/mandelbrot-fractal.cpp -o out/mandelbrot-fractal-no-mp.exe		
	
#
Item20PassByRef:
	g++ effective/Item20PassByRef.cpp -o out/Item20PassByRef.exe	

# 
clean:	
	@echo 'Cleaning...';
	-$(RM) out/$(TEST_NAME).o out/$(TEST_NAME).exe;
	@echo 'Done';

	