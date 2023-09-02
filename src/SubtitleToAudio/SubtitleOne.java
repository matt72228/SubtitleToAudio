package SubtitleToAudio;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class SubtitleOne {
	public String startTime = null;
	public String endTime = null;
	public long durationTime = 0;
	public String subtitle;
	public long lstartTime = 0;
	public long lendTime = 0;

	public SubtitleOne(String startTime, String endTime, long durationTime, String subtitle, long lstartTime,
			long lendTime) {
		this.startTime = startTime;
		this.endTime = endTime;
		this.durationTime = durationTime;
		this.subtitle = subtitle;
		this.lstartTime = lstartTime;
		this.lendTime = lendTime;
	}

	public static void show(ArrayList<SubtitleOne> subtitles) {
		try {
			if (subtitles == null || subtitles.size() <= 0) {
				common.pln("subtitle is null, no show");
				return;
			}

			for (int i = 0; i < subtitles.size(); i++) {
				SubtitleOne so = subtitles.get(i);
				common.pln(i + "\t" + so.startTime + "(" + so.lstartTime + ")\t" + so.endTime + "(" + so.lendTime
						+ ")\t" + so.durationTime + "\t\t" + so.subtitle);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static ArrayList<SubtitleOne> readSubtitles(File f) {
		ArrayList<SubtitleOne> subtitles = new ArrayList<SubtitleOne>();

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f.getAbsolutePath()), "UTF-8"));

			String line = null;
			String startTime = null;
			String endTime = null;
			String subtitle = null;

			if (f.getAbsolutePath().endsWith("srt")) {
				common.pln("Try to load srt file : " + f.getAbsolutePath());

				while ((line = reader.readLine()) != null) {
					common.pln( "line=" + line );
					
					if (line == null || line.length() <= 0) {
						// by pass
					} else if (line.contains("-->")) {
						try {
							// Do time select
							String tparts[] = line.split(" --> ");
							long lstartTime = convertTimeToMillis(tparts[0]);
							long lendTime = convertTimeToMillis(tparts[1]);
							long totalLength = calculateTotalLength(lstartTime, lendTime);

							// and do subtitle get
							subtitle = reader.readLine();

							// add to ArrayList
							subtitles.add(
									new SubtitleOne(tparts[0], tparts[1], totalLength, subtitle, lstartTime, lendTime));
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}
			} else if (f.getAbsolutePath().endsWith("vtt")) {
				common.pln("Try to load vtt file : " + f.getAbsolutePath());

				while ((line = reader.readLine()) != null) {
					if (line == null || line.length() <= 0) {
						// by pass
					} else if (line.contains("-->")) {
						try {
							// Do time select
							String tparts[] = line.split(" --> ");
							long lstartTime = convertTimeToMillis(tparts[0]);
							long lendTime = convertTimeToMillis(tparts[1]);
							long totalLength = calculateTotalLength(lstartTime, lendTime);

							// and do subtitle get
							subtitle = reader.readLine();

							// add to ArrayList
							subtitles.add(
									new SubtitleOne(tparts[0], tparts[1], totalLength, subtitle, lstartTime, lendTime));
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}
			} else {
				common.pln("Nothing was loaded");
			}
			
			reader.close();

			return subtitles;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/*
	 * public static ArrayList<SubtitleOne> readSubtitles(File f) {
	 * ArrayList<String> subtitles = new ArrayList<String>();
	 * 
	 * try { BufferedReader reader = new BufferedReader(new FileReader(f));
	 * 
	 * StringBuilder subtitleContent = new StringBuilder(); String line; while
	 * ((line = reader.readLine()) != null) {
	 * subtitleContent.append(line).append("\n"); }
	 * subtitles.add(subtitleContent.toString()); } catch (Exception e) {
	 * e.printStackTrace(); }
	 * 
	 * return null; }
	 */

	public static long calculateTotalLength(String startTime, String endTime) {
		long startTimeMillis = convertTimeToMillis(startTime);
		long endTimeMillis = convertTimeToMillis(endTime);
		return endTimeMillis - startTimeMillis;
	}

	public static long calculateTotalLength(long startTimeMillis, long endTimeMillis) {
		return endTimeMillis - startTimeMillis;
	}

	public static long convertTimeToMillis(String time) {
		try {
			String[] parts = time.split("[:,]");
			int hours = Integer.parseInt(parts[0]);
			int minutes = Integer.parseInt(parts[1]);
			int seconds = Integer.parseInt(parts[2]);
			int milliseconds = Integer.parseInt(parts[3]);

			long totalMillis = hours * 3600000L + minutes * 60000L + seconds * 1000L + milliseconds;
			return totalMillis;
		} catch (Exception e) {
			String[] parts = time.split("[:.]");
			int hours = Integer.parseInt(parts[0]);
			int minutes = Integer.parseInt(parts[1]);
			int seconds = Integer.parseInt(parts[2]);
			int milliseconds = Integer.parseInt(parts[3]);

			long totalMillis = hours * 3600000L + minutes * 60000L + seconds * 1000L + milliseconds;
			return totalMillis;
		}
	}
}
