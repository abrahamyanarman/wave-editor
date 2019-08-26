package se.askware.audio;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class PlayerButtonPanel extends JPanel {

	PlayerButtonPanel(PlayerController controller) {

		initGraphics(controller);

	}

	private void initGraphics(final PlayerController controller) {
		final JButton playButton = createImageButton();
		playButton.setToolTipText("Start playback (space bar)");
		playButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.togglePlay();
			}
		});
		controller.addPlayModeListener(new PlayerModeListener() {

			@Override
			public void playerStarted() {
				playButton.setIcon(new ImageIcon(getClass().getResource("/stop.png")));
			}

			@Override
			public void playerStopped() {
				playButton.setIcon(new ImageIcon(getClass().getResource("/play.png")));
			}

		});

		setLayout(new FlowLayout());
		Dimension preferredSize = new Dimension(52, 52);
		playButton.setPreferredSize(preferredSize);



		add(playButton);

	}

	private JButton createImageButton() {
		return new JButton(new ImageIcon(getClass().getResource("/" + "play.png")));
	}

}
