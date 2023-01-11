package jlab.tvchannels.component;
/*
 * Created by Javier on 24/06/2021.
 */

import android.content.Context;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import jlab.tvchannels.R;

public class SwipeLoadingLayout extends SwipeRefreshLayout {
    public SwipeLoadingLayout(@NonNull Context context) {
        super(context);
        setColorSchemeColors(getResources().getColor(R.color.colorPrimaryDark));
    }

    public SwipeLoadingLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setColorSchemeColors(getResources().getColor(R.color.colorPrimaryDark));
    }

    @Override
    public boolean canChildScrollUp() {
        return true;
    }

    public void show () {
        setRefreshing(true);
    }

    public void hide () {
        setRefreshing(false);
    }
}
