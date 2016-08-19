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
	// verbose only used to debug
	private boolean verbose = false;
	private int maxIts = 5; // Max iterations for debug output

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
			pageTable[i] = new Entry(blkSize);
			pageTable[i].blockSize = blkSize;
			// Clock first points to first element
			clockPtr = 0;
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
    	// Disk block number of cached data
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
    	
    	// Byte array storing cache data
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
    	public Entry(int blockSize)
    	{
    		this(-1, false, false, blockSize);
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
    }
    
    
    // ----------------- END OF ENTRY CLASS -----------------
    

    /*
     * Cache fields 
     */
    private Entry[] pageTable = null;
    
    // clock hand used in 2nd chance alg. 
    private int clockPtr;
    
    
    

    // ------------------------ findFreePage() ------------------
    
    /*
     *  Looks for the next free page. If all pages
     *  are being used, method that calls 
     *  findFreePage() will look for a victim
     */
    private int findFreePage() {
    	// Starting and ending indexes
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
    			//incrClockPtr();
    			return index;
    		}
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
    		if(verbose)
    		{
    			SysLib.cerr("Case ref = 0, dirty = 0 FOUND on first scan. ");
    		}
    		return victim;
    	}
    	
    	if(verbose)
		{
			SysLib.cerr("Case ref = 0, dirty = 0 NOT found after FIRST SCAN \n");
			
		}
    	
    	// Looking again in case all 
    	// reference bits were cleared
    	// in the previous search
    	victim = findCase(false, false);
    	if(victim > -1)
    	{
    		return victim;
    	}
    	
    	if(verbose)
		{
			SysLib.cerr("Case ref = 0, dirty = 0 NOT found after second scan \n");
			
		}
    	
    	if(verbose)
    	{
    		SysLib.cerr("About to sync() \n");
    		try									/// FOR DEBUGGING
			{
				Thread.sleep(2000);
			}
			catch(InterruptedException ex)
			{
				Thread.currentThread().interrupt();
			}									// DEBUG END
    	}
    	// At this point, need to sync dirty entries
    	this.sync();
    	// looking for victim after sync
    	victim = findCase(false, false);
    	if(victim > -1)
    	{
    		return victim;
    	}
    	
    	if(verbose)
		{
    		SysLib.cerr("NO VICTIM FOUND AFTER SYNC() \n");
    		try									/// FOR DEBUGGING
			{
				Thread.sleep(2000);
			}
			catch(InterruptedException ex)
			{
				Thread.currentThread().interrupt();
			}									// DEBUG END
			SysLib.cerr("Case ref = 0, dirty = 0 NOT found after sync \n");
			
		}
    	// At this point, need to 
    	// look for case:
    	// ( ref = 1, dirty = 1 )
    	victim = findCase(false, false);
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
     *  (refBit, dirtyBit). This method will 
     *  change the reference bit.
     */
    private int findCase(boolean ref, boolean dirty)
    {
    	int start = getClockPtr();			// Begin iterations here
    	int end = start + getTableSize();	// End iterations here
    	int currentindex = 0;				// Current entry to check
    	boolean currentRef = false;			// Current refBit of current entry
    	boolean currentDirty = false;		// Current dirtyBit of current entry
    	
    	// Iterating over pageTable[currentIndex]
    	// Note: this should also CLEAR the refBit, 
    	// i.e., it should be set to false or 0.
    	if(verbose)
    	{
    		boolean startR = pageTable[start].getRefBit();
    		boolean startD = pageTable[start].getDirtyBit();
    		SysLib.cerr("From findCase(0, 0) start = getClockPtr() == " + start + "(rev, dirty)= (" + startR + ", " + startD + ")\n");
    	}
    	for(int i = start; i < end; i++)
    	{
    		currentindex = i % getTableSize();
    		// Retrieving reference and dirty bitss
    		currentRef = pageTable[currentindex].getRefBit();
    		currentDirty = pageTable[currentindex].getDirtyBit();
    		if( (currentRef == ref) && (currentDirty == dirty) )
    		{
    			// Case found!!!!!
    			incrClockPtr();
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
    
    // Returns current location where
    // clock points. 
    private int getClockPtr()
    {
    	return clockPtr;
    }
    
    // Moving the clock pointer
    // ahead by one. 
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
    	if(pageTable[victimEntry].getDirtyBit() == false)
    	{
    		throw new IllegalArgumentException("Error in writeBack(victimEnt): Entry is not dirty.");
    	}
    	
    	// Getting physical  block frame number
    	int physFrame = pageTable[victimEntry].getBFN(); 
    	// Writing byte data in victimEntry to physFrame
    	SysLib.rawwrite(physFrame, pageTable[victimEntry].cacheBlock);
    	pageTable[victimEntry].clearDirtyBit();
    	
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
    	
    	int tableLength = this.getTableSize();
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
    			
    			// verbose block for debugging
    			if(verbose)
    			{
    				SysLib.cerr("From Cache.read(blockId == " + blockId + ", buffer) MATCH FOUND  after copyarray \n");
    				for(int k = 0; k < maxIts; k++)
    				{
    					SysLib.cerr("k == " + k + ", pageTable[" + currentIndex + "].cacheBlock[k] == " 
    							+ pageTable[currentIndex].cacheBlock[k] + ", buffer[k] == " + buffer[k]  +" \n");
    				}	
    			} // End of verbose block 
    			
    			// Since we just used this entry, we 
    			// need to set its referenceBit
    			this.pageTable[currentIndex].setRefBit();
    			
    			return true;
    		}
    		//this.incrClockPtr();
    	}
    	
    	// At this point, blockId not found in
    	// cache entry. Need to find a slot in 
    	// the cache to place disk block and then 
    	// write to it. 
    	int cachePageToFill = this.findFreePage();
    	boolean needVictim = false;
    	if(cachePageToFill == -1)
    	{
    		// Finding victim
    		cachePageToFill = this.nextVictim();
    		needVictim = true;
    	}
    	
    	if(cachePageToFill == -1)
    	{
    		SysLib.cerr("Error in Cache.read(int blkId, byte buffer[]). No victim found. \n");
    		return false;
    	}
    	
    	// Need to write to disk if the entry has the 
    	// dirty bit set. 
    	if(needVictim && pageTable[cachePageToFill].getDirtyBit() == true)
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
    	// rawread returns 0 upon success!!!!!!!!!!!
    	if(bytesRead == 0)
    	{
    		readSuccess = true;
    		// Placing data into cache
    		System.arraycopy(buffer, 0, pageTable[cachePageToFill].cacheBlock, 0, buffer.length);
    		
    		// verbose block for debugging!!
    		if(verbose)
			{
				SysLib.cerr("From Cache.read(blockId == " + blockId + ", buffer) VICTIM FOUND  after copyarray \n");
				for(int k = 0; k < maxIts; k++)
				{
					SysLib.cerr("k == " + k + ", pageTable[" + cachePageToFill + "].cacheBlock[k] == " 
							+ pageTable[cachePageToFill].cacheBlock[k] + ", buffer[k] == " + buffer[k]  +" \n");
				}
			} // End of debugging verbose block
    		
    		// Setting reference bit
    		pageTable[cachePageToFill].setRefBit();
    		pageTable[cachePageToFill].clearDirtyBit();
    	}
    	// Else no bytes read!!
    	else
    	{
    		if(verbose)
    		{
    			SysLib.cerr("Error in Cache.read(blockId, buffer): no bytes read. \n");
    			try
    			{
    				Thread.sleep(1000);
    			}
    			catch(InterruptedException ex)
    			{
    				Thread.currentThread().interrupt();
    			}
    			
    		}
    		readSuccess = false;
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
    			
    			// verbose block for debugging!!
    			if(verbose)
    			{
    				SysLib.cerr("Inside Cache.write() MATCH FOUND, blockId == " + blockId + " ... after arraycopy(buffer, cacheBlock) \n");
    				SysLib.cerr("Writing to pageTable[" + currentIndex + "] \n");
    				for(int k = 0; k < maxIts; k++)
    				{
    					SysLib.cerr("k == " + k + ", buffer[k] == " + buffer[k] 
    							+ ", cacheBlock[k] == " + pageTable[currentIndex].cacheBlock[k] + "\n");
    				}
    				
    	    		try									/// FOR DEBUGGING
    				{
    					Thread.sleep(2000);
    				}
    				catch(InterruptedException ex)
    				{
    					Thread.currentThread().interrupt();
    				}									// DEBUG END
    				
    				
    				

    			} // End of debugging verbose block
    			
    			// Updating bits
    			pageTable[currentIndex].setRefBit();
    			pageTable[currentIndex].setDirtyBit();
    			
    			return true;
    		}
    		
    	}

    	// If no match is found, find an empty 
    	// slot in cache to write to. 
    	
    	int emptySlot = this.findFreePage();
    	if(emptySlot > -1)
    	{
    		
    		// Empty slot found!
    		// Write to slot, set ref
    		pageTable[emptySlot].blockFrameNumber = blockId;
    		System.arraycopy(buffer, 0, pageTable[emptySlot].cacheBlock, 0, buffer.length);
    		
    		// verbose block for debugging
    		if(verbose)
			{
				SysLib.cerr("Inside Cache.write() EMPTY SLOT, blockId == " + blockId + " after arraycopy(buffer, cacheBlock) \n");
				SysLib.cerr("Writing to pageTable[" + emptySlot + "] \n");
				for(int k = 0; k < maxIts; k++)
				{
					SysLib.cerr("k == " + k + ", buffer[k] == " + buffer[k] 
							+ ", cacheBlock[k] == " + pageTable[emptySlot].cacheBlock[k] + "\n");
				}
				try									/// FOR DEBUGGING
				{
					Thread.sleep(2000);
				}
				catch(InterruptedException ex)
				{
					Thread.currentThread().interrupt();
				}									// DEBUG END
			} // End of debugging verbose block 

    		// Updating bits
    		pageTable[emptySlot].setRefBit();
    		pageTable[emptySlot].setDirtyBit();
    		return true;
    	}
    	
    	// At this point, there's no empty slots in 
    	// cache, need to find victim
    	int victim = this.nextVictim();
    	if(victim == -1)
    	{
    		SysLib.cerr("Error in Cache.write(blockId, buffer). No victim found. \n");
    		try
			{
				Thread.sleep(1000);
			}
			catch(InterruptedException ex)
			{
				Thread.currentThread().interrupt();
			}
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
    	pageTable[victim].blockFrameNumber = blockId;
    	System.arraycopy(buffer, 0, pageTable[victim].cacheBlock, 0, buffer.length);
    	
    	// verbose block for debugging
    	if(verbose)
		{
			SysLib.cerr("Inside Cache.write() VICTIM FOUND, blockId == " + blockId + " after arraycopy(buffer, cacheBlock) \n");
			SysLib.cerr("Writing to pageTable[" + victim + "] \n");
			for(int k = 0; k < maxIts; k++)
			{
				SysLib.cerr("k == " + k + ", buffer[k] == " + buffer[k] 
						+ ", cacheBlock[k] == " + pageTable[victim].cacheBlock[k] + "\n");
			}
			try									/// FOR DEBUGGING
			{
				Thread.sleep(2000);
			}
			catch(InterruptedException ex)
			{
				Thread.currentThread().interrupt();
			}									// DEBUG END
		} // End of debugging verbose block
    	
    	// Setting reference bit
    	pageTable[victim].setRefBit();
    	// This could have different data than 
    	// what is stored on the disk, so setting
    	// dirty bit
		pageTable[victim].setDirtyBit();

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
    		SysLib.cerr("Error in flush(). page table contains null element. \n");
    	}
	
    }
    
    // Returns size of page table
    public int getTableSize()
    {
    	return pageTable.length;
    }
}
