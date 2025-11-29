package com.example.fake_tiktok;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 文件日志工具类，将日志保存到文件
 */
public class FileLogger {
    
    private static final String LOG_DIR_NAME = "logs";
    private static final String LOG_FILE_NAME = "app_log.txt";
    private static final int MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final int MAX_BACKUP_FILES = 5; // 最多保留5个备份文件
    
    private static FileLogger instance;
    private File logFile;
    private SimpleDateFormat dateFormat;
    
    private FileLogger(Context context) {
        try {
            // 日志文件保存在应用的私有目录
            File logDir = new File(context.getFilesDir(), LOG_DIR_NAME);
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
            
            logFile = new File(logDir, LOG_FILE_NAME);
            dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
            
            // 检查文件大小，如果太大则备份
            checkAndRotateLogFile();
            
            // 写入启动日志
            writeToFile("INFO", "FileLogger", "日志系统初始化，日志文件: " + logFile.getAbsolutePath());
        } catch (Exception e) {
            Log.e("FileLogger", "初始化日志文件失败", e);
        }
    }
    
    /**
     * 获取单例实例
     */
    public static synchronized FileLogger getInstance(Context context) {
        if (instance == null) {
            instance = new FileLogger(context);
        }
        return instance;
    }
    
    /**
     * 检查并轮转日志文件（如果文件太大）
     */
    private void checkAndRotateLogFile() {
        if (logFile != null && logFile.exists() && logFile.length() > MAX_FILE_SIZE) {
            try {
                // 备份当前日志文件
                String backupName = LOG_FILE_NAME + "." + System.currentTimeMillis();
                File backupFile = new File(logFile.getParent(), backupName);
                logFile.renameTo(backupFile);
                
                // 删除旧的备份文件
                File logDir = logFile.getParentFile();
                File[] backupFiles = logDir.listFiles((dir, name) -> 
                    name.startsWith(LOG_FILE_NAME + ".") && name.endsWith(".txt"));
                
                if (backupFiles != null && backupFiles.length > MAX_BACKUP_FILES) {
                    // 按修改时间排序，删除最旧的
                    java.util.Arrays.sort(backupFiles, (f1, f2) -> 
                        Long.compare(f1.lastModified(), f2.lastModified()));
                    
                    for (int i = 0; i < backupFiles.length - MAX_BACKUP_FILES; i++) {
                        backupFiles[i].delete();
                    }
                }
                
                // 创建新的日志文件
                logFile.createNewFile();
                writeToFile("INFO", "FileLogger", "日志文件已轮转，旧文件: " + backupName);
            } catch (Exception e) {
                Log.e("FileLogger", "轮转日志文件失败", e);
            }
        }
    }
    
    /**
     * 写入日志到文件
     */
    private synchronized void writeToFile(String level, String tag, String message) {
        if (logFile == null) {
            return;
        }
        
        try {
            FileWriter writer = new FileWriter(logFile, true);
            PrintWriter printWriter = new PrintWriter(writer);
            
            String timestamp = dateFormat.format(new Date());
            String logEntry = String.format("%s [%s] %s: %s%n", 
                timestamp, level, tag, message);
            
            printWriter.print(logEntry);
            printWriter.flush();
            printWriter.close();
        } catch (IOException e) {
            Log.e("FileLogger", "写入日志文件失败", e);
        }
    }
    
    /**
     * 写入异常信息到文件
     */
    private synchronized void writeExceptionToFile(String level, String tag, String message, Throwable throwable) {
        if (logFile == null) {
            return;
        }
        
        try {
            FileWriter writer = new FileWriter(logFile, true);
            PrintWriter printWriter = new PrintWriter(writer);
            
            String timestamp = dateFormat.format(new Date());
            String logEntry = String.format("%s [%s] %s: %s%n", 
                timestamp, level, tag, message);
            
            printWriter.print(logEntry);
            
            if (throwable != null) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                throwable.printStackTrace(pw);
                printWriter.print(sw.toString());
            }
            
            printWriter.flush();
            printWriter.close();
        } catch (IOException e) {
            Log.e("FileLogger", "写入异常日志失败", e);
        }
    }
    
    /**
     * DEBUG级别日志
     */
    public void d(String tag, String message) {
        Log.d(tag, message);
        writeToFile("DEBUG", tag, message);
    }
    
    /**
     * INFO级别日志
     */
    public void i(String tag, String message) {
        Log.i(tag, message);
        writeToFile("INFO", tag, message);
    }
    
    /**
     * WARN级别日志
     */
    public void w(String tag, String message) {
        Log.w(tag, message);
        writeToFile("WARN", tag, message);
    }
    
    /**
     * ERROR级别日志
     */
    public void e(String tag, String message) {
        Log.e(tag, message);
        writeToFile("ERROR", tag, message);
    }
    
    /**
     * ERROR级别日志（带异常）
     */
    public void e(String tag, String message, Throwable throwable) {
        Log.e(tag, message, throwable);
        writeExceptionToFile("ERROR", tag, message, throwable);
    }
    
    /**
     * 获取日志文件路径
     */
    public String getLogFilePath() {
        return logFile != null ? logFile.getAbsolutePath() : null;
    }
    
    /**
     * 获取日志文件
     */
    public File getLogFile() {
        return logFile;
    }
    
    /**
     * 清除日志文件
     */
    public void clearLog() {
        if (logFile != null && logFile.exists()) {
            try {
                FileWriter writer = new FileWriter(logFile, false);
                writer.write("");
                writer.close();
                writeToFile("INFO", "FileLogger", "日志文件已清除");
            } catch (IOException e) {
                Log.e("FileLogger", "清除日志文件失败", e);
            }
        }
    }
    
    /**
     * 读取日志文件内容（最后N行）
     */
    public String readLog(int lastLines) {
        if (logFile == null || !logFile.exists()) {
            return "日志文件不存在";
        }
        
        try {
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.FileReader(logFile));
            
            java.util.List<String> lines = new java.util.ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            reader.close();
            
            // 返回最后N行
            int start = Math.max(0, lines.size() - lastLines);
            StringBuilder sb = new StringBuilder();
            for (int i = start; i < lines.size(); i++) {
                sb.append(lines.get(i)).append("\n");
            }
            
            return sb.toString();
        } catch (IOException e) {
            return "读取日志文件失败: " + e.getMessage();
        }
    }
}

