package se.askware.audio;

import javax.swing.table.TableModel;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

final class WavKeyAdapter extends KeyAdapter{
	private PlayerController player;
	private final SingleWaveformPanel panel ;
    private final TableModel tableModel;





	WavKeyAdapter(PlayerController player, TableModel tableModel, SingleWaveformPanel panel) {
		this.player = player;
        this.panel = panel;
        panel.addKeyListener(this);
        this.tableModel = tableModel;
    }

	public void keyReleased(KeyEvent e)  {
        Track track;
		switch (e.getKeyCode()) {
		case KeyEvent.VK_SPACE:
			player.togglePlay();
			break;
		case KeyEvent.VK_RIGHT:
			if (e.isShiftDown()) {
				player.nextBoundary();
			} else if (e.isControlDown()) {
				player.fastForward();
			} else {
				player.forward();
			}
			break;
		case KeyEvent.VK_LEFT:
			if (e.isShiftDown()) {
				player.previousBoundary();
			} else if (e.isControlDown()) {
				player.fastRewind();
			} else {
				player.rewind();
			}
			break;

		}

	}



}