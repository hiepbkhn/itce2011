
#TEST_NAME = map-test
#TEST_NAME = stack-queue-test

TEST_NAME = ex_lambda

#
BOOST_INCLUDE = E:\caidat\Cpp\boost_1_59_0\

# Specify compiler
CC=cl.exe

# Specify linker
LINK=link.exe

.PHONY : all
all : app

# Link the object files into a binary
app : $(TEST_NAME).o
	$(LINK) /OUT:app.exe $(TEST_NAME).o 

# Compile the source files into object files
#$(TEST_NAME).o : $(TEST_NAME).cpp
#	$(CC) /c $(TEST_NAME).cpp /Fo$(TEST_NAME).o

# Boost ! (NO lib)
$(TEST_NAME).o : $(TEST_NAME).cpp
	$(CC) /c $(TEST_NAME).cpp /Fo$(TEST_NAME).o	-I"$(BOOST_INCLUDE)"

# Clean target
clean :
	del $(TEST_NAME).o app.exe
