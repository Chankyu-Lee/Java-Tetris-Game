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
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import com.zetcode.Shape.Tetrominoe;

public class Board extends JPanel {

    private final int BOARD_WIDTH = 10;
    private final int BOARD_HEIGHT = 22;
    private int periodInterval = 500;
    private int level = 1;
    private int line = 10;

    private Timer timer;
    private boolean isFallingFinished = false;
    private boolean isPaused = false;
    private int numLinesRemoved = 0;
    private int curX = 0;
    private int curY = 0;
    private int ghostBlockX = 0;
    private int ghostBlockY = 0;
    private int shapeIndex = 0;
    private JLabel scorebar;
    private JLabel levelbar;
    private JLabel linebar;
    private JLabel statusbar;
    private JPanel nextBlock;
    private JPanel holdBlock;
    private JPanel leftSidebar;
    private JPanel rightSidebar;
    private Shape curPiece;
    private Shape nextPiece;
    private Shape holdPiece;
    private SubBoard holdBoard;
    private SubBoard nextBoard;
    private Tetrominoe[] board;
    private Tetrominoe[] shapeList;
    
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
    	
    	scorebar = new JLabel(String.valueOf(numLinesRemoved));
    	list.add(new JLabel(String.valueOf("SCORE")));
    	list.add(scorebar);
    	
    	statusbar = new JLabel();
    	statusbar.setPreferredSize(new Dimension(240, 50));
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
    		
    		label.setPreferredSize(new Dimension(60, 50));
    		if (label == statusbar) {
    			label.setPreferredSize(new Dimension(160, 50));
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
    	

        nextPiece = new Shape();
    	holdPiece = new Shape();
        
    	holdBoard = new SubBoard(holdPiece, String.valueOf("HOLD BLOCK"));
    	nextBoard = new SubBoard(nextPiece, String.valueOf("NEXT BLOCK"));
    	rightSidebar.add(holdBoard);
    	rightSidebar.add(nextBoard);
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

        curPiece = new Shape();
        nextPiece.setRandomShape();
        shapeIndex = 7;
        
        board = new Tetrominoe[BOARD_WIDTH * BOARD_HEIGHT];

        clearBoard();
        newPiece();

        timer = new Timer(periodInterval, new GameCycle());
        timer.start();
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

        for (int i = 0; i < 4; i++) {

            int x = curX + curPiece.x(i);
            int y = curY - curPiece.y(i);
            board[(y * BOARD_WIDTH) + x] = curPiece.getShape();
        }

        removeFullLines();

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

        repaint();

        return true;
    }

    private void removeFullLines() {

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

            numLinesRemoved += numFullLines;

            scorebar.setText(String.valueOf(numLinesRemoved));
            isFallingFinished = true;
            curPiece.setShape(Tetrominoe.NoShape);
        }
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

            if (curPiece.getShape() == Tetrominoe.NoShape) {

                return;
            }

            int keycode = e.getKeyCode();

            // Java 12 switch expressions
            switch (keycode) {

                case KeyEvent.VK_P -> pause();
                case KeyEvent.VK_LEFT -> tryMove(curPiece, curX - 1, curY);
                case KeyEvent.VK_RIGHT -> tryMove(curPiece, curX + 1, curY);
                case KeyEvent.VK_DOWN -> tryMove(curPiece.rotateRight(), curX, curY);
                case KeyEvent.VK_UP -> tryMove(curPiece.rotateLeft(), curX, curY);
                case KeyEvent.VK_SPACE -> dropDown();
                case KeyEvent.VK_D -> oneLineDown();
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



































