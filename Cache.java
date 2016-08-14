/*
 *  CSS 430
 *  Lab 4, Paging
 *  Oscar Garcia-Telles
 *  Modified 11 August 2016
 *  
 *  We will implement an enhanced second chance caching
 *  algorithm. 
 *  
 *  NOTE:	In general, I prefer readability over 
 *  		compactness, so there may be more
 * 			lines of code than necessary. 			
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
    	
    	// Invalidating entry
    	public void invalidate()
    	{
    		this.blockFrameNumber = -1;
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
    		// Retrieving and checking
    		// block frame number. 
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
    	return -1;
    }
    
    // ---------------------- nextVictom() ----------------------
    
    /*
     *  Next victim found by using 
     *  enhanced 2nd chance algorithm.
     *  Cases are looked for in decreasing
     *  replacement priority
     */
    private int nextVictim() {
    	int victim = -1;
    	// First look for case of highest priority
    	// to replace: 
    	// ( ref = 0, dirty = 0 )
    	victim = findCase(false, false);
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
    	victim = findCase(false, true);
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
    	victim = findCase(true, false);
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
    	victim = findCase(true, true);
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
     *  combination to replace. This method will 
     *  change the reference bit.
     */
    private int findCase(boolean ref, boolean dirty)
    {
    	// First incrementing clock ptr to check at next entry
    	incrClockPtr();
    	int start = getClockPtr();			// Begin iterations here
    	int end = start + getTableSize();	// End iterations here
    	int currentindex = 0;				// Current entry to check
    	boolean currentRef = false;			// Current refBit of current entry
    	boolean currentDirty = false;		// Current dirtyBit of current entry
    	
    	// Iterating over pageTable[currentIndex]
    	// Note: this should also CLEAR the refBit, 
    	// i.e., it should be set to false or 0.
    	for(int i = start; i < end; i++)
    	{
    		currentindex = i % getTableSize();
    		// Retrieving reference and dirty bitss
    		currentRef = pageTable[currentindex].getRefBit();
    		currentDirty = pageTable[currentindex].getDirtyBit();
    		if( (currentRef == ref) && (currentDirty == dirty) )
    		{
    			// Case found!!!!!
    			return currentindex;
    		}
    		// If case not found in current iteration, 
    		// clear reference bit (it may or may not 
    		// be cleared already...). 
    		pageTable[currentindex].clearRefBit();
    		// Incrementing clock pointer
    		incrClockPtr();
    		
    	}
    	
    	//  At this point, no cases matching
    	//  (ref, dirty) were found...
    	return -1;
    }	// End of findCase( ref, dirty )
    
    
    private int getClockPtr()
    {
    	return clockPtr;
    }
    
    private void incrClockPtr()
    {
    	clockPtr = (clockPtr + 1) % getTableSize();
    }

    // ------------------------- writeBack( victimEntry ) -------------------------
    /*
     *  Victim to be replaced is written back to the 
     *  disk so that the cache page can be written 
     *  with new data. 
     */
    private void writeBack(int victimEntry) {
    	// First validating argument
    	if(victimEntry < 0 || victimEntry >= getTableSize() )
    	{
    		throw new IllegalArgumentException("Error in writeBack(victimEnt): argument must be a valid array index.");
    	}
    	
    	// Getting physical  block frame number
    	int physFrame = pageTable[victimEntry].getBFN(); 
    	// Writing byte data in victimEntry to physFrame
    	SysLib.rawwrite(physFrame, pageTable[victimEntry].cacheBlock);
    	
    }

    // --------------------- read( blockId, buffer[] ) -------------------------
    /*
     *  Takes data from cache block associated with blockId
     *  and copies it to buffer[]. If blockId is not found
     *  in cache, reads from disk. Must then place this data
     *  in the cache, so it will look for an open spot or
     *  a victim to replace. 
     */
    public synchronized boolean read(int blockId, byte buffer[]) {
    	// First incrementing clock pointer
    	int tableLength = this.getTableSize();
    	this.incrClockPtr();
    	int start = getClockPtr();			// Begin iterations here
    	int end = start + tableLength;		// End iterations here
    	int currentIndex = 0;				// Current entry to check
    	int currBlkId = -1;
    	for(int i = start; i < end; i++)
    	{
    		currentIndex = i % tableLength;
    		// Retrieving current block frame number in entry
    		currBlkId = pageTable[currentIndex].getBFN();
    		if(currBlkId == blockId)
    		{
    			// Block found!!!
    			// Copying data in cache block into buffer[]
    			System.arraycopy(pageTable[currentIndex].cacheBlock, 0, buffer, 0, buffer.length);
    			
    			// Since we just used this entry, we 
    			// need to set its referenceBit
    			this.pageTable[currentIndex].setRefBit();
    			
    			return true;
    		}
    		this.incrClockPtr();
    	}
    	
    	// At this point, blockId not found in
    	// cache entry. Need to find a slot in 
    	// the cache to place disk block and then 
    	// write to it. 
    	int cachePageToFill = this.findFreePage();
    	if(cachePageToFill == -1)
    	{
    		cachePageToFill = this.nextVictim();
    	}
    	
    	if(cachePageToFill == -1)
    	{
    		SysLib.cerr("Error in Cache.read(int blkId, byte buffer[]). No victim found.");
    		return false;
    	}
    	
    	// Need to write to disk if the entry has the 
    	// dirty bit set. 
    	if(pageTable[cachePageToFill].getDirtyBit() == true)
    	{
    		// Write entry that will be replaced back 
    		// to the disk 
    		int blkFrNum = pageTable[cachePageToFill].getBFN();
    		SysLib.rawwrite(blkFrNum, pageTable[cachePageToFill].cacheBlock);
    		// Clearing dirty bit. 
    		pageTable[cachePageToFill].clearDirtyBit();
    	}
    	
    	// Reading from disk into cache slot
    	boolean readSuccess = false;
    	int bytesRead = SysLib.rawread(blockId, buffer);
    	if(bytesRead > 0)
    	{
    		readSuccess = true;
    		// Placing data into cache
    		System.arraycopy(buffer, 0, pageTable[cachePageToFill], 0, buffer.length);
    		// Setting reference bit
    		pageTable[cachePageToFill].setRefBit();
    	}
    	else
    	{
    		if(verbose)
    		{
    			SysLib.cerr("Error in Cache.read(blockId, buffer): no bytes read.");
    			readSuccess = false;
    		}
    	}
    	return readSuccess;
    }

    // --------------------------- write() ---------------------------
    /*
     *  Writes the buffer[] contents into the cache. First look for 
     *  a cache entry that's associated with blockFrameNumber == blockId, 
     *  then write to it. If no cache page has an entry with blockId, 
     *  find an empty slot in cache or find a victim.
     *  NOTE:	NO WRITE-THROUGH. I.E., DON'T NEED TO WRITE TO 
     *  		DISK, ONLY CACHE.
     */
    public synchronized boolean write(int blockId, byte buffer[]) {
    	// Validating arguments
    	if(blockId < 0)
    	{
    		throw new IllegalArgumentException("Error in" 
    			+ "Cache.write(int blockid, buffer[]). blockId must be >= 0.");
    	}
    	
    	if(buffer.length != pageTable[0].getBlockSize())
    	{
    		throw new IllegalArgumentException("Error in" 
        			+ "Cache.write(int blockid, buffer[]). buffer length invalid");
    	}

    	// First need to find cache entry with a 
    	// blockFrameNumber == blockId
    	int tableLength = this.getTableSize();
    	this.incrClockPtr();
    	int start = getClockPtr();			// Begin iterations here
    	int end = start + tableLength;		// End iterations here
    	int currentIndex = 0;				// Current entry to check
    	int currBlkId = -1;
    	for(int i = start; i < end; i++)
    	{
    		currentIndex = i % tableLength;
    		// Retrieving current block frame number in entry
    		currBlkId = pageTable[currentIndex].getBFN();
    		if(currBlkId == blockId)
    		{
    			// Match found!
    	    	// Write to cache entry, set ref and dirty bits.
    			System.arraycopy(buffer, 0, pageTable[currentIndex].cacheBlock, 0, buffer.length);
    			pageTable[currentIndex].setRefBit();
    			pageTable[currentIndex].setDirtyBit();
    			
    			return true;
    		}
    		this.incrClockPtr();
    	}

    	// If no match is found, find an empty 
    	// slot in cache to write to. 
    	int emptySlot = this.findFreePage();
    	if(emptySlot > -1)
    	{
    		// Empty slot found!
    		// Write to slot, set ref
    		System.arraycopy(buffer, 0, pageTable[emptySlot].cacheBlock, 0, buffer.length);
    		pageTable[emptySlot].setRefBit();
    		return true;
    	}
    	
    	// At this point, there's no empty slots in 
    	// cache, need to find victim
    	int victim = this.nextVictim();
    	if(victim == -1)
    	{
    		SysLib.cerr("Error in Cache.write(blockId, buffer). No victim found.");
    		return false;
    	}
    	
    	// Victim found!
    	// Need to check victim's dirty bit and 
    	// write to disk if needed. 
    	if(pageTable[victim].getDirtyBit() == true)
    	{
    		int victimBlockFrameNum = pageTable[victim].getBFN();
    		SysLib.rawwrite(victimBlockFrameNum, pageTable[victim].cacheBlock);
    	}
    	
    	// Finally writing to the cache!
    	System.arraycopy(buffer, 0, pageTable[victim].cacheBlock, 0, buffer.length);
    	// Setting reference bit
    	pageTable[currentIndex].setRefBit();
    	// Clearing dirty bit since this is a
    	// different block frame number entry
		pageTable[currentIndex].clearDirtyBit();

    	// End of method
        return true;
    }

    // ------------------- sync() -----------------------
    
    /*
     *  sync() writes back all dirby blocks 
     *  to Disk. Keeps clean cache entries
     *  in cache (dirtyBit = false)
     */
    public synchronized void sync() 
    {
    	int currentBFN = -1; // Current block frame number
    	for(int i = 0; i < this.getTableSize(); i++)
    	{
    		// Checking each entry for a set dirty bit
    		if(this.pageTable[i].getDirtyBit() == true)
    		{
    			currentBFN = this.pageTable[i].getBFN();
    			SysLib.rawwrite(currentBFN, this.pageTable[i].cacheBlock);
    			this.pageTable[i].clearDirtyBit();
    		}
    	}
    }

    // ------------------- flush() -------------------------
    
    /*
     *  Invalidates all cache entries. 
     */
    public synchronized void flush() 
    {
    	try
    	{
    		for(int i = 0; i < this.getTableSize(); i++)
    		{
    			pageTable[i].invalidate();
    		}
    	}
    	catch (NullPointerException e)
    	{
    		SysLib.cerr("Error in flush(). page table contains null element.");
    	}
	
    }
    
    // Returns size of page table
    public int getTableSize()
    {
    	return pageTable.length;
    }
}
