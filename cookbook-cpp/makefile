
#SRC_DIR := src-container
#SRC_DIR := src-algorithm
#SRC_DIR := src-other
SRC_DIR := boost

INCLUDE_DIR := header

#TEST_NAME := basic-seq-technique
#TEST_NAME := stack-queue-test
#TEST_NAME := user-def-obj
#TEST_NAME := map-test
#TEST_NAME := use-map
#TEST_NAME := multimap-test
#TEST_NAME := set-test
#TEST_NAME := cout-overload

#TEST_NAME := permutation-test
#TEST_NAME := unique-test
#TEST_NAME := less-test
#TEST_NAME := for-each-test
#TEST_NAME := binder-test
#TEST_NAME := new-algo-test

#TEST_NAME := random-test

# Boost
#TEST_NAME := ex_lambda
TEST_NAME := ex_filesize
#TEST_NAME := ex_prog_options


#BOOST_INCLUDE := E:/caidat/Cpp/boost_1_46_1
#BOOST_LIB := E:/caidat/Cpp/boost_1_46_1/lib

#BOOST_INCLUDE := E:/caidat/Cpp/boost_1_57_0_mingw_x64/include/boost-1_57
#BOOST_LIB := E:/caidat/Cpp/boost_1_57_0_mingw_x64/

BOOST_INCLUDE := E:/caidat/Cpp/boost_1_63_0
BOOST_LIB := E:/caidat/Cpp/boost_1_63_0/stage/lib

#LIBS :=
LIBS := -lboost_system-mgw51-mt-s-1_63 -lboost_filesystem-mgw51-mt-s-1_63
#LIBS := -lboost_program_options-mgw51-mt-s-1_63

RM := rm -f

all: 	
	@echo 'Compiling $(TEST_NAME) example...';

#	g++ -Wall -c -DDEBUG -O0 -o out/$(TEST_NAME).o $(SRC_DIR)/$(TEST_NAME).cpp;
#	g++ -o out/$(TEST_NAME).exe out/$(TEST_NAME).o;
	
	# for C++11
#	g++ -std=c++11 -Wall -c -O0 -o out/$(TEST_NAME).o $(SRC_DIR)/$(TEST_NAME).cpp;
#	g++ -o out/$(TEST_NAME).exe out/$(TEST_NAME).o;
	
	# for boost examples only (-DDEBUG)  -std=c++11 (program_options)
	g++ -I"$(BOOST_INCLUDE)" -Wall -c -O0 -o out/$(TEST_NAME).o $(SRC_DIR)/$(TEST_NAME).cpp;
#	g++ -std=c++11 -I"$(BOOST_INCLUDE)" -Wall -c -O0 -o out/$(TEST_NAME).o $(SRC_DIR)/$(TEST_NAME).cpp;

	g++ -o out/$(TEST_NAME).exe out/$(TEST_NAME).o -L"$(BOOST_LIB)" ${LIBS}

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

	