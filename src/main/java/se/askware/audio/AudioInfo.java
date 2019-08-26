package se.askware.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

class AudioInfo {
	private static final int NUM_BITS_PER_BYTE = 8;

	private AudioInputStream audioInputStream;
	private int[][] samplesContainer;


	private int sampleMax = 0;
	private int sampleMin = 0;
	private double biggestSample;
	private int numberOfChannels = 0;
	
	private long startPos;
	private long endPos;

	private File wavFile;

	private long streamLength;

	AudioInfo(File wavFile, int initialNumSamples) {
		this.wavFile = wavFile;
		try {
			initAudioStream();
		}catch (Exception e){e.printStackTrace();}


		startPos = 0;

		try{
			assert audioInputStream != null;
			endPos = audioInputStream.getFormat().getFrameSize()
                    * audioInputStream.getFrameLength();
            streamLength = endPos;
            createSampleArrayCollection(0, endPos, initialNumSamples);
        }
        catch (Exception e){
		    e.printStackTrace();
        }


		// createSampleArrayCollection();
	}

	private void initAudioStream() throws IOException {
		if (audioInputStream != null) {
			audioInputStream.close();
		}
		try {


		this.audioInputStream =  AudioSystem
				.getAudioInputStream(new BufferedInputStream(new FileInputStream(wavFile)));
		}catch (Exception e){e.printStackTrace();}
		initNumberOfChannels();
	}

	int getNumberOfChannels() {
		return numberOfChannels;
	}

	private void initNumberOfChannels() {
		int numBytesPerSample = audioInputStream.getFormat()
				.getSampleSizeInBits()
				/ NUM_BITS_PER_BYTE;
		numberOfChannels = audioInputStream.getFormat().getFrameSize() / numBytesPerSample;
	}

	void createSampleArrayCollection(int numSamples) {
		createSampleArrayCollection(getStartPos(), getEndPos(), numSamples);
	}

	private void createSampleArrayCollection(long startPos, long endPos,
			int numSamples) {
		sampleMax = 0;
		sampleMin = 0;
		biggestSample = 0;
		try {
			initAudioStream();
			long skipped = skip(startPos);
			System.out.println("reading " + startPos + " " + skipped + " " + endPos);
			long streamLength = endPos - startPos;

			int sampleSize = getNumberOfChannels() * 2;
			byte[] samples = new byte[numSamples * sampleSize];
			int offset = 0;

			int read = 0;
			int totalRead = 0;
			while (read >= 0 && offset < samples.length) {
				int chunkSize = (int) (streamLength / numSamples);
				byte[] chunk = new byte[sampleSize];
				read = audioInputStream.read(chunk);
				read += skip(chunkSize - sampleSize);
				//System.out.println(" " + chunkSize + " " + read);
				System.arraycopy(chunk, 0, samples, offset, sampleSize);
				streamLength -= read;				
				numSamples--;
				offset += sampleSize;
				totalRead += read;
			}
			samplesContainer = getSampleArray(samples);
			System.out.println(totalRead);
			System.out.println(endPos);
			if (totalRead > endPos) {
				setEndPos(totalRead);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private long skip(long bytesToSkip) throws IOException {
		long skipped = 0;
		int frameSize = audioInputStream.getFormat().getFrameSize();
		while (bytesToSkip - skipped > frameSize
				&& audioInputStream.available() > frameSize) {
			skipped += audioInputStream.skip(bytesToSkip - skipped);
		}
		return skipped;
	}

	private int[][] getSampleArray(byte[] eightBitByteArray) {
		int[][] toReturn = new int[getNumberOfChannels()][eightBitByteArray.length
				/ (2 * getNumberOfChannels())];
		int index = 0;

		// loop through the byte[]
		for (int t = 0; t < eightBitByteArray.length;) {
			// for each iteration, loop through the channels
			for (int a = 0; a < getNumberOfChannels(); a++) {
				// do the byte to sample conversion
				// see AmplitudeEditor for more info
				int low = (int) eightBitByteArray[t];
				t++;
				int high = (int) eightBitByteArray[t];
				t++;
				int sample = (high << 8) + (low & 0x00ff);

				if (sample < sampleMin) {
					sampleMin = sample;
				} else if (sample > sampleMax) {
					sampleMax = sample;
				}
				// set the value.
				toReturn[a][index] = sample;
			}
			index++;
		}
		biggestSample = Math.max(sampleMax, Math.abs(sampleMin));

		return toReturn;
	}

	double getYScaleFactor(int panelHeight) {
		return (panelHeight / (biggestSample * 3));
	}

	int[] getAudio(int channel) {
		return samplesContainer[channel];
	}


	long getStartPos() {
		return startPos;
	}

	long getEndPos() {
		return endPos;
	}

	void setStartPos(long startPos) {
		System.out.println("Request start pos = " + startPos + ". Current value = " + this.startPos);
		if (startPos < 0) {
			this.startPos = 0;
		} else if (startPos > streamLength) {
			this.startPos = streamLength;
		} else {
			this.startPos = startPos;
		}
		System.out.println("New start pos = " + this.startPos);
	}

	void setEndPos(long endPos) {
		System.out.println("Request end pos = " + endPos + ". Current value = " + this.endPos);
		if (endPos < 0) {
			this.endPos = 0;
		} else if (endPos > streamLength) {
			this.endPos = streamLength;
		} else {
			this.endPos = endPos;
		}
		System.out.println("New end pos = " + this.endPos);
	}

	private double toSeconds(long position) {
		AudioFormat format = audioInputStream.getFormat();
		return position / (format.getFrameRate()
				* format.getFrameSize());
	}
	
	String toTimeStamp(long position) {
		double sec = toSeconds(position);
		return String.format("%02d:%02d:%02d", (int)(sec / 60), (int)(sec % 60),(int)(sec * 1000 % 1000));
		
	}
}
