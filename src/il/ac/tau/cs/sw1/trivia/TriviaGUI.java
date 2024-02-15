package il.ac.tau.cs.sw1.trivia;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class TriviaGUI 
{

	private static final int MAX_ERRORS = 3;
	private static final String CORRECT_STR = "Correct! ";
	private static final String WRONG_STR = "Wrong... ";
	private Shell shell;
	private Label scoreLabel;
	private Composite questionPanel;
	private Label startupMessageLabel;
	private Font boldFont;
	private String feedbackText = "";
	private int lastIndex = 0;
	
	// lastently visible UI elements.
	Label instructionLabel;
	Label questionLabel;
	private List<Button> answerButtons = new LinkedList<>();
	private Button passButton;
	private Button fiftyButton;
	private int score = 0;
	private int lossStreak = 0;
	private boolean first50 = true;
	private boolean firstPass = true;
	private boolean playing = true;
	private String correctAnswer;
	List<String[]> questions = new LinkedList<>();

	public void open() 
	{
		createShell();
		runApplication();
	}

	/**
	 * Creates the widgets of the application main window
	 */
	private void createShell() 
	{
		Display display = Display.getDefault();
		shell = new Shell(display);
		shell.setText("Trivia");

		// window style
		Rectangle monitor_bounds = shell.getMonitor().getBounds();
		shell.setSize(new Point(monitor_bounds.width / 3, monitor_bounds.height / 4));
		shell.setLayout(new GridLayout());

		FontData fontData = new FontData();
		fontData.setStyle(SWT.BOLD);
		boldFont = new Font(shell.getDisplay(), fontData);

		// create window panels
		createFileLoadingPanel();
		createScorePanel();
		createQuestionPanel();
	}

	/**
	 * Creates the widgets of the form for trivia file selection
	 */
	private void createFileLoadingPanel() 
	{
		final Composite fileSelection = new Composite(shell, SWT.NULL);
		fileSelection.setLayoutData(GUIUtils.createFillGridData(1));
		fileSelection.setLayout(new GridLayout(4, false));

		final Label label = new Label(fileSelection, SWT.NONE);
		label.setText("Enter trivia file path: ");

		// text field to enter the file path
		final Text filePathField = new Text(fileSelection, SWT.SINGLE | SWT.BORDER);
		filePathField.setLayoutData(GUIUtils.createFillGridData(1));

		// "Browse" button
		final Button browseButton = new Button(fileSelection, SWT.PUSH);
		browseButton.setText("Browse");
		browseButton.addSelectionListener(new SelectionListener()
		{
			public void widgetDefaultSelected(SelectionEvent e)
			{
			}
			public void widgetSelected(SelectionEvent e) 
			{
				filePathField.setText(GUIUtils.getFilePathFromFileDialog(shell));
			}
		});


		// "Play!" button
		final Button playButton = new Button(fileSelection, SWT.PUSH);
		playButton.setText("Play!");
		playButton.addSelectionListener(new SelectionListener() 
		{
			public void widgetDefaultSelected(SelectionEvent arg0) 
			{	
			}
			public void widgetSelected(SelectionEvent arg0) 
			{
				//Reset parameters
				score = 0;
				lossStreak = 0;
				lastIndex = 0;
				feedbackText = "";
				correctAnswer = "";
				playing = true;
				first50 = true;
				firstPass = true;
				questions.clear();
				scoreLabel.setText("0");
				if (filePathField.getText() != "") 
				{
					try {
						File file = new File(filePathField.getText());
						BufferedReader bReader = new BufferedReader(new FileReader(file));
						String line = bReader.readLine();
						int lineIndex = 0;
						while (line != null) 
						{
							String[] lineArr = line.split("\t");
							if(lineArr.length != 5) 
							{
								GUIUtils.showErrorDialog(shell, "Trivia file format error: Trivia file row must containg a question and four answers, seperated by tabs. (line " + lineIndex + ")");
							}
							questions.add(lineArr);
							line = bReader.readLine();
							lineIndex++;
						}
						bReader.close();	
					}
					catch(IOException e) 
					{
						GUIUtils.showErrorDialog(shell, "Trivia file format error: Trivia file row must containg a question and four answers, seperated by tabs. (line " + 1 + ")");
					}
					Collections.shuffle(questions);
					nextQuestion();
				}
			}
			
		});
	}
	
	private void nextQuestion() 
	{
		if(lastIndex < questions.size()) 
		{
			correctAnswer = questions.get(lastIndex)[1];
			List<String> answers = new ArrayList<>(Arrays.asList(Arrays.copyOfRange(questions.get(lastIndex), 1, 5)));
			Collections.shuffle(answers);
			updateQuestionPanel(questions.get(lastIndex)[0], answers);
			lastIndex++;
		}
		else 
		{
			GUIUtils.showInfoDialog(shell, "GAME OVER", "Your final score is " + score + " after " + lastIndex + " questions.");
			playing = false;
		}
	}


	/**
	 * Creates the panel that displays the lastent score
	 */
	private void createScorePanel() 
	{
		Composite scorePanel = new Composite(shell, SWT.BORDER);
		scorePanel.setLayoutData(GUIUtils.createFillGridData(1));
		scorePanel.setLayout(new GridLayout(2, false));

		final Label label = new Label(scorePanel, SWT.NONE);
		label.setText("Total score: ");

		// The label which displays the score; initially empty
		scoreLabel = new Label(scorePanel, SWT.NONE);
		scoreLabel.setLayoutData(GUIUtils.createFillGridData(1));
	}

	/**
	 * Creates the panel that displays the questions, as soon as the game
	 * starts. See the updateQuestionPanel for creating the question and answer
	 * buttons
	 */
	private void createQuestionPanel() 
	{
		questionPanel = new Composite(shell, SWT.BORDER);
		questionPanel.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
		questionPanel.setLayout(new GridLayout(2, true));

		// Initially, only displays a message
		startupMessageLabel = new Label(questionPanel, SWT.NONE);
		startupMessageLabel.setText("No question to display, yet.");
		startupMessageLabel.setLayoutData(GUIUtils.createFillGridData(2));
	}

	/**
	 * Serves to display the question and answer buttons
	 */
	private void updateQuestionPanel(String question, List<String> answers) 
	{
		// clear the question panel
		Control[] children = questionPanel.getChildren();
		for (Control control : children) 
		{
			control.dispose();
		}

		// create the instruction label
		instructionLabel = new Label(questionPanel, SWT.CENTER | SWT.WRAP);
		instructionLabel.setText(feedbackText + "Answer the following question:");
		instructionLabel.setLayoutData(GUIUtils.createFillGridData(2));

		// create the question label
		questionLabel = new Label(questionPanel, SWT.CENTER | SWT.WRAP);
		questionLabel.setText(question);
		questionLabel.setFont(boldFont);
		questionLabel.setLayoutData(GUIUtils.createFillGridData(2));

		// create the answer buttons
		answerButtons.clear();
		for (int i = 0; i < 4; i++) 
		{
			Button answerButton = new Button(questionPanel, SWT.PUSH | SWT.WRAP);
			answerButton.setText(answers.get(i));
			GridData answerLayoutData = GUIUtils.createFillGridData(1);
			answerLayoutData.verticalAlignment = SWT.FILL;
			answerButton.setLayoutData(answerLayoutData);
			answerButton.addSelectionListener(new SelectionListener() 
			{
				public void widgetDefaultSelected(SelectionEvent arg0) 
				{
				}
				public void widgetSelected(SelectionEvent arg0) 
				{
					if(playing) 
					{
						if(answerButton.getText().equals(correctAnswer)) 
						{
							score += 3;
							feedbackText = CORRECT_STR;
							lossStreak = 0;
						}
						else 
						{
							score -= 2;
							feedbackText = WRONG_STR;
							lossStreak++;
						}
						scoreLabel.setText(String.valueOf(score));
						if(lossStreak < MAX_ERRORS) 
						{
							nextQuestion();
						}
						else 
						{
							GUIUtils.showInfoDialog(shell, "GAME OVER", "Your final score is " + score + " after " + lastIndex + " questions.");
							playing = false;
						}
					}
				}
			});
			answerButtons.add(answerButton);
		}

		// create the "Pass" button to skip a question
		passButton = new Button(questionPanel, SWT.PUSH);
		passButton.setText("Pass");
		GridData data = new GridData(GridData.END, GridData.CENTER, true, false);
		data.horizontalSpan = 1;
		passButton.setLayoutData(data);
		if(!firstPass && score <= 0) 
		{
			passButton.setEnabled(false);
		}
		passButton.addSelectionListener(new SelectionListener() 
		{
			public void widgetDefaultSelected(SelectionEvent arg0) 
			{
			}
			public void widgetSelected(SelectionEvent arg0) 
			{
				if(playing) 
				{
					if(!firstPass) 
					{
						score--;
						scoreLabel.setText(String.valueOf(score));	
					}
					feedbackText = "";
					firstPass = false;
					nextQuestion();
				}
			}
		});
		
		// create the "50-50" button to show fewer answer options
		fiftyButton = new Button(questionPanel, SWT.PUSH);
		fiftyButton.setText("50-50");
		data = new GridData(GridData.BEGINNING, GridData.CENTER, true,	false);
		data.horizontalSpan = 1;
		fiftyButton.setLayoutData(data);
		if(!first50 && score <= 0) 
		{
			fiftyButton.setEnabled(false);
		}
		fiftyButton.addSelectionListener(new SelectionListener() 
		{
			public void widgetDefaultSelected(SelectionEvent arg0) 
			{
			}
			public void widgetSelected(SelectionEvent arg0) 
			{
				if(playing) 
				{
					if(!first50) 
					{
						score--;
						scoreLabel.setText(String.valueOf(score));	
					}
					first50 = false;
					int i = 0;
					while (i < 2) 
					{
						Random rand = new Random();
						int eliminatedAnswer =rand.nextInt(4);
						if(!answerButtons.get(eliminatedAnswer).getText().equals(correctAnswer) && answerButtons.get(eliminatedAnswer).getEnabled()) 
						{
							answerButtons.get(eliminatedAnswer).setEnabled(false);
							i++;
						}
					}
					fiftyButton.setEnabled(false);
				}	
			}
		});

		// two operations to make the new widgets display properly
		questionPanel.pack();
		questionPanel.getParent().layout();
	}
	
	/**
	 * Opens the main window and executes the event loop of the application
	 */
	private void runApplication() 
	{
		shell.open();
		Display display = shell.getDisplay();
		while (!shell.isDisposed()) 
		{
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
		boldFont.dispose();
	}
}
