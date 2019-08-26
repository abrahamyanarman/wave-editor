package se.askware.audio;

import com.google.common.base.Optional;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static javax.swing.JOptionPane.YES_NO_OPTION;

public class WaveSplitter {

	private static  int DEFAULT_HEIGHT = 500;
	private static  int DEFAULT_WIDTH = 800;
	private Project project;
	private JFrame frame;
	private WavFilePlayer player;
	private TableModel tableModel;
	private JPanel trackPanel;
	private AudioInfo audioInfo;
	private TracksHandler tracks;
	private SingleWaveformPanel waveDisplayPanel;
	private JTable trackTable;
	private PlayerController controller;
	private TracksTableModel savingTable;


	public static void main(String[] args) throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		WaveSplitter splitter = new WaveSplitter();
		splitter.initUI();
	}

	private void initUI() {
		frame = new JFrame("Wave File Splitter");
		frame.getContentPane().setLayout(new BorderLayout());
		frame.setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));


		JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
		ImageIcon icon = new ImageIcon(getClass().getResource("/page_blank.png"));
		System.out.println(icon.getIconWidth());
		JButton newButton = new JButton("Open Wave File", icon);
		newButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

			    try{
                    project = new Project();
                    loadProject();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }

            }

		});
		p.add(newButton);


		frame.getContentPane().add(p, BorderLayout.CENTER);
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (int) ((dimension.getWidth() - frame.getWidth()) / 5);
		int y = (int) ((dimension.getHeight() - frame.getHeight()) / 5);
		frame.setLocation(x, y);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
		frame.validate();
		frame.repaint();

	}

	private void loadProject(){

		try {
			Optional<File> waveFileOptional = project.getWavFile();
			File waveFile = waveFileOptional.orNull();
			if (!waveFileOptional.isPresent() || !waveFile.exists()) {
				JFileChooser chooser = new JFileChooser(new File("."));
				FileNameExtensionFilter filter = new FileNameExtensionFilter("Wave File", "wav");
				chooser.setFileFilter(filter);

				int returnVal = chooser.showOpenDialog(frame);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
				    String path = chooser.getSelectedFile().getPath();
				    if (path.contains(".wav")){
                        frame.getContentPane().removeAll();
                        waveFile = chooser.getSelectedFile();
                        project.setWaveFile(waveFile);
                    }
                    else {
                        JOptionPane.showMessageDialog(frame,"Invalid file format!","Error",JOptionPane.ERROR_MESSAGE);
                    }

				}

			}

            if (waveFileOptional.isPresent() || (waveFile != null && waveFile.exists())){
                audioInfo = new AudioInfo(waveFile, DEFAULT_WIDTH);
                tracks = project.loadTracks();

                waveDisplayPanel = new SingleWaveformPanel(audioInfo, tracks);
                waveDisplayPanel.setMinimumSize(new Dimension(500, DEFAULT_HEIGHT));
                waveDisplayPanel.setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));

                player = new WavFilePlayer(waveFile);
                player.addPlayerPositionListener(waveDisplayPanel);

                controller = new PlayerController(player, tracks, waveDisplayPanel);
                KeyAdapter keyAdapter = new WavKeyAdapter(controller, tableModel, waveDisplayPanel);

                initTrackPanel();

                waveDisplayPanel.addMouseListener(new WavMouseListener(waveDisplayPanel, player, tableModel));

                waveDisplayPanel.addMouseMotionListener(new WavMouseMotionListener(waveDisplayPanel, tableModel));

                waveDisplayPanel.setFocusable(true);

                waveDisplayPanel.addKeyListener(keyAdapter);
                frame.addKeyListener(keyAdapter);
                frame.getContentPane().add(waveDisplayPanel, BorderLayout.CENTER);
                frame.getContentPane().add(trackPanel, BorderLayout.SOUTH);

                frame.pack();
            }

		} catch (NullPointerException | IOException e) {
			e.printStackTrace();
		}
	}

    private void initTrackPanel() {
		tableModel = new TracksTableModel(audioInfo, tracks);


		final JButton backButton = createImageButton("page_blank.png");
		backButton.setToolTipText("Open new wav file");
		frame.getContentPane().add(backButton);

		backButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				WavFilePlayer.stop();
				Thread restartThread = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							frame.dispose();
							UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
							WaveSplitter splitter1 = new WaveSplitter();
							splitter1.initUI();
						} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e1) {
							e1.printStackTrace();
						}
					}

				});
				int close = JOptionPane.showOptionDialog(null,"Are you exit of this project?","Please save the project before exiting!", 2,YES_NO_OPTION,null,null,null);
					if (close == JOptionPane.YES_OPTION) {

						WavFilePlayer.stop();
						restartThread.start();
					}else{
						return;
					}



			}
		});
		final JButton saveButton = createImageButton("save.png");
		saveButton.setToolTipText("Save Selected Row Info");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
				FileNameExtensionFilter filterTxt = new FileNameExtensionFilter("Text File", "txt");
				JFileChooser fc = new JFileChooser();
				fc.setFileFilter(filterTxt);
				int ret = fc.showSaveDialog(null);
				FileWriter fw = null;
				try {
					fw = new FileWriter(fc.getSelectedFile() + ".txt");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				for (int i=0;i<trackTable.getRowCount();i++) {


					if (i >= 0) {
						savingTable = new TracksTableModel(audioInfo, tracks);



								String name = String.valueOf("Name: " + savingTable.getValueAt(i, 1));
								String startPos = String.valueOf("Start Position: " + savingTable.getValueAt(i, 2));
								String color = String.valueOf("Color : "+savingTable.getColor(i));




						if (ret == JFileChooser.APPROVE_OPTION) {
							try {

									fw.write(name);
									fw.write(System.getProperty("line.separator"));
									fw.write(startPos);
									fw.write(System.getProperty("line.separator"));
									fw.write(color);
									fw.write(System.getProperty("line.separator"));
									fw.write(System.getProperty("line.separator"));
									fw.write(System.getProperty("line.separator"));


							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}


					} else {
						JOptionPane.showMessageDialog(null, "Please Select Audio Chunk", "No Selection", JOptionPane.ERROR_MESSAGE);
					}
				}
				try {
					fw.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}

			}
        });


		JButton zoomInButton = createImageButton("zoom_in.png");
		zoomInButton.setToolTipText("Zoom in (up arrow)");
		zoomInButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				controller.zoomIn();
			}

		});

		JButton zoomOutButton = createImageButton("zoom_out.png");
		zoomOutButton.setToolTipText("Zoom out (down arrow)");
		zoomOutButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				controller.zoomOut();
			}

		});

		JButton deleteButton = createImageButton("delete.png");
		deleteButton.setToolTipText("Remove selected track");
		deleteButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (trackTable.getSelectedRow() >= 0) {
					tracks.removeTrack(tracks.getTrack(trackTable.getSelectedRow()));
					((AbstractTableModel) tableModel).fireTableDataChanged();
					waveDisplayPanel.repaint();
				}
			}

		});



		trackPanel = new JPanel(new BorderLayout());
		trackTable = new JTable(tableModel);
		trackTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		trackTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					int selectedRow = trackTable.getSelectedRow();
					if (selectedRow >= 0) {
						Track track = tracks.getTrack(selectedRow);
						player.setPosition(track.getStreamStartPos());
					}
				}
			}

		});
		DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer();
		cellRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		trackTable.getColumnModel().getColumn(0).setPreferredWidth(10);
		trackTable.getColumnModel().getColumn(1).setPreferredWidth(100);
		trackTable.getColumnModel().getColumn(2).setPreferredWidth(20);
		trackTable.getColumnModel().getColumn(3).setPreferredWidth(20);
		/*trackTable.getColumnModel().getColumn(4).setPreferredWidth(20);*/
		trackTable.getColumnModel().getColumn(2).setCellRenderer(cellRenderer);
		trackTable.getColumnModel().getColumn(3).setCellRenderer(cellRenderer);
		/*trackTable.getColumnModel().getColumn(4).setCellRenderer(cellRenderer);*/
		JScrollPane scrollPane = new JScrollPane(trackTable);
		scrollPane.setPreferredSize(new Dimension(DEFAULT_WIDTH, 250));
		trackPanel.add(scrollPane, BorderLayout.CENTER);
		JPanel buttonPanel = new JPanel();
		buttonPanel.setAlignmentX(JComponent.RIGHT_ALIGNMENT);
		buttonPanel.add(backButton);
		buttonPanel.add(saveButton);

		buttonPanel.add(deleteButton);
		buttonPanel.add(zoomInButton);
		buttonPanel.add(zoomOutButton);

		PlayerButtonPanel playerButtons = new PlayerButtonPanel(controller);
		buttonPanel.add(playerButtons);

		JSlider volumeSlider = new JSlider(JSlider.HORIZONTAL);
		volumeSlider.setValue(100);
		volumeSlider.setMaximum(100);
		volumeSlider.setMinimum(0);
		volumeSlider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider slider = (JSlider) e.getSource();
				player.setVolumePercent(slider.getValue());
			}

		});
		volumeSlider.setPreferredSize(new Dimension(150, 30));
		buttonPanel.add(volumeSlider);

		trackPanel.add(buttonPanel, BorderLayout.SOUTH);
	}

	private JButton createImageButton(String imageFileName) {
		return new JButton(new ImageIcon(getClass().getResource("/" + imageFileName)));
	}
}
