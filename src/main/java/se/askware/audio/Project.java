package se.askware.audio;

import com.google.common.base.Optional;

import java.io.File;
import java.util.Properties;

public class Project {

	private enum Key {
		WAV_FILE_PATH("wavfile.path");

		private String value;

		Key(String value) {
			this.value = value;
		}
	}

	private final Properties properties = new Properties();

	Project() {
	}

	Optional<File> getWavFile() {
		return getOptionalFile(getWavFilePath());
	}

	private Optional<File> getOptionalFile(Optional<String> filePath) {
		if (filePath.isPresent()) {
			return Optional.of(new File(filePath.get()));
		} else {
			return Optional.absent();
		}
	}

	private Optional<String> getWavFilePath() {
		return getProperty();
	}

	private Optional<String> getProperty() {
		return Optional.fromNullable(properties.getProperty(Key.WAV_FILE_PATH.value));
	}

	private void setProperty(String value) {
		properties.setProperty(Key.WAV_FILE_PATH.value, value);
	}

	TracksHandler loadTracks() {
		TracksHandler tracks = new TracksHandler();
		String numTracksString = properties.getProperty("num.tracks");
		if (numTracksString != null) {
			int numTracks = Integer.parseInt(numTracksString);
			for (int i = 1; i <= numTracks; i++) {
				long streamStartPos = Long.parseLong(properties.getProperty("track." + i + ".start"));
				long streamStartEnd = Long.parseLong(properties.getProperty("track." + i + ".end"));
				String name = properties.getProperty("track." + i + ".name");
				Track t = new Track();
				t.setStreamStartPos(streamStartPos);
				t.setStreamEndPos(streamStartEnd);
				t.setName(name);
				t.setTrackId(i);
				if (name.equals("A")) {
					t.setColor("RED");
				}else if (name.equals("B")){t.setColor("BLUE");
				}else if (name.equals("C")){t.setColor("YELLOW");
				}else {t.setColor("GREEN");}
				tracks.addTrack(t);
			}
		}
		return tracks;
	}
	void setWaveFile(File waveFile) {
		setProperty(waveFile.getAbsolutePath());
	}

}
