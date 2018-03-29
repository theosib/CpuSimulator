/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilitytypes;

import java.util.List;

/**
 * Basic interface implemented by every CPU core.
 * 
 * @author millerti
 */
public interface ICpuCore {
    public void advanceClock();
    public void reset();
    public void resetGlobals();
    IGlobals getGlobalResources();
    
    /**
     * Get pipeline stage by name.
     * @param name
     * @return pipeline stage
     */
    public IPipeStage getPipeStage(String name);

    /**
     * Get pipeline register by name.
     * @param name
     * @return pipeline register
     */
    public IPipeReg getPipeReg(String name);

    /**
     * Add a new pipeline stage to the set of pipeline stages.  The order
     * in which addPipeStage is called does not affect the order in which
     * they connect or are evaluated.  
     * 
     * The new pipeline stage must have been assigned a name.
     * 
     * @param stage New pipeline stage with name.
     */
    public void addPipeStage(IPipeStage stage);
    
    /**
     * Add a new pipeline register to the set of pipeline registers.  The order
     * in which addPipeStage is called does not affect the order in which
     * they connect or are clocked.  
     * 
     * The new pipeline register must have been assigned a name.
     * 
     * @param reg New pipeline register with name.
     */
    public void addPipeReg(IPipeReg reg);
    
    /**
     * Add an existing pipeline register as an output from an existing pipeline
     * stage.  Pipeline stages can have multiple outputs, but a pipeline
     * register may have a single source.
     * 
     * @param source_stage Pipe stage getting new output.
     * @param target_reg Pipe register being assigned source.
     */
    public void connect(IPipeStage source_stage, IPipeReg target_reg);
    
    /**
     * Add an existing pipeline register as an input to an existing pipeline
     * stage.  Pipeline stages can have multiple inputs, but a pipeline 
     * register may only have a single sink.
     * 
     * @param source_reg Pipe register being assigned sink.
     * @param target_stage Pipe stage getting new input.
     */
    public void connect(IPipeReg source_reg, IPipeStage target_stage);
    
    /**
     * Connect two pipeline elements by name.  One argument must be a
     * pipeline register, and the other must be a pipeline stage.
     * 
     * @param source
     * @param target
     */
    public void connect(String source, String target);

    /**
     * Traverse the graph of pipeline elements and compute an optimal order
     * in which pipeline stages are to be evaluated.
     * 
     * @param first_stage Starting point; typically the Fetch stage.
     */
    public void stageTopologicalSort(IPipeStage first_stage);    
    
    /**
     * Retrieve the results of the topological sort.
     * 
     * @return Ordered list of pipeline stages
     */
    public List<IPipeStage> getStageComputeOrder();

    /**
     * Look up a pipeline register by name and return the destination 
     * architectural or physical register of the instruction in the slave
     * latch, if any.  Returns -1 if no destinaton register.
     * 
     * @param pipe_reg_name Name of pipeline register
     * @return Destination arch/phys register number
     */
    public default int getResultRegister(String pipe_reg_name) { return -1; }

    /**
     * Look up a pipeline register by name and return the result value
     * that will be written to the destination register, if any.  If there is
     * no target arch/phys register, the return value of getResultValue is
     * undefined.
     * 
     * @param pipe_reg_name Name of pipeline register
     * @return Result value of computation
     */
    public default int getResultValue(String pipe_reg_name) { return 0; }

    /**
     * Look up a pipeline register by name and determine if there is or will 
     * be a valid result computed for the given arch/phys register number.
     * This is used to determine if the given pipeline register is a valid
     * forwarding source.
     * 
     * Return values:
     * EnumForwardingStatus.NULL -- No match to the given arch/phys register.
     * EnumForwardingStatus.VALID_NOW -- There is an arch/phys register number
     *     match to the instruction in the slave latch, and there is also a 
     *     valid result that can be retrieved immediately.
     * EnumForwardingStatus.VALID_NEXT_CYCLE -- There is an arch/phys register
     *     number match to the instruction in the master latch, and based on
     *     absence of stall condition, it can be determined that this 
     *     pipeline register's slave latch will contain a matching register
     *     and valid result on the next clock cycle.
     * 
     * @param pipe_reg_name Name of pipeline register
     * @param regnum Physical or architectural register being sought for
     *               forwarding.
     * @return EnumForwardingStatus match indicator
     */
    public default IPipeReg.EnumForwardingStatus matchForwardingRegister(String pipe_reg_name, int regnum) {
        return IPipeReg.EnumForwardingStatus.NULL;
    }

}
