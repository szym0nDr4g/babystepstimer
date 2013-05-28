package net.davidtanzer.babysteps.ui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.JFrame;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import net.davidtanzer.babysteps.Timer;
import net.davidtanzer.babysteps.TimerEventListener;
import net.davidtanzer.babysteps.TimerFactory;
import net.davidtanzer.babysteps.TimerThread;
import net.davidtanzer.babysteps.ui.TimerPresentationModel.TimerState;

public class TimerView implements TimerEventListener {
	private static JFrame timerFrame;
	private static JTextPane timerPane;
	private final TimerPresentationModel presentationModel;

	public TimerView(final TimerPresentationModel presentationModel, final long secondsInCycle) {
		this.presentationModel = presentationModel;
		
		timerFrame = new JFrame("Babysteps Timer");
		timerFrame.setUndecorated(true);

		timerFrame.setSize(250, 120);
		timerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		timerPane = new JTextPane();
		timerPane.setContentType("text/html");
		timerPane.setText(presentationModel.getTimerHtml());
		timerPane.setEditable(false);
		timerPane.addMouseMotionListener(new MouseMotionListener() {
			private int lastX;
			private int lastY;

			@Override
			public void mouseMoved(final MouseEvent e) {
				lastX = e.getXOnScreen();
				lastY = e.getYOnScreen();
			}
			
			@Override
			public void mouseDragged(final MouseEvent e) {
				int x = e.getXOnScreen();
				int y = e.getYOnScreen();
				
				timerFrame.setLocation(timerFrame.getLocation().x + (x-lastX), timerFrame.getLocation().y + (y-lastY));
				
				lastX = x;
				lastY = y;
			}
		});
		timerPane.addHyperlinkListener(new HyperlinkListener() {
			private TimerThread timerThread;
			
			@Override
			public void hyperlinkUpdate(final HyperlinkEvent e) {
				if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					if("command://start".equals(e.getDescription())) {
						timerFrame.setAlwaysOnTop(true);
						timerPane.setText(presentationModel.getTimerHtml());
						timerFrame.repaint();
						
						Timer timer = TimerFactory.get().createTimer(secondsInCycle, presentationModel, TimerView.this);

						timerThread = new TimerThread(timer, presentationModel);
						timerThread.start();
					} else if("command://stop".equals(e.getDescription())) {
						timerThread.stopTimer();
						timerFrame.setAlwaysOnTop(false);
						
						presentationModel.setRemainingSeconds(secondsInCycle);
						presentationModel.setRunning(false);
						presentationModel.setTimerState(TimerState.NORMAL);
						
						timerPane.setText(presentationModel.getTimerHtml());
						timerFrame.repaint();
					} else  if("command://reset".equals(e.getDescription())) {
						timerThread.resetTimer();
					} else  if("command://quit".equals(e.getDescription())) {
						System.exit(0);
					}
				}
			}
		});
		timerFrame.getContentPane().add(timerPane);

		timerFrame.setVisible(true);
	}

	@Override
	public void onNewTimeAvailable(final long elapsedSeconds, final long remainingSeconds) {
		timerPane.setText(presentationModel.getTimerHtml());
		timerFrame.repaint();
	}
}