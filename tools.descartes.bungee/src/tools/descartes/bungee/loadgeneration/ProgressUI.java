/*******************************************************************************
Copyright 2015 Andreas Weber, Nikolas Herbst

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*******************************************************************************/

package tools.descartes.bungee.loadgeneration;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class ProgressUI extends JPanel  implements ActionListener, ProgressController {

	private static final long serialVersionUID = 1L;

	private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private Process process = null;
	private JFrame frame;
	private JLabel timeLabel;
	private JProgressBar progressBar;

	private WindowListener listener = new WindowAdapter() {
		@Override 
		public void windowClosing(WindowEvent e) {
			System.out.println("jooo9.2");
			if (process != null)
			{
				process.destroy();
				process = null;
			}
		}};

		public ProgressUI() {
			super(new BorderLayout());

			progressBar = new JProgressBar(0, 100);
			progressBar.setValue(0);
			progressBar.setStringPainted(true);

			JButton abortButton = new JButton("Abort");
			abortButton.setActionCommand("abort");
			abortButton.addActionListener(this);

			JPanel infoPanel = new JPanel();


			JLabel label = new JLabel("Estimated Finishing Time: ");
			timeLabel = new JLabel("?");

			infoPanel.add(label);
			infoPanel.add(timeLabel);

			JPanel controlPanel = new JPanel();
			controlPanel.add(progressBar);
			controlPanel.add(abortButton);

			add(infoPanel, BorderLayout.PAGE_START);
			add(controlPanel, BorderLayout.PAGE_END);
		}

		/**
		 * Create the GUI and show it. As with all GUI code, this must run
		 * on the event-dispatching thread.
		 */
		private void createAndShowUI() {


			frame = new JFrame("Measurement Progress");
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

			setOpaque(true); //content panes must be opaque
			frame.setContentPane(this);
			//Display the window.
			frame.pack();

			frame.setVisible(true);

			frame.addWindowListener(listener);

		}


		public void processStarted(Process process, final long estimatedFinishingTime) {
			this.process = process;
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					timeLabel.setText(formatter.format(new Date(estimatedFinishingTime)));
					createAndShowUI();
				}
			});
		}

		public void currentProgress(double percentage) {
			progressBar.setValue((int) (percentage*100));
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			closeWindow();
		}

		@Override
		public void finished() {
			try{
				frame.removeWindowListener(listener);
				frame.setVisible(false);
			}catch(NullPointerException e){
				System.out.println("any error in UI");
				System.out.println(e);
			}
		}

		private void closeWindow() {
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
				}
			});
		}
}
