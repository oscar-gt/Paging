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
		}
	}

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
    	
    	// Size of cache block
    	private int blockSize;
    	// Number of free bytes remaining
    	private int spaceRemaining;
    	
    	// --------------------------------------------------
    	// --------------- Entry Constructors ---------------
    	// --------------------------------------------------
    	
    	/*
    	 *  Default constructor w/o parameters
    	 */
    	public Entry()
    	{
    		this(-1, false, false, 0, 0);
    	}
    	
    	/*
    	 *  Single parameter constructor
    	 */
    	public Entry(int blockFrameNum)
    	{
    		this(blockFrameNum, false, false, 0, 0);
    	}
    	
    	/*
    	 *  Constructor with parameters for all fields, 
    	 *  blockFrameNum, referenceBit, and dirtyBit
    	 */
    	public Entry(int blockFrameNum, boolean refBit, boolean dirtyB, int size, int space)
    	{
    		this.blockFrameNumber = blockFrameNum;
    		this.referenceBit = refBit;
    		this.dirtyBit = dirtyB;
    		this.blockSize = size;
    		this.spaceRemaining = space;
    	}
    }

    /*
     * Cache field is an array of entries
     */
    private Entry[] pageTable = null;

    /*
     * findFreePage()
     */
    private int findFreePage() {
        return -1;
    }

    private int nextVictim() {
        return -1;
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
}
