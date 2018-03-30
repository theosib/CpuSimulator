/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilitytypes;

import java.util.List;
import java.util.Set;

/**
 * Basic interface implemented by every CPU core.
 * 
 * @author millerti
 */
public interface ICpuCore extends IModule {
    public void advanceClock();    

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
    public int getResultRegister(String pipe_reg_name);

    /**
     * Look up a pipeline register by name and return the result value
     * that will be written to the destination register, if any.  If there is
     * no target arch/phys register, the return value of getResultValue is
     * undefined.
     * 
     * @param pipe_reg_name Name of pipeline register
     * @return Result value of computation
     */
    public int getResultValue(String pipe_reg_name);

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
    public IPipeReg.EnumForwardingStatus matchForwardingRegister(String pipe_reg_name, int regnum);
    
    
    public Set<String> getForwardingSources();
    public Set<String> getForwardingTargets();
    
    void computeFlattenedPipeStageMap();
    void computeFlattenedPipeRegMap();
    IPipeStage getFirstStage();
    
    default void initModule() {
        IModule.super.initModule();
        computeFlattenedPipeStageMap();
        computeFlattenedPipeRegMap();
        stageTopologicalSort(getFirstStage());
    }
}
