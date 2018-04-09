


# Definitions

*"clock period" or "clock cycle"*  
Time during which pipeline stages compute outputs from inputs.

*"master latch"*  
Output from a pipeline stage, input to a pipeline register.

*"slave latch"*  
Input to a pipeline stage, output to a pipeline register.

*"clock edge" or "clock boundary"*  
An event when data coming into all pipeline registers (master latch) is 
transferred to their outputs (slave latch).  This is when the whole CPU
advances in time from one clock period to the next.



# Class hierarchy

Below is documentation of the most important methods for each class type.
To see documentation on more methods provided by these classes, look at the
interface classes in the utilitytypes directory.


## Component
`class ComponentBase implements IComponent`

Any object in the system that can have a name and a parent Module.  Most other classes
in this system subclass ComponentBase.


## Pipeline Register
`class PipelineRegister extends ComponentBase implements IPipeReg`

Pipeline registers pass data between pipeline stages when advancing from one clock period to the next. 
Output from a preceding pipeline stage is input to a pipeline register's master Latch. 
Output from a pipeline register's slave latch is input to the succeeding pipeline stage.

The pipeline register's master latch is filled with data from the preceding pipeline stage.  When
advancing to the next clock period (by using the 1advanceClock()` method), the pipeline register transfers
master latch contents to the slave latch.  This makes the data available for reading by the 
succeeding pipeline stage.

### Important methods:

**`Latch read()`**  
For a pipeline register that is input to a pipeline stage read the contents of the slave Latch.  Note that
this does not dequeue, invalidate, or consume the slave latch.  This method allows you to access the 
input data and evaluate whether or not it can be processed at this time.

**`Latch newLatch()`**  
Allocate an empty Latch object to be filled with the output from a pipeline stage.

**`boolean canAcceptWork()`**  
Returns true when the pipeline register will be able to transfer data written to the
master match to its slave latch on the next clock boundary.  Returns false when a 
stall condition in the next stage is blocking such a transfer.


## Latch
`class Latch extends PropertiesContainer`

A data container used to communicate an instruction and other information between 
pipeline stages on clock boundaries.  Latches contain a
pre-defined field to hold an Instruction and extend PropertiesContainer so that
instructions and other arbitrary data can be passed between pipeline stages on 
clock boundaries.

A pipeline register slave latch is retrieved as input to a pipeline stage
by calling the `read()` method on an input pipeline register.  

### Important methods:

**`boolean canAcceptWork()`**  
For an output Latch produced by calling `PipelineRegister.newLatch()`, this method
returns true when the pipeline register will be able to transfer data written to the
master match to its slave latch on the next clock boundary.  This yields the
same result as calling canAcceptWork on the pipeline register that this Latch
was allocated from.

**`void write()`**  
For a pipeline register that takes output from a pipeline stage, write the contents 
of this newly allocated and filled Latch object to the master latch of the pipeline register.

**`void consume()`**  
For an input Latch provided by calling `PipelineRegister.read()`, this method informs
the pipeline register that its slave latch contents have been processed and can be
freed (dequeued, invalidated).

*Important:*  Not calling this method implicitly causes a pipeline stall condition.
It is important to explicitly consume input in order that new input can be received
on the next cycle.

This method is equivalent to calling `consumeSlave()` on the pipeline register that
produced this latch.

**`InstructionBase getInstruction()`**  
Returns the instruction field in this Latch.  

**`void setInstruction(InstructionBase ins)`**  
Sets the instruction field in this Latch.

**`boolean hasResultValue()`**  
Returns true if this Latch contains a result value.

**`boolean isResultFloat()`**  
Returns true if the result came from a floating point computation.

**`int getResultRegNum()`**  
If this latch contains a result value, return the architectural or physical
register number.  Otherwise, return -1.

**`int getResultValue()`**  
Returns result value in integer form.

**`float getResultValueAsFloat()`**  
Returns result value reinterpreted as a float.

**`void setResultValue(int value) `**  
Sets an integer result value in the Latch.

**`void setResultFloatValue(int value)`**  
Sets a float result value (stored in an int) in the Latch.

**`void setResultFloatValue(float value)`**  
Sets a float result value in the latch.  The bits of the float value are
internally stored in an int.

**`void setResultValue(int value, boolean isfloat)`**  
Set a result value (encoded in an int), optionally marking it as an encoded
float.

**`boolean isNull()`**  
Return true if the Latch itself is null or the instruction field in it is null.

**`void copyAllPropertiesFrom(Latch source)`**  
Copies all properties (excluding the instruction) from another latch.  Useful
for copying all properties from an input latch to an output latch.

**`void copyParentPropertiesFrom(Latch source)`**  
The pipeline register that this Latch belongs to (its parent) can be given a
list of property names (using `PipelineRegister.setPropertiesList(Set<String> pl)`).
This method copies only those properties from the specified source to this
Latch.

**`Latch duplicate()`**  
Makes a deep copy of of this Latch.  This is useful if you want to modify the
contents of the Latch without affecting the original.  
Note that posted forwarding requires that the original input Latch be modified.

## Pipeline Stage
`abstract class PipelineStageBase extends ComponentBase implements IPipeStage`

`PipelineStageBase` is an abstract class that must be subclassed by every pipeline stage in your 
processor architecture.

### Important methods:

**`Latch readInput(int input_num)`**  
Pipeline stages may have multiple inputs.  This reads the slave latch of the
specified input pipeline register.

**`List<IPipeReg> getInputRegisters()`**  
Return the list of all input pipeline registers.

**`int numInputRegisters()`**  
Returns the number of input pipeline registers.

**`int lookupInput(String name)`**  
Look up the index of an input register by its name.

**`Latch newOutput(int out_num)`**  
Pipeline stages may have multiple outputs.  This allocates a new latch for
the specified output pipeline register.

**`boolean outputCanAcceptWork(int out_num)`**  
Returns true if the specified output pipeline register can accept new work.

**`List<IPipeReg> getOutputRegisters()`**  
Return the list of all output pipeline registers.

**`int numOutputRegisters()`**  
Returns the number of output pipeline registers.

**`void registerFileLookup(Latch input)`**  
Tries to look up all source register operands from the register file.  Only registers
marked valid are retrieved.  This is used by a pipeline stage authorized to access the 
register file, like Decode.  The argument must be a duplicate of the original
input latch to the pipeline stage.

**`void forwardingSearch(Latch input)`**  
At initialization time, a list of forwarding source is provided to
the processor core.  This method searches the pipeline registers named 
in that list for forwarding opportunities. 

Register values that are already valid are retrieved immediately and 
`setValue` is called on matching operands.

Register values that will be available on the next cycle are posted
for forwarding to the next stage by setting `forward0`, `forward1`,
and/or `forward2` properties on the latch.  These properties can be
copied to the output latch so that the next stage can satisfy its
dependencies on the next cycle by calling `doPostedFowarding`.

**Important:** Call this method on a duplicate of the original input latch. 

**`void doPostedForwarding(Latch input)`**  
A pipeline stage that receives input with properties named
`forward0`, `forward1`, and/or `forward2` can call this method to
retrieve operand values from the pipeline registers whose names
are values of those properties.

**Important: Do not** call this on a duplicate of the input latch.
The original latch must be updated so that forwarded values are
retained in the in the input latch even under a stall condition.

**`void setResourceWait(String reason_stalled)`**  
Indicate that this pipeline stage is stalled waiting on an external resource
(e.g. register to become valid), providing a string that specifies what 
the stage is waiting on.  To rescind a stall condition, call this method with
null as the argument.

**`void setActivity(String act)`**  
Set the string that specifies the primary activity of this pipeline stage,
i.e. the instruction it is executing.  Usually, this is automatic, but 
sometimes it has to be set explicitly.

**`void addStatusWord(String word)`**  
Add a string to the list of status keywords, indicating the state of this
pipeline stage.


## Module
`abstract class ModuleBase extends ComponentBase implements IModule`

A Module is a type of Component that is able to contain pipeline stages,
pipeline registers, child functional units, and properties.  ModuleBase is
abstract and must be subclassed for each module in your design; however,
most modules you create will be subclasses of FunctionalUnitBase, which itself
is a subclass of ModuleBase.

### Important methods:

**`IProperties getProperties()`**  
Return a reference to the PropertiesContainer that belongs to this module.

**`IGlobals getGlobals()`**  
Return a reference to the global PropertiesContainer that is contained in the
top-level (CpuCore) module.

**`void createPipelineRegisters()`**  
This method is automatically called by the constructor and is where you add
code that instantiates all pipeline registers in this module (typically by
calling `createPipeReg(String name)`.  

**`void createPipelineStages()`**  
This method is automatically called by the constructor and is where you add
code that instantiates all pipeline stages in this module.  Create new instances
of each of your pipeline stages and use `addPipeStage()` to add them.

**`void createChildModules()`**  
This method is automatically called by the constructor and is where you add
code that instantiates all child `FunctionalUnit` objects.  Create new instances of
each child unit and use `addChildUnit()` to add them.

**`void createConnections()`**  
This method is automatically called by the constructor and is where you add
code that connects all pipeline registers, pipeline stages, and child
functional units.  There is a set of `connect()` methods provided for 
making connections.

**`void specifyForwardingSources()`**  
This method is automatically called by the constructor and is where you add
code that indicates which pipeline registers are forwarding sources.  Call the
`addForwardingSource()` for each forwarding source.

**`void addPipeStage(IPipeStage stage)`**  
Call this to add to this Module a newly created pipeline stage object (which must 
be a subclass of PipelineStageBase or implement the IPipeStage interface).

**`void addPipeReg(IPipeReg reg)`**  
Call this to add to this Module a newly created pipeline register object;
however, most pipeline registers will be created by calling `createPipeReg` 
instead.

**`void addChildUnit(IFunctionalUnit unit)`**  
Call this to add to this Module a newly created child functional unit object
(which must be a subclass of FunctionalUnitBase or implement the IFunctionalUnit 
interface).  

**`void connect(String source_name, String target_name)`**  
Specified by names, connect a pipeline stage to a succeeding pipeline register
or a pipeline register to its succeeding pipeline stage.

**`void connect(String stage1, String reg, String stage2)`**  
Specified by names, connect two pipeline stages through a pipeline register.

**`void createPipeReg(String name)`**  
Create a new pipeline register with no default property names and add it to 
this module.

**`void createPipeReg(String name, String[] props)`**  
Create a new pipeline register with a specified array of property names and
add it to this module.  This list of property names is used when 
the `copyParentPropertiesFrom` method is called on new output latches created
by this pipeline register.

**`void addForwardingSource(String name)`**  
Call this with the name of a pipeline register to specify that register as a
forwarding source.

**`void addStageAlias(String real_name, String alias_name)`**  
Create an alias for a specified pipeline stage.  This is particularly useful
when making a pipeline stage appear as an input stage higher up in the module
hierarchy.  

For instance, consider functional unit "A" that has a child functional unit "B" with a pipeline stage
"Compute".  In `A.createConnections`, call `addStageAlias("B.Compute", "in:Compute")`.
This will make "A.B.Compute" appear as a stage in "A", and using the "in:" prefix
(or just the name "in") will
also make the stage automatically findable as an input to functional unit A.

**`void addRegAlias(String real_name, String alias_name)`**  
Create an alias for a specified pipeline register.  This is particularly useful
when making a pipeline register appear as an output register higher up in the module
hierarchy.  

For instance, consider functional unit "Example" that has a child functional unit "Delay" with a 
pipeline register "out".  In 'Example.createConnections`, call `addRegAlias("Delay.out", "out")`.
This will make "Example.Delay.out" appear as a register in "Example," and using the name "out"
(or the prefix "out:") will also make the register automatically findable as an output
from functional unit Example.

## FunctionalUnit
`abstract class FunctionalUnitBase extends ModuleBase implements IFunctionalUnit`

A functional unit is a type of Module that adds inputs and outputs to the the Module
so that the the module can be added as a child unit to another module and connected
into the processor pipeline.

FunctionalUnitBase is abstract, and all of the methods you must implement are 
explained above under Module.

### Important methods:

**`IPipeStage getInputPipeStage(String name)`**  
Given a name, look up an individual functional unit input pipeline stage.  Only stages
with the name "in" or that start with the prefix "in:" are considered valid 
functional unit inputs.

**`Map<String,IPipeStage> getInputPipeStages()`**  
Returns all input pipeline stages for this functional unit.  The returned map will
contain only local pipeline stages and aliases whose names are "in" or start with
"in:".  

**`IPipeReg getOutputPipeReg(String name)`**  
Given a name, look up an individual functional unit output pipeline register.  Only registers
with the name "out" or that start with the prefix "out:" are considered valid 
functional unit outputs.

**`Map<String,IPipeReg> getOutputPipeRegs()`**  
Returns all output pipeline registers for this functional unit.  The returned map will
contain only local pipeline registers and aliases whose names are "out" or start with
"out:".  

## CpuCore
`abstract class CpuCore extends ModuleBase implements ICpuCore`

A CPU core is a type of Module that represents the top level of the CPU 
module hierarchy.  CpuCore is abstract, and most of the methods you must implement are 
explained above under Module.

### Important methods:

**`IPipeStage getFirstStage()`**  
This is called automatically by the constructor and passed to the code that automatically
sorts pipeline stages into optimal evaluation order and print order.

**`void printHierarchy()`**  
Prints out the module hierarchy, specifically which registers are inputs and outputs
for each pipeline stage.  Full hierarchical names are printed (along with aliases), which
makes this a good reference when trying to figure out the full name of a component.

## PropertiesContainer

Genetic container of properties of various data types, indexed by String
name.  This is the base class for global data and for pipeline latches.  This
is the base class for Latch and GlobalData.

### Important methods:

**`Set<String> propertyNames()`**  
Returns the names of all properties.

**`Map<String,Object> getProperties()`**  
Returns the internal HashMap from property names to property values.

**`boolean hasProperty(String name)`**  
Returns true if the specified property name exists in the container.

**`void setProperty(String name, Object val)`**  
Set a property (by name) to a specified value.

**`void setClockedProperty(String name, Object val)`**  
Set a property (by name) to a specified value.  The setting is queued and does not take effect
until the next clock period.

**`void deleteProperty(String name)`**  
Remove a property from the container.

**`void deleteClockedProperty(String name)`**  
Remove a property from the container.  The deletion is queued and does not take effect
until the next clock period.

**`Integer getPropertyInteger(String name)`**  
Returns the value of an Integer property.  If the property is not an Integer, an exception is thrown.

**`Integer getPropertyIntArray(String name)`**  
Returns the value of an int array (`int[]`) property.  If the property is not an int array, an exception is thrown.

**`Integer getPropertyBoolean(String name)`**  
Returns the value of a Boolean property.  If the property is not a Boolean, an exception is thrown.

**`Integer getPropertyBooleanArray(String name)`**  
Returns the value of an int array (`boolean[]`) property.  If the property is not a boolean array, an exception is thrown.

**`Integer getPropertyString(String name)`**  
Returns the value of a String property.  If the property is not a String, an exception is thrown.

**`Object getPropertyObject(Object name)`**  
Returns the Object value of a property.  This is useful for storing properties of types not explicitly supported.

**`void copyPropertiesFrom(IProperties source, Set<String> propertiesToCopy)`**  
Copies properties from the specified source container to this PropertiesContainer.  The `propertiesToCopy` argument specifies the 
names of properties to copy.

**`void copyAllPropertiesFrom(IProperties source)`**  
Copy ALL properties from the specified source container.

## Globals
`interface IGlobals extends IProperties`
`class GlobalData extends PropertiesContainer implements IGlobals`

The Globals class is a type of PropertiesContainer tha adds program store and register file members.

### Important methods:

**`void loadProgram(InstructionSequence seq)`**  
Load a parsed assembly language program into the container.

**`InstructionBase getInstructionAt(int pc_address)`**  
Retrieve an instruction at the specified address from the program store.

**`IRegFile getRegisterFile()`**  
Returns a reference to the register file.

**`void setup()`**  
Called automatically by the constructor, you must implement this method to 
pre-set properties that must already exist and set up the RegisterFile.

**`void advanceClock()`**  
Must call `super.advanceClock()` and `regfile.advanceClock()` in order to 
apply queued global property changes and register file updates.

## RegisterFile
`class RegisterFile implements IRegFile`

Contains a specifiable number of architectural or physical registers, along
with their states (validity, result of float computation).  

32-bit integers and single-precision floating point values occupy the same number of bytes in
memory, so the raw bits encoding floating point values are reinterpreted as
integers for storage in the register file.  This reinterpretation of memory
bits is not the same as type casting, which attempts to convert numbers between 
types so that they have the same meaning.  For example, the floating point 
number 1.0 when stored in a register is not converted to the integer value 1.  It is
stored as the integer 0x7F800000, which is the underlying binary representation
of 1.0 in single-precision floating point.

### Important methods:

**`boolean isInvalid(int index)`**  
**`boolean isValid(int index)`**  
**`boolean isFloat(int index)`**  
Check if a specified arch/phys register is invalid, valid, or the result of
floating point computation.

**`int getValue(int index)`**  
Returns the contents of the specified register in integer form (even if the
bits stored there are a float).  Throws an exception if the register is not
valid.

**`float getValueAsFloat(int index)`**  
Returns the contents of the specified register in float form.  Throws an exception
if the register is not valid or it does not contain a float value.

**`void setIntValue(int index, int value)`**  
Updates a register with a new integer value (resulting from integer computation).
The update is queued so that it does not take effect until the next clock cycle.
The register will also be marked as valid and not float.

**`void setFloatValue(int index, int value)`**  
Updates a register with a new float value (resulting from float computation),
although the float bits encoded in an integer.
The update is queued so that it does not take effect until the next clock cycle.
The register will also be marked as valid and float.

**`void setFloatValue(int index, float value)`**  
Updates a register with a new float value (resulting from float computation).  The 
raw float bits are reinterpreted in integer form.
The update is queued so that it does not take effect until the next clock cycle.
The register will also be marked as valid and float.

**`void setValue(int index, int value, boolean is_float)`**  
Updates a register with a new value.  Whether or not the value (encoded as an int)
is a reinterpreted float is specified by the `is_float` argument.
The update is queued so that it does not take effect until the next clock cycle.
The register will also be marked as valid and float.

