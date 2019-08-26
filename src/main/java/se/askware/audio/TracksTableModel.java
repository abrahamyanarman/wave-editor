package se.askware.audio;

import javax.swing.table.AbstractTableModel;

final class TracksTableModel extends AbstractTableModel {
	private final AudioInfo audioInfo;
	private final TracksHandler tracks;
	private String[] columns = new String[] { "Id", "Name", "Start","Color"};
	private static final long serialVersionUID = -5263480932620061973L;

	TracksTableModel(AudioInfo audioInfo, TracksHandler tracks) {
		this.audioInfo = audioInfo;
		this.tracks = tracks;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return String.class;
	}

	@Override
	public int getColumnCount() {
		return columns.length;
	}

	@Override
	public String getColumnName(int columnIndex) {
		return columns[columnIndex];
	}

	@Override
	public int getRowCount() {
		return tracks.getNumTracks();
	}

	public String getColor(int rowIndex){
		return tracks.getTrack(rowIndex).getColor();
	}
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Track track = tracks.getTrack(rowIndex);
		switch (columnIndex) {
		case 0:
			return track.getTrackId();
		case 1:
			return track.getName();
		case 2:
			return audioInfo.toTimeStamp(track.getStreamStartPos());
		case 3:
			return track.getColor();
	/*	case 4:
			return audioInfo.toTimeStamp(track.getStreamEndPos()
					- track.getStreamStartPos());*/
		/*case 5:
			return track.getStreamStartPos();
		case 6:
			return track.getStreamEndPos();*/
		}
		return null;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex == 1;
	}

	@Override
	public void setValueAt(Object value, int rowIndex, int columnIndex) {
		if (columnIndex == 1) {
			tracks.getTrack(rowIndex).setName(value.toString());
		}
	}
}