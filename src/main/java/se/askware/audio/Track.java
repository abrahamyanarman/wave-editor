package se.askware.audio;

public class Track implements Comparable<Track> {

	private String name;
	private long streamStartPos;
	private long streamEndPos;
	private int trackId;
	private String color;

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	int getTrackId() {
		return trackId;
	}

	void setTrackId(int trackId) {
		this.trackId = trackId;
	}

	String getName() {

		if (name == null) {
			return "track " + getTrackId();
		}
		return name;
	}



	void setName(String name) {

	this.name = name;

	}

	@Override
	public int compareTo(Track o) {
		return (int) (getStreamStartPos() - o.getStreamStartPos());
	}

	long getStreamStartPos() {
		return streamStartPos;
	}

	void setStreamStartPos(long streamStartPos) {
		this.streamStartPos = streamStartPos;
	}

	long getStreamEndPos() {
		return streamEndPos;
	}

	void setStreamEndPos(long streamEndPos) {
		this.streamEndPos = streamEndPos;
	}
}
