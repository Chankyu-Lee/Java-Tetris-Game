package com.zetcode;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

public class Sound{
	Clip bgmclip;
	List<File> bgmList;			// BGM 파일 리스트
	Map<String, File> sfxList;	// SFX 파일 리스트
	int currentBgmIndex;		// 현재 재생 중인 BGM 인덱스
	int[] currentBgmList;		// 현재 재생할 BGM 리스트
	
	public Sound() {
		bgmList = new ArrayList<>();
		bgmList.add(new File("sound\\bgm\\Change the Game.wav"));
		bgmList.add(new File("sound\\bgm\\Fill Me Up.wav"));
		bgmList.add(new File("sound\\bgm\\Last Kiss.wav"));
		bgmList.add(new File("sound\\bgm\\Rolling Blocks.wav"));
		bgmList.add(new File("sound\\bgm\\Step! Step! Step!.wav"));
		bgmList.add(new File("sound\\bgm\\별이 떨어지면.wav"));
		bgmList.add(new File("sound\\bgm\\신나게 테트리스.wav"));
		bgmList.add(new File("sound\\bgm\\카츄샤.wav"));
		bgmList.add(new File("sound\\bgm\\칼링카.wav"));
		bgmList.add(new File("sound\\bgm\\테트리스.wav"));
		bgmList.add(new File("sound\\bgm\\함께 테트리스.wav"));
		bgmList.add(new File("sound\\bgm\\혼자만의 주말.wav"));
		currentBgmList = new int[bgmList.size()];
		currentBgmIndex = bgmList.size();
		
		sfxList = new HashMap<String, File>();
		sfxList.put("Tetris", new File("sound\\sfx\\01_tetris.wav"));
		sfxList.put("b2bTetris", new File("sound\\sfx\\02_b2btetris.wav"));
		sfxList.put("Tspin", new File("sound\\sfx\\03_tspin.wav"));
		sfxList.put("TspinSingle", new File("sound\\sfx\\04_tspinsingle.wav"));
		sfxList.put("TspinDouble", new File("sound\\sfx\\05_tspindouble.wav"));
		sfxList.put("TspinTriple", new File("sound\\sfx\\06_tspintriple.wav"));
		sfxList.put("b2bTspinSingle", new File("sound\\sfx\\07_b2btspinsingle.wav"));
		sfxList.put("b2bTspinDouble", new File("sound\\sfx\\08_b2btspindouble.wav"));
		sfxList.put("b2bTspinTriple", new File("sound\\sfx\\09_b2btspintriple.wav"));
		sfxList.put("Combo1", new File("sound\\sfx\\10_combo1.wav"));
		sfxList.put("Combo2", new File("sound\\sfx\\11_combo2.wav"));
		sfxList.put("Combo3", new File("sound\\sfx\\12_combo3.wav"));
		sfxList.put("Combo4", new File("sound\\sfx\\13_combo4.wav"));
		sfxList.put("Combo5", new File("sound\\sfx\\14_combo5.wav"));
		sfxList.put("Combo6", new File("sound\\sfx\\15_combo6.wav"));
		sfxList.put("Combo7", new File("sound\\sfx\\16_combo7.wav"));
		sfxList.put("Combo8", new File("sound\\sfx\\17_combo8.wav"));
		sfxList.put("Hold", new File("sound\\sfx\\18_hold.wav"));
		sfxList.put("Levelup", new File("sound\\sfx\\19_levelup.wav"));
		sfxList.put("Move", new File("sound\\sfx\\20_move.wav"));
		sfxList.put("MoveFail", new File("sound\\sfx\\21_movefail.wav"));
		sfxList.put("Rotate", new File("sound\\sfx\\22_rotate.wav"));
		sfxList.put("RotateFail", new File("sound\\sfx\\23_rotatefail.wav"));
		sfxList.put("Victory1", new File("sound\\sfx\\24_victory01.wav"));
		sfxList.put("Victory2", new File("sound\\sfx\\24_victory02.wav"));
		sfxList.put("GameStart", new File("sound\\sfx\\25_gamestart.wav"));
		sfxList.put("LockDown", new File("sound\\sfx\\26_lockdown.wav"));
		sfxList.put("SoftDrop", new File("sound\\sfx\\27_softdrop.wav"));
	}

	// BGM 재생 메서드
	public void playBgm(float vol, boolean repeat){
	    try {
	        // Clip 객체 생성 및 설정
	        bgmclip = (Clip)AudioSystem.getLine(new Line.Info(Clip.class));
	        bgmclip.open(AudioSystem.getAudioInputStream(bgmList.get(getNextBgmIndex())));
	        bgmclip.addLineListener(new LineListener() {
	            @Override
	            public void update(LineEvent event) {
	            	if (event.getType() == LineEvent.Type.STOP) {
	                    // 현재 재생 중인 BGM이 끝나면 다음 곡을 재생
	                    try {
	                    	bgmclip.close(); // 현재 재생 중인 BGM을 닫음
	                        bgmclip.open(AudioSystem.getAudioInputStream(bgmList.get(getNextBgmIndex()))); // 새로운 BGM으로 다시 열어줌
	                        bgmclip.start(); // 새로 열린 BGM 재생
	                    } catch (Exception e) {
	                        e.printStackTrace();
	                    }
	                }
	            }
	        });
	        
	        // 볼륨 설정 및 재생
	        FloatControl volume = (FloatControl)bgmclip.getControl(FloatControl.Type.MASTER_GAIN);
	        volume.setValue(vol);
	        bgmclip.start();
	        if(repeat)
	            bgmclip.loop(bgmclip.LOOP_CONTINUOUSLY);
	    } catch(Exception e){
	        e.printStackTrace();
	    }
	}
	
	 public void stopBgm(){
         
         if(bgmclip!=null && bgmclip.isRunning()){
                 bgmclip.stop();
                 bgmclip.close();
         }
	 }
	 
	 public void playSound(File file, float vol, boolean repeat){
         try {
                 final Clip clip = (Clip)AudioSystem.getLine(new Line.Info(Clip.class));
                 clip.open(AudioSystem.getAudioInputStream(file));
                 clip.addLineListener(new LineListener() {
                         @Override
                         public void update(LineEvent event) {
                                 // TODO Auto-generated method stub
                                 if(event.getType()==LineEvent.Type.STOP){
                                         //이 부분이 없으면 효과음이 메모리에 점점 쌓여서 언젠가 크래시된다
                                         clip.close();
                                 }
                         }
                 });
                 FloatControl volume = (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
                 volume.setValue(vol);
                 clip.start();
                 if(repeat)
                         clip.loop(Clip.LOOP_CONTINUOUSLY);
         } catch(Exception e){
                 e.printStackTrace();
         }
	 }
	 
	 public int getNextBgmIndex() {
		 
		 if (currentBgmIndex == bgmList.size()) {
		        currentBgmIndex = 0;
		        Collections.shuffle(bgmList);
		 }
		    
		 return currentBgmIndex++;
	 }
	 
	 public File getSfxFile(String str) {
		 return sfxList.get(str);
	 }
}






























