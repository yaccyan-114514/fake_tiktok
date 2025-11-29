package com.example.fake_tiktok;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Scanner;

/**
 * Qwen AI文本生成辅助类
 * 使用Qwen qwen3-vl-flash模型根据图片生成标题和正文
 */
public class QwenActivity {
    
    private static final String TAG = "QwenActivity";
    private static final String API_BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
    private static final String API_KEY = "sk-f0fef76e5fb54b77a4642a3cd5c06d40";
    
    /**
     * 生成文本的回调接口
     */
    public interface TextGenerationCallback {
        /**
         * 接收到新的文本片段（流式输出）
         * @param text 新的文本片段
         * @param isTitle 是否为标题（true=标题，false=正文）
         */
        void onTextChunk(String text, boolean isTitle);
        
        /**
         * 生成完成
         * @param title 生成的标题
         * @param content 生成的正文
         */
        void onComplete(String title, String content);
        
        /**
         * 生成失败
         * @param error 错误信息
         */
        void onError(String error);
    }
    
    /**
     * 根据图片生成标题和正文
     * @param context 上下文
     * @param imageUris 图片URI列表
     * @param callback 回调接口
     */
    public static void generateTextFromImages(Context context, List<Uri> imageUris, TextGenerationCallback callback) {
        if (imageUris == null || imageUris.isEmpty()) {
            callback.onError("没有选择图片");
            return;
        }
        
        // 在后台线程执行
        new Thread(() -> {
            try {
                // 将图片转换为base64
                List<String> base64Images = convertImagesToBase64(context, imageUris);
                if (base64Images.isEmpty()) {
                    callback.onError("无法读取图片");
                    return;
                }
                
                // 调用API生成文本
                callQwenAPI(base64Images, callback);
                
            } catch (Exception e) {
                Log.e(TAG, "生成文本失败", e);
                callback.onError("生成失败: " + e.getMessage());
            }
        }).start();
    }
    
    /**
     * 将图片URI列表转换为base64编码列表
     */
    private static List<String> convertImagesToBase64(Context context, List<Uri> imageUris) {
        List<String> base64Images = new java.util.ArrayList<>();
        
        for (Uri imageUri : imageUris) {
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
                if (inputStream != null) {
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    inputStream.close();
                    
                    if (bitmap != null) {
                        // 压缩图片以减小大小（最大宽度1000px）
                        int maxWidth = 1000;
                        if (bitmap.getWidth() > maxWidth) {
                            int height = (int) (bitmap.getHeight() * ((float) maxWidth / bitmap.getWidth()));
                            bitmap = Bitmap.createScaledBitmap(bitmap, maxWidth, height, true);
                        }
                        
                        // 转换为base64
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream);
                        byte[] imageBytes = outputStream.toByteArray();
                        String base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP);
                        base64Images.add(base64Image);
                        
                        bitmap.recycle();
                        Log.d(TAG, "成功转换图片为base64，大小: " + base64Image.length());
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "转换图片失败: " + imageUri, e);
            }
        }
        
        return base64Images;
    }
    
    /**
     * 调用Qwen API生成文本
     */
    private static void callQwenAPI(List<String> base64Images, TextGenerationCallback callback) {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        
        try {
            URL url = new URL(API_BASE_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(60000);
            
            // 构建请求体
            JSONObject requestBody = buildRequestBody(base64Images);
            String requestBodyString = requestBody.toString();
            
            Log.d(TAG, "发送Qwen API请求，图片数量: " + base64Images.size());
            
            // 发送请求
            connection.getOutputStream().write(requestBodyString.getBytes("UTF-8"));
            connection.getOutputStream().flush();
            connection.getOutputStream().close();
            
            // 读取响应
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = connection.getInputStream();
                Scanner scanner = new Scanner(inputStream, "UTF-8");
                
                StringBuilder fullTextBuilder = new StringBuilder();
                
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine().trim();
                    if (line.startsWith("data: ")) {
                        String data = line.substring(6);
                        if (data.equals("[DONE]")) {
                            break;
                        }
                        
                        try {
                            JSONObject jsonObject = new JSONObject(data);
                            JSONArray choices = jsonObject.getJSONArray("choices");
                            if (choices.length() > 0) {
                                JSONObject choice = choices.getJSONObject(0);
                                JSONObject delta = choice.optJSONObject("delta");
                                
                                if (delta != null) {
                                    String content = delta.optString("content", "");
                                    if (!content.isEmpty()) {
                                        fullTextBuilder.append(content);
                                        // 实时流式输出
                                        final String currentText = fullTextBuilder.toString();
                                        mainHandler.post(() -> callback.onTextChunk(content, false));
                                    }
                                    
                                    // 检查是否完成
                                    String finishReason = choice.optString("finish_reason", "");
                                    if ("stop".equals(finishReason)) {
                                        break;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "解析响应失败: " + data, e);
                        }
                    }
                }
                
                scanner.close();
                inputStream.close();
                
                // 解析最终结果
                String fullText = fullTextBuilder.toString().trim();
                String finalTitle = "";
                String finalContent = "";
                
                // 按双换行符分割标题和正文
                String[] parts = fullText.split("\n\n", 2);
                if (parts.length >= 2) {
                    finalTitle = parts[0].trim();
                    finalContent = parts[1].trim();
                } else {
                    // 如果没有双换行符，尝试按单换行符分割
                    String[] singleParts = fullText.split("\n", 2);
                    if (singleParts.length >= 2) {
                        finalTitle = singleParts[0].trim();
                        finalContent = singleParts[1].trim();
                    } else {
                        // 如果都没有，全部作为标题，正文为空
                        finalTitle = fullText;
                        finalContent = "";
                    }
                }
                
                // 限制长度
                if (finalTitle.length() > 100) {
                    finalTitle = finalTitle.substring(0, 100);
                }
                if (finalContent.length() > 100) {
                    finalContent = finalContent.substring(0, 100);
                }
                
                final String title = finalTitle;
                final String content = finalContent;
                mainHandler.post(() -> callback.onComplete(title, content));
                
            } else {
                // 读取错误响应
                InputStream errorStream = connection.getErrorStream();
                String finalErrorMessage;
                if (errorStream != null) {
                    Scanner errorScanner = new Scanner(errorStream, "UTF-8");
                    StringBuilder errorBuilder = new StringBuilder();
                    while (errorScanner.hasNextLine()) {
                        errorBuilder.append(errorScanner.nextLine());
                    }
                    finalErrorMessage = errorBuilder.toString();
                    errorScanner.close();
                } else {
                    finalErrorMessage = "API请求失败: " + responseCode;
                }
                Log.e(TAG, finalErrorMessage);
                final String errorMsg = finalErrorMessage;
                mainHandler.post(() -> callback.onError(errorMsg));
            }
            
            connection.disconnect();
            
        } catch (Exception e) {
            Log.e(TAG, "调用Qwen API失败", e);
            mainHandler.post(() -> callback.onError("网络错误: " + e.getMessage()));
        }
    }
    
    /**
     * 构建API请求体
     */
    private static JSONObject buildRequestBody(List<String> base64Images) throws Exception {
        JSONObject requestBody = new JSONObject();
        // 使用Qwen的视觉模型
        requestBody.put("model", "qwen3-vl-flash");
        requestBody.put("stream", true);
        requestBody.put("temperature", 0.5);
        requestBody.put("max_tokens", 300); // 减少token数量以限制生成长度（标题+正文总共约200字）
        
        JSONArray messages = new JSONArray();
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        
        JSONArray contentArray = new JSONArray();
        
        // 添加文本提示
        JSONObject textContent = new JSONObject();
        textContent.put("type", "text");
        textContent.put("text", "请根据这些图片生成一个吸引人的标题（不超过100字）和一篇简短的正文（不超过100字）。\n\n格式要求：\n第一行：标题\n第二行：空行\n第三行开始：正文内容\n\n请直接输出，不要添加任何说明文字。");
        contentArray.put(textContent);
        
        // 添加图片（Qwen支持视觉功能）
        for (String base64Image : base64Images) {
            JSONObject imageContent = new JSONObject();
            imageContent.put("type", "image_url");
            JSONObject imageUrl = new JSONObject();
            imageUrl.put("url", "data:image/jpeg;base64," + base64Image);
            imageContent.put("image_url", imageUrl);
            contentArray.put(imageContent);
        }
        
        userMessage.put("content", contentArray);
        messages.put(userMessage);
        
        requestBody.put("messages", messages);
        
        return requestBody;
    }
}
