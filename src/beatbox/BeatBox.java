/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package beatbox;

import java.awt.*;
import javax.swing.*;
import javax.sound.midi.*;
import java.util.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class BeatBox  implements ChangeListener{

    JPanel mainPanel;
    JLabel sliderValue;
    JSlider changeTempo;
    ArrayList<JCheckBox> checkboxList;
    Sequencer sequencer;
    Sequence sequence;
    Track track;
    JFrame theFrame;
    
    String[] instrumentNames = {"Bass Drum", "Closed Hi-Hat", "Open Hi-Hat",
        "Acoustic Snare", "Crash Cymbal", "Hand Clap", "High Tom", "Hi Bongo",
        "Maracas", "Whistle", "Low Conga", "Cowbell", "Vibraslap", "Low-Mid Tom",
        "High Agogo", "Open Hi Conga"};
    
    int[] instruments = {35,42,46,38,49,39,50,60,70,72,64,56,58,47,67,63};
    
    
    
    public static void main(String[] args) {
       new BeatBox().buildGUI();
    }
    
    public void buildGUI(){
        
        theFrame = new JFrame("Cyber BeatBox");
        theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BorderLayout layout = new BorderLayout();
        JPanel background = new JPanel(layout);
        background.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        
        checkboxList = new ArrayList<JCheckBox>();
        Box buttonBox = new Box(BoxLayout.Y_AXIS);
        
        JButton start = new JButton("Start");
        start.addActionListener(new MyStartListener());
        buttonBox.add(start);
        
        JButton stop = new JButton("Stop");
        stop.addActionListener(new MyStopListener());
        buttonBox.add(stop);
        
        
        changeTempo = new JSlider(JSlider.HORIZONTAL,0,220, 120);
        changeTempo.addChangeListener(this);
        sliderValue = new JLabel("120");
        buttonBox.add(sliderValue);
        buttonBox.add(changeTempo);
        
        JButton upTempo = new JButton("Tempo Up");
        upTempo.addActionListener(new MyUpTempoListener());
        buttonBox.add(upTempo);
        
        JButton downTempo = new JButton("Tempo Down");
        downTempo.addActionListener(new MyDownTempoListener());
        buttonBox.add(downTempo);
        
        JButton reset = new JButton("reset");
        reset.addActionListener(new MyResetListener());
        buttonBox.add(reset);
        
        JButton serialize = new JButton("Save(serialize)");
        serialize.addActionListener(new MySendListener());
        buttonBox.add(serialize);
        
        JButton restore = new JButton("Load(restore)");
        restore.addActionListener(new MyReadInListener());
        buttonBox.add(restore);
        
        JButton exit = new JButton("exit");
        exit.addActionListener(new MyExitListener());
        buttonBox.add(exit);
        
        Box nameBox = new Box(BoxLayout.Y_AXIS);
        for(int i = 0; i < 16; i ++){
            nameBox.add(new Label(instrumentNames[i]));
        }
                
        background.add(BorderLayout.EAST, buttonBox);
        background.add(BorderLayout.WEST, nameBox);
        
        theFrame.getContentPane().add(background);
        
        GridLayout grid = new GridLayout(16,16);
        grid.setVgap(1);
        grid.setHgap(2);
        mainPanel = new JPanel(grid);
        background.add(BorderLayout.CENTER, mainPanel);
        
        for(int i = 0; i < 256; i++){
            JCheckBox c = new JCheckBox();
            c.setSelected(false);
            checkboxList.add(c);
            mainPanel.add(c);
            
        }
        
        setUpMidi();
        
        theFrame.setBounds(50, 50, 300, 300);
        theFrame.pack();
        theFrame.setVisible(true);
        
        
    }
    
    public void setUpMidi(){
        
        try{
            
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequence = new Sequence(Sequence.PPQ,4);
            track = sequence.createTrack();
            sequencer.setTempoInBPM(120);
        }catch(Exception e) {e.printStackTrace();}
    }
    
    public void buildTrackAndStart(){
        int[] trackList = null;
        
        sequence.deleteTrack(track);
        track = sequence.createTrack();
        
        for(int i = 0; i < 16; i++){
            trackList = new int[16];
            
            int key = instruments[i];
            
            for(int j = 0; j < 16; j ++){
                
                JCheckBox jc = checkboxList.get(j+16*i);
                if( jc.isSelected()){
                    trackList[j] = key;
                }
                else{
                    trackList[j] = 0;
                }
            }
            
            makeTracks(trackList);
            track.add(makeEvent(176,1,127,0,16));
            
            
        }
        
        track.add(makeEvent(192,9,1,0,15));
        try{
            
            sequencer.setSequence(sequence);
            sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
            sequencer.start();
            sequencer.setTempoInBPM(120);
            
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void stateChanged(ChangeEvent ce) {
       
        sliderValue.setText(Integer.toString(changeTempo.getValue()));
        sequencer.setTempoInBPM(changeTempo.getValue());
        
    }
    
    public class MyStartListener implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent ae) {
            buildTrackAndStart();
        }
        
    }
    
    public class MyStopListener implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent ae) {
            sequencer.stop();
        }
        
    }
    
    
    public class MyUpTempoListener implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent ae) {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float)(tempoFactor * 1.03));
            changeTempo.setValue((int)(sequencer.getTempoInBPM()));
            sliderValue.setText(Integer.toString(changeTempo.getValue()));
            
        }
        
    }
    
    public class MyDownTempoListener implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent ae) {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float)(tempoFactor * 0.97));
        }
        
    }
    
    public class MyResetListener implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent ae) {
            for(int i = 0; i < checkboxList.size(); i ++){
            
                JCheckBox c = (JCheckBox) checkboxList.get(i);
                c.setSelected(false);
                
            }
        }
        
    }
    
    public class MyExitListener implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent ae) {
            
            int exit = JOptionPane.showConfirmDialog(null, "Are you sure you want to quit?", "Warning", JOptionPane.YES_NO_OPTION);
            if(exit == JOptionPane.YES_OPTION){
                System.exit(0);
            }
            
            
        }
        
    }
    
    public void makeTracks(int[] list){
        for(int i = 0; i < 16; i++){
            int key = list[i];
            
            if(key != 0){
                track.add(makeEvent(144,9,key,100,i));
                track.add(makeEvent(128,9,key,100,i+1));
            }
        }
    }
    
    public MidiEvent makeEvent(int comd, int chan, int one, int two, int tick){
        MidiEvent event = null;
        
        try{
            ShortMessage a = new ShortMessage();
            a.setMessage(comd, chan, one, two);
            event = new MidiEvent(a, tick);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        
        return event;
        
    }
    
    public class MySendListener implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent ae) {
            boolean[] checkboxState = new boolean[256];
            
            for (int i = 0; i < checkboxState.length; i ++){
                
                JCheckBox check = (JCheckBox) checkboxList.get(i);
                if(check.isSelected()){
                    checkboxState[i] = true;
                }
            }
            
            try{
                JFileChooser fileSave = new JFileChooser();
                fileSave.showSaveDialog(theFrame);
                FileOutputStream fileStream = new FileOutputStream(fileSave.getSelectedFile());
                ObjectOutputStream os = new ObjectOutputStream(fileStream);
                os.writeObject(checkboxState);
                
            }
            catch(Exception ex){
                ex.printStackTrace();
            }
        }
        
        
    }
    
    public class MyReadInListener implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent ae) {
            
            boolean[] checkboxState = null;
            try{
                JFileChooser fileOpen = new JFileChooser();
                fileOpen.showOpenDialog(theFrame);
            
                FileInputStream fileIn = new FileInputStream(fileOpen.getSelectedFile());
                ObjectInputStream is = new ObjectInputStream(fileIn);
                checkboxState = (boolean[]) is.readObject();
            } 
            catch(Exception ex){
                ex.printStackTrace();
            }
            
            for(int i = 0; i < checkboxState.length; i ++){
                JCheckBox check = (JCheckBox) checkboxList.get(i);
                if(checkboxState[i]){
                    check.setSelected(true);
                }
                else{
                    check.setSelected(false);
                }
            }
            sequencer.stop();
            buildTrackAndStart();
        }
        
    }
    
    
    
}
