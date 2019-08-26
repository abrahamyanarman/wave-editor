package se.askware.audio;

import java.awt.Cursor;

class PlayerController {

	private static Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
	private static Cursor normalCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);

	private static WavFilePlayer player;
	private TracksHandler tracks;
	private static SingleWaveformPanel wavDisplayPanel;

	PlayerController(WavFilePlayer player, TracksHandler tracks,
					 SingleWaveformPanel wavPanel) {
		super();
		this.player = player;
		this.tracks = tracks;
		this.wavDisplayPanel = wavPanel;
	}

	void forward() {
		player.forward(false);
	}

	void fastForward() {
		player.forward(true);
	}

	void rewind() {
		player.rewind(false);
	}

	void fastRewind() {
		player.rewind(true);
	}

	void nextBoundary() {
		long newPos = tracks.getNextTrackBoundary(player.getCurrentPosition());
		player.setPosition(newPos);
		wavDisplayPanel.center(newPos);
	}

	void previousBoundary() {
		long newPos = tracks.getPreviousTrackBoundary(player
				.getCurrentPosition());
		player.setPosition(newPos);
		wavDisplayPanel.center(newPos);
	}

	static void togglePlay() {
		player.toggle();
	}

    public static void  zoomIn() {
		wavDisplayPanel.setCursor(waitCursor);
		wavDisplayPanel.zoomIn(wavDisplayPanel
				.streamPositionToViewPosition(player.getCurrentPosition()));
		wavDisplayPanel.setCursor(normalCursor);
	}

	public static void zoomOut() {
		wavDisplayPanel.setCursor(waitCursor);
		wavDisplayPanel.zoomOut();
		wavDisplayPanel.setCursor(normalCursor);
	}

	void addPlayModeListener(PlayerModeListener playerModeListener) {
		player.addPlayModeListener(playerModeListener);
	}

}
