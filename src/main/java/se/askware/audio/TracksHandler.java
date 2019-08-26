package se.askware.audio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class TracksHandler implements Iterable<Track> {

	private List<Track> tracks = new ArrayList<>();

	@Override
	public Iterator<Track> iterator() {

		return tracks.iterator();

	}

	void addTrack(Track track) {
		tracks.add(track);


		updateTrackIds();

	}

	void removeTrack(Track track) {
		tracks.remove(track);
		updateTrackIds();
	}

	private void updateTrackIds() {
		Collections.sort(tracks);
		for (int i = 0; i < tracks.size(); i++) {
			tracks.get(i).setTrackId(i + 1);
		}
	}

	long getNextTrackBoundary(long currentPosition) {
		long best = 1;
		long bestDiff = Long.MAX_VALUE;
		for (Track track : tracks) {
			long diff = track.getStreamEndPos() - currentPosition;
			if (diff > 0 && diff < bestDiff) {
				best = track.getStreamEndPos();
				bestDiff = diff;
			}
			diff = track.getStreamStartPos() - currentPosition;
			if (diff > 0 && diff < bestDiff) {
				best = track.getStreamStartPos();
				bestDiff = diff;
			}
		}
		return best;
	}

	long getPreviousTrackBoundary(long currentPosition) {
		long best = 0;
		long bestDiff = Long.MAX_VALUE;
		for (Track track : tracks) {
			long diff = currentPosition - track.getStreamEndPos();
			if (diff > 0 && diff < bestDiff) {
				best = track.getStreamEndPos();
				bestDiff = diff;
			}
			diff = currentPosition - track.getStreamStartPos();
			if (diff > 0 && diff < bestDiff) {
				best = track.getStreamStartPos();
				bestDiff = diff;
			}
		}
		return best;
	}

	int getNumTracks() {
		return tracks.size();
	}

	Track getTrack(int rowIndex) {
		Collections.sort(tracks);
		return tracks.get(rowIndex);
	}

}
