/*
 *  CSS 430
 *  Lab 4, Paging
 *  Oscar Garcia-Telles
 *  Modified 11 August 2016
 *  
 *  We will implement an enhanced second chance caching
 *  algorithm. 
 * 
 */

import java.util.*;

public class Cache {
	
	private boolean verbose = true;

	/*
	 * Constructor with blockSize and cacheBlocks
	 * parameters
	 */
	public Cache(int blkSize, int cacheBlocks) 
	{
		// First checking if parameters are valid
		if(blkSize < 1 || cacheBlocks < 1)
		{
			throw new IllegalArgumentException("Error in Cache(int blockSize, int cacheBlocks): " 
					+ " parameters must be > 0");
		}

		// Initializing pageTable[pages]
		pageTable = new Entry[cacheBlocks];
		for(int i = 0; i < cacheBlocks; i++)
		{
			// Default constructor will create 
			// entries with
			// blockFrameNumber = -1
			// referenceBit = false
			// dirtyBit = false
			pageTable[i] = new Entry();
			pageTable[i].blockSize = blkSize;
			// Clock first points to first element
			clockPtr = -1;
		}
	}

	// ------------------- PRIVATE ENTRY CLASS -------------------
    /*
     * Private class for an entry
     */
    private class Entry 
    {
    	/*
    	 *  Private fields
    	 */
    	// *Disk* block number of cached data
    	private int blockFrameNumber;
    	
    	// Ref bit set to 1 whenever this block is accessed.
    	// Reset to 0 in the 2nd chance alg when looking for 
    	// a victim. 
    	private boolean referenceBit; 
    	
    	// Dirty bit set to 1 whenever this block has been modified
    	// and the changed data has not been written back to the disk.
    	// Reset to 0 when the block is written back to the disk. 
    	private boolean dirtyBit;
    	
    	// Size of cache block in bytes
    	private int blockSize;
    	// Number of free bytes remaining
    	private int spaceRemaining;
    	
    	private byte cacheBlock[];
    	
    	// --------------------------------------------------
    	// --------------- Entry Constructors ---------------
    	// --------------------------------------------------
    	
    	/*
    	 *  Default constructor w/o parameters
    	 */
    	public Entry()
    	{
    		// blkFrameNum, refBit, dirtyBit, blkSize, 
    		this(-1, false, false, 0);
    	}
    	
    	/*
    	 *  Single parameter constructor
    	 */
    	public Entry(int blockFrameNum)
    	{
    		this(blockFrameNum, false, false, 0);
    	}
    	
    	/*
    	 *  Constructor with parameters for all fields, 
    	 *  blockFrameNum, referenceBit, and dirtyBit, blkSize
    	 */
    	public Entry(int blockFrameNum, boolean refBit, boolean dirtyB, int size)
    	{
    		this.blockFrameNumber = blockFrameNum;
    		this.referenceBit = refBit;
    		this.dirtyBit = dirtyB;
    		this.blockSize = size;
    		this.spaceRemaining = size;
    		// Array initialized with 0s by default.
    		this.cacheBlock = new byte[size];
    	}
    	
    	// -------- Setting and clearing reference and dirty bits --------
    	
    	public void setRefBit()
    	{
    		this.referenceBit = true;
    	}
    	
    	public void clearRefBit()
    	{
    		this.referenceBit = false;
    	}
    	
    	public void setDirtyBit()
    	{
    		this.dirtyBit = true;
    	}
    	
    	public void clearDirtyBit()
    	{
    		this.dirtyBit = false;
    	}
    	
    	// ------------------ Getter Methods --------------------
    	/*
    	 *  Returns block frame number
    	 */
    	public int getBFN()
    	{
    		return blockFrameNumber;
    	}
    	
    	/*
    	 *  Returns block size
    	 */
    	public int getBlockSize()
    	{
    		return blockSize;
    	}
    	
    	public boolean getRefBit()
    	{
    		return referenceBit;
    	}
    	
    	public boolean getDirtyBit()
    	{
    		return dirtyBit;
    	}
    	
    	public int getSpaceRemaining()
    	{
    		return spaceRemaining;
    	}
    }
    
    
    // ----------------- END OF ENTRY CLASS -----------------
    

    /*
     * Cache field is an array of entries
     */
    private Entry[] pageTable = null;
    
    private int clockPtr;
    
    
    

    // ------------------------ findFreePage() ------------------
    
    /*
     *  Looks for the next free page. If all pages
     *  are being used, will remove a page using
     *  the enhanced 2nd chance alg.
     */
    private int findFreePage() {
    	// First looking for an empty page
    	incrClockPtr(); // Moving ahead to check entry
    	int startHere = getClockPtr();
    	int endHere = startHere + getTableSize();
    	
    	// Will circularly cycle through array
    	for(int i = startHere; i < endHere; i++)
    	{
    		int index = i % getTableSize();
    		if(pageTable[index].getBFN() == -1)
    		{
    			// We've found block that's not being used!
    			return index;
    		}
    		// Incrementing clock pointer
    		incrClockPtr();
    	}
    	// At this point, all pages being used, 
    	// need to run enhanced 2nd chance alg. 
    	return nextVictim();
    }
    
    // ---------------------- nextVictom() ----------------------
    
    /*
     *  Next victim found by using 
     *  enhanced 2nd chance algorithm.
     */
    private int nextVictim() {
    	int victim = -1;
    	// First look for case: 
    	// ( ref = 0, dirty = 0 )
    	victim = findCase(0, 0);
    	if(victim > -1)
    	{
    		return victim;
    	}
    	
    	if(verbose)
		{
			SysLib.cerr("Case ref = 0, dirty = 0 NOT found \n");
			
		}
    	
    	// At this point, need to 
    	// look for case:
    	// ( ref = 0, dirty = 1 )
    	victim = findCase(0, 1);
    	if(victim > -1)
    	{
    		return victim;
    	}
    	
    	if(verbose)
		{
			SysLib.cerr("Case ref = 0, dirty = 1 NOT found \n");
			
		}
    	// At this point, need to 
    	// look for case:
    	// ( ref = 1, dirty = 0 )
    	victim = findCase(1, 0);
    	if(victim > -1)
    	{
    		return victim;
    	}
    	
    	if(verbose)
		{
			SysLib.cerr("Case ref = 1, dirty = 0 NOT found \n");
			
		}
    	// At this point, need to 
    	// look for case:
    	// ( ref = 1, dirty = 1 )
    	victim = findCase(1, 1);
    	if(victim > -1)
    	{
    		return victim;
    	}
    	
    	// Code should NOT reach this point!!!!!!!!!!!
    	SysLib.cerr("No cases of valid (rev, dirty) found (NOT POSSIBLE UNDER NORMAL CIRCUMSTANCES) \n");
    	
    	
        return victim;
    }
    
    // ----------------------- findCase(ref, dirty) ------------------
    
    /*
     *  Helps nextVictim() by looking at combinations of 
     *  (refBit, dirtyBit) and returning the best
     *   combination to replace. This method will 
     *   change the reference bit 
     */
    private int findCase(int ref, int dirty)
    {
    	int resultIndex = -1;
    	// Fill in !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    	// ...
    	
    	return resultIndex;
    }
    
    private int getClockPtr()
    {
    	return clockPtr;
    }
    
    private void incrClockPtr()
    {
    	clockPtr = (clockPtr + 1) % getTableSize();
    }

    private void writeBack(int victimEntry) {
    	
    }

    public synchronized boolean read(int blockId, byte buffer[]) {
        return false;
    }

    public synchronized boolean write(int blockId, byte buffer[]) {
        return false;
    }

    public synchronized void sync() {
    }

    public synchronized void flush() {
    }
    
    // Returns size of page table
    public int getTableSize()
    {
    	return pageTable.length;
    }
}
