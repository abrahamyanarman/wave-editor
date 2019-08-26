package se.askware.audio;

import se.askware.audio.SingleWaveformPanel.Mode;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.*;

final class WavMouseMotionListener implements MouseMotionListener{
	private final SingleWaveformPanel panel;
	private final TableModel tableModel;


	WavMouseMotionListener(SingleWaveformPanel panel, TableModel tableModel) {
		this.panel = panel;
		this.tableModel = tableModel;

	}

	@Override
	public void mouseDragged(MouseEvent e) {
		Track track = panel.getCurrentTrack();
		if (panel.getMode() == Mode.MOVE_START) {
			track.setStreamStartPos(panel
					.viewPositionToStreamPosition(e.getX()));

		}else {
			panel.setZoomEndPos(e.getX());			
		}
		panel.repaint();
		((AbstractTableModel) tableModel).fireTableDataChanged();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		panel.setMarkerPos(e.getX());
		panel.setCursor(Cursor.getDefaultCursor());
		panel.setMode(Mode.NONE);

		for (Track track : panel.getTracks()) {

			Cursor resizeCursor = Cursor
					.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
			if (e.getX() == panel.streamPositionToViewPosition(track
					.getStreamStartPos())) {
				panel.setCursor(resizeCursor);
				panel.setMode(Mode.MOVE_START);
				panel.setCurrentEditedTrack(track);
				break;
			} else if (e.getX() == panel.streamPositionToViewPosition(track
					.getStreamEndPos())) {
				panel.setCursor(resizeCursor);

				panel.setCurrentEditedTrack(track);
				break;
			}
		}
		panel.repaint();
	}


}