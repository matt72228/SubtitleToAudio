package SubtitleToAudio;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import SubtitleToAudio.SubtitleOne;

public class UI extends JFrame implements ActionListener {

	public UI() {
		init();
	}

	public int fx, fy, fw = 800, fh = 320;
	private JButton jbtnSelectSubtitle = new JButton("Select Subtitle(srt/vtt)");
	private JFileChooser subtitleFileChooser = new JFileChooser();
	private JLabel jlLoadStatusSubtitles = new JLabel("Subtitle Status");
	
	private JFileChooser outputDirChooser = new JFileChooser();
	private JButton jbtnSelectOutputDir = new JButton("Select Output Dir");
	private JLabel jlOutputDir = new JLabel("Output Dir");
	
	private JButton jbtnSetFFMPEG = new JButton("Set ffmpeg.exe path");
	private JLabel jlFFMPEGPath = new JLabel("FFMpeg Path");
	private JFileChooser ffmpegChooser = new JFileChooser();
	
	private JButton jbtnProcess = new JButton("Process!!");

	public void init() {
		Dimension d = common.getScreenSize();
		fx = ((int) d.getWidth() / 2) - fw / 2;
		fy = ((int) d.getHeight() / 2) - fh / 2;

		this.setBounds(fx, fy, fw, fh);

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		this.getContentPane().setLayout(null);

		addButtons();
		initFileChooser();

		this.show();
	}

	public void addButtons() {
		jbtnSelectSubtitle.setBounds(10, 10, 200, 50);
		jlLoadStatusSubtitles.setBounds( 260, 10, 300, 50);
		jbtnSelectOutputDir.setBounds(10, 80, 200, 50);
		jlOutputDir.setBounds( 260, 80, 500, 50);
		jbtnSetFFMPEG.setBounds(10, 150, 200, 50);
		jlFFMPEGPath.setBounds( 260, 150, 500, 50 );
		jbtnProcess.setBounds( 10, 220, 760, 50 );
		
		jlLoadStatusSubtitles.setFont( new Font("Arial", Font.BOLD, 24) );
		jlOutputDir.setFont( new Font("Arial", Font.BOLD, 18) );
		jlFFMPEGPath.setFont( new Font("Arial", Font.BOLD, 18) );
		
		jbtnSelectSubtitle.addActionListener(this);
		jbtnSelectOutputDir.addActionListener(this);
		jbtnProcess.addActionListener(this);
		jbtnSetFFMPEG.addActionListener(this);

		this.getContentPane().add(jbtnSelectSubtitle);
		this.getContentPane().add(jlLoadStatusSubtitles);
		this.getContentPane().add(jbtnSelectOutputDir);
		this.getContentPane().add(jlOutputDir);
		this.getContentPane().add(jbtnSetFFMPEG);
		this.getContentPane().add(jbtnProcess);
		this.getContentPane().add(jlFFMPEGPath);

	}
	
	public void setLoadStatusSubtitles( String text ) {
		jlLoadStatusSubtitles.setText( text );
	}
	
	public void setOutputDirPath( String text ) {
		jlOutputDir.setText( text );
	}
	
	public void setFFMPEGPath( String text ) {
		jlFFMPEGPath.setText( text );
	}
	
	public void setProcessBtnTXT( String text ) {
		this.jbtnProcess.setText(text);
	}
	
	public void setJFTitle( String text ) {
		this.setTitle(text);
	}

	public void initFileChooser() {
		// Load Subtitle File
		subtitleFileChooser.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isDirectory() || file.getName().toLowerCase().endsWith(".srt")
						|| file.getName().toLowerCase().endsWith(".vtt");
			}

			@Override
			public String getDescription() {
				return "Subtitle Files (*.srt, *.vtt)";
			}
		});
		
		outputDirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		ffmpegChooser.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isDirectory() || file.getName().toLowerCase().endsWith(".exe");
			}

			@Override
			public String getDescription() {
				return "Subtitle Files (*.exe)";
			}
		});

	}
	
	public void showFFMPEGPathChooser() {		
		int returnValue = this.ffmpegChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedDirectory = ffmpegChooser.getSelectedFile();
            System.out.println("ffmpeg path selected: " + selectedDirectory.getAbsolutePath());
            SubtitleToAudio.FFMPEGPath = selectedDirectory.getAbsolutePath();            
            setFFMPEGPath( SubtitleToAudio.FFMPEGPath );
            
        } else {
        	
            System.out.println("ffmpeg path selected.");
        }
	}
	
	public void showOutputDirChooser() {		
		int returnValue = outputDirChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedDirectory = outputDirChooser.getSelectedFile();
            System.out.println("Selected directory: " + selectedDirectory.getAbsolutePath());
            SubtitleToAudio.OutputDir = selectedDirectory.getAbsolutePath();            
            setOutputDirPath( SubtitleToAudio.OutputDir );
        } else {
            System.out.println("No directory selected.");
        }
	}
	
	public void showSubtitleFileChooser() {		
		int result = subtitleFileChooser.showOpenDialog(null);

		if (result == JFileChooser.APPROVE_OPTION) {
			SubtitleToAudio.SrcSubtitleFile = subtitleFileChooser.getSelectedFile();
			System.out.println("Selected File嚗�" + SubtitleToAudio.SrcSubtitleFile.getAbsolutePath());			
			SubtitleToAudio.loadAndParseSubtitleFile();			
		} else {
			System.out.println("Not Selected");
		}
	}
	
	

	public void actionPerformed(ActionEvent e) {
		Object obj = e.getSource();

		if (obj.equals(jbtnSelectSubtitle)) {
			showSubtitleFileChooser();
		} else if(obj.equals(jbtnSelectOutputDir)) {
			showOutputDirChooser();
		} else if(obj.equals(jbtnProcess)) {
			SubtitleToAudio.process();
		} else if(obj.equals(jbtnSetFFMPEG)) {
			this.showFFMPEGPathChooser();
		} 

	}
	
	public static void showErrorDialog(String message, String title) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
    }
	
	public static void showOKDialog(String message, String title) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
}
