# Minijava Compiler
## Introduction
This minijava compiler was completed as the main project for a University course. The project aims to compile a subset of Java into the MIPS assembly language. The original project was completed in four separate stages.

1. Type checking
2. Java -> Vapor IR
3. Vapor IR -> Vapor-M IR
4. Vapor-M IR -> MIPS

The project in this repository still follows this general structure, with a Gradle subproject for each step, but has been modified from the initially prescribed format.

## Building and Running
The entire project can be built and packaged correctly by using  
``` gradlew uberJar ```  
from the base directory.  
  
The compiler can then be used with a supplied run script
``` ./minijava [example-file.java] ```  
This would produce a new file named 'example-file.s'

## Running generated assembly
A jar file is included under the tools directory to run the generated MIPs assembly.  
``` java -jar tools/mars.jar nc [example-file.s] ```

## Other information

### Type System 
 Restrictions in minijava:
 * println statement can only print integers
 * there is no overloading
 * .length expressions only apply to int[] expressions

### Test files
Test files are provided in the test directory, giving examples in java, both IR's, and generated MIPs. 
