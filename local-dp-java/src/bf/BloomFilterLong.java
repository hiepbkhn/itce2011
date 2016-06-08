/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Jun 7, 2016
 * 	- copied from BloomFilter, use Long data type instead of generic
 * 	- add containsListBF()
 * 	- add removeBits() to realize the case alpha < 1
 * Jun 8
 * 	- add compressBF(), decompressBF()
 */

package bf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import nayuki.arithcode.ArithmeticCompress;
import nayuki.arithcode.ArithmeticDecompress;
import nayuki.arithcode.BitInputStream;
import nayuki.arithcode.BitOutputStream;
import nayuki.arithcode.FrequencyTable;
import nayuki.arithcode.SimpleFrequencyTable;

/**
 * Implementation of a Bloom-filter, as described here:
 * http://en.wikipedia.org/wiki/Bloom_filter
 *
 * For updates and bugfixes, see http://github.com/magnuss/java-bloomfilter
 *
 * Inspired by the SimpleBloomFilter-class written by Ian Clarke. This
 * implementation provides a more evenly distributed Hash-function by
 * using a proper digest instead of the Java RNG. Many of the changes
 * were proposed in comments in his blog:
 * http://blog.locut.us/2008/01/12/a-decent-stand-alone-java-bloom-filter-implementation/
 *
 * @param <E> Object type that is to be inserted into the Bloom filter, e.g. String or Integer.
 * @author Magnus Skjegstad <magnus@skjegstad.com>
 */
public class BloomFilterLong implements Serializable {
    private BitSet bitset;
    private int bitSetSize;
    private double bitsPerElement;
    private int expectedNumberOfFilterElements; // expected (maximum) number of elements to be added
    private int numberOfAddedElements; // number of elements actually added to the Bloom filter
    private int k; // number of hash functions

    static final Charset charset = Charset.forName("UTF-8"); // encoding used for storing hash values as strings

    static final String hashName = "MD5"; // MD5 gives good enough accuracy in most circumstances. Change to SHA1 if it's needed
    static final MessageDigest digestFunction;
    static { // The digest method is reused between instances
        MessageDigest tmp;
        try {
            tmp = java.security.MessageDigest.getInstance(hashName);
        } catch (NoSuchAlgorithmException e) {
            tmp = null;
        }
        digestFunction = tmp;
    }

    /**
      * Constructs an empty Bloom filter. The total length of the Bloom filter will be
      * c*n.
      *
      * @param c is the number of bits used per element.
      * @param n is the expected number of elements the filter will contain.
      * @param k is the number of hash functions used.
      */
    public BloomFilterLong(double c, int n, int k) {
      this.expectedNumberOfFilterElements = n;
      this.k = k;
      this.bitsPerElement = c;
      this.bitSetSize = (int)Math.ceil(c * n);
      numberOfAddedElements = 0;
      this.bitset = new BitSet(bitSetSize);
    }
    
    //// hiepnh - Jun 7, 2016
    public BloomFilterLong clone(){
    	BloomFilterLong ret = new BloomFilterLong(this.bitsPerElement, this.expectedNumberOfFilterElements, this.k);
    	
    	ret.bitSetSize = (int)Math.ceil(this.bitsPerElement * this.expectedNumberOfFilterElements);
        ret.numberOfAddedElements = this.numberOfAddedElements;
        ret.bitset = (BitSet)this.bitset.clone();
    	
    	//
    	return ret;
    }

    /**
     * Constructs an empty Bloom filter. The optimal number of hash functions (k) is estimated from the total size of the Bloom
     * and the number of expected elements.
     *
     * @param bitSetSize defines how many bits should be used in total for the filter.
     * @param expectedNumberOElements defines the maximum number of elements the filter is expected to contain.
     */
    public BloomFilterLong(int bitSetSize, int expectedNumberOElements) {
        this(bitSetSize / (double)expectedNumberOElements,
             expectedNumberOElements,
             (int) Math.round((bitSetSize / (double)expectedNumberOElements) * Math.log(2.0)));
    }

    /**
     * Constructs an empty Bloom filter with a given false positive probability. The number of bits per
     * element and the number of hash functions is estimated
     * to match the false positive probability.
     *
     * @param falsePositiveProbability is the desired false positive probability.
     * @param expectedNumberOfElements is the expected number of elements in the Bloom filter.
     */
    public BloomFilterLong(double falsePositiveProbability, int expectedNumberOfElements) {
        this(Math.ceil(-(Math.log(falsePositiveProbability) / Math.log(2))) / Math.log(2), // c = k / ln(2)
             expectedNumberOfElements,
             (int)Math.ceil(-(Math.log(falsePositiveProbability) / Math.log(2)))); // k = ceil(-log_2(false prob.))
    }

    /**
     * Construct a new Bloom filter based on existing Bloom filter data.
     *
     * @param bitSetSize defines how many bits should be used for the filter.
     * @param expectedNumberOfFilterElements defines the maximum number of elements the filter is expected to contain.
     * @param actualNumberOfFilterElements specifies how many elements have been inserted into the <code>filterData</code> BitSet.
     * @param filterData a BitSet representing an existing Bloom filter.
     */
    public BloomFilterLong(int bitSetSize, int expectedNumberOfFilterElements, int actualNumberOfFilterElements, BitSet filterData) {
        this(bitSetSize, expectedNumberOfFilterElements);
        this.bitset = filterData;
        this.numberOfAddedElements = actualNumberOfFilterElements;
    }

    /**
     * Generates a digest based on the contents of a String.
     *
     * @param val specifies the input data.
     * @param charset specifies the encoding of the input data.
     * @return digest as long.
     */
    public static int createHash(String val, Charset charset) {
        return createHash(val.getBytes(charset));
    }

    /**
     * Generates a digest based on the contents of a String.
     *
     * @param val specifies the input data. The encoding is expected to be UTF-8.
     * @return digest as long.
     */
    public static int createHash(String val) {
        return createHash(val, charset);
    }

    /**
     * Generates a digest based on the contents of an array of bytes.
     *
     * @param data specifies input data.
     * @return digest as long.
     */
    public static int createHash(byte[] data) {
        return createHashes(data, 1)[0];
    }

    /**
     * Generates digests based on the contents of an array of bytes and splits the result into 4-byte int's and store them in an array. The
     * digest function is called until the required number of int's are produced. For each call to digest a salt
     * is prepended to the data. The salt is increased by 1 for each call.
     *
     * @param data specifies input data.
     * @param hashes number of hashes/int's to produce.
     * @return array of int-sized hashes
     */
    public static int[] createHashes(byte[] data, int hashes) {
        int[] result = new int[hashes];

        int k = 0;
        byte salt = 0;
        while (k < hashes) {
            byte[] digest;
            synchronized (digestFunction) {
                digestFunction.update(salt);
                salt++;
                digest = digestFunction.digest(data);                
            }
        
            for (int i = 0; i < digest.length/4 && k < hashes; i++) {
                int h = 0;
                for (int j = (i*4); j < (i*4)+4; j++) {
                    h <<= 8;
                    h |= ((int) digest[j]) & 0xFF;
                }
                result[k] = h;
                k++;
            }
        }
        return result;
    }

    /**
     * Compares the contents of two instances to see if they are equal.
     *
     * @param obj is the object to compare to.
     * @return True if the contents of the objects are equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BloomFilterLong other = (BloomFilterLong) obj;        
        if (this.expectedNumberOfFilterElements != other.expectedNumberOfFilterElements) {
            return false;
        }
        if (this.k != other.k) {
            return false;
        }
        if (this.bitSetSize != other.bitSetSize) {
            return false;
        }
        if (this.bitset != other.bitset && (this.bitset == null || !this.bitset.equals(other.bitset))) {
            return false;
        }
        return true;
    }

    /**
     * Calculates a hash code for this class.
     * @return hash code representing the contents of an instance of this class.
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + (this.bitset != null ? this.bitset.hashCode() : 0);
        hash = 61 * hash + this.expectedNumberOfFilterElements;
        hash = 61 * hash + this.bitSetSize;
        hash = 61 * hash + this.k;
        return hash;
    }


    /**
     * Calculates the expected probability of false positives based on
     * the number of expected filter elements and the size of the Bloom filter.
     * <br /><br />
     * The value returned by this method is the <i>expected</i> rate of false
     * positives, assuming the number of inserted elements equals the number of
     * expected elements. If the number of elements in the Bloom filter is less
     * than the expected value, the true probability of false positives will be lower.
     *
     * @return expected probability of false positives.
     */
    public double expectedFalsePositiveProbability() {
        return getFalsePositiveProbability(expectedNumberOfFilterElements);
    }

    /**
     * Calculate the probability of a false positive given the specified
     * number of inserted elements.
     *
     * @param numberOfElements number of inserted elements.
     * @return probability of a false positive.
     */
    public double getFalsePositiveProbability(double numberOfElements) {
        // (1 - e^(-k * n / m)) ^ k
        return Math.pow((1 - Math.exp(-k * (double) numberOfElements
                        / (double) bitSetSize)), k);

    }

    /**
     * Get the current probability of a false positive. The probability is calculated from
     * the size of the Bloom filter and the current number of elements added to it.
     *
     * @return probability of false positives.
     */
    public double getFalsePositiveProbability() {
        return getFalsePositiveProbability(numberOfAddedElements);
    }

    // http://stackoverflow.com/questions/4485128/how-do-i-convert-long-to-byte-and-back-in-java
    public static byte[] longToBytes(long l) {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte)(l & 0xFF);
            l >>= 8;
        }
        return result;
    }

    /**
     * Returns the value chosen for K.<br />
     * <br />
     * K is the optimal number of hash functions based on the size
     * of the Bloom filter and the expected number of inserted elements.
     *
     * @return optimal k.
     */
    public int getK() {
        return k;
    }

    /**
     * Sets all bits to false in the Bloom filter.
     */
    public void clear() {
        bitset.clear();
        numberOfAddedElements = 0;
    }

    /**
     * Adds an object to the Bloom filter. The output from the object's
     * toString() method is used as input to the hash functions.
     *
     * @param element is an element to register in the Bloom filter.
     */
    public void add(long element) {
       add(longToBytes(element));
    }

    /**
     * Adds an array of bytes to the Bloom filter.
     *
     * @param bytes array of bytes to add to the Bloom filter.
     */
    public void add(byte[] bytes) {
       int[] hashes = createHashes(bytes, k);
       for (int hash : hashes)
           bitset.set(Math.abs(hash % bitSetSize), true);
       numberOfAddedElements ++;
    }

    /**
     * Adds all elements from a Collection to the Bloom filter.
     * @param c Collection of elements.
     */
    public void addAll(Collection<Long> c) {
        for (Long element : c)
            add(element);
    }
        
    /**
     * Returns true if the element could have been inserted into the Bloom filter.
     * Use getFalsePositiveProbability() to calculate the probability of this
     * being correct.
     *
     * @param element element to check.
     * @return true if the element could have been inserted into the Bloom filter.
     */
    public boolean contains(long element) {
        return contains(longToBytes(element));
    }

    /**
     * Returns true if the array of bytes could have been inserted into the Bloom filter.
     * Use getFalsePositiveProbability() to calculate the probability of this
     * being correct.
     *
     * @param bytes array of bytes to check.
     * @return true if the array could have been inserted into the Bloom filter.
     */
    public boolean contains(byte[] bytes) {
        int[] hashes = createHashes(bytes, k);
        for (int hash : hashes) {
            if (!bitset.get(Math.abs(hash % bitSetSize))) {
                return false;
            }
        }
        return true;
    }
    


    /**
     * Returns true if all the elements of a Collection could have been inserted
     * into the Bloom filter. Use getFalsePositiveProbability() to calculate the
     * probability of this being correct.
     * @param c elements to check.
     * @return true if all the elements in c could have been inserted into the Bloom filter.
     */
    public boolean containsAll(Collection<Long> c) {
        for (Long element : c)
            if (!contains(element))
                return false;
        return true;
    }

    /**
     * Read a single bit from the Bloom filter.
     * @param bit the bit to read.
     * @return true if the bit is set, false if it is not.
     */
    public boolean getBit(int bit) {
        return bitset.get(bit);
    }

    /**
     * Set a single bit in the Bloom filter.
     * @param bit is the bit to set.
     * @param value If true, the bit is set. If false, the bit is cleared.
     */
    public void setBit(int bit, boolean value) {
        bitset.set(bit, value);
    }

    /**
     * Return the bit set used to store the Bloom filter.
     * @return bit set representing the Bloom filter.
     */
    public BitSet getBitSet() {
        return bitset;
    }

    /**
     * Returns the number of bits in the Bloom filter. Use count() to retrieve
     * the number of inserted elements.
     *
     * @return the size of the bitset used by the Bloom filter.
     */
    public int size() {
        return this.bitSetSize;
    }

    /**
     * Returns the number of elements added to the Bloom filter after it
     * was constructed or after clear() was called.
     *
     * @return number of elements added to the Bloom filter.
     */
    public int count() {
        return this.numberOfAddedElements;
    }

    /**
     * Returns the expected number of elements to be inserted into the filter.
     * This value is the same value as the one passed to the constructor.
     *
     * @return expected number of elements.
     */
    public int getExpectedNumberOfElements() {
        return expectedNumberOfFilterElements;
    }

    /**
     * Get expected number of bits per element when the Bloom filter is full. This value is set by the constructor
     * when the Bloom filter is created. See also getBitsPerElement().
     *
     * @return expected number of bits per element.
     */
    public double getExpectedBitsPerElement() {
        return this.bitsPerElement;
    }

    /**
     * Get actual number of bits per element based on the number of elements that have currently been inserted and the length
     * of the Bloom filter. See also getExpectedBitsPerElement().
     *
     * @return number of bits per element.
     */
    public double getBitsPerElement() {
        return this.bitSetSize / (double)numberOfAddedElements;
    }
    
    /////////////////////////////////////////////
    /* 
     * hiepnh
     */
    ////
    public int getBitSetSize(){
    	return this.bitSetSize;
    }
    
    ////
    public void union(BloomFilterLong other){
    	this.bitset.or(other.bitset);
    }
    
    
    //// hiepnh - Jun 7, 2016
    // check one element against a list of filters
    public static boolean[] containsListBF(long element, List<BloomFilterLong> filters) {
    	byte[] bytes = longToBytes(element);
    	
    	int k = filters.get(0).getK();
    	int bitSetSize = filters.get(0).getBitSetSize();
    	int n = filters.size();
    	
    	boolean[] ret = new boolean[n];
    	for (int i = 0; i < n; i++)
    		ret[i] = true;
    	
        int[] hashes = createHashes(bytes, k);		// locations of 1-bits
        
        
        for (int i = 0; i < n; i++){
        	BloomFilterLong bf = filters.get(i);
        	for (int hash : hashes){ 
	            if (!bf.bitset.get(Math.abs(hash % bitSetSize))) {
	            	ret[i] = false;
	            	break;
	            }
        	}
        }
        //
        return ret;
    }
    
    //// remove (1-alpha) fraction of 1 bits
    public BloomFilterLong removeBits(double alpha, Random random){
    	
    	BloomFilterLong ret = this.clone();
    	
    	// remove bits
    	int d = (int)( ret.bitset.cardinality() * (1 - Math.pow(alpha, 1.0/ret.k)) );	// 
    	
    	// pick d bits from ret and set them to false
    	// WAY - 1 (not random, first d bits)
    	int count = 0;
    	for (int i = ret.bitset.nextSetBit(0); i >= 0; i = ret.bitset.nextSetBit(i+1)) {
    		ret.bitset.set(i, false);
    		count ++;
    		if (count == d)
    			break;
    	}
    	// WAY - 2 ??
//    	int count = 0;
//    	for (int i = ret.bitset.nextSetBit(0); i >= 0; i = ret.bitset.nextSetBit(i+1)) {
//    		ret.bitset.set(i, false);
//    		count ++;
//    		if (count == d)
//    			break;
//    	}
    	
    	
    	//
    	return ret;
    }
    
    ////
    public static byte[] compressBF(BloomFilterLong bf) throws IOException{
    	byte[] b = bf.bitset.toByteArray();
    	
    	InputStream in = new ByteArrayInputStream(b);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		BitOutputStream bitOut = new BitOutputStream(out);
		
		FrequencyTable freq = new SimpleFrequencyTable(new int[257]);
		for (byte x : b)
			freq.increment(x & 0xFF);
		freq.increment(256);  // EOF symbol gets a frequency of 1
		
		//
		ArithmeticCompress.writeFrequencies(bitOut, freq);
		ArithmeticCompress.compress(freq, in, bitOut);
		bitOut.close();
		return out.toByteArray();
    }
    
    ////
    public static BloomFilterLong decompressBF(byte[] b, double falsePositive, int m, int numberOfAddedElements) throws IOException{
    	BloomFilterLong bf = new BloomFilterLong(falsePositive, m);
    	bf.numberOfAddedElements = numberOfAddedElements;
    	//
    	InputStream in = new ByteArrayInputStream(b);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		BitInputStream bitIn = new BitInputStream(in);
		
		FrequencyTable freq = ArithmeticDecompress.readFrequencies(bitIn);
		ArithmeticDecompress.decompress(freq, bitIn, out);
		
		bf.bitset = BitSet.valueOf(out.toByteArray());
		
		return bf;
    }
    
	////////////////////////////////////////////////
	public static void main(String[] args) throws Exception{
		
		
	}
}