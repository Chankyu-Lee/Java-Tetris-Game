package com.zetcode;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import com.zetcode.Shape.Tetrominoe;

public class Board extends JPanel {

    private final int BOARD_WIDTH = 10;
    private final int BOARD_HEIGHT = 22;
    private int periodInterval = 500;
    private int level = 1;
    private int line = 10;
    private int dropCount = 0;
    private int comboCount = -1;

    private Timer timer;
    private Timer statusbarBlankTimer;
    private boolean isFallingFinished = false;
    private boolean isPaused = false;
    private boolean isHeld = false;
    private boolean isTSpin = false;
    private boolean isStart = false;
    private boolean isb2b = false;
    private int score = 0;
    private int curX = 0;
    private int curY = 0;
    private int ghostBlockX = 0;
    private int ghostBlockY = 0;
    private int shapeIndex = 0;
    private JLabel scorebar;
    private JLabel levelbar;
    private JLabel linebar;
    private JLabel statusbar;
    private JPanel leftSidebar;
    private JPanel rightSidebar;
    private JTextArea textArea;
    private Shape curPiece;
    private Shape nextPiece;
    private Shape holdPiece;
    private SubBoard holdBoard;
    private SubBoard nextBoard;
    private Tetrominoe[] board;
    private Tetrominoe[] shapeList;
    private Sound sound;
    private String log;
    
    public Board(Tetris parent) {

        initBoard(parent);
    }

    private void initBoard(Tetris parent) {

    	setBackground(Color.LIGHT_GRAY);
        setFocusable(true);
        addKeyListener(new TAdapter());
        setFont(new Font("digital-7", Font.BOLD, 25));
        
        leftSidebar = new JPanel();
        leftSidebar.setLayout(new GridBagLayout());
    	
        List<JLabel> list = new ArrayList<JLabel>();
        
    	levelbar = new JLabel(String.valueOf(level));
    	list.add(new JLabel(String.valueOf("LEVEL")));
    	list.add(levelbar);
    	
    	linebar = new JLabel(String.valueOf(line));
    	list.add(new JLabel(String.valueOf("LINE")));
    	list.add(linebar);
    	
    	scorebar = new JLabel(String.valueOf(score));
    	list.add(new JLabel(String.valueOf("SCORE")));
    	list.add(scorebar);
    	
    	statusbar = new JLabel();
    	statusbar.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

    	list.add(statusbar);
    	
    	GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.ipadx = 10;
        gbc.ipady = -5;
        gbc.weightx = 10;
        gbc.weighty = 2;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.NORTH;
        
    	boolean check = false;
    	
    	var iter = list.listIterator();
    	int i = 0, j = 0;
    	while (iter.hasNext()) {
    		JLabel label = iter.next();
    		label.setFont(getFont());
    		
    		label.setPreferredSize(new Dimension(100, 50));
    		if (label == statusbar) {
    			label.setPreferredSize(new Dimension(260, 50));
    			gbc.gridwidth = 2;
    			gbc.insets = new Insets(200, 10, 50, 10);
    		}
    		gbc.gridx = i;
            gbc.gridy = j;
            gbc.anchor = GridBagConstraints.NORTH;
            leftSidebar.add(label, gbc);
    		if (check) {
    			label.setHorizontalAlignment(SwingConstants.RIGHT);
    			label.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
    		}
    		
    		else label.setHorizontalAlignment(SwingConstants.LEFT);
    		check = !check;
    		
    		if (i == 1) {
    			j++;
    			i = 0;
    		} 
    		else {
    			i++;
    		}
    	}
    	
    	rightSidebar = new JPanel();
    	rightSidebar.setLayout(new BoxLayout(rightSidebar, BoxLayout.Y_AXIS));
    	
        nextPiece = new Shape();
    	holdPiece = new Shape();

    	nextBoard = new SubBoard(nextPiece, String.valueOf("NEXT BLOCK"));
    	holdBoard = new SubBoard(holdPiece, String.valueOf("HOLD BLOCK"));
    	
    	JPanel tempPanel = new JPanel();
    	tempPanel.add(nextBoard);
    	tempPanel.add(holdBoard);
    	rightSidebar.add(tempPanel);
    	//rightSidebar.add(holdBoard);
    	
        textArea = new JTextArea(10, 20);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        log = new String(String.valueOf(""));
        textArea.setText(log);

    	JScrollPane scrollPane = new JScrollPane(textArea);
    	rightSidebar.add(scrollPane);
    	
    	sound = new Sound();
    	
    	statusbarBlankTimer = new Timer(1500, new ActionListener() {
    		
    	    @Override
    	    public void actionPerformed(ActionEvent e) {
    	        statusbar.setText("");
    	    }
    	});
    }

    private int squareWidth() {

        return (int) getSize().getWidth() / BOARD_WIDTH;
    }

    private int squareHeight() {

        return (int) getSize().getHeight() / BOARD_HEIGHT;
    }

    private Tetrominoe shapeAt(int x, int y) {

        return board[(y * BOARD_WIDTH) + x];
    }

    void start() {
    	
    	timer = new Timer(periodInterval, new GameCycle());
   
        curPiece = new Shape();
        nextPiece.setRandomShape();
        shapeIndex = 7;
        
        board = new Tetrominoe[BOARD_WIDTH * BOARD_HEIGHT];

        clearBoard();
        newPiece();
    	
    	sound.playSound(sound.getSfxFile("GameStart"), 0.5f, false);
    	
    	Timer t = new Timer(770, new ActionListener() {
    	    int count = 3;
    	    @Override
    	    public void actionPerformed(ActionEvent e) {
    	        if (count == 0) {
    	            ((Timer)e.getSource()).stop();
    	            statusbar.setText("Go!");

    	            // 게임 시작
    	        	timer.start();
    	        	statusbarBlankTimer.start();
    	            isStart = true;
    	        	sound.playBgm(0.5f, false);
    	            return;
    	        }
    	        statusbar.setText(Integer.toString(count) + String.valueOf("..."));
    	        count--;
    	    }
    	});
    	
    	t.setInitialDelay(0);
    	t.start();
    }

    private void pause() {

        isPaused = !isPaused;

        if (isPaused) {

            statusbar.setText("paused");
        } else {

            statusbar.setText(String.valueOf(""));
        }

        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        holdBoard.repaint();
    	rightSidebar.repaint();
        doDrawing(g);
        
    }

    private void doDrawing(Graphics g) {

        var size = getSize();
        int boardTop = (int) size.getHeight() - BOARD_HEIGHT * squareHeight();

        for (int i = 0; i < BOARD_HEIGHT; i++) {

            for (int j = 0; j < BOARD_WIDTH; j++) {

                Tetrominoe shape = shapeAt(j, BOARD_HEIGHT - i - 1);

                if (shape != Tetrominoe.NoShape) {

                    drawSquare(g, j * squareWidth(),
                            boardTop + i * squareHeight(), shape, false);
                }
            }
        }

        if (curPiece.getShape() != Tetrominoe.NoShape) {

            for (int i = 0; i < 4; i++) {
            	 
                int gx = ghostBlockX + curPiece.x(i);
                int gy = ghostBlockY - curPiece.y(i);
                
                drawSquare(g, gx * squareWidth(),
                        boardTop + (BOARD_HEIGHT - gy - 1) * squareHeight(),
                        curPiece.getShape(), true);
                
                int x = curX + curPiece.x(i);
                int y = curY - curPiece.y(i);

                drawSquare(g, x * squareWidth(),
                        boardTop + (BOARD_HEIGHT - y - 1) * squareHeight(),
                        curPiece.getShape(), false);
            }
        }
    }

    private void dropDown() {

        int newY = curY;

        while (newY > 0) {

            if (!tryMove(curPiece, curX, newY - 1)) {

                break;
            }

            newY--;
        }

        dropCount = 3;
        pieceDropped();
    }

    private void oneLineDown() {

        if (!tryMove(curPiece, curX, curY - 1)) {

            pieceDropped();
        }
    }

    private void clearBoard() {

        for (int i = 0; i < BOARD_HEIGHT * BOARD_WIDTH; i++) {

            board[i] = Tetrominoe.NoShape;
        }
    }

    private void pieceDropped() {
    	
    	if (dropCount < 3) {
    		dropCount++;
    		return;
    	}
    	else {
    		dropCount = 0;
    	}

        for (int i = 0; i < 4; i++) {

            int x = curX + curPiece.x(i);
            int y = curY - curPiece.y(i);
            board[(y * BOARD_WIDTH) + x] = curPiece.getShape();
        }

        log(removeFullLines());
        isHeld = false;

        if (!isFallingFinished) {

            newPiece();
        }
    }

    private void newPiece() {

    	if (shapeIndex == 7) {
        	shapeList = curPiece.getRandomShapeList();
        	shapeIndex = 0;
        }
        
        curPiece.setShape(nextPiece.getShape());
        nextPiece.setShape(shapeList[shapeIndex++]);
        
        curX = BOARD_WIDTH / 2 + 1;
        curY = BOARD_HEIGHT - 1 + curPiece.minY();
        findGhostBlock();

        if (!tryMove(curPiece, curX, curY)) {

            curPiece.setShape(Tetrominoe.NoShape);
            timer.stop();

            var msg = String.valueOf("Game over.");
            statusbar.setText(msg);
        }
    }

    private boolean tryMove(Shape newPiece, int newX, int newY) {

        for (int i = 0; i < 4; i++) {

            int x = newX + newPiece.x(i);
            int y = newY - newPiece.y(i);

            if (x < 0 || x >= BOARD_WIDTH || y < 0 || y >= BOARD_HEIGHT) {

                return false;
            }

            if (shapeAt(x, y) != Tetrominoe.NoShape) {

                return false;
            }
        }

        curPiece = newPiece;
        curX = newX;
        curY = newY;
        findGhostBlock();
        isTSpin = false;

        repaint();

        return true;
    }

    private int removeFullLines() {

        int numFullLines = 0;

        for (int i = BOARD_HEIGHT - 1; i >= 0; i--) {

            boolean lineIsFull = true;

            for (int j = 0; j < BOARD_WIDTH; j++) {

                if (shapeAt(j, i) == Tetrominoe.NoShape) {

                    lineIsFull = false;
                    break;
                }
            }

            if (lineIsFull) {

                numFullLines++;

                for (int k = i; k < BOARD_HEIGHT - 1; k++) {
                    for (int j = 0; j < BOARD_WIDTH; j++) {
                        board[(k * BOARD_WIDTH) + j] = shapeAt(j, k + 1);
                    }
                }
            }
        }

        checkLineAndLevel(numFullLines);
        
        if (numFullLines > 0) {

            comboCount++;
            isFallingFinished = true;
            curPiece.setShape(Tetrominoe.NoShape);
        }
        else {
        	comboCount = -1;
        }
        
        return numFullLines;
    }
    
    private void checkLineAndLevel(int num) {
    	
    	line -= num;
    	
    	if (line <= 0) {
    		level++;
    		line += 5 + level * 5;
    		
    		if (periodInterval > 100) {
    			periodInterval -= 50;
    			timer.setDelay(periodInterval);
    		}
    		
    		setStatusbarText("level up!");
    		sound.playSound(sound.sfxList.get("Levelup"), 0.5f, false);
    	}
    	
    	levelbar.setText(String.valueOf(level));
    	linebar.setText(String.valueOf(line));
    }

    private void drawSquare(Graphics g, int x, int y, Tetrominoe shape, boolean isGhostBlock) {

        Color colors[] = {new Color(0, 0, 0), new Color(204, 102, 102),
                new Color(102, 204, 102), new Color(102, 102, 204),
                new Color(204, 204, 102), new Color(204, 102, 204),
                new Color(102, 204, 204), new Color(218, 170, 0)
        };

        var color = colors[shape.ordinal()];

        if (isGhostBlock) {
        	g.setColor(color.brighter());
        }
        else {
        	g.setColor(color);
        }
        g.fillRect(x + 1, y + 1, squareWidth() - 2, squareHeight() - 2);

        g.setColor(color.brighter());
        g.drawLine(x, y + squareHeight() - 1, x, y);
        g.drawLine(x, y, x + squareWidth() - 1, y);

        g.setColor(color.darker());
        g.drawLine(x + 1, y + squareHeight() - 1,
                x + squareWidth() - 1, y + squareHeight() - 1);
        g.drawLine(x + squareWidth() - 1, y + squareHeight() - 1,
                x + squareWidth() - 1, y + 1);
    }
    
    public JPanel getLeftSidebar() {
    	return leftSidebar;
    }
    
    public JPanel getRightSidebar() {
		return rightSidebar;
	}
    
    private void findGhostBlock() {
    	ghostBlockX = curX;
        ghostBlockY = curY;

        while (true) {

        	boolean check = true;
        	
        	for (int i = 0; i < 4; i++) {
        		
                int x = ghostBlockX + curPiece.x(i);
                int y = ghostBlockY - curPiece.y(i);

                if (x < 0 || x >= BOARD_WIDTH || y < 0 || y >= BOARD_HEIGHT) {

                    check = false;
                    break;
                }

                if (shapeAt(x, y) != Tetrominoe.NoShape) {

                    check = false;
                    break;
                }
            }

            if (!check) {

            	ghostBlockY++;
                break;
            }

            ghostBlockY--;
        }
        
    }
    
    private void holdPiece() {
    	
    	if (isHeld) {
    		return;
    	}
    	else {
    		isHeld = true;
    	}
    	
    	if (holdPiece.getShape() == Tetrominoe.NoShape) {
    		holdPiece.setShape(curPiece.getShape());
    		newPiece();
    		return;
    	}
    	
    	Tetrominoe temp;
    	temp = curPiece.getShape();
    	curPiece.setShape(holdPiece.getShape());
    	holdPiece.setShape(temp);
    	moveTop();
    	sound.playSound(sound.sfxList.get("Hold"), 0.5f, false);
    }
    
    private void moveTop() {
    	curX = BOARD_WIDTH / 2 + 1;
        curY = BOARD_HEIGHT - 1 + curPiece.minY();
        findGhostBlock();
    }
    
    private boolean isTSpin() {
    	int count = 0;
    	
    	for (int i = 0; i < 2; i++) {
    		for (int j = 0; j < 2; j++) {
    			int x = i == 0 ? curX-1 : curX+1;
    			int y = j == 0 ? curY-1 : curY+1;
    			
    			if (board[(y * BOARD_WIDTH) + x] != Tetrominoe.NoShape) count++;
    		}
    	}
    	
    	return count >= 3;
    }
    
    private void log(int numOfFullLine) {

    	int currentScore = 0;
    	String currentString = "";
    	String currentLog = "";
    	String currentSfxString = "";
    	boolean doubleScore = false;
    	
    	if (isTSpin && numOfFullLine >= 1 || numOfFullLine == 4) {
    		
    		if (isb2b) {
    			currentString += "b2b ";
    			currentLog += "백투백 ";
    			doubleScore = true;
    		}
    		else {
    			isb2b = true;
    		}
    	}
    	
    	if (isTSpin) {
  
    		isTSpin = false;  		
    		if (numOfFullLine == 0) {
    			currentScore += 2;
    			currentString += "T-Spin";
    			currentLog += "티스핀";
    			currentSfxString = "Tspin";
    		}
    		else if (numOfFullLine == 1) {
    			currentScore += 6;
    			currentString += "T-Spin Single";
    			currentLog += "티스핀 싱글";
    			if (!doubleScore) {
    				currentSfxString = "TspinSingle";
    			}
    			else {
    				currentSfxString = "b2bTspinSingle";
    			}
    		}
    		else if (numOfFullLine == 2) {
    			currentScore += 10;
    			currentString += "T-Spin Double";
    			currentLog += "티스핀 더블";
    			if (!doubleScore) {
    				currentSfxString = "TspinDouble";
    			}
    			else {
    				currentSfxString = "b2bTspinDouble";
    			}
    		}
    		else if (numOfFullLine == 3) {
    			currentScore += 15;
    			currentString += "T-Spin Triple";
    			currentLog += "티스핀 트리플";
    			if (!doubleScore) {
    				currentSfxString = "TspinTriple";
    			}
    			else {
    				currentSfxString = "b2bTspinTriple";
    			}
    		}
    	}
    	else if (numOfFullLine == 1) {
    		currentScore += 1;
    	}
    	else if (numOfFullLine == 2) {
    		currentScore += 3;
			currentString += "Double";
			currentLog += "더블";
    	}
    	else if (numOfFullLine == 3) {
    		currentScore += 6;
			currentString += "Triple";
			currentLog += "트리플";
    	}
    	else if (numOfFullLine == 4) {
    		currentScore += 10;
			currentString += "Tetris";
			currentLog += "테트리스";
			if (!doubleScore) {
				currentSfxString = "Tetris";
			}
			else {
				currentSfxString = "b2bTetris";
			}
    	}
    	
    	if (comboCount >= 9) {
    		currentScore += 16;
    		if (currentSfxString.equals("")) {
    			currentSfxString = "Combo8";
    		}
    	}
    	else if (comboCount >= 8) {
    		currentScore += 8;
    		if (currentSfxString.equals("")) {
    			currentSfxString = "Combo8";
    		}
    	}
    	else if (comboCount >= 7) {
    		currentScore += 8;
    		if (currentSfxString.equals("")) {
    			if (comboCount == 7) {
    				currentSfxString = "Combo7";
    			}
    			else {
    				currentSfxString = "Combo6";
    			}
    		}
    	}
    	else if (comboCount >= 5) {
    		currentScore += 4;
    		if (currentSfxString.equals("")) {
    			if (comboCount == 5) {
    				currentSfxString = "Combo5";
    			}
    			else {
    				currentSfxString = "Combo4";
    			}
    		}
    	}
    	else if (comboCount >= 3) {
    		currentScore += 2;
    		if (currentSfxString.equals("")) {
    			if (comboCount == 3) {
    				currentSfxString = "Combo3";
    			}
    			else {
    				currentSfxString = "Combo2";
    			}
    		}
    	}
    	else if (comboCount >= 1) {
    		currentScore += 1;
    		currentSfxString = "Combo1";
    	}
    	
    	if (comboCount >= 1) {
    		if (currentLog.equals("")) {
    			currentLog = comboCount + "콤보";
    		}
    		else {
    			currentLog = "(" + comboCount + "콤보) " + currentLog;
    		}
    	}
    	
    	if (doubleScore) {
    		currentScore *= 2;
    	}
    	
    	if (!currentLog.equals("")) {
    		log += currentLog + " (" + currentScore + "점)" + '\n';
    	}
    	score += currentScore;
    	setStatusbarText(currentString);
    	textArea.setText(log);
    	scorebar.setText(String.valueOf(score));
    	
    	if (!currentSfxString.equals("")) {
    		sound.playSound(sound.sfxList.get(currentSfxString), 0.5f, false);
    	}
    }
    
    private void setStatusbarText(String str) {
    	
    	statusbar.setText(str);
    	
    	statusbarBlankTimer.stop();
    	statusbarBlankTimer.start();
    }

    private class GameCycle implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {

            doGameCycle();
        }
    }

    private void doGameCycle() {

        update();
        repaint();
    }

    private void update() {

        if (isPaused) {

            return;
        }

        if (isFallingFinished) {

            isFallingFinished = false;
            newPiece();
        } else {

            oneLineDown();
        }
    }

    class TAdapter extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {

        	if (!isStart) {
        		
        		return;
        	}
        	
            if (curPiece.getShape() == Tetrominoe.NoShape) {

                return;
            }

            int keycode = e.getKeyCode();

            if (isPaused && keycode != KeyEvent.VK_P) return;
            
            // Java 12 switch expressions
            switch (keycode) {

                case KeyEvent.VK_P -> pause();
                case KeyEvent.VK_LEFT -> {
                	if (tryMove(curPiece, curX - 1, curY)) {
                		sound.playSound(sound.sfxList.get("Move"), 0.5f, false);
                	} 
                	else {
                		sound.playSound(sound.sfxList.get("MoveFail"), 0.5f, false);
                	}
                }
                case KeyEvent.VK_RIGHT -> {
                	if (tryMove(curPiece, curX + 1, curY)) {
                		sound.playSound(sound.sfxList.get("Move"), 0.5f, false);
                	}
                	else {
                		sound.playSound(sound.sfxList.get("MoveFail"), 0.5f, false);
                	}
                }
                case KeyEvent.VK_DOWN -> {
                	boolean check = tryMove(curPiece.rotateRight(), curX, curY);
                	if (check) {
                		sound.playSound(sound.sfxList.get("Rotate"), 0.5f, false);
                	}
                	else {
                		sound.playSound(sound.sfxList.get("RotateFail"), 0.5f, false);
                	}
                	
                	if (check && curPiece.getShape() == Tetrominoe.TShape) {
                		isTSpin = isTSpin();
                	}
                }
                case KeyEvent.VK_UP -> {
                	boolean check = tryMove(curPiece.rotateLeft(), curX, curY);
                	if (check) {
                		sound.playSound(sound.sfxList.get("Rotate"), 0.5f, false);
                	}
                	else {
                		sound.playSound(sound.sfxList.get("RotateFail"), 0.5f, false);
                	}
                	
                	if (check && curPiece.getShape() == Tetrominoe.TShape) {
                		isTSpin = isTSpin();
                	}
                }
                case KeyEvent.VK_SPACE -> {
                	dropDown();
                	sound.playSound(sound.sfxList.get("LockDown"), 0.5f, false);
                }
                case KeyEvent.VK_D -> oneLineDown();
                case KeyEvent.VK_Q -> holdPiece();
            }
        }
    }
    
    class SubBoard extends JPanel {
    	private Shape piece;
    	private JLabel subBoardName;
    	
    	public SubBoard(Shape piece, String str) {

    		piece.setShape(Shape.Tetrominoe.NoShape);
    		this.piece = piece;
    		
    		this.setPreferredSize(new Dimension(120, 150));
        	this.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        	this.setLayout(null);
        	
        	subBoardName = new JLabel(str);
        	subBoardName.setBounds(60 - str.length()/2*7, 120, 120, 30);
        	this.add(subBoardName);
		}
    	
    	@Override
    	public void paintComponent(Graphics g) {
    		super.paintComponent(g);
    		
    		g.drawRect(0, 120, 120, 30);
            doDrawing(g);
    	}
    	
    	
    	private void doDrawing(Graphics g) {
    		
            Point mid = piece.getMiddlePoint();
            int piecex = mid.x;
            int piecey = mid.y;
            
            if (piece.getShape() != Tetrominoe.NoShape) {
            	
            	for (int i = 0; i < 4; i++) {
            		
            		int x = piecex + 30*piece.x(i);
            		int y = piecey + 30*piece.y(i);
                    
                    drawSquare(g, x, y, piece.getShape(), false);
            	}
            }
            
    	}
    	private int squareWidth() { return 30;}
    	private int squareHeight() { return 30;}
    	
    	
    	private void drawSquare(Graphics g, int x, int y, Tetrominoe shape, boolean isGhostBlock) {

    		
            Color colors[] = {new Color(0, 0, 0), new Color(204, 102, 102),
                    new Color(102, 204, 102), new Color(102, 102, 204),
                    new Color(204, 204, 102), new Color(204, 102, 204),
                    new Color(102, 204, 204), new Color(218, 170, 0)
            };

            var color = colors[shape.ordinal()];

            if (isGhostBlock) {
            	g.setColor(color.brighter());
            }
            else {
            	g.setColor(color);
            }
            g.fillRect(x + 1, y + 1, squareWidth() - 2, squareHeight() - 2);

            g.setColor(color.brighter());
            g.drawLine(x, y + squareHeight() - 1, x, y);
            g.drawLine(x, y, x + squareWidth() - 1, y);

            g.setColor(color.darker());
            g.drawLine(x + 1, y + squareHeight() - 1,
                    x + squareWidth() - 1, y + squareHeight() - 1);
            g.drawLine(x + squareWidth() - 1, y + squareHeight() - 1,
                    x + squareWidth() - 1, y + 1);
                    
    		 
    	}
    	
    	private void setPiece(Shape piece) {
			this.piece = piece;
		}
		
    }
}



































