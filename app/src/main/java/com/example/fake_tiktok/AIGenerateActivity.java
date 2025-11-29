package com.example.fake_tiktok;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * AI文本生成页面
 */
public class AIGenerateActivity extends AppCompatActivity {
    
    private static final String EXTRA_IMAGE_URIS = "image_uris";
    public static final String RESULT_TITLE = "result_title";
    public static final String RESULT_CONTENT = "result_content";
    
    private EditText editTextAIGenerate;
    private TextView textViewAITitle;
    private Button buttonApply;
    private Button buttonCancel;
    
    private List<Uri> imageUris;
    private String generatedTitle = "";
    private String generatedContent = "";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.util.Log.d("AIGenerateActivity", "onCreate开始");
        
        setContentView(R.layout.activity_ai_generate);
        android.util.Log.d("AIGenerateActivity", "布局加载完成");
        
        // 获取传递的图片URI列表
        Intent intent = getIntent();
        if (intent == null) {
            android.util.Log.e("AIGenerateActivity", "Intent为null");
            Toast.makeText(this, "Intent为null", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        imageUris = intent.getParcelableArrayListExtra(EXTRA_IMAGE_URIS);
        android.util.Log.d("AIGenerateActivity", "获取图片URI列表，数量: " + (imageUris != null ? imageUris.size() : 0));
        
        if (imageUris == null || imageUris.isEmpty()) {
            android.util.Log.w("AIGenerateActivity", "没有选择图片");
            Toast.makeText(this, "没有选择图片", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        initViews();
        setupListeners();
        
        // 开始生成文本
        startGeneration();
    }
    
    private void initViews() {
        editTextAIGenerate = findViewById(R.id.editTextAIGenerate);
        textViewAITitle = findViewById(R.id.textViewAITitle);
        buttonApply = findViewById(R.id.buttonApplyGenerate);
        buttonCancel = findViewById(R.id.buttonCancelGenerate);
        
        // 初始状态禁用应用按钮
        if (buttonApply != null) {
            buttonApply.setEnabled(false);
        }
    }
    
    private void setupListeners() {
        // 应用按钮
        if (buttonApply != null) {
            buttonApply.setOnClickListener(v -> {
                if (!generatedTitle.isEmpty() || !generatedContent.isEmpty()) {
                    // 返回结果
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra(RESULT_TITLE, generatedTitle);
                    resultIntent.putExtra(RESULT_CONTENT, generatedContent);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                } else {
                    Toast.makeText(this, "还没有生成内容", Toast.LENGTH_SHORT).show();
                }
            });
        }
        
        // 取消按钮
        if (buttonCancel != null) {
            buttonCancel.setOnClickListener(v -> {
                setResult(RESULT_CANCELED);
                finish();
            });
        }
    }
    
    private void startGeneration() {
        // 更新状态
        if (textViewAITitle != null) {
            textViewAITitle.setText("AI正在生成文本...");
        }
        if (editTextAIGenerate != null) {
            editTextAIGenerate.setText("");
        }
        
        // 调用AI生成
        QwenActivity.generateTextFromImages(this, imageUris, new QwenActivity.TextGenerationCallback() {
            @Override
            public void onTextChunk(String text, boolean isTitle) {
                // 在主线程更新UI
                runOnUiThread(() -> {
                    if (editTextAIGenerate != null) {
                        String currentText = editTextAIGenerate.getText().toString();
                        editTextAIGenerate.setText(currentText + text);
                        // 自动滚动到底部
                        editTextAIGenerate.setSelection(editTextAIGenerate.getText().length());
                    }
                });
            }
            
            @Override
            public void onComplete(String title, String content) {
                // 在主线程更新UI
                runOnUiThread(() -> {
                    generatedTitle = title;
                    generatedContent = content;
                    
                    // 更新文本框，显示最终结果
                    if (editTextAIGenerate != null) {
                        String fullText = "标题：\n" + title + "\n\n正文：\n" + content;
                        editTextAIGenerate.setText(fullText);
                        editTextAIGenerate.setSelection(editTextAIGenerate.getText().length());
                    }
                    
                    // 更新标题TextView
                    if (textViewAITitle != null) {
                        textViewAITitle.setText("AI生成完成");
                    }
                    
                    // 启用应用按钮
                    if (buttonApply != null) {
                        buttonApply.setEnabled(true);
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                // 在主线程显示错误
                runOnUiThread(() -> {
                    if (editTextAIGenerate != null) {
                        editTextAIGenerate.setText("生成失败: " + error);
                    }
                    
                    if (textViewAITitle != null) {
                        textViewAITitle.setText("生成失败");
                    }
                    
                    Toast.makeText(AIGenerateActivity.this, "生成失败: " + error, Toast.LENGTH_LONG).show();
                    android.util.Log.e("AIGenerateActivity", "AI生成失败: " + error);
                });
            }
        });
    }
    
    /**
     * 创建启动Intent
     */
    public static Intent createIntent(android.content.Context context, List<Uri> imageUris) {
        Intent intent = new Intent(context, AIGenerateActivity.class);
        intent.putParcelableArrayListExtra(EXTRA_IMAGE_URIS, new ArrayList<>(imageUris));
        return intent;
    }
}

