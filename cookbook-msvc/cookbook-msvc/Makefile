
#TEST_NAME = map-test
#TEST_NAME = stack-queue-test

#TEST_NAME = ex_lambda
TEST_NAME = ex_filesize

#
BOOST_INCLUDE = E:\caidat\Cpp\boost_1_63_0
BOOST_LIB = E:\caidat\Cpp\boost_1_63_0\lib64-msvc-12.0

# Specify compiler
#CC=cl.exe
# 64bit
CC="C:\Program Files (x86)\Microsoft Visual Studio 12.0\VC\bin\amd64\cl.exe"

# Specify linker
#LINK=link.exe
# 64bit
LINK="C:\Program Files (x86)\Microsoft Visual Studio 12.0\VC\bin\amd64\link.exe"

.PHONY : all
all : app

# Link the object files into a binary
#app : $(TEST_NAME).o
#	$(LINK) /OUT:app.exe $(TEST_NAME).o 

# Compile the source files into object files
#$(TEST_NAME).o : $(TEST_NAME).cpp
#	$(CC) /c $(TEST_NAME).cpp /Fo$(TEST_NAME).o
	
#### BOOST	
# Boost (WITH lib)
app : $(TEST_NAME).o
	$(LINK) /OUT:app.exe $(TEST_NAME).o /LIBPATH:"$(BOOST_LIB)" /DEFAULTLIB:libboost_filesystem-vc120-mt-s-1_63 

# Boost ! 
$(TEST_NAME).o : $(TEST_NAME).cpp
	$(CC) /c $(TEST_NAME).cpp /Fo$(TEST_NAME).o	-I"$(BOOST_INCLUDE)"



# Clean target
clean :
	del $(TEST_NAME).o app.exe
