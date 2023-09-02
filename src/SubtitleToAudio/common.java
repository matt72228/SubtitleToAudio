package SubtitleToAudio;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;

public class common {

	public static void pln(String str) {
		System.out.println(str);
	}

	public static void p(String str) {
		System.out.print(str);
	}

	public static void sleep(long d) {
		try {
			Thread.sleep(d);
		} catch (Exception e) {
		}
	}

	public static Dimension getScreenSize() {
		try {
			return Toolkit.getDefaultToolkit().getScreenSize();
		} catch (Exception e) {
		}

		return null;
	}

	public static long getWavDuration(String wavFilePath) throws Exception {
        File audioFile = new File(wavFilePath);
        AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(audioFile);

        // 獲取音訊長度（毫秒）
        long durationMillis = (long) ((fileFormat.getFrameLength() / (double) fileFormat.getFormat().getFrameRate()) * 1000);

        return durationMillis;
    }
	
	public static int executeCommand(String[] command) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();

            // 等待 FFmpeg 執行完畢
            int exitCode = process.waitFor();
            return exitCode;
        } catch (Exception e) {
            e.printStackTrace();
            return -1; // 表示執行失敗
        }
    }
	
	public static void addPreSilenceToAudioInSec(String ffmpeg, String inputPath, String outputPath, double silenceDurationInSeconds) throws Exception, InterruptedException {
        int silenceDurationInMillis = (int) (silenceDurationInSeconds * 1000);

        /*
        String ffmpegCommand = ffmpeg + " -i " + inputPath +
                " -filter_complex \"[0:a]aevalsrc=0:d=" + silenceDurationInMillis +
                "[s];[s][0:a]concat=n=2:v=0:a=1[aout]\" -map \"[aout]\" " + outputPath;
                */

        String ffmpegCommand = ffmpeg + " -i " + inputPath + " -af " + "\"adelay=" + silenceDurationInMillis + "|" + silenceDurationInMillis +"\" " + outputPath;
        
        pln( "ffmpegCommand=" + ffmpegCommand );
        
        Process process = Runtime.getRuntime().exec(ffmpegCommand);
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("音訊處理失敗，退出碼：" + exitCode);
        }
    }
	
	public static void addEndSilenceToAudioInSec(String ffmpeg, String inputPath, String outputPath, double silenceDurationInSeconds) throws Exception, InterruptedException {
        //int silenceDurationInMillis = (int) (silenceDurationInSeconds * 1000);

        /*
        String ffmpegCommand = ffmpeg + " -i " + inputPath +
                " -filter_complex \"[0:a]aevalsrc=0:d=" + silenceDurationInMillis +
                "[s];[s][0:a]concat=n=2:v=0:a=1[aout]\" -map \"[aout]\" " + outputPath;
                */

        String ffmpegCommand = ffmpeg + " -i " + inputPath + " -af " + "\"apad=pad_dur=" + silenceDurationInSeconds + "\" " + outputPath;
        
        pln( "ffmpegCommand=" + ffmpegCommand );
        
        Process process = Runtime.getRuntime().exec(ffmpegCommand);
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("音訊處理失敗，退出碼：" + exitCode);
        }
    }
	
	public static void mergeWavFilesUsingFFmpeg(String ffmpeg, String inputFilePath, String outputFilePath) throws Exception {
        String ffmpegCommand = ffmpeg + " -f concat -safe 0 -i " + inputFilePath + " " + outputFilePath;

        pln( "ffmpegCommand=" + ffmpegCommand );
        
        Process process = Runtime.getRuntime().exec(ffmpegCommand);
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("音訊檔案合併失敗，退出碼：" + exitCode);
        }
    }
	
	public static void copyFile(String sourcePath, String destinationPath) throws Exception {
        File sourceFile = new File(sourcePath);
        File destinationFile = new File(destinationPath);

        // 使用 Files.copy 方法進行複製
        Files.copy(sourceFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
}
