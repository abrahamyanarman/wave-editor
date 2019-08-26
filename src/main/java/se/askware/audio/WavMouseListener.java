package se.askware.audio;

import se.askware.audio.SingleWaveformPanel.Mode;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

final class WavMouseListener extends MouseAdapter implements MouseWheelListener {
	private final SingleWaveformPanel panel;
	private final WavFilePlayer player;
	private final TableModel tableModel;

	WavMouseListener(SingleWaveformPanel panel, WavFilePlayer player, TableModel tableModel) {
		this.panel = panel;
		this.player = player;
		this.tableModel = tableModel;
		panel.addMouseWheelListener(this);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.isControlDown()){
			Track track = panel.getCurrentTrack();
			if (panel.getMode() == Mode.MOVE_START) {
				track.setStreamStartPos(panel
						.viewPositionToStreamPosition(e.getX()));
			} else {
				panel.setZoomEndPos(e.getX());
			}
			panel.repaint();

			((AbstractTableModel) tableModel).fireTableDataChanged();
		}else {
		if (e.getButton() != MouseEvent.BUTTON1) {
			panel.zoomOut();
		}
		else {
			long position = panel.viewPositionToStreamPosition(e.getX());
			player.setPosition(position);
		}
		panel.requestFocus();

		panel.repaint();
		panel.setMode(Mode.NONE);
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.isControlDown()) {
			panel.createNewTrack(e.getX());
			panel.setMode(Mode.NONE);
			panel.repaint();
			((AbstractTableModel) tableModel).fireTableDataChanged();
		}
		panel.setMarkerPos(0);
		panel.repaint();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (panel.getMode() == Mode.ZOOM) {
			panel.setZoomEndPos(e.getX());
			panel.zoomSelection();
		}
		panel.setMode(Mode.NONE);
		panel.setCurrentEditedTrack(null);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		panel.requestFocus();
	}

	@Override
	public void mouseExited(MouseEvent e) {
		panel.setMarkerPos(0);
		panel.repaint();
	}
	@Override
	public  void mouseWheelMoved(MouseWheelEvent e) {
		if (e.getUnitsToScroll() == (-3)){

			PlayerController.zoomIn();
		}else if (e.getUnitsToScroll() == 3){PlayerController.zoomOut();}
	}
}