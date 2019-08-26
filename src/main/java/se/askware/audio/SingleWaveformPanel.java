package se.askware.audio;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.lang.reflect.Field;

public class SingleWaveformPanel extends JPanel implements PlayerPositionListener, ComponentListener {

	private static final Color BACKGROUND_COLOR = new Color(0x4F4B4F);
	private static final Color REFERENCE_LINE_COLOR = Color.black;
	private static final Color[] WAVEFORM_COLOR = new Color[] { new Color(0xC2813A), new Color(0xB27437) };
	private static final Color PLAY_MARKER_COLORA = Color.RED;
	private static final Color PLAY_MARKER_COLORB = Color.YELLOW;
	private static final Color PLAY_MARKER_COLORC = Color.BLUE;
	private static final Color PLAY_MARKER_COLOR = Color.GREEN;



	private AudioInfo audio;

	private long playerPosition;

	enum Mode {
		MOVE_START, ZOOM, NONE
	}

	private Mode mode;
	private TracksHandler tracks;
	private Track currentEditedTrack = null;
	private Track currentPlayedTrack = null;
	private int markerPos;
	private int zoomStartPos;
	private int zoomEndPos;
	public static Color  trackColor;


	SingleWaveformPanel(AudioInfo audio, TracksHandler tracks) {
		this.audio = audio;
		this.tracks = tracks;
		setBackground(BACKGROUND_COLOR);
		addComponentListener(this);
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		int lineHeight = getHeight() / 2;
		g.setColor(REFERENCE_LINE_COLOR);
		g.drawLine(0, lineHeight, getWidth(), lineHeight);

		for (int i = 0; i < audio.getNumberOfChannels(); i++) {
			drawWaveform(g, audio.getAudio(i), WAVEFORM_COLOR[i]);
		}

		FontMetrics metrics = g.getFontMetrics();


		g.setColor(PLAY_MARKER_COLOR);


		int pPos = streamPositionToViewPosition(playerPosition);
		String playerTime = audio.toTimeStamp(playerPosition);
		g.drawLine(pPos, 0, pPos, getHeight() - metrics.getHeight() - 22);
		g.drawString(playerTime, pPos - metrics.stringWidth(playerTime) / 2, getHeight() - 12);

		if (markerPos > 0) {
			g.setColor(Color.PINK);
			playerTime = audio.toTimeStamp(viewPositionToStreamPosition(markerPos));
			g.drawLine(markerPos, 0, markerPos, getHeight() - metrics.getHeight() - 22);
			g.drawString(playerTime, markerPos - metrics.stringWidth(playerTime) / 2, getHeight() - 12);
		}
		if (mode == Mode.ZOOM) {
			Color trackColor = Color.PINK;
			g.setColor(new Color(trackColor .getRed(), trackColor.getGreen(), trackColor.getBlue(), 20));
			g.fillRect(zoomStartPos, 0, zoomEndPos - zoomStartPos, getHeight());
			g.setColor(trackColor);
			g.drawLine(zoomStartPos, 0, zoomStartPos, getHeight() - metrics.getHeight() -22);
			g.drawLine(zoomEndPos, 0, zoomEndPos, getHeight() - metrics.getHeight() -22);
			playerTime = audio.toTimeStamp(viewPositionToStreamPosition(zoomStartPos));
			g.drawString(playerTime, zoomStartPos - metrics.stringWidth(playerTime) / 2, getHeight() - 12);
			playerTime = audio.toTimeStamp(viewPositionToStreamPosition(zoomEndPos));
			g.drawString(playerTime, zoomEndPos - metrics.stringWidth(playerTime) / 2, getHeight() - 12);
		}

		for (Track track : tracks) {

			if (track.getName().equals("A")){
				 trackColor = Color.RED;
			}else if (track.getName().equals("B")){
				 trackColor = Color.BLUE;
			}else if (track.getName().equals("C")){
				 trackColor = Color.YELLOW;
			}else {
				trackColor = Color.GREEN;
			}

			if (track == currentPlayedTrack) {
				 trackColor = Color.BLUE;
			} else if (track == currentEditedTrack) {
				trackColor = Color.RED;
			}
			int x1 = streamPositionToViewPosition(track.getStreamStartPos());


			drawAlphaRect(g, trackColor, x1);
		}

		g.setColor(REFERENCE_LINE_COLOR);
		g.drawString(audio.toTimeStamp(audio.getStartPos()), 2, getHeight() - 2);

	}
	public static String getNameReflection(Color color) {
		try {
			//first read all fields in array
			Field[] field = Class.forName("java.awt.Color").getDeclaredFields();
			for (Field f : field) {
				String colorName = f.getName();
				Class<?> t = f.getType();
				if (t == java.awt.Color.class) {
					Color defined = (Color) f.get(null);
					if (defined.equals(color)) {
						System.out.println(colorName);
						return colorName.toUpperCase();
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Error... " + e.toString());
		}
		return "NO_MATCH";
	}
	private void drawAlphaRect(Graphics g, Color trackColor, int x1) {

		g.setColor(trackColor);
		g.drawLine(x1, 0, x1, getHeight());

	}

	private void drawWaveform(Graphics g, int[] samples, Color color) {
		if (samples == null) {
			return;
		}

		int oldX = 0;
		int oldY = (int) (getHeight() / 2);
		int xIndex = 0;

		int increment = 1;// helper.getIncrement(helper.getXScaleFactor(getWidth(
		// )));
		g.setColor(color);

		int t = 0;

		for (t = 0; t < increment; t += increment) {
			g.drawLine(oldX, oldY, xIndex, oldY);
			xIndex++;
			oldX = xIndex;
		}

		for (; t < samples.length; t += increment) {
			double scaleFactor = audio.getYScaleFactor(getHeight());
			double scaledSample = samples[t] * scaleFactor;
			int y = (int) ((getHeight() / 2) - (scaledSample));
			g.drawLine(oldX, oldY, xIndex, y);

			oldX = xIndex;
			oldY = y;
			xIndex++;
		}
	}

	void createNewTrack(int viewPosition) {
		Track track = new Track();
		track.setStreamStartPos(viewPositionToStreamPosition(viewPosition));
		track.setStreamEndPos(viewPositionToStreamPosition(viewPosition));
		String[] selectPointName = { "A", "B", "C","Other" };
		String selectedValue = (String) JOptionPane.showInputDialog( null, "Select Point or select \"Other\" for set point name auto!", "Point Name",
				JOptionPane.QUESTION_MESSAGE,
				null,
				selectPointName,
				selectPointName[ 0 ] );

		if (selectedValue.equals("A")){

			track.setName("A");
			track.setColor("RED");

		}else if (selectedValue =="B"){
			track.setName("B");
			track.setColor("BLUE");

		}else if (selectedValue=="C") {
			track.setName("C");
			track.setColor("YELLOW");

		}else {
			track.setName(null);
			track.setColor("GREEN");

		}

		tracks.addTrack(track);
		setCurrentEditedTrack(track);
		repaint();


    }

	Mode getMode() {
		return mode;
	}

	void setMode(Mode mode) {
		this.mode = mode;
	}

	Track getCurrentTrack() {
		return currentEditedTrack;
	}

	void setCurrentEditedTrack(Track currentTrack) {
		this.currentEditedTrack = currentTrack;
	}

	Iterable<Track> getTracks() {
		return tracks;
	}

	long viewPositionToStreamPosition(int viewXPosition) {
		long span = audio.getEndPos() - audio.getStartPos();
		return (long) (span * (viewXPosition / (getWidth() * 1.0)) + audio.getStartPos());
	}

	int streamPositionToViewPosition(long streamPosition) {
		long span = audio.getEndPos() - audio.getStartPos();
		double xPos = ((streamPosition - audio.getStartPos()) / (span * 1.0)) * getWidth();
		return (int) xPos;
	}

	void zoomIn(int centerXPosition) {
		long span = audio.getEndPos() - audio.getStartPos();
		long context = span / 4;
		long center = viewPositionToStreamPosition(centerXPosition);
		audio.setStartPos(center - context);
		audio.setEndPos(center + context);
		long time = System.currentTimeMillis();
		new ReaderThread().start();

		System.out.println((System.currentTimeMillis() - time) + "ms");
		repaint();
	}

	void zoomOut() {
		long span = audio.getEndPos() - audio.getStartPos();
		if (span == 0) {

			audio.setStartPos(audio.getStartPos() - getWidth() / 2);
			audio.setEndPos(audio.getEndPos() + getWidth() / 2);
		} else {
			long skip = span / 2;
			audio.setStartPos(audio.getStartPos() - skip);
			audio.setEndPos(audio.getEndPos() + skip);
		}
		new ReaderThread().start();

		repaint();
	}

	private class ReaderThread extends Thread {
		@Override
		public void run() {
			audio.createSampleArrayCollection(getWidth());
			repaint();
		}
	}

	@Override
	public void positionChanged(long newPosition) {
		playerPosition = newPosition;

		if (currentPlayedTrack != null) {
			if (playerPosition < currentPlayedTrack.getStreamStartPos()
					|| playerPosition > currentPlayedTrack.getStreamEndPos()) {
				currentPlayedTrack = null;
			}
		} else {
			for (Track track : tracks) {
				if (playerPosition >= track.getStreamStartPos() && playerPosition <= track.getStreamEndPos()) {
					currentPlayedTrack = track;
					break;
				}
			}
		}

		repaint();
	}

	void center(long center) {
		long span = audio.getEndPos() - audio.getStartPos();
		long context = span / 2;
		audio.setStartPos(center - context);
		audio.setEndPos(center + context);
		audio.createSampleArrayCollection(getWidth());
		repaint();
	}

	@Override
	public void componentHidden(ComponentEvent e) {
	}

	@Override
	public void componentMoved(ComponentEvent e) {
	}

	@Override
	public void componentResized(ComponentEvent e) {
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		audio.createSampleArrayCollection(getWidth());
		repaint();
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	@Override
	public void componentShown(ComponentEvent e) {
	}

	void setMarkerPos(int x) {
		this.markerPos = x;
	}

	void setZoomStartPos(int x) {
		this.zoomStartPos = x;
	}

	void setZoomEndPos(int x) {
		this.zoomEndPos = x;
	}

	void zoomSelection() {
		if (zoomStartPos != zoomEndPos) {
			System.out.println(zoomStartPos + "-" + zoomEndPos);
			audio.setStartPos(viewPositionToStreamPosition(zoomStartPos));
			audio.setEndPos(viewPositionToStreamPosition(zoomEndPos));
			mode = Mode.NONE;
			new ReaderThread().start();
		}
		zoomStartPos = -1;
		zoomEndPos = -1;
	}
}
