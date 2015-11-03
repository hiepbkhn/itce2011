
SRC_DIR := src

INCLUDE_DIR := header

BOOST_INCLUDE_32 := E:/caidat/Cpp/boost_1_46_1
#BOOST_INCLUDE_32 := E:/caidat/Cpp/boost_1_57_0
BOOST_INCLUDE := E:/caidat/Cpp/boost_1_57_0_mingw_x64/include/boost-1_57

BOOST_LIB_32 := E:/caidat/Cpp/boost_1_46_1/lib
#BOOST_LIB_32 := E:/caidat/Cpp/boost_1_57_0/stage/lib
BOOST_LIB := E:/caidat/Cpp/boost_1_57_0_mingw_x64/lib


OPT := -O0 -g3 -Wall -c -fmessage-length=0
#OPT := -O0 -g3 -Wall -c -fmessage-length=0 -static

LIBS_32 := -lboost_program_options-mgw46-mt-1_46_1
#LIBS_32 := -lboost_program_options-mgw48-mt-1_57 
LIBS := -lboost_program_options-mgw48-mt-1_57


RM := rm -f

all: dkUtils dkRewire dkDist dkRescale dkMetrics dkTopoGen0k dkTopoGen1k dkTopoGen2k	

dkUtils:
	g++ -I$(INCLUDE_DIR) -I$(BOOST_INCLUDE) ${OPT} -o out2/dkUtils.o $(SRC_DIR)/dkUtils.cc

dkRewire: 	
	@echo 'Compiling dkRewire ...';
	g++ -I$(INCLUDE_DIR) -I$(BOOST_INCLUDE) ${OPT} -o out2/dkRewire.o $(SRC_DIR)/dkRewire.cc
	g++ -o out2/dkRewire.exe out2/dkUtils.o out2/dkRewire.o
	@echo 'Done';

dkDist:	
	@echo 'Compiling dkDist ...';
	g++ -I$(INCLUDE_DIR) -I$(BOOST_INCLUDE) ${OPT} -o out2/dkDist.o $(SRC_DIR)/dkDist.cc
	g++ -L$(BOOST_LIB) -o out2/dkDist.exe out2/dkUtils.o out2/dkDist.o ${LIBS}
	@echo 'Done';
	
dkRescale:	
	@echo 'Compiling dkRescale ...';
	g++ -I$(INCLUDE_DIR) -I$(BOOST_INCLUDE) ${OPT} -o out2/dkRescale.o $(SRC_DIR)/dkRescale.cc
	g++ -L$(BOOST_LIB) -o out2/dkRescale.exe out2/dkUtils.o out2/dkRescale.o ${LIBS}
	@echo 'Done';
	
dkMetrics:	
	@echo 'Compiling dkDist ...';
	g++ -I$(INCLUDE_DIR) -I$(BOOST_INCLUDE) ${OPT} -o out2/dkMetrics.o $(SRC_DIR)/dkMetrics.cc
	g++ -L$(BOOST_LIB) -o out2/dkMetrics.exe out2/dkUtils.o out2/dkMetrics.o ${LIBS}
	@echo 'Done';
 
dkTopoGen0k:	
	@echo 'Compiling dkTopoGen0k ...';
	g++ -I$(INCLUDE_DIR) -I$(BOOST_INCLUDE) ${OPT} -o out2/dkTopoGen0k.o $(SRC_DIR)/dkTopoGen0k.cc
	g++ -L$(BOOST_LIB) -o out2/dkTopoGen0k.exe out2/dkUtils.o out2/dkTopoGen0k.o ${LIBS}
	@echo 'Done';

dkTopoGen1k:	
	@echo 'Compiling dkTopoGen1k ...';
	g++ -I$(INCLUDE_DIR) -I$(BOOST_INCLUDE) ${OPT} -o out2/dkTopoGen1k.o $(SRC_DIR)/dkTopoGen1k.cc
	g++ -L$(BOOST_LIB) -o out2/dkTopoGen1k.exe out2/dkUtils.o out2/dkTopoGen1k.o ${LIBS}
	@echo 'Done';	
	
dkTopoGen2k:	
	@echo 'Compiling dkTopoGen2k ...';
	g++ -I$(INCLUDE_DIR) -I$(BOOST_INCLUDE) ${OPT} -o out2/dkTopoGen2k.o $(SRC_DIR)/dkTopoGen2k.cc
	g++ -L$(BOOST_LIB) -o out2/dkTopoGen2k.exe out2/dkUtils.o out2/dkTopoGen2k.o ${LIBS}
	@echo 'Done';	

dkTopoGen1k_new: dkUtils	
	@echo 'Compiling dkTopoGen1k_new ...';
	g++ -I$(INCLUDE_DIR) -I$(BOOST_INCLUDE) ${OPT} -o out2/dkTopoGen1k_new.o $(SRC_DIR)/dkTopoGen1k_new.cc
	g++ -L$(BOOST_LIB) -o out2/dkTopoGen1k_new.exe out2/dkUtils.o out2/dkTopoGen1k_new.o ${LIBS}
	@echo 'Done';	
	
dkTopoGen1k_stub: dkUtils	
	@echo 'Compiling dkTopoGen1k_stub ...';
	g++ -I$(INCLUDE_DIR) -I$(BOOST_INCLUDE) ${OPT} -o out2/dkTopoGen1k_stub.o $(SRC_DIR)/dkTopoGen1k_stub.cc
	g++ -L$(BOOST_LIB) -o out2/dkTopoGen1k_stub.exe out2/dkUtils.o out2/dkTopoGen1k_stub.o ${LIBS}
	@echo 'Done';	
		
shuffle1k: dkUtils	
	@echo 'Compiling shuffle1k ...';
	g++ -I$(INCLUDE_DIR) -I$(BOOST_INCLUDE) ${OPT} -o out2/shuffle1k.o $(SRC_DIR)/shuffle1k.cc
	g++ -L$(BOOST_LIB) -o out2/shuffle1k.exe out2/dkUtils.o out2/shuffle1k.o ${LIBS}
	@echo 'Done';			

clean:	
	@echo 'Cleaning...';
	-$(RM) out2/dkUtils.o out2/dkRewire.o out2/dkDist.o out2/dkRescale.o out2/dkMetrics.o out2/dkTopoGen0k.o \
	out2/dkTopoGen1k.o out2/dkTopoGen2k.o out2/dkTopoGen1k_new.o;
	-$(RM) out2/dkRewire.exe out2/dkDist.exe out2/dkRescale.exe out2/dkMetrics.exe out2/dkTopoGen0k.exe \
	out2/dkTopoGen1k.exe out2/dkTopoGen2k.exe out2/dkTopoGen1k_new.exe;
	@echo 'Done';


################ 32 bit #################
dkUtils_32:
	g++ -I$(INCLUDE_DIR) -I$(BOOST_INCLUDE_32) ${OPT} -m32 -o out2/dkUtils_32.o $(SRC_DIR)/dkUtils.cc

dkRewire_32: 	
	@echo 'Compiling dkRewire ...';
	g++ -I$(INCLUDE_DIR) -I$(BOOST_INCLUDE_32) ${OPT} -m32 -o out2/dkRewire_32.o $(SRC_DIR)/dkRewire.cc
	g++ -m32 -o out2/dkRewire_32.exe out2/dkUtils_32.o out2/dkRewire_32.o
	@echo 'Done';

dkDist_32:	
	@echo 'Compiling dkDist ...';
	g++ -I$(INCLUDE_DIR) -I$(BOOST_INCLUDE_32) ${OPT} -m32 -o out2/dkDist_32.o $(SRC_DIR)/dkDist.cc
	g++ -L$(BOOST_LIB_32) -m32 -o out2/dkDist_32.exe out2/dkUtils_32.o out2/dkDist_32.o ${LIBS_32}
	@echo 'Done';
	
dkRescale_32:	
	@echo 'Compiling dkRescale ...';
	g++ -I$(INCLUDE_DIR) -I$(BOOST_INCLUDE_32) ${OPT} -m32 -o out2/dkRescale_32.o $(SRC_DIR)/dkRescale.cc
	g++ -L$(BOOST_LIB_32) -m32 -o out2/dkRescale_32.exe out2/dkUtils_32.o out2/dkRescale_32.o ${LIBS_32}
	@echo 'Done';
	
dkMetrics_32:	
	@echo 'Compiling dkDist ...';
	g++ -I$(INCLUDE_DIR) -I$(BOOST_INCLUDE_32) ${OPT} -m32 -o out2/dkMetrics_32.o $(SRC_DIR)/dkMetrics.cc
	g++ -L$(BOOST_LIB_32) -m32 -o out2/dkMetrics_32.exe out2/dkUtils_32.o out2/dkMetrics_32.o ${LIBS_32}
	@echo 'Done';
 
dkTopoGen0k_32:	
	@echo 'Compiling dkTopoGen0k ...';
	g++ -I$(INCLUDE_DIR) -I$(BOOST_INCLUDE_32) ${OPT} -m32 -o out2/dkTopoGen0k_32.o $(SRC_DIR)/dkTopoGen0k.cc
	g++ -L$(BOOST_LIB_32) -m32 -o out2/dkTopoGen0k_32.exe out2/dkUtils_32.o out2/dkTopoGen0k_32.o ${LIBS_32}
	@echo 'Done';

dkTopoGen1k_32:	
	@echo 'Compiling dkTopoGen1k ...';
	g++ -I$(INCLUDE_DIR) -I$(BOOST_INCLUDE_32) ${OPT} -m32 -o out2/dkTopoGen1k_32.o $(SRC_DIR)/dkTopoGen1k.cc
	g++ -L$(BOOST_LIB_32) -m32 -o out2/dkTopoGen1k_32.exe out2/dkUtils_32.o out2/dkTopoGen1k_32.o ${LIBS_32}
	@echo 'Done';	
	
dkTopoGen2k_32:	
	@echo 'Compiling dkTopoGen2k ...';
	g++ -I$(INCLUDE_DIR) -I$(BOOST_INCLUDE_32) ${OPT} -m32 -o out2/dkTopoGen2k_32.o $(SRC_DIR)/dkTopoGen2k.cc
	g++ -L$(BOOST_LIB_32) -m32 -o out2/dkTopoGen2k_32.exe out2/dkUtils_32.o out2/dkTopoGen2k_32.o ${LIBS_32}
	@echo 'Done';		
	
	