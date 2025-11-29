package com.example.fake_tiktok;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;

import android.content.res.ColorStateList;

import com.example.fake_tiktok.DAO.AppDatabase;
import com.example.fake_tiktok.DAO.FriendDao;
import com.example.fake_tiktok.DAO.GroupDao;
import com.example.fake_tiktok.DAO.TagDao;

import java.util.ArrayList;
import java.util.List;

public class PostActivity extends AppCompatActivity {
    
    private static final int REQUEST_CODE_PICK_IMAGES = 1001;
    private static final int REQUEST_CODE_TAKE_PICTURE = 1002;
    private static final int REQUEST_CODE_CAMERA_PERMISSION = 1003;
    private static final int REQUEST_CODE_AI_GENERATE = 1004;
    
    private Uri photoOutputUri; // 保存拍照输出URI
    
    private EditText editTextTitle;
    private EditText editTextContent;
    private TextView textViewTitleCount;
    private TextView textViewContentCount;
    private RecyclerView recyclerViewImages;
    private ImageAdapter imageAdapter;
    private List<Uri> selectedImages;
    private Button buttonAddTag;
    private Button buttonAddMention;
    private Button buttonAIGenerate;
    private TextView textViewLocation;
    private Button buttonGetLocation;
    private Button buttonPost;
    
    private com.google.android.material.chip.ChipGroup chipGroupTags; // 标签显示区域
    private List<String> selectedTags; // 已选择的标签列表
    
    private LocationActivity locationActivity;
    
    private DraftDatabaseHelper dbHelper;
    private AppDatabase appDatabase;
    private long currentDraftId = 0;
    private Draft.Location currentLocation;
    private String currentLocationAddress;
    private com.example.fake_tiktok.DAO.DraftLocationDao.LocationData currentLocationData;
    private boolean isDataChanged = false;
    
    private FileLogger fileLogger; // 文件日志工具
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            // 初始化文件日志
            fileLogger = FileLogger.getInstance(this);
            
            setContentView(R.layout.activity_post);
            
            try {
                dbHelper = new DraftDatabaseHelper(this);
                appDatabase = new AppDatabase(this);
            } catch (Exception e) {
                android.util.Log.e("PostActivity", "数据库初始化失败", e);
                // 数据库初始化失败不应该阻止Activity启动
            }
            
            initViews();
            initLocationServices();
            setupRecyclerView();
            setupListeners();
            
            try {
                loadDraft();
            } catch (Exception e) {
                android.util.Log.e("PostActivity", "加载草稿失败", e);
                // 如果加载草稿失败，继续使用空内容
            }
            
            // 监听标题变化
            if (editTextTitle != null) {
                editTextTitle.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        isDataChanged = true;
                        updateTitleCount(s != null ? s.length() : 0);
                    }
                    
                    @Override
                    public void afterTextChanged(Editable s) {}
                });
                // 初始化标题字数统计
                updateTitleCount(editTextTitle.getText() != null ? editTextTitle.getText().length() : 0);
            }
            
            // 监听内容变化
            if (editTextContent != null) {
                editTextContent.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        isDataChanged = true;
                        updateContentCount(s != null ? s.length() : 0);
                    }
                    
                    @Override
                    public void afterTextChanged(Editable s) {}
                });
                // 初始化内容字数统计
                updateContentCount(editTextContent.getText() != null ? editTextContent.getText().length() : 0);
            }
        } catch (Exception e) {
            android.util.Log.e("PostActivity", "onCreate发生严重错误", e);
            Toast.makeText(this, "页面加载失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
            // 即使出错也尝试显示页面
        }
    }
    
    private void initViews() {
        try {
            editTextTitle = findViewById(R.id.editTextTitle);
            if (editTextTitle == null) {
                android.util.Log.e("PostActivity", "找不到editTextTitle");
            }
            
            textViewTitleCount = findViewById(R.id.textViewTitleCount);
            if (textViewTitleCount == null) {
                android.util.Log.e("PostActivity", "找不到textViewTitleCount");
            }
            
            editTextContent = findViewById(R.id.editTextContent);
            if (editTextContent == null) {
                android.util.Log.e("PostActivity", "找不到editTextContent");
            }
            
            textViewContentCount = findViewById(R.id.textViewContentCount);
            if (textViewContentCount == null) {
                android.util.Log.e("PostActivity", "找不到textViewContentCount");
            }
            
            recyclerViewImages = findViewById(R.id.recyclerViewImages);
            if (recyclerViewImages == null) {
                android.util.Log.e("PostActivity", "找不到recyclerViewImages");
            }
            
            chipGroupTags = findViewById(R.id.chipGroupTags);
            if (chipGroupTags == null) {
                android.util.Log.e("PostActivity", "找不到chipGroupTags");
            }
            
            buttonAddTag = findViewById(R.id.buttonAddTag);
            if (buttonAddTag != null) {
                // 设置标签按钮图标
                android.graphics.drawable.Drawable tagIcon = ContextCompat.getDrawable(this, R.drawable.tag);
                if (tagIcon != null) {
                    // 设置图标颜色为白色
                    tagIcon = tagIcon.mutate();
                    tagIcon.setColorFilter(android.graphics.Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN);
                    int iconSize = (int) (16 * getResources().getDisplayMetrics().density);
                    tagIcon.setBounds(0, 0, iconSize, iconSize);
                    buttonAddTag.setCompoundDrawables(tagIcon, null, null, null);
                    buttonAddTag.setCompoundDrawablePadding((int) (4 * getResources().getDisplayMetrics().density));
                    buttonAddTag.setGravity(android.view.Gravity.CENTER);
                }
            }
            
            buttonAddMention = findViewById(R.id.buttonAddMention);
            if (buttonAddMention != null) {
                // 设置@好友按钮图标
                android.graphics.drawable.Drawable friendIcon = ContextCompat.getDrawable(this, R.drawable.friend);
                if (friendIcon != null) {
                    // 设置图标颜色为白色
                    friendIcon = friendIcon.mutate();
                    friendIcon.setColorFilter(android.graphics.Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN);
                    int iconSize = (int) (16 * getResources().getDisplayMetrics().density);
                    friendIcon.setBounds(0, 0, iconSize, iconSize);
                    buttonAddMention.setCompoundDrawables(friendIcon, null, null, null);
                    buttonAddMention.setCompoundDrawablePadding((int) (4 * getResources().getDisplayMetrics().density));
                    buttonAddMention.setGravity(android.view.Gravity.CENTER);
                }
            }
            
            buttonAIGenerate = findViewById(R.id.buttonAIGenerate);
            if (buttonAIGenerate != null) {
                // 设置一键生成文本按钮图标
                android.graphics.drawable.Drawable icon = ContextCompat.getDrawable(this, R.drawable.text);
                if (icon != null) {
                    // 设置图标颜色为白色
                    icon = icon.mutate();
                    icon.setColorFilter(android.graphics.Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN);
                    int iconSize = (int) (16 * getResources().getDisplayMetrics().density);
                    icon.setBounds(0, 0, iconSize, iconSize);
                    buttonAIGenerate.setCompoundDrawables(icon, null, null, null);
                    buttonAIGenerate.setCompoundDrawablePadding((int) (4 * getResources().getDisplayMetrics().density));
                    buttonAIGenerate.setGravity(android.view.Gravity.CENTER);
                }
            }
            
            textViewLocation = findViewById(R.id.textViewLocation);
            
            buttonGetLocation = findViewById(R.id.buttonGetLocation);
            if (buttonGetLocation != null) {
                // 设置位置按钮图标
                android.graphics.drawable.Drawable locationIcon = ContextCompat.getDrawable(this, R.drawable.location);
                if (locationIcon != null) {
                    // 设置图标颜色为白色
                    locationIcon = locationIcon.mutate();
                    locationIcon.setColorFilter(android.graphics.Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN);
                    int iconSize = (int) (16 * getResources().getDisplayMetrics().density);
                    locationIcon.setBounds(0, 0, iconSize, iconSize);
                    buttonGetLocation.setCompoundDrawables(locationIcon, null, null, null);
                    buttonGetLocation.setCompoundDrawablePadding((int) (4 * getResources().getDisplayMetrics().density));
                    buttonGetLocation.setGravity(android.view.Gravity.CENTER);
                }
            }
            
            buttonPost = findViewById(R.id.buttonPost);
            if (buttonPost != null) {
                // 确保发布按钮使用自定义背景，不受Material主题影响
                buttonPost.setBackgroundResource(R.drawable.button_post_background);
                buttonPost.setTextColor(android.graphics.Color.WHITE);
                buttonPost.setGravity(android.view.Gravity.CENTER);
            }
            
            // 初始化标签列表
            selectedTags = new ArrayList<>();
            selectedImages = new ArrayList<>();
            
            // 设置点击监听器，确保点击时弹出键盘
            setupKeyboardListeners();
        } catch (Exception e) {
            android.util.Log.e("PostActivity", "initViews失败", e);
            throw e; // 重新抛出异常，让调用者知道
        }
    }
    
    /**
     * 设置键盘弹出监听器
     */
    private void setupKeyboardListeners() {
        // 标题文本框
        if (editTextTitle != null) {
            editTextTitle.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    showKeyboard(editTextTitle);
                }
            });
            editTextTitle.setOnClickListener(v -> {
                editTextTitle.requestFocus();
                showKeyboard(editTextTitle);
            });
        }
        
        // 内容文本框
        if (editTextContent != null) {
            editTextContent.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    showKeyboard(editTextContent);
                }
            });
            editTextContent.setOnClickListener(v -> {
                editTextContent.requestFocus();
                showKeyboard(editTextContent);
            });
        }
        
        // 标签输入框
        // 标签按钮和@对象按钮不需要键盘监听
    }
    
    /**
     * 显示虚拟键盘
     */
    private void showKeyboard(EditText editText) {
        if (editText == null) {
            return;
        }
        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
        }
    }
    
    /**
     * 隐藏虚拟键盘
     */
    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }
    
    private void initLocationServices() {
        // 初始化定位服务
        try {
            locationActivity = new LocationActivity(this, new LocationActivity.LocationCallback() {
                @Override
                public void onLocationSuccess(Draft.Location location, String address, 
                        com.example.fake_tiktok.DAO.DraftLocationDao.LocationData locationData) {
                    currentLocation = location;
                    currentLocationAddress = address;
                    currentLocationData = locationData;
                    updateLocationDisplay(address);
                }
                
                @Override
                public void onLocationFailed(String errorInfo) {
                    android.util.Log.e("PostActivity", "定位失败: " + errorInfo);
                    Toast.makeText(PostActivity.this, "定位失败：" + errorInfo, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            android.util.Log.e("PostActivity", "初始化LocationActivity失败", e);
            locationActivity = null;
        }
    }
    
    private void setupRecyclerView() {
        try {
            if (recyclerViewImages == null) {
                return;
            }
            
            if (selectedImages == null) {
                selectedImages = new ArrayList<>();
            }
            
            imageAdapter = new ImageAdapter(selectedImages, new ImageAdapter.OnImageClickListener() {
                @Override
                public void onAddImageClick() {
                    showImageSourceDialog();
                }
                
                @Override
                public void onDeleteImageClick(int position) {
                    if (selectedImages != null && position >= 0 && position < selectedImages.size()) {
                        selectedImages.remove(position);
                        if (imageAdapter != null) {
                            imageAdapter.notifyDataSetChanged();
                        }
                        isDataChanged = true;
                    }
                }
            });
            
            recyclerViewImages.setLayoutManager(new GridLayoutManager(this, 3));
            recyclerViewImages.setAdapter(imageAdapter);
            
            // 启用拖拽排序
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                    ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT,
                    0) {
                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                    int fromPos = viewHolder.getAdapterPosition();
                    int toPos = target.getAdapterPosition();
                    
                    if (selectedImages != null && fromPos < selectedImages.size() && toPos < selectedImages.size()) {
                        Uri temp = selectedImages.get(fromPos);
                        selectedImages.remove(fromPos);
                        selectedImages.add(toPos, temp);
                        if (imageAdapter != null) {
                            imageAdapter.notifyItemMoved(fromPos, toPos);
                        }
                    }
                    return true;
                }
                
                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                    // 不需要滑动删除，使用删除按钮
                }
            });
            itemTouchHelper.attachToRecyclerView(recyclerViewImages);
        } catch (Exception e) {
            android.util.Log.e("PostActivity", "setupRecyclerView失败", e);
        }
    }
    
    private void setupListeners() {
        try {
            // 添加标签按钮
            if (buttonAddTag != null) {
                buttonAddTag.setOnClickListener(v -> showTagSearchDialog());
            }
            
            // 添加@对象按钮
            if (buttonAddMention != null) {
                buttonAddMention.setOnClickListener(v -> showFriendSelectionDialog());
            }
            
            // 一键生成文本按钮
            if (buttonAIGenerate != null) {
                buttonAIGenerate.setOnClickListener(v -> generateAIText());
            }
            
            // 获取位置
            if (buttonGetLocation != null) {
                buttonGetLocation.setOnClickListener(v -> requestLocation());
            }
            
            // 发布按钮
            if (buttonPost != null) {
                buttonPost.setOnClickListener(v -> {
                    if (validatePost()) {
                        postContent();
                    }
                });
            }
        } catch (Exception e) {
            android.util.Log.e("PostActivity", "setupListeners失败", e);
        }
    }
    
    /**
     * 显示图片来源选择对话框
     */
    private void showImageSourceDialog() {
        new AlertDialog.Builder(this)
                .setTitle("选择图片来源")
                .setItems(new String[]{"拍照获取", "从相册选择"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            // 拍照获取
                            takePicture();
                        } else if (which == 1) {
                            // 从相册选择
                            pickImages();
                        }
                    }
                })
                .show();
    }
    
    /**
     * 拍照获取图片
     */
    private void takePicture() {
        // 检查相机权限
        if (!CameraActivity.hasCameraPermission(this)) {
            // 请求相机权限
            CameraActivity.requestCameraPermission(this, REQUEST_CODE_CAMERA_PERMISSION);
            return;
        }
        
        // 创建拍照Intent
        String authority = getPackageName() + ".fileprovider";
        Intent takePictureIntent = CameraActivity.createTakePictureIntent(this, authority);
        
        if (takePictureIntent == null) {
            Toast.makeText(this, "无法启动相机", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 保存输出URI（从Intent的EXTRA_OUTPUT中获取）
        photoOutputUri = takePictureIntent.getParcelableExtra(MediaStore.EXTRA_OUTPUT);
        
        // 启动相机
        startActivityForResult(takePictureIntent, REQUEST_CODE_TAKE_PICTURE);
    }
    
    /**
     * 从相册选择图片（原有功能）
     */
    private void pickImages() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        // 添加标志以支持持久化权限
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(Intent.createChooser(intent, "选择图片"), REQUEST_CODE_PICK_IMAGES);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        // 处理AI生成结果
        if (requestCode == REQUEST_CODE_AI_GENERATE && resultCode == RESULT_OK) {
            if (data != null) {
                String title = data.getStringExtra(AIGenerateActivity.RESULT_TITLE);
                String content = data.getStringExtra(AIGenerateActivity.RESULT_CONTENT);
                
                // 将生成的文本填入标题和正文
                if (editTextTitle != null && title != null && !title.isEmpty()) {
                    editTextTitle.setText(title);
                    updateTitleCount(title.length());
                }
                if (editTextContent != null && content != null && !content.isEmpty()) {
                    editTextContent.setText(content);
                    updateContentCount(content.length());
                }
                
                isDataChanged = true;
                Toast.makeText(this, "已应用生成的文本", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        
        // 处理拍照结果
        if (requestCode == REQUEST_CODE_TAKE_PICTURE && resultCode == RESULT_OK) {
            try {
                Uri photoURI = null;
                
                // 优先使用保存的输出URI
                if (photoOutputUri != null) {
                    photoURI = photoOutputUri;
                } else {
                    // 如果没有保存的URI，尝试从Intent中获取
                    String authority = getPackageName() + ".fileprovider";
                    photoURI = CameraActivity.getPhotoUri(this, data, authority);
                }
                
                if (photoURI != null) {
                    // 获取持久化权限
                    try {
                        getContentResolver().takePersistableUriPermission(
                            photoURI, 
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );
                    } catch (SecurityException e) {
                        android.util.Log.w("PostActivity", "无法获取拍照图片持久化权限", e);
                        // 继续添加图片，即使没有持久化权限
                    }
                    
                    selectedImages.add(photoURI);
                    imageAdapter.notifyDataSetChanged();
                    isDataChanged = true;
                    photoOutputUri = null;
                } else {
                    Toast.makeText(this, "无法获取拍照图片", Toast.LENGTH_SHORT).show();
                    android.util.Log.e("PostActivity", "无法从拍照结果中获取图片URI");
                }
            } catch (Exception e) {
                android.util.Log.e("PostActivity", "处理拍照结果时失败", e);
                Toast.makeText(this, "处理拍照结果失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            return;
        }
        
        // 处理从相册选择图片的结果（原有功能）
        if (requestCode == REQUEST_CODE_PICK_IMAGES && resultCode == RESULT_OK) {
            if (data != null) {
                try {
                    if (data.getClipData() != null) {
                        // 多选图片
                        int count = data.getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            Uri imageUri = data.getClipData().getItemAt(i).getUri();
                            // 获取持久化权限，这样应用重启后也能访问
                            try {
                                getContentResolver().takePersistableUriPermission(
                                    imageUri, 
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                                );
                            } catch (SecurityException e) {
                                android.util.Log.w("PostActivity", "无法获取图片持久化权限", e);
                                // 继续添加图片，即使没有持久化权限
                            }
                            selectedImages.add(imageUri);
                        }
                    } else if (data.getData() != null) {
                        // 单选图片
                        Uri imageUri = data.getData();
                        // 获取持久化权限
                        try {
                            getContentResolver().takePersistableUriPermission(
                                imageUri, 
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                            );
                        } catch (SecurityException e) {
                            android.util.Log.w("PostActivity", "无法获取图片持久化权限", e);
                            // 继续添加图片，即使没有持久化权限
                        }
                        selectedImages.add(imageUri);
                    }
                    imageAdapter.notifyDataSetChanged();
                    isDataChanged = true;
                } catch (Exception e) {
                    android.util.Log.e("PostActivity", "处理图片选择结果时失败", e);
                    Toast.makeText(this, "添加图片失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    
    /**
     * 添加标签（显示为可删除的Chip组件）
     */
    private void addTag(String tag) {
        try {
        if (chipGroupTags == null || selectedTags == null) {
            return;
        }
        
        // 移除#号（如果有），统一处理
        String tagName = tag.startsWith("#") ? tag.substring(1) : tag;
        String tagWithHash = "#" + tagName;
        
        // 检查标签是否已存在
        if (selectedTags.contains(tagWithHash)) {
            Toast.makeText(this, "标签已存在", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 添加到列表
        selectedTags.add(tagWithHash);
        
            // 创建Chip组件（使用Material Components样式）
        Chip chip = new Chip(this);
        chip.setText(tagWithHash);
            chip.setChipBackgroundColor(ColorStateList.valueOf(android.graphics.Color.parseColor("#424242")));
        chip.setTextColor(android.graphics.Color.WHITE);
        chip.setCloseIconVisible(true);
        chip.setCloseIconTint(ColorStateList.valueOf(android.graphics.Color.WHITE));
        
        // 设置删除监听器
        chip.setOnCloseIconClickListener(v -> {
                try {
            // 从列表中移除
                    if (selectedTags != null) {
            selectedTags.remove(tagWithHash);
                    }
            // 从ChipGroup中移除
                    if (chipGroupTags != null) {
            chipGroupTags.removeView(chip);
                    }
            isDataChanged = true;
                } catch (Exception e) {
                    android.util.Log.e("PostActivity", "删除标签时出错", e);
                }
        });
        
        // 添加到ChipGroup
        chipGroupTags.addView(chip);
        isDataChanged = true;
        } catch (Exception e) {
            android.util.Log.e("PostActivity", "添加标签时发生异常", e);
            Toast.makeText(this, "添加标签失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void addMention(String mention) {
        if (editTextContent == null) {
            return;
        }
        String currentText = editTextContent.getText().toString();
        String mentionText = mention.startsWith("@") ? mention : "@" + mention;
        String newText = currentText.isEmpty() ? mentionText : currentText + " " + mentionText;
        editTextContent.setText(newText);
        editTextContent.setSelection(newText.length()); // 将光标移到末尾
        isDataChanged = true;
    }
    
    private void requestLocation() {
        if (locationActivity == null) {
            initLocationServices();
            if (locationActivity == null) {
                Toast.makeText(this, "定位服务未初始化，请重启应用", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        locationActivity.requestLocation();
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        // 处理相机权限请求结果
        if (requestCode == REQUEST_CODE_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限已授予，开始拍照
                takePicture();
            } else {
                // 权限被拒绝
                Toast.makeText(this, "需要相机权限才能拍照", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        
        // 处理定位权限请求结果
        if (locationActivity != null) {
            locationActivity.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    
    /**
     * 更新位置显示
     */
    private void updateLocationDisplay(String address) {
        if (textViewLocation != null) {
            textViewLocation.setText(address);
            textViewLocation.setVisibility(View.VISIBLE);
            isDataChanged = true;
        }
    }
    
    private boolean validatePost() {
        String title = editTextTitle != null ? editTextTitle.getText().toString().trim() : "";
        String content = editTextContent.getText().toString().trim();
        if (title.isEmpty() && content.isEmpty() && selectedImages.isEmpty()) {
            Toast.makeText(this, "请输入标题、内容或添加图片", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    
    private void postContent() {
        // 删除草稿
        if (currentDraftId > 0 && dbHelper != null) {
            try {
                dbHelper.deleteDraft(currentDraftId);
            } catch (Exception e) {
                e.printStackTrace();
            }
            currentDraftId = 0;
        }
        
        // 这里可以添加实际的发布逻辑
        Toast.makeText(this, "发布成功！", Toast.LENGTH_SHORT).show();
        isDataChanged = false;
        finish();
    }
    
    /**
     * 加载草稿
     */
    private void loadDraft() {
        if (dbHelper == null) {
            android.util.Log.e("PostActivity", "dbHelper为null，无法加载草稿");
            return;
        }
        
        try {
            Draft draft = dbHelper.getLatestDraft();
            
            if (draft != null) {
                currentDraftId = draft.getId();
                
                // 加载标题
                try {
                    if (editTextTitle != null && draft.getTitle() != null && !draft.getTitle().isEmpty()) {
                        editTextTitle.setText(draft.getTitle());
                        updateTitleCount(draft.getTitle().length());
                    } else if (editTextTitle != null) {
                        updateTitleCount(0);
                    }
                } catch (Exception e) {
                    android.util.Log.e("PostActivity", "加载标题失败", e);
                }
                
                // 加载文本内容
                try {
                    if (editTextContent != null && draft.getContent() != null && !draft.getContent().isEmpty()) {
                        editTextContent.setText(draft.getContent());
                        updateContentCount(draft.getContent().length());
                    } else {
                        if (editTextContent != null) {
                            updateContentCount(0);
                        }
                    }
                } catch (Exception e) {
                    android.util.Log.e("PostActivity", "加载内容失败", e);
                }
                
                // 加载标签（显示为Chip组件）
                try {
                    if (draft.getTags() != null && !draft.getTags().isEmpty()) {
                        // 清空现有标签
                        if (selectedTags != null) {
                            selectedTags.clear();
                        }
                        if (chipGroupTags != null) {
                            chipGroupTags.removeAllViews();
                        }
                        
                        // 加载标签并显示为Chip
                        for (String tag : draft.getTags()) {
                            if (tag != null && !tag.isEmpty()) {
                                // 添加到列表
                                if (selectedTags != null) {
                                    selectedTags.add(tag);
                                }
                                
                                // 创建Chip组件
                                if (chipGroupTags != null) {
                                    Chip chip = new Chip(this);
                                    chip.setText(tag);
                                    chip.setChipBackgroundColorResource(android.R.color.darker_gray);
                                    chip.setTextColor(android.graphics.Color.WHITE);
                                    chip.setCloseIconVisible(true);
                                    chip.setCloseIconTint(ColorStateList.valueOf(android.graphics.Color.WHITE));
                                    
                                    // 设置删除监听器
                                    final String tagToRemove = tag;
                                    chip.setOnCloseIconClickListener(v -> {
                                        if (selectedTags != null) {
                                            selectedTags.remove(tagToRemove);
                                        }
                                        chipGroupTags.removeView(chip);
                                        isDataChanged = true;
                                    });
                                    
                                    // 添加到ChipGroup
                                    chipGroupTags.addView(chip);
                            }
                        }
                        }
                    }
                } catch (Exception e) {
                    android.util.Log.e("PostActivity", "加载标签时失败", e);
                }
                
                // 加载图片
                try {
                    if (draft.getImages() != null && !draft.getImages().isEmpty()) {
                        if (selectedImages == null) {
                            selectedImages = new ArrayList<>();
                        }
                        
                        if (imageAdapter != null) {
                            selectedImages.clear();
                            
                            for (int i = 0; i < draft.getImages().size(); i++) {
                                Uri uri = draft.getImages().get(i);
                                try {
                                    if (uri != null) {
                                        // 检查URI是否有效，如果权限失效则尝试重新获取权限
                                        try {
                                            android.content.ContentResolver resolver = getContentResolver();
                                            if (resolver != null) {
                                                java.io.InputStream is = resolver.openInputStream(uri);
                                                if (is != null) {
                                                    is.close();
                                                }
                                            }
                                        } catch (SecurityException e) {
                                            // 权限问题，尝试获取持久化权限
                                            try {
                                                getContentResolver().takePersistableUriPermission(
                                                    uri, 
                                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                                                );
                                            } catch (Exception e2) {
                                                // 继续添加，即使没有权限
                                            }
                                        } catch (Exception e) {
                                            // 继续尝试添加
                                        }
                                        
                                        selectedImages.add(uri);
                                    }
                                } catch (Exception e) {
                                    android.util.Log.e("PostActivity", "添加图片URI时失败", e);
                                }
                            }
                            
                                imageAdapter.notifyDataSetChanged();
                            }
                    }
                } catch (Exception e) {
                    android.util.Log.e("PostActivity", "加载图片时发生异常", e);
                }
                
                // 加载位置信息
                try {
                    if (draft.getLocation() != null) {
                        currentLocation = draft.getLocation();
                        currentLocationAddress = draft.getLocationAddress();
                        if (textViewLocation != null && currentLocationAddress != null && !currentLocationAddress.isEmpty()) {
                            textViewLocation.setText(currentLocationAddress);
                            textViewLocation.setVisibility(View.VISIBLE);
                        }
                    }
                } catch (Exception e) {
                    android.util.Log.e("PostActivity", "加载位置信息失败", e);
                }
                
                isDataChanged = false;
            }
        } catch (Exception e) {
            android.util.Log.e("PostActivity", "加载草稿失败", e);
            Toast.makeText(this, "加载草稿失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * 保存草稿
     */
    private void saveDraft() {
        if (dbHelper == null) {
            android.util.Log.e("PostActivity", "dbHelper为null，无法保存草稿");
            Toast.makeText(this, "数据库未初始化", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            Draft draft = new Draft();
            draft.setId(currentDraftId);
            
            String title = "";
            String content = "";
            if (editTextTitle != null) {
                title = editTextTitle.getText().toString().trim();
            }
            if (editTextContent != null) {
                content = editTextContent.getText().toString().trim();
            }
            
            draft.setTitle(title);
            draft.setContent(content);
            
            // 过滤掉空的图片URI
            List<Uri> validImages = new ArrayList<>();
            if (selectedImages != null) {
                for (Uri uri : selectedImages) {
                    if (uri != null) {
                        validImages.add(uri);
                    }
                }
            }
            draft.setImages(validImages);
        
            // 保存标签（从selectedTags列表获取，不再从文本中提取）
            List<String> tags = selectedTags != null ? new ArrayList<>(selectedTags) : new ArrayList<>();
            // 从文本内容中提取@对象（标签不再从文本中提取）
            List<String> mentions = extractMentions(content);
            draft.setTags(tags);
            draft.setMentions(mentions);
        
            // 保存位置信息
            draft.setLocation(currentLocation);
            draft.setLocationAddress(currentLocationAddress);
            draft.setLocationData(currentLocationData);
            
            long savedDraftId = dbHelper.saveDraft(draft);
            
            if (savedDraftId > 0) {
                currentDraftId = savedDraftId;
                isDataChanged = false;
                
                // 验证数据库中实际保存的图片数量
                try {
                    int actualImageCount = dbHelper.getImageCount(savedDraftId);
                    
                    if (actualImageCount == 0 && validImages.size() > 0) {
                        android.util.Log.e("PostActivity", "图片保存失败！期望保存 " + validImages.size() + " 张，但数据库中为 0");
                        Toast.makeText(this, "草稿已保存，但图片保存失败（" + validImages.size() + "张）", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "草稿已保存（" + actualImageCount + "张图片）", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    android.util.Log.e("PostActivity", "验证图片保存时出错", e);
                    Toast.makeText(this, "草稿已保存", Toast.LENGTH_SHORT).show();
                }
            } else {
                android.util.Log.e("PostActivity", "草稿保存失败，返回ID无效: " + savedDraftId);
                Toast.makeText(this, "草稿保存失败", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            android.util.Log.e("PostActivity", "保存草稿失败", e);
            String errorMsg = e.getMessage();
            if (errorMsg == null || errorMsg.isEmpty()) {
                errorMsg = e.getClass().getSimpleName();
            }
            Toast.makeText(this, "保存草稿失败: " + errorMsg, Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * 检查是否有内容
     */
    private boolean hasContent() {
        String title = editTextTitle != null ? editTextTitle.getText().toString().trim() : "";
        String content = editTextContent != null ? editTextContent.getText().toString().trim() : "";
        boolean hasTags = selectedTags != null && !selectedTags.isEmpty();
        return !title.isEmpty() || !content.isEmpty() || !selectedImages.isEmpty() || 
               hasTags || currentLocation != null;
    }
    
    /**
     * 从文本内容中提取标签（以#开头的词）
     */
    private List<String> extractTags(String content) {
        List<String> tags = new ArrayList<>();
        if (content == null || content.isEmpty()) {
            return tags;
        }
        String[] words = content.split("\\s+");
        for (String word : words) {
            if (word.startsWith("#")) {
                tags.add(word);
            }
        }
        return tags;
    }
    
    /**
     * 从文本内容中提取@对象（以@开头的词）
     */
    private List<String> extractMentions(String content) {
        List<String> mentions = new ArrayList<>();
        if (content == null || content.isEmpty()) {
            return mentions;
        }
        String[] words = content.split("\\s+");
        for (String word : words) {
            if (word.startsWith("@")) {
                mentions.add(word);
            }
        }
        return mentions;
    }
    
    @Override
    public void onBackPressed() {
        if (isDataChanged && hasContent()) {
            showSaveDialog();
        } else {
            super.onBackPressed();
        }
    }
    
    /**
     * 显示保存对话框
     */
    private void showSaveDialog() {
        new AlertDialog.Builder(this)
                .setTitle("保存草稿")
                .setMessage("是否保存当前编辑内容？")
                .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveDraft();
                        finish();
                    }
                })
                .setNegativeButton("不保存", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNeutralButton("取消", null)
                .show();
    }
    
    /**
     * 显示好友选择对话框
     */
    private void showFriendSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择好友");
        
        // 加载对话框布局
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_select_friend, null);
        builder.setView(dialogView);
        
        RecyclerView recyclerViewFriends = dialogView.findViewById(R.id.recyclerViewFriends);
        
        // 创建对话框
        AlertDialog dialog = builder.create();
        
        // 设置RecyclerView
        List<String> friends = new ArrayList<>();
        MentionOptionAdapter friendAdapter = new MentionOptionAdapter(friends, friend -> {
            // 用户选择了好友
            String mention = friend.startsWith("@") ? friend : "@" + friend;
            addMention(mention);
            isDataChanged = true;
            // 选择后关闭对话框
            dialog.dismiss();
        });
        recyclerViewFriends.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        recyclerViewFriends.setAdapter(friendAdapter);
        
        // 加载好友列表
        loadAndDisplayFriends(recyclerViewFriends, friendAdapter);
        
        builder.setPositiveButton("关闭", null);
        dialog.show();
    }
    
    /**
     * 加载并显示好友列表
     */
    private void loadAndDisplayFriends(RecyclerView recyclerView, MentionOptionAdapter adapter) {
        if (appDatabase == null || adapter == null) {
            return;
        }
        
        try {
            android.database.sqlite.SQLiteDatabase db = appDatabase.getReadableDatabase();
            android.database.Cursor cursor = FriendDao.queryAll(db);
            List<String> friends = FriendDao.getFriendNamesFromCursor(cursor);
            cursor.close();
            
            adapter.updateData(friends);
            android.util.Log.d("PostActivity", "加载了 " + friends.size() + " 个好友");
        } catch (Exception e) {
            android.util.Log.e("PostActivity", "加载好友列表失败", e);
            e.printStackTrace();
        }
    }
    
    /**
     * 显示标签搜索对话框
     */
    private void showTagSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择或输入标签");
        
        // 加载对话框布局
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_search_tag, null);
        builder.setView(dialogView);
        
        EditText editTextSearchTag = dialogView.findViewById(R.id.editTextSearchTag);
        RecyclerView recyclerViewTagResults = dialogView.findViewById(R.id.recyclerViewTagResults);
        
        // 创建对话框
        builder.setPositiveButton("确定", null); // 先设置为null，稍后设置监听器
        builder.setNegativeButton("取消", null);
        AlertDialog dialog = builder.create();
        
        // 设置RecyclerView
        List<String> tagResults = new ArrayList<>();
        MentionOptionAdapter tagAdapter = new MentionOptionAdapter(tagResults, tag -> {
            // 用户从列表选择了标签
            try {
            String tagWithHash = tag.startsWith("#") ? tag : "#" + tag;
            addTag(tagWithHash);
            isDataChanged = true;
                // 选择后关闭对话框
                dialog.dismiss();
            } catch (Exception e) {
                android.util.Log.e("PostActivity", "选择标签时出错", e);
                Toast.makeText(PostActivity.this, "添加标签失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        recyclerViewTagResults.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        recyclerViewTagResults.setAdapter(tagAdapter);
        
        // 加载所有标签
        loadAndDisplayTags(recyclerViewTagResults, tagAdapter, "");
        
        // 搜索功能
        editTextSearchTag.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().trim();
                loadAndDisplayTags(recyclerViewTagResults, tagAdapter, keyword);
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // 设置确定按钮的点击监听器
        dialog.setOnShowListener(dialogInterface -> {
            android.widget.Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                // 获取用户输入的标签
                String inputTag = editTextSearchTag.getText().toString().trim();
                
                if (inputTag.isEmpty()) {
                    // 如果输入框为空，检查是否从列表选择了标签
                    if (tagResults.isEmpty()) {
                        Toast.makeText(PostActivity.this, "请输入标签或从列表中选择", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // 如果没有输入但有搜索结果，提示用户选择
                    Toast.makeText(PostActivity.this, "请从列表中选择标签或输入新标签", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // 移除#号（如果有），统一处理
                String tagName = inputTag.startsWith("#") ? inputTag.substring(1) : inputTag;
                tagName = tagName.trim();
                
                if (tagName.isEmpty()) {
                    Toast.makeText(PostActivity.this, "标签不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // 检查标签是否已存在于标签库中
                boolean tagExists = false;
                if (appDatabase != null) {
                    try {
                        android.database.sqlite.SQLiteDatabase db = appDatabase.getReadableDatabase();
                        android.database.Cursor cursor = TagDao.searchTags(db, tagName);
                        if (cursor != null) {
                            // 检查是否有完全匹配的标签
                            while (cursor.moveToNext()) {
                                String existingTag = cursor.getString(cursor.getColumnIndexOrThrow(TagDao.COL_TAGNAME));
                                if (existingTag != null && existingTag.equalsIgnoreCase(tagName)) {
                                    tagExists = true;
                                    break;
                                }
                            }
                            cursor.close();
                        }
                    } catch (Exception e) {
                        android.util.Log.e("PostActivity", "检查标签是否存在时出错", e);
                    }
                }
                
                // 如果标签不存在，添加到标签库
                if (!tagExists && appDatabase != null) {
                    try {
                        android.database.sqlite.SQLiteDatabase db = appDatabase.getWritableDatabase();
                        TagDao.insert(db, tagName);
                    } catch (Exception e) {
                        android.util.Log.e("PostActivity", "添加标签到标签库时出错", e);
                    }
                }
                
                // 添加标签到chip中
                String tagWithHash = "#" + tagName;
                addTag(tagWithHash);
                isDataChanged = true;
                
                // 关闭对话框
                dialog.dismiss();
            });
        });
        
        dialog.show();
        
        // 对话框显示时自动弹出键盘
        editTextSearchTag.requestFocus();
        showKeyboard(editTextSearchTag);
    }
    
    /**
     * 加载并显示标签
     */
    private void loadAndDisplayTags(RecyclerView recyclerView, MentionOptionAdapter adapter, String keyword) {
        if (appDatabase == null || adapter == null) {
            return;
        }
        
        try {
            android.database.sqlite.SQLiteDatabase db = appDatabase.getReadableDatabase();
            if (db == null) {
                return;
            }
            
            android.database.Cursor cursor = TagDao.searchTags(db, keyword);
            if (cursor == null) {
                return;
            }
            
            List<String> tags = TagDao.getTagNamesFromCursor(cursor);
            cursor.close();
            adapter.updateData(tags);
        } catch (Exception e) {
            android.util.Log.e("PostActivity", "加载标签失败", e);
            Toast.makeText(this, "加载标签失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 一键生成长文
     */
    private void generateAIText() {
        if (selectedImages == null || selectedImages.isEmpty()) {
            // 使用Snackbar在底部显示提示
            View rootView = findViewById(android.R.id.content);
            if (rootView != null) {
                Snackbar snackbar = Snackbar.make(rootView, "请至少上传一张图片", Snackbar.LENGTH_LONG);
                snackbar.setBackgroundTint(android.graphics.Color.parseColor("#FF4444"));
                snackbar.setTextColor(android.graphics.Color.WHITE);
                snackbar.show();
            } else {
                Toast.makeText(this, "请至少上传一张图片", Toast.LENGTH_LONG).show();
            }
            return;
        }
        
        try {
            // 启动AI生成页面
            Intent intent = AIGenerateActivity.createIntent(this, selectedImages);
            startActivityForResult(intent, REQUEST_CODE_AI_GENERATE);
        } catch (Exception e) {
            android.util.Log.e("PostActivity", "启动AI生成页面失败", e);
            Toast.makeText(this, "启动AI生成页面失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * 更新标题字数统计
     */
    private void updateTitleCount(int currentCount) {
        if (textViewTitleCount != null) {
            int maxCount = 100;
            String countText = currentCount + "/" + maxCount;
            textViewTitleCount.setText(countText);
            
            // 如果接近或超过限制，改变文字颜色
            if (currentCount >= maxCount) {
                textViewTitleCount.setTextColor(android.graphics.Color.parseColor("#FF4444")); // 红色
            } else if (currentCount >= maxCount * 0.9) {
                textViewTitleCount.setTextColor(android.graphics.Color.parseColor("#FFA500")); // 橙色
            } else {
                textViewTitleCount.setTextColor(android.graphics.Color.parseColor("#808080")); // 灰色
            }
        }
    }
    
    /**
     * 更新内容字数统计
     */
    private void updateContentCount(int currentCount) {
        if (textViewContentCount != null) {
            int maxCount = 2000;
            String countText = currentCount + "/" + maxCount;
            textViewContentCount.setText(countText);
            
            // 如果接近或超过限制，改变文字颜色
            if (currentCount >= maxCount) {
                textViewContentCount.setTextColor(android.graphics.Color.parseColor("#FF4444")); // 红色
            } else if (currentCount >= maxCount * 0.9) {
                textViewContentCount.setTextColor(android.graphics.Color.parseColor("#FFA500")); // 橙色
            } else {
                textViewContentCount.setTextColor(android.graphics.Color.parseColor("#808080")); // 灰色
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        // 释放定位资源
        if (locationActivity != null) {
            locationActivity.destroy();
            locationActivity = null;
        }
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
        if (appDatabase != null) {
            appDatabase.close();
        }
    }
}

