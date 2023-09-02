package SubtitleToAudio;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import marytts.LocalMaryInterface;
import marytts.modules.synthesis.Voice;
import marytts.util.data.audio.AudioDestination;
import marytts.util.data.audio.AudioPlayer;

public class SubtitleToAudio implements Runnable {

	public static void main(String args[]) {
		new Thread(new SubtitleToAudio()).start();
	}

	public static UI _UI = null;
	public static File SrcSubtitleFile = null;
	public static ArrayList<SubtitleOne> subtitles = null;
	public static String OutputDir = null;
	public static String FFMPEGPath = null;
	public static long SAFETY_OFFSET_FOR_TEMPO = 10; // ms
	public static String ZERO_TIME_IN_STRING = "00:00:00,000";

	public void run() {
		init();
		// genTTSAndSave( "Test To Marry T T S", "c:/javajar/test.wav" );
	}

	public void init() {
		_UI = new UI();
	}

	public static void loadAndParseSubtitleFile() {
		subtitles = SubtitleOne.readSubtitles(SrcSubtitleFile);
		SubtitleOne.show(subtitles);

		if (subtitles == null || subtitles.size() <= 0) {
			UI.showErrorDialog("Subtitle Load error", "Error");
			_UI.setLoadStatusSubtitles("Subtitle Status : Error");
		} else {
			/*
			 * UI.showOKDialog("Subtitle Load Fine!" + "\n" + "Line:" + subtitles.size() +
			 * "\n" + "Total Duration:" + subtitles.get(subtitles.size() - 1).endTime + "("
			 * + (float)(subtitles.get(subtitles.size() - 1).lendTime)/1000.0 + "sec)",
			 * "OK");
			 */
			_UI.setLoadStatusSubtitles("Subtitle Status : Done");
		}
	}

	public static void genTTSAndSave(String text, String filePath) {

		try {
			common.pln("genTTSAndSave.1");
			LocalMaryInterface maryTts = new LocalMaryInterface();
			common.pln("genTTSAndSave.2");
			AudioInputStream audioInputStream = maryTts.generateAudio(text); // Generate audio
			common.pln("genTTSAndSave.3");
			saveAudioAsWAV(audioInputStream, filePath);
			common.pln("genTTSAndSave.4");
			System.out.println("Audio saved as: " + filePath);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void saveAudioAsWAV(AudioInputStream audioInputStream, String filePath) throws IOException {
		File audioFile = new File(filePath);
		AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, audioFile);
	}

	public static void process() {
		common.pln("Process start");

		if (OutputDir == null || OutputDir.length() <= 0) {
			UI.showErrorDialog("OutputDir is null!", "Error");
			return;
		}

		if (SrcSubtitleFile == null || SrcSubtitleFile.getAbsolutePath() == null
				|| SrcSubtitleFile.getAbsolutePath().length() <= 0) {
			UI.showErrorDialog("SrcSubtitleFile is null!", "Error");
			return;
		}

		if (FFMPEGPath == null || FFMPEGPath.length() <= 0) {
			UI.showErrorDialog("FFMPEGPath is null!", "Error");
			return;
		}

		try {
			// A_Gen TTS WAV
			_UI.setJFTitle( "Processing : (0/4)" );
			for (int i = 0; i < subtitles.size(); i++) {
				common.pln("Gen TTS WAV" + "\t" + (i + 1) + "/" + subtitles.size());
				SubtitleOne so = subtitles.get(i);
				String outputFilePath = OutputDir + "/A_" + String.format("%06d", i) + ".wav";
				genTTSAndSave(so.subtitle, outputFilePath);
				
				_UI.setJFTitle( "Processing : (0/4) [" + (i+1) + "/" + subtitles.size() + "]" );
			}

			// B_Adjust Rate for WAV
			_UI.setJFTitle( "Processing : (1/4)" );
			for (int i = 0; i < subtitles.size(); i++) {
				common.pln("Adjust Rate for WAV" + "\t" + (i + 1) + "/" + subtitles.size());

				String inputFilePath = OutputDir + "/A_" + String.format("%06d", i) + ".wav";

				long WAVDuration = common.getWavDuration(inputFilePath);
				SubtitleOne so = subtitles.get(i);
				double tempo = 1.0 / ((double) (so.durationTime - SAFETY_OFFSET_FOR_TEMPO) / (double) WAVDuration);
				common.pln("SubtitleDuration:" + so.durationTime + "\t" + "WAVDuration:" + WAVDuration + "\t" + "tempo="
						+ tempo);

				String outputFilePath = OutputDir + "/B_" + String.format("%06d", i) + ".wav";

				String[] command = { FFMPEGPath, "-i", inputFilePath, "-filter:a", "atempo=" + tempo, outputFilePath };

				common.executeCommand(command);
				
				_UI.setJFTitle( "Processing : (1/4) [" + (i+1) + "/" + subtitles.size() + "]" );
			}

			// C_Append_silence_audio
			_UI.setJFTitle( "Processing : (2/4)" );
			long totalTimeNow = 0;
			long preTime = 0;
			long targetTime = 0;
			long diffTime = 0;
			String inputFile = null;
			String outputFile = null;
			for (int i = 0; i < subtitles.size(); i++) {
				SubtitleOne so = subtitles.get(i);

				// C0_Append_Front
				common.pln("Do Append Front silence audio");
				inputFile = OutputDir + "/B_" + String.format("%06d", i) + ".wav";
				outputFile = OutputDir + "/C0_" + String.format("%06d", i) + ".wav";
				preTime = totalTimeNow;
				targetTime = SubtitleOne.calculateTotalLength(ZERO_TIME_IN_STRING, so.startTime);
				diffTime = targetTime - preTime;
				common.pln("preTime=" + totalTimeNow + "\t" + "targetTime=" + targetTime + "\t" + "diffTime=" + diffTime);
				if( diffTime <= 0 ) {
					//Just copy file
					common.pln( "copy file " + inputFile + " to " + outputFile );
					common.copyFile(inputFile, outputFile);
				} else {
					common.pln( "addPreSilenceToAudioInSec file " + outputFile + ":" + ((double)diffTime/1000.0) );
					common.addPreSilenceToAudioInSec(FFMPEGPath, inputFile, outputFile, ((double)diffTime/1000.0));				
				}
				totalTimeNow = totalTimeNow + common.getWavDuration( outputFile );
				
				// C_Append_End
				common.pln("Do Append End silence audio");
				inputFile = OutputDir + "/C0_" + String.format("%06d", i) + ".wav";
				outputFile = OutputDir + "/C_" + String.format("%06d", i) + ".wav";
				preTime = totalTimeNow;
				targetTime = SubtitleOne.calculateTotalLength(ZERO_TIME_IN_STRING, so.endTime);
				diffTime = targetTime - preTime;
				common.pln("preTime=" + totalTimeNow + "\t" + "targetTime=" + targetTime + "\t" + "diffTime=" + diffTime);
				if( diffTime <= 0 ) {
					//Just copy file
					common.pln( "copy file " + inputFile + " to " + outputFile );
					common.copyFile(inputFile, outputFile);
					//totalTimeNow no change
				} else {
					common.pln( "addPreSilenceToAudioInSec file " + outputFile + ":" + ((double)diffTime/1000.0) );
					common.addEndSilenceToAudioInSec(FFMPEGPath, inputFile, outputFile, ((double)diffTime/1000.0));
					totalTimeNow = totalTimeNow + diffTime;
				}
				
				_UI.setJFTitle( "Processing : (2/4) [" + (i+1) + "/" + subtitles.size() + "]" );
			}

			// D_Combine All to one
			// D.1 Make File List
			_UI.setJFTitle( "Processing : (3/4)" );
			BufferedWriter bw = new BufferedWriter(new FileWriter(OutputDir + "/C_list.txt"));
			for (int i = 0; i < subtitles.size(); i++) {
				String strTmp = "file " + "'file:" + OutputDir + "/C_" + String.format("%06d", i) + ".wav" + "'";
				strTmp = strTmp.replaceAll("\\\\", "/");
				bw.write(strTmp);

				if (i != subtitles.size() - 1) {
					bw.newLine();
				}
				
				_UI.setJFTitle( "Processing : (3/4) [" + (i+1) + "/" + subtitles.size() + "]" );
			}
			bw.flush();
			bw.close();
			// D.2 ffmpeg to combine audio
			_UI.setJFTitle( "Processing : (4/4)" );
			common.mergeWavFilesUsingFFmpeg(FFMPEGPath, OutputDir + "/C_list.txt", OutputDir + "/D.wav");
			
			_UI.setJFTitle( "Process Fin" );
	
		} catch (Exception e) {
			e.printStackTrace();
		}

		common.pln("Process fin");
	}
}
