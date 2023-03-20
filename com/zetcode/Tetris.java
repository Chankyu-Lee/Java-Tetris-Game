package com.zetcode;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/*
Java Tetris game clone

Author: Jan Bodnar
Website: https://zetcode.com
 */
public class Tetris extends JFrame {

    public Tetris() {

        initUI();
    }

    private void initUI() {

        var board = new Board(this);
        add(board, BorderLayout.CENTER);
        
        var leftSidebar = board.getLeftSidebar();
        add(leftSidebar, BorderLayout.WEST);
        
        var rightSidebar = board.getRightSidebar();
        add(rightSidebar, BorderLayout.EAST);
        
        board.start();

        setTitle("Tetris");
        setSize(800, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {

        EventQueue.invokeLater(() -> {

            var game = new Tetris();
            game.setVisible(true);
        });
    }
}
