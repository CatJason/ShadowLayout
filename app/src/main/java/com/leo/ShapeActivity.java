package com.leo;

import android.os.Bundle;
import com.leo.databinding.ActivityShapeBinding;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

/**
 * shape功能的各项使用
 */
public class ShapeActivity extends AppCompatActivity {
    ActivityShapeBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_shape);
    }
}
