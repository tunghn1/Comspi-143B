// Path and Buffer class
import java.nio.file.*;

// Options in BufferReader class
import static java.nio.file.StandardOpenOption.*;

// Charset
import java.nio.charset.Charset;

// Buffer
import java.io.*;

public class VM2 {
			
			// TLB 4 by 3 array
			static int [][] TLB = new int [4][3];
			
			// PM 1 by 1 array
			static int [] PM = new int [1024 * 521];
			
			// BM 1 by 1 array 
			static int [] BM = new int [32];
			
			// Mask1 
			static int [] Mask1 = new int [32];
			
			// Mask 2
			static int [] Mask2 = new int [32];
			
			// Path to initialization file
			static Path readInit = Paths.get("C:\\Users\\TungNguyen230893\\Desktop\\UCI\\Fall 2017\\Compsi 143B Operating system project\\Project 2\\Init3.txt");
			
			// Path to VA file
			static Path readVA = Paths.get("C:\\Users\\TungNguyen230893\\Desktop\\UCI\\Fall 2017\\Compsi 143B Operating system project\\Project 2\\VA3.txt");
			
			// Path to TLB file
			
			static Path writeTLB = Paths.get("C:\\Users\\TungNguyen230893\\Desktop\\UCI\\Fall 2017\\Compsi 143B Operating system project\\Project 2\\TLB.txt");
			
			// Path to ATP file
			
			static Path writeATP = Paths.get("C:\\Users\\TungNguyen230893\\Desktop\\UCI\\Fall 2017\\Compsi 143B Operating system project\\Project 2\\ATP.txt");
			
			
			static {
				
				// Initialize TLB
				for (int i = 0; i < 4; i++) {
					for (int j = 0; j < 3; j++) {
						
							TLB[i][j] = -1;
					}
				}
				
				// Initialize LRU  
				for (int i = 0; i < 4; i++) {
					
					TLB[i][0] = i;
					
				}
				
				// Initialize PM
				for (int i = 0; i < PM.length; i++) {
					PM[i] = 0;
				}
				
				// Initialize BM
				for (int i = 0; i < BM.length; i++) {
					BM[i] = 0;
				}
				
				// Initialize Mask1
				Mask1[Mask1.length - 1] = 1;
				for (int i = Mask1.length - 2; i >= 0; i--) {
					Mask1[i] = Mask1[i + 1] << 1; 
				}
				
				// Initialize Mask2
				for (int i = 0; i < Mask2.length; i++) {
					Mask2[i] = ~Mask1[i];
				}
				
				
			}
			
			
			// Mask3
			
			static int Mask (int lower, int upper) {
				
				int index = lower;
				int mask = 0;
				
				while (index <= upper) {
				
					int temp = 1;
					int shifter = 31 - index;
					temp = temp << shifter;
					mask |= temp;
					index++;
				}
				
				return mask;
			}
			
			// Set BM
			static void set (int frame) {

				int indexBM = frame / 32;
				int indexMask = frame % 32; 
				BM[indexBM] = BM[indexBM] | Mask1[indexMask]; 
				System.out.println(" free frame " + frame);
			}
			
			// Search BM

			static int search () {

				for (int i = 0; i < 32;  i++) {
					for (int j = 0; j < 32; j++) {

						int test = BM[i] & Mask1[j];
					
						if (test == 0) 
							return i * 32 + j;
					}
				
				}

			return 0;
			} 
			
			// TLBWrite
			static void TLBWrite (int f, int w) throws IOException {
				
				BufferedWriter writeBuff = Files.newBufferedWriter (writeTLB, WRITE, APPEND, CREATE);	

				try {
				
				int PA = f + w;
				writeBuff.write(" h " + PA + " ");

				}

				finally {

					if (writeBuff != null) { writeBuff.close();}
				}

			}
			
			// ATP's read
			
			static boolean ATPRead(int s, int  p, int w, Path writeObject) throws IOException {
				
				BufferedWriter writeBuff = Files.newBufferedWriter (writeObject, WRITE, APPEND, CREATE);
				boolean exist = false;

				try {
					if (PM[s] == -1 || PM[PM[s] + p] == -1) {
						
						if (writeObject.compareTo(writeTLB) == 0) {
							
							writeBuff.write( " m " + " pf ");
							
						}
						
						if (writeObject.compareTo(writeATP) == 0) {
							
							writeBuff.write(" pf ");
							
						}
					}

					else if (PM[s] == 0 || PM[PM[s] + p] == 0) {

						if (writeObject.compareTo(writeTLB) == 0) {
							
							writeBuff.write( " m " + " err ");
							
						}
						
						if (writeObject.compareTo(writeATP) == 0) {
							
							writeBuff.write(" err ");
							
						}
					
					}

					else {
						
						int  PA = PM[PM[s] + p] + w;
						
						if (writeObject.compareTo(writeTLB) == 0) {
						
							writeBuff.write(" m " + PA + " ");
						}
						
						if (writeObject.compareTo(writeATP) == 0) {
							
							writeBuff.write(" " + PA + " ");
						}
						exist = true;

					} 
				}

				finally {

					if (writeBuff != null ) { writeBuff.close();}
					return exist;
				}

		} 
			
			
			// ATP's write
			
			static boolean ATPWrite(int s, int p, int w, Path writeObject) throws IOException {
				
				// Create BufferedWrite object and write to the output file
				BufferedWriter writeBuff = Files.newBufferedWriter (writeObject, WRITE, APPEND, CREATE);	
				boolean exist = false;

				try {

					if ( PM[s] == -1 || PM[PM[s] + p] == -1) {
						
						if (writeObject.compareTo(writeTLB) == 0) {
							
							writeBuff.write( " m " + " pf ");
							
						}
						
						if (writeObject.compareTo(writeATP) == 0) {
							
							writeBuff.write(" pf ");
							
						}
						
					}
					
					else if (PM[s] == 0) {
						
						// Find and set frame
						int frame1 = search();
						set(frame1);
						int frame2 = search();
						set(frame2);
						
						// Set ST's entry to the address of frame1
						PM[s] = frame1 * 512;
					}
					
					else if (PM[PM[s] + p] == 0) {
							
							int frame3 = search();
							set(frame3);
							
							PM[PM[s] + p] = frame3 * 512;
							int  PA = PM[PM[s] + p] + w;
							
							if (writeObject.compareTo(writeTLB) == 0) {
								
								writeBuff.write(" m " + PA + " ");
								
							}
							
							if (writeObject.compareTo(writeATP) == 0) {
								
								writeBuff.write(" " + PA + " ");
								
							}
						
					}
					
					else {
						
						int  PA = PM[PM[s] + p] + w;
						
						if (writeObject.compareTo(writeTLB) == 0) {
							
							writeBuff.write(" m " + PA + " ");
						}
						
						if (writeObject.compareTo(writeATP) == 0) {
							
							writeBuff.write(" " + PA + " ");
						}
						
						exist = true;
					}

				}


				finally {

					if (writeBuff != null) { writeBuff.close();}
					return exist;
				}
		}
			
			// update LRU for TLB
			static void updateTLB (int sp) {

				for (int row = 0; row < 4; row ++) {
								
					if (TLB[row][1] == sp) {
						
						int row1 = 0;
						while (row1 < 4) {
							
							if (TLB[row1][1] > sp) {
								
								TLB[row][0] -= 1;
							}
							row1++;
						}
						TLB[row][0] = 3;
					}
												
				}

		}
			
			// Update LRU for ATP
			
			static void updateATP (int s, int p, int w, int sp) {

					for (int row = 0; row < 4; row ++ ) {
										
						if (TLB[row][0] > 0) {

							TLB[row][0] -= 1;
						}
											
						else {

							TLB[row][0] = 3;
							TLB[row][1] = sp;
							TLB[row][2] = PM[PM[s] + p]; 
												
						}
					}

			} 
			
			// TLB's driver
			
			static void TLB (int o, int VA) throws IOException {

				// Break VA into sp and w
				int sp = VA >> 9;
				int w = VA & Mask (23, 31);
				int hit = 0;
				int row1 = 0;

				// Search for a hit
				 for (int row = 0; row < 4; row++){

					// Search TLB for match on sp
					if (TLB[row][1] == sp){

						hit = 1;
						row1 = row;
					}

				}

				// Check for hit and miss
				if (hit == 1) {
					
					// update LRU
					updateTLB(sp);

					// Invoke TLBWrite
					int f = TLB[row1][2];
					TLBWrite (f, w);
				}

				else {
					
					// Break into s, p, and w
					int s = VA >> 19;
					int p = (VA & Mask (13, 22)) >> 9;
					
					if (o == 1) {

						// Invoke ATP's write
						boolean exist = ATPWrite (s, p, w, writeTLB);

						// Update LRU if applicable
						if (exist == true) {
						
							updateATP (s, p, w, sp);
						}
					}

					else {

						// Invoke ATP's read
						boolean exist = ATPRead (s, p, w, writeTLB);

						// Update LRU if applicable
						if (exist == true) {
						
							updateATP (s, p, w, sp);
						}
					}
				}
			}
			
			// ATP's driver
			static void ATPDriver (int o, int VA) throws IOException {
				
				// Break VA into s, p, and w
				int s = VA >> 19;
				int p = (VA & Mask (13, 22)) >> 9;
				int w = VA & Mask (23, 31);
				
				// Compare o with 1 and 0
				if (o == 1) {

					// Invoke ATP's write
					boolean exist = ATPWrite (s, p, w, writeATP);

				}

				else {

					// Invoke ATP's read
					boolean exist = ATPRead (s, p, w, writeATP);

				}
				
			}
			
			// readVAATP
			static void readVAATP () throws IOException {
				// Initialize read buffer
				BufferedReader readBuff = Files.newBufferedReader(readVA);
				int sizeVA = 0;
				int offset = 0;
				String line;
				String [] parse = null;
				
				try {
					
					// Count total element
					while ((line = readBuff.readLine()) != null) {
						
						parse = line.split(" ");
						sizeVA = parse.length;
					}
					
					// Create array
					int [] VA = new int [sizeVA];
					
					// Initialize array
					while (offset < VA.length) {
						
							VA[offset] = Integer.parseInt(parse[offset]);
							offset++;
						
						}

					offset = 0;
					
					// Translate VA to PA
					while (offset * 2 < VA.length){
						
						int o = offset * 2;
						int va = offset * 2 + 1;
						offset++;
						ATPDriver (VA[o], VA[va]); 
					}			 	
				}		

				finally {

					if (readBuff != null) { readBuff.close();}
				} 
			}
			
			// Read initialization file 
			static void readInitFile () throws IOException {
					
					// Initialize read buffer
					
					String line;
					int sizeST = 0;
					int sizePT = 0;
					int offset = 0;
					BufferedReader readBuff1= Files.newBufferedReader(readInit);
					BufferedReader readBuff2= Files.newBufferedReader(readInit);
					
					// Problem: Travel to the end of the file, but no return
					try {
						
						
						// Count elements
						while ((line = readBuff1.readLine()) != null){
							
							String parse[];
							parse = line.split(" ");
							
							if (sizeST == 0) 
								sizeST = parse.length;
							else 
								sizePT = parse.length;
						}
					
					}
					
					finally {if (readBuff1 != null) { readBuff1.close();}}
					
					
					try {
						
						
						// Create two arrays to store elements in the first and second line

						int [] ST = new int[sizeST];
						int [] PT = new int[sizePT];
						
						// Store elements to the appropriate array
						
						while ((line = readBuff2.readLine()) != null){
							
							String parse[];
							parse = line.split(" ");
							
							if (offset == 0) {
								
								while (offset < parse.length) {		
									ST[offset] = Integer.parseInt(parse[offset]);
									offset++;
								}	
							}

							else {
							
								offset = 0;
								while (offset < parse.length) {		
									PT[offset] = Integer.parseInt(parse[offset]);
									offset++;
								}
							}	
						}

						offset = 0;
						
						// Using the first array to initialize the PM

						set (0);
						while (offset * 2 < sizeST) {

							int offsetST = offset * 2;
							int pageTableAddress = offset * 2 + 1;
							int frame1 = ST[pageTableAddress] / 512;
							int frame2 = ST[pageTableAddress] / 512 + 1;

							PM [ST[offsetST]] = ST[pageTableAddress];
							
							if (ST[pageTableAddress] != 0 && ST[pageTableAddress] != -1) {
							set (frame1);
							set (frame2); 
							}

							++ offset;
							
						}

						// Using the second array to initialize the PM
						offset = 0;
						while (offset * 3 < sizePT) {

							int offsetPT = PT[offset * 3] + PM[PT[offset * 3 + 1]];
							int pageAddress = offset * 3 + 2;
							int frame = 0;
							
							if ( PT[pageAddress] != 0 && PT [pageAddress] != -1) {
							
								frame = PT[pageAddress] / 512;
								set(frame);
							
							}
							
							if (PM[PT[offset * 3 + 1]] != 0 && PM[PT[offset * 3 + 1]] != -1) {
								
							PM[offsetPT] = PT[pageAddress];
							
							}
							
							offset++;
							
						}
					}
					
					finally {
					
						// Close the file 
						if (readBuff2 != null) { readBuff2.close();}
						
					}
				}
			
			// Read VA file
			static void readVA () throws IOException {
				
				// Initialize read buffer
				BufferedReader readBuff = Files.newBufferedReader(readVA);
				int sizeVA = 0;
				int offset = 0;
				String line;
				String [] parse = null;
				
				try {
					
					// Count total element
					while ((line = readBuff.readLine()) != null) {
						
						parse = line.split(" ");
						sizeVA = parse.length;
					}
					
					// Create array
					int [] VA = new int [sizeVA];
					
					// Initialize array
					while (offset < VA.length) {
						
							VA[offset] = Integer.parseInt(parse[offset]);
							offset++;
						
						}

					offset = 0;
					
					// Translate VA to PA
					while (offset * 2 < VA.length){
						
						int o = offset * 2;
						int va = offset * 2 + 1;
						offset++;
						TLB (VA[o], VA[va]); 
					}			 	
				}		

				finally {

					if (readBuff != null) { readBuff.close();}
				} 
			}
			
			public static void main(String[] args) throws IOException{
				
				// Invoke readInitFile and readVaATP
				readInitFile();
				readVAATP();
				
				// Clear PM
				for (int i = 0; i < PM.length; i++) {
					PM[i] = 0;
				}
				
				// Clear BM
				for (int i = 0; i < BM.length; i++) {
					BM[i] = 0;
				}
				
				// Invoke readInitFIle and readVA
				readInitFile();
				readVA();

			}

}
