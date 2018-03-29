/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilitytypes;

import baseclasses.Latch;
import baseclasses.PipelineRegister;
import java.util.List;

/**
 *
 * @author millerti
 */
public interface IPipeStage {
    /**
     * Clear the list of pipeline stage status keywords.  This is called
 by evaluate at the beginning of the clock cycle.
     */
    public void clearStatus();

    /**
     * For diagnostic purposes, pipeline stages have a facility for reporting
     * status information.  Some status information is inferred from stall
     * conditions.  However, pipeline stage can add arbitrary status 
     * information by calling this method.  The getStatus method then returns
     * a string that joins all of the status keywords in a comma-delimited
     * list.
     * @param word Status information to be added
     */
    public void addStatusWord(String word);

    /**
     * Clear the activity string.  This is called by evaluate at the start
 of a clock cycle.
     */
    public void clearActivity();
    
    /**
     * For diagnostic purposes, this is used to set the activity string, which
     * is typically the instruction being processed by the pipeline stage.
     * @param act String representation of what the stage is working on
     */
    public void setActivity(String act);

    /**
     * @return What this stage is currently working on (i.e. an instruction)
     */
    public String getActivity();

    /**
     * @return The current status string (stall conditions, etc.)
     */
    public String getStatus();
    
    /**
     * The topological sort algorithm uses this to specify the relative order
     * of this stage in relation to other stages before and after.
     * @param pos Position in partial ordering of pipeline stages
     */
    public void setTopoOrder(int pos);

    /**
     * @return Current topological order position.
     */
    public int getTopoOrder();
    
    /**
     * Returns true if the stage is stuck waiting on a resource.  
     * This defaults to false at the start of each clock cycle, and its
     * value can be changed by calling setResourceStall.
     * @return 
     */
    public boolean stageWaitingOnResource();

    /**
     * Set (or clear) the pipeline stage stall status.
     * @param stalled
     */
    public void setResourceStall(boolean stalled);

    /**
     * Returns true when any input to the pipeline stage contains work to do
     * (i.e. an instruction).
     * @return
     */
    public boolean stageHasWorkToDo();

    /**
     * The evaluate method implements the "combinational logic" of the 
     * pipeline stage.  Its job is to:
     * - Read input pipeline registers, using readInput(input_number).
     * - Perform calculations on those inputs.
     * - Determine when those calculations require external resources that are
     *   not available, using setResourceStall() to indicate when the pipeline
     *   stage is in a wait condition.
     * - Allocate output latches for filling with output data, by calling 
     *   newOutput(output_number)
     * - Find out if any needed output pipeline registers are stalled, by 
     *   calling outputCanAcceptWork(output_number) or 
     *   theOutputLatch.calAcceptWork().
     * - Indicate which inputs are being consumed in the process of computing
     *   outputs that are NOT stalled, by calling claimInput(input_number) or
     *   theInputLatch.claim();
     * - Write results to output pipeline registers by calling
     *   writeOutput(theOutputLatch, output_number) or theOutputLatch.write().
     * 
     * Other diagnostic oriented things this can do include:
     * - Specify what the stage is working on by calling setActivity().
     * - Provide status information by calling addStatusWord().
     * 
     * See javadocs on PipelineStageBase.compute methods, which are called
     * by evaluate in the default implementation.
     */
    public void evaluate();
    
    /**
     * Get the index of the named pipeline register in this stage's list of
     * input registers.
     * 
     * @param name
     * @return
     */
    public int lookupInput(String name);
    
    /**
     * Indicates that the specified input register's slave latch data has been
     * consumed in the process of doing work.  
     * 
     * Calling this method (or theInputLatch.claim()) is mandatory to indicate
     * which inputs are being used.
     * 
     * Pipeline registers default to assuming that the pipeline stage
     * is NOT consuming its inputs.  Therefore if you do not claim an input,
     * the pipeline register will not be aware that you have used it, and
     * this will cause the pipeline register to stall.
     * 
     * @param input_num Index of input being consumed.
     */
    public void claimInput(int input_num);
    
    /**
     * Read the slave latch of the specified input register.  This does not
     * automatically indicate that the input is being used.  If an input
     * is actually used, you must call claimInput(input_number) or
     * theInputLatch.claim().
     * 
     * @param input_num
     * @return
     */
    public Latch readInput(int input_num);

    /**
     * @return The whole list of input pipeline registers
     */
    public List<IPipeReg> getInputRegisters();

    /**
     * Get the index of the named pipeline register in this stage's list of
     * output registers.
     * 
     * @param name
     * @return
     */
    public int lookupOutput(String name);
    
    /**
     * Query if the specified output is able to accept new work (not stalled).
     * 
     * NOTE: This can be called AT ANY TIME during the evaluation of a
     * pipeline stage.  If a succeeding pipeline stage has not been evaluated 
     * (thus making stall information unknown), then that other stage will be 
     * evaluated on demand.
     * 
     * @param out_num
     * @return
     */
    public boolean outputCanAcceptWork(int out_num);

    /**
     * Ask the specified output pipeline register to allocate an empty
     * latch to be filled with data to be passed to the next pipeline stage.
     * 
     * The returned latch is merely a container that can be committed to the
     * pipeline register or thrown away if you don't need it.
     * 
     * In order to commit the latch (send it to the pipeline register), 
     * call writeOutput(theNewOutputLatch, output_number) or
     * call theNewOutputLatch.write();
     * 
     * Committing/writing the new latch to the pipeline register assigns it 
     * to the master latch of the pipeline register.
     * 
     * When advanceClock is called on the pipeline register, the master
     * latch is copied to the slave latch, atomically transferring its
     * contents to the next pipeline stage.  (As long as there are no stall
     * conditions preventing it.)
     * 
     * @param out_num
     * @return
     */
    public Latch newOutput(int out_num); 

    /**
     * Ask the specified output pipeline register to return a reference to
     * an invalid output latch, which represents a bubble.  This is optional,
     * because pipeline register master latches default to invalid.
     * 
     * @param out_num
     * @return
     */
    public Latch invalidOutput(int out_num);
    
    /**
     * Commit the specified latch to the specified output so that the latch
     * contents can be transferred to the next pipeline stage.
     * 
     * Alternatively, this can be performed by calling theOutputLatch.write().
     * 
     * @param out
     * @param out_num
     */
    public void writeOutput(Latch out, int out_num);

    /**
     * @return The whole list of output pipeline registers
     */
    public List<IPipeReg> getOutputRegisters();
    
    /**
     * Connect the specified pipeline register to this pipeline stage as an
     * input.  Any number of inputs may be added.  The order in which 
     * pipeline registers is added implicitly specifies the index of the
     * given register in the pipeline stage's list of inputs.  (The first to
     * be added is at index 0.)
     * 
     * @param input_reg
     */
    public void addInputRegister(IPipeReg input_reg);

    /**
     * Connect the specified pipeline register to this pipeline stage as an
     * output.  Any number of outputs may be added.  The order in which 
     * pipeline registers is added implicitly specifies the index of the
     * given register in the pipeline stage's list of outputs.  (The first to
     * be added is at index 0.)
     * 
     * @param output_reg
     */
    public void addOutputRegister(IPipeReg output_reg);
    
    /**
     * Restore pipeline stage to initial conditions
     */
    public void reset();

    /**
     * @return the name of this pipeline stage
     */
    public String getName();
}
