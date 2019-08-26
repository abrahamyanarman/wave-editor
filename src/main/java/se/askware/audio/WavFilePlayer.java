package se.askware.audio;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WavFilePlayer implements Runnable {
	private File wavFile;

    private static boolean running = false;

	private AudioInputStream audioInputStream;

	private SourceDataLine auline;

	private long currentPosition;

	private List<PlayerPositionListener> listeners = new ArrayList<PlayerPositionListener>();

	private List<PlayerModeListener> modeListeners = new ArrayList<PlayerModeListener>();

	private FloatControl volumeControl;

	WavFilePlayer(File wavfile) throws IOException {
		wavFile = wavfile;
		if (!wavFile.exists()) {
			System.err.println("Wave file not found: " + wavFile);
			return;
		}
		setupAudio();
	}

	public void run() {

		running = true;
		notifyStarted();

		int nBytesRead = 0;
        int EXTERNAL_BUFFER_SIZE = 1000;
        byte[] abData = new byte[EXTERNAL_BUFFER_SIZE];

		try {
			while (running && nBytesRead != -1) {
				synchronized (this) {
                    nBytesRead = audioInputStream
                            .read(abData, 0, abData.length);
                    currentPosition += nBytesRead;

                    notifyPositionChanged(currentPosition);
                    if (nBytesRead >= 0) {
                        auline.write(abData, 0, nBytesRead);
                    }
                }
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		running = false;
		notifyStopped();
	}

	private void notifyPositionChanged(long newPosition) {
		for (PlayerPositionListener listener : listeners) {
			listener.positionChanged(newPosition);
		}
	}

	private void notifyStarted() {
		for (PlayerModeListener listener : modeListeners) {
			listener.playerStarted();
		}
	}

	private void notifyStopped() {
		for (PlayerModeListener listener : modeListeners) {
			listener.playerStopped();
		}
	}

	void addPlayerPositionListener(PlayerPositionListener listener) {
		listeners.add(listener);
	}

	void addPlayModeListener(PlayerModeListener playerModeListener) {
		modeListeners.add(playerModeListener);
	}

	public static   void stop() {
		running = false;
	}


	private void start() {
		if (!running) {
			new Thread(this).start();
		}
	}

	private void setupAudio() throws IOException {

		setupAudioInput();

		setupAudioOutput();
	}

	private void setupAudioOutput() {
		AudioFormat format = audioInputStream.getFormat();
		auline = null;
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
		System.out.println(format.getSampleSizeInBits());
		System.out.println(format.getChannels());

		try {
			auline = (SourceDataLine) AudioSystem.getLine(info);
			auline.open(format);
			volumeControl = (FloatControl) auline
					.getControl(FloatControl.Type.MASTER_GAIN);

		} catch (Exception e) {
			e.printStackTrace();
		}

		auline.start();
	}

	private void setupAudioInput() throws IOException {
		if (audioInputStream != null) {
			audioInputStream.close();
		}
		try {
			audioInputStream = AudioSystem
					.getAudioInputStream(new BufferedInputStream(
							new FileInputStream(wavFile)));
		} catch (UnsupportedAudioFileException | IOException e1) {
			e1.printStackTrace();
		}
		currentPosition = 0;
	}

	void toggle() {
		if (running) {
			stop();
		} else {
			start();
		}
	}

	private void skip(int seconds) {
		AudioFormat format = audioInputStream.getFormat();
		try {
			synchronized (this) {
				long skipSize = (long) (format.getFrameRate()
						* format.getFrameSize() * seconds);
				long skipped = skip(skipSize);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void setPosition(long position) {
		synchronized (this) {
			if (position < 0) {
				setPosition(0);
			} else {
				try {
					long toSkip = position - currentPosition;
					System.out.print("Requesting position " + position
							+ ", current position = " + currentPosition
							+ ", to skip = " + toSkip);
					long skipped = skip(toSkip);
					System.out.println(". Skipped " + skipped
							+ ", new position = " + currentPosition);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			notifyPositionChanged(currentPosition);
		}
	}

	private long skip(long bytesToSkip) throws IOException {
		synchronized (this) {
			if (bytesToSkip < 0) {
				long newPosition = currentPosition + bytesToSkip;
				setupAudioInput();
				setPosition(newPosition);
				return bytesToSkip;
			} else {
				long skipped = 0;
				int frameSize = audioInputStream.getFormat().getFrameSize();
				long lastSkipped = Integer.MAX_VALUE;
				while (Math.abs(bytesToSkip - skipped) > frameSize
						&& lastSkipped > 0) {
					lastSkipped = audioInputStream.skip(bytesToSkip - skipped);
					skipped += lastSkipped;
				}
				currentPosition += skipped;
				if (audioInputStream.available() < frameSize) {
					System.out.println("Available: "
							+ audioInputStream.available());
				}
				notifyPositionChanged(currentPosition);
				return skipped;
			}
		}
	}

	void forward(boolean fast) {
		skip(fast ? 5 : 2);
	}

	void rewind(boolean fast) {
		skip(fast ? -5 : -2);
	}

	long getCurrentPosition() {
		return currentPosition;
	}

	void setVolumePercent(int percent) {
		float f = (float) (percent / 100.00);
		float max = volumeControl.getMaximum() - volumeControl.getMinimum();
		volumeControl.setValue(max * f + volumeControl.getMinimum());
		//float value = max * f + volumeControl.getMinimum();
		/*System.out.println(f + "% of span [" + volumeControl.getMinimum() + ","
				+ volumeControl.getMaximum() + "] = " + value);*/
	}
}
