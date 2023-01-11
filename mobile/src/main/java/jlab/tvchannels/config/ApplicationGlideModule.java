package jlab.tvchannels.config;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator;
import com.bumptech.glide.module.AppGlideModule;

public class ApplicationGlideModule extends AppGlideModule {

    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        MemorySizeCalculator calculator = new MemorySizeCalculator.Builder(context)
                .setMemoryCacheScreens(2)
                .build();
        builder.setMemoryCache(new LruResourceCache(calculator.getMemoryCacheSize()));
    }
}