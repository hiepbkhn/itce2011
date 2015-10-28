#
# Makefile for non-Microsoft compilers
#	tested only on Linux

#
#	Global configuration for SNAP makefiles
#

GLIB = glib-core
SNAP = snap-core
GLIBADV = glib-adv
SNAPADV = snap-adv


CGLIB = ../include/snap/$(GLIB)
CSNAP = ../include/snap/$(SNAP)

EXGLIB = ../include/snap/$(GLIB)
EXSNAP = ../include/snap/$(SNAP)
EXGLIBADV = ../include/snap/$(GLIBADV)
EXSNAPADV = ../include/snap/$(SNAPADV)


UNAME := $(shell uname)

ifeq ($(UNAME), Linux)
  # Linux flags
  CC = g++
  CXXFLAGS += -std=c++98 -Wall
  CXXFLAGS += -O3 -DNDEBUG
  # turn on for crash debugging, get symbols with <prog> 2>&1 | c++filt
  #CXXFLAGS += -g -rdynamic
  CXXOPENMP = -fopenmp
  LDFLAGS +=
  LIBS += -lrt

else ifeq ($(UNAME), Darwin)
  # OS X flags
  CC = g++
  CXXFLAGS += -std=c++98 -Wall
  CXXFLAGS += -O3 -DNDEBUG
  CXXOPENMP = -fopenmp
  LDFLAGS +=
  LIBS +=

else ifeq ($(shell uname -o), Cygwin)
  # Cygwin flags
  CC = g++
  CXXFLAGS += -Wall -D__STDC_LIMIT_MACROS
  CXXFLAGS += -O3 -DNDEBUG
  CXXOPENMP = -fopenmp
  LDFLAGS +=
  LIBS +=

endif

MAIN=network
# Define a variable for classpath
CLASS_PATH = ../bin

# Define a virtual path for .class in the bin directory
vpath %.class $(CLASS_PATH)

DEPH = $(EXSNAP)/network.h
DEPCPP = $(EXSNAP)/network.cpp



## Main application file
DEMOS = \
	demo-TUNGraph demo-TNGraph demo-TNEGraph \
	demo-TNodeNet demo-TNodeEDatNet demo-TNodeEdgeNet \
        demo-TNEANet \
	demo-subgraph demo-ggen \
	demo-gio demo-gviz \
	demo-alg demo-cncom demo-cncom1 demo-bfsdfs \
	demo-triad \
	demo-THash \
	demo-topology-benchmark \
	demo-hashvec-benchmark \
	\

all-tutorials: $(DEMOS)

# COMPILE
demo-%: demo-%.cpp 
	g++ $(CXXFLAGS) -o $@ $@.cpp ../out/Snap.o -I$(CSNAP) -I$(CGLIB) $(LDFLAGS) $(LIBS)

clean:
	rm -f *.o $(DEMOS) *.exe
	rm -rf Debug Release
	rm -rf *.Err demo*.dat