package com.example.attendanceweb;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

import java.nio.file.Files;
import java.nio.file.Paths;

@Controller
public class AttendanceController {

	@GetMapping("/")
	public String index(Model model) {
		
		String status = getStatus();
		
		model.addAttribute("status", getStatus());
		model.addAttribute("todayWorkTime", getTodayWorkTime());
		model.addAttribute("isWorking", status.equals("出勤"));
		
		return "index";
	}
	@GetMapping("/start")
	public String start(Model model) {
		
		saveAttendance();
		
		model.addAttribute("message", "出勤ボタンが押されました");
		model.addAttribute("history", loadHistory());
		
		return "start";
	}
	@GetMapping("/end")
	public String end(Model model) {
		
		saveEndAttendance();
		
		model.addAttribute("message", "退勤しました");
		model.addAttribute("history", loadHistory());
		
		return "start";
	}
	@GetMapping("/clear")
	public String clear(Model model) {
		clearHistory();
		
		return "redirect:/";
	}
	@GetMapping("/daily")
	public String daily(Model model) {
		
		model.addAttribute("dailyResult", calculateDailyWorkTime());
		
		return "daily";
	}
	@GetMapping("/history")
	public String history(Model model) {
		
		model.addAttribute("historyList", loadHistoryList());
		
		return "history";
	}
	@GetMapping("/edit")
	public String edit(int index, Model model) {
		
		java.util.List<String> historyList = loadHistoryList();
		
		model.addAttribute("index", index);
		model.addAttribute("line", historyList.get(index));
		
		return "edit";
	}
	@GetMapping("/update")
	public String update(int index,
			             String workDate,
			             String hours,
			             String minutes,
			             String type) {
		try {
			String line =workDate.replace("-", "/") +" "
					     + String.format("%02d", Integer.parseInt(hours)) + ":"
					     + String.format("%02d", Integer.parseInt(minutes)) + " " + type; 
			java.util.List<String> historyList = loadHistoryList();
			historyList.set(index, line);
			Files.write(
				Paths.get("C:/Users/shish/eclipse-workspace/WebSample/attendance.txt"),
				historyList);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "redirect:/history";
	}
	@GetMapping("/delete")
	public String delete(int index) {
		try {
			java.util.List<String> historyList = loadHistoryList();
			
			historyList.remove(index);
			historyList.remove(index);
			historyList.remove(index);
			
			Files.write(
				Paths.get("C:/Users/shish/eclipse-workspace/WebSample/attendance.txt"),
				historyList);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "redirect:/history";
	}
	
	private String loadHistory() {
		try {
			return Files.readString(
				Paths.get("C:/Users/shish/eclipse-workspace/WebSample/attendance.txt"));
		} catch (Exception e) {
			return "履歴なし";
		}
	}
	private void saveAttendance() {
		try {
			String now = java.time.LocalDateTime.now()
					.format(java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"));
			String line = now + " 出勤\n";
			Files.writeString(
					Paths.get("C:/Users/shish/eclipse-workspace/WebSample/attendance.txt"),
					line,
					java.nio.file.StandardOpenOption.CREATE,
					java.nio.file.StandardOpenOption.APPEND);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void saveEndAttendance() {
		try {
			String now = java.time.LocalDateTime.now()
					.format(java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"));
			String line = now + " 退勤\n" + "------------------------------------------\n";
			Files.writeString(
					Paths.get("C:/Users/shish/eclipse-workspace/WebSample/attendance.txt"),
					line,
					java.nio.file.StandardOpenOption.CREATE,
					java.nio.file.StandardOpenOption.APPEND);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private String getStatus() {
		try {
			String history = loadHistory();
			if (history.trim().endsWith("出勤")) {
				return "出勤";
			}
		} catch (Exception e) {
		}
	    return "退勤済";
	}
	private String getTodayWorkTime() {
		try {
			String history = loadHistory();
			String[] lines = history.split("\n");
			int totalMinutes = 0;
			for (int i = 0; i < lines.length -1; i++)
			  if (lines[i].endsWith("出勤")
				  	&& lines[i + 1].endsWith("退勤")) {
				  String startTime = lines[i].substring(11, 16);
				  String endTime = lines[i + 1].substring(11, 16);
				  java.time.LocalTime start = java.time.LocalTime.parse(startTime);
				  java.time.LocalTime end = java.time.LocalTime.parse(endTime);
				  totalMinutes += java.time.Duration.between(start, end).toMinutes();
			  }
		   long hours = totalMinutes / 60;
		   long minutes = totalMinutes % 60;
		   return hours + "時間" + minutes + "分";
	   } catch (Exception e) {
		   e.printStackTrace();
	  	   return "0時間0分";
	   }
	}
	private void clearHistory() {
		try {
			Files.writeString(
				  Paths.get("C:/Users/shish/eclipse-workspace/WebSample/attendance.txt"),
				  "");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private String calculateDailyWorkTime() {
		try {
			String history = loadHistory();
			String[] lines = history.split("\n");
			for (String line : lines) {
				System.out.println("[" + line + "]");
			}
			java.util.Map<String, Long> dailyWorkTime = new java.util.HashMap<>();
			String currentDate = "";
			for (int i = 0; i < lines.length - 1; i++) {
				
			String currentLine = lines[i].trim();
			String nextLine = lines[i + 1].trim();
				
				if (currentLine.endsWith("出勤")
						&& nextLine.endsWith("退勤")) {
					
					currentDate = lines[i].substring(0, 10);
					
					String startTime = lines[i].substring(11, 16);
					String endTime = lines[i + 1].substring(11, 16);
					
					java.time.LocalTime start = java.time.LocalTime.parse(startTime);
					java.time.LocalTime end = java.time.LocalTime.parse(endTime);
					long minutes = java.time.Duration.between(start, end).toMinutes();
					
					System.out.println(currentDate + " = " + minutes);
					
					dailyWorkTime.put(currentDate, dailyWorkTime.getOrDefault(currentDate, 0L) + minutes);
				}
			}
			String result = "";
			for (String date : dailyWorkTime.keySet()) {
				long totalMinutes = dailyWorkTime.get(date);
				long hours = totalMinutes / 60;
				long minutes = totalMinutes % 60;
				
				result += date + " : " + hours + "時間" + minutes + "分\n";
			}
			
			System.out.println("result = " + result);
			
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return "集計失敗";
		}
	}
	private java.util.List<String> loadHistoryList() {
		try {
			return Files.readAllLines(
				Paths.get("C:/Users/shish/eclipse-workspace/WebSample/attendance.txt"));
		} catch (Exception e) {
			return new java.util.ArrayList<>();
		}
	}
}

