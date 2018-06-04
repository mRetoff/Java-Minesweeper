import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * This program runs the game, "Minesweeper". This game has a 12x12 grid of
 * JButtons in a GUI. It keeps track of how many mines (20 mines total) are left
 * and the time elapsed. It randomly places the mines throughout the grid for
 * every game. There is also a reset button to reset the game and timer at any
 * time. Left-clicking on the buttons reveals the spaces and right-clicking
 * leaves a "!" to show as to where you think a mines is. The game ends when the
 * player either left-clicks a mine or when every mine is marked with a "!".
 * 
 * @author Matt Retoff
 */
public class Minesweeper2 extends JFrame {
	private JLabel mine, timer;
	private JTextField mineTF;
	private static JTextField timerTF;
	private JButton reset;
	public String[][] invisField = new String[12][12];
	public JButton[][] field = new JButton[12][12];
	private JPanel buttonsP, labelsP, fieldP;
	private FieldListener fHandler;
	private ResetListener rHandler;
	private int numOfMines = 20;
	private int count = 0;
	
	// -------------------------------------Timer-------------------------------------
	private static Timer time = new Timer();
	private static int secondsPassed = 0;
	private static TimerTask task = new TimerTask() {
		public void run() {
			secondsPassed++;
			timerTF.setText(Integer.toString(secondsPassed));
		}
	};

	public static void start() {
		task = new TimerTask() {
			public void run() {
				secondsPassed++;
				timerTF.setText(Integer.toString(secondsPassed));
			}
		};
		time.scheduleAtFixedRate(task, 1000, 1000);
	}

	public void stop() {
		task.cancel();
	}

	// -------------------------------------------------------------------------------
	public Minesweeper2() {
		// JLabels
		mine = new JLabel("Mines Remaining: ");
		timer = new JLabel("Timer: ");

		// JTextFields
		mineTF = new JTextField("20");
		mineTF.setEditable(false);
		mineTF.setHorizontalAlignment(JTextField.CENTER);
		timerTF = new JTextField();
		timerTF.setEditable(false);
		timerTF.setHorizontalAlignment(JTextField.CENTER);
		timerTF.setText("0");

		// JButtons
		reset = new JButton("Reset");
		rHandler = new ResetListener();
		reset.addActionListener(rHandler);
		fHandler = new FieldListener();
		for (int i = 0; i < 12; i++) {
			for (int j = 0; j < 12; j++) {
				field[i][j] = new JButton();
				field[i][j].setMargin(new Insets(0, 0, 0, 0));
				field[i][j].setFont(new Font("Arial", Font.PLAIN, 25));
				field[i][j].addMouseListener(fHandler);
			}
		}

		// JPanel
		labelsP = new JPanel();
		labelsP.setLayout(new BoxLayout(labelsP, BoxLayout.LINE_AXIS));
		labelsP.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		labelsP.add(mine);
		labelsP.add(mineTF);
		labelsP.add(Box.createRigidArea(new Dimension(100, 0)));
		labelsP.add(timer);
		labelsP.add(timerTF);

		buttonsP = new JPanel();
		buttonsP.add(reset);

		fieldP = new JPanel();
		fieldP.setLayout(new GridLayout(12, 12, 2, 2));

		fieldP.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		for (int i = 0; i < 12; i++)
			for (int j = 0; j < 12; j++)
				fieldP.add(field[i][j]);

		// Content Pane
		Container pane = getContentPane();
		pane.setLayout(new BorderLayout());
		pane.add(labelsP, BorderLayout.NORTH);
		pane.add(buttonsP, BorderLayout.SOUTH);
		pane.add(fieldP, BorderLayout.CENTER);

		// JFrame
		setTitle("Minesweeper");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
		setSize(500, 500);
		
		// Set up invisField
		for (int i = 0; i < 12; i++)
			for (int j = 0; j < 12; j++)
				invisField[i][j] = "";
		int mines1, mines2;
		for (int i = 0; i < 20; i++) // Places 20 mines randomly
		{
			mines1 = ThreadLocalRandom.current().nextInt(0, 12); // Row
			mines2 = ThreadLocalRandom.current().nextInt(0, 12); // Column
			if (invisField[mines1][mines2].equals(""))
				invisField[mines1][mines2] = ("*");
			else
				i--;
		}
		placeMines(invisField);
	}

	public static void main(String[] args) {
		
		Minesweeper2 run = new Minesweeper2();
		start();
	}

	/**
	 * This class controls all left-click and right-click operations for the
	 * playing field.
	 */
	private class FieldListener implements MouseListener {
		@Override
		public void mouseClicked(MouseEvent e) {
			if (count < 20) {
				for (int i = 0; i < 12; i++) {
					for (int j = 0; j < 12; j++) {
						// -------------------------Left-Click Operations------------------------
						if (e.getSource() == field[i][j] && e.getButton() == MouseEvent.BUTTON1
								&& !(field[i][j].getText().equals("!"))) {
							field[i][j].setEnabled(false);
							// ----------------------Reveals the mines---------------------------
							if (invisField[i][j].equals("*")) {
								field[i][j].setText("*");
								stop();
								for (int k = 0; k < 12; k++) {
									for (int l = 0; l < 12; l++) {
										if (invisField[k][l].equals("*")) {
											field[k][l].setText("*");
											field[k][l].setEnabled(false);
										}
									}
								}
							} // End if "Mine"
							// ---------------Reveals the numbers around the mines---------------
							else if (!(invisField[i][j].equals("")))
								field[i][j].setText(invisField[i][j]);
							// -----------------------Reveals blank spaces-----------------------
							else
								revealBlanks(i,j);
						} // End if left-click
						// ------------------------Right-Click Operations------------------------
						else if (e.getSource() == field[i][j] && e.getButton() == MouseEvent.BUTTON3) {
							if (field[i][j].getText().equals("") && field[i][j].isEnabled() && numOfMines > 0) {
								field[i][j].setText("!");
								numOfMines--;
								mineTF.setText(Integer.toString(numOfMines));
								if (invisField[i][j].equals("*"))
									count++;
							} else if (field[i][j].getText().equals("!") && field[i][j].isEnabled()) {
								field[i][j].setText("");
								numOfMines++;
								count--;
								mineTF.setText(Integer.toString(numOfMines));
							}
						} // End if right-click
					} // End inner loop
				} // End outer loop
			}
			// ------------------------Code for winning the game------------------------
			if (count == 20) {
				for (int i = 0; i < 12; i++) {
					for (int j = 0; j < 12; j++) {
						stop();
						field[i][j].setEnabled(false);
						if (field[i][j].equals("!"))
							field[i][j].setText("*");
					}
				}
				JOptionPane.showMessageDialog(null, "Congrats, you win!", "!!! WINNER !!!", JOptionPane.INFORMATION_MESSAGE);
			} // End if statement for winning game
		}// End mouseClicked

		public void mouseEntered(MouseEvent e) {}

		public void mouseExited(MouseEvent e) {}

		public void mousePressed(MouseEvent e) {}

		public void mouseReleased(MouseEvent e) {}
	}

	/**
	 * This class resets the game when the reset button is pressed.
	 */
	private class ResetListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			for (int i = 0; i < 12; i++) {
				for (int j = 0; j < 12; j++) {
					field[i][j].setText("");
					invisField[i][j] = "";
					field[i][j].setEnabled(true);
				}
			}
			int mines1, mines2;
			for (int i = 0; i < 20; i++) {
				mines1 = ThreadLocalRandom.current().nextInt(0, 12);
				mines2 = ThreadLocalRandom.current().nextInt(0, 12);
				if (invisField[mines1][mines2].equals(""))
					invisField[mines1][mines2] = "*";
				else
					i--;
			}
			placeMines(invisField);
			numOfMines = 20;
			mineTF.setText(Integer.toString(numOfMines));
			count = 0;
			secondsPassed = 0;
			timerTF.setText("0");
			stop();
			start();
		}
	}

	/**
	 * This method is for placing the mines in the invisField array.
	 * 
	 * @param invisField
	 *            A 2D array that the player doesn't see, but corresponds to a
	 *            visible JButton array
	 */
	public static void placeMines(String invisField[][]) {
		for (int i = 0; i < 12; i++) {
			for (int j = 0; j < 12; j++) {
				int numOfMines = 0;
				try {
					if (invisField[i-1][j-1].equals("*"))
						numOfMines++;
				} catch (ArrayIndexOutOfBoundsException a) {}
				try {
					if (invisField[i-1][j].equals("*"))
						numOfMines++;
				} catch (ArrayIndexOutOfBoundsException a) {}
				try {
					if (invisField[i-1][j+1].equals("*"))
						numOfMines++;
				} catch (ArrayIndexOutOfBoundsException a) {}
				try {
					if (invisField[i][j-1].equals("*"))
						numOfMines++;
				} catch (ArrayIndexOutOfBoundsException a) {}
				try {
					if (invisField[i][j+1].equals("*"))
						numOfMines++;
				} catch (ArrayIndexOutOfBoundsException a) {}
				try {
					if (invisField[i+1][j-1].equals("*"))
						numOfMines++;
				} catch (ArrayIndexOutOfBoundsException a) {}
				try {
					if (invisField[i+1][j].equals("*"))
						numOfMines++;
				} catch (ArrayIndexOutOfBoundsException a) {}
				try {
					if (invisField[i+1][j+1].equals("*"))
						numOfMines++;
				} catch (ArrayIndexOutOfBoundsException a) {}

				if ((numOfMines > 0) && invisField[i][j].equals(""))
					invisField[i][j] = Integer.toString(numOfMines);
			}
		}
	}
	
	/**
	 * This method uses recursion to reveal all blanks in a clump.
	 * @param i Row
	 * @param j Column
	 */
	public void revealBlanks(int i, int j) {
		//---------------Reveal surrounding 8 spaces---------------
		try {
			field[i-1][j-1].setText(invisField[i-1][j-1]);
			field[i-1][j-1].setEnabled(false);
		} catch (ArrayIndexOutOfBoundsException a) {}
		try {
			field[i-1][j].setText(invisField[i-1][j]);
			field[i-1][j].setEnabled(false);
		} catch (ArrayIndexOutOfBoundsException a) {}
		try {
			field[i-1][j+1].setText(invisField[i-1][j+1]);
			field[i-1][j+1].setEnabled(false);
		} catch (ArrayIndexOutOfBoundsException a) {}
		try {
			field[i][j-1].setText(invisField[i][j-1]);
			field[i][j-1].setEnabled(false);
		} catch (ArrayIndexOutOfBoundsException a) {}
		try {
			field[i][j+1].setText(invisField[i][j+1]);
			field[i][j+1].setEnabled(false);
		} catch (ArrayIndexOutOfBoundsException a) {}
		try {
			field[i+1][j-1].setText(invisField[i+1][j-1]);
			field[i+1][j-1].setEnabled(false);
		} catch (ArrayIndexOutOfBoundsException a) {}
		try {
			field[i+1][j].setText(invisField[i+1][j]);
			field[i+1][j].setEnabled(false);
		} catch (ArrayIndexOutOfBoundsException a) {}
		try {
			field[i+1][j+1].setText(invisField[i+1][j+1]);
			field[i+1][j+1].setEnabled(false);
		} catch (ArrayIndexOutOfBoundsException a) {}
		//---------------------Start Recursion---------------------
		try {
			if(invisField[i-1][j-1].equals("")) {
				invisField[i-1][j-1] = " ";
				revealBlanks(i-1,j-1);
			}
		} catch (ArrayIndexOutOfBoundsException a) {}
		try {
			if(invisField[i-1][j].equals("")) {
				invisField[i-1][j] = " ";
				revealBlanks(i-1,j);
			}
		} catch (ArrayIndexOutOfBoundsException a) {}
		try {
			if(invisField[i-1][j+1].equals("")) {
				invisField[i-1][j+1] = " ";
				revealBlanks(i-1,j+1);
			}
		} catch (ArrayIndexOutOfBoundsException a) {}
		try {
			if(invisField[i][j+1].equals("")) {
				invisField[i][j+1] = " ";
				revealBlanks(i,j+1);
			}
		} catch (ArrayIndexOutOfBoundsException a) {}
		try {
			if(invisField[i+1][j+1].equals("")) {
				invisField[i+1][j+1] = " ";
				revealBlanks(i+1,j+1);
			}
		} catch (ArrayIndexOutOfBoundsException a) {}
		try {
			if(invisField[i+1][j].equals("")) {
				invisField[i+1][j] = " ";
				revealBlanks(i+1,j);
			}
		} catch (ArrayIndexOutOfBoundsException a) {}
		try {
			if(invisField[i+1][j-1].equals("")) {
				invisField[i+1][j-1] = " ";
				revealBlanks(i+1,j-1);
			}
		} catch (ArrayIndexOutOfBoundsException a) {}
		try {
			if(invisField[i][j-1].equals("")) {
				invisField[i][j-1] = " ";
				revealBlanks(i,j-1);
			}
		} catch (ArrayIndexOutOfBoundsException a) {}
	}
}