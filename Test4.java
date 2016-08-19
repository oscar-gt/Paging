import java.util.Date;
import java.util.Random;

class Test4 extends Thread {
	private boolean enabled;
	private int testcase;
	private long startTime;
	private long endTime;
	private byte[] wbytes;
	private byte[] rbytes;
	private Random rand;
	
	// Time keeping
	private int timeLength = 200;
	private long writeTimes[] = new long[timeLength];
	private long readTimes[] = new long[timeLength];
	private int readIndex = 0;
	private int writeIndex = 0;
	private long avgStartTime;
	private long avgEndTime;
	private boolean firstEnabled;
	private StringBuffer mode = new StringBuffer();
	

	private void getPerformance( String msg ) {
		if ( enabled == true )
			SysLib.cout( "Test " + msg + "(cache enabled): " 
					+ (endTime - startTime) + "\n" );
		else
			SysLib.cout( "Test " + msg + "(cache disabled): " 
					+ (endTime - startTime) + "\n" );
	}

	private void read( int blk, byte[] bytes ) {
		
		if ( enabled == true )
		{
			if(firstEnabled == false)
			{
				mode.append("[Cache Enabled]");
				firstEnabled = true;
			}
			
			avgStartTime = new Date( ).getTime( );
			SysLib.cread( blk, bytes );
			avgEndTime = new Date().getTime();
			readTimes[readIndex] = avgEndTime - avgStartTime;
		}	
		else
		{
			if(firstEnabled == false)
			{
				mode.append("[Cache Disabled]");
				firstEnabled = true;
			}
			
			avgStartTime = new Date( ).getTime( );
			SysLib.rawread( blk, bytes );
			avgEndTime = new Date( ).getTime( );
			readTimes[readIndex] = avgEndTime - avgStartTime;
		}
		readIndex = readIndex + 1;
			
	}

	private void write( int blk, byte[] bytes ) {
		
		if ( enabled == true )
		{
			if(firstEnabled == false)
			{
				mode.append("[Cache Enabled]");
				firstEnabled = true;
			}
			
			avgStartTime = new Date( ).getTime( );
			SysLib.cwrite( blk, bytes );
			avgEndTime = new Date( ).getTime( );
			writeTimes[writeIndex] = avgEndTime - avgStartTime;
		}
			
		else
		{
			if(firstEnabled == false)
			{
				mode.append("[Cache Disabled]");
				firstEnabled = true;
			}
			
			avgStartTime = new Date( ).getTime( );
			SysLib.rawwrite( blk, bytes );
			avgEndTime = new Date( ).getTime( );
			writeTimes[writeIndex] = avgEndTime - avgStartTime;
		}
		writeIndex = writeIndex + 1;
			
	}

	private void randomAccess( ) {
		firstEnabled = false;
		int bufferLength = mode.length();
		mode.delete(0, bufferLength);
		writeIndex = 0;
		readIndex = 0;
		mode.append("[Random Access]");
		int acc = 200;	// Blocks that will be accessed.
		
		int maxVal = 512; // Instead of all 512.
		int[] accesses = new int[acc];
		for ( int i = 0; i < acc; i++ ) {
			accesses[i] = Math.abs(rand.nextInt( ) % 512);
			// SysLib.cout( accesses[i] + " " );
		}
		// SysLib.cout( "\n" );
		for ( int i = 0; i < acc; i++ ) {
			for ( int j = 0; j < 512; j++ )
			{
				wbytes[j] = (byte)(j);
			}
			
			write( accesses[i], wbytes );
			
		}
		
		for ( int i = 0; i < acc; i++ ) {
			// Start read timer!!
			
			read( accesses[i], rbytes );
			// End read timer, store in array
			
		// DEBUG DEBUG
//			SysLib.cerr("i == " + i + ", will read from block " + accesses[i] + "\n");
//			try									/// FOR DEBUGGING
//			{
//				Thread.sleep(1000);
//			}
//			catch(InterruptedException ex)
//			{
//				Thread.currentThread().interrupt();
//			}									// DEBUG END
			// END OF DEBUG 
			
			for ( int k = 0; k < maxVal; k++ ) {
				if ( rbytes[k] != wbytes[k] ) {
					SysLib.cerr("rbytes[k] == " + rbytes[k] + ", wbytes[k] == " + wbytes[k] + " ");
					SysLib.cerr( "ERROR\n" );
					try									/// FOR DEBUGGING
					{
						Thread.sleep(2000);
					}
					catch(InterruptedException ex)
					{
						Thread.currentThread().interrupt();
					}									// DEBUG END
					SysLib.exit( );
				}
			}
		}
		// Displaying results
		displayAvgResults();

	}

	private void localizedAccess( ) {
		firstEnabled = false;
		int bufferLength = mode.length();
		mode.delete(0, bufferLength);
		writeIndex = 0;
		readIndex = 0;
		mode.append("[Localized Access]");
		
		for ( int i = 0; i < 20; i++ ) {
			for ( int j = 0; j < 512; j++ )
				wbytes[j] = (byte)(i + j);
			for ( int j = 0; j < 1000; j += 100 )
			{
				write( j, wbytes );
			}
		
				
			for ( int j = 0; j < 1000; j += 100 ) {
				read( j, rbytes );
				for ( int k = 0; k < 512; k++ ) {
					if ( rbytes[k] != wbytes[k] ) {
						SysLib.cerr( "ERROR\n" );
						SysLib.exit( );
					}
				}
			}
			// Display Time info here
		}
		// Displaying results
		displayAvgResults();
	}

	private void mixedAccess( ) {
		firstEnabled = false;
		int bufferLength = mode.length();
		mode.delete(0, bufferLength);
		writeIndex = 0;
		readIndex = 0;
		mode.append("[Mixed Access]");
		int[] accesses = new int[200];
		for ( int i = 0; i < 200; i++ ) {
			if ( Math.abs( rand.nextInt( ) % 10 ) > 8 ) {
				// random
				accesses[i] = Math.abs( rand.nextInt( ) % 512 );
			} else {
				// localized
				accesses[i] = Math.abs( rand.nextInt( ) % 10 );
			}
		}
		for ( int i = 0; i < 200; i++ ) {
			for ( int j = 0; j < 512; j++ )
				wbytes[j] = (byte)(j);
			write( accesses[i], wbytes );
		}
		for ( int i = 0; i < 200; i++ ) {
			read( accesses[i], rbytes );
			for ( int k = 0; k < 512; k++ ) {
				if ( rbytes[k] != wbytes[k] ) {
					SysLib.cerr( "ERROR\n" );
					SysLib.exit( );
				}
			}
		}
		// Displaying results
		displayAvgResults();
	}

	private void adversaryAccess( ) {
		firstEnabled = false;
		int bufferLength = mode.length();
		mode.delete(0, bufferLength);
		writeIndex = 0;
		readIndex = 0;;
		mode.append("[Adversary Access]");
		for ( int i = 0; i < 20; i++ ) {
			for ( int j = 0; j < 512; j++ )
				wbytes[j] = (byte)(j);
			for ( int j = 0; j < 10; j++ )
				write( i * 10 + j, wbytes );
		}
		for ( int i = 0; i < 20; i++ ) {
			for ( int j = 0; j < 10; j++ ) {
				read( i * 10 + j, rbytes );
				for ( int k = 0; k < 512; k++ ) {
					if ( rbytes[k] != wbytes[k] ) {
						SysLib.cerr( "ERROR\n" );
						SysLib.exit( );
					}
				}
			}
		}
		// Displaying results
		displayAvgResults();
	}
	
	private void displayAvgResults()
	{
		// Displaying Average times
		// Average times
		long readSum = (long) 0;
		long writeSum = (long) 0;
		long readAvg = (long) 0;
		long writeAvg = (long) 9;
		for(int i = 0; i < readTimes.length; i++)
		{
			readSum = readSum + readTimes[i];
			writeSum = writeSum + writeTimes[i];
		}
		readAvg = readSum/readTimes.length;
		writeAvg = writeSum/writeTimes.length;
		String modeStr = mode.toString();
		SysLib.cerr("*************************************************** \n");
		SysLib.cerr(modeStr + "Avg Read Time: " + readAvg + "\n");
		SysLib.cerr(modeStr + "Avg Write Time: " + readAvg + "\n");
		SysLib.cerr("*************************************************** \n");

	}


	public Test4( String[] args ) {
		enabled = args[0].equals( "enabled" ) ? true : false;
		testcase = Integer.parseInt( args[1] );
		wbytes = new byte[Disk.blockSize];
		rbytes = new byte[Disk.blockSize];
		rand = new Random( );
	}

	public void run( ) {
		SysLib.flush( );
		startTime = new Date( ).getTime( );
		switch ( testcase ) {
		case 1: 
			randomAccess( );
			endTime = new Date( ).getTime( );
			getPerformance( "random accesses" );
			break;
		case 2:
			localizedAccess( );
			endTime = new Date( ).getTime( );
			getPerformance( "localized accesses" );
			break;
		case 3:
			mixedAccess( );
			endTime = new Date( ).getTime( );
			getPerformance( "mixed accesses" );
			break;
		case 4:
			adversaryAccess( );
			endTime = new Date( ).getTime( );
			getPerformance( "adversary accesses" );
			break;
		case 5:
			randomAccess( );
			endTime = new Date( ).getTime( );
			getPerformance( "random accesses" );

			startTime = new Date( ).getTime( );
			localizedAccess( );
			endTime = new Date( ).getTime( );
			getPerformance( "localized accesses" );

			startTime = new Date( ).getTime( );
			mixedAccess( );
			endTime = new Date( ).getTime( );
			getPerformance( "mixed accesses" );

			startTime = new Date( ).getTime( );
			adversaryAccess( );
			endTime = new Date( ).getTime( );
			getPerformance( "adversary accesses" );
			break;
		}
		

		SysLib.exit( );
	}
}
