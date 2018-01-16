package com.android.volvocars.sensortestview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

/**
 * Created by pwinzell on 2017-12-22.
 */

public class SpeedRpmView extends View {

    public SpeedRpmView(Context context) {
        super(context);
    }

    private Paint getPaintObject(){
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);

        return paint;
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        Paint painter = getPaintObject();
        // canvas.
    }
}
