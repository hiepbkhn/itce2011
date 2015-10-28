#
#	Global configuration for SNAP makefiles
#

GLIB = glib-core
SNAP = snap-core
GLIBADV = glib-adv
SNAPADV = snap-adv


CGLIB = include/snap/$(GLIB)
CSNAP = include/snap/$(SNAP)

EXGLIB = include/snap/$(GLIB)
EXSNAP = include/snap/$(SNAP)
EXGLIBADV = include/snap/$(GLIBADV)
EXSNAPADV = include/snap/$(SNAPADV)


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

#all :testSnap

testSnap: src/demo-TNodeEDatNet.cpp Snap.o 
	g++ -o out/testSnap src/demo-TNodeEDatNet.cpp out/Snap.o -I$(CSNAP) -I$(CGLIB) $(LDFLAGS) $(LIBS)

testgraph: src/testgraph.cpp #Snap.o 
	g++ -o out/testgraph src/testgraph.cpp out/Snap.o -I$(CSNAP) -I$(CGLIB) $(LDFLAGS) $(LIBS)


bigclam: examples/bigclam.cpp # Snap.o 
	g++ -o out/bigclam examples/bigclam.cpp $(EXSNAPADV)/agm.cpp $(EXSNAPADV)/agmfit.cpp $(EXSNAPADV)/agmfast.cpp out/Snap.o \
	-I$(CSNAP) -I$(CGLIB) -I${EXSNAPADV} $(LDFLAGS) $(LIBS)	$(CXXOPENMP) 	

kronfit: examples/kronfit.cpp # Snap.o 
	g++ -o out/kronfit examples/kronfit.cpp $(EXSNAPADV)/kronecker.cpp out/Snap.o \
	-I$(CSNAP) -I$(CGLIB) -I${EXSNAPADV} $(LDFLAGS) $(LIBS)	$(CXXOPENMP)
	
kronem: examples/kronem.cpp # Snap.o 
	g++ -o out/kronem examples/kronem.cpp $(EXSNAPADV)/kronecker.cpp out/Snap.o \
	-I$(CSNAP) -I$(CGLIB) -I${EXSNAPADV} $(LDFLAGS) $(LIBS)	$(CXXOPENMP)	

Snap.o: include/snap/snap-core/*.h include/snap/snap-core/*.cpp include/snap/glib-core/*.h include/snap/glib-core/*.cpp
	 g++ -c include/snap/snap-core/Snap.cpp -I$(CSNAP) -I$(CGLIB) $(CXXFLAGS) $(LDFLAGS) $(LIBS) -o out/Snap.o

# same as above !
#Snap.o: 
#	 g++ -c include/snap/snap-core/Snap.cpp -I$(CSNAP) -I$(CGLIB) $(CXXFLAGS) $(LDFLAGS) $(LIBS) -o out/Snap.o


#
CXXOPENMP = -fopenmp

Snap-adv.o: include/snap/snap-core/*.h include/snap/snap-core/*.cpp include/snap/glib-core/*.h include/snap/glib-core/*.cpp include/snap/snap-adv/*.h include/snap/snap-adv/*.cpp
	 g++ -c include/snap/snap-core/Snap.cpp -I$(CSNAP) -I$(CGLIB) -I${EXSNAPADV} $(CXXFLAGS) $(LDFLAGS) $(LIBS) -o out/Snap-adv.o
	 
Snap-adv.so: include/snap/snap-core/*.h include/snap/snap-core/*.cpp include/snap/glib-core/*.h include/snap/glib-core/*.cpp include/snap/snap-adv/*.h include/snap/snap-adv/*.cpp
	 g++ -c include/snap/snap-core/Snap.cpp -I$(CSNAP) -I$(CGLIB) -I${EXSNAPADV} $(CXXFLAGS) $(LDFLAGS) $(LIBS) -shared -o out/Snap-adv.so 	 
	 
# static linking	 
bigclam-static: examples/bigclam.cpp # Snap-adv.o 
	g++ -o out/bigclam examples/bigclam.cpp $(EXSNAPADV)/agm.cpp $(EXSNAPADV)/agmfit.cpp $(EXSNAPADV)/agmfast.cpp out/Snap-adv.o \
	-I$(CSNAP) -I$(CGLIB) -I${EXSNAPADV} $(LDFLAGS) $(LIBS)	$(CXXOPENMP) 


# dynamic linking
bigclam-dynamic: examples/bigclam.cpp 
	g++ -o out/bigclam-dynamic examples/bigclam.cpp $(EXSNAPADV)/agm.cpp $(EXSNAPADV)/agmfit.cpp $(EXSNAPADV)/agmfast.cpp out/Snap-adv.so \
	-I$(CSNAP) -I$(CGLIB) -I${EXSNAPADV} $(LDFLAGS) $(LIBS)	$(CXXOPENMP) 
	
	
	
	